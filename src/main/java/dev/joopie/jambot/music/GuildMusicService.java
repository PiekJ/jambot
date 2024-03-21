package dev.joopie.jambot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuildMusicService {
    private static final String RICKASTLEY = "https://youtu.be/E9TYbwI8xsE";
    private final AudioPlayerManager audioPlayerManager;
    private final Map<Long, GuildMusicPlayer> musicPlayerMap = new HashMap<>();
    private final TaskScheduler taskScheduler;

    public void initializeGuildMusicService(final Guild guild) {
        if (musicPlayerMap.containsKey(guild.getIdLong())) {
            log.warn("Derp. How can we initialize guild `{}` twice?!", guild.getName());
            return;
        }

        final var audioPlayer = audioPlayerManager.createPlayer();

        final var musicPlayer = new GuildMusicPlayer(
                guild.getIdLong(),
                audioPlayer,
                taskScheduler,
                new GuildProvider(guild.getJDA()));

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

    public void leave(final Member member) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        assertConnectedToVoiceChannel(musicPlayer);
        assertMemberInSameVoiceChannel(musicPlayer, member);

        musicPlayer.leave();
    }

    public void play(final Member member, final String input) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        if (musicPlayer.isConnectedToVoiceChannel()) {
            assertMemberInSameVoiceChannel(musicPlayer, member);
        } else {
            var localDate = LocalDate.now();
            if (localDate.getMonthValue() == 4 && localDate.getDayOfMonth() == 1) { // April Fools Joke -- Only if the bot gets connected in a voice channel
                audioPlayerManager.loadItemOrdered(musicPlayer, RICKASTLEY, new AudioTrackLoadResultHandler(musicPlayer));
            }
            musicPlayer.joinVoiceChannelOfMember(member);
        }

        audioPlayerManager.loadItemOrdered(musicPlayer, input, new AudioTrackLoadResultHandler(musicPlayer));
    }

    public void pause(final Member member) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        assertConnectedToVoiceChannel(musicPlayer);
        assertMemberInSameVoiceChannel(musicPlayer, member);

        musicPlayer.pause();
    }

    public void stop(final Member member) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        assertConnectedToVoiceChannel(musicPlayer);
        assertMemberInSameVoiceChannel(musicPlayer, member);

        musicPlayer.stop();
    }

    public void next(final Member member) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        assertConnectedToVoiceChannel(musicPlayer);
        assertMemberInSameVoiceChannel(musicPlayer, member);

        musicPlayer.next();
    }

    public void remove(final Member member, final int songIndex) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        assertConnectedToVoiceChannel(musicPlayer);
        assertMemberInSameVoiceChannel(musicPlayer, member);

        musicPlayer.remove(songIndex);
    }

    public void clear(final Member member) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        assertConnectedToVoiceChannel(musicPlayer);
        assertMemberInSameVoiceChannel(musicPlayer, member);

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

    public void shuffleQueuedAudioTracks(final Member member) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        assertConnectedToVoiceChannel(musicPlayer);
        assertMemberInSameVoiceChannel(musicPlayer, member);

        musicPlayer.shuffleQueuedAudioTracks();
    }

    public void volume(final Member member, final int volume) {
        final var musicPlayer = getAudioPlayer(member.getGuild());

        assertConnectedToVoiceChannel(musicPlayer);
        assertMemberInSameVoiceChannel(musicPlayer, member);

        musicPlayer.volume(volume);
    }

    public double countGuilds() {
        return musicPlayerMap.size();
    }

    public double countGuildsConnectedToVoice() {
        return musicPlayerMap.values().stream()
                .filter(GuildMusicPlayer::isConnectedToVoiceChannel)
                .count();
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

    private void assertMemberInSameVoiceChannel(final GuildMusicPlayer musicPlayer, final Member member) {
        if (!musicPlayer.isSameVoiceChannelAsMember(member)) {
            throw new JambotMusicServiceException("Join the voice channel!");
        }
    }
}
