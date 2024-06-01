package org.zeveon.stockpricepredictionbot.state.handler;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;

/**
 * @author zejaven
 */
public interface CallbackQueryHandler extends UpdateHandler {
    default void handle(Update update, StockPricePredictionBot bot) {
        var callbackQuery = update.getCallbackQuery();
        if (callbackQuery != null) {
            handleCallbackQuery(callbackQuery);
        }
    }
    void handleCallbackQuery(CallbackQuery callbackQuery);
}
