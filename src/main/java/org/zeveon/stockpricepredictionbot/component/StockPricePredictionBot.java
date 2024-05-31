package org.zeveon.stockpricepredictionbot.component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.zeveon.stockpricepredictionbot.controller.UpdateController;

/**
 * @author Zejaven
 */
@Component
@RequiredArgsConstructor
public class StockPricePredictionBot extends TelegramLongPollingBot {

    @Value("${stock-price-prediction-bot.token}")
    private String accessToken;

    @Value("${stock-price-prediction-bot.name}")
    private String botUsername;

    private final UpdateController updateController;

    @PostConstruct
    public void init() {
        updateController.registerBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateController.processUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return accessToken;
    }
}
