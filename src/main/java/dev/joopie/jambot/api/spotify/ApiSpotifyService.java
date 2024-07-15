package dev.joopie.jambot.api.spotify;

import dev.joopie.jambot.service.SpotifyAPIConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
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
    private static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("https?:\\/\\/(?:open\\.)?spotify.com\\/(user|episode|playlist|track)\\/(?:spotify\\/playlist\\/)?(\\w*)");
    private final SpotifyProperties properties;
    private SpotifyApi spotifyApi;

    @Autowired
    private final SpotifyAPIConverterService spotifyAPIConverterService;
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

    public Optional<dev.joopie.jambot.models.Track> getTrack(String link) {
        Optional <dev.joopie.jambot.models.Track> track = Optional.empty();
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
