package dev.joopie.jambot.api.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import dev.joopie.jambot.model.TrackSource;
import dev.joopie.jambot.service.TrackSourceService;
import dev.lavalink.youtube.CannotBeLoaded;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.MusicWithThumbnail;
import dev.lavalink.youtube.clients.WebWithThumbnail;
import dev.lavalink.youtube.clients.skeleton.Client;
import dev.lavalink.youtube.http.BaseYoutubeHttpContextFilter;
import dev.lavalink.youtube.http.YoutubeHttpContextFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.*;
import org.apache.http.client.config.RequestConfig;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiYouTubeService {
    private final YouTubeProperties properties;
    private final TrackSourceService trackSourceService;
    private final YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, new MusicWithThumbnail());

    private Client getYoutubeSearchClient() {
        return youtubeAudioSourceManager.getClient(MusicWithThumbnail.class);
    }

    // TODO: figure out how to use the token
    public SearchResultDto searchForSong(final String input, final Duration minDuration, final Duration maxDuration, final List<String> artistNames) {
        try (var client = createHttpClient()) {
            AudioItem audioItem = getYoutubeSearchClient().loadSearchMusic(youtubeAudioSourceManager, new HttpInterface(client, createHttpClientContext(), true, new BaseYoutubeHttpContextFilter()), input);

            if (audioItem == null) {
                return null;
            }

            if (audioItem instanceof BasicAudioPlaylist) {
                var audioTracks = ((BasicAudioPlaylist) audioItem).getTracks();
                var rejectedIds = trackSourceService
                        .areRejected(audioTracks.stream().map(AudioTrack::getIdentifier).toList())
                        .stream()
                        .filter(TrackSource::isRejected)
                        .map(TrackSource::getYoutubeId)
                        .toList();

                var filteredTracks = audioTracks.stream().filter((track) -> !rejectedIds.contains(track.getIdentifier())).toList();

                var filteredVideos = getFilteredVideos(filteredTracks, minDuration, maxDuration, artistNames);
                return filteredVideos.stream().findFirst()
                        .orElse(SearchResultDto.builder().found(false).build());
            } else {
                log.warn("No implementation for audioItem class instance: {}", audioItem.getClass().getName());
            }
        } catch (IOException | CannotBeLoaded e) {
            log.warn("Error while fetching data from YouTube API", e);
        }

        return SearchResultDto.builder().found(false).build();
    }

    private CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setContentCompressionEnabled(true)
                        .build())
                .build();
    }

    private HttpClientContext createHttpClientContext() {
        return HttpClientContext.create();
    }

    private List<SearchResultDto> getFilteredVideos(List<AudioTrack> audioTracks, Duration minDuration, Duration maxDuration, List<String> artistNames) throws IOException {
        var matchingChannels = audioTracks.stream()
                .filter(item -> artistNames.stream().anyMatch(artist -> item.getInfo().author.toLowerCase().contains(artist.toLowerCase())))
                .map(this::mapAudioTrackToSearchResult)
                .toList();

        if (!matchingChannels.isEmpty()) {
            return matchingChannels;
        }

        return audioTracks.stream()
                .filter(item -> {
                    var videoDuration = parseDuration(item.getDuration());
                    return videoDuration.compareTo(minDuration) >= 0 && videoDuration.compareTo(maxDuration) <= 0;
                })
                .sorted((item1, item2) -> {
                    var duration1 = parseDuration(item1.getDuration());
                    var duration2 = parseDuration(item2.getDuration());
                    return Long.compare(Math.abs(duration1.minus(minDuration).getSeconds()), Math.abs(duration2.minus(minDuration).getSeconds()));
                })
                .map(this::mapAudioTrackToSearchResult)
                .toList();
    }

    private Duration parseDuration(long durationMillis) {
        return Duration.ofMillis(durationMillis);
    }

    private SearchResultDto mapAudioTrackToSearchResult(AudioTrack audioTrack) {
        // TODO: description
        return SearchResultDto.builder()
                .found(true)
                .videoId(audioTrack.getIdentifier())
                .title(audioTrack.getInfo().title)
                .thumbnailUrl(audioTrack.getInfo().artworkUrl)
                .duration(Duration.ofMillis(audioTrack.getDuration()).toString())
                .build();
    }
}
