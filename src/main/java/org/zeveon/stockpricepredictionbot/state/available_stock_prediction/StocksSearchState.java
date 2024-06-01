package org.zeveon.stockpricepredictionbot.state.available_stock_prediction;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;
import org.zeveon.stockpricepredictionbot.context.ChatContext;
import org.zeveon.stockpricepredictionbot.controller.UpdateController;
import org.zeveon.stockpricepredictionbot.state.BotState;
import org.zeveon.stockpricepredictionbot.state.handler.MessageHandler;

import java.util.Map;

import static org.zeveon.stockpricepredictionbot.state.ConvertKey.BASIC;
import static org.zeveon.stockpricepredictionbot.state.Variable.PAGE;
import static org.zeveon.stockpricepredictionbot.state.Variable.SEARCH_TEXT;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createMessage;

/**
 * @author Zejaven
 */
public class StocksSearchState extends BotState implements MessageHandler {

    private static final String BASIC_MESSAGE = """
            Write down search text to find specific stocks:
            """;

    public StocksSearchState(UpdateController updateController, StockPricePredictionBot bot) {
        super(updateController, bot);
    }

    @Override
    public void handleMessage(Message message) {
        bot.putSessionVariable(PAGE, 1);
        bot.putSessionVariable(SEARCH_TEXT, message.getText());
        bot.nextState(new StocksPageState(updateController, bot), BASIC);
    }

    @Override
    protected Map<Object, SendMessage> buildStateMessages() {
        var chatId = ChatContext.getInstance().getChatId();
        return Map.of(
                BASIC, createMessage(chatId, BASIC_MESSAGE)
        );
    }
}
