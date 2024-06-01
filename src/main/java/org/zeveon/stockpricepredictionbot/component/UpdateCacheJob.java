package org.zeveon.stockpricepredictionbot.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zeveon.stockpricepredictionbot.model.Cache;
import org.zeveon.stockpricepredictionbot.service.TinkoffInvestService;

/**
 * @author Zejaven
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateCacheJob {

    private final TinkoffInvestService tinkoffInvestService;

    @Scheduled(cron = "0 0 0 * * *")
    @CacheEvict(value = Cache.TICKERS, allEntries = true)
    public void updateTickersCache() {
        log.info("Evicted Cache.TICKERS cache");
        tinkoffInvestService.getAvailableTickers();
    }
}
