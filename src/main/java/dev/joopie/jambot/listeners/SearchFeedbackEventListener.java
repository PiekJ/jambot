package dev.joopie.jambot.listeners;

import dev.joopie.jambot.models.TrackSource;
import dev.joopie.jambot.service.TrackSourceService;
import dev.joopie.jambot.util.YouTubeLinkParser;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchFeedbackEventListener extends ListenerAdapter {
    private final TrackSourceService trackSourceService;

    public SearchFeedbackEventListener(TrackSourceService trackSourceService) {
        this.trackSourceService = trackSourceService;
    }


    private TrackSource getSpotifyToYoutubeFromMessage(Message message) {
        return trackSourceService.findByYoutubeId(YouTubeLinkParser.extractYouTubeId(message.getContentStripped()));
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getUser().isBot()) return;

        String componentId = event.getComponentId();
        log.info("Button interaction with id: {}", componentId);
        if (componentId.equals("accept")) {
            event.reply("You accepted! :star_struck: Great to hear that my Dora The Exploring did work out for you. Have fun listening to this banger! :muscle_tone2:").setEphemeral(true).queue();
        } else if (componentId.equals("reject")) {
            // Reject the link and delete the SpotifyToYoutube record
            TrackSource trackSource = getSpotifyToYoutubeFromMessage(event.getMessage());
            if (trackSource != null) {
                trackSource.setRejected(true);
                trackSourceService.save(trackSource);

                event.reply("Thanks for your input! :pray_tone1: Only together we can make Jambot better. I will try to get another link next time. :wink:").setEphemeral(true).queue();
            }
        }
    }
}
