package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.models.AlbumTrack;

import javax.xml.bind.ValidationException;

public interface AlbumTrackRepository {
    AlbumTrack save(AlbumTrack albumTrack) throws ValidationException;;
    void delete(AlbumTrack albumTrack) throws ValidationException;;
}
