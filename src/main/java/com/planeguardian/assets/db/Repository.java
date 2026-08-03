package com.planeguardian.assets.db;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD abstraction for JDBC-backed repositories.
 *
 * @param <T>  entity type
 * @param <ID> primary-key type
 */
public interface Repository<T, ID> {

    /** Persist a new entity (id == null) or update an existing one. */
    void save(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    void delete(ID id);
}
