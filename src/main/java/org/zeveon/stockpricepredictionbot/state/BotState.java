package org.zeveon.stockpricepredictionbot.state;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.zeveon.stockpricepredictionbot.component.StockPricePredictionBot;
import org.zeveon.stockpricepredictionbot.context.ChatContext;
import org.zeveon.stockpricepredictionbot.controller.UpdateController;
import org.zeveon.stockpricepredictionbot.state.handler.UpdateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zejaven
 */
@Log4j2
public abstract class BotState implements UpdateHandler {

    protected final UpdateController updateController;

    protected final StockPricePredictionBot bot;

    @Getter
    protected List<Message> sentMessages = new ArrayList<>();

    @Getter
    private final Map<Object, SendMessage> stateMessages;

    private final Map<Variable, Object> stateVariables;

    public BotState(UpdateController updateController, StockPricePredictionBot bot) {
        this.updateController = updateController;
        this.bot = bot;
        this.stateVariables = cloneAll(bot.getSessionVariables());
        this.stateMessages = buildStateMessages();
    }

    private Map<Variable, Object> cloneAll(Map<Variable, Object> sessionVariables) {
        return sessionVariables.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public final void handleUpdate(Update update, StockPricePredictionBot bot) {
        log.info("State: %s".formatted(this.getClass().getSimpleName()));
        log.info(sentMessages.stream().map(m -> "[%s, %s]".formatted(m.getMessageId(), m.getText())).toList());
        log.info(bot.getSessionVariables().entrySet().stream().map(v -> "%s -> %s".formatted(v.getKey(), v.getValue().toString())).toList());
        this.handle(update, bot);
        log.info(stateVariables.entrySet().stream().map(v -> "%s -> %s".formatted(v.getKey(), v.getValue().toString())).toList());
        log.info(sentMessages.stream().map(m -> "[%s, %s]".formatted(m.getMessageId(), m.getText())).toList());
    }

    @Override
    public void addSentMessage(Message message) {
        sentMessages.add(message);
    }

    @Override
    public void deletePreviousMessages() {
        sentMessages.forEach(message ->
                updateController.deleteMessage(ChatContext.getInstance().getChatId(), message.getMessageId()));
    }

    public void restoreVariables(BotState currentState) {
        bot.getSessionVariables().clear();
        bot.getSessionVariables().putAll(cloneAll(currentState.stateVariables));
    }

    protected Object getStateVariable(Variable key) {
        return stateVariables.get(key);
    }

    protected void updateStateVariable(Variable key, Object value) {
        if (isVariableUpdatable(key)) {
            stateVariables.put(key, value);
        } else {
            throw new IllegalStateException("Attempted to update an unmodifiable state variable");
        }
    }

    protected boolean isVariableUpdatable(Variable key) {
        return false;
    }

    protected abstract Map<Object, SendMessage> buildStateMessages();
}
