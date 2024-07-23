package dev.joopie.jambot.api.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResultDto {
    private final boolean found;
    private final String videoId;
    private final String title;
    private final String description;
    private final String thumbnailUrl;
    private final String duration;
}
