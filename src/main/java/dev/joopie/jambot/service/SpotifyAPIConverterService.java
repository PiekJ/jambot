package dev.joopie.jambot.service;

import dev.joopie.jambot.model.AlbumTrack;
import dev.joopie.jambot.model.Artist;
import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.model.album.Album;
import dev.joopie.jambot.model.album.AlbumGroup;
import dev.joopie.jambot.model.album.AlbumType;
import dev.joopie.jambot.repository.album.AlbumRepository;
import dev.joopie.jambot.repository.album.AlbumTrackRepository;
import dev.joopie.jambot.repository.artist.ArtistRepository;
import dev.joopie.jambot.repository.track.TrackRepository;
import dev.joopie.jambot.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SpotifyAPIConverterService {
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final AlbumTrackRepository albumTrackRepository;
    private final TrackRepository trackRepository;

    public Track saveAPIResult(se.michaelthelin.spotify.model_objects.specification.Track trackResult) {
        Track track = null;
        if (trackResult != null) {
            try {
                List<Artist> artists = mapAndSaveArtists(trackResult.getArtists());
                Album album = mapAndSaveAlbum(trackResult.getAlbum());
                track = mapAndSaveTrack(trackResult, artists, album);
            } catch (RuntimeException e) {
                log.error("Error while saving Spotify API results into our database");
            }
        } else {
            log.info("API result already found in our local database");
        }
        return track;
    }

    private Track mapAndSaveTrack(se.michaelthelin.spotify.model_objects.specification.Track trackresult, List<Artist> artists, Album album) throws ValidationException {
        Track dbTrack = trackRepository.find().byExternalId(trackresult.getId());

        if (dbTrack != null && dbTrack.getAlbum().contains(album)) {
            return dbTrack;

        } else if (!dbTrack.getAlbum().contains(album)) {
            AlbumTrack albumTrack = new AlbumTrack();
            albumTrack.setTrackNumber(trackresult.getTrackNumber());
            albumTrack.setTrackId(dbTrack.getId());
            albumTrack.setAlbumId(album.getId());

            albumTrackRepository.save(albumTrack);
            return dbTrack;
        } else {
            Track track = new dev.joopie.jambot.model.Track();
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
        }

    }

    private Album mapAndSaveAlbum(AlbumSimplified albumSimplified) throws ValidationException {
        Album dbAlbum = albumRepository.find().byExternalId(albumSimplified.getId());

        if (dbAlbum != null) {
            return dbAlbum;
        } else {
            Album album = new Album();
            album.setName(albumSimplified.getName());
            album.setArtists(mapAndSaveArtists(albumSimplified.getArtists()));
            album.setAlbumGroup(albumSimplified.getAlbumGroup() != null ? AlbumGroup.keyOf(albumSimplified.getAlbumGroup().group) : null);
            album.setAlbumType(albumSimplified.getAlbumType() != null ? AlbumType.keyOf(albumSimplified.getAlbumType().type) : null);
            album.setExternalId(albumSimplified.getId());
            return albumRepository.save(album);
        }
    }

    private List<Artist> mapAndSaveArtists(ArtistSimplified[] artistSimplifieds) throws ValidationException {
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
