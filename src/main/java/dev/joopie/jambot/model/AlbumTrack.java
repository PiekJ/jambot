package dev.joopie.jambot.model;

import dev.joopie.jambot.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Entity;

@Entity(name = "album_track")
@Getter
@Setter
public class AlbumTrack extends BaseModel {
    private long albumId;
    private long trackId;
    private int trackNumber;
}
