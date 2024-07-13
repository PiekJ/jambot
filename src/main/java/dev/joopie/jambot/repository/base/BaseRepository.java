package dev.joopie.jambot.repository.base;

import dev.joopie.jambot.models.BaseModel;
import io.ebean.DB;

import javax.xml.bind.ValidationException;
import java.util.Collection;
import java.util.List;

public abstract class BaseRepository<T extends BaseModel<?>> {

    public T save(T entity) throws ValidationException {
        if (!entity.validateSave()) {
            throw new ValidationException("Validation failed for: " + entity.getClass());
        }

        entity.save();
        return entity;
    }

    
    public T update(T entity) throws ValidationException {
        if (!entity.validateSave()) {
            throw new ValidationException("Validation failed for: " + entity.getClass());
        }

        DB.update(entity);
        return entity;
    }

    
    public List<T> saveAll(final List<T> entities) throws ValidationException {
        for (T entity : entities) {
            if (!entity.validateSave()) {
                throw new ValidationException("Validation failed for: " + entity.getClass());
            }
        }
        DB.saveAll(entities);
        return entities;
    }

    
    public void delete(T entity) throws ValidationException {
        if (!entity.validateDelete()) {
            throw new ValidationException("Validation failed for: " + entity.getClass());
        }
        DB.delete(entity);
    }

    
    public void deleteAll(final Collection<T> entities) throws ValidationException {
        for (T entity : entities) {
            if (!entity.validateSave()) {
                throw new ValidationException("Validation failed for: " + entity.getClass());
            }
            DB.deleteAll(entities);
        }
    }
}
