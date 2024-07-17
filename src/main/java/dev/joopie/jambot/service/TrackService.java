package dev.joopie.jambot.service;

import dev.joopie.jambot.models.Track;
import dev.joopie.jambot.repository.track.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TrackService {
    private final TrackRepository trackRepository;

    @Autowired
    public TrackService (TrackRepository trackRepository) {
        this.trackRepository = trackRepository;

    }

    public Track findByExternalId(String externalId) {
        return trackRepository.find().byExternalId(externalId);
    }
}
