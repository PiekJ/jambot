package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.exception.ValidationException;
import dev.joopie.jambot.model.album.Album;

public interface AlbumRepository {
    default AlbumFinder find() {
        return new AlbumFinder();
    }

    Album save(Album album) throws ValidationException;

    void delete(Album album) throws ValidationException;
}
