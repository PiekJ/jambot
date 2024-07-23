package dev.joopie.jambot.listener;

import dev.joopie.jambot.model.TrackSource;
import dev.joopie.jambot.service.TrackSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@RequiredArgsConstructor
@Component
public class SearchFeedbackEventListener extends ListenerAdapter {
    private final TrackSourceService trackSourceService;
    private static final String REGEX_1 = "youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+)";
    private static final String REGEX_2 = "youtu\\.be/([a-zA-Z0-9_-]+)";
    private static final Pattern PATTERN_1 = Pattern.compile(REGEX_1);
    private static final Pattern PATTERN_2 = Pattern.compile(REGEX_2);

    private Optional<TrackSource> getSpotifyToYoutubeFromMessage(Message message) {
        return extractYouTubeId(message.getContentStripped()).flatMap(trackSourceService::findByYoutubeId);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getUser().isBot()) return;

        if (!event.getUser().equals(event.getMessage().getInteraction().getUser())) {
            event.reply(":rotating_light: You *nasty* boy! You are trying to react to a command that is not initiated by you. I see everything :wink:").setEphemeral(true).queue();
            return;
        }
        var componentId = event.getComponentId();
        if (componentId.equals("accept")) {
            event.reply("You accepted! :star_struck:  Great to hear that my Dora The Exploring did work out for you. Have fun listening to this banger! :muscle_tone2:").setEphemeral(true).queue();
        } else if (componentId.equals("reject")) {
            // Reject the link and delete the SpotifyToYoutube record
            final var trackSource = getSpotifyToYoutubeFromMessage(event.getMessage());

            if (trackSource.isPresent()) {
                trackSource.get().setRejected(true);
                trackSourceService.save(trackSource.get());
                event.reply("Thanks for your input! :pray_tone1:  Only together we can make Jambot better. I will try to get another link next time. :wink:").setEphemeral(true).queue();
            } else {
                log.error("Hmm. Tracksource is empty. This is strange...");
                event.reply("Something went wrong on our side with handling your request. Try again later!").setEphemeral(true).queue();
            }
        }
        event.getMessage().editMessageComponents().queue();
    }

    private static Optional<String> extractYouTubeId(String url) {


        var matcher1 = PATTERN_1.matcher(url);
        var matcher2 = PATTERN_2.matcher(url);

        if (matcher1.find()) {
            // Extract the ID from youtube.com/watch?v=...
            return Optional.of(matcher1.group(1));
        } else if (matcher2.find()) {
            // Extract the ID from youtu.be/...
            return Optional.of(matcher2.group(1));
        }
        return Optional.empty();
    }
}

