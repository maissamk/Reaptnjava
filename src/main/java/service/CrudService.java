package service;

import java.util.List;

/**
 * Generic CRUD service interface
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public interface CrudService<T, ID> {
    
    /**
     * Add a new entity
     * @param entity The entity to add
     * @return The added entity
     */
    T add(T entity);
    
    /**
     * Update an existing entity
     * @param entity The entity to update
     * @return The updated entity
     */
    T update(T entity);
    
    /**
     * Delete an entity by its ID
     * @param id The ID of the entity to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean delete(ID id);
    
    /**
     * Find an entity by its ID
     * @param id The ID of the entity to find
     * @return The found entity or null if not found
     */
    T findById(ID id);
    
    /**
     * Find all entities
     * @return A list of all entities
     */
    List<T> findAll();
} 