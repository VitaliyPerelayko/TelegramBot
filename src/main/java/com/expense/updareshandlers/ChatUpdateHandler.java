package com.expense.updareshandlers;

import com.expense.BotConfig;
import com.expense.inlinekeyboard.InlineKeyboardFactory;
import com.expense.salesforceconection.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

import static com.expense.inlinekeyboard.InlineKeyboardFactory.*;

public class ChatUpdateHandler {

    private static final String SUCCESS = "\"SUCCESS\"";

    private LoginToExpenseApp loginToExpenseApp;
    private CalloutsToSalesforce calloutsToSalesforce;
    private Long chatId;
    private boolean isActive = false;
    private LocalDate monthToRenderCalendar;

    public ChatUpdateHandler(Long chatId) {
        this.chatId = chatId;
        this.monthToRenderCalendar = LocalDate.now();
    }

    public BotApiMethod commandHandler(String text, Integer messageId) {
        switch (text) {
            case Commands.START:
                return startHandler();
            case Commands.STOP:
                return stopHandler();
            case Commands.CANCEL:
                return cancelHandler();
            case Commands.BALANCE:
                return balanceHandler();
            case Commands.NEW_EXPENSE_CARD:
                return newCardHandler();
            case Commands.SET_AMOUNT:
                return setAmountHandler();
            case Commands.SET_DATE:
                return setDateHandler();
            case Commands.SET_DESCRIPTION:
                return setDescriptionHandler();
            case Commands.CREATE_CARD:
                return createCardHandler();
            case Commands.CALENDAR:
                return calendarHandler();
            case Commands.TODAY:
                return todayHandler();
            case Commands.PREV_MONTH:
                return prevMonthHandler(messageId);
            case Commands.NEXT_MONTH:
                return nextMonthHandler(messageId);
            case Commands.DO_NOTHING:
                System.out.println("==================== ERROR WHEN DO NOTHING ====================");
                return null;
//            case Commands.HELP:
//                helpHandler();
//                break;
            default:
                return textHandler(text);
        }
    }

    private BotApiMethod startHandler() {
        RequestParameters requestParameters = Login.getToken();
        loginToExpenseApp = new LoginToExpenseApp(requestParameters);
        calloutsToSalesforce = new CalloutsToSalesforce(requestParameters);
        isActive = true;
        return sendMsg("Введите логин:");
    }

    private BotApiMethod stopHandler() {
        isActive = false;
        return sendMsg("Вы вышли из учётной записи");
    }

    private BotApiMethod cancelHandler() {
        BotApiMethod method;
        if ((method = checkNewCardProcess()) == null) {
            // user is in process of creating new expense card
            if (calloutsToSalesforce.getNewCardStage() == null) {
                calloutsToSalesforce.closeNewCardProcess();
                return sendMsg("Что хотите сделать?", getBaseCommands());
            } else {
                calloutsToSalesforce.setNewCardStage(null);
                return checkIfReadyToSave();
            }
        } else {
            return method;
        }
    }

    private BotApiMethod balanceHandler() {
        BotApiMethod method;
        if ((method = checkAuth()) == null) {
            // user is logged
            final String result = calloutsToSalesforce.getBalance(loginToExpenseApp.getKeeperId());
            switch (result) {
                case CalloutsToSalesforce.ERROR:
                    sendMsg("Произоша ошибка на сервере, попытайтесь ещё раз", getBaseCommands());
                case "\"NO_DATA\"":
                    return sendMsg("В текщем месяце ещё не было пополнения баланся или расходов. Создайте сначала карточку",
                            getCommandsAfterBalance());
                default:
                    String balance = result.substring(1, result.length() - 1);
                    return sendMsg("Баланс за теущий месяц: " + balance + "$", getCommandsAfterBalance());
            }
        } else {
            return method;
        }
    }

    private BotApiMethod newCardHandler() {
        BotApiMethod method;
        if ((method = checkAuth()) == null) {
            // user is logged
            calloutsToSalesforce.setNewCardProcessGoIn(true);
            return sendMsg("Заполните данные", getCommandsToFillNewCard());
        } else {
            return method;
        }
    }

    private BotApiMethod setAmountHandler() {
        BotApiMethod method;
        if ((method = checkNewCardProcess()) == null) {
            // user is in process of creating new expense card
            calloutsToSalesforce.setNewCardStage(NewCardStage.AMOUNT);
            String amount = calloutsToSalesforce.getAmount();
            if (amount != null) {
                return sendMsg("Желаете изменить стоимость? Предыдущее значение: " + amount +
                        ". Введите новую цену или нажмите Отмена", getCancel());
            } else {
                return sendMsg("Введите стомость", getCancel());
            }
        } else {
            return method;
        }
    }

