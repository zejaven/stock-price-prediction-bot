package org.zeveon.stockpricepredictionbot.component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.zeveon.stockpricepredictionbot.context.BotSession;
import org.zeveon.stockpricepredictionbot.context.ChatContext;
import org.zeveon.stockpricepredictionbot.controller.UpdateController;
import org.zeveon.stockpricepredictionbot.state.BotState;
import org.zeveon.stockpricepredictionbot.state.Final;
import org.zeveon.stockpricepredictionbot.state.Variable;

import java.util.HashMap;
import java.util.Map;

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

    private Map<Long, BotSession> botSessions;

    private final UpdateController updateController;

    @PostConstruct
    public void init() {
        updateController.registerBot(this);
        botSessions = new HashMap<>();
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

    public void nextState(BotState newState, Object conditionKey) {
        validateStatePresence(newState);
        if (hasState()) {
            var currentState = getBotState();
            currentState.deletePreviousMessages();
            if (newState == null) {
                handleFinalState(currentState);
            } else if (newState.equals(currentState)) {
                handleSameState(newState, conditionKey);
            } else {
                handleTransition(newState, conditionKey);
            }
        } else {
            handleTransition(newState, conditionKey);
        }
    }

    private void handleTransition(BotState newState, Object conditionKey) {
        var newStateMessage = newState.getStateMessages().get(conditionKey);
        var sentMessage = updateController.sendResponse(newStateMessage);
        newState.addSentMessage(sentMessage);
        setBotState(newState);
    }

    private void handleSameState(BotState newState, Object conditionKey) {
        newState.getSentMessages().clear();
        var sentMessage = updateController.sendResponse(newState.getStateMessages().get(conditionKey));
        newState.addSentMessage(sentMessage);
    }

    private void handleFinalState(BotState currentState) {
        validateFinalState(currentState);
        var finalMessage = ((Final) currentState).complete();
        updateController.sendResponse(finalMessage);
        clearSession();
    }

    private void validateStatePresence(BotState newState) {
        if (!hasState() && newState == null) {
            throw new IllegalStateException("Cannot finish non-existing state chain");
        }
    }

    private void validateStatePresence() {
        if (!hasState()) {
            throw new IllegalStateException("Cannot get previous state for non-existing state chain");
        }
    }

    private void validateFinalState(BotState currentState) {
        if (!(currentState instanceof Final)) {
            throw new IllegalStateException("Either the current state must be final or the next state must not be null");
        }
    }

    public void previousState() {
        var chatContext = ChatContext.getInstance();
        validateStatePresence();
        var botSession = botSessions.get(chatContext.getChatId());
        var previousState = botSession.popState();
        previousState.deletePreviousMessages();
        if (hasState()) {
            previousState.restoreVariables(botSession.currentState());
            resendStateMessages();
        } else {
            clearSession();
        }
    }

    private void resendStateMessages() {
        var currentState = getBotState();
        var newMessages = currentState.getSentMessages().stream()
                .map(updateController::sendResponse)
                .toList();
        currentState.getSentMessages().clear();
        newMessages.forEach(currentState::addSentMessage);
    }

    public void newSession() {
        botSessions.putIfAbsent(ChatContext.getInstance().getChatId(), new BotSession());
    }

    public Map<Variable, Object> getSessionVariables() {
        return botSessions.get(ChatContext.getInstance().getChatId()).getSessionVariables();
    }

    public void putSessionVariable(Variable key, Object value) {
        botSessions.get(ChatContext.getInstance().getChatId()).getSessionVariables().put(key, value);
    }

    public BotState getBotState() {
        return botSessions.get(ChatContext.getInstance().getChatId()).currentState();
    }

    public void setBotState(BotState newState) {
        botSessions.get(ChatContext.getInstance().getChatId()).pushState(newState);
    }

    public void clearSession() {
        botSessions.get(ChatContext.getInstance().getChatId()).clearSession();
    }

    public boolean hasState() {
        var chatId = ChatContext.getInstance().getChatId();
        return botSessions.containsKey(chatId) && !botSessions.get(chatId).isEmptyStates();
    }
}
