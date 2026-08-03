package com.planeguardian.assets.db;

import com.planeguardian.assets.model.AssetVersion;
import com.planeguardian.assets.model.VersionSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-backed repository for {@link AssetVersion} records.
 */
@Slf4j
public class AssetVersionRepository {

    /**
     * Persists a new version.  The {@code versionNumber} is auto-assigned as
     * {@code MAX(version_number) + 1} for the asset; any value already set on
     * the entity is ignored.
     */
    public void save(AssetVersion version) {
        int nextNum = nextVersionNumber(version.getAssetId());
        String sql = """
                INSERT INTO asset_versions
                    (asset_id, version_number, file_path, source, notes, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime now = LocalDateTime.now();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, version.getAssetId());
            ps.setInt(2, nextNum);
            ps.setString(3, version.getFilePath());
            ps.setString(4, version.getSource() != null ? version.getSource().name() : null);
            ps.setString(5, version.getNotes());
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    version.setId(keys.getLong(1));
                }
            }
            version.setVersionNumber(nextNum);
            version.setCreatedAt(now);
            log.debug("Saved version {} for asset id={}", nextNum, version.getAssetId());
        } catch (SQLException e) {
            log.error("Failed to save version for asset id={}", version.getAssetId(), e);
            throw new RuntimeException(e);
        }
    }

    /** Returns all versions for the given asset, ordered by version number ascending. */
    public List<AssetVersion> findByAssetId(Long assetId) {
        String sql = "SELECT * FROM asset_versions WHERE asset_id = ? ORDER BY version_number";
        List<AssetVersion> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query versions for asset id={}", assetId, e);
            throw new RuntimeException(e);
        }
        return results;
    }

    /** Returns the most recent version for the given asset, if any. */
    public Optional<AssetVersion> findLatestByAssetId(Long assetId) {
        String sql = """
                SELECT * FROM asset_versions WHERE asset_id = ?
                ORDER BY version_number DESC LIMIT 1
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Failed to query latest version for asset id={}", assetId, e);
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /** Removes all versions belonging to the given asset. */
    public void deleteByAssetId(Long assetId) {
        String sql = "DELETE FROM asset_versions WHERE asset_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            int rows = ps.executeUpdate();
            log.debug("Deleted {} version(s) for asset id={}", rows, assetId);
        } catch (SQLException e) {
            log.error("Failed to delete versions for asset id={}", assetId, e);
            throw new RuntimeException(e);
        }
    }

    // ---- helpers ----------------------------------------------------------

    private int nextVersionNumber(Long assetId) {
        String sql = "SELECT COALESCE(MAX(version_number), 0) + 1 FROM asset_versions WHERE asset_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 1;
            }
        } catch (SQLException e) {
            log.error("Failed to compute next version number for asset id={}", assetId, e);
            throw new RuntimeException(e);
        }
    }

    private AssetVersion mapRow(ResultSet rs) throws SQLException {
        String srcStr = rs.getString("source");
        Timestamp createdTs = rs.getTimestamp("created_at");
        return AssetVersion.builder()
                .id(rs.getLong("id"))
                .assetId(rs.getLong("asset_id"))
                .versionNumber(rs.getInt("version_number"))
                .filePath(rs.getString("file_path"))
                .source(srcStr != null ? VersionSource.valueOf(srcStr) : null)
                .notes(rs.getString("notes"))
                .createdAt(createdTs != null ? createdTs.toLocalDateTime() : null)
                .build();
    }
}
