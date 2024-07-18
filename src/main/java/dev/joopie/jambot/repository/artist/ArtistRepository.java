package dev.joopie.jambot.repository.artist;

import dev.joopie.jambot.model.Artist;
import dev.joopie.jambot.exception.ValidationException;

public interface ArtistRepository {
    default ArtistFinder find() {
        return new ArtistFinder();
    }

    Artist save(Artist artist) throws ValidationException;

    void delete(Artist artist) throws ValidationException;
}
