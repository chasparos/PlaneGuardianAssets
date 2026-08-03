package com.planeguardian.assets.db;

import com.planeguardian.assets.model.Asset;
import com.planeguardian.assets.model.AssetType;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-backed CRUD repository for {@link Asset} records.
 * Each method acquires its own connection from the shared {@link DatabaseManager}
 * data source and closes it afterwards.
 */
@Slf4j
public class AssetRepository implements Repository<Asset, Long> {

    /** Persist a new asset (id == null) or update an existing one. */
    @Override
    public void save(Asset asset) {
        if (asset.getId() == null) {
            insert(asset);
        } else {
            update(asset);
        }
    }

    @Override
    public Optional<Asset> findById(Long id) {
        String sql = "SELECT * FROM assets WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to find asset by id={}", id, e);
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /** Returns all assets ordered by name. */
    @Override
    public List<Asset> findAll() {
        return executeQuery("SELECT * FROM assets ORDER BY name");
    }

    /** Returns only assets flagged for export, ordered by name. */
    public List<Asset> findIncludedInExport() {
        return executeQuery("SELECT * FROM assets WHERE include_in_export = TRUE ORDER BY name");
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM assets WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            log.debug("Deleted asset id={}", id);
        } catch (SQLException e) {
            log.error("Failed to delete asset id={}", id, e);
            throw new RuntimeException(e);
        }
    }

    // ---- private helpers --------------------------------------------------

    private void insert(Asset asset) {
        String sql = """
                INSERT INTO assets
                    (name, file_path, asset_type, include_in_export, metadata, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime now = LocalDateTime.now();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, asset.getName());
            ps.setString(2, asset.getFilePath());
            ps.setString(3, asset.getAssetType() != null ? asset.getAssetType().name() : null);
            ps.setBoolean(4, asset.isIncludeInExport());
            ps.setString(5, asset.getMetadata());
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setTimestamp(7, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    asset.setId(keys.getLong(1));
                }
            }
            asset.setCreatedAt(now);
            asset.setUpdatedAt(now);
            log.debug("Inserted asset: {}", asset.getName());
        } catch (SQLException e) {
            log.error("Failed to insert asset: {}", asset.getName(), e);
            throw new RuntimeException(e);
        }
    }

    private void update(Asset asset) {
        String sql = """
                UPDATE assets
                SET name = ?, file_path = ?, asset_type = ?, include_in_export = ?,
                    metadata = ?, updated_at = ?
                WHERE id = ?
                """;
        LocalDateTime now = LocalDateTime.now();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, asset.getName());
            ps.setString(2, asset.getFilePath());
            ps.setString(3, asset.getAssetType() != null ? asset.getAssetType().name() : null);
            ps.setBoolean(4, asset.isIncludeInExport());
            ps.setString(5, asset.getMetadata());
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setLong(7, asset.getId());
            ps.executeUpdate();
            asset.setUpdatedAt(now);
            log.debug("Updated asset: {}", asset.getName());
        } catch (SQLException e) {
            log.error("Failed to update asset: {}", asset.getName(), e);
            throw new RuntimeException(e);
        }
    }

    private List<Asset> executeQuery(String sql) {
        List<Asset> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to query assets: {}", sql, e);
            throw new RuntimeException(e);
        }
        return results;
    }

    private Asset mapRow(ResultSet rs) throws SQLException {
        String typeStr = rs.getString("asset_type");
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return Asset.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .filePath(rs.getString("file_path"))
                .assetType(typeStr != null ? AssetType.valueOf(typeStr) : null)
                .includeInExport(rs.getBoolean("include_in_export"))
                .metadata(rs.getString("metadata"))
                .createdAt(createdTs != null ? createdTs.toLocalDateTime() : null)
                .updatedAt(updatedTs != null ? updatedTs.toLocalDateTime() : null)
                .build();
    }
}

