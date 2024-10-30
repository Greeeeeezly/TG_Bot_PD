package com.example.tg_bot_pd;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyTelegramBot extends TelegramLongPollingBot {

    private final String BOT_USERNAME = "MyMIITAdmissionBot";
    private final String BOT_TOKEN = "8034250486:AAE0IbxMfgOntzJq_uADS4cg_lgiPjO27cg";
    private final Set<String> selectedSubjects = new HashSet<>(); // Хранит выбранные предметы

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            String userText = message.getText();

            if (userText.equals("/start")) {
                selectedSubjects.clear(); // Очищаем выбранные предметы
                sendSubjectList(chatId);
            } else if (selectedSubjects.size() < 4 && isSubject(userText)) {
                // Если выбранный предмет, добавляем его в выбранные
                selectedSubjects.add(userText);
                sendMessage(chatId, "Выбран предмет: " + userText + ". Выберите еще или введите сумму баллов.");
            } else if (userText.equals("/confirm")) {
                // Подтверждение выбора и запрос суммы баллов
                if (selectedSubjects.isEmpty()) {
                    sendMessage(chatId, "Вы не выбрали ни одного предмета. Пожалуйста, выберите предметы.");
                } else {
                    sendMessage(chatId, "Введите сумму баллов:");
                }
            } else if (isInteger(userText)) {
                // Если введено целое число, выводим результаты
                int score = Integer.parseInt(userText);
                sendMessage(chatId, "Вы выбрали предметы: " + String.join(", ", selectedSubjects) + "\nСумма баллов: " + score);
                selectedSubjects.clear(); // Очищаем после вывода
            } else {
                sendMessage(chatId, "Пожалуйста, выберите предмет из списка или введите команду /confirm для подтверждения выбора.");
            }
        }
    }

    private void sendSubjectList(String chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Математика"));
        row1.add(new KeyboardButton("Русский"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Информатика"));
        row2.add(new KeyboardButton("Физика"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Английский"));
        row3.add(new KeyboardButton("География"));

        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("Химия"));
        row4.add(new KeyboardButton("Биология"));

        KeyboardRow row5 = new KeyboardRow();
        row5.add(new KeyboardButton("Литература"));
        row5.add(new KeyboardButton("/confirm")); // Кнопка для подтверждения

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите до 4 предметов:");
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isSubject(String text) {
        return text.equals("Математика") || text.equals("Русский") || text.equals("Информатика") ||
                text.equals("Физика") || text.equals("Английский") || text.equals("География") ||
                text.equals("Химия") || text.equals("Биология") || text.equals("Литература");
    }

    private boolean isInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