    private BotApiMethod setDateHandler() {
        BotApiMethod method;
        if ((method = checkNewCardProcess()) == null) {
            // user is in process of creating new expense card
            calloutsToSalesforce.setNewCardStage(NewCardStage.DATE);
            LocalDate cardDate = calloutsToSalesforce.getCardDate();
            if (cardDate != null) {
                return sendMsg("Желаете изменить дату? Предыдущее значение: " + cardDate.toString()
                                + ". Выберите новую дату или нажмите Отмена",
                        getWaysToSetDate());
            } else {
                return sendMsg("На какой день желаете создать карточку?", getWaysToSetDate());
            }
        } else {
            return method;
        }
    }

    private BotApiMethod setDescriptionHandler() {
        BotApiMethod method;
        if ((method = checkNewCardProcess()) == null) {
            // user is in process of creating new expense card
            calloutsToSalesforce.setNewCardStage(NewCardStage.DESCRIPTION);
            String description = calloutsToSalesforce.getDescription();
            if (description != null) {
                return sendMsg("Желаете изменить описание? Предыдущее описание: " + description
                                + ". Введите новое или нажмите Отмена",
                        getCancel());
            } else {
                return sendMsg("Введите описание товара", getCancel());
            }
        } else {
            return method;
        }
    }

    private BotApiMethod createCardHandler() {
        BotApiMethod method;
        if ((method = checkNewCardProcess()) == null) {
            // user is in process of creating new expense card
            String description = calloutsToSalesforce.getDescription();
            String amount = calloutsToSalesforce.getAmount();
            LocalDate cardDate = calloutsToSalesforce.getCardDate();
            if ((method = checkData(amount, description, cardDate)) == null) {
                String result = calloutsToSalesforce.createNew(loginToExpenseApp.getKeeperId());
                if (SUCCESS.equals(result)) {
                    calloutsToSalesforce.closeNewCardProcess();
                    return sendMsg("Карточка успешно создана", getBaseCommands());
                } else {
                    return sendMsg("Произошла ошибка на сервере. Можете попытаться ещё раз",
                            getCommandsToCreateNewCard());
                }
            } else {
                return method;
            }
        } else {
            return method;
        }
    }

    private BotApiMethod calendarHandler() {
        BotApiMethod method;
        if ((method = checkIfDateSetStage()) == null) {
            // user is setting date now
            return sendMsg("Выберите дату: ", calendar(monthToRenderCalendar));
        } else {
            return method;
        }
    }

    private BotApiMethod todayHandler() {
        BotApiMethod method;
        if ((method = checkIfDateSetStage()) == null) {
            // user is setting date now
            setCardDateHandler(LocalDate.now());
            return checkIfReadyToSave();
        } else {
            return method;
        }
    }

    private BotApiMethod prevMonthHandler(Integer messageId) {
        BotApiMethod method;
        if ((method = checkIfDateSetStage()) == null) {
            // user is setting date now
            monthToRenderCalendar = monthToRenderCalendar.minusMonths(1);
            return updateMsg(messageId, calendar(monthToRenderCalendar));
        } else {
            return method;
        }
    }

    private BotApiMethod nextMonthHandler(Integer messageId) {
        BotApiMethod method;
        if ((method = checkIfDateSetStage()) == null) {
            // user is setting date now
            monthToRenderCalendar = monthToRenderCalendar.plusMonths(1);
            return updateMsg(messageId, calendar(monthToRenderCalendar));
        } else {
            return method;
        }
    }

    private BotApiMethod textHandler(String text) {
        System.out.println(text);
        BotApiMethod method;
        if ((method = checkActive()) == null) {
            if (loginToExpenseApp.isInProcess()) {
                return handleLogin(text);
            } else {
                if (calloutsToSalesforce.isNewCardProcessGoIn()) {
                    switch (calloutsToSalesforce.getNewCardStage()) {
                        case DATE:
                            try {
                                LocalDate cardDate = LocalDate.parse(text);
                                return setCardDateHandler(cardDate);
                            } catch (DateTimeParseException ex) {
                                ex.printStackTrace();
                                return sendMsg("Дата не может быть сохранена. Возможно вы ошиблись при вводе.\nДата должна быть в формате YYYY-MM-DD.\nВведите дату в нужном формате или воспользуйтесь календарём",
                                        calendar(monthToRenderCalendar));
                            }
                        case AMOUNT:
                            return setAmountHandler(text);
                        case DESCRIPTION:
                            return setDescriptionHandler(text);
                        //unreachable state
                        default:
                            return null;
                    }
                } else {
                    return sendMsg("Я не болтаю попусту, я выполняю команды!!!", InlineKeyboardFactory.getBaseCommands());
                }
            }
        } else {
            return method;
        }
    }

