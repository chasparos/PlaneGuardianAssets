package com.planeguardian.assets.db;

import com.planeguardian.assets.model.Asset;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * Simple CRUD repository for {@link Asset} entities.
 * Each method opens its own {@link EntityManager} and closes it after use.
 */
@Slf4j
public class AssetRepository {

    /** Persist a new asset or merge changes to an existing one. */
    public void save(Asset asset) {
        EntityManager em = DatabaseManager.createEntityManager();
        try {
            em.getTransaction().begin();
            if (asset.getId() == null) {
                em.persist(asset);
            } else {
                em.merge(asset);
            }
            em.getTransaction().commit();
            log.debug("Saved asset: {}", asset.getName());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            log.error("Failed to save asset: {}", asset.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Asset> findById(Long id) {
        EntityManager em = DatabaseManager.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Asset.class, id));
        } finally {
            em.close();
        }
    }

    /** Returns all assets ordered by name. */
    public List<Asset> findAll() {
        EntityManager em = DatabaseManager.createEntityManager();
        try {
            return em.createQuery("SELECT a FROM Asset a ORDER BY a.name", Asset.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Returns only assets flagged for export, ordered by name. */
    public List<Asset> findIncludedInExport() {
        EntityManager em = DatabaseManager.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Asset a WHERE a.includeInExport = true ORDER BY a.name",
                    Asset.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = DatabaseManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Asset asset = em.find(Asset.class, id);
            if (asset != null) {
                em.remove(asset);
                log.debug("Deleted asset id={}", id);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            log.error("Failed to delete asset id={}", id, e);
            throw e;
        } finally {
            em.close();
        }
    }
}
