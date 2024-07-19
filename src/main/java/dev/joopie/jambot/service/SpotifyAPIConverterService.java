package dev.joopie.jambot.service;

import dev.joopie.jambot.exception.ValidationException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
                log.error("Error while saving Spotify API results into our database", e);
            }
        } else {
            log.info("API result already found in our local database");
        }
        return track;
    }

    private Track mapAndSaveTrack(se.michaelthelin.spotify.model_objects.specification.Track trackresult, List<Artist> artists, Album album) throws ValidationException {
        Optional<Track> dbTrack = trackRepository.findByExternalId(trackresult.getId());

        if (dbTrack.isPresent() && dbTrack.get().getAlbum().contains(album)) {
            return dbTrack.get();

        } else if (dbTrack.isPresent() && !dbTrack.get().getAlbum().contains(album)) {
            AlbumTrack albumTrack = new AlbumTrack();
            albumTrack.setTrackNumber(trackresult.getTrackNumber());
            albumTrack.setTrackId(dbTrack.get().getId());
            albumTrack.setAlbumId(album.getId());

            albumTrackRepository.save(albumTrack);
            return dbTrack.get();
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
        return albumRepository.findByExternalId(albumSimplified.getId()).orElseGet(() -> {
            Album album = new Album();
            album.setName(albumSimplified.getName());
            album.setArtists(mapAndSaveArtists(albumSimplified.getArtists()));
            album.setAlbumGroup(albumSimplified.getAlbumGroup() != null ? AlbumGroup.keyOf(albumSimplified.getAlbumGroup().group) : null);
            album.setAlbumType(albumSimplified.getAlbumType() != null ? AlbumType.keyOf(albumSimplified.getAlbumType().type) : null);
            album.setExternalId(albumSimplified.getId());
            return albumRepository.save(album);
        });
    }

    private List<Artist> mapAndSaveArtists(ArtistSimplified[] artistSimplifieds) throws ValidationException {
        return Arrays.stream(artistSimplifieds).map(artistSimplified -> artistRepository.findByExternalId(artistSimplified.getId()).orElseGet(() -> {
            Artist artist = new Artist();
            artist.setName(artistSimplified.getName());
            artist.setExternalId(artistSimplified.getId());
            artistRepository.save(artist);
            return artist;
        })).toList();
    }
}
