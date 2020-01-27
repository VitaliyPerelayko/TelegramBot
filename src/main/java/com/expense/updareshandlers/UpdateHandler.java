package com.expense.updareshandlers;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class UpdateHandler {

    private LongPollingBot bot;
    private Map<Long, ChatUpdateHandler> chatsMap = new HashMap<>();

    public UpdateHandler(LongPollingBot bot) {
        this.bot = bot;
    }

    public void handleUpdate(Update update) {
        if (update.hasMessage()){
            Message message = update.getMessage();
            handle(message.getChatId(), message.getText(), message.getMessageId());
        } else if (update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            handle(callbackQuery.getMessage().getChatId(), callbackQuery.getData(),
                    callbackQuery.getMessage().getMessageId());
        }
    }

    private void handle(Long chatId, String text, Integer messageId){
        ChatUpdateHandler chatUpdateHandler = chatsMap.get(chatId);
        if (chatUpdateHandler == null) {
            chatUpdateHandler = new ChatUpdateHandler(bot, chatId);
            chatsMap.put(chatId, chatUpdateHandler);
            chatUpdateHandler.commandHandler(text, messageId);
        } else {
            chatUpdateHandler.commandHandler(text, messageId);
        }
    }
}