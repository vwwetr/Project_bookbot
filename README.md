# Project_bookbot

Telegram learning bot on JavaSpringBoot.

- После билда всего проекта:
    - Расписать здесь структуру проекта и навигацию по ней
    - Привести в порядок документацию во всех директориях
    - Актуализировать логическую схему

## Стек:
- 
- 
- 
- 

## Документация:
- Логическая схема проекта: /Documentation/project_scheme.png (или .drawio)
- (Обязательно перед запуском!): Открой и выполни **Get_started.md** (Путь: /Documentation/Get_started.md)
- В случае интеграции git + Jira (планировщик): открой и настрой **Project_gitflow.md**
- Для работы с ChatGPT используй готовые шаблоны PROMPT-запросов (Путь: /Documentation/PROMPTS (GPT))

## 📘 Java
```sh
brew install maven 
mvn clean package -DskipTests
java -jar target/learningbot-1.0.0.jar
```

## CentOS nodes setting up
```sh
# Пользователи и права:
sudo usermod -aG wheel vwwetr
echo "vwwetr ALL=(ALL) NOPASSWD:ALL" | sudo tee /etc/sudoers.d/vwwetr > /dev/null
sudo chmod 440 /etc/sudoers.d/vwwetr
# Firewall:
sudo dnf isntall -y # Обновить актуальный Firewall
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --list-all # Отобразить список, к каким сервисам уже предоставлен доступ через брандмауэр (Как это происходит в delivery?)
sudo dnf install -y cockpit
sudo systemctl enable --now cockpit.socket # Установка и немедленный запуск cockpit
sudo firewall-cmd --reload
#SSH - есть у vagrant при взаимодейтсвии с localhost, нужно будет прокидывать 
```
## Ansible
### Roles:
- database
- ZabbixAgent
- FluentD
- Nginx
- OpenSearch
- K8s
- FluentBit
### Users
- postgres (системный, для SQL)
    - Postgres roles:
        - postgres (для SQL)
        - app_user (для JSB)
        - metric_user (Для Zabbix и Prometheus)
- dbbackup (бекап базы данных)
    - Присутсвие на нодах: node4, node5, node1, 
- ansible
- Monitoring
- Logs

### Для приложения:
# Нормализация заголовка книги в Java Spring Boot + PostgreSQL

## 1. Поле `normalizedTitle` в сущности `Book`

Пример JPA-сущности:

```java
import jakarta.persistence.*;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Поле для нормализованного значения
    @Column(name = "normalized_title", nullable = false)
    private String normalizedTitle;

    // геттеры/сеттеры ...

    @PrePersist
    @PreUpdate
    private void normalize() {
        this.normalizedTitle = TitleNormalizer.normalizeTitle(this.title);
    }

    // геттеры/сеттеры ...
}
Ключевая идея: перед вставкой/обновлением в БД Spring/JPA вызывает @PrePersist / @PreUpdate, и там мы всегда пересчитываем normalizedTitle.

2. Реализация normalizeTitle(title) в Java
Утилитный класс, который делает trim + lower + "unaccent":

java
Copy code
import java.text.Normalizer;
import java.util.Locale;

public final class TitleNormalizer {

    private TitleNormalizer() {
    }

    public static String normalizeTitle(String title) {
        if (title == null) {
            return null;
        }

        // trim + lower
        String result = title.trim().toLowerCase(Locale.ROOT);

        // Убираем диакритику (акценты) через Unicode-normalization
        // Пример: "Café" -> "Cafe"
        String normalized = Normalizer.normalize(result, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
}
Аналог lower(trim(unaccent(title))), но на стороне Java:

trim() — убирает пробелы по краям;

toLowerCase(Locale.ROOT) — приводит к нижнему регистру;

Normalizer + replaceAll("\\p{M}", "") — удаляет диакритику (акценты).
```

1. Как Ansible ищет переменные в group_vars

Правило такое:

group_vars/all.yml → переменные для всех хостов;

group_vars/<groupname>.yml → переменные для группы [<groupname>] из hosts.ini;

group_vars/<groupname>/vars.yml → то же самое, только в подкаталоге;

аналогично с host_vars/<hostname>.yml — переменные для конкретного хоста.