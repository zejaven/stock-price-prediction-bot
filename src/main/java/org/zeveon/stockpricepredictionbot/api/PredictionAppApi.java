package org.zeveon.stockpricepredictionbot.api;

import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppRequest;
import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author Zejaven
 */
public interface PredictionAppApi {

    @POST("/predict")
    Call<PredictionAppResponse> getResponse(@Body PredictionAppRequest request);
}
