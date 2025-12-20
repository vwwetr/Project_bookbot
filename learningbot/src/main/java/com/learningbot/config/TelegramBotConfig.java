package com.learningbot.config;

import com.learningbot.telegram.LearningBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(LearningBot learningBot) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(learningBot);
        learningBot.execute(new SetMyCommands(commandList(), new BotCommandScopeDefault(), null));
        return botsApi;
    }

    private List<BotCommand> commandList() {
        return List.of(
                new BotCommand("/start", "Старт и подсказки"),
                new BotCommand("/add", "Добавить ресурс"),
                new BotCommand("/get", "Найти по времени"),
                new BotCommand("/cancel", "Отменить действие")
        );
    }
}
