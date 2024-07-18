package dev.joopie.jambot.model;

import dev.joopie.jambot.model.base.BaseModel;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class Artist extends BaseModel {
    public static final String NAME = "name";
    public static final String EXTERNALID = "external_id";

    private String name;
    private String externalId;
}
