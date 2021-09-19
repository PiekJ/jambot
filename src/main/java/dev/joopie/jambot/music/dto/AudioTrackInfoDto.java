package dev.joopie.jambot.music.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class AudioTrackInfoDto {
    private final int index;
    private final String author;
    private final String title;
    private final long duration;
}
