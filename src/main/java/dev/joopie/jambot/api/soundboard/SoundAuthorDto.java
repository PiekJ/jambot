package dev.joopie.jambot.api.soundboard;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Random;

@Getter
@Builder
@RequiredArgsConstructor
public class SoundAuthorDto {
    @Getter
    @Builder
    @RequiredArgsConstructor
    public static class Sound {
        private final String title;
        private final String file;
    }

    private final String authorName;
    private final List<Sound> sounds;
}
