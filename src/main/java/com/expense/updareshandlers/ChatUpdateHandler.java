package com.expense.updareshandlers;

import com.expense.BotConfig;
import com.expense.inlinekeyboard.InlineKeyboardFactory;
import com.expense.salesforceconection.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

import static com.expense.inlinekeyboard.InlineKeyboardFactory.*;

public class ChatUpdateHandler {

    private static final String SUCCESS = "\"SUCCESS\"";

    private LoginToExpenseApp loginToExpenseApp;
    private CalloutsToSalesforce calloutsToSalesforce;
    private LongPollingBot bot;
    private Long chatId;
    private boolean isActive = false;
    private LocalDate monthToRenderCalendar;

    public ChatUpdateHandler(LongPollingBot bot, Long chatId) {
        this.bot = bot;
        this.chatId = chatId;
        this.monthToRenderCalendar = LocalDate.now();
    }

    public void commandHandler(String text, Integer messageId) {
        switch (text) {
            case Commands.START:
                startHandler();
                break;
            case Commands.STOP:
                stopHandler();
                break;
            case Commands.CANCEL:
                cancelHandler();
                break;
            case Commands.BALANCE:
                balanceHandler();
                break;
            case Commands.NEW_EXPENSE_CARD:
                newCardHandler();
                break;
            case Commands.SET_AMOUNT:
                setAmountHandler();
                break;
            case Commands.SET_DATE:
                setDateHandler();
                break;
            case Commands.SET_DESCRIPTION:
                setDescriptionHandler();
                break;
            case Commands.CREATE_CARD:
                createCardHandler();
                break;
            case Commands.CALENDAR:
                calendarHandler();
                break;
            case Commands.TODAY:
                todayHandler();
                break;
            case Commands.PREV_MONTH:
                prevMonthHandler(messageId);
                break;
            case Commands.NEXT_MONTH:
                nextMonthHandler(messageId);
                break;
            case Commands.DO_NOTHING:
                break;
//            case Commands.HELP:
//                helpHandler();
//                break;
            default:
                textHandler(text);
                break;
        }
    }

    private void startHandler() {
        RequestParameters requestParameters = Login.getToken();
        loginToExpenseApp = new LoginToExpenseApp(requestParameters);
        calloutsToSalesforce = new CalloutsToSalesforce(requestParameters);
        isActive = true;
        sendMsg("ВВедите логин:");
    }

    private void stopHandler() {
        isActive = false;
    }

