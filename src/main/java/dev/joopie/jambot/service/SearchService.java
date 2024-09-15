package dev.joopie.jambot.service;

import dev.joopie.jambot.api.spotify.ApiSpotifyService;
import dev.joopie.jambot.api.youtube.ApiYouTubeService;
import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.model.TrackSource;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ApiSpotifyService apiSpotifyService;
    private final TrackSourceService trackSourceService;
    private final ApiYouTubeService apiYouTubeService;
    private final TrackService trackService;

    public Optional<String> performYoutubeSearch(final Track track) {
        if (track.getTrackSources() == null || track.getTrackSources().isEmpty() ||  track.getTrackSources().stream().allMatch(TrackSource::isRejected)) {
            var trackSource = new TrackSource();
            var videoResult = apiYouTubeService.searchForSong(track);

            if (videoResult != null && videoResult.isFound() && !Strings.isBlank(videoResult.getVideoId())) {
                trackSource.setYoutubeId(videoResult.getVideoId());
                trackSource.setSpotifyId(track.getExternalId());
                trackSource.setTrack(track);
                trackSourceService.save(trackSource);
            }

            return Strings.isBlank(trackSource.getYoutubeId()) ? Optional.empty() : Optional.of(trackSource.getYoutubeId());
        }

        return track.getTrackSources().stream().filter(trackSource -> !trackSource.isRejected()).map(TrackSource::getYoutubeId).findFirst();
    }

    public Optional<String> performYoutubeSearch(final String artist, final String trackName) {
       return Optional.of(apiYouTubeService.searchForSong(artist, trackName).getVideoId());
    }

    @Transactional
    public Optional<String> performSpotifyAndYoutubeSearch(final String artist, final String trackName) {
        final var track = trackService.findByNameAndArtistsName(trackName, artist);
        if (track.isPresent() && track.get().getTrackSources() != null && !track.get().getTrackSources().stream().allMatch(TrackSource::isRejected)) {
            return track.get().getTrackSources().stream().filter(source -> !source.isRejected()).findFirst().map(TrackSource::getYoutubeId);
        }

        final var spotifyTrack = apiSpotifyService.searchForTrack(artist, trackName);

        if (spotifyTrack.isEmpty()) {
            return performYoutubeSearch(artist, trackName);
        }

        if (spotifyTrack.get().getTrackSources() == null || spotifyTrack.get().getTrackSources().isEmpty() || spotifyTrack.get().getTrackSources().stream().allMatch(TrackSource::isRejected)) {
            return performYoutubeSearch(spotifyTrack.get());
        } else {
            return spotifyTrack.get().getTrackSources().stream().filter(trackSource -> !trackSource.isRejected()).findFirst().map(TrackSource::getYoutubeId);
        }
    }

    public Optional<Track> getTrack(final String input) {
        return apiSpotifyService.getTrack(input);
    }
}
