package org.zeveon.stockpricepredictionbot.component;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.zeveon.stockpricepredictionbot.context.ChatContext;

/**
 * @author Stanislav Vafin
 */
@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class ChatContextAspect {

    private final StockPricePredictionBot bot;

    @Before("execution(* org.zeveon.stockpricepredictionbot.controller.UpdateController.processUpdate(..))")
    public void onUpdateReceived(JoinPoint joinPoint) {
        var update = (Update) joinPoint.getArgs()[0];
        if (update == null) {
            throw new RuntimeException("Received update is null");
        } else if (update.hasMessage()) {
            var message = update.getMessage();
            ChatContext.setInstance(message.getChatId());
        } else if (update.hasCallbackQuery()) {
            var callbackQuery = update.getCallbackQuery();
            ChatContext.setInstance(callbackQuery.getMessage().getChatId());
        } else if (update.hasMyChatMember()) {
            var myChatMember = update.getMyChatMember();
            ChatContext.setInstance(myChatMember.getChat().getId());
        }
        bot.newSession();
    }
}
