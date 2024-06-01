package org.zeveon.stockpricepredictionbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.zeveon.stockpricepredictionbot.service.KeyboardService;
import org.zeveon.stockpricepredictionbot.service.TinkoffInvestService;

import static org.zeveon.stockpricepredictionbot.util.TinkoffMessageUtil.buildStocksKeyboard;

/**
 * @author Zejaven
 */
@Service
@RequiredArgsConstructor
public class KeyboardServiceImpl implements KeyboardService {

    private final TinkoffInvestService tinkoffInvestService;

    @Override
    public Pair<InlineKeyboardMarkup, Integer> getStocksKeyboard(Integer page) {
        var availableTickers = tinkoffInvestService.getAvailableTickers();
        return Pair.of(buildStocksKeyboard(availableTickers, page), availableTickers.size());
    }
}
