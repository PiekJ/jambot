package dev.joopie.jambot.models;

import dev.joopie.jambot.models.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "track_source")
public class TrackSource extends BaseModel<TrackSource> {
    private String youtubeId;
    private String spotifyId;
    private boolean rejected;

    @OneToOne
    @JoinColumn(name = "track_id", referencedColumnName = "id")
    private Track track;

    @Override
    public boolean validateSave() {
        return spotifyId != null && !spotifyId.isEmpty() && youtubeId != null && !youtubeId.isEmpty();
    }
}
