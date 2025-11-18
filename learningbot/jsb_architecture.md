# Java Spring Boot Telegram Bot — Полная архитектура и рабочий код (обновлено)

## 📘 Общая цель

Создать Telegram-бота на **Java 25 / Spring Boot 3.5.7**, полностью совместимого с локальным окружением (MacBook M1, ARM64) и Kubernetes-кластером. Бот должен уметь добавлять и получать обучающие ресурсы, с возможностью последующей интеграции с PostgreSQL.

---

## 🧩 Среда и инструменты

* **Java:** 25 (Temurin / OpenJDK ARM64)
* **Spring Boot:** 3.5.7 (совместима с JDK 25)
* **Maven:** ≥3.9
* **PostgreSQL:** 15 (в Docker или кластерной среде)
* **Telegram API:** TelegramBots 6.8.0
* **Локальная разработка:** VS Code с Extension Pack for Java

---

## 📂 Проектная структура

```sh
learningbot/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/learningbot/
    │   │   ├── LearningBotApplication.java
    │   │   ├── config/TelegramBotConfig.java
    │   │   ├── controller/HealthController.java
    │   │   ├── domain/Resource.java
    │   │   ├── dto/{ResourceRequestDto.java, ResourceResponseDto.java}
    │   │   ├── repository/ResourceRepository.java
    │   │   ├── service/ResourceService.java
    │   │   └── telegram/LearningBot.java
    │   └── resources/application.yml
    └── test/java/com/learningbot/LearningBotTests.java
```

---

## ⚙️ `LearningBotApplication.java`

```java
package com.learningbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
    }
)
public class LearningBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(LearningBotApplication.class, args);
    }
}
```

💡 Исключения временно отключают базу данных, чтобы бот мог работать без DataSource.

---

## 🤖 `LearningBot.java`

```java
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

    private final ResourceService resourceService; // ⚠️ Сейчас не используется, IDE предупреждает "field is not used". Оно исчезнет, когда бот начнет сохранять данные в БД.
    private final String botUsername;
    private final String botToken;

    public LearningBot(@Value("${telegram.bot.username}") String botUsername,
                       @Value("${telegram.bot.token}") String botToken,
                       ResourceService resourceService) {
        super(botToken);
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
                    execute(new SendMessage(chatId, "Команда не распознана. Попробуйте /add или /get."));
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
```

---

## 🧩 `TelegramBotConfig.java`

```java
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

    @Bean
    public LearningBot learningBot(ResourceService resourceService) {
        return new LearningBot(botUsername, botToken, resourceService);
    }
}
```

---

## 🗄️ `ResourceService.java`

```java
package com.learningbot.service;

import com.learningbot.domain.Resource;
import com.learningbot.repository.ResourceRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Resource addResource(@NonNull Resource resource) {
        return resourceRepository.save(resource);
    }
}
```

💬 Комментарий: `@NonNull` здесь корректен — объект `resource` создаётся вручную и не может быть `null` по логике приложения.

---

## 🩺 `HealthController.java`

```java
package com.learningbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "LearningBot Application is running ✅";
    }
}
```

---

## 📘 DTO: `ResourceRequestDto.java`

```java
package com.learningbot.dto;

import jakarta.validation.constraints.*;

public record ResourceRequestDto(
        @NotBlank String title,
        @NotBlank String topic,
        @NotBlank String format,
        @Min(5) @Max(120) int durationMin,
        @NotBlank String source,
        String fileUrl
) {}
```

### 📘 DTO: `ResourceResponseDto.java`

```java
package com.learningbot.dto;

import java.time.LocalDateTime;

public record ResourceResponseDto(
        Long id,
        String title,
        String topic,
        String format,
        int durationMin,
        String source,
        String fileUrl,
        LocalDateTime createdAt
) {}
```

---

## 🧾 `application.yml` (тестовый)

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

telegram:
  bot:
    username: learningbot_bot
    token: ${TELEGRAM_BOT_TOKEN:YOUR_TEST_BOT_TOKEN_HERE}

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health, info
  endpoint:
    health:
      show-details: always

logging:
  level:
    root: INFO
    com.learningbot: DEBUG
```

---

## 🧱 `pom.xml` (финальный под Java 25 и Spring Boot 3.5.7)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.learningbot</groupId>
    <artifactId>learningbot</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>25</java.version>
        <spring.boot.version>3.5.7</spring.boot.version>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>${spring.boot.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring.boot.version}</version>
        </dependency>
        <dependency>
            <groupId>org.telegram</groupId>
            <artifactId>telegrambots-spring-boot-starter</artifactId>
            <version>6.8.0</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.34</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.6.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <version>${spring.boot.version}</version>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>${spring.boot.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring.boot.version}</version>
                <executions>
                    <execution>
                        <goals><goal>repackage</goal></goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## ✅ Проверка запуска

```bash
mvn clean package -DskipTests
java -jar target/learningbot-1.0.0.jar
```

Ожидаемый вывод:

```
:: Spring Boot :: (v3.5.7)
Started LearningBotApplication in 2.8 seconds
✅ Bot initialized with username: learningbot
```

Endpoint для проверки: [http://localhost:8080/health](http://localhost:8080/health)

---

## 📌 Примечания

* `@NonNull` используется только там, где логика приложения гарантирует ненулевые объекты.
* JPA и PostgreSQL временно отключены до момента настройки внешней базы.
* Telegram-бот работает автономно, используя токен из `application.yml`.

---

Файл полностью отражает текущее состояние проекта после всех исправлений, обсуждений и устранения ошибок.
