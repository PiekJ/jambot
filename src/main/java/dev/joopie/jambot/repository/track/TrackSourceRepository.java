package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.exception.ValidationException;
import dev.joopie.jambot.model.TrackSource;

public interface TrackSourceRepository {

    default TrackSourceFinder find() {
        return new TrackSourceFinder();
    }

    TrackSource save(TrackSource trackSource) throws ValidationException;

    void delete(TrackSource trackSource) throws ValidationException;

}
