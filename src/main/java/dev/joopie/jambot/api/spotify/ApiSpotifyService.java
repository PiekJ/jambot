package dev.joopie.jambot.api.spotify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApiSpotifyService {
    private static final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("https?:\\/\\/(?:open\\.)?spotify.com\\/(user|episode|playlist|track)\\/(?:spotify\\/playlist\\/)?(\\w*)");
    private final SpotifyProperties properties;
    private SpotifyApi spotifyApi;
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

    public Optional<String> getTrack(String link) {
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty() || isAccessTokenExpired()) {
            initSpotifyAccessToken();
        }
        final var trackId = getSpotifyIdFromLink(link);

        if (trackId.isEmpty()) {
            return Optional.empty();
        }
        final var getTrackRequest = spotifyApi.getTrack(trackId.get()).build();

        try {
            //TODO Save relevant info into our database for later use
            final var track = getTrackRequest.execute();
            return Optional.of(getFormattedTrack(track));

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error(e.getMessage(), e);
        }

        return Optional.empty();
    }

    public List<String> getTracksFromPlaylist(String link) {
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty() || isAccessTokenExpired()) {
            initSpotifyAccessToken();
        }
        final var playlistId = getSpotifyIdFromLink(link);

        if (playlistId.isEmpty()) {
            return List.of();
        }
        final var playlistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId.get()).build();

        try {
            final var playlist = playlistsItemsRequest.execute();
            return Arrays.stream(playlist.getItems())
                    .map(playlistItem -> (Track) playlistItem.getTrack())
                    .map(this::getFormattedTrack)
                    .toList();

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error(e.getMessage(), e);
            return List.of();
        }
    }

    private String getFormattedTrack(Track track) {
        var artists = Arrays.stream(track.getArtists())
                .map(ArtistSimplified::getName)
                .collect(Collectors.joining(","));
        return "%s-%s".formatted(artists, track.getName());
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
