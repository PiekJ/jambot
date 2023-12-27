package dev.joopie.jambot.music;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuildProvider {
    private final JDA jda;

    public Guild getGuild(long id) {
        return jda.getGuildById(id);
    }
}