    private BotApiMethod handleLogin(String text) {
        final LoginStage stageOfLogin = loginToExpenseApp.getStage();
        if (stageOfLogin == LoginStage.USERNAME) {
            if (validateUsername(text)) {
                String result = loginToExpenseApp.setUsername(text);
                if (SUCCESS.equals(result)) {
                    loginToExpenseApp.setValidUsername(text);
                    loginToExpenseApp.setStage(LoginStage.PASSWORD);
                    return sendMsg("Введите пароль:");
                } else {
                    return sendMsg("В системе нет такого юзера. Попробуйте другой логин или введите команду /stop",
                            getStop());
                }
            } else {
                return sendMsg("Форат логина неверный. Логин должен быть в формате email.\n Попробуйте другой логин или введите команду /stop",
                        getStop());
            }
        } else if (stageOfLogin == LoginStage.PASSWORD) {
            String result = loginToExpenseApp.setPassword(text);
            if (result.startsWith("\"TOKEN")) {
                loginToExpenseApp.setInProcess(false);
                parseAccessToken(result);
                return sendMsg("Авторизация прошла успешно", getBaseCommands());
            } else {
                return sendMsg("Пароль не подходит. Попробуйте ещё раз или введите команду /stop", getStop());
            }
        } else {
            return sendMsg("Ошибка авторизации. Нажмите опять Старт", getStart());
        }
    }

    private BotApiMethod setCardDateHandler(LocalDate cardDate) {
        // set card date
        calloutsToSalesforce.setCardDate(cardDate);
        // set stage to null
        calloutsToSalesforce.setNewCardStage(null);
        // set default month to render calendar
        monthToRenderCalendar = LocalDate.now();
        return checkIfReadyToSave();
    }

    private BotApiMethod setAmountHandler(String amountText) {
        try {
            Double.valueOf(amountText);
            // set card amount
            calloutsToSalesforce.setAmount(amountText);
            // set stage to null
            calloutsToSalesforce.setNewCardStage(null);
            return checkIfReadyToSave();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return sendMsg("Введенный текст не является числом. Вы ввели: " + amountText +
                    ". Введите корректное число или нажмите отмена", getCancel());
        }
    }

    private BotApiMethod setDescriptionHandler(String descriptionText) {
        // set description
        calloutsToSalesforce.setDescription(descriptionText);
        // set stage to null
        calloutsToSalesforce.setNewCardStage(null);
        return checkIfReadyToSave();
    }

    private BotApiMethod checkIfReadyToSave() {
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
            return sendMsg(message + "\n Сохранить карточку?", getCommandsToCreateNewCard());
        } else {
            // there are some null fields
            return sendMsg(message, getCommandsToFillNewCard());
        }
    }

    /**
     * Метод для настройки сообщения и его отправки.
     *
     * @param textMsg Строка, которую необходимот отправить в качестве сообщения.
     */
    private BotApiMethod sendMsg(String textMsg) {
        return configTextMessage(textMsg);
    }

    /**
     * Метод для настройки сообщения и его отправки.
     *
     * @param s              Строка, которую необходимот отправить в качестве сообщения.
     * @param keyboardMarkup inline keyboard of commands
     */
    private BotApiMethod sendMsg(String s, ReplyKeyboard keyboardMarkup) {
        SendMessage sendMessage = configTextMessage(s);
        sendMessage.setReplyMarkup(keyboardMarkup);
        return sendMessage;
    }

    private BotApiMethod updateMsg(Integer messageId, InlineKeyboardMarkup keyboardMarkup) {
        return new EditMessageReplyMarkup().
                setChatId(chatId).
                setMessageId(messageId).setReplyMarkup(keyboardMarkup);
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

    private BotApiMethod checkActive() {
        if (isActive) {
            return null;
        } else {
            // bot is inactive
            return sendMsg("Вы не авторизированы. Введите команду /start", getStart());
        }
    }

    private BotApiMethod checkAuth() {
        BotApiMethod method;
        if ((method = checkActive()) == null) {
            if (!loginToExpenseApp.isInProcess()) {
                // user is logged
                return null;
            } else {
                // user is in process of login
                return sendMsg("Вы ещё не авторизовались!!!\nВведите необходимые данные или команду /stop",
                        getStop());
            }
        } else {
            return method;
        }
    }

    private BotApiMethod checkNewCardProcess() {
        BotApiMethod method;
        if ((method = checkAuth()) == null) {
            if (calloutsToSalesforce.isNewCardProcessGoIn()) {
                return null;
            } else {
                return sendMsg("Эта команда в данный млмент не доступна", getBaseCommands());
            }
        } else {
            return method;
        }
    }

    private BotApiMethod checkIfDateSetStage() {
        BotApiMethod method;
        if ((method = checkNewCardProcess()) == null) {
            if (calloutsToSalesforce.getNewCardStage() == NewCardStage.DATE) {
                return null;
            } else {
                return sendMsg("Эта команда в данный млмент не доступна", getCommandsToFillNewCard());
            }
        } else {
            return method;
        }
    }

    private BotApiMethod checkData(String amount, String description, LocalDate cardDate) {
        if (amount != null) {
            if (description != null) {
                if (cardDate != null) {
                    return null;
                } else {
                    return sendMsg("Дата не заполнена. Все поля должны быть заполнены!!!",
                            getCommandsToFillNewCard());
                }
            } else {
                return sendMsg("Описание не заполнено. Все поля должны быть заполнены!!!",
                        getCommandsToFillNewCard());
            }
        } else {
            return sendMsg("Цена не указана. Все поля должны быть заполнены!!!",
                    getCommandsToFillNewCard());
        }
    }
}
