package dev.joopie.jambot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@RequiredArgsConstructor
public class GuildMusicPlayer {
    private static final int VOLUME_MAX = 50;
    private static final int SCHEDULE_LEAVE_MINUTES = 5;

    private final long guildId;
    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> audioTrackQueue = new LinkedBlockingQueue<>();
    private final TaskScheduler taskScheduler;
    private final GuildProvider guildProvider;

    private ScheduledFuture<?> scheduledLeaveTask;

    public void joinVoiceChannelOfUser(final User user) {
        final var guild = guildProvider.getGuild(guildId);
        joinVoiceChannelOfUser(user, guild);
    }

    public void joinVoiceChannelOfMember(final Member member) {
        final var guild = guildProvider.getGuild(guildId);
        joinVoiceChannelOfMember(member, guild);
    }

    public void join(final AudioChannelUnion voiceChannel) {
        final var guild = guildProvider.getGuild(guildId);
        join(voiceChannel, guild);
    }

    public void leave() {
        leave(false);
    }

    public void leave(final boolean scheduleTask) {
        final var guild = guildProvider.getGuild(guildId);
        if (isConnectedToVoiceChannel(guild)) {
            if (scheduleTask) {
                scheduleLeaveTask();
                return;
            }

            guild.getAudioManager().closeAudioConnection();
            audioPlayer.checkCleanup(0);
            clear();
        } else {
            throw new JambotMusicPlayerException();
        }
    }

    public void play(final AudioTrack audioTrack) {
        if (!audioPlayer.startTrack(audioTrack, true)) {
            audioTrackQueue.offer(audioTrack);
        }

        cancelLeaveTask();
    }

    public void pause() {
        if (audioPlayer.getPlayingTrack() != null) {
            audioPlayer.setPaused(!audioPlayer.isPaused());

            if (audioPlayer.isPaused()) {
                scheduleLeaveTask();
            } else {
                cancelLeaveTask();
            }
        }
    }

    public void stop() {
        audioPlayer.stopTrack();

        scheduleLeaveTask();
    }

    public void next() {
        if (audioPlayer.startTrack(audioTrackQueue.poll(), false)) {
            return;
        }

        scheduleLeaveTask();
    }

    public synchronized void remove(final int songIndex) {
        if (songIndex >= audioTrackQueue.size()) {
            throw new JambotMusicPlayerException("Track index is waaaaay to big for queued songs.");
        }

        if (songIndex < 0) {
            throw new JambotMusicPlayerException("Track index is a bit small...");
        }

        var iteratorIndex = 0;

        final var iterator = audioTrackQueue.iterator();
        while (iterator.hasNext()) {
            iterator.next();

            if (iteratorIndex++ == songIndex) {
                iterator.remove();

                break;
            }
        }
    }

    public void clear() {
        audioTrackQueue.clear();
    }

    public List<AudioTrack> getQueuedAudioTracks() {
        if (audioPlayer.getPlayingTrack() == null) {
            return Collections.emptyList();
        }

        final List<AudioTrack> result = new ArrayList<>(audioTrackQueue);
        result.add(0, audioPlayer.getPlayingTrack());
        return result;
    }

    public synchronized void shuffleQueuedAudioTracks() {
        final List<AudioTrack> temp = new ArrayList<>(audioTrackQueue);
        Collections.shuffle(temp);
        audioTrackQueue.clear();
        audioTrackQueue.addAll(temp);
    }

    public void volume(int volume) {
        volume = Math.min(Math.max(volume, 0), 200);
        volume = (int) Math.floor(VOLUME_MAX / 100.0 * volume);
        audioPlayer.setVolume(volume);
    }

    public boolean isLeftAloneInVoiceChannel() {
        return isLeftAloneInVoiceChannel(guildProvider.getGuild(guildId));
    }

    public boolean isConnectedToVoiceChannel() {
        return isConnectedToVoiceChannel(guildProvider.getGuild(guildId));
    }

    public boolean isSameVoiceChannelAsMember(final Member member) {
        return isSameVoiceChannelAsMember(member, guildProvider.getGuild(guildId));
    }

    private void joinVoiceChannelOfUser(final User user, final Guild guild) {
        final var member = guild.retrieveMember(user).complete();
        if (Objects.isNull(member)) {
            log.warn("User `{}` is not a member of guild `{}` or not joined any voice channel.",
                    user.getName(),
                    guild.getName());
            throw new JambotMusicPlayerException("It appears I can't find you on a voice channel in the server...");
        }

        joinVoiceChannelOfMember(member, guild);
    }

    private void joinVoiceChannelOfMember(final Member member, final Guild guild) {
        assertMemberInGuild(member, guild);

        if (guild.getAudioManager().isConnected()) {
            log.warn("Already connected to a voice channel in guild `{}`.", guild.getName());
            throw new JambotMusicPlayerException("I'm already connected!");
        }

        final var voiceState = member.getVoiceState();

        if (Objects.isNull(voiceState) || !voiceState.inAudioChannel()) {
            log.warn("User `{}` is not in voice channel.", member.getEffectiveName());
            throw new JambotMusicPlayerException("Are you sure you're in a voice channel, yes?");
        }

        join(voiceState.getChannel(), guild);
    }

    private void join(final AudioChannelUnion voiceChannel, final Guild guild) {
        if (isConnectedToVoiceChannel(guild)) {
            throw new JambotMusicPlayerException("I'm already connected!");
        }

        volume(100);

        try {
            guild.getAudioManager().openAudioConnection(voiceChannel);
        } catch (InsufficientPermissionException e) {
            throw new JambotMusicPlayerException(
                    "I don't have enough permissions to join you, missing `%s`.".formatted(e.getPermission()));
        }

        if (!(guild.getAudioManager().getSendingHandler() instanceof AudioPlayerAudioSendHandler)) {
            guild.getAudioManager().setSendingHandler(new AudioPlayerAudioSendHandler(audioPlayer));
        }
    }

    private synchronized void scheduleLeaveTask() {
        if (scheduledLeaveTask == null) {
            log.info("Scheduled leave task.");
            scheduledLeaveTask = taskScheduler.schedule(
                    this::leave,
                    Instant.now().plus(SCHEDULE_LEAVE_MINUTES, ChronoUnit.MINUTES));
        }
    }

    private synchronized void cancelLeaveTask() {
        if (scheduledLeaveTask == null) {
            return;
        }

        if (!scheduledLeaveTask.isCancelled() && !scheduledLeaveTask.isDone()) {
            log.info("Cancelled scheduled leave task.");
            scheduledLeaveTask.cancel(false);
        }

        scheduledLeaveTask = null;
    }

    private void assertMemberInGuild(final Member member, final Guild guild) {
        if (guild.getIdLong() != member.getGuild().getIdLong()) {
            throw new JambotMusicPlayerException(
                    "Member `%s` not part of guild `%s`.".formatted(
                            member.getEffectiveName(),
                            guild.getName()));
        }
    }

    private boolean isLeftAloneInVoiceChannel(final Guild guild) {
        return isConnectedToVoiceChannel(guild) &&
                guild.getAudioManager().getConnectedChannel().getMembers().size() <= 1;
    }

    private boolean isConnectedToVoiceChannel(final Guild guild) {
        return guild.getAudioManager().isConnected();
    }

    private boolean isSameVoiceChannelAsMember(final Member member, final Guild guild) {
        return isConnectedToVoiceChannel(guild) &&
                guild.getAudioManager().getConnectedChannel().getMembers().stream()
                        .anyMatch(member::equals);
    }
}
