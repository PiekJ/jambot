package dev.joopie.jambot.listeners;

import dev.joopie.jambot.models.SpotifyToYoutube;
import dev.joopie.jambot.service.SpotifyToYoutubeService;
import dev.joopie.jambot.util.YouTubeLinkParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class PlayReactionEventListener extends ListenerAdapter {
    private final SpotifyToYoutubeService spotifyToYoutubeService;

    public PlayReactionEventListener(SpotifyToYoutubeService spotifyToYoutubeService) {
        this.spotifyToYoutubeService = spotifyToYoutubeService;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;

        String emoji = event.getReaction().getEmoji().getAsReactionCode();
        Message message = event.retrieveMessage().complete();

        if (emoji.equals("✅")) { // ✅
            // Confirm the link
            message.getChannel().sendMessage("Link confirmed!").queue();
        } else if (emoji.equals("❌")) { // ❌
            // Reject the link and delete the SpotifyToYoutube record
            SpotifyToYoutube spotifyToYoutube = getSpotifyToYoutubeFromMessage(message);
            if (spotifyToYoutube != null) {
                spotifyToYoutubeService.delete(spotifyToYoutube);
                message.getChannel().sendMessage("Thanks for your input! \uD83E\uDD29 Only together we can make Jambot better. I will try to get another link next time.").queue();
            }
        }
    }

    private SpotifyToYoutube getSpotifyToYoutubeFromMessage(Message message) {
        return spotifyToYoutubeService.findByYoutubeId(YouTubeLinkParser.extractYouTubeId(message.getContentStripped()));
    }
}
