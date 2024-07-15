package dev.joopie.jambot.models;

import dev.joopie.jambot.models.base.BaseModel;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "track_source")
public class TrackSource extends BaseModel<TrackSource> {
    private String youtubeId;
    private String spotifyId;

    @OneToOne
    private Track track;

    @Override
    public boolean validateSave() {
        return !spotifyId.isEmpty() && !youtubeId.isEmpty();
    }
}
