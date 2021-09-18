package dev.joopie.jambot.response;

import lombok.Builder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;

public class ReactionResponse {
    private ReactionResponse() {

    }

    public static RestAction<Void> build(final Message message, final String reaction) {
        return message.addReaction(reaction);
    }

    @Builder(builderMethodName = "ok")
    public static RestAction<Void> ok(final Message message) {
        return build(message, "\uD83D\uDC4C");
    }

    @Builder(builderMethodName = "fail")
    public static RestAction<Void> fail(final Message message) {
        return build(message, "❌");
    }
}
