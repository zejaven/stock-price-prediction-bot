package org.zeveon.stockpricepredictionbot.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * @author Zejaven
 */
public class CommonMessageUtil {

    public static SendMessage createMessage(Long chatId, String text) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    public static SendMessage createMessage(Long chatId, String text, InlineKeyboardMarkup backButtonKeyboardMarkup) {
        var message = createMessage(chatId, text);
        message.setReplyMarkup(backButtonKeyboardMarkup);
        return message;
    }

    public static InlineKeyboardMarkup createHorizontalKeyboardMarkup(List<InlineKeyboardButton> buttons) {
        return createButtonsKeyboardMarkup(singletonList(buttons));
    }

    public static InlineKeyboardMarkup createButtonsKeyboardMarkup(List<List<InlineKeyboardButton>> buttonsTable) {
        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buttonsTable);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardButton createButton(String text, String callbackData) {
        var backButton = new InlineKeyboardButton();
        backButton.setText(text);
        backButton.setCallbackData(callbackData);
        return backButton;
    }
}
