package com.expense.updareshandlers;

import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;

public class UpdateHandler {

    private LongPollingBot bot;
    private Map<Long, ChatUpdateHandler> chatsMap = new HashMap<>();

    public UpdateHandler(LongPollingBot bot) {
        this.bot = bot;
    }

    public void handleMessage(Message message) {
        Long chatId = message.getChatId();
        ChatUpdateHandler chatUpdateHandler = chatsMap.get(chatId);
        if (chatUpdateHandler == null) {
            chatUpdateHandler = new ChatUpdateHandler(bot);
            chatsMap.put(chatId, chatUpdateHandler);
            chatUpdateHandler.commandHandler(message);
        } else {
            chatUpdateHandler.commandHandler(message);
        }
    }


}