package org.zeveon.stockpricepredictionbot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zeveon.stockpricepredictionbot.model.prediction_app.PredictionAppMessage;

import static org.zeveon.stockpricepredictionbot.util.CommonMessageUtil.createMessage;

/**
 * @author Zejaven
 */
@RequestMapping("/api/prediction-bot")
@RestController
@RequiredArgsConstructor
public class PredictionProcessController {

    private final UpdateController updateController;

    @PostMapping("/message")
    public Boolean sendMessage(@RequestBody PredictionAppMessage message) {
        return updateController.sendResponse(createMessage(message.getChatId(), message.getText())) != null;
    }
}
