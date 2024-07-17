package dev.joopie.jambot.service;

import dev.joopie.jambot.models.Artist;
import dev.joopie.jambot.models.Track;
import dev.joopie.jambot.models.album.Album;
import dev.joopie.jambot.models.album.AlbumGroup;
import dev.joopie.jambot.models.album.AlbumType;
import dev.joopie.jambot.repository.album.AlbumRepository;
import dev.joopie.jambot.repository.artist.ArtistRepository;
import dev.joopie.jambot.repository.track.TrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import javax.xml.bind.ValidationException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SpotifyAPIConverterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyAPIConverterService.class);
    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private TrackRepository trackRepository;

    public Track saveAPIResult(se.michaelthelin.spotify.model_objects.specification.Track trackresult) {
        Track track = trackRepository.find().byExternalId(trackresult.getId());
        if (track == null) {
            try {
                List<Artist> artists = convertArtists(trackresult);
                Album album = convertAlbum(trackresult, artists);
                track = convertTrack(trackresult, artists, album);
            } catch (ValidationException e) {
                LOGGER.error("Error while saving Spotify API results into our database");
            }
        } else {
            LOGGER.info("API result already found in our local database");
        }
        return track;
    }

    private Track convertTrack(se.michaelthelin.spotify.model_objects.specification.Track trackresult, List<Artist> artists, Album album) throws ValidationException {
        Track track = new dev.joopie.jambot.models.Track();
        track.setName(trackresult.getName());
        track.setDuration(Duration.ofMillis(trackresult.getDurationMs()));
        track.setArtists(artists);
        track.setAlbum(Set.of(album));
        track.setExternalId(trackresult.getId());

        return trackRepository.save(track);
    }

    private Album convertAlbum(se.michaelthelin.spotify.model_objects.specification.Track trackresult, List<Artist> artists) throws ValidationException {
        AlbumSimplified albumResult = trackresult.getAlbum();
        Album dbAlbum = albumRepository.find().byExternalId(trackresult.getAlbum().getId());

        if (dbAlbum == null) {
            Album album = new Album();
            album.setName(albumResult.getName());
            album.setArtists(artists);
            album.setAlbumGroup(albumResult.getAlbumGroup() != null ? AlbumGroup.keyOf(albumResult.getAlbumGroup().group) : null);
            album.setAlbumType(albumResult.getAlbumType() != null ? AlbumType.keyOf(albumResult.getAlbumType().type) : null);
            album.setExternalId(albumResult.getId());
            return albumRepository.save(album);
        } else {
            return dbAlbum;
        }
    }

    private List<Artist> convertArtists(se.michaelthelin.spotify.model_objects.specification.Track trackresult) throws ValidationException {
        List<Artist> artistList = new ArrayList<>();
        for (ArtistSimplified artistresult : trackresult.getArtists()) {
            Artist dbArtist = artistRepository.find().byExternalId(artistresult.getId());
            if (dbArtist == null) {
                Artist artist = new Artist();
                artist.setName(artistresult.getName());
                artist.setExternalId(artistresult.getId());

                artistList.add(artistRepository.save(artist));

            } else {
                artistList.add(dbArtist);
            }
        }
        return artistList;
    }
}
