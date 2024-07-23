package dev.joopie.jambot.api.youtube;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SearchResponse {

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("nextPageToken")
    private String nextPageToken;

    @JsonProperty("regionCode")
    private String regionCode;

    @JsonProperty("pageInfo")
    private PageInfo pageInfo;

    @JsonProperty("items")
    private List<Item> items;

    @Getter
    @Setter
    public static class PageInfo {
        @JsonProperty("totalResults")
        private int totalResults;

        @JsonProperty("resultsPerPage")
        private int resultsPerPage;
    }

    @Getter
    @Setter
    public static class Item {
        @JsonProperty("kind")
        private String kind;

        @JsonProperty("etag")
        private String etag;

        @JsonProperty("id")
        @JsonDeserialize(using = dev.joopie.jambot.api.youtube.IdDeserializer.class)
        private Id id;

        @JsonProperty("snippet")
        private Snippet snippet;

        @JsonProperty("contentDetails")
        private ContentDetails contentDetails;

        @Getter
        @Setter
        public static class Id {
            @JsonProperty("kind")
            private String kind;

            @JsonProperty("videoId")
            private String videoId;
        }

        @Getter
        @Setter
        public static class Snippet {
            @JsonProperty("publishedAt")
            private String publishedAt;

            @JsonProperty("channelId")
            private String channelId;

            @JsonProperty("title")
            private String title;

            @JsonProperty("description")
            private String description;

            @JsonProperty("thumbnails")
            private Map<String, Thumbnail> thumbnails;

            @JsonProperty("channelTitle")
            private String channelTitle;

            @JsonProperty("liveBroadcastContent")
            private String liveBroadcastContent;

            @JsonProperty("publishTime")
            private String publishTime;

            @Getter
            @Setter
            public static class Thumbnail {
                @JsonProperty("url")
                private String url;

                @JsonProperty("width")
                private int width;

                @JsonProperty("height")
                private int height;
            }
        }

        @Getter
        @Setter
        public static class ContentDetails {
            @JsonProperty("duration")
            private String duration;
        }
    }
}
