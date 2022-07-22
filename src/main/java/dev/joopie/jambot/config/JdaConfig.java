package dev.joopie.jambot.config;

import dev.joopie.jambot.JambotListener;
import dev.joopie.jambot.config.properties.JdaProperties;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
@EnableConfigurationProperties(JdaProperties.class)
public class JdaConfig {
    @Bean
    public JDA jda(final JdaProperties properties, final JambotListener listener) throws LoginException {
        return JDABuilder.createDefault(properties.getToken())
                .setMemberCachePolicy(MemberCachePolicy.VOICE)
                .addEventListeners(listener)
                .setActivity(Activity.playing("some music"))
                .build();
    }
}
