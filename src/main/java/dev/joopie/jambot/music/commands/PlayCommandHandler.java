package dev.joopie.jambot.music.commands;

import dev.joopie.jambot.api.youtube.ApiYouTubeService;
import dev.joopie.jambot.api.youtube.dto.SearchResultDto;
import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.exceptions.JambotMusicPlayerException;
import dev.joopie.jambot.exceptions.JambotMusicServiceException;
import dev.joopie.jambot.exceptions.JambotYouTubeException;
import dev.joopie.jambot.music.GuildMusicService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PlayCommandHandler implements CommandHandler {
    private static final String COMMAND_NAME = "jam";
    private static final String COMMAND_OPTION_INPUT_NAME = "input";

    private static final Pattern URL_OR_ID_PATTERN = Pattern.compile("^(http(|s)://.*|[\\w\\-]{11})$");

    private final GuildMusicService musicService;
    private final ApiYouTubeService apiYouTubeService;

    @Override
    public Command.Type type() {
        return Command.Type.SLASH;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "play a track url or search for one")
                .addOption(
                        OptionType.STRING,
                        COMMAND_OPTION_INPUT_NAME,
                        "YouTube, Soundcloud etc url or search term",
                        true);
    }

    @Override
    public boolean shouldHandle(final CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(final CommandInteraction event) {
        final var inputOption = event.getOption(COMMAND_OPTION_INPUT_NAME);

        if (Objects.isNull(inputOption)) {
            return event.reply("U whut m8, provide an track url or search term.")
                    .setEphemeral(true);
        }

        event.deferReply().queue();

        try {
            final var input = inputOption.getAsString();

            if (URL_OR_ID_PATTERN.matcher(input).matches()) {
                musicService.play(event.getGuild(), event.getUser(), input);

                // TODO: unify responses for both url/search with embed which song got queued.
                return event.getHook()
                        .sendMessage("OK, we added the track to the queue! %s".formatted(input));
            } else {
                final var dto = apiYouTubeService.searchForSong(input);
                if (dto.isFound()) {
                    musicService.play(event.getGuild(), event.getUser(), dto.getVideoId());

                    return event.getHook()
                            .sendMessageEmbeds(createMessageEmbedOfSearchTrack(event.getGuild().getName(), dto));
                }
            }
        } catch (JambotMusicServiceException | JambotMusicPlayerException | JambotYouTubeException exception) {
            return event.getHook().sendMessage(exception.getMessage())
                    .setEphemeral(true);
        }

        return event.getHook()
                .sendMessage("Well... we tried, but were unable to find a track.")
                .setEphemeral(true);
    }

    private static MessageEmbed createMessageEmbedOfSearchTrack(final String guildName, final SearchResultDto dto) {
        return new EmbedBuilder()
                .setColor(new Color(0x0099FF))
                .setTitle("%s Track Queued".formatted(guildName))
                .setDescription(dto.getTitle())
                .setImage(dto.getThumbnailUrl())
                .build();
    }
}
