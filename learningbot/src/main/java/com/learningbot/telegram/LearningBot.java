package com.learningbot.telegram;

import com.learningbot.domain.Resource;
import com.learningbot.service.ResourceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LearningBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(LearningBot.class);

    private final ResourceService resourceService;
    private final String botUsername;
    private final String botToken;
    private final Map<Long, Conversation> conversations = new ConcurrentHashMap<>();

    public LearningBot(@Value("${telegram.bot.username}") String botUsername,
                       @Value("${telegram.bot.token}") String botToken,
                       ResourceService resourceService) {
        super(botToken);
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.resourceService = resourceService;
        log.info("Telegram bot initialized: {}", botUsername);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("Update message from chat {}: {}", update.getMessage().getChatId(), update.getMessage().getText());
        } else if (update.hasCallbackQuery()) {
            log.info("Callback from chat {}: {}", update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getData());
        } else {
            log.debug("Update received with no text/callback");
        }
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();
            long chatIdLong = update.getMessage().getChatId();
            Conversation conversation = conversations.computeIfAbsent(chatIdLong, key -> new Conversation());

            try {
                String normalizedText = text.trim().toLowerCase();
                if (text.equalsIgnoreCase("/start")) {
                    SendMessage message = new SendMessage(chatId, "Привет! Я ваш обучающий бот 🤖\nВыберите команду ниже.");
                    message.setReplyMarkup(commandKeyboard());
                    execute(message);
                    conversation.reset();
                } else if (text.equalsIgnoreCase("/add") || normalizedText.equals("добавить ресурс")) {
                    execute(new SendMessage(chatId, "Введите название ресурса:"));
                    conversation.startAdd();
                } else if (text.equalsIgnoreCase("/get") || normalizedText.equals("получить ресурс")) {
                    execute(durationKeyboard(chatId, "Выберите доступное вам время на изучение, в мин.:", CallbackMode.GET));
                    conversation.startGet();
                } else if (text.equalsIgnoreCase("/cancel") || normalizedText.equals("выход")) {
                    conversation.reset();
                    SendMessage message = new SendMessage(chatId, "Бот остановлен. Как будешь готов, выбери одну из команд ниже.");
                    message.setReplyMarkup(commandKeyboard());
                    execute(message);
                } else {
                    handleConversationText(chatId, conversation, text);
                }
            } catch (TelegramApiException e) {
                log.error("Telegram API error while handling message", e);
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

    private void handleConversationText(String chatId, Conversation conversation, String text) throws TelegramApiException {
        switch (conversation.state) {
            case ADD_TITLE -> {
                conversation.draft.title = text.trim();
                conversation.state = State.ADD_AUTHOR;
                execute(new SendMessage(chatId, "Введите автора в формате \"Фамилия И.О.\" или введите \"Без автора\":"));
            }
            case ADD_AUTHOR -> {
                conversation.draft.author = text.trim();
                conversation.state = State.ADD_TOPIC;
                execute(topicKeyboard(chatId));
            }
            case ADD_URL -> {
                conversation.draft.link = normalizeUrl(text.trim());
                boolean saved = resourceService.addResource(conversation.draft.toRecord());
                if (saved) {
                    execute(new SendMessage(chatId, "Ресурс сохранен."));
                } else {
                    execute(new SendMessage(chatId, "Этот ресурс уже есть в базе!"));
                }
                conversation.reset();
            }
            default -> execute(new SendMessage(chatId, "Команда не распознана. Попробуйте /add или /get"));
        }
    }

    private void handleCallback(Update update) {
        String data = update.getCallbackQuery().getData();
        long chatIdLong = update.getCallbackQuery().getMessage().getChatId();
        String chatId = Long.toString(chatIdLong);
        Conversation conversation = conversations.computeIfAbsent(chatIdLong, key -> new Conversation());

        try {
            execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));
            if (data.startsWith("TOPIC:") && conversation.state == State.ADD_TOPIC) {
                conversation.draft.section = data.substring("TOPIC:".length());
                conversation.state = State.ADD_FORMAT;
                execute(formatKeyboard(chatId));
                return;
            }
            if (data.startsWith("FORMAT:") && conversation.state == State.ADD_FORMAT) {
                conversation.draft.format = data.substring("FORMAT:".length());
                conversation.state = State.ADD_DURATION;
                execute(durationKeyboard(chatId, "Выберите доступное вам время на изучение, в мин.:", CallbackMode.ADD));
                return;
            }
            if (data.startsWith("DURATION:") && conversation.state == State.ADD_DURATION) {
                conversation.draft.studyTime = Integer.parseInt(data.substring("DURATION:".length()));
                conversation.state = State.ADD_URL;
                execute(new SendMessage(chatId, "Введите ссылку на ресурс или введите \"Нет URL\":"));
                return;
            }
            if (data.startsWith("GET_DURATION:") && conversation.state == State.GET_DURATION) {
                int duration = Integer.parseInt(data.substring("GET_DURATION:".length()));
                List<Resource> resources = resourceService.getResourcesByStudyTime(duration);
                if (resources.isEmpty()) {
                    execute(new SendMessage(chatId, "Ресурсов по этому времени еще нет в базе!"));
                } else {
                    execute(new SendMessage(chatId, formatResourceList(resources)));
                }
                conversation.reset();
                return;
            }
            execute(new SendMessage(chatId, "Сначала выберите команду /add или /get."));
        } catch (TelegramApiException e) {
            log.error("Telegram API error while handling callback", e);
        }
    }

    private SendMessage topicKeyboard(String chatId) {
        SendMessage message = new SendMessage(chatId, "Выберите раздел, к которому нужно отнести ресурс:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(button("IT", "TOPIC:IT"), button("Health", "TOPIC:Health")));
        rows.add(List.of(button("Finance", "TOPIC:Finance"), button("Lifestyle", "TOPIC:Lifestyle")));
        rows.add(List.of(button("Network", "TOPIC:Network"), button("Spiritual", "TOPIC:Spiritual")));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        return message;
    }

    private SendMessage formatKeyboard(String chatId) {
        SendMessage message = new SendMessage(chatId, "Выберите формат ресурса:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(button("Book", "FORMAT:Book"), button("Article", "FORMAT:Article")));
        rows.add(List.of(button("Video", "FORMAT:Video"), button("Audio", "FORMAT:Audio")));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        return message;
    }

    private SendMessage durationKeyboard(String chatId, String text, CallbackMode mode) {
        SendMessage message = new SendMessage(chatId, text);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        String prefix = mode == CallbackMode.ADD ? "DURATION:" : "GET_DURATION:";

        rows.add(List.of(button("15", prefix + "15"), button("30", prefix + "30"), button("60", prefix + "60")));
        rows.add(List.of(button("90", prefix + "90"), button("120", prefix + "120")));

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        return message;
    }

    private InlineKeyboardButton button(String text, String data) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(data);
        return button;
    }

    private ReplyKeyboardMarkup commandKeyboard() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);

        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Добавить ресурс");
        row.add("Получить ресурс");
        row.add("Выход");
        rows.add(row);

        markup.setKeyboard(rows);
        return markup;
    }

    private String normalizeUrl(String value) {
        if (value.equalsIgnoreCase("Нет URL")) {
            return null;
        }
        return value;
    }

    private String formatResourceList(List<Resource> resources) {
        StringBuilder builder = new StringBuilder("Подходящие ресурсы:\n");
        for (Resource resource : resources) {
            builder.append("• ")
                    .append(resource.getTitle())
                    .append(" — ")
                    .append(resource.getAuthor())
                    .append(" (")
                    .append(resource.getFormat())
                    .append(", ")
                    .append(resource.getStudyTime())
                    .append(" мин.)");
            if (resource.getLink() != null && !resource.getLink().isBlank()) {
                builder.append("\n").append(resource.getLink());
            }
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    private enum State {
        IDLE,
        ADD_TITLE,
        ADD_AUTHOR,
        ADD_TOPIC,
        ADD_FORMAT,
        ADD_DURATION,
        ADD_URL,
        GET_DURATION
    }

    private enum CallbackMode {
        ADD,
        GET
    }

    private static class Conversation {
        private State state = State.IDLE;
        private Draft draft = new Draft();

        void startAdd() {
            this.state = State.ADD_TITLE;
            this.draft = new Draft();
        }

        void startGet() {
            this.state = State.GET_DURATION;
        }

        void reset() {
            this.state = State.IDLE;
            this.draft = new Draft();
        }
    }

    private static class Draft {
        private String title;
        private String author;
        private String section;
        private String format;
        private Integer studyTime;
        private String link;

        ResourceService.ResourceDraft toRecord() {
            String safeAuthor = (author == null || author.isBlank()) ? "Без автора" : author;
            return new ResourceService.ResourceDraft(
                    title,
                    safeAuthor,
                    section,
                    format,
                    studyTime,
                    link
            );
        }
    }
}
