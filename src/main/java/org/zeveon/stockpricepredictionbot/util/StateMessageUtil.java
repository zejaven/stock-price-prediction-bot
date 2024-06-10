package org.zeveon.stockpricepredictionbot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

import static org.zeveon.stockpricepredictionbot.model.CallbackCommand.DAY;
import static org.zeveon.stockpricepredictionbot.model.CallbackCommand.HOUR;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createButton;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createVerticalAndBackButtonKeyboardMarkup;

/**
 * @author Zejaven
 */
public class StateMessageUtil {

    public static InlineKeyboardMarkup createIntervalKeyboard() {
        return createVerticalAndBackButtonKeyboardMarkup(
                List.of(
                        createButton("Hour", HOUR.getText()),
                        createButton("Day", DAY.getText())
                )
        );
    }
}
