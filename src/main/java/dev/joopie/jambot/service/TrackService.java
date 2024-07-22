package dev.joopie.jambot.service;

import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.repository.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository trackRepository;

    public Optional<Track> findByExternalId(String externalId) {
        return trackRepository.findByExternalId(externalId);
    }

    public Optional<Track> findByNameAndArtistsName(String trackname, String artistName) {
        return trackRepository.findByNameAndArtistsName(trackname, artistName);
    }
}
