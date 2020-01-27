package com.expense.inlinekeyboard;

import com.expense.BotConfig;
import com.expense.updareshandlers.Commands;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class InlineKeyboardFactory {

    private static InlineKeyboardMarkup start;
    private static InlineKeyboardMarkup stop;
    private static InlineKeyboardMarkup cancel;
    private static InlineKeyboardMarkup baseCommands;
    private static InlineKeyboardMarkup commandsAfterBalance;
    private static InlineKeyboardMarkup commandsToFillNewCard;
    private static InlineKeyboardMarkup commandsToCreateNewCard;
    private static InlineKeyboardMarkup waysToSetDate;

    private static List<InlineKeyboardButton> daysOfWeekLine;
    private static List<InlineKeyboardButton> navigationButtons;

    static {
        // start
        Map<String, String> startMap = new LinkedHashMap<>();
        startMap.put("Старт. Авторизоваться в приложении", Commands.START);
        start = keyboardLine(startMap);
        // stop
        Map<String, String> stopMap = new LinkedHashMap<>();
        stopMap.put("Выйти из учетной записи", Commands.STOP);
        stop = keyboardLine(stopMap);
        // cancel
        Map<String, String> cancelMap = new LinkedHashMap<>();
        cancelMap.put("Отмена", Commands.CANCEL);
        cancel = keyboardLine(cancelMap);
        // base commands
        Map<String, String> baseCommandsMap = new LinkedHashMap<>();
        baseCommandsMap.put("Текущий баланс", Commands.BALANCE);
        baseCommandsMap.put("Создать карточку", Commands.NEW_EXPENSE_CARD);
        baseCommandsMap.put("Выйти из учетгой записи", Commands.STOP);
        baseCommands = keyboardLine(baseCommandsMap);
        // commands after success balance getting
        Map<String, String> afterBalanceCommandsMap = new LinkedHashMap<>();
        afterBalanceCommandsMap.put("Создать карточку", Commands.NEW_EXPENSE_CARD);
        afterBalanceCommandsMap.put("Выйти из учетгой записи", Commands.STOP);
        commandsAfterBalance = keyboardColumn(afterBalanceCommandsMap);
        // commands to fill new expense card
        Map<String, String> newCardCommand = new LinkedHashMap<>();
        newCardCommand.put("Указать дату", Commands.SET_DATE);
        newCardCommand.put("Указать стоимоть покупки", Commands.SET_AMOUNT);
        newCardCommand.put("Указать описание покупки", Commands.SET_DESCRIPTION);
        newCardCommand.put("Отмена", Commands.CANCEL);
        commandsToFillNewCard = keyboardColumn(newCardCommand);
        // commands to create new expense card
        Map<String, String> newCardCreateCommand = new LinkedHashMap<>();
        newCardCreateCommand.put("Изменить дату", Commands.SET_DATE);
        newCardCreateCommand.put("Изменить стоимоть покупки", Commands.SET_AMOUNT);
        newCardCreateCommand.put("Изменить описание покупки", Commands.SET_DESCRIPTION);
        newCardCreateCommand.put("Создать карточку", Commands.CREATE_CARD);
        newCardCreateCommand.put("Отмена", Commands.CANCEL);
        commandsToCreateNewCard = keyboardColumn(newCardCreateCommand);
        // ways to set date
        Map<String, String> waysToSetDateMap = new LinkedHashMap<>();
        waysToSetDateMap.put("Сегодня", Commands.TODAY);
        waysToSetDateMap.put("Календарь", Commands.CALENDAR);
        waysToSetDateMap.put("Отмена", Commands.CANCEL);
        waysToSetDate = keyboardLine(waysToSetDateMap);

        // set days of week
        daysOfWeekLine = Arrays.asList(
                new InlineKeyboardButton().setText("Пн.").setCallbackData(Commands.DO_NOTHING),
                new InlineKeyboardButton().setText("Вт.").setCallbackData(Commands.DO_NOTHING),
                new InlineKeyboardButton().setText("Ср.").setCallbackData(Commands.DO_NOTHING),
                new InlineKeyboardButton().setText("Чт.").setCallbackData(Commands.DO_NOTHING),
                new InlineKeyboardButton().setText("Пт.").setCallbackData(Commands.DO_NOTHING),
                new InlineKeyboardButton().setText("Сб.").setCallbackData(Commands.DO_NOTHING),
                new InlineKeyboardButton().setText("Вс.").setCallbackData(Commands.DO_NOTHING)
        );
        // set navigation button
        navigationButtons = Arrays.asList(
                new InlineKeyboardButton().setText("Предидущий \n месяц").setCallbackData(Commands.PREV_MONTH),
                new InlineKeyboardButton().setText("Следующий \n месяц").setCallbackData(Commands.NEXT_MONTH)
        );
    }



    public static InlineKeyboardMarkup getStart() {
        return start;
    }

    public static InlineKeyboardMarkup getStop() {
        return stop;
    }

    public static InlineKeyboardMarkup getCancel() {
        return cancel;
    }

    public static InlineKeyboardMarkup getBaseCommands() {
        return baseCommands;
    }

    public static InlineKeyboardMarkup getCommandsAfterBalance() {
        return commandsAfterBalance;
    }

    public static InlineKeyboardMarkup getCommandsToFillNewCard() {
        return commandsToFillNewCard;
    }

    public static InlineKeyboardMarkup getCommandsToCreateNewCard() {
        return commandsToCreateNewCard;
    }

    public static InlineKeyboardMarkup getWaysToSetDate() {
        return waysToSetDate;
    }

    public static InlineKeyboardMarkup calendar(LocalDate date) {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        // set first line monthName and Year
        keyboard.add(Collections.singletonList(
                new InlineKeyboardButton().
                        setText(date.getMonth().getDisplayName(TextStyle.FULL, BotConfig.defaultLocale)
                                + " " + date.getYear()).
                        setCallbackData(Commands.DO_NOTHING)
                )
        );
        // set days of week
        keyboard.add(daysOfWeekLine);
        //Calendar
        final int year = date.getYear();
        final int month = date.getMonth().getValue();
        List<String[]> monthDates = Calendar.generateMonthTable(year, month);
        monthDates.forEach(week ->
            keyboard.add(
            Arrays.stream(week).map(s ->
                    new InlineKeyboardButton().
                            setText(s).
                            setCallbackData(String.format("%1$s-%2$s-%3$s", year, month, s))).
                    collect(Collectors.toList())
            )
        );
        // set navigation Buttons
        keyboard.add(navigationButtons);
        markup.setKeyboard(keyboard);
        return markup;
    }

    private static InlineKeyboardMarkup keyboardLine(Map<String, String> commandsNames) {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        commandsNames.forEach((label, command) ->
                buttonRow.add(new InlineKeyboardButton().setText(label).setCallbackData(command)));
        markup.setKeyboard(Collections.singletonList(buttonRow));
        return markup;
    }


    private static InlineKeyboardMarkup keyboardColumn(Map<String, String> commandsNames) {
        final InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        commandsNames.forEach((label, command) ->
                keyboard.add(Collections.singletonList(
                        new InlineKeyboardButton().setText(label).setCallbackData(command))));
        markup.setKeyboard(keyboard);
        return markup;
    }
}
