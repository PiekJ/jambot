package dev.joopie.jambot.service;

import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.repository.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository trackRepository;


    public Track findByExternalId(String externalId) {
        return trackRepository.find().byExternalId(externalId);
    }
}
