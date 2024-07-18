package dev.joopie.jambot.service;

import dev.joopie.jambot.models.AlbumTrack;
import dev.joopie.jambot.models.Artist;
import dev.joopie.jambot.models.Track;
import dev.joopie.jambot.models.album.Album;
import dev.joopie.jambot.models.album.AlbumGroup;
import dev.joopie.jambot.models.album.AlbumType;
import dev.joopie.jambot.repository.album.AlbumRepository;
import dev.joopie.jambot.repository.album.AlbumTrackRepository;
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

@Service
public class SpotifyAPIConverterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyAPIConverterService.class);
    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumTrackRepository albumTrackRepository;

    @Autowired
    private TrackRepository trackRepository;

    public Track saveAPIResult(se.michaelthelin.spotify.model_objects.specification.Track trackResult) {
        Track track = null;
        if (trackResult != null) {
            try {
                List<Artist> artists = convertArtists(trackResult.getArtists());
                Album album = convertAlbum(trackResult.getAlbum());
                track = convertTrack(trackResult, artists, album);
            } catch (ValidationException e) {
                LOGGER.error("Error while saving Spotify API results into our database");
            }
        } else {
            LOGGER.info("API result already found in our local database");
        }
        return track;
    }

    private Track convertTrack(se.michaelthelin.spotify.model_objects.specification.Track trackresult, List<Artist> artists, Album album) throws ValidationException {
        Track dbTrack = trackRepository.find().byExternalId(trackresult.getId());

        if (dbTrack == null) {
            Track track = new dev.joopie.jambot.models.Track();
            track.setName(trackresult.getName());
            track.setDuration(Duration.ofMillis(trackresult.getDurationMs()));
            track.setArtists(artists);
            track.setExternalId(trackresult.getId());
            track = trackRepository.save(track);

            AlbumTrack albumTrack = new AlbumTrack();
            albumTrack.setTrackNumber(trackresult.getTrackNumber());
            albumTrack.setTrackId(track.getId());
            albumTrack.setAlbumId(album.getId());
            albumTrackRepository.save(albumTrack);

            return track;

        } else if (!dbTrack.getAlbum().contains(album)) {

            AlbumTrack albumTrack = new AlbumTrack();
            albumTrack.setTrackNumber(trackresult.getTrackNumber());
            albumTrack.setTrackId(dbTrack.getId());
            albumTrack.setAlbumId(album.getId());

            albumTrackRepository.save(albumTrack);
        }
        return dbTrack;
    }

    private Album convertAlbum(AlbumSimplified albumSimplified) throws ValidationException {
        Album dbAlbum = albumRepository.find().byExternalId(albumSimplified.getId());

        if (dbAlbum == null) {
            Album album = new Album();
            album.setName(albumSimplified.getName());
            album.setArtists(convertArtists(albumSimplified.getArtists()));
            album.setAlbumGroup(albumSimplified.getAlbumGroup() != null ? AlbumGroup.keyOf(albumSimplified.getAlbumGroup().group) : null);
            album.setAlbumType(albumSimplified.getAlbumType() != null ? AlbumType.keyOf(albumSimplified.getAlbumType().type) : null);
            album.setExternalId(albumSimplified.getId());
            return albumRepository.save(album);
        } else {
            return dbAlbum;
        }
    }

    private List<Artist> convertArtists(ArtistSimplified[] artistSimplifieds) throws ValidationException {
        List<Artist> artistList = new ArrayList<>();
        for (ArtistSimplified artistresult : artistSimplifieds) {
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
