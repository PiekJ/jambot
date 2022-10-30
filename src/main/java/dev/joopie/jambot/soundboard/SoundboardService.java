package dev.joopie.jambot.soundboard;

import dev.joopie.jambot.api.soundboard.ApiSoundBoardService;
import dev.joopie.jambot.api.soundboard.SoundAuthorDto;
import dev.joopie.jambot.api.soundboard.JambotSoundBoardException;
import dev.joopie.jambot.music.GuildMusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoundboardService {
    private final GuildMusicService musicService;
    private final ApiSoundBoardService apiSoundBoardService;
    private final Random random;

    private Map<String, SoundAuthor> soundAuthors;

    @PostConstruct
    public void postConstruct() {
        final var dtos = apiSoundBoardService.fetchSoundBoardSounds();
        soundAuthors = dtos.stream().collect(Collectors.toMap(
                SoundAuthorDto::getAuthorName,
                x -> SoundAuthor.builder()
                        .name(x.getAuthorName())
                        .soundUrls(x.getSounds().stream()
                                .map(SoundAuthorDto.Sound::getFile)
                                .collect(Collectors.toList()))
                        .build()));
    }

    public List<String> autocompleteAuthorStartWith(final String authorStartWith) {
        return soundAuthors.keySet().stream()
                .filter(x -> x.startsWith(authorStartWith))
                .toList();
    }

    public void playRandomSoundByAuthor(final Guild guild, final User user, final String authorName) {
        if (soundAuthors.containsKey(authorName)) {
            final var soundUrl = soundAuthors.get(authorName).getRandomSoundUrl(random);
            log.info("SoundBoard %s track %s".formatted(authorName, soundUrl));
            musicService.play(guild, user, soundUrl);
        } else {
            throw new JambotSoundBoardException("We have no sounds by said author.");
        }
    }
}
