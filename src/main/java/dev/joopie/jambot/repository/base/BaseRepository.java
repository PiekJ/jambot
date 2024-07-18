package dev.joopie.jambot.repository.base;

import dev.joopie.jambot.model.base.BaseModel;
import dev.joopie.jambot.exception.ValidationException;
import io.ebean.DB;

import java.util.Collection;
import java.util.List;

public abstract class BaseRepository<T extends BaseModel> {

    public static final String VALIDATION_FAILED_FOR = "Validation failed for: ";

    public T save(T entity) throws ValidationException {
        if (!entity.validateSave()) {
            throw new RuntimeException(VALIDATION_FAILED_FOR + entity.getClass());
        }

        entity.save();
        return entity;
    }


    public T update(T entity) throws ValidationException {
        if (!entity.validateSave()) {
            throw new RuntimeException(VALIDATION_FAILED_FOR + entity.getClass());
        }

        DB.update(entity);
        return entity;
    }


    public List<T> saveAll(final List<T> entities) throws ValidationException {
        for (T entity : entities) {
            if (!entity.validateSave()) {
                throw new RuntimeException(VALIDATION_FAILED_FOR + entity.getClass());
            }
        }
        DB.saveAll(entities);
        return entities;
    }


    public void delete(T entity) throws ValidationException {
        if (!entity.validateDelete()) {
            throw new RuntimeException(VALIDATION_FAILED_FOR + entity.getClass());
        }
        DB.delete(entity);
    }


    public void deleteAll(final Collection<T> entities) throws ValidationException {
        for (T entity : entities) {
            if (!entity.validateSave()) {
                throw new RuntimeException(VALIDATION_FAILED_FOR + entity.getClass());
            }
            DB.deleteAll(entities);
        }
    }
}
