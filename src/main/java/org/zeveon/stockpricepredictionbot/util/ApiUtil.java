package org.zeveon.stockpricepredictionbot.util;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * @author zejaven
 */
@Slf4j
public class ApiUtil {

    public static <T> T executeApiCall(Call<T> call) {
        try {
            var response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            } else {
                throw new RuntimeException("API call failed or returned null");
            }
        } catch (SocketTimeoutException e) {
            log.warn("IOException during API call");
            return executeApiCall(call.clone());
        } catch (IOException e) {
            throw new RuntimeException("IOException during API call", e);
        }
    }
}
