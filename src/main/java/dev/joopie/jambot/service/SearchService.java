package dev.joopie.jambot.service;

import dev.joopie.jambot.api.spotify.ApiSpotifyService;
import dev.joopie.jambot.api.youtube.ApiYouTubeService;
import dev.joopie.jambot.model.Artist;
import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.model.TrackSource;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private static final int SECONDS_OFFSET = 15;
    private final ApiSpotifyService apiSpotifyService;

    private final TrackSourceService trackSourceService;

    private final ApiYouTubeService apiYouTubeService;

    public String performYoutubeSearch(Track track) {
        if (track.getTrackSource() == null) {
            TrackSource trackSource = new TrackSource();
            trackSource.setYoutubeId(apiYouTubeService.searchForSong(track.getFormattedTrack(), track.getDuration().minusSeconds(SECONDS_OFFSET), track.getDuration().plusSeconds(SECONDS_OFFSET), track.getArtists().stream().map(Artist::getName).toList()).getVideoId());
            trackSource.setSpotifyId(track.getExternalId());
            trackSource.setTrack(track);
            trackSourceService.save(trackSource);
            return trackSource.getYoutubeId();
        } else if (track.getTrackSource().isRejected()) {
            TrackSource trackSource = new TrackSource();
            trackSource.setYoutubeId(apiYouTubeService.searchForSong(track.getFormattedTrack(), track.getDuration().minusSeconds(SECONDS_OFFSET), track.getDuration().plusSeconds(SECONDS_OFFSET), track.getArtists().stream().map(Artist::getName).toList()).getVideoId());
            trackSource.setSpotifyId(track.getExternalId());
            trackSource.setTrack(track);
            trackSourceService.save(trackSource);
            return trackSource.getYoutubeId();
        }

        return track.getTrackSource().getYoutubeId();
    }

    public String performSpotifyAndYoutubeSearch(String artist, String trackname) {
        Optional<Track> track = apiSpotifyService.searchForTrack(artist, trackname);

        if (track.isPresent() && track.get().getTrackSource() != null && !track.get().getTrackSource().isRejected()) {
            return track.get().getTrackSource().getYoutubeId();
        } else if (track.isPresent()) {
            return performYoutubeSearch(track.get());
        } else {
            return Strings.EMPTY;
        }
    }

    public Optional<Track> getTrack(String input) {
        return apiSpotifyService.getTrack(input);
    }
}
