package org.zeveon.stockpricepredictionbot.context;

import lombok.Getter;
import org.zeveon.stockpricepredictionbot.state.BotState;
import org.zeveon.stockpricepredictionbot.state.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author zejaven
 */
public class BotSession {
    private final Stack<BotState> states = new Stack<>();

    @Getter
    private final Map<Variable, Object> sessionVariables = new HashMap<>();

    public void pushState(BotState state) {
        states.push(state);
    }

    public BotState popState() {
        return states.pop();
    }

    public BotState currentState() {
        return states.peek();
    }

    public void clearSession() {
        states.clear();
        sessionVariables.clear();
    }

    public boolean isEmptyStates() {
        return states.isEmpty();
    }
}
