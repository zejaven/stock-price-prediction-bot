package org.zeveon.stockpricepredictionbot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.zeveon.stockpricepredictionbot.model.CallbackCommand.*;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createButton;
import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createCancelButton;

/**
 * @author Zejaven
 */
public class TinkoffMessageUtil {

    public static final Long LIMIT = 10L;

    public static InlineKeyboardMarkup buildStocksKeyboard(
            Map<String, String> stocks, Integer page, boolean searchTextExists
    ) {
        var buttons = stocks.entrySet().stream()
                .skip((page - 1) * LIMIT)
                .limit(LIMIT)
                .map(share -> createButton(share.getKey(), share.getValue()))
                .toList();
        var pageCount = (stocks.size() + LIMIT - 1) / LIMIT;
        var navigationRow = List.of(
                createButton("<-", LEFT.getText()),
                createButton("%d / %d".formatted(page, pageCount), PAGE.getText()),
                createButton("->", RIGHT.getText())
        );
        var searchRow = singletonList(createButton("Search", SEARCH.getText()));
        var extraRows = new ArrayList<List<InlineKeyboardButton>>();
        extraRows.add(navigationRow);
        extraRows.add(searchRow);
        if (searchTextExists) {
            var cancelRow = singletonList(createCancelButton());
            extraRows.add(cancelRow);
        }
        return buildGridKeyboard(buttons, extraRows, 1);
    }

    private static InlineKeyboardMarkup buildGridKeyboard(
            List<InlineKeyboardButton> buttons,
            List<List<InlineKeyboardButton>> extra,
            int columnsSize
    ) {
        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        var buttonsTable = new ArrayList<List<InlineKeyboardButton>>();
        for (int i = 0; i < buttons.size(); i += columnsSize) {
            buttonsTable.add(buttons.subList(i, Math.min(i + columnsSize, buttons.size())));
        }
        buttonsTable.addAll(extra);
        inlineKeyboardMarkup.setKeyboard(buttonsTable);
        return inlineKeyboardMarkup;
    }
}
