package org.zeveon.stockpricepredictionbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author zejaven
 */
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class StockPricePredictionBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockPricePredictionBotApplication.class, args);
	}

}
