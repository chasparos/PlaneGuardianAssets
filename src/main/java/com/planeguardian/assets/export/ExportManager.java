package com.planeguardian.assets.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.planeguardian.assets.db.AssetRepository;
import com.planeguardian.assets.model.Asset;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports the asset library to a directory:
 * <ul>
 *   <li>Copies every asset file flagged as <em>Include in Export</em>.</li>
 *   <li>Writes {@code asset_index.json} consumed by the game runtime.</li>
 * </ul>
 */
@Slf4j
public final class ExportManager {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
        AssetRepository repo = new AssetRepository();
        List<Asset> assets = repo.findIncludedInExport();

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
        int exported = 0;
        int skipped = 0;

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

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

            entries.add(AssetIndexEntry.builder()
                    .id(asset.getId())
                    .name(asset.getName())
                    .assetType(asset.getAssetType())
                    .exportedPath("assets/" + exportedFileName)
                    .originalPath(filePath)
                    .metadata(asset.getMetadata())
                    .build());
            exported++;
        }

        AssetIndex index = AssetIndex.builder()
                .version("1.0")
                .exportDate(LocalDateTime.now())
                .totalAssets(exported)
                .assets(entries)
                .build();

        MAPPER.writeValue(exportDir.resolve("asset_index.json").toFile(), index);
        log.info("Export complete – {} exported, {} skipped → {}", exported, skipped, exportDir);

        int finalExported = exported;
        int finalSkipped = skipped;
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
}
