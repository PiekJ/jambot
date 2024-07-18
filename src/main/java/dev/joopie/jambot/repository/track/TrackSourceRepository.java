package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.models.TrackSource;

import javax.xml.bind.ValidationException;

public interface TrackSourceRepository {

    default TrackSourceFinder find() {
        return new TrackSourceFinder();
    }

    TrackSource save(TrackSource trackSource) throws RuntimeException;
    void delete(TrackSource trackSource) throws RuntimeException;

}
