package dev.joopie.jambot.listeners;

import dev.joopie.jambot.models.TrackSource;
import dev.joopie.jambot.service.TrackSourceService;
import dev.joopie.jambot.util.YouTubeLinkParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Component
public class SearchFeedbackEventListener extends ListenerAdapter {
    private final TrackSourceService trackSourceService;

    private Optional<TrackSource> getSpotifyToYoutubeFromMessage(Message message) {
        Optional<String> youtubeId = YouTubeLinkParser.extractYouTubeId(message.getContentStripped());
        return youtubeId.map(trackSourceService::findByYoutubeId);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getUser().isBot()) return;
        String componentId = event.getComponentId();
        if (componentId.equals("accept")) {
            event.reply("You accepted! :star_struck: Great to hear that my Dora The Exploring did work out for you. Have fun listening to this banger! :muscle_tone2:").setEphemeral(true).queue();
        } else if (componentId.equals("reject")) {
            // Reject the link and delete the SpotifyToYoutube record
            final var trackSource = getSpotifyToYoutubeFromMessage(event.getMessage());

            if (trackSource.isPresent()) {
                trackSource.get().setRejected(true);
                trackSourceService.save(trackSource.get());
                event.reply("Thanks for your input! :pray_tone1: Only together we can make Jambot better. I will try to get another link next time. :wink:").setEphemeral(true).queue();
            } else {
                log.error("Hmm. Tracksource is empty. This is strange...");
                event.reply("Something went wrong on our side with handling your request. Try again later!").setEphemeral(true).queue();
            }
        }
    }
}

