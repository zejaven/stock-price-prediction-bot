package org.zeveon.stockpricepredictionbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author Zejaven
 */
@Getter
@AllArgsConstructor
public enum CallbackCommand {

    LEFT("/left"),
    RIGHT("/right"),
    PAGE("/page"),
    SEARCH("/search");

    private final String text;

    public static CallbackCommand fromText(String text) {
        return Arrays.stream(CallbackCommand.values())
                .filter(c -> c.getText().equals(text))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No enum found with text: %s"
                        .formatted(text)));
    }
}
