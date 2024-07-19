package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.model.TrackSource;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface TrackSourceRepository extends ListCrudRepository<TrackSource, Long> {

    Optional<TrackSource> findByYoutubeId(String youtubeId);

    Optional<TrackSource> findBySpotifyId(String spotifyId);
}
