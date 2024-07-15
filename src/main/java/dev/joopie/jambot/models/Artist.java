package dev.joopie.jambot.models;

import dev.joopie.jambot.models.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class Artist extends BaseModel<Artist> {
    private String name;
    private String externalId;
}
