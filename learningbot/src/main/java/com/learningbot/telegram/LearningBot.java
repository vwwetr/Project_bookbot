package com.learningbot.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.learningbot.service.ResourceService;

@Component
public class LearningBot extends TelegramLongPollingBot {

    private final ResourceService resourceService; // ⚠️ Сейчас не используется внутри класса, поэтому IDE показывает предупреждение "field is not used". 
    //Оно исчезнет, когда бот начнет вызывать методы resourceService (например, при добавлении или выдаче материалов пользователю). Поле уже корректно внедряется Spring, это не ошибка.
    private final String botUsername;
    private final String botToken;

    public LearningBot(@Value("${telegram.bot.username}") String botUsername,
                       @Value("${telegram.bot.token}") String botToken,
                       ResourceService resourceService) {
        super(botToken); // ✅ правильный вызов конструктора суперкласса
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.resourceService = resourceService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            try {
                if (text.equalsIgnoreCase("/start")) {
                    execute(new SendMessage(chatId, "Привет! Я ваш обучающий бот 🤖"));
                } else if (text.equalsIgnoreCase("/add")) {
                    execute(new SendMessage(chatId, "Введите название материала:"));
                } else {
                    execute(new SendMessage(chatId, "Команда не распознана. Попробуйте /add или /get"));
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
