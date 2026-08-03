package com.planeguardian.assets.db;

import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton that owns the JDBC {@link DataSource}.
 * <p>
 * Call {@link #initialize()} once at startup (e.g. from {@code Main.main}),
 * and {@link #shutdown()} in a JVM shutdown hook.
 * <p>
 * To override the JDBC URL (e.g. for in-memory testing) set system property
 * {@code planeguardian.db.url} before calling {@link #initialize()}.
 */
@Slf4j
public final class DatabaseManager {

    private static volatile DataSource dataSource;

    private DatabaseManager() {}

    public static void initialize() {
        if (dataSource != null) {
            return;
        }

        String jdbcUrl = System.getProperty("planeguardian.db.url");
        if (jdbcUrl == null) {
            Path dbDir = Path.of(System.getProperty("user.home"), ".planeguardian");
            try {
                Files.createDirectories(dbDir);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create database directory: " + dbDir, e);
            }
            jdbcUrl = "jdbc:h2:file:" + dbDir.resolve("assets").toAbsolutePath();
        }

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(jdbcUrl);
        ds.setUser("sa");
        ds.setPassword("");
        dataSource = ds;

        createSchema();
        log.info("Database initialised – url: {}", jdbcUrl);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException(
                    "DatabaseManager is not initialised. Call initialize() first.");
        }
        return dataSource.getConnection();
    }

    public static void shutdown() {
        log.info("Shutting down database…");
        dataSource = null;
    }

    private static void createSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS assets (
                        id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name              VARCHAR(255) NOT NULL,
                        file_path         VARCHAR(1024),
                        asset_type        VARCHAR(50),
                        include_in_export BOOLEAN NOT NULL DEFAULT TRUE,
                        metadata          TEXT,
                        created_at        TIMESTAMP,
                        updated_at        TIMESTAMP
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS asset_versions (
                        id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                        asset_id       BIGINT NOT NULL,
                        version_number INT NOT NULL,
                        file_path      VARCHAR(1024),
                        source         VARCHAR(50),
                        notes          TEXT,
                        created_at     TIMESTAMP,
                        FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
                    )
                    """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create schema", e);
        }
    }
}

