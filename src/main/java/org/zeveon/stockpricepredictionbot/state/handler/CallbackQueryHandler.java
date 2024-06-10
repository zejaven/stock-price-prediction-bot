package org.zeveon.stockpricepredictionbot.state.handler;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;

import static org.zeveon.stockpricepredictionbot.model.CallbackCommand.BACK;

/**
 * @author zejaven
 */
public interface CallbackQueryHandler extends UpdateHandler {
    default void handle(Update update, StockPricePredictionBot bot) {
        var callbackQuery = update.getCallbackQuery();
        if (callbackQuery != null) {
            if (BACK.getText().equals(callbackQuery.getData())) {
                bot.previousState();
            } else {
                handleCallbackQuery(callbackQuery);
            }
        }
    }
    void handleCallbackQuery(CallbackQuery callbackQuery);
}
