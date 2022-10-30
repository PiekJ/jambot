package dev.joopie.jambot.music;

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
    private final long position;

    public long getPlayTimeLeft() {
        return duration - position;
    }
}
