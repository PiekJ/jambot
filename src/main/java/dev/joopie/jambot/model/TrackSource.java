package dev.joopie.jambot.model;

import dev.joopie.jambot.model.base.BaseModel;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "track_source")
public class TrackSource extends BaseModel {
    public static final String YOUTUBEID_FIELD = "youtubeId";
    public static final String SPOTIFYID_FIELD = "spotifyId";

    @Nonnull
    private String youtubeId;

    @Nonnull
    private String spotifyId;

    private boolean rejected;

    @ManyToOne
    @JoinColumn(name = "track_id", referencedColumnName = "id")
    private Track track;
}
