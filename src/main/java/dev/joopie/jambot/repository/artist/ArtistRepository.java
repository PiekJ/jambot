package dev.joopie.jambot.repository.artist;

import dev.joopie.jambot.models.Artist;

public interface ArtistRepository {
    default ArtistFinder find() {
        return new ArtistFinder();
    }

    Artist save(Artist artist) throws RuntimeException;

    void delete(Artist artist) throws RuntimeException;
}
