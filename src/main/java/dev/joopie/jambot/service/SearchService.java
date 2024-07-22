package dev.joopie.jambot.service;

import dev.joopie.jambot.api.spotify.ApiSpotifyService;
import dev.joopie.jambot.api.youtube.ApiYouTubeService;
import dev.joopie.jambot.model.Artist;
import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.model.TrackSource;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private static final int SECONDS_OFFSET = 15;
    private final ApiSpotifyService apiSpotifyService;

    private final TrackSourceService trackSourceService;

    private final ApiYouTubeService apiYouTubeService;
    private final TrackService trackService;


    public String performYoutubeSearch(Track track) {
        if (track.getTrackSources() == null || track.getTrackSources().isEmpty() ||  track.getTrackSources().stream().allMatch(TrackSource::isRejected)) {
            var trackSource = new TrackSource();
            trackSource.setYoutubeId(apiYouTubeService.searchForSong(track.getFormattedTrack(), Duration.ofMillis(track.getDuration().longValue()).minusSeconds(SECONDS_OFFSET), Duration.ofMillis(track.getDuration().longValue()).plusSeconds(SECONDS_OFFSET), track.getArtists().stream().map(Artist::getName).toList()).getVideoId());
            trackSource.setSpotifyId(track.getExternalId());
            trackSource.setTrack(track);
            trackSourceService.save(trackSource);
            return trackSource.getYoutubeId();
        }

        return track.getTrackSources().stream().filter(trackSource -> !trackSource.isRejected()).map(TrackSource::getYoutubeId).findFirst().orElseGet(() -> Strings.EMPTY);
    }

    public String performSpotifyAndYoutubeSearch(String artist, String trackname) {
        final var track = trackService.findByNameAndArtistsName(trackname, artist);
        if (track.isPresent() && track.get().getTrackSources() != null && !track.get().getTrackSources().stream().allMatch(TrackSource::isRejected)) {
            return track.get().getTrackSources().stream().filter(source -> !source.isRejected()).findFirst().get().getYoutubeId();
        }

        final var spotifyTrack = apiSpotifyService.searchForTrack(artist, trackname);
        if (spotifyTrack.isPresent() && (spotifyTrack.get().getTrackSources() == null || spotifyTrack.get().getTrackSources().isEmpty() || spotifyTrack.get().getTrackSources().stream().allMatch(TrackSource::isRejected))) {
            return performYoutubeSearch(spotifyTrack.get());
        } else {
            return spotifyTrack.get().getTrackSources().stream().filter(trackSource -> !trackSource.isRejected()).findFirst().map(TrackSource::getYoutubeId).orElseGet(() -> Strings.EMPTY);
        }
    }

    public Optional<Track> getTrack(String input) {
        return apiSpotifyService.getTrack(input);
    }
}
