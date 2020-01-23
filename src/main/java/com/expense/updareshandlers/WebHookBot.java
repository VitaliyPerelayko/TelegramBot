package com.expense.updareshandlers;

import com.expense.BotConfig;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class WebHookBot extends TelegramWebhookBot {

    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setText(update.getMessage().getText());
            return sendMessage;
        }
        return null;
    }

    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    public String getBotPath() {
        return BotConfig.BOT_PATH;
    }
}
