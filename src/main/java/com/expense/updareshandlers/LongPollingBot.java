package com.expense.updareshandlers;

import com.expense.BotConfig;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LongPollingBot extends TelegramLongPollingBot {

    private UpdateHandler updateHandler = new UpdateHandler();

    public void onUpdateReceived(Update update) {
        try {
            execute(updateHandler.handleUpdate(update));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }
}
