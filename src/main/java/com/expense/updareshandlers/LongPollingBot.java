package com.expense.updareshandlers;

import com.expense.BotConfig;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class LongPollingBot extends TelegramLongPollingBot {

    private UpdateHandler updateHandler = new UpdateHandler(this);

    public void onUpdateReceived(Update update) {
        updateHandler.handleUpdate(update);
    }

    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }
}
