package dev.joopie.jambot.api.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.joopie.jambot.service.TrackSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiYouTubeService {

    private static final String SEARCH_URL = "https://youtube.googleapis.com/youtube/v3/search"
            + "?part=id%%2Csnippet&maxResults=10&order=relevance&q=%s&regionCode=nl&topicId=%%2Fm%%2F04rlf"
            + "&type=video&videoDefinition=high&videoCategoryId=10&key=%s";
    private static final String VIDEO_DETAILS_URL = "https://youtube.googleapis.com/youtube/v3/videos"
            + "?part=contentDetails,snippet&id=%s&key=%s";

    private final YouTubeProperties properties;
    private final ObjectMapper objectMapper;
    private final TrackSourceService trackSourceService;

    public SearchResultDto searchForSong(final String input, final Duration minDuration, final Duration maxDuration, final List<String> artistNames) {
        String encodedInput = URLEncoder.encode(input, StandardCharsets.UTF_8);
        String url = String.format(SEARCH_URL, encodedInput, properties.getToken());

        try (var client = HttpClients.createDefault();
             var response = client.execute(RequestBuilder.get(url).build())) {

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                List<String> videoIds = getVideoIdsFromResponse(response.getEntity());
                List<SearchResultDto> filteredVideos = getFilteredVideos(videoIds, minDuration, maxDuration, artistNames);

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

    private List<String> getVideoIdsFromResponse(HttpEntity entity) throws IOException {
        SearchResponse response = parseResponse(entity, SearchResponse.class);
        return Optional.ofNullable(response)
                .map(SearchResponse::getItems)
                .orElse(List.of())
                .stream()
                .map(SearchResponse.Item::getId)
                .map(SearchResponse.Item.Id::getVideoId)
                .filter(videoId -> {
                    // Check if the video ID is not rejected
                    return !trackSourceService.isRejected(videoId);
                })
                .toList();
    }

    private List<SearchResultDto> getFilteredVideos(List<String> videoIds, Duration minDuration, Duration maxDuration, List<String> artistNames) throws IOException {
        String url = String.format(VIDEO_DETAILS_URL, String.join(",", videoIds), properties.getToken());
        try (var client = HttpClients.createDefault();
             var response = client.execute(RequestBuilder.get(url).build())) {

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                List<SearchResponse.Item> items = getVideoDetailsFromResponse(response.getEntity());

                List<SearchResultDto> matchingChannels = items.stream()
                        .filter(item -> artistNames.stream().anyMatch(artist -> item.getSnippet().getChannelTitle().toLowerCase().contains(artist.toLowerCase())))
                        .map(this::mapItemToSearchResult)
                        .toList();

                if (!matchingChannels.isEmpty()) {
                    return matchingChannels;
                }

                return items.stream()
                        .filter(item -> {
                            Duration videoDuration = parseDuration(item.getContentDetails().getDuration());
                            return videoDuration.compareTo(minDuration) >= 0 && videoDuration.compareTo(maxDuration) <= 0;
                        })
                        .sorted((item1, item2) -> {
                            Duration duration1 = parseDuration(item1.getContentDetails().getDuration());
                            Duration duration2 = parseDuration(item2.getContentDetails().getDuration());
                            return Long.compare(Math.abs(duration1.minus(minDuration).getSeconds()), Math.abs(duration2.minus(minDuration).getSeconds()));
                        })
                        .map(this::mapItemToSearchResult)
                        .toList();
            } else {
                log.warn("Invalid HTTP response status: {}", response.getStatusLine().getStatusCode());
            }
        }

        return List.of();
    }

    private List<SearchResponse.Item> getVideoDetailsFromResponse(HttpEntity entity) throws IOException {
        SearchResponse response = parseResponse(entity, SearchResponse.class);
        return Optional.ofNullable(response)
                .map(SearchResponse::getItems)
                .orElse(List.of());
    }

    private <T> T parseResponse(HttpEntity entity, Class<T> valueType) throws IOException {
        try (InputStream content = getDecompressedInputStream(entity);
             BufferedReader reader = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8))) {

            StringBuilder responseContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }

            return objectMapper.readValue(responseContent.toString(), valueType);
        } catch (IOException e) {
            log.error("Error parsing response", e);
            throw e;
        }
    }

    private InputStream getDecompressedInputStream(HttpEntity entity) throws IOException {
        InputStream inputStream = entity.getContent();
        if (entity.getContentEncoding() != null && "gzip".equalsIgnoreCase(entity.getContentEncoding().getValue())) {
            return new GZIPInputStream(inputStream);
        }
        return inputStream;
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
}
