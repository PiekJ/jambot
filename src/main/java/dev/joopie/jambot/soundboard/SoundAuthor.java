package dev.joopie.jambot.soundboard;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Random;

@Getter
@Builder
@RequiredArgsConstructor
public class SoundAuthor {
    private final String name;

    private final List<String> soundUrls;

    public String getRandomSoundUrl(final Random random) {
        return soundUrls.get(random.nextInt(soundUrls.size()));
    }
}
