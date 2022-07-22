package dev.joopie.jambot.music.commands;

import dev.joopie.jambot.command.CommandHandler;
import dev.joopie.jambot.music.GuildMusicService;
import dev.joopie.jambot.music.dto.AudioTrackInfoDto;
import dev.joopie.jambot.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class QueueCommandHandler implements CommandHandler {
    private static final int FIELD_VALUE_MAX_LENGTH = 66;
    private static final Pattern SHOULD_HANDLE_PATTERN = Pattern.compile("^-(q|queue)$");

    private final GuildMusicService musicService;

    @Override
    public boolean shouldHandle(final GuildMessageReceivedEvent event) {
        return SHOULD_HANDLE_PATTERN.matcher(event.getMessage().getContentRaw()).matches();
    }

    @Override
    public RestAction<?> handle(final GuildMessageReceivedEvent event) {
        final List<AudioTrackInfoDto> dtos = musicService.getQueuedAudioTracks(event.getGuild());

        final EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(new Color(0x0099FF));
        builder.setTitle("%s's Track Queue List".formatted(event.getGuild().getName()));
        builder.setTimestamp(OffsetDateTime.now());

        if (dtos.isEmpty()) {
            builder.setDescription("Currently no tracks playing...");

            return MessageResponse.reply(event.getMessage(), builder.build());
        }

        builder.setDescription("Current tracks in the queue in order:");
        builder.setFooter("Total tracks: %s. Total queue time: %s."
                .formatted(
                        dtos.size(),
                        formatMillisToHumanTime(dtos.stream()
                                .mapToLong(AudioTrackInfoDto::getPlayTimeLeft)
                                .sum())));

        final AudioTrackInfoDto currentlyPlayingDto = dtos.remove(0);

        builder.addField(
                "Currently Playing",
                "%s : %s".formatted(currentlyPlayingDto.getAuthor(), currentlyPlayingDto.getTitle()),
                false);

        builder.addField(
                "Play Time Left",
                formatMillisToHumanTime(currentlyPlayingDto.getPlayTimeLeft()),
                false);

        if (dtos.isEmpty()) {
            return MessageResponse.reply(event.getMessage(), builder.build());
        }

        final StringBuilder trackNameStringBuilder = new StringBuilder();
        final StringBuilder trackDurationStringBuilder = new StringBuilder();

        for (AudioTrackInfoDto dto : dtos) {
            if (dto.getIndex() >= 11) {
                break;
            }

            trackNameStringBuilder
                    .append(stringWithMaxLength(
                            "%02d) %s : %s".formatted(dto.getIndex(), dto.getAuthor(), dto.getTitle()),
                            FIELD_VALUE_MAX_LENGTH))
                    .append("\r\n");
            trackDurationStringBuilder
                    .append(formatMillisToHumanTime(dto.getDuration()))
                    .append("\r\n");
        }

        builder.addField("Track List", trackNameStringBuilder.toString(), true);
        builder.addField("Duration", trackDurationStringBuilder.toString(), true);

        return MessageResponse.reply(event.getMessage(), builder.build());
    }

    private static String formatMillisToHumanTime(final long millis) {
        Duration duration = Duration.ofMillis(millis);
        return "%02d:%02d:%02d".formatted(duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    private static String stringWithMaxLength(final String string, final int maxLength) {
        if (string.length() <= maxLength) {
            return string;
        }

        return string.substring(0, maxLength - 1).concat("…");
    }
}
