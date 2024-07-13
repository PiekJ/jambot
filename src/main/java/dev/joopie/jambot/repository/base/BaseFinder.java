package dev.joopie.jambot.repository.base;

import io.ebean.DB;

import java.util.List;
import java.util.Optional;

public class BaseFinder<T> {

    private final Class<T> type;

    public BaseFinder(Class<T> type) {
        this.type = type;
    }

    public T byId(long objectId) {
        return DB.find(type, objectId);
    }

    public Optional<T> byIdAsOptional(long objectId) {
        return Optional.ofNullable(DB.find(type, objectId));
    }

    public List<T> byIds(long... objectIds) {
        return DB.find(type).where().idIn(objectIds).findList();
    }

    public List<T> all() {
        return DB.find(type).findList();
    }
}
