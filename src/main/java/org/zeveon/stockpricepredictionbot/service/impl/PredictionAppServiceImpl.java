package org.zeveon.stockpricepredictionbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zeveon.stockpricepredictionbot.api.PredictionAppApi;
import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppRequest;
import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppResponse;
import org.zeveon.stockpricepredictionbot.service.PredictionAppService;

import static org.zeveon.stockpricepredictionbot.util.ApiUtil.executeApiCall;

/**
 * @author Zejaven
 */
@Service
@RequiredArgsConstructor
public class PredictionAppServiceImpl implements PredictionAppService {

    private final PredictionAppApi api;

    @Override
    public PredictionAppResponse predict(PredictionAppRequest request) {
        return executeApiCall(api.getResponse(request));
    }
}
