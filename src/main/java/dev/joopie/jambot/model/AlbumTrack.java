package dev.joopie.jambot.model;

import dev.joopie.jambot.model.base.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "album_track")
@Getter
@Setter
public class AlbumTrack extends BaseModel {
    @Column(name = "album_id")
    private long albumId;
    @Column(name = "track_id")
    private long trackId;
    private int trackNumber;
}
