package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.models.Artist;
import dev.joopie.jambot.models.Track;
import dev.joopie.jambot.repository.artist.ArtistFinder;

import javax.xml.bind.ValidationException;

public interface TrackRepository {
    default TrackFinder find() {
        return new TrackFinder();
    }

    Track save(Track track) throws ValidationException;
    void delete(Track track) throws ValidationException;
}
