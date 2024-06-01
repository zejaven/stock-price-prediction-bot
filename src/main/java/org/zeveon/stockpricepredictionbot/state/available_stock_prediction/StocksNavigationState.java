package org.zeveon.stockpricepredictionbot.state.available_stock_prediction;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;
import org.zeveon.stockpricepredictionbot.context.ChatContext;
import org.zeveon.stockpricepredictionbot.controller.UpdateController;
import org.zeveon.stockpricepredictionbot.state.BotState;
import org.zeveon.stockpricepredictionbot.state.handler.MessageHandler;

import java.util.Map;

import static org.apache.commons.lang3.math.NumberUtils.isDigits;
import static org.zeveon.stockpricepredictionbot.state.ConvertKey.BASIC;
import static org.zeveon.stockpricepredictionbot.state.ConvertKey.NOT_A_NUMBER;
import static org.zeveon.stockpricepredictionbot.state.Variable.PAGE;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createMessage;

/**
 * @author Zejaven
 */
public class StocksNavigationState extends BotState implements MessageHandler {

    private static final String BASIC_MESSAGE = """
            Write page number to move:
            """;
    private static final String NOT_A_NUMBER_MESSAGE = """
            Error. Not a number.
            Write page number to move:
            """;

    public StocksNavigationState(UpdateController updateController, StockPricePredictionBot bot) {
        super(updateController, bot);
    }

    @Override
    public void handleMessage(Message message) {
        var textAmount = message.getText();
        if (isDigits(textAmount)) {
            bot.putSessionVariable(PAGE, Integer.parseInt(textAmount));
            bot.nextState(new StocksPageState(updateController, bot), BASIC);
        } else {
            bot.nextState(this, NOT_A_NUMBER);
        }
    }

    @Override
    protected Map<Object, SendMessage> buildStateMessages() {
        var chatId = ChatContext.getInstance().getChatId();
        return Map.of(
                BASIC, createMessage(chatId, BASIC_MESSAGE),
                NOT_A_NUMBER, createMessage(chatId, NOT_A_NUMBER_MESSAGE)
        );
    }
}