    private void cancelHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()){
                    // user is in process of creating new expense card
                    if (calloutsToSalesforce.getNewCardStage() == null){
                        calloutsToSalesforce.closeNewCardProcess();
                        sendMsg("Что хотіте сделать?", getBaseCommands());
                    } else {
                        calloutsToSalesforce.setNewCardStage(null);
                        checkIfReadyToSave();
                    }
                }
            }
        }
    }

    private void balanceHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                final String result = calloutsToSalesforce.getBalance(loginToExpenseApp.getKeeperId());
                switch (result) {
                    case "\"ERROR\"":
                        sendMsg("Произоша ошибка на сервере, попытайтесь ещё раз", getBaseCommands());
                    case "\"NO_DATA\"":
                        sendMsg("В текщем месяце ещё не было пополнения баланся или расходов. Создайте сначала карточку",
                                getCommandsAfterBalance());
                    default:
                        sendMsg("Баланс за теущий месяц: " + result, getCommandsAfterBalance());
                }
            }
        }
    }

    private void newCardHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                calloutsToSalesforce.setNewCardProcessGoIn(true);
                sendMsg("Заполните данные", getCommandsToFillNewCard());
            }
        }
    }

    private void setAmountHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()) {
                    // user is in process of creating new expense card
                    calloutsToSalesforce.setNewCardStage(NewCardStage.AMOUNT);
                    String amount = calloutsToSalesforce.getAmount();
                    if (amount != null) {
                        sendMsg("Желаете изменить стоимость? Предыдущее значение: " + amount +
                                ". Введите новую цену или нажмите Отмена", getCancel());
                    } else {
                        sendMsg("Введите стомость", getCancel());
                    }
                }
            }
        }
    }

    private void setDateHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()) {
                    // user is in process of creating new expense card
                    calloutsToSalesforce.setNewCardStage(NewCardStage.DATE);
                    LocalDate cardDate = calloutsToSalesforce.getCardDate();
                    if (cardDate != null) {
                        sendMsg("Желаете изменить дату? Предыдущее значение: " + cardDate.toString()
                                        + ". Выберите новую дату или нажмите Отмена",
                                getWaysToSetDate());
                    } else {
                        sendMsg("На какой день желаете создать карточку?", getWaysToSetDate());
                    }
                }
            }
        }
    }

    private void setDescriptionHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()) {
                    // user is in process of creating new expense card
                    calloutsToSalesforce.setNewCardStage(NewCardStage.DESCRIPTION);
                    String description = calloutsToSalesforce.getDescription();
                    if (description != null) {
                        sendMsg("Желаете изменить описание? Предыдущее описание: " + description
                                        + ". Введите новое или нажмите Отмена",
                                getCancel());
                    } else {
                        sendMsg("Введите описание товара", getCancel());
                    }
                }
            }
        }
    }

    private void createCardHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()) {
                    // user is in process of creating new expense card
                    String description = calloutsToSalesforce.getDescription();
                    String amount = calloutsToSalesforce.getAmount();
                    LocalDate cardDate = calloutsToSalesforce.getCardDate();
                    if (checkData(amount, description, cardDate)) {
                        String result = calloutsToSalesforce.createNew(loginToExpenseApp.getKeeperId());
                        if (SUCCESS.equals(result)) {
                            calloutsToSalesforce.closeNewCardProcess();
                            sendMsg("Карточка успешно создана", getBaseCommands());
                        } else {
                            sendMsg("Произошла ошибка на сервере. Можете попытаться ещё раз",
                                    getCommandsToCreateNewCard());
                        }
                    }
                }
            }
        }
    }

    private void calendarHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()) {
                    // user is in process of creating new expense card
                    if (checkIfDateSetStage()) {
                        // user is setting date now
                        sendMsg("Выберите дату: ", calendar(monthToRenderCalendar));
                    }
                }
            }
        }
    }

    private void todayHandler() {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()) {
                    // user is in process of creating new expense card
                    if (checkIfDateSetStage()) {
                        // user is setting date now
                        setCardDateHandler(LocalDate.now());
                    }
                }
            }
        }
    }

    private void prevMonthHandler(Integer messageId) {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()) {
                    // user is in process of creating new expense card
                    if (checkIfDateSetStage()) {
                        // user is setting date now
                        monthToRenderCalendar = monthToRenderCalendar.minusMonths(1);
                        updateMsg(messageId, calendar(monthToRenderCalendar));
                    }
                }
            }
        }
    }

    private void nextMonthHandler(Integer messageId) {
        if (checkActive()) {
            // bot is active (command /start was executed)
            if (checkAuth()) {
                // user is logged
                if (checkNewCardProcess()) {
                    // user is in process of creating new expense card
                    if (checkIfDateSetStage()) {
                        // user is setting date now
                        monthToRenderCalendar = monthToRenderCalendar.plusMonths(1);
                        updateMsg(messageId, calendar(monthToRenderCalendar));
                    }
                }
            }
        }
    }

    private void textHandler(String text) {
        System.out.println(text);
        if (checkActive()) {
            if (loginToExpenseApp.isInProcess()) {
                handleLogin(text);
            } else {
                if (calloutsToSalesforce.isNewCardProcessGoIn()) {
                    switch (calloutsToSalesforce.getNewCardStage()) {
                        case DATE:
                            try {
                                LocalDate cardDate = LocalDate.parse(text);
                                setCardDateHandler(cardDate);
                            } catch (DateTimeParseException ex) {
                                ex.printStackTrace();
                                sendMsg("Дата не может быть сохранена. Возможно вы ошиблись при вводе.\nДата должна быть в формате YYYY-MM-DD.\nВведите дату в нужном формате или воспользуйтесь календарём",
                                        calendar(monthToRenderCalendar));
                            }
                            break;
                        case AMOUNT:
                            setAmountHandler(text);
                            break;
                        case DESCRIPTION:
                            setDescriptionHandler(text);
                            break;
                    }
                } else {
                    sendMsg("Я не болтаю попусту, я выполняю команды!!!", InlineKeyboardFactory.getBaseCommands());
                }
            }
        }
    }

    private void handleLogin(String text) {
        final LoginStage stageOfLogin = loginToExpenseApp.getStage();
        if (stageOfLogin == LoginStage.USERNAME) {
            if (validateUsername(text)) {
                String result = loginToExpenseApp.setUsername(text);
                if (SUCCESS.equals(result)) {
                    loginToExpenseApp.setValidUsername(text);
                    loginToExpenseApp.setStage(LoginStage.PASSWORD);
                    sendMsg("Введите пароль:");
                } else {
                    sendMsg("В системе нет такого юзера. Попробуйте другой логин или введите команду /stop",
                            getStop());
                }
            } else {
                sendMsg("Форат логина неверный. Логин должен быть в формате email.\n Попробуйте другой логин или введите команду /stop",
                        getStop());
            }
        } else if (stageOfLogin == LoginStage.PASSWORD) {
            String result = loginToExpenseApp.setPassword(text);
            if (result.startsWith("\"TOKEN")) {
                loginToExpenseApp.setInProcess(false);
                parseAccessToken(result);
                sendMsg("Авторизация прошла успешно", getBaseCommands());
            } else {
                sendMsg("Пароль не подходит. Попробуйте ещё раз или введите команду /stop", getStop());
            }
        }
    }

    // TODO: check if 2 years ago <= data <= next year
    private void setCardDateHandler(LocalDate cardDate) {
        // set card date
        calloutsToSalesforce.setCardDate(cardDate);
        // set stage to null
        calloutsToSalesforce.setNewCardStage(null);
        // set default month to render calendar
        monthToRenderCalendar = LocalDate.now();
        checkIfReadyToSave();
    }

    private void setAmountHandler(String amountText) {
        try {
            Double.valueOf(amountText);
            // set card amount
            calloutsToSalesforce.setAmount(amountText);
            // set stage to null
            calloutsToSalesforce.setNewCardStage(null);
            checkIfReadyToSave();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            sendMsg("Введенный текст не является числом. Вы ввели: " + amountText +
                    ". Введите корректное число или нажмите отмена", getCancel());
        }
    }

    private void setDescriptionHandler(String descriptionText) {
        // set description
        calloutsToSalesforce.setDescription(descriptionText);
        // set stage to null
        calloutsToSalesforce.setNewCardStage(null);
        checkIfReadyToSave();
    }

    private void checkIfReadyToSave() {
        String amount = calloutsToSalesforce.getAmount() == null ? "ещё не задана" : calloutsToSalesforce.getAmount();
        String cardDate = calloutsToSalesforce.getCardDate() == null ? "ещё не задана" :
                calloutsToSalesforce.getCardDate().format(DateTimeFormatter.
                        ofLocalizedDate(FormatStyle.LONG).
                        withLocale(BotConfig.defaultLocale));
        String description = calloutsToSalesforce.getDescription() == null ? "ещё не задано" :
                calloutsToSalesforce.getDescription();
        String message = "Дата для картлчки: " + cardDate +
                "\nСтоимомсть товара: " + amount +
                "\nОписание товара: " + description;
        if (calloutsToSalesforce.isCardReadyToSave()) {
            // card ready to save (all required fields are full)
            sendMsg(message + "\n Сохранить карточку?", getCommandsToCreateNewCard());
        } else {
            // there are some null fields
            sendMsg(message, getCommandsToFillNewCard());
        }
    }

    /**
     * Метод для настройки сообщения и его отправки.
     *
     * @param textMsg Строка, которую необходимот отправить в качестве сообщения.
     */
    private void sendMsg(String textMsg) {
        SendMessage sendMessage = configTextMessage(textMsg);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для настройки сообщения и его отправки.
     *
     * @param s              Строка, которую необходимот отправить в качестве сообщения.
     * @param keyboardMarkup inline keyboard of commands
     */
    private void sendMsg(String s, ReplyKeyboard keyboardMarkup) {
        SendMessage sendMessage = configTextMessage(s);
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void updateMsg(Integer messageId, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageReplyMarkup editedMsg = new EditMessageReplyMarkup().
                setChatId(chatId).
                setMessageId(messageId).setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(editedMsg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage configTextMessage(String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        return sendMessage;
    }

    private void parseAccessToken(String token) {
        String[] attributes = token.split("&");
        loginToExpenseApp.setAdmin(Boolean.getBoolean(attributes[2]));
        loginToExpenseApp.setKeeperId(attributes[1]);
        loginToExpenseApp.setOffice(attributes[3]);
    }

    private boolean validateUsername(String username) {
        String regex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        return username.matches(regex);
    }

    private boolean checkActive() {
        if (isActive) {
            return true;
        } else {
            // bot is inactive
            sendMsg("Вы не авторизированы. Введите команду /start", getStart());
            return false;
        }
    }

    private boolean checkAuth() {
        if (!loginToExpenseApp.isInProcess()) {
            // user is logged
            return true;
        } else {
            // user is in process of login
            sendMsg("Вы ещё не авторизовались!!!\nВведите необходимые данные или команду /stop",
                    getStop());
            return false;
        }
    }

    private boolean checkNewCardProcess() {
        if (calloutsToSalesforce.isNewCardProcessGoIn()) {
            return true;
        } else {
            sendMsg("Эта команда в данный млмент не доступна", getBaseCommands());
            return false;
        }
    }

    private boolean checkIfDateSetStage() {
        if (calloutsToSalesforce.getNewCardStage() == NewCardStage.DATE) {
            return true;
        } else {
            sendMsg("Эта команда в данный млмент не доступна", getCommandsToFillNewCard());
            return false;
        }
    }

    private boolean checkData(String amount, String description, LocalDate cardDate) {
        if (amount != null) {
            if (description != null) {
                if (cardDate != null) {
                    return true;
                } else {
                    sendMsg("Дата не заполнена. Все поля должны быть заполнены!!!",
                            getCommandsToFillNewCard());
                    return false;
                }
            } else {
                sendMsg("Описание не заполнено. Все поля должны быть заполнены!!!",
                        getCommandsToFillNewCard());
                return false;
            }
        } else {
            sendMsg("Цена не указана. Все поля должны быть заполнены!!!",
                    getCommandsToFillNewCard());
            return false;
        }
    }
}
