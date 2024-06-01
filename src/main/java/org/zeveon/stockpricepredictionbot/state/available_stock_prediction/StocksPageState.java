package org.zeveon.stockpricepredictionbot.state.available_stock_prediction;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;
import org.zeveon.stockpricepredictionbot.context.ChatContext;
import org.zeveon.stockpricepredictionbot.controller.UpdateController;
import org.zeveon.stockpricepredictionbot.model.CallbackCommand;
import org.zeveon.stockpricepredictionbot.state.BotState;
import org.zeveon.stockpricepredictionbot.state.handler.CallbackQueryHandler;

import java.util.Map;

import static org.zeveon.stockpricepredictionbot.state.ConvertKey.BASIC;
import static org.zeveon.stockpricepredictionbot.state.Variable.*;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createMessage;
import static org.zeveon.stockpricepredictionbot.util.TinkoffMessageUtil.LIMIT;

/**
 * @author Zejaven
 */
@Slf4j
public class StocksPageState extends BotState implements CallbackQueryHandler {

    private static final String BASIC_MESSAGE = """
            Here are available stocks for prediction:
            """;

    public StocksPageState(UpdateController updateController, StockPricePredictionBot bot) {
        super(updateController, bot);
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        var command = callbackQuery.getData();
        try {
            switch (CallbackCommand.fromText(command)) {
                case LEFT -> {
                    var page = (Integer) getStateVariable(PAGE);
                    if (page > 1) {
                        bot.putSessionVariable(PAGE, page - 1);
                        bot.nextState(new StocksPageState(updateController, bot), BASIC);
                    }
                }
                case RIGHT -> {
                    var page = (Integer) getStateVariable(PAGE);
                    var pageCount = (Long) getStateVariable(PAGE_COUNT);
                    if (pageCount == null || page < pageCount) {
                        bot.putSessionVariable(PAGE, page + 1);
                        bot.nextState(new StocksPageState(updateController, bot), BASIC);
                    }
                }
                case PAGE -> bot.nextState(new StocksNavigationState(updateController, bot), BASIC);
                case SEARCH -> bot.nextState(new StocksSearchState(updateController, bot), BASIC);
            }
        } catch (IllegalArgumentException e) {
            log.info("Chosen ticker: %s".formatted(command));
        }
    }

    @Override
    protected Map<Object, SendMessage> buildStateMessages() {
        var chatId = ChatContext.getInstance().getChatId();
        var page = (Integer) getStateVariable(PAGE);
        var searchText = (String) getStateVariable(SEARCH_TEXT);
        var stocksKeyboardMarkup = updateController.getStocksKeyboard(page, searchText);
        var pageCount = (stocksKeyboardMarkup.getRight() + LIMIT - 1) / LIMIT;
        bot.putSessionVariable(PAGE_COUNT, pageCount);
        return Map.of(
                BASIC, createMessage(chatId, BASIC_MESSAGE, stocksKeyboardMarkup.getLeft())
        );
    }
}
