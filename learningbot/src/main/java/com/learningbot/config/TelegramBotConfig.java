package com.learningbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.learningbot.telegram.LearningBot;
import com.learningbot.service.ResourceService;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Bean
    public TelegramBotsApi telegramBotsApi(LearningBot learningBot) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(learningBot);
        return botsApi;
    }

    /**
     * Бин бота создаётся с зависимостью ResourceService.
     * Имя и токен передаются из application.yml.
     */
    @Bean
    public LearningBot learningBot(ResourceService resourceService) {
        return new LearningBot(botUsername, botToken, resourceService);
    }
}
