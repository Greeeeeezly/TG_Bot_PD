package com.example.tg_bot_pd;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class MyTelegramBot extends TelegramLongPollingBot {

    private final String BOT_USERNAME = "MyMIITAdmissionBot";
    private final String BOT_TOKEN = "8034250486:AAE0IbxMfgOntzJq_uADS4cg_lgiPjO27cg";

    private final Map<String, Integer> selectedSubjects = new HashMap<>(); // Хранит предметы и их баллы
    private boolean awaitingScore = false; // Флаг для ожидания ввода баллов
    private String currentSubject = null; // Текущий выбранный предмет
    private int sumScore = 0; // Общая сумма баллов

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
                resetSelection();
                sendSubjectList(chatId);
            } else if (!awaitingScore && isSubject(userText)) {
                currentSubject = userText;
                awaitingScore = true;
                sendMessage(chatId, "Введите баллы за предмет \"" + userText + "\" (0–100):");
            } else if (awaitingScore && isInteger(userText)) {
                int score = Integer.parseInt(userText);
                if (score >= 0 && score <= 100) {
                    selectedSubjects.put(currentSubject, score);
                    sumScore += score;
                    awaitingScore = false;
                    currentSubject = null;
                    sendMessage(chatId, "Баллы сохранены. Вы можете выбрать следующий предмет или ввести /confirm для завершения.");
                    sendSubjectList(chatId);
                } else {
                    sendMessage(chatId, "Введите корректные баллы (0–100):");
                }
            } else if (userText.equals("/confirm")) {
                if (selectedSubjects.isEmpty()) {
                    sendMessage(chatId, "Вы не выбрали ни одного предмета. Начните с команды /start.");
                } else {
                    sendMessage(chatId, "Ваш выбор:\n" + formatSelection() + "\nОбщая сумма баллов: " + sumScore + "\nСпасибо за использование бота!");
                    resetSelection();
                }
            } else {
                sendMessage(chatId, "Пожалуйста, выберите предмет из списка или введите баллы.");
            }
        }
    }

    // Метод для отправки списка предметов
    private void sendSubjectList(String chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        // Создание кнопок из SubjectEnum
        List<KeyboardRow> keyboard = generateKeyboardFromEnum();

        // Добавление кнопки подтверждения
        KeyboardRow confirmRow = new KeyboardRow();
        confirmRow.add(new KeyboardButton("/confirm"));
        keyboard.add(confirmRow);

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите предмет:");
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // Генерация кнопок на основе SubjectEnum
    private List<KeyboardRow> generateKeyboardFromEnum() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();

        // Итерация по перечислениям SubjectEnum
        for (SubjectEnum subject : SubjectEnum.values()) {
            currentRow.add(new KeyboardButton(subject.getDescription()));

            // Если в строке уже 2 кнопки, добавляем строку в клавиатуру
            if (currentRow.size() == 2) {
                keyboard.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        // Добавляем оставшиеся кнопки в строке
        if (!currentRow.isEmpty()) {
            keyboard.add(currentRow);
        }

        return keyboard;
    }

    // Проверка, является ли текст предметом из SubjectEnum
    private boolean isSubject(String text) {
        for (SubjectEnum subject : SubjectEnum.values()) {
            if (subject.getDescription().equals(text)) {
                return true;
            }
        }
        return false;
    }

    // Проверка, является ли текст числом
    private boolean isInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Отправка сообщения
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

    // Сброс текущего выбора
    private void resetSelection() {
        selectedSubjects.clear();
        awaitingScore = false;
        currentSubject = null;
        sumScore = 0;
    }

    // Форматирование выбранных предметов
    private String formatSelection() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Integer> entry : selectedSubjects.entrySet()) {
            result.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return result.toString();
    }
}
