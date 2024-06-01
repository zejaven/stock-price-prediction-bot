package org.zeveon.stockpricepredictionbot.state.handler;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;

/**
 * @author zejaven
 */
public interface UpdateHandler {
    void handle(Update update, StockPricePredictionBot bot);

    void addSentMessage(Message message);

    void deletePreviousMessages();
}
