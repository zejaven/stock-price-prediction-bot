package org.zeveon.stockpricepredictionbot.service;

import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

/**
 * @author Zejaven
 */
public interface KeyboardService {
    Pair<InlineKeyboardMarkup, Integer> getStocksKeyboard(Integer page, String searchText);
}
