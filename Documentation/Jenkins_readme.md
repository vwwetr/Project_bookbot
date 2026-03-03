
## Настройка VScode для работы с Jenkins
**Плагины**:
    - Jenkins Runner 
    - Groovy Lint, Format and Fix
    - Jenkins Pipeline Linter Connector
    - Jenkins Jack

## Запуск контейнера с Jenkins
- Из директории `/Jenkins`:
```bash
docker compose up -d
docker ps
docker compose ps
```
- При запуске контейнера, в CLI отобразится Jenkins токен для настройки пользователя. Его нужно вводить в UI Jenkins единоразово по запросу, далее, вход по настроенному логину и паролю;

## Настройка Jenkins клиента
- Для корректной работы Jenkins и запуска пайплайна необходимо установить набор плагинов. Список необходимых плагинов смотри в разделе `/Documentation/Jenkins_plugins.md`

### Настройка Jenkins host-agent
- Необходимо создать агента в UI, получить ссылки на jar и запуск агента.
    - Manage Jenkins → Nodes → + New node → Введи "Node name" и выбери "Permanent type"
    - Настройки host-agent смотри тут:  `/Documentation/Jenkins_host-agent_settings.png`

### Запуск Jenkins host-agent

- Создай рабочую директорию для Jenkins host-agent:

```sh
mkdir -p "$HOME/jenkins-agent/work"
```
- Запусти host-agent: 
> Примечение: команды ниже даны для примера. Рабочие команды для своего Jenkins агента возьми из Jenkins UI

```sh
curl -sO http://127.0.0.1:8080/jnlpJars/agent.jar
java -jar agent.jar -url http://127.0.0.1:8080/ -secret какойтосекретныйсекрет -name "host-agent" -webSocket -workDir "/Users/vwwetr/jenkins-agent"
```

## Добавь все секреты (SSH, passwords, token, etc.) в Jenkins credentionals
### Пример создания креды для ssh privat key:**

Manage Jenkins → Credentials → (global) → Add Credentials

Kind: SSH Username with private key
Username: ansible
Private Key: Enter directly → вставь приватный ключ от ~/.ssh/jenkins-test-stage/id_ed25519
ID: например ssh-ansible-host
Description: любое

**Добавить комментарий, для какого секрета лучше подходит конкретный тип креды**
**Добавить комментарий, какие креды нужны для работы Jenkins**