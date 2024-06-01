package org.zeveon.stockpricepredictionbot.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.zeveon.stockpricepredictionbot.model.Cache;
import org.zeveon.stockpricepredictionbot.service.TinkoffInvestService;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InvestApi;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * @author Zejaven
 */
@Service
@RequiredArgsConstructor
public class TinkoffInvestServiceImpl implements TinkoffInvestService {

    @Value("${stock-price-prediction-bot.tinkoff-api-token}")
    private String tinkoffApiToken;

    private InvestApi api;

    @PostConstruct
    public void init() {
        api = InvestApi.create(tinkoffApiToken);
    }

    @Override
    @Cacheable(value = Cache.TICKERS)
    public Map<String, String> getAvailableTickers() {
        var sharesResponse = api.getInstrumentsService().getTradableSharesSync();
        return sharesResponse.stream()
                .collect(toMap(Share::getName, Share::getTicker, (a, b) -> a, LinkedHashMap::new));
    }
}
