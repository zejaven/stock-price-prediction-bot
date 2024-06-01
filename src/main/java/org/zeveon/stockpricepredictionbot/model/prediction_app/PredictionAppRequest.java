package org.zeveon.stockpricepredictionbot.model.prediction_app;

import lombok.*;

/**
 * @author Zejaven
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionAppRequest {
    private String ticker;
}
