package dev.joopie.jambot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AudioTrackLoadResultHandler implements AudioLoadResultHandler {
    private final GuildMusicPlayer musicPlayer;
    private final MetaData metaData;

    @Override
    public void trackLoaded(final AudioTrack audioTrack) {
        audioTrack.setUserData(metaData);
        log.info("Audio track queued `{}}`.", audioTrack.getInfo().title);
        musicPlayer.play(audioTrack);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist audioPlaylist) {
        if (audioPlaylist.isSearchResult()) {
            trackLoaded(audioPlaylist.getSelectedTrack());
        }
        else {
            log.info("Playlist of {} audio tracks loaded.", audioPlaylist.getTracks().size());
            audioPlaylist.getTracks().forEach(musicPlayer::play);
        }
    }

    @Override
    public void noMatches() {
        log.info("No track found.");
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        log.warn("We tried our best, but that wasn't enough :(", exception);
    }

    public record MetaData(String userId, String mediaId) {
    }
}
