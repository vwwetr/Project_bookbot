# Metrics, grabbing by cluster components

## Logging
### Fluent Bit
- Основное: логи/события (например, stdout контейнеров в k8s, файлы логов, journald и т.п.) и пересылка в агрегатор FluenD по TCP
- Дополнительно: Fluent Bit умеет экспортировать собственные метрики пайплайна (сколько записей обработал, ошибки, ретраи, буферы) через HTTP-сервер, включая Prometheus-формат.
- Присутствие на нодах:
    - Localhost k8s (forwarder);
    - Monitoring (forwarder);
    - Database (forwarder);
    - NginxUi (forwarder);
    - NginxTech (forwarder);
- Роль: агент-сборщик для контейнерных логов (stdout/stderr, файлы контейнерного рантайма), обычно как DaemonSet.
- Почему он тут уместен: минимальные ресурсы, высокая скорость, достаточно простого парсинга/обогащения и отправки дальше
### Fluentd
- Основное: логи/события (как агрегатор на logging-ноде): читает, парсит/обогащает, буферизует, маршрутизирует в storage (OpenSearch и т.п.).
- Self-metrics: Fluentd может отдавать свои внутренние метрики через in_monitor_agent (REST API), а также поддерживает метрики/экспорт в Prometheus через plugins.
- Присутствие на нодах:
    - Logging (agragator);
- Настройки:
    - fluentd_aggregator (agregator)
        - in_forward и открой firewall 24224/TCP и 24224/UDP только из подсетей/групп, где живут твои VM и Minikube (по твоей схеме 192.168.56.0/24)
        - Если нужен health/инспекция — включи monitor_agent, но bind=127.0.0.1 (или отдельная mgmt-сеть) и не публикуй через nginx_ui без необходимости
### OpenSearch
- Основное: хранилище и поисково-аналитический движок для документов (часто это логи/events, но может быть любая структура).
- OpenSearch (engine): хранит индексы, принимает данные (логи/метрики/события) и обслуживает поисковые запросы через REST API.
- Метрики OpenSearch (self): у OpenSearch есть собственные метрики кластера/производительности (метрики framework/performance analyzer и т.п.), которые используются для мониторинга самого OpenSearch.
- Порты: 
    - 9200/TCP — HTTP(S) REST API (куда обычно пишут FluentBit/Fluentd/приложения и откуда читает Dashboards)
    - 9300/TCP — transport (внутрикластерное node-to-node общение).
    - 9600/TCP — Performance Analyzer API (метрики производительности).
    - 9650/TCP — RPC/служебный порт, который в некоторых сценариях связан с Performance Analyzer / RCA (часто виден в docker-compose примерах).
### OpenSearch Dashboards
- Не собирает данные.
- Роль: UI для исследования/поиска/визуализации данных, лежащих в OpenSearch (Discover, визуализации, dashboards).
- веб-морда для поиска, построения дашбордов, работы с плагинами (Alerting/ISM/Security и т.д.).
- OpenSearch Dashboards не “принимает данные” от OpenSearch — он делает запросы в OpenSearch по API и отображает результаты.
- Порты:
    - 5601/TCP — веб-интерфейс Dashboards.
## Monitoring
### Zabbix Agent
- Собирает: метрики хоста/ОС и сервисов (CPU load, memory, disk, network, процессы, порты и т.п.), плюс любые кастомные метрики (UserParameter, скрипты). Принцип: пассивно отвечает на запрос сервера или активно отправляет собранные значения.
- Важно: Zabbix Agent не про логи по умолчанию; логи в Zabbix — отдельная тема (log items), но твой текущий “лог-стек” лучше держать в Fluent/OpenSearch.
- Режим работы: Active check:
    - Агенты сами подключаются к серверу на 10051/tcp, и тебе не нужно открывать 10050 inbound на каждой ноде
### Zabbix server
- Не “собирает метрику с ОС” напрямую как агент, а выполняет роль центрального сборщика/обработчика:
    - принимает/опрашивает данные у агентов/прокси,
    - вычисляет триггеры,
    - шлёт нотификации,
    - хранит данные (в своей БД).
- Zabbix Server (и лучше Zabbix Web) — на monitoring-ноде:
    - Zabbix Server: inbound 10051/TCP только от подсети VM (например, 192.168.56.0/24)
- Сервису нужна своя база данных: ⛔
    - В PostgreSQL создаём отдельную БД zabbix и пользователя zabbix. ⛔
    - Разрешить сетевой доступ к PostgreSQL только от Zabbix-ноды ⛔
    - Импортировать initial schema Zabbix в БД zabbix ⛔
    - Настроить Zabbix Server на подключение к удалённой БД ⛔
### Prometheus
- Собирает: временные ряды метрик (time series) по pull-модели (скрейпит HTTP endpoint’ы экспортеров/приложений), хранит их и даёт язык запросов PromQL.
- В твоём проекте: это метрики k8s/нод/приложения (например /actuator/prometheus).
### Grafana
- Не является “сборщиком метрик”.
- Роль: подключается к источникам данных (Prometheus, OpenSearch/Elasticsearch, Loki и др.), визуализирует (дашборды/панели) и может делать алерты на основе данных из источников.
### General:
- Логи: Fluent Bit / Fluentd → OpenSearch → (просмотр) OpenSearch Dashboards
- Метрики: Zabbix Agent → Zabbix Server → (UI) Zabbix Frontend; и/или Prometheus → (UI) Grafana
- Визуализация: Grafana и OpenSearch Dashboards (оба сами ничего не “собирают”)
- “best practice разделения”: Zabbix = хосты/VM, Prometheus = Kubernetes/приложения.