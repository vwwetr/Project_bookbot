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
### План работ в проекте
- Написать ансибл роль для деплоя JSB в minikube
- Определиться, откуда будет дергаться пайплайн и приклад (предполагаю, что из одного репозитория)
- Написать Jenkins pipeline со структурой:
    - Настройка пользователей
    - Установка и настройки постгрес
    - Создание и настройка БД
    - Настройка и запуск pg_dump скрипта по крону

### Старт и работа с Jenkins
```bash
docker compose up -d
docker ps
docker compose ps
```
