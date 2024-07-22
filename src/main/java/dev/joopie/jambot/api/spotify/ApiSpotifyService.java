package dev.joopie.jambot.api.spotify;

import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.service.SpotifyAPIConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApiSpotifyService {
    private static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("https?:\\/\\/(?:open\\.)?spotify.com\\/(user|episode|playlist|track)\\/(?:spotify\\/playlist\\/)?(\\w*)");
    private final SpotifyProperties properties;
    private SpotifyApi spotifyApi;
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
            final var clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            tokenExpireDate = LocalDateTime.now().plusSeconds(clientCredentials.getExpiresIn());

            log.debug("Expires in: {}", clientCredentials.getExpiresIn());
        } catch (final IOException | SpotifyWebApiException | ParseException e) {
            log.error(e.getMessage(), e);
        }
    }


    public Optional<Track> getTrack(String link) {
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
               return Optional.of(spotifyAPIConverterService.saveAPIResult(apiTrack));
            }

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    public Optional<Track> searchForTrack(String artistName, String trackName) {
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty() || isAccessTokenExpired()) {
            initSpotifyAccessToken();
        }
        final var searchQuery = "%s - %s".formatted(artistName, trackName);
        try {
            return Arrays.stream(spotifyApi.searchTracks(searchQuery).build().execute().getItems())
                            .filter(track -> Arrays.stream(track.getArtists())
                                    .anyMatch(artist -> artist.getName().toLowerCase().contains(artistName.toLowerCase())))
                            .findFirst()
                            .map(spotifyAPIConverterService::saveAPIResult);

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error while fetching the Spotify API", e);
            return Optional.empty();
        }
    }



    private Optional<String> getSpotifyIdFromLink(String link) {
            var matcher = SPOTIFY_URL_PATTERN.matcher(link);
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
