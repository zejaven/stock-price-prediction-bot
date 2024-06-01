package org.zeveon.stockpricepredictionbot.state.handler;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;

/**
 * @author zejaven
 */
public interface MessageHandler extends UpdateHandler {
    default void handle(Update update, StockPricePredictionBot bot) {
        var message = update.getMessage();
        if (message != null) {
            addSentMessage(message);
            handleMessage(message);
        }
    }
    void handleMessage(Message message);
}
