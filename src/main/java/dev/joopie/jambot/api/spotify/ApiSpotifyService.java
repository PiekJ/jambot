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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApiSpotifyService {
    private final SpotifyProperties properties;
    private SpotifyApi spotifyApi;

    private ClientCredentials clientCredentials;


    public void fetchSpotifyAccessToken() {
        spotifyApi =  new SpotifyApi.Builder()
                .setClientId(properties.getClientId())
                .setClientSecret(properties.getClientSecret())
                .build();

        final var clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();

        try {
            clientCredentials = clientCredentialsRequest.execute();

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            log.debug("Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error: {0}", e.getMessage());
        }
    }

    public Optional<String> getTrack(String link) {
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty() || clientCredentials.getExpiresIn() < 0) {
            fetchSpotifyAccessToken();
        }
        final var trackId = getSpotifyIdFromLink(link);

        if (trackId.isPresent()) {
            final var getTrackRequest = spotifyApi.getTrack(trackId.get()).build();

            try {
                final var track = getTrackRequest.execute();
                final var artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(","));
                return Optional.of(getFormattedTrack(artists, track));

            } catch (IOException | SpotifyWebApiException | ParseException e) {
                log.error("Error: %s".formatted(e.getMessage()));
            }
        }
        return Optional.empty();
    }

    public List<String> getTracksFromPlaylist(String link) {
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty() || clientCredentials.getExpiresIn() < 0) {
            fetchSpotifyAccessToken();
        }
        final var playlistId = getSpotifyIdFromLink(link);

        if (playlistId.isPresent()) {
            final var playlistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId.get()).build();

            try {
                final Paging<PlaylistTrack> playlist = playlistsItemsRequest.execute();
                return Arrays.stream(playlist.getItems())
                        .map(playlistItem -> (Track) playlistItem.getTrack())
                        .map(track -> getFormattedTrack(Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(",")), track))
                        .toList();

            } catch (IOException | SpotifyWebApiException | ParseException e) {
                log.error("Error: %s".formatted(e.getMessage()));
            }
        }
        return List.of();
    }

    private String getFormattedTrack(String artists, Track track) {
        return "%s-%s".formatted(artists, track.getName());
    }

    private Optional<String> getSpotifyIdFromLink(String link) {
        Pattern pattern = Pattern.compile("https?:\\/\\/(?:open\\.)?spotify.com\\/(user|episode|playlist|track)\\/(?:spotify\\/playlist\\/)?(\\w*)");
        Matcher matcher = pattern.matcher(link);

        if (matcher.find()) {
            return Optional.of(matcher.group(2));
        } else {
            log.warn("No ID found in the link.");
            return Optional.empty();
        }
    }
}
