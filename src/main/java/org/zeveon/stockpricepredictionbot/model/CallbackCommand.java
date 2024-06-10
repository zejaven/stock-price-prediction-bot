package org.zeveon.stockpricepredictionbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Zejaven
 */
@Getter
@AllArgsConstructor
public enum CallbackCommand {

    LEFT("/left"),
    RIGHT("/right"),
    PAGE("/page"),
    SEARCH("/search"),
    HOUR("/hour"),
    DAY("/day");

    private final String text;

    public static Optional<CallbackCommand> fromText(String text) {
        return Arrays.stream(CallbackCommand.values())
                .filter(c -> c.getText().equals(text))
                .findAny();
    }
}
