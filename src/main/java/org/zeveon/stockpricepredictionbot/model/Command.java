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
    TEST("/test");

    public final String text;

    public static final Map<Command, String> LIST = new LinkedHashMap<>() {{
        put(TEST, "Test response");
    }};

    public static Command fromText(String text) {
        return Arrays.stream(Command.values())
                .filter(c -> c.getText().equals(text))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No enum found with text: %s"
                        .formatted(text)));
    }
}
