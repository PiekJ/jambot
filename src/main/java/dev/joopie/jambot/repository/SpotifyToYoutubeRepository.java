package dev.joopie.jambot.repository;

import dev.joopie.jambot.models.SpotifyToYoutube;

import javax.xml.bind.ValidationException;
import java.util.List;

public interface SpotifyToYoutubeRepository {
    default SpotifyToYoutubeFinder find() {
        return new SpotifyToYoutubeFinder();
    }

    SpotifyToYoutube save(SpotifyToYoutube spotifyToYoutube) throws ValidationException;
    void delete(SpotifyToYoutube spotifyToYoutube) throws ValidationException;

}
