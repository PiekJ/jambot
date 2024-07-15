package dev.joopie.jambot.listeners;

import dev.joopie.jambot.models.TrackSource;
import dev.joopie.jambot.service.TrackSourceService;
import dev.joopie.jambot.util.YouTubeLinkParser;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class PlayReactionEventListener extends ListenerAdapter {
    private final TrackSourceService trackSourceService;

    public PlayReactionEventListener(TrackSourceService trackSourceService) {
        this.trackSourceService = trackSourceService;
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
            TrackSource trackSource = getSpotifyToYoutubeFromMessage(message);
            if (trackSource != null) {
                trackSourceService.delete(trackSource);
                message.getChannel().sendMessage("Thanks for your input! \uD83E\uDD29 Only together we can make Jambot better. I will try to get another link next time.").queue();
            }
        }
    }

    private TrackSource getSpotifyToYoutubeFromMessage(Message message) {
        return trackSourceService.findByYoutubeId(YouTubeLinkParser.extractYouTubeId(message.getContentStripped()));
    }
}
