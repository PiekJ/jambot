package dev.joopie.jambot.response;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;

public class MessageResponse {
    private MessageResponse() {

    }

    public static RestAction<Message> reply(final Message message, final String replyMessage) {
        return message.reply(new MessageBuilder().setContent(replyMessage).build());
    }

    public static RestAction<Message> reply(final Message message, final MessageEmbed replyMessageEmbed) {
        return message.replyEmbeds(replyMessageEmbed);
    }
}
