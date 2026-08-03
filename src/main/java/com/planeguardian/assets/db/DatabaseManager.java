package com.planeguardian.assets.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton that owns the JPA {@link EntityManagerFactory}.
 * <p>
 * Call {@link #initialize()} once at startup (e.g. from {@code Main.main}),
 * and {@link #shutdown()} in a JVM shutdown hook.
 * <p>
 * To override the JDBC URL (e.g. for in-memory testing) set system property
 * {@code planeguardian.db.url} before calling {@link #initialize()}.
 */
@Slf4j
public final class DatabaseManager {

    private static volatile EntityManagerFactory emf;

    private DatabaseManager() {}

    public static void initialize() {
        if (emf != null && emf.isOpen()) {
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

        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url", jdbcUrl);
        props.put("jakarta.persistence.jdbc.user", "sa");
        props.put("jakarta.persistence.jdbc.password", "");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");

        emf = Persistence.createEntityManagerFactory("planeguardian-assets", props);
        log.info("Database initialised – url: {}", jdbcUrl);
    }

    public static EntityManager createEntityManager() {
        if (emf == null || !emf.isOpen()) {
            throw new IllegalStateException(
                    "DatabaseManager is not initialised. Call initialize() first.");
        }
        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            log.info("Shutting down database…");
            emf.close();
        }
    }
}
