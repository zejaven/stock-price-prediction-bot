package org.zeveon.stockpricepredictionbot.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;
import org.zeveon.stockpricepredictionbot.context.ChatContext;
import org.zeveon.stockpricepredictionbot.model.Command;
import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppRequest;
import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppResponse;
import org.zeveon.stockpricepredictionbot.service.FileService;
import org.zeveon.stockpricepredictionbot.service.KeyboardService;
import org.zeveon.stockpricepredictionbot.service.PredictionAppService;
import org.zeveon.stockpricepredictionbot.state.available_stock_prediction.StocksPageState;

import static org.zeveon.stockpricepredictionbot.state.ConvertKey.BASIC;
import static org.zeveon.stockpricepredictionbot.state.Variable.PAGE;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createMessage;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createVideoMessage;
import static org.zeveon.stockpricepredictionbot.util.StringUtil.*;

/**
 * @author Zejaven
 */
@Controller
@RequiredArgsConstructor
public class UpdateController {

    private static final String NEW_LINE_TEMPLATE = "%s\n%s";

    private static final String INTRO_1 = "intro.mp4";
    private static final String INTRO_2 = "intro2.mp4";

    private StockPricePredictionBot bot;

    private final KeyboardService keyboardService;

    private final PredictionAppService predictionAppService;

    private final FileService fileService;

    public void registerBot(StockPricePredictionBot stockPricePredictionBot) {
        this.bot = stockPricePredictionBot;
        try {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(stockPricePredictionBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void processUpdate(Update update) {
        if (bot.hasState()) {
            var state = bot.getBotState();
            state.handleUpdate(update, bot);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage();
            var chatId = message.getChatId();
            var text = message.getText();
            var command = text.split(WHITESPACE_CHARACTER)[0];
            if (command.startsWith(SLASH)) {
                var commandAndInvocation = command.split(AT_SIGN);
                if (commandAndInvocation.length == 1 || commandAndInvocation[1].equals(bot.getBotUsername())) {
                    switch (Command.fromText(commandAndInvocation[0])) {
                        case START -> sendResponse(createVideoMessage(chatId, buildVideoResponse(INTRO_1), buildHelpResponse()));
                        case HELP -> sendResponse(createVideoMessage(chatId, buildVideoResponse(INTRO_2), buildHelpResponse()));
                        case AVAILABLE_STOCKS -> processAvailableStocksResponse();
                        default -> sendResponse(createMessage(chatId, buildEmptyResponse()));
                    }
                }
            }
        } else if (update.hasMyChatMember()) {
            var chatId = ChatContext.getInstance().getChatId();
            sendResponse(createVideoMessage(chatId, buildVideoResponse(INTRO_1), buildHelpResponse()));
        }
    }

    public PredictionAppResponse predict(PredictionAppRequest request) {
        return predictionAppService.predict(request);
    }

    private InputFile buildVideoResponse(String filename) {
        var videoFile = fileService.getVideo(filename);
        return new InputFile(videoFile);
    }

    private void processAvailableStocksResponse() {
        bot.putSessionVariable(PAGE, 1);
        bot.nextState(
                new StocksPageState(this, bot),
                BASIC
        );
    }

    public Pair<InlineKeyboardMarkup, Integer> getStocksKeyboard(Integer page, String searchText) {
        return keyboardService.getStocksKeyboard(page, searchText != null ? searchText : "");
    }

    public Message sendResponse(Message message) {
        return sendResponse(convertMessageToSendMessage(message));
    }

    public Message sendResponse(SendMessage message) {
        try {
            return bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Message sendResponse(SendVideo video) {
        try {
            return bot.execute(video);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteMessage(Long chatId, int messageId) {
        var deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage convertMessageToSendMessage(Message message) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(message.getText());
        sendMessage.setReplyMarkup(message.getReplyMarkup());
        return sendMessage;
    }

    private String buildHelpResponse() {
        return Command.LIST.entrySet().stream()
                .map(e -> "%s - %s".formatted(e.getKey().getText(), e.getValue()))
                .reduce(NEW_LINE_TEMPLATE::formatted)
                .orElse("There is nothing I can help. Help disappeared somewhere \uD83D\uDE31");
    }

    private String buildEmptyResponse() {
        return "There is no such command. Use /help to check which commands are available.";
    }
}
