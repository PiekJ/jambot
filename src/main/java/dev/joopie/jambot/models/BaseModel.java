package dev.joopie.jambot.models;

import io.ebean.Model;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseModel<T> extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long objectid;

    @Temporal(TemporalType.DATE)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Temporal(TemporalType.DATE)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Version
    private int version;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt =  LocalDateTime.now();
        this.version = version++;
    }

    public boolean validateSave() {
        return true;
    }

    public boolean validateDelete() {
        return true;
    }
}
