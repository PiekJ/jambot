package dev.joopie.jambot.model;

import dev.joopie.jambot.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class Artist extends BaseModel {
    public static final String NAME = "name";
    public static final String EXTERNALID = "external_id";

    private String name;
    private String externalId;
}
