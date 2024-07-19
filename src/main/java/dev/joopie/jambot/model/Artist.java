package dev.joopie.jambot.model;

import dev.joopie.jambot.model.base.BaseModel;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class Artist extends BaseModel {
    public static final String NAME_FIELD = "name";
    public static final String EXTERNALID_FIELD = "external_id";

    private String name;
    private String externalId;
}
