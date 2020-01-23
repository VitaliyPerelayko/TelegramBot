package com.expense.inlinekeyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InlineKeyboardFactory {

    public static InlineKeyboardMarkup keyboard(Map<String, String> commandsNames) {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        commandsNames.forEach((label, command ) -> {
            InlineKeyboardButton button = new InlineKeyboardButton(label);
            button.setCallbackData(command);
            buttonRow.add(button);
        });
        keyboard.add(buttonRow);
        markup.setKeyboard(keyboard);
        return markup;
    }
}
