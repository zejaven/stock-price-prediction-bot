package org.zeveon.stockpricepredictionbot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static org.zeveon.stockpricepredictionbot.model.CallbackCommand.DAY;
import static org.zeveon.stockpricepredictionbot.model.CallbackCommand.HOUR;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createButton;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createVerticalButtonKeyboardMarkup;

/**
 * @author Zejaven
 */
public class StateMessageUtil {

    public static InlineKeyboardMarkup createIntervalKeyboard() {
        return createVerticalButtonKeyboardMarkup(
                List.of(
                        createButton("Hour", HOUR.getText()),
                        createButton("Day", DAY.getText())
                )
        );
    }
}
