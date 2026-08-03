package com.planeguardian.assets.gltf;

import com.google.gson.*;
import com.planeguardian.assets.model.MaterialShaderRef;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Injects {@code extras} blocks into GLTF material definitions so that
 * custom-shader associations are embedded in the exported file.
 *
 * <p>The injected structure looks like:</p>
 * <pre>{@code
 * "extras": {
 *   "custom_shader_id": "procedural_wind_bark",
 *   "shader_parameters": {
 *     "wind_sway_amplitude": 0.15,
 *     "wind_speed_multiplier": 1.2
 *   }
 * }
 * }</pre>
 *
 * <p>Supports both plain {@code .gltf} (JSON text) and binary {@code .glb}
 * files.  For GLB the JSON chunk is extracted, modified, and re-packed
 * according to the
 * <a href="https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html#glb-file-format-specification">
 * glTF 2.0 GLB specification</a>.</p>
 *
 * <p>Existing {@code extras} fields on materials are <em>merged</em>:
 * {@code custom_shader_id} and {@code shader_parameters} are written or
 * overwritten, other keys are left untouched.</p>
 */
@Slf4j
public final class GltfExtrasInjector {

    // GLB magic / chunk-type constants (little-endian)
    private static final int GLB_MAGIC       = 0x46546C67; // "glTF"
    private static final int CHUNK_TYPE_JSON = 0x4E4F534A; // "JSON"

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GltfExtrasInjector() {}

