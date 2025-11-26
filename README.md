# Project_bookbot
Telegram learning JSB bot pet project.

- В самом конце расписать здесь структуру проекта и навигацию по ней.

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
- postgres (для SQL)
    - Postgres roles:
        - postgres (для SQL)
        - app_user (для JSB)
        - metric_user (Для Zabbix и Prometheus)
- app_user (для JSB)
- metric_user (Для Zabbix и Prometheus)

## Lightweight Git Workflow + Jira Integration

Этот проект использует простой Git workflow: одна основная ветка
(`main`) и короткие feature-ветки под задачи.\
Jira автоматически закрывает задачи при merge Pull Request благодаря
правилу:

## Pull request merged → Transition issue to Done**
## 1. Обновить основную ветку

Перейти в `main` и скачать последние изменения:

``` bash
git checkout main
git pull origin main
```

## 2. Создать короткую feature-ветку

Создать новую ветку под задачу (короткое имя без тикета Jira):

``` bash
git checkout -b feature/<name>
```

Примеры:

``` bash
git checkout -b feature/db
git checkout -b feature/ansible
git checkout -b feature/monitoring
```

## 3. Внести изменения

Работай над задачей, редактируй код, конфиги и т. д.

## 4. Сделать коммит с ключом задачи Jira

Каждый коммит должен содержать **issue key** (например `PBB-2`):

``` bash
git add .
git commit -m "PBB-2: создание роли для базы"
```

Это гарантирует связь коммита и PR с задачей в Jira.

## 5. Отправить ветку на GitHub

Первый push:

``` bash
git push -u origin feature/<name>
```

Например:

``` bash
git push -u origin feature/db
```

## 6. Создать Pull Request

На GitHub нажать:

**Compare & pull request**

Название PR должно содержать issue key:

    PBB-2: создание роли для базы

## 7. Выполнить merge PR

После проверки нажать:

**Merge pull request**

Изменения попадут в `main`.

## 8. Jira автоматически переведёт задачу в Done

Благодаря правилу:

    Pull request merged → Transition issue to Done

После merge:

-   задача перейдёт в статус Done,
-   PR и коммиты появятся в разделе Development,
-   никаких ручных действий не требуется.

## 9. Удалить ветку (рекомендуется)

На GitHub:

**Delete branch**

Или локально:

``` bash
git branch -d feature/<name>
```

## 10. Начать новую задачу

Повторить шаги 1--9.

