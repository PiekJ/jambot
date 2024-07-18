package dev.joopie.jambot.model;

import dev.joopie.jambot.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "track_source")
public class TrackSource extends BaseModel {
    public  static final String YOUTUBEID = "youtubeId";
    public static final String SPOTIFYID = "spotifyId";
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
