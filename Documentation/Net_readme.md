### Сетевой конфиг проекта
- В проекте эмулируется отдельный сегмент сети с помощью Vagrant + VirtualBox: фиксируется подсеть 192.168.56.0/24 через config.vm.network "private_network". VirtualBox при первом создании host-only сети назначает IP хоста, как правило 192.168.56.1, и далее Vagrant эту сеть переиспользует, пока не менется подсеть.
- Поэтому в рамках одного хоста и одной подсети 192.168.56.0 считается стабильным адресом хоста и дополнительно валидируется на нодах через ip route и ping шлюза.
- При этом, понимаются ограничения: если изменить конфигурацию VirtualBox или перенести ВМ на другой хост, адрес может поменяться. Поэтому в проде есть смысл вынести IP в инфраструктурный конфиг (Ansible);
- Обмен данными приложения и telegran по сети происходит следующим образом:
    - Long polling — это способ получать события “почти в реальном времени”: клиент делает запрос к серверу и держит его открытым, пока не появятся новые данные.
    - Как только сервер отдаёт данные, клиент сразу же открывает следующий запрос.
    - В контексте Telegram: бот сам регулярно вызывает getUpdates и ждёт новые сообщения.

Тебе удобно держать в голове 4 потока:
    1. Client/UI traffic (браузер/админка) → NginxUi → внутренние UI-сервисы
    2. API/tech traffic (тех. эндпоинты, вебхуки, внутренние API) → NginxTech → внутренние сервисы
    3. Observability traffic
        - Metrics (Prometheus pull): Monitoring → (все ноды/экспортеры/приложение)
        - Logs (FluentBit push): (все ноды) → Logging/агрегатор → OpenSearch
    4. Data traffic (PostgreSQL) : приложение/сервисы → Database

### Взаимодействие нод кластера:
- 192.168.56.1 (localhost) - приложение; 
    - Роли: Application node (Spring Boot + Telegram Bot), Prometheus, Grafana;
    - Входящий трафик:
        - Prometheus принимает HTTP-запросы от Grafana (localhost → localhost)
        - 192.168.56.210:9187 — postgres_exporter
        - 192.168.56.211:9100 — node_exporter (NginxTech)
        - 192.168.56.212:9100 — node_exporter (NginxUi)
        - 192.168.56.213:9100 — node_exporter (Logging)
        - 192.168.56.214:9100 — node_exporter (Monitoring)
    - Исходящий трафик:
        - api.telegram.org:443 — Telegram Bot API ✅
        - 192.168.56.210:5432 — PostgreSQL ✅
        - 192.168.56.211:80/443 — если дергаешь tech-endpoint’ы через NginxTech
        - 192.168.56.214:10051 — Zabbix Server (если используешь active checks с агента)
- 192.168.56.210 (Database)
    - Роль: принимает 5432/tcp от приложений/тех-сервисов/возможно от backup/логгинг-ноды.
    - Входящий трафик:
        - localhost:5432 — приложение ✅
        - localhost:9187 — Prometheus (postgres_exporter)
    - Исходящий трафик:
        - 192.168.56.214:10051 — Zabbix (если agent active)
        - 192.168.56.213 — логи (через Fluent Bit)
- 192.168.56.211 (NginxTech)
    - Роль: Technical reverse-proxy ingress/ NginxTech = application gateway;
    - Входящий трафик:
        - 192.168.56.1:80/443 — tech-запросы, API, webhooks (локальные)
    - Исходящий трафик:
        - 192.168.56.1:8080 — приложение
        - 192.168.56.214:10051 — мониторингш (Zabbix, FluentD)
        - 192.168.56.213 — логи (Opensearch, Zabbix, FluentD)
    - localhost:9100 — Prometheus (node_exporter)
- 192.168.56.212 (NginxUi)
    - Роль: UI reverse-proxy (админские интерфейсы) / Browser/UI ingress (human-facing)
    - Входящмй трафик:
        - 192.168.56.1:80/443 — браузер
    - Исходящий трафик:
        - 192.168.56.1:3000 — Grafana
        - 192.168.56.214 — Zabbix UI (если проксируешь)
    localhost:9100 — Prometheus
    192.168.56.213 — Fluent Bit
- 192.168.56.213 (Loggig)
    - Роль: Log-collector / buffer node
    - Входящий трафик: 
        - все ноды — Fluent Bit (push логов)
    - Исходящий трафик:
        - OpenSearch (если он здесь или дальше по сети)
        - 192.168.56.214 — Fluentd (если Monitoring — aggregator)
    localhost:9100 — Prometheus
    192.168.56.214:10051 — Zabbix
- 192.168.56.214 (Monitoring)
    - Роль: Zabbix Server, Fluentd (log aggregation / routing)
    - Входящий трафик:
        - все ноды: 10051/tcp — Zabbix agents; Fluentd input (logs);
    - Исходящий трафик:
        - все ноды — Zabbix passive checks (если используешь)
    - localhost:9100 — Prometheus (node_exporter)

### Движение трафика
- User/Web traffic: Browser → nginx_ui:443 → внутрь на UI сервисы (grafana, zabbix frontend, opensearch dashboards, etc.).
- App traffic: k8s app → DB:5432 (напрямую).
- Telemetry/Logs: FluentBit/forwarders → FluentD aggregator:24224 → OpenSearch:9200.
- Monitoring;
    - Active: Agents → Zabbix server:10051
    - Passive: Zabbix server → Agents:10050

### SELinux
[Статейка на хабре:](https://habr.com/ru/companies/kingservers/articles/209644/)
- Текущий статус: enforcing;
- Основные задачи (в режиме enforcing):
    - Проверить, что PostgreSQL, Nginx, Docker/K8s стартуют и работают без AVC-ошибок.
    - Если что-то ломается — смотреть audit.log, править контексты/boolean’ы/политику.
https://habr.com/ru/articles/815479/

## Команды для работы с сетью

```sh
ip route | grep default # Узнать адрес хоста Vagrant запросом с любой ноды
arp -n # Узнать адрес хоста Vagrant запcросом с любой ноды
```