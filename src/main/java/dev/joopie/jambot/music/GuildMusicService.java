package dev.joopie.jambot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.joopie.jambot.exceptions.JambotMusicPlayerException;
import dev.joopie.jambot.exceptions.JambotMusicServiceException;
import dev.joopie.jambot.music.dto.AudioTrackInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildMusicService {
    private final AudioPlayerManager audioPlayerManager;
    private final Map<Long, GuildMusicPlayer> musicPlayerMap = new HashMap<>();
    private final TaskScheduler taskScheduler;

    public void initializeGuildMusicService(final Guild guild) {
        if (musicPlayerMap.containsKey(guild.getIdLong())) {
            log.warn("Derp. How can we initialize guild `%s` twice?!".formatted(guild.getName()));
            return;
        }

        final var audioPlayer = audioPlayerManager.createPlayer();

        final var musicPlayer = new GuildMusicPlayer(guild, audioPlayer, taskScheduler);

        audioPlayer.addListener(new AudioPlayerListener(musicPlayer));

        musicPlayerMap.put(guild.getIdLong(), musicPlayer);
    }

    public void destroyGuildMusicService(final Guild guild) {
        final var musicPlayer = musicPlayerMap.remove(guild.getIdLong());

        if (musicPlayer == null) {
            log.warn("Whaaat? How can we destroy something we expected to be there when it's not?!");
            return;
        }

        musicPlayer.leave();
    }

    public void leaveWhenLeftAlone(final Guild guild) {
        final var musicPlayer = getAudioPlayer(guild);

        if (musicPlayer.isLeftAloneInVoiceChannel()) {
            musicPlayer.leave(true);
        }
    }

    public void leave(final Guild guild, final User user) {
        final var musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.leave();
    }

    public void play(final Guild guild, final User user, final String input) {
        final var musicPlayer = getAudioPlayer(guild);

        if (musicPlayer.isConnectedToVoiceChannel()) {
            assertUserInSameVoiceChannel(musicPlayer, user);
        } else {
            musicPlayer.joinVoiceChannelOfUser(user);
        }

        audioPlayerManager.loadItemOrdered(musicPlayer, input, new AudioTrackLoadResultHandler(musicPlayer));
    }

    public void pause(final Guild guild, final User user) {
        final var musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.pause();
    }

    public void stop(final Guild guild, final User user) {
        final var musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.stop();
    }

    public void next(final Guild guild, final User user) {
        final var musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.next();
    }

    public void clear(final Guild guild, final User user) {
        final var musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.clear();
    }

    public List<AudioTrackInfoDto> getQueuedAudioTracks(final Guild guild) {
        final var musicPlayer = getAudioPlayer(guild);

        final var index = new AtomicInteger();
        return musicPlayer.getQueuedAudioTracks().stream()
                .map(x -> AudioTrackInfoDto.builder()
                        .index(index.getAndIncrement())
                        .author(x.getInfo().author)
                        .title(x.getInfo().title)
                        .duration(x.getDuration())
                        .position(x.getPosition())
                        .build())
                .collect(Collectors.toList());
    }

    public void shuffleQueuedAudioTracks(final Guild guild, final User user) {
        final var musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.shuffleQueuedAudioTracks();
    }

    public void volume(final Guild guild, final User user, final int volume) {
        final var musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.volume(volume);
    }

    private GuildMusicPlayer getAudioPlayer(final Guild guild) {
        assertMusicPlayerForGuild(guild);

        return musicPlayerMap.get(guild.getIdLong());
    }

    private void assertMusicPlayerForGuild(final Guild guild) {
        if (!musicPlayerMap.containsKey(guild.getIdLong())) {
            throw new JambotMusicServiceException("Music player for guild `%s` not found.".formatted(guild.getName()));
        }
    }

    private void assertConnectedToVoiceChannel(final GuildMusicPlayer musicPlayer) {
        if (!musicPlayer.isConnectedToVoiceChannel()) {
            throw new JambotMusicPlayerException("Sad face. I'm not connected.");
        }
    }

    private void assertUserInSameVoiceChannel(final GuildMusicPlayer musicPlayer, final User user) {
        if (!musicPlayer.isSameVoiceChannelAsUser(user)) {
            throw new JambotMusicServiceException("Join the voice channel!");
        }
    }
}
