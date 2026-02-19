### Jenkins CLI
Примеры:
Информация справочная для  Jenkins API

http://<jenkins host>/api/

Получить информацию в формате json

http://<jenkins host>/api/json?pretty=true

Сделать фильтры по name, color, url

http://<host>/api/json?pretty=true&tree=jobs[name,color, url]

Получить информацию о конкретной сборке

http://<host>/job/<JOB_NAME>/api/json?pretty=true

### Jenkins + VScode
**Плагины**:
    - Jenkins Runner 
    - Groovy Lint, Format and Fix
    - Jenkins Pipeline Linter Connector
    - Jenkins Jack
### План работ
- Написать ансибл роль для деплоя JSB в minikube ✅
- Определиться, откуда будет дергаться пайплайн и приклад (предполагаю, что из одного репозитория) ✅
- Обновить приклад в репозитории ✅
- Написать Jenkins pipeline со структурой:
    - Настройка пользователей в кластере ✅
    - Установка и настройка постгрес ✅
    - Создание и настройка БД ✅
    - Настройка и запуск pg_dump скрипта по крону ✅
    - Контейнеризация и запуск пода с приложением ✅
    - Креды в Jenkins credentionals✅
    - Понять, как и когда передавать vault pass
    - Понять, как и когда передавать инвентарь
    - Понять, как и с какими тегами дергать плейбукиo 

2) Создай креды
Manage Jenkins → Credentials → (global) → Add Credentials

Kind: SSH Username with private key
Username: ansible
Private Key: Enter directly → вставь приватный ключ от ~/.ssh/jenkins-test-stage/id_ed25519
ID: например ssh-ansible-host
Description: любое
### Старт и работа с Jenkins
```bash
ssh-keyscan -H 192.168.56.1 >> ~/.ssh/jenkins-test-stage/known_hosts

docker compose up -d
docker ps
docker compose ps

## Запуск локального агента Jenkins:
## Необходимо создать агента в UI, получить ссылки на jar и креды. Креду добавить в директорию jenkins-agent, оттуда же запускать команды.
## Файл с кредами расширения .env, креда в формате export JENKINS_SECRET=ваш_секрет
mkdir -p "$HOME/jenkins-agent/work"
cd $HOME/jenkins-agent
curl -sO http://127.0.0.1:8080/jnlpJars/agent.jar
# or:
curl -fL --retry 3 -o "$HOME/jenkins-agent/agent.jar" http://localhost:8080/jnlpJars/agent.jar
source .jenkins_node_token.env && java -jar agent.jar  -url http://localhost:8080/  -secret "$JENKINS_SECRET"  -name "host-agent"  -webSocket  -workDir "$HOME/jenkins-agent/work"
```
### Настройка Jenkins
1) Зафиксировать версию ansible (для чего?)
- sudo python3 -m venv /opt/ansible_2.19.3
- sudo /opt/ansible_2.19.3/bin/pip install ansible-core==2.19.3 # Внутри созданной директории
- В Jenkins/tools/ansible:
    - Указываем путь к бинарнику (у меня это /opt/ansible_2.19.3/bin) и тег для пайплайна
Controller (Jenkins в Docker): executors = 0 (best practice: controller не выполняет jobs).

Host-agent (macOS): один или несколько executors, именно он исполняет stage’и.

На собесе: “controller only schedules, agents run workloads”.

Храни vault password как Secret file credential в Jenkins.

Прокидывай через withCredentials.

Минимальный enterprise-пайп для Ansible:

    syntax-check

    check mode + diff (если применимо)

    deploy

    post проверки (curl health / systemctl status / smoke tests)

“Vault password хранится как Secret file в Jenkins Credentials. В pipeline используется withCredentials, который временно создаёт файл и удаляет его после выполнения. Это исключает хранение секретов в репозитории и минимизирует их жизненный цикл.”

лучше чтобы пайплайн вызывал плейбуки предсказуемо и параметризовано:

    параметр ENV=dev|stage|prod

    параметр TARGET=postgres|nginx|k8s|all

    маппинг на конкретный inventory + playbook.