package dev.joopie.jambot.service;

import dev.joopie.jambot.model.TrackSource;
import dev.joopie.jambot.repository.track.TrackSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackSourceService {

    private final TrackSourceRepository trackSourceRepository;


    public Optional<Object> save(TrackSource trackSource) {
        try {
            return Optional.of(trackSourceRepository.save(trackSource));
        } catch (RuntimeException e) {
            log.error("Could not save entity. Check the validateSave() in " + TrackSource.class.getName(), e);
        }
        return Optional.empty();
    }

    public void delete(TrackSource trackSource) {
        try {
            trackSourceRepository.delete(trackSource);
        } catch (RuntimeException e) {
            log.error("Could not delete entity. Check the validateDelete() in " + TrackSource.class.getName(), e);
        }
    }

    public TrackSource findByYoutubeId(String youtubeId) {
        return trackSourceRepository.find().byYoutubeId(youtubeId);
    }


    public TrackSource findBySpotifyId(String spotifyId) {
        return trackSourceRepository.find().bySpotifyId(spotifyId);
    }

    public boolean isRejected(String youtubeId) {
        TrackSource trackSource = trackSourceRepository.find().byRejectedYoutubeId(youtubeId);
        return trackSource != null && trackSource.isRejected();
    }

}
