package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.model.Track;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends ListCrudRepository<Track, Long> {
    Optional<Track> findByExternalId(String externalId);

    Optional<Track> findByNameAndArtistsName(String trackname, String artistname);
    List<Track> findFirst25ByNameStartingWithAndArtistsName(String trackName, String artistName);
}
