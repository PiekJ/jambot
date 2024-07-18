package dev.joopie.jambot.repository.base;

import dev.joopie.jambot.models.base.BaseModel;
import io.ebean.DB;

import java.util.Collection;
import java.util.List;

public abstract class BaseRepository<T extends BaseModel<?>> {

    public T save(T entity) throws RuntimeException {
        if (!entity.validateSave()) {
            throw new RuntimeException("Validation failed for: " + entity.getClass());
        }

        entity.save();
        return entity;
    }

    
    public T update(T entity) throws RuntimeException {
        if (!entity.validateSave()) {
            throw new RuntimeException("Validation failed for: " + entity.getClass());
        }

        DB.update(entity);
        return entity;
    }

    
    public List<T> saveAll(final List<T> entities) throws RuntimeException {
        for (T entity : entities) {
            if (!entity.validateSave()) {
                throw new RuntimeException("Validation failed for: " + entity.getClass());
            }
        }
        DB.saveAll(entities);
        return entities;
    }

    
    public void delete(T entity) throws RuntimeException {
        if (!entity.validateDelete()) {
            throw new RuntimeException("Validation failed for: " + entity.getClass());
        }
        DB.delete(entity);
    }

    
    public void deleteAll(final Collection<T> entities) throws RuntimeException {
        for (T entity : entities) {
            if (!entity.validateSave()) {
                throw new RuntimeException("Validation failed for: " + entity.getClass());
            }
            DB.deleteAll(entities);
        }
    }
}
