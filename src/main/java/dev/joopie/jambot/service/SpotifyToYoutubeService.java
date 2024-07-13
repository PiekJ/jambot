package dev.joopie.jambot.service;

import dev.joopie.jambot.models.SpotifyToYoutube;
import dev.joopie.jambot.repository.SpotifyToYoutubeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.ValidationException;

@Service
public class SpotifyToYoutubeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyToYoutubeService.class);
    @Autowired
    private SpotifyToYoutubeRepository spotifyToYoutubeRepository;


    public SpotifyToYoutube save(SpotifyToYoutube spotifyToYoutube) {
        try {
            return spotifyToYoutubeRepository.save(spotifyToYoutube);
        } catch (ValidationException e) {
            LOGGER.error("Could not save entity. Check the validateSave() in " + SpotifyToYoutube.class.getName());
        }
        return null;
    }

    public void delete(SpotifyToYoutube spotifyToYoutube) {
        try {
            spotifyToYoutubeRepository.delete(spotifyToYoutube);
        } catch (ValidationException e) {
            LOGGER.error("Could not delete entity. Check the validateDelete() in " + SpotifyToYoutube.class.getName());
        }
    }

    public SpotifyToYoutube findByYoutubeId(String youtubeId) {
        return spotifyToYoutubeRepository.find().byYoutubeId(youtubeId);
    }

    public SpotifyToYoutube findBySpotifyId(String spotifyId) {
        return spotifyToYoutubeRepository.find().bySpotifyId(spotifyId);
    }

}
