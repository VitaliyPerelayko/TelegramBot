package com.expense.updareshandlers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateHandler {

    private Map<Long, ChatUpdateHandler> chatsMap = new HashMap<>();

    public BotApiMethod handleUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            return handle(message.getChatId(), message.getText(), message.getMessageId());
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return handle(callbackQuery.getMessage().getChatId(), callbackQuery.getData(),
                    callbackQuery.getMessage().getMessageId());
        } else {
            System.out.println("========= Update doesn't have any message or callback query ===========");
            return null;
        }
    }

    private BotApiMethod handle(Long chatId, String text, Integer messageId) {
        ExecutorService es = Executors.newFixedThreadPool(10);
        ChatUpdateHandler chatUpdateHandler = chatsMap.get(chatId);
        if (chatUpdateHandler == null) {
            chatUpdateHandler = new ChatUpdateHandler(chatId);
            chatsMap.put(chatId, chatUpdateHandler);
        }
        chatUpdateHandler.setTextAndMessageId(text, messageId);
        try {
            return es.submit(chatUpdateHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}