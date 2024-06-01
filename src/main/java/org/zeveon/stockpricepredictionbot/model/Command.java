package org.zeveon.stockpricepredictionbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Zejaven
 */
@Getter
@AllArgsConstructor
public enum Command {

    HELP("/help"),
    AVAILABLE_STOCKS("/available_stocks");

    public final String text;

    public static final Map<Command, String> LIST = new LinkedHashMap<>() {{
        put(AVAILABLE_STOCKS, """
                Checks which stocks are available for prediction.
                Returns buttons with stock names.
                Starts prediction after pressing specific stock button.
                """);
    }};

    public static Command fromText(String text) {
        return Arrays.stream(Command.values())
                .filter(c -> c.getText().equals(text))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No enum found with text: %s"
                        .formatted(text)));
    }
}
