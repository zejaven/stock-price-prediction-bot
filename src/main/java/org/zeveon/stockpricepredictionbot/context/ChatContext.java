package org.zeveon.stockpricepredictionbot.context;

import lombok.Getter;

import java.util.Objects;

/**
 * @author Stanislav Vafin
 */
@Getter
public class ChatContext {

    private static final ThreadLocal<ChatContext> INSTANCE = new ThreadLocal<>();

    private final Long chatId;

    private ChatContext(Long chatId) {
        this.chatId = chatId;
    }

    public static ChatContext getInstance() {
        return INSTANCE.get();
    }

    public static void setInstance(Long chatId) {
        INSTANCE.set(new ChatContext(chatId));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var that = (ChatContext) obj;
        return Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }
}
