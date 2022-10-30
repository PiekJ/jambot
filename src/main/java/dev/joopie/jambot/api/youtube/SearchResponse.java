package dev.joopie.jambot.api.youtube;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class SearchResponse {
    @Data
    @NoArgsConstructor
    public static class Id {
        private String kind;
        private String videoId;
    }

    @Data
    @NoArgsConstructor
    public static class Snippet {
        private String publishedAt;
        private String title;
        private String description;
        private String channelTitle;
        private Map<String, Thumbnail> thumbnails;
    }

    @Data
    @NoArgsConstructor
    public static class Thumbnail {
        private String url;
        private int width;
        private int height;
    }

    @Data
    @NoArgsConstructor
    public static class Item {
        private String kind;
        private String etag;
        private Id id;
        private Snippet snippet;
    }

    private String kind;
    private String etag;
    private String regionCode;
    private List<Item> items;
}
