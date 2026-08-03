package com.planeguardian.assets.db;

import com.planeguardian.assets.model.MaterialShaderRef;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-backed repository for {@link MaterialShaderRef} records.
 *
 * <p>A {@link MaterialShaderRef} links a named material inside an asset file
 * to a custom shader (or standard JME3 shader), together with any
 * per-instance parameter overrides.</p>
 */
@Slf4j
public class MaterialShaderRefRepository implements Repository<MaterialShaderRef, Long> {

    @Override
    public void save(MaterialShaderRef ref) {
        if (ref.getId() == null) {
            insert(ref);
        } else {
            update(ref);
        }
    }

    @Override
    public Optional<MaterialShaderRef> findById(Long id) {
        String sql = "SELECT * FROM material_shader_refs WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find material shader ref by id={}", id, e);
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /** Returns all {@link MaterialShaderRef} records for the given asset. */
    public List<MaterialShaderRef> findByAssetId(Long assetId) {
        String sql = "SELECT * FROM material_shader_refs WHERE asset_id = ? ORDER BY material_name";
        List<MaterialShaderRef> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query material shader refs for asset id={}", assetId, e);
            throw new RuntimeException(e);
        }
        return results;
    }

    /** Returns all {@link MaterialShaderRef} records across all assets. */
    @Override
    public List<MaterialShaderRef> findAll() {
        String sql = "SELECT * FROM material_shader_refs ORDER BY asset_id, material_name";
        List<MaterialShaderRef> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to query all material shader refs", e);
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM material_shader_refs WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            log.debug("Deleted material shader ref id={}", id);
        } catch (SQLException e) {
            log.error("Failed to delete material shader ref id={}", id, e);
            throw new RuntimeException(e);
        }
    }

    /** Removes all shader refs for the given asset. */
    public void deleteByAssetId(Long assetId) {
        String sql = "DELETE FROM material_shader_refs WHERE asset_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            int rows = ps.executeUpdate();
            log.debug("Deleted {} material shader ref(s) for asset id={}", rows, assetId);
        } catch (SQLException e) {
            log.error("Failed to delete material shader refs for asset id={}", assetId, e);
            throw new RuntimeException(e);
        }
    }

    // ---- private helpers --------------------------------------------------

    private void insert(MaterialShaderRef ref) {
        String sql = """
                INSERT INTO material_shader_refs
                    (asset_id, material_name, shader_id, shader_parameters)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ref.getAssetId());
            ps.setString(2, ref.getMaterialName());
            ps.setString(3, ref.getShaderId());
            ps.setString(4, ref.getShaderParameters());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    ref.setId(keys.getLong(1));
                }
            }
            log.debug("Inserted material shader ref: asset={} material={} shader={}",
                    ref.getAssetId(), ref.getMaterialName(), ref.getShaderId());
        } catch (SQLException e) {
            log.error("Failed to insert material shader ref for asset id={}", ref.getAssetId(), e);
            throw new RuntimeException(e);
        }
    }

    private void update(MaterialShaderRef ref) {
        String sql = """
                UPDATE material_shader_refs
                SET asset_id = ?, material_name = ?, shader_id = ?, shader_parameters = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ref.getAssetId());
            ps.setString(2, ref.getMaterialName());
            ps.setString(3, ref.getShaderId());
            ps.setString(4, ref.getShaderParameters());
            ps.setLong(5, ref.getId());
            ps.executeUpdate();
            log.debug("Updated material shader ref id={}", ref.getId());
        } catch (SQLException e) {
            log.error("Failed to update material shader ref id={}", ref.getId(), e);
            throw new RuntimeException(e);
        }
    }

    private MaterialShaderRef mapRow(ResultSet rs) throws SQLException {
        return MaterialShaderRef.builder()
                .id(rs.getLong("id"))
                .assetId(rs.getLong("asset_id"))
                .materialName(rs.getString("material_name"))
                .shaderId(rs.getString("shader_id"))
                .shaderParameters(rs.getString("shader_parameters"))
                .build();
    }
}