    /**
     * Reads {@code sourcePath}, injects the shader extras described by
     * {@code refs}, and writes the result to {@code targetPath}.
     * If {@code refs} is empty the file is copied without modification.
     *
     * @param sourcePath the original GLTF or GLB file
     * @param targetPath destination for the modified file (may equal sourcePath)
     * @param refs       the material-to-shader bindings to inject
     * @throws IOException if reading or writing fails
     */
    public static void inject(Path sourcePath, Path targetPath,
                               List<MaterialShaderRef> refs) throws IOException {
        if (refs == null || refs.isEmpty()) {
            if (!sourcePath.equals(targetPath)) {
                Files.copy(sourcePath, targetPath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return;
        }

        String filename = sourcePath.getFileName().toString().toLowerCase();
        if (filename.endsWith(".glb")) {
            injectGlb(sourcePath, targetPath, refs);
        } else if (filename.endsWith(".gltf")) {
            injectGltf(sourcePath, targetPath, refs);
        } else {
            log.warn("GltfExtrasInjector: unsupported extension for '{}' – copying as-is", sourcePath);
            if (!sourcePath.equals(targetPath)) {
                Files.copy(sourcePath, targetPath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    // ---- GLTF (plain JSON) ------------------------------------------------

    private static void injectGltf(Path source, Path target,
                                    List<MaterialShaderRef> refs) throws IOException {
        String jsonText = Files.readString(source, StandardCharsets.UTF_8);
        String modified = injectIntoJson(jsonText, refs);
        Files.writeString(target, modified, StandardCharsets.UTF_8);
        log.debug("Injected shader extras into GLTF: {}", target);
    }

    // ---- GLB (binary) -----------------------------------------------------

    private static void injectGlb(Path source, Path target,
                                   List<MaterialShaderRef> refs) throws IOException {
        byte[] raw = Files.readAllBytes(source);
        ByteBuffer buf = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);

        // Validate header
        int magic = buf.getInt(0);
        if (magic != GLB_MAGIC) {
            throw new IOException("Not a valid GLB file (bad magic): " + source);
        }

        // First chunk starts at offset 12
        int offset = 12;
        int jsonChunkLength = buf.getInt(offset);
        int jsonChunkType   = buf.getInt(offset + 4);

        if (jsonChunkType != CHUNK_TYPE_JSON) {
            throw new IOException("First GLB chunk is not JSON (type=" +
                    Integer.toHexString(jsonChunkType) + "): " + source);
        }

        // Extract JSON text (may have trailing space-padding)
        String jsonText = new String(raw, offset + 8, jsonChunkLength, StandardCharsets.UTF_8).trim();
        String modifiedJson = injectIntoJson(jsonText, refs);

        // Re-encode JSON chunk with 4-byte alignment (padded with spaces per spec)
        byte[] newJsonBytes = modifiedJson.getBytes(StandardCharsets.UTF_8);
        int paddedLength = alignTo4(newJsonBytes.length);
        byte[] paddedJson = new byte[paddedLength];
        System.arraycopy(newJsonBytes, 0, paddedJson, 0, newJsonBytes.length);
        for (int i = newJsonBytes.length; i < paddedLength; i++) {
            paddedJson[i] = 0x20; // space padding as per glTF spec
        }

        // Copy remaining chunks (BIN\0 etc.) unchanged
        int remainingOffset = offset + 8 + jsonChunkLength;
        int remainingLength = raw.length - remainingOffset;
        byte[] remainingChunks = new byte[remainingLength];
        if (remainingLength > 0) {
            System.arraycopy(raw, remainingOffset, remainingChunks, 0, remainingLength);
        }

        // Rebuild GLB
        int newTotalLength = 12 + 8 + paddedLength + remainingLength;
        ByteBuffer out = ByteBuffer.allocate(newTotalLength).order(ByteOrder.LITTLE_ENDIAN);
        out.putInt(GLB_MAGIC);
        out.putInt(2);               // version
        out.putInt(newTotalLength);  // updated total length
        out.putInt(paddedLength);    // JSON chunk length
        out.putInt(CHUNK_TYPE_JSON);
        out.put(paddedJson);
        out.put(remainingChunks);

        Files.write(target, out.array());
        log.debug("Injected shader extras into GLB: {}", target);
    }

    // ---- JSON modification ------------------------------------------------

    /**
     * Parses {@code jsonText} as a glTF JSON object, injects shader extras
     * into each matching material, and returns the re-serialised JSON.
     */
    static String injectIntoJson(String jsonText, List<MaterialShaderRef> refs) {
        JsonObject root = JsonParser.parseString(jsonText).getAsJsonObject();

        if (!root.has("materials")) {
            return GSON.toJson(root);
        }

        JsonArray materials = root.getAsJsonArray("materials");
        for (int i = 0; i < materials.size(); i++) {
            JsonObject material = materials.get(i).getAsJsonObject();
            String matName = material.has("name")
                    ? material.get("name").getAsString() : null;

            MaterialShaderRef ref = findRef(refs, matName, i);
            if (ref == null) continue;

            JsonObject extras = material.has("extras")
                    ? material.getAsJsonObject("extras")
                    : new JsonObject();

            extras.addProperty("custom_shader_id", ref.getShaderId());

            if (ref.getShaderParameters() != null && !ref.getShaderParameters().isBlank()) {
                try {
                    JsonObject params = JsonParser.parseString(ref.getShaderParameters())
                            .getAsJsonObject();
                    extras.add("shader_parameters", params);
                } catch (JsonParseException e) {
                    log.warn("Could not parse shader_parameters JSON for material '{}': {}",
                            matName, e.getMessage());
                    extras.addProperty("shader_parameters", ref.getShaderParameters());
                }
            }

            material.add("extras", extras);
        }

        return GSON.toJson(root);
    }

    /**
     * Finds the {@link MaterialShaderRef} matching by material name (preferred)
     * or by index (as a string) when the name is absent.
     */
    private static MaterialShaderRef findRef(List<MaterialShaderRef> refs,
                                              String materialName, int index) {
        if (materialName != null) {
            for (MaterialShaderRef ref : refs) {
                if (materialName.equals(ref.getMaterialName())) {
                    return ref;
                }
            }
        }
        String idxStr = String.valueOf(index);
        for (MaterialShaderRef ref : refs) {
            if (idxStr.equals(ref.getMaterialName())) {
                return ref;
            }
        }
        return null;
    }

    private static int alignTo4(int length) {
        return (length + 3) & ~3;
    }
}
