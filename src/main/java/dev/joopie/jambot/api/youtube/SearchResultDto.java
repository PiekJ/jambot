package dev.joopie.jambot.api.youtube;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor

//TODO; Save this as Youtube Video in Database
public class SearchResultDto {
    private final boolean found;

    private final String videoId;
    private final String title;
    private final String description;
    private final String thumbnailUrl;
}
