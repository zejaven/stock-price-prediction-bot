package org.zeveon.stockpricepredictionbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.zeveon.stockpricepredictionbot.service.KeyboardService;
import org.zeveon.stockpricepredictionbot.service.TinkoffInvestService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.zeveon.stockpricepredictionbot.util.TinkoffMessageUtil.buildStocksKeyboard;

/**
 * @author Zejaven
 */
@Service
@RequiredArgsConstructor
public class KeyboardServiceImpl implements KeyboardService {

    private final TinkoffInvestService tinkoffInvestService;

    @Override
    public Pair<InlineKeyboardMarkup, Integer> getStocksKeyboard(Integer page, String searchText) {
        var availableTickers = tinkoffInvestService.getAvailableTickers().entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains(searchText.toLowerCase())
                        || e.getValue().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        return Pair.of(buildStocksKeyboard(availableTickers, page, isNotEmpty(searchText)), availableTickers.size());
    }
}
