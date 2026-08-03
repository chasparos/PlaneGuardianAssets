package com.planeguardian.assets.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.planeguardian.assets.db.AssetRepository;
import com.planeguardian.assets.db.CustomShaderRepository;
import com.planeguardian.assets.db.MaterialShaderRefRepository;
import com.planeguardian.assets.gltf.GltfExtrasInjector;
import com.planeguardian.assets.model.Asset;
import com.planeguardian.assets.model.CustomShader;
import com.planeguardian.assets.model.MaterialShaderRef;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Exports the asset library to a directory:
 * <ul>
 *   <li>Copies every asset file flagged as <em>Include in Export</em>.</li>
 *   <li>Injects {@code extras} blocks into GLTF/GLB materials for custom shaders.</li>
 *   <li>Writes {@code asset_index.json} consumed by the game runtime.</li>
 *   <li>Writes {@code shader_registry.json} with consolidated custom-shader definitions.</li>
 * </ul>
 */
@Slf4j
public final class ExportManager {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    private ExportManager() {}

    /**
     * Opens a directory chooser and then exports. Intended to be called from
     * the Event Dispatch Thread.
     */
    public static void exportLibrary(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Export Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path exportDir = chooser.getSelectedFile().toPath();

        // Run on a background thread to avoid freezing the EDT
        Thread worker = new Thread(() -> {
            try {
                exportToDirectory(parent, exportDir);
            } catch (Exception e) {
                log.error("Export failed", e);
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(parent,
                                "Export failed:\n" + e.getMessage(),
                                "Export Error", JOptionPane.ERROR_MESSAGE));
            }
        }, "ExportWorker");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Performs the actual export to {@code exportDir}.
     * Safe to call from any thread; Swing dialogs are posted to the EDT.
     */
    public static void exportToDirectory(Component parent, Path exportDir) throws IOException {
        AssetRepository assetRepo = new AssetRepository();
        MaterialShaderRefRepository shaderRefRepo = new MaterialShaderRefRepository();
        CustomShaderRepository customShaderRepo = new CustomShaderRepository();

        List<Asset> assets = assetRepo.findIncludedInExport();

        if (assets.isEmpty()) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(parent,
                            "No assets are marked for export.\n" +
                            "Enable 'Include in Export' on at least one asset.",
                            "Nothing to Export", JOptionPane.INFORMATION_MESSAGE));
            return;
        }

        Files.createDirectories(exportDir);
        Path assetsDir = exportDir.resolve("assets");
        Files.createDirectories(assetsDir);

        List<AssetIndexEntry> entries = new ArrayList<>();
        // Consolidated map of shaderId → ShaderRegistryEntry (deduplication)
        Map<String, ShaderRegistryEntry> shaderMap = new LinkedHashMap<>();
        int exported = 0;
        int skipped  = 0;

        for (Asset asset : assets) {
            String filePath = asset.getFilePath();
            if (filePath == null || filePath.isBlank()) {
                log.warn("Asset '{}' (id={}) has no file path – skipping", asset.getName(), asset.getId());
                skipped++;
                continue;
            }

            Path source = Path.of(filePath);
            if (!Files.exists(source)) {
                log.warn("Asset file not found: {} – skipping", filePath);
                skipped++;
                continue;
            }

            String safeName = asset.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String ext = extension(source.getFileName().toString());
            String exportedFileName = safeName + "_" + asset.getId() + ext;
            Path target = assetsDir.resolve(exportedFileName);

            // Load material-shader refs for this asset
            List<MaterialShaderRef> refs = shaderRefRepo.findByAssetId(asset.getId());

            // Inject extras into GLTF/GLB (or plain copy for other formats)
            try {
                GltfExtrasInjector.inject(source, target, refs);
            } catch (IOException e) {
                log.warn("GltfExtrasInjector failed for '{}': {} – falling back to plain copy",
                        source, e.getMessage());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // Build per-asset materialShaders list and accumulate shader registry entries
            List<MaterialShaderRefEntry> matShaderEntries = new ArrayList<>();
            for (MaterialShaderRef ref : refs) {
                matShaderEntries.add(MaterialShaderRefEntry.builder()
                        .materialName(ref.getMaterialName())
                        .shaderId(ref.getShaderId())
                        .shaderParameters(ref.getShaderParameters())
                        .build());

                // Accumulate custom (non-JME3) shader definitions into the registry
                if (!shaderMap.containsKey(ref.getShaderId())) {
                    customShaderRepo.findByShaderId(ref.getShaderId()).ifPresent(shader -> {
                        if (!shader.isStandardJme3()) {
                            shaderMap.put(shader.getShaderId(), ShaderRegistryEntry.builder()
                                    .shaderId(shader.getShaderId())
                                    .displayName(shader.getDisplayName())
                                    .description(shader.getDescription())
                                    .parameterSchema(shader.getParameterSchema())
                                    .build());
                        }
                    });
                }
            }

            entries.add(AssetIndexEntry.builder()
                    .id(asset.getId())
                    .name(asset.getName())
                    .assetType(asset.getAssetType())
                    .exportedPath("assets/" + exportedFileName)
                    .originalPath(filePath)
                    .metadata(asset.getMetadata())
                    .materialShaders(matShaderEntries.isEmpty() ? null : matShaderEntries)
                    .build());
            exported++;
        }

        // Build and write shader_registry.json
        List<ShaderRegistryEntry> shaderList = new ArrayList<>(shaderMap.values());
        ShaderRegistry shaderRegistry = ShaderRegistry.builder()
                .version("1.0")
                .exportDate(LocalDateTime.now())
                .totalShaders(shaderList.size())
                .shaders(shaderList)
                .build();
        Files.writeString(exportDir.resolve("shader_registry.json"), GSON.toJson(shaderRegistry));
        log.info("Wrote shader_registry.json with {} custom shader(s)", shaderList.size());

        // Build and write asset_index.json (includes the shader registry for convenience)
        AssetIndex index = AssetIndex.builder()
                .version("1.0")
                .exportDate(LocalDateTime.now())
                .totalAssets(exported)
                .assets(entries)
                .shaderRegistry(shaderRegistry)
                .build();
        Files.writeString(exportDir.resolve("asset_index.json"), GSON.toJson(index));
        log.info("Export complete – {} exported, {} skipped → {}", exported, skipped, exportDir);

        int finalExported = exported;
        int finalSkipped  = skipped;
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(parent,
                        String.format("Export complete!%nExported: %d  Skipped: %d%nOutput: %s",
                                finalExported, finalSkipped, exportDir.toAbsolutePath()),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE));
    }

    private static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    /** Serialises {@link LocalDateTime} as an ISO-8601 string. */
    private static final class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            out.value(value != null ? FORMATTER.format(value) : null);
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            String s = in.nextString();
            return s != null ? LocalDateTime.parse(s, FORMATTER) : null;
        }
    }
}

