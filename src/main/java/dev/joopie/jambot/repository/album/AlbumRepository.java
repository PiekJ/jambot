package dev.joopie.jambot.repository.album;

import dev.joopie.jambot.models.album.Album;

public interface AlbumRepository {
    default AlbumFinder find() {
        return new AlbumFinder();
    }

    Album save(Album album) throws RuntimeException;

    void delete(Album album) throws RuntimeException;
}
