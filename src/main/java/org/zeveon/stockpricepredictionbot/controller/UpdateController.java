package org.zeveon.stockpricepredictionbot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;
import org.zeveon.stockpricepredictionbot.model.Command;

import java.util.stream.IntStream;

import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.*;
import static org.zeveon.stockpricepredictionbot.util.StringUtil.*;

/**
 * @author Zejaven
 */
@Controller
@RequiredArgsConstructor
public class UpdateController {

    private static final String NEW_LINE_TEMPLATE = "%s\n%s";

    private StockPricePredictionBot bot;

    public void registerBot(StockPricePredictionBot stockPricePredictionBot) {
        this.bot = stockPricePredictionBot;
        try {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(stockPricePredictionBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void processUpdate(Update update) {
        var message = update.getMessage();
        var chatId = message.getChatId();
        var text = message.getText();
        var command = text.split(WHITESPACE_CHARACTER)[0];
        if (command.startsWith(SLASH)) {
            var commandAndInvocation = command.split(AT_SIGN);
            if (commandAndInvocation.length == 1 || commandAndInvocation[1].equals(bot.getBotUsername())) {
                switch (Command.fromText(commandAndInvocation[0])) {
                    case HELP -> sendResponse(createMessage(chatId, buildHelpResponse()));
                    case TEST -> processTestResponse(chatId);
                    default -> sendResponse(createMessage(chatId, buildEmptyResponse()));
                }
            }
        }
    }

    private void processTestResponse(Long chatId) {
        sendResponse(createMessage(chatId, "Some test message",
                createHorizontalKeyboardMarkup(IntStream.range(1, 5).boxed()
                        .map(n -> createButton("Button %d".formatted(n), n.toString()))
                        .toList())));
    }

    public Message sendResponse(SendMessage message) {
        try {
            return bot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
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
