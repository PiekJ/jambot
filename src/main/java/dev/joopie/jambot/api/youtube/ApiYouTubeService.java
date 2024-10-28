package dev.joopie.jambot.api.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.joopie.jambot.model.Track;
import dev.joopie.jambot.model.TrackSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiYouTubeService {

    private static final String SEARCH_URL = "https://youtube.googleapis.com/youtube/v3/search"
            + "?part=id%%2Csnippet&maxResults=10&order=relevance&q=%s&topicId=%%2Fm%%2F04rlf"
            + "&type=video&key=%s";
    private static final String VIDEO_DETAILS_URL = "https://youtube.googleapis.com/youtube/v3/videos"
            + "?part=contentDetails,snippet,statistics&id=%s&key=%s";


    private final YouTubeProperties properties;
    private final ObjectMapper objectMapper;

    public SearchResultDto searchForSong(final Track track) {
        if (track == null) {
            return null;
        }

        var encodedInput = URLEncoder.encode(track.getFormattedTrack(), StandardCharsets.UTF_8);
        var url = SEARCH_URL.formatted(encodedInput, properties.getToken());

        try (var client = createHttpClient();
             var response = client.execute(RequestBuilder.get(url).build())) {

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                var videoIds = getVideoIdsFromResponse(response.getEntity(), track);
                var filteredVideos = getFilteredVideos(videoIds, track);

                return filteredVideos.stream().findFirst()
                        .orElse(SearchResultDto.builder().found(false).build());
            } else {
                log.warn("Invalid HTTP response status: {}", response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            log.warn("Error while fetching data from YouTube API", e);
        }

        return SearchResultDto.builder().found(false).build();
    }

    public SearchResultDto searchForSong(final String artist, final String trackName) {
        var input = "%s - %s".formatted(artist, trackName);
        final var encodedInput = URLEncoder.encode(input, StandardCharsets.UTF_8);
        final var request = RequestBuilder.get(SEARCH_URL.formatted(encodedInput, properties.getToken()))
                .build();

        try (final var client = HttpClients.createDefault();
             final var response = client.execute(request)) {

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return Optional.ofNullable(response.getEntity())
                        .map(this::mapHttpEntityToSearchResponse)
                        .flatMap(x -> x.getItems().stream().findFirst())
                        .map(this::mapSearchResponseItemToSearchResult)
                        .orElseThrow(() -> new JambotYouTubeException("Did not find any search result."));
            } else {
                log.warn("Invalid http response status ({}) returned.", response.getStatusLine().getStatusCode());
            }
        } catch (IOException exception) {
            log.warn("YouTube did an oepsie.", exception);
        }

        return SearchResultDto.builder()
                .found(false)
                .build();
    }

    private SearchResultDto mapSearchResponseItemToSearchResult(final SearchResponse.Item item) {
        return SearchResultDto.builder()
                .found(true)
                .videoId(item.getId().getVideoId())
                .title(item.getSnippet().getTitle())
                .description(item.getSnippet().getDescription())
                .build();
    }

    private SearchResponse mapHttpEntityToSearchResponse(final HttpEntity httpEntity) {
        try {
            return objectMapper.readValue(httpEntity.getContent(), SearchResponse.class);
        } catch (IOException exception) {
            throw new JambotYouTubeException("Whoeps, we couldn't handle that thick search response.", exception);
        }
    }

    private CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setContentCompressionEnabled(true)
                        .build())
                .build();
    }

    private List<String> getVideoIdsFromResponse(HttpEntity entity, final Track track) throws IOException {
        final var trackSources = track.getTrackSources();
        final var trackSourcesIds = trackSources == null ? new HashSet<>() : trackSources.stream().map(TrackSource::getYoutubeId).collect(Collectors.toSet());
        var response = parseResponse(entity, SearchResponse.class);
        return Optional.ofNullable(response)
                .map(SearchResponse::getItems)
                .orElse(List.of())
                .stream()
                .map(SearchResponse.Item::getId)
                .map(SearchResponse.Item.Id::getVideoId)
                .filter(videoId -> !trackSourcesIds.contains(videoId))
                .toList();
    }

    private List<SearchResultDto> getFilteredVideos(final List<String> videoIds, final Track track) throws IOException {
        var url = VIDEO_DETAILS_URL.formatted(String.join(",", videoIds), properties.getToken());
        var minDuration = Duration.ofMillis(track.getDuration().longValue()).minusSeconds(properties.getVideoDurationOffset());
        var maxDuration = Duration.ofMillis(track.getDuration().longValue()).plusSeconds(properties.getVideoDurationOffset());

        try (var client = createHttpClient();
             var response = client.execute(RequestBuilder.get(url).build())) {

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                var items = getVideoDetailsFromResponse(response.getEntity());

                return items.stream()
                        .filter(item -> {
                            var videoDuration = parseDuration(item.getContentDetails().getDuration());
                            return videoDuration.compareTo(minDuration) >= 0 && videoDuration.compareTo(maxDuration) <= 0;
                        })
                        .sorted((item1, item2) -> {
                            var duration1 = parseDuration(item1.getContentDetails().getDuration());
                            var duration2 = parseDuration(item2.getContentDetails().getDuration());
                            return Long.compare(Math.abs(duration1.minus(minDuration).getSeconds()), Math.abs(duration2.minus(minDuration).getSeconds()));
                        })
                        .sorted((item1, item2) -> Double.compare(
                                calculateRelevanceScore(item2, track),
                                calculateRelevanceScore(item1, track)
                        ))

                        .map(this::mapItemToSearchResult)
                        .toList();
            } else {
                log.warn("Invalid HTTP response status: {}", response.getStatusLine().getStatusCode());
            }
        }

        return List.of();
    }

    private List<SearchResponse.Item> getVideoDetailsFromResponse(HttpEntity entity) throws IOException {
        var response = parseResponse(entity, SearchResponse.class);
        return Optional.ofNullable(response)
                .map(SearchResponse::getItems)
                .orElse(List.of());
    }

    private <T> T parseResponse(HttpEntity entity, Class<T> valueType) throws IOException {
        try (var content = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
            var responseContent = new StringBuilder();
            String line;
            while ((line = content.readLine()) != null) {
                responseContent.append(line);
            }
            return objectMapper.readValue(responseContent.toString(), valueType);
        } catch (IOException e) {
            log.error("Error parsing response", e);
            throw e;
        }
    }

    private Duration parseDuration(String isoDuration) {
        return Duration.parse(isoDuration);
    }

    private SearchResultDto mapItemToSearchResult(SearchResponse.Item item) {
        return SearchResultDto.builder()
                .found(true)
                .videoId(item.getId().getVideoId())
                .title(item.getSnippet().getTitle())
                .description(item.getSnippet().getDescription())
                .thumbnailUrl(getHighestQualityThumbnail(item.getSnippet().getThumbnails()))
                .duration(item.getContentDetails().getDuration())
                .build();
    }

    private String getHighestQualityThumbnail(Map<String, SearchResponse.Item.Snippet.Thumbnail> thumbnails) {
        if (thumbnails.containsKey("high")) {
            return thumbnails.get("high").getUrl();
        } else if (thumbnails.containsKey("medium")) {
            return thumbnails.get("medium").getUrl();
        } else {
            return thumbnails.get("default").getUrl();
        }
    }

    public double calculateRelevanceScore(SearchResponse.Item video, Track track) {
        double score = 0.0;
        String title = video.getSnippet().getTitle().toLowerCase();
        String channelName = video.getSnippet().getChannelTitle().toLowerCase();

        // Check if statistics are available to avoid NullPointerException
        if (video.getStatistics() != null) {
            long viewCount = video.getStatistics().getViewCount();
            long likeCount = video.getStatistics().getLikeCount();

            // 1. Only process videos that meet the minimum view count requirement
            if (viewCount >= properties.getMinimalViewCount()) {
                score += Math.log10(viewCount) * 2; // Use logarithmic scaling to avoid large jumps for high view counts
            } else {
                score -= Math.log10(properties.getMinimalViewCount() - viewCount + 1) * 2; // Penalize for low view counts
            }

            // 2. Exact title match for the track name
            if (title.equalsIgnoreCase(track.getName().toLowerCase())) {
                score *= 1.5; // Boost for exact match on track title
            }

            // 3. Matching any artist in channel name or title
            boolean artistMatch = track.getArtists().stream()
                    .anyMatch(artist -> channelName.contains(artist.getName().toLowerCase()) || title.contains(artist.getName().toLowerCase()));
            if (artistMatch) {
                score *= 1.2; // Boost score if any artist name matches
            }

            // 4. Additional relevance for likes (reduced influence)
            if (likeCount > 0) {
                score += Math.log10(likeCount + 1) * 0.2; // Reduced impact for likes, emphasizing the release date more
            }

            // 5. Positive boost for official videos or channels based on heuristics
            if (title.contains("official") || title.contains("official music video") || title.contains("official release") ||
                    channelName.contains("official")) {
                score *= 1.5; // Boost for official content or channels that look official
            }

            // 6. Check for release date with publish date. Give more weight to this factor
            LocalDate videoUploadDate = Instant.parse(video.getSnippet().getPublishedAt())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            long daysDifference = ChronoUnit.DAYS.between(track.getReleaseDate(), videoUploadDate);

            // Adjust score with more emphasis on how close the upload date is to the track's release date
            if (daysDifference < -2) {
                score -= 10; //Probably HQ RIP, other song, leak or remake of a song. We penalize
            } else if (daysDifference > -2 && daysDifference <=0) {
                score += 5.0; // Strong boost for videos uploaded on the release date
            } else if (daysDifference > 0 && daysDifference <= 30) {
                score += 3.0; // Boost for videos uploaded within 30 days of the release
            } else if (daysDifference > 30 && daysDifference <= 90) {
                score += 1.5; // Small boost for videos uploaded within 90 days of release
            } else if (daysDifference > 90) {
                score -= 10.0; // Penalty for videos uploaded far after the release
            }
        }

        return score;
    }


}
