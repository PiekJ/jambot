package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.models.Track;

public interface TrackRepository {
    default TrackFinder find() {
        return new TrackFinder();
    }

    Track save(Track track) throws RuntimeException;

    void delete(Track track) throws RuntimeException;
}
