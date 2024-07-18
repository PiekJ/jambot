package dev.joopie.jambot.models;

import dev.joopie.jambot.models.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity(name = "album_track")
@Getter
@Setter
public class AlbumTrack extends BaseModel<AlbumTrack> {
    private long albumId;
    private long trackId;
    private int trackNumber;
}
