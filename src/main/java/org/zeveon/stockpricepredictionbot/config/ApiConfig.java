package org.zeveon.stockpricepredictionbot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zeveon.stockpricepredictionbot.api.PredictionAppApi;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author Stanislav Vafin
 */
@Configuration
public class ApiConfig {

    @Value("${stock-price-prediction-bot.app.base-url}")
    private String byBitBaseUrl;

    @Bean
    public PredictionAppApi byBitJsonApi(ObjectMapper objectMapper) {
        return new Retrofit.Builder()
                .baseUrl(byBitBaseUrl)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .build().create(PredictionAppApi.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        return mapper;
    }
}
