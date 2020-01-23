package com.expense.updareshandlers;

import com.expense.inlinekeyboard.InlineKeyboardFactory;
import com.expense.salesforceconection.Login;
import com.expense.salesforceconection.LoginStage;
import com.expense.salesforceconection.LoginToExpenseApp;
import com.expense.salesforceconection.RequestParameters;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class ChatUpdateHandler {

    private RequestParameters requestParameters;
    private LoginToExpenseApp loginToExpenseApp;
    private LongPollingBot bot;
    private Message message;
    private boolean isActive = false;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public ChatUpdateHandler(LongPollingBot bot) {
        this.bot = bot;
    }

    public void commandHandler(Message message) {
        this.message = message;
        switch (message.getText()) {
            case Commands.START:
                startHandler();
                break;
            case Commands.BALANCE:
                balanceHandler();
                break;
            case Commands.NEW_EXPENSE_CARD:
                newCardHandler();
                break;
            default:
                textHandler();
                break;
        }
    }

    private void startHandler() {
        requestParameters = Login.getToken();
        loginToExpenseApp = new LoginToExpenseApp(requestParameters);
        isActive = true;
        sendMsg(message.getChatId(), "ВВедите логин:");
    }

    private void balanceHandler() {
        if (isActive){
            // bot is active (command /start was executed)
            if (!loginToExpenseApp.isInProcess()){
                // user is logged

            } else {
                // user is in process of login
                sendMsg(message.getChatId(), "Вы ещё не авторизовались!!!\nВведите необходимые данные или команду /stop");
            }
        } else {
            // bot is inactive
            sendMsg(message.getChatId(), "Введите команду /start");
        }
    }

    private void newCardHandler(){

    }

    private void textHandler() {
        String text = message.getText();
        System.out.println(text);
        if (isActive) {
            if (loginToExpenseApp.isInProcess()) {
                handleLogin(text);
            } else {
                // TODO Я не болтаю попусту, я выполняю команды и снова кнопки
            }
        } else {
            sendMsg(message.getChatId(), "Введите команду /start");
        }
    }

    private void handleLogin(String text) {
        Long chatId = message.getChatId();
        final LoginStage stageOfLogin = loginToExpenseApp.getStage();
        if (stageOfLogin == LoginStage.USERNAME) {
            if (validate(text)) {
                String result = loginToExpenseApp.setUsername(text);
                if ("\"SUCCESS\"".equals(result)) {
                    loginToExpenseApp.setValidUsername(text);
                    loginToExpenseApp.setStage(LoginStage.PASSWORD);
                    sendMsg(chatId, "Введите пароль:");
                } else {
                    sendMsg(chatId, "В системе нет такого юзера. Попробуйте другой логин или введите команду /stop");
                }
            } else {
                sendMsg(chatId, "Форат логина неверный. Логин должен быть в формате email");
            }
        } else if (stageOfLogin == LoginStage.PASSWORD) {
            String result = loginToExpenseApp.setPassword(text);
            if (result.startsWith("\"TOKEN")) {
                loginToExpenseApp.setInProcess(false);
                parseAccessToken(result);
                InlineKeyboardMarkup keyboardMarkup =
                        InlineKeyboardFactory.keyboard(baseCommandButtons());
                sendMsg(chatId, "Авторизация прошла успешно", keyboardMarkup);
            } else {
                sendMsg(chatId, "Пароль не подходит. Попробуйте ещё раз или введите команду /stop");
            }
        }
    }

    private Map<String, String> baseCommandButtons() {
        Map<String, String> map = new HashMap<>();
        map.put("Текущий баланс",Commands.BALANCE);
        map.put("Создать карточку",Commands.NEW_EXPENSE_CARD);
        return map;
    }

    /**
     * Метод для настройки сообщения и его отправки.
     *
     * @param chatId id чата
     * @param s      Строка, которую необходимот отправить в качестве сообщения.
     */
    private synchronized void sendMsg(Long chatId, String s) {
        SendMessage sendMessage = configTextMessage(chatId, s);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для настройки сообщения и его отправки.
     *
     * @param chatId         id чата
     * @param s              Строка, которую необходимот отправить в качестве сообщения.
     * @param keyboardMarkup inline keyboard of commands
     */
    private synchronized void sendMsg(Long chatId, String s, ReplyKeyboard keyboardMarkup) {
        SendMessage sendMessage = configTextMessage(chatId, s);
        sendMessage.setReplyMarkup(keyboardMarkup);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private SendMessage configTextMessage(Long chatId, String s) {
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

    private boolean validate(String username) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return username.matches(regex);
    }
}
