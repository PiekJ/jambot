package dev.joopie.jambot.models;

import dev.joopie.jambot.models.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "track_source")
public class TrackSource extends BaseModel<TrackSource> {
    private String youtubeId;
    private String spotifyId;

    @OneToOne
    @JoinColumn(name = "track_id", referencedColumnName = "id")
    private Track track;

    @Override
    public boolean validateSave() {
        return spotifyId != null && !spotifyId.isEmpty() && youtubeId != null && !youtubeId.isEmpty();
    }
}
