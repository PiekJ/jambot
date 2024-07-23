package dev.joopie.jambot.music.command;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.music.AudioTrackInfoDto;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QueueCommandHandler implements CommandHandler {
    private static final int FIELD_VALUE_MAX_LENGTH = 66;

    private static final String COMMAND_NAME = "queue";

    private final GuildMusicService musicService;

    @Override
    public Command.Type type() {
        return Command.Type.SLASH;
    }

    @Override
    public CommandData registerCommand() {
        return Commands.slash(COMMAND_NAME, "List all tracks currently queued");
    }

    @Override
    public boolean shouldHandle(final CommandInteractionPayload event) {
        return COMMAND_NAME.equals(event.getName());
    }

    @Override
    public RestAction<?> handle(final CommandInteraction event) {
        final var dtos = musicService.getQueuedAudioTracks(event.getGuild());

        final var builder = new EmbedBuilder();
        builder.setColor(new Color(0x0099FF));
        builder.setTitle("%s's Track Queue List".formatted(event.getGuild().getName()));
        builder.setTimestamp(OffsetDateTime.now());

        if (dtos.isEmpty()) {
            builder.setDescription("Currently no tracks playing...");

            return event.replyEmbeds(builder.build());
        }

        builder.setDescription("Current tracks in the queue in order:");
        builder.setFooter("Total tracks: %s. Total queue time: %s."
                .formatted(
                        dtos.size(),
                        formatMillisToHumanTime(dtos.stream()
                                .mapToLong(AudioTrackInfoDto::getPlayTimeLeft)
                                .sum())));

        final var currentlyPlayingDto = dtos.remove(0);

        builder.addField(
                "Currently Playing",
                "%s : %s".formatted(currentlyPlayingDto.getAuthor(), currentlyPlayingDto.getTitle()),
                false);

        builder.addField(
                "Play Time Left",
                formatMillisToHumanTime(currentlyPlayingDto.getPlayTimeLeft()),
                false);

        if (dtos.isEmpty()) {
            return event.replyEmbeds(builder.build());
        }

        final var trackNameStringBuilder = new StringBuilder();
        final var trackDurationStringBuilder = new StringBuilder();

        for (final var dto : dtos) {
            if (dto.getIndex() >= 11) {
                break;
            }

            trackNameStringBuilder
                    .append(stringWithMaxLength(
                            "%02d) %s : %s".formatted(dto.getIndex(), dto.getAuthor(), dto.getTitle()),
                            FIELD_VALUE_MAX_LENGTH))
                    .append("\r%n");
            trackDurationStringBuilder
                    .append(formatMillisToHumanTime(dto.getDuration()))
                    .append("\r%n");
        }

        builder.addField("Track List", trackNameStringBuilder.toString(), true);
        builder.addField("Duration", trackDurationStringBuilder.toString(), true);

        return event.replyEmbeds(builder.build());
    }

    private static String formatMillisToHumanTime(final long millis) {
        final var duration = Duration.ofMillis(millis);
        return "%02d:%02d:%02d".formatted(duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    private static String stringWithMaxLength(final String string, final int maxLength) {
        if (string.length() <= maxLength) {
            return string;
        }

        return string.substring(0, maxLength - 1).concat("…");
    }
}
