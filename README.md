# Project_bookbot

Telegram learning bot on JavaSpringBoot.
Здесь описан quick quidde проекта. Остальную документацию смотри в `./Documentation`

- После билда всего проекта:
    - Расписать здесь структуру проекта и навигацию по ней
    - Привести в порядок документацию во всех директориях

## Стек:
- Vagrant (version) + Virtualbox (version) - Виртуализация нод кластера;
    - vagrant plugin install vagrant-vbguest (на хосте) для синхронизации времени хоста и времени нод;
    - https://formulae.brew.sh/cask/vagrant
    - https://formulae.brew.sh/cask/virtualbox
- 

## Документация:
- Логическая схема проекта: /Documentation/project_scheme.png (или .drawio)
- (Обязательно перед запуском!): Открой и выполни **Get_started.md** (Путь: /Documentation/Get_started.md)
- Git-flow проекта: открой и настрой **Project_gitflow.md**
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
- Users
    - cluster_users
- database
    - pg_install
    - db_create
    - db_bakcup
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
- Logs (192.168.56.213)

## Для приложения:
### Нормализация заголовка книги в Java Spring Boot + PostgreSQL

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

### Сетевой конфиг проекта
- В проекте эмулируется отдельный сегмент сети с помощью Vagrant + VirtualBox: фиксируется подсеть 192.168.56.0/24 через config.vm.network "private_network". VirtualBox при первом создании host-only сети назначает IP хоста, как правило 192.168.56.1, и далее Vagrant эту сеть переиспользует, пока не менется подсеть.
- Поэтому в рамках одного Mac’а и одной подсети 192.168.56.1 считается стабильным адресом хоста и дополнительно валидируется на нодах через ip route и ping шлюза.
- При этом, понимаются ограничения: если изменить конфигурацию VirtualBox или перенести ВМ на другой хост, адрес может поменяться. Поэтому в проде есть смысл вынести IP в инфраструктурный конфиг (Terraform/Ansible) или в DNS/Service Discovery.

## Безопасность
### SELinux
[Статейка на хабре:](https://habr.com/ru/companies/kingservers/articles/209644/)
- Текущий статус: enforcing;
- Основные задачи (в режиме enforcing):
    - Проверить, что PostgreSQL, Nginx, Docker/K8s стартуют и работают без AVC-ошибок.
    - Если что-то ломается — смотреть audit.log, править контексты/boolean’ы/политику.

https://habr.com/ru/articles/815479/