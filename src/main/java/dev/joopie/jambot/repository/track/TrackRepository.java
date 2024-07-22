package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.model.Track;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface TrackRepository extends ListCrudRepository<Track, Long> {
    Optional<Track> findByExternalId(String externalId);

    Optional<Track> findByNameAndArtistsName(String trackname, String artistname);
}
