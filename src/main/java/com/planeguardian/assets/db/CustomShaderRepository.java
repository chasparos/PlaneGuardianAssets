package com.planeguardian.assets.db;

import com.planeguardian.assets.model.CustomShader;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-backed CRUD repository for {@link CustomShader} records.
 */
@Slf4j
public class CustomShaderRepository implements Repository<CustomShader, Long> {

    @Override
    public void save(CustomShader shader) {
        if (shader.getId() == null) {
            insert(shader);
        } else {
            update(shader);
        }
    }

    @Override
    public Optional<CustomShader> findById(Long id) {
        String sql = "SELECT * FROM custom_shaders WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find custom shader by id={}", id, e);
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /**
     * Looks up a shader by its string identifier (e.g. {@code "procedural_wind_bark"}).
     */
    public Optional<CustomShader> findByShaderId(String shaderId) {
        String sql = "SELECT * FROM custom_shaders WHERE shader_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shaderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find custom shader by shaderId={}", shaderId, e);
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /** Returns all registered shaders ordered by display name. */
    @Override
    public List<CustomShader> findAll() {
        return executeQuery("SELECT * FROM custom_shaders ORDER BY display_name, shader_id");
    }

    /**
     * Returns only non-standard (custom) shaders, i.e. those that will be
     * written to {@code shader_registry.json} during export.
     */
    public List<CustomShader> findCustomOnly() {
        return executeQuery(
                "SELECT * FROM custom_shaders WHERE is_standard_jme3 = FALSE ORDER BY display_name, shader_id");
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM custom_shaders WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            log.debug("Deleted custom shader id={}", id);
        } catch (SQLException e) {
            log.error("Failed to delete custom shader id={}", id, e);
            throw new RuntimeException(e);
        }
    }

    // ---- private helpers --------------------------------------------------

    private void insert(CustomShader shader) {
        String sql = """
                INSERT INTO custom_shaders
                    (shader_id, display_name, description, parameter_schema,
                     is_standard_jme3, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime now = LocalDateTime.now();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, shader.getShaderId());
            ps.setString(2, shader.getDisplayName());
            ps.setString(3, shader.getDescription());
            ps.setString(4, shader.getParameterSchema());
            ps.setBoolean(5, shader.isStandardJme3());
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setTimestamp(7, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    shader.setId(keys.getLong(1));
                }
            }
            shader.setCreatedAt(now);
            shader.setUpdatedAt(now);
            log.debug("Inserted custom shader: {}", shader.getShaderId());
        } catch (SQLException e) {
            log.error("Failed to insert custom shader: {}", shader.getShaderId(), e);
            throw new RuntimeException(e);
        }
    }

    private void update(CustomShader shader) {
        String sql = """
                UPDATE custom_shaders
                SET shader_id = ?, display_name = ?, description = ?,
                    parameter_schema = ?, is_standard_jme3 = ?, updated_at = ?
                WHERE id = ?
                """;
        LocalDateTime now = LocalDateTime.now();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, shader.getShaderId());
            ps.setString(2, shader.getDisplayName());
            ps.setString(3, shader.getDescription());
            ps.setString(4, shader.getParameterSchema());
            ps.setBoolean(5, shader.isStandardJme3());
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setLong(7, shader.getId());
            ps.executeUpdate();
            shader.setUpdatedAt(now);
            log.debug("Updated custom shader: {}", shader.getShaderId());
        } catch (SQLException e) {
            log.error("Failed to update custom shader: {}", shader.getShaderId(), e);
            throw new RuntimeException(e);
        }
    }

    private List<CustomShader> executeQuery(String sql) {
        List<CustomShader> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to query custom_shaders: {}", sql, e);
            throw new RuntimeException(e);
        }
        return results;
    }

    private CustomShader mapRow(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return CustomShader.builder()
                .id(rs.getLong("id"))
                .shaderId(rs.getString("shader_id"))
                .displayName(rs.getString("display_name"))
                .description(rs.getString("description"))
                .parameterSchema(rs.getString("parameter_schema"))
                .standardJme3(rs.getBoolean("is_standard_jme3"))
                .createdAt(createdTs != null ? createdTs.toLocalDateTime() : null)
                .updatedAt(updatedTs != null ? updatedTs.toLocalDateTime() : null)
                .build();
    }
}
