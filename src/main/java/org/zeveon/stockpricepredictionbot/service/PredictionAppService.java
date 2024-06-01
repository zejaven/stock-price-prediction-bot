package org.zeveon.stockpricepredictionbot.service;

import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppRequest;
import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppResponse;

/**
 * @author Zejaven
 */
public interface PredictionAppService {

    PredictionAppResponse predict(PredictionAppRequest request);
}
