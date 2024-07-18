package dev.joopie.jambot.service;

import dev.joopie.jambot.models.TrackSource;
import dev.joopie.jambot.repository.track.TrackSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.ValidationException;

@Service
public class TrackSourceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackSourceService.class);
    @Autowired
    private TrackSourceRepository trackSourceRepository;


    public TrackSource save(TrackSource trackSource) {
        try {
            return trackSourceRepository.save(trackSource);
        } catch (ValidationException e) {
            LOGGER.error("Could not save entity. Check the validateSave() in " + TrackSource.class.getName());
        }
        return null;
    }

    public void delete(TrackSource trackSource) {
        try {
            trackSourceRepository.delete(trackSource);
        } catch (ValidationException e) {
            LOGGER.error("Could not delete entity. Check the validateDelete() in " + TrackSource.class.getName());
        }
    }

    public TrackSource findByYoutubeId(String youtubeId) {
        return trackSourceRepository.find().byYoutubeId(youtubeId);
    }


    public TrackSource findBySpotifyId(String spotifyId) {
        return trackSourceRepository.find().bySpotifyId(spotifyId);
    }

    public boolean isRejected(String youtubeId) {
        TrackSource trackSource = trackSourceRepository.find().byYoutubeId(youtubeId);
        return trackSource != null && trackSource.isRejected();
    }

}
