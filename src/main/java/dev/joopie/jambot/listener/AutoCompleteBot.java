package dev.joopie.jambot.listener;

import dev.joopie.jambot.repository.artist.ArtistRepository;
import dev.joopie.jambot.repository.track.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutoCompleteBot extends ListenerAdapter {
   private final ArtistRepository artistRepository;
   private final TrackRepository trackRepository;

   @Transactional
   @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("search") && event.getFocusedOption().getName().equals("artist")) {
            var options = artistRepository.findAll().stream()
                    .filter(artist -> artist.getName().startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                    .map(artist -> new Command.Choice(artist.getName(), artist.getName())) // map the words to choices
                            .toList();
            event.replyChoices(options).queue();
        }

        if (event.getName().equals("search") && event.getFocusedOption().getName().equals("songname")) {
            var options = trackRepository.findAll().stream()
                    .filter(artistTrack -> artistTrack.getArtists().stream()
                            .anyMatch(artist -> artist.getName().contains(event.getOptions().getFirst().getAsString())))
                    .filter(track -> track.getName().startsWith(event.getFocusedOption().getValue()))
                    .map(track -> new Command.Choice(track.getName(), track.getName()))
                            .toList();
            event.replyChoices(options).queue();
        }
    }
}