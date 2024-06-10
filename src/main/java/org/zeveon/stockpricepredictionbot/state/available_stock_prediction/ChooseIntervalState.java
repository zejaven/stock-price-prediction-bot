package org.zeveon.stockpricepredictionbot.state.available_stock_prediction;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;
import org.zeveon.stockpricepredictionbot.context.ChatContext;
import org.zeveon.stockpricepredictionbot.controller.UpdateController;
import org.zeveon.stockpricepredictionbot.model.CallbackCommand;
import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppRequest;
import org.zeveon.stockpricepredictionbot.state.BotState;
import org.zeveon.stockpricepredictionbot.state.Final;
import org.zeveon.stockpricepredictionbot.state.Variable;
import org.zeveon.stockpricepredictionbot.state.handler.CallbackQueryHandler;

import java.util.Map;

import static org.zeveon.stockpricepredictionbot.state.ConvertKey.BASIC;
import static org.zeveon.stockpricepredictionbot.state.Variable.MESSAGE;
import static org.zeveon.stockpricepredictionbot.state.Variable.TICKER;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createMessage;
import static org.zeveon.stockpricepredictionbot.util.StateMessageUtil.createIntervalKeyboard;

/**
 * @author Zejaven
 */
public class ChooseIntervalState extends BotState implements CallbackQueryHandler, Final {

    private static final String BASIC_MESSAGE = """
            Do you want to predict the stock price for the next hour or day?
            """;

    public ChooseIntervalState(UpdateController updateController, StockPricePredictionBot bot) {
        super(updateController, bot);
    }

    @Override
    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        var command = callbackQuery.getData();
        CallbackCommand.fromText(command).ifPresent(c -> {
            var ticker = (String) getStateVariable(TICKER);
            var response = updateController.predict(PredictionAppRequest.builder()
                    .chatId(ChatContext.getInstance().getChatId())
                    .ticker(ticker)
                    .intervalChoice(c.name().toLowerCase())
                    .build());
            updateStateVariable(MESSAGE, response.getMessage());
            bot.nextState(null, null);
        });
    }

    @Override
    protected Map<Object, SendMessage> buildStateMessages() {
        var chatId = ChatContext.getInstance().getChatId();
        return Map.of(
                BASIC, createMessage(chatId, BASIC_MESSAGE, createIntervalKeyboard())
        );
    }

    @Override
    public SendMessage complete() {
        var chatId = ChatContext.getInstance().getChatId();
        var message = (String) getStateVariable(MESSAGE);
        return createMessage(chatId, message);
    }

    @Override
    protected boolean isVariableUpdatable(Variable key) {
        return key == MESSAGE;
    }
}
