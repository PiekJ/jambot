package dev.joopie.jambot.api.spotify;

import dev.joopie.jambot.models.Track;
import dev.joopie.jambot.repository.track.TrackRepository;
import dev.joopie.jambot.service.SpotifyAPIConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApiSpotifyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiSpotifyService.class);
    private static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("https?:\\/\\/(?:open\\.)?spotify.com\\/(user|episode|playlist|track)\\/(?:spotify\\/playlist\\/)?(\\w*)");
    private final SpotifyProperties properties;
    private SpotifyApi spotifyApi;

    @Autowired
    private final SpotifyAPIConverterService spotifyAPIConverterService;

    @Autowired
    private final TrackRepository trackRepository;
    private LocalDateTime tokenExpireDate;


    public void initSpotifyAccessToken() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(properties.getClientId())
                .setClientSecret(properties.getClientSecret())
                .build();

        final var clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();

        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            tokenExpireDate = LocalDateTime.now().plusSeconds(clientCredentials.getExpiresIn());

            log.debug("Expires in: {}", clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public Optional<Track> getTrack(String link) {
        Optional <Track> track = Optional.empty();
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty() || isAccessTokenExpired()) {
            initSpotifyAccessToken();
        }
        final var trackId = getSpotifyIdFromLink(link);

        if (trackId.isEmpty()) {
            return Optional.empty();
        }
        final var getTrackRequest = spotifyApi.getTrack(trackId.get()).build();

        try {
            final var apiTrack = getTrackRequest.execute();

            if (apiTrack != null) {
                track = Optional.of(spotifyAPIConverterService.saveAPIResult(apiTrack));
            }
            return track;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    public Optional<Track> searchForTrack(String searchQuery) {
        se.michaelthelin.spotify.model_objects.specification.Track[] searchResult = new se.michaelthelin.spotify.model_objects.specification.Track[0];
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty() || isAccessTokenExpired()) {
            initSpotifyAccessToken();
        }

        try {
            searchResult = spotifyApi.searchTracks(searchQuery).build().execute().getItems();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.error("Error while fetching the Spotify API");
        }

        if (searchResult == null || searchResult.length == 0) {
            return Optional.empty();
        }

        return Optional.of(spotifyAPIConverterService.saveAPIResult(searchResult[0]));

    }



    private Optional<String> getSpotifyIdFromLink(String link) {
        final var matcher = SPOTIFY_URL_PATTERN.matcher(link);

        if (matcher.find()) {
            return Optional.of(matcher.group(2));
        } else {
            log.debug("No ID found in the link.");
            return Optional.empty();
        }
    }

    private boolean isAccessTokenExpired() {
        return LocalDateTime.now().isAfter(tokenExpireDate);
    }
}
