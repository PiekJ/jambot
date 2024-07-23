package dev.joopie.jambot.model;

import dev.joopie.jambot.model.base.BaseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlayHistory extends BaseModel {
    private String userId;
    private String guildId;

    @ManyToOne
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;
}
