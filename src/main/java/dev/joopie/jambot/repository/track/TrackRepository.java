package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.exception.ValidationException;

public interface TrackRepository {
    default TrackFinder find() {
        return new TrackFinder();
    }

    Track save(Track track) throws ValidationException;

    void delete(Track track) throws ValidationException;
}
