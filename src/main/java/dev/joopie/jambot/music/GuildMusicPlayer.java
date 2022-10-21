package dev.joopie.jambot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.joopie.jambot.exceptions.JambotMusicPlayerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
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

    private final Guild guild;
    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> audioTrackQueue = new LinkedBlockingQueue<>();
    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledLeaveTask;

    public void joinVoiceChannelOfUser(final User user) {
        if (guild.getAudioManager().isConnected()) {
            log.warn("Already connected to a voice channel in guild `%s`.".formatted(guild.getName()));
            throw new JambotMusicPlayerException("I'm already connected!");
        }

        final var member = guild.getMemberById(user.getIdLong());
        if (Objects.isNull(member)) {
            log.warn("User `%s` is not a member of guild `%s` or not joined any voice channel.".formatted(
                    user.getName(),
                    guild.getName()));
            throw new JambotMusicPlayerException("It appears I can't find you on a voice channel in the server...");
        }

        final var voiceState = member.getVoiceState();

        if (Objects.isNull(voiceState) || !voiceState.inAudioChannel()) {
            log.warn("User `%s` is not in voice channel.".formatted(user.getName()));
            throw new JambotMusicPlayerException("Are you sure you're in a voice channel, yes?");
        }

        join(voiceState.getChannel());
    }

    public void join(final AudioChannelUnion voiceChannel) {
        if (isConnectedToVoiceChannel()) {
            throw new JambotMusicPlayerException("I'm already connected!");
        }

        volume(100);

        guild.getAudioManager().openAudioConnection(voiceChannel);

        if (!(guild.getAudioManager().getSendingHandler() instanceof AudioPlayerAudioSendHandler)) {
            guild.getAudioManager().setSendingHandler(new AudioPlayerAudioSendHandler(audioPlayer));
        }
    }

    public void leave() {
        leave(false);
    }

    public void leave(final boolean scheduleTask) {
        if (isConnectedToVoiceChannel()) {
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
            }
            else {
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

    public void clear() {
        audioTrackQueue.clear();
    }

    public List<AudioTrack> getQueuedAudioTracks() {
        if (audioPlayer.getPlayingTrack() == null) {
            return Collections.emptyList();
        }

        final var result = new ArrayList<>(audioTrackQueue);
        result.add(0, audioPlayer.getPlayingTrack());
        return result;
    }

    public synchronized void shuffleQueuedAudioTracks() {
        final var temp = new ArrayList<>(audioTrackQueue);
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
        return isConnectedToVoiceChannel() &&
                guild.getAudioManager().getConnectedChannel().getMembers().size() <= 1;
    }

    public boolean isConnectedToVoiceChannel() {
        return guild.getAudioManager().isConnected();
    }

    public boolean isSameVoiceChannelAsUser(final User user) {
        return isConnectedToVoiceChannel() &&
                guild.getAudioManager().getConnectedChannel().getMembers().stream()
                        .anyMatch(x -> Objects.equals(x.getUser(), user));
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
}
