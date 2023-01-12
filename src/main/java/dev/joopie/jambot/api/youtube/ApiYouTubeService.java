package dev.joopie.jambot.api.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiYouTubeService {
    private final static String SEARCH_URL = "https://youtube.googleapis.com/youtube/v3/search"
            + "?part=id%%2Csnippet&maxResults=1&order=relevance&q=%s&regionCode=nl&topicId=%%2Fm%%2F04rlf"
            + "&type=video&videoDefinition=high&key=%s";

    private final YouTubeProperties properties;
    private final ObjectMapper objectMapper;

    public SearchResultDto searchForSong(final String input) {
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

    private SearchResponse mapHttpEntityToSearchResponse(final HttpEntity httpEntity) {
        try {
            return objectMapper.readValue(httpEntity.getContent(), SearchResponse.class);
        } catch (IOException exception) {
            throw new JambotYouTubeException("Whoeps, we couldn't handle that thick search response.", exception);
        }
    }

    private SearchResultDto mapSearchResponseItemToSearchResult(final SearchResponse.Item item) {
        return SearchResultDto.builder()
                .found(true)
                .videoId(item.getId().getVideoId())
                .title(item.getSnippet().getTitle())
                .description(item.getSnippet().getDescription())
                .thumbnailUrl(getHighestQualityThumbnail(item.getSnippet()).getUrl())
                .build();
    }

    private static SearchResponse.Thumbnail getHighestQualityThumbnail(final SearchResponse.Snippet snippet) {
        final var thumbnails = snippet.getThumbnails();

        if (thumbnails.containsKey("high")) {
            return thumbnails.get("high");
        } else if (thumbnails.containsKey("medium")) {
            return thumbnails.get("medium");
        }

        return thumbnails.get("default");
    }
}
