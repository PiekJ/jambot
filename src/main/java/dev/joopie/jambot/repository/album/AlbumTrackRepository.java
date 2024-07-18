package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.exception.ValidationException;
import dev.joopie.jambot.model.AlbumTrack;

public interface AlbumTrackRepository {
    AlbumTrack save(AlbumTrack albumTrack) throws ValidationException;

    void delete(AlbumTrack albumTrack) throws ValidationException;

}
