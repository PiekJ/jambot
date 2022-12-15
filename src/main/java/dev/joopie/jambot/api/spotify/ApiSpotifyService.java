package dev.joopie.jambot.api.spotify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApiSpotifyService {
    private static final Logger LOGGER = Logger.getLogger(ApiSpotifyService.class.getName());
    private final SpotifyProperties properties;
    private SpotifyApi spotifyApi;
    private ClientCredentialsRequest clientCredentialsRequest;

    private ClientCredentials clientCredentials;


    public void getSpotifyAccessToken() {
        spotifyApi =  new SpotifyApi.Builder()
                .setClientId(properties.getClientId())
                .setClientSecret(properties.getClientSecret())
                .build();

        clientCredentialsRequest =  spotifyApi.clientCredentials()
                .build();

        try {
            clientCredentials = clientCredentialsRequest.execute();

            // Set access token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            System.out.println("Expires in: " + clientCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
        }
    }

    public String getTrack(String trackId) {
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty() || clientCredentials.getExpiresIn() < 0) {
            getSpotifyAccessToken();
        }

        GetTrackRequest getTrackRequest = spotifyApi.getTrack(trackId).build();

        try {
            final Track track = getTrackRequest.execute();
            String artists = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(","));
            return String.join("-", artists, track.getName());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
        }
        return "";
    }

    public List<String> getTracksFromPlaylist(String playlistId) {
        if (spotifyApi == null || spotifyApi.getAccessToken().isEmpty()) {
            getSpotifyAccessToken();
        }
        GetPlaylistsItemsRequest playlistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId).build();

        try {
            final Paging<PlaylistTrack> playlist = playlistsItemsRequest.execute();
            return Arrays.stream(playlist.getItems())
                        .map(playlistItem -> (Track) playlistItem.getTrack())
                        .map(track -> String.join("-" , Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(",")),track.getName()))
                    .toList();

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
        }
        return new ArrayList<>();
    }
}
