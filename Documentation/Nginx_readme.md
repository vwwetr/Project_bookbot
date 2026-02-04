### Nginx config files
- /etc/nginx/nginx.conf
- /etc/nginx/conf.d
- /etc/yum.repos.d/nginx.repo
- /var/log/nginx/access.log
- /var/log/nginx/error.log
- /run/nginx.pid
- /usr/share/nginx/html
### Настроить:
- Пользаки
    - Кажется, что пользователь должен быть непревелигированный
- Сеть
  - 192.168.56.1 - 80 порт отдаёт редирект на 443, а доступ по 443 разрешён только с 192.168.56.1.
- Firewall (Ограничь доступ по firewall)
  - Открой 443/tcp (и 80/tcp только если делаешь редирект).
  - Если хочешь жёстче: разреши доступ на 443 только от 192.168.56.1 (или всей 192.168.56.0/24) — это уже конкретная реализация через firewalld rich rules.
- Выпустить сирты
- Определи единственный внешний endpoint: nginx_ui:443.
- Закрой прямой доступ к:
    Grafana service (не публиковать наружу, только internal)
    OpenSearch Dashboards (только из подсети/через nginx_ui)
    Zabbix Frontend (только через nginx_ui)
- На nginx_ui сделай маршруты по путям (/grafana, /dashboards, /zabbix) и включи:
    единый TLS termination,
    basic auth / allowlist IP (минимум для стенда),
    логирование access/error.
    (Опционально) Если хочешь “единое окно” — подключи в Grafana OpenSearch и Zabbix плагины.
# Контуры работы nginx:

1) `nginx_ui` — **внешний entrypoint** (north–south трафик: пользователь → кластер), публикуется наружу.  
2) `nginx_tech` — **внутренний gateway** для админских/технических сервисов (только trusted-сеть/VPN), наружу не публикуется.

---

# Контур 1: `nginx_ui` (External / DMZ entrypoint)

## Назначение
Единая внешняя точка входа в “веб” кластер (UI для мониторинга/логов и т.п.).

## Кто к нему ходит
- Браузер/клиент (ты с ноутбука) по **HTTPS 443**.

## Куда он ходит (внутренние upstream’ы)
`nginx_ui` по `location + proxy_pass` проксирует на web-UI сервисы, например:
- `/zabbix` → `zabbix_frontend:80/443`
- `/dashboards` → `opensearch_dashboards:5601`
- (опционально) `/grafana` → `grafana:3000`

## Порты и firewall (минимум)
- **Inbound:** `nginx_ui` ← `443/tcp` (извне)
- **Outbound:** `nginx_ui` → upstream’ы по их портам (80/443/5601/3000 и т.д.)

---

# Контур 2: `nginx_tech` (Internal tools gateway)

## Назначение
Единая внутренняя точка входа к “техническим” системам: Jenkins, Registry, Vault (и прочие internal UI/API).

## Кто к нему ходит
- Только trusted-клиенты: твой ноутбук из приватной сети/VPN  
  **или** `nginx_ui` (если делаешь каскад 2-hop).

## Куда он ходит (upstream’ы tools)
- `/jenkins` → `jenkins:8080`
- `/registry` → `registry:5000`
- `/vault` → `vault:8200`

## Порты и firewall (минимум)
- **Inbound:** `nginx_tech` ← `443/tcp` только от trusted-сети (или только от `nginx_ui` в варианте каскада)
- **Outbound:** `nginx_tech` → `jenkins:8080`, `registry:5000`, `vault:8200`

---

# Как контуры взаимодействуют между собой

## Вариант A (рекомендую): два независимых входа по смыслу
- `nginx_ui` — “общие” UI: Zabbix, OpenSearch Dashboards, Grafana и т.п.
- `nginx_tech` — “инженерные” UI: Jenkins/Registry/Vault, доступен только тебе/через VPN.

**Плюсы:** проще, ясные границы, легче объяснить на собеседовании.  
**Минус:** два адреса/хоста (это нормально для админского контура).

## Вариант B: каскад (2-hop)
- Браузер → `nginx_ui` → `nginx_tech` → tool-service

**Плюсы:** один внешний вход.  
**Минусы:** выше связность, сложнее дебажить, чаще проблемы с путями/заголовками/redirect.

---

# Сводная карта потоков

## North–South (внешний доступ)
- `Browser` → `nginx_ui:443` → `{ zabbix_frontend, opensearch_dashboards, grafana, ... }`

## Admin/Tools (внутренний доступ)
- `Admin(laptop/VPN)` → `nginx_tech:443` → `{ jenkins, registry, vault }`

---

# Ссылки (1 дока + 1 статья)

- Документация: NGINX Reverse Proxy (`proxy_pass`)  
  https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_pass

- Статья (Habr): практические нюансы `proxy_pass` и заголовков `X-Forwarded-*`  
  https://habr.com/ru/articles/349232/
