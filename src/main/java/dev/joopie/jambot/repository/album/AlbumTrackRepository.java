package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.models.AlbumTrack;

public interface AlbumTrackRepository {
    AlbumTrack save(AlbumTrack albumTrack) throws RuntimeException;

    void delete(AlbumTrack albumTrack) throws RuntimeException;

}
