package dev.joopie.jambot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.joopie.jambot.exceptions.JambotMusicPlayerException;
import dev.joopie.jambot.exceptions.JambotMusicServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildMusicService {
    private final AudioPlayerManager audioPlayerManager;
    private final Map<Long, GuildMusicPlayer> musicPlayerMap = new HashMap<>();

    public void initializeGuildMusicService(final Guild guild) {
        if (musicPlayerMap.containsKey(guild.getIdLong())) {
            log.warn("Derp. How can we initialize guild `%s` twice?!".formatted(guild.getName()));
            return;
        }

        AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
        audioPlayer.setVolume(GuildMusicPlayer.VOLUME_MAX);

        GuildMusicPlayer musicPlayer = new GuildMusicPlayer(guild, audioPlayer);

        audioPlayer.addListener(new AudioPlayerListener(musicPlayer));

        musicPlayerMap.put(guild.getIdLong(), musicPlayer);
    }

    public void destroyGuildMusicService(final Guild guild) {
        GuildMusicPlayer musicPlayer = musicPlayerMap.remove(guild.getIdLong());

        if (musicPlayer == null) {
            log.warn("Whaaat? How can we destroy something we expected to be there when it's not?!");
            return;
        }

        musicPlayer.leave();
    }

    public void leave(Guild guild, User user) {
        GuildMusicPlayer musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.leave();
    }

    public void play(Guild guild, User user, String input) {
        GuildMusicPlayer musicPlayer = getAudioPlayer(guild);

        if (musicPlayer.isConnectedToVoiceChannel()) {
            assertUserInSameVoiceChannel(musicPlayer, user);
        } else {
            musicPlayer.joinVoiceChannelOfUser(user);
        }

        audioPlayerManager.loadItemOrdered(musicPlayer, input, new AudioTrackLoadResultHandler(musicPlayer));
    }

    public void pause(Guild guild, User user) {
        GuildMusicPlayer musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.pause();
    }

    public void stop(Guild guild, User user) {
        GuildMusicPlayer musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.stop();
    }

    public void next(Guild guild, User user) {
        GuildMusicPlayer musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.next();
    }

    public void clear(Guild guild, User user) {
        GuildMusicPlayer musicPlayer = getAudioPlayer(guild);

        assertConnectedToVoiceChannel(musicPlayer);
        assertUserInSameVoiceChannel(musicPlayer, user);

        musicPlayer.clear();
    }

    public void volume(Guild guild, User user, int volume) {
        GuildMusicPlayer musicPlayer = getAudioPlayer(guild);

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
