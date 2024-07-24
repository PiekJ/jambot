package dev.joopie.jambot.repository.track;

import dev.joopie.jambot.model.TrackSource;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackSourceRepository extends ListCrudRepository<TrackSource, Long> {

    Optional<TrackSource> findByYoutubeId(String youtubeId);

    Optional<TrackSource> findBySpotifyId(String spotifyId);

    List<TrackSource> findAllByYoutubeIdInAndRejectedIsTrue(List<String> youtubeIds);
}
