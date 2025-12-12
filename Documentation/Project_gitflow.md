## Lightweight Git Workflow + Jira Integration description

- Этот проект использует упрощенный Git workflow: ветка разработки `develop`, и прод ветка `master`. Без фичей, хотфиксов и релизов.
- Удаленный GitHub репозиторий интеграирован с Jira (канбан проекта), которая автоматически закрывает задачи при merge Pull Request благодаря настроенной автоматизации.

### Настройка локального Git
- ~/.gitconfig - общий конфиг для всех локальных репозиториев конкретного пользователя
``` bash
git config --list --show-origin
git config --list # Проверка используемых конфигураций
git config --global iniy.defaultBranch develop
git branch -a # Отобразить лоакальные и удаленные ветки
git flow init 
git push -u origin develop 
git push --force-with-lease # 
git branch --unset-upstream # Отвязаться от удаленной ветки
git branch --set-upstream-to=origin/master master # Привязать локальный master к удаленному
git rev-parse develop # Выведет хэш коммита, на который в данный момент указывает ветка develop.

git log master..develop --oneline # Если вывода нет вообще, значит в develop нет уникальных коммитов относительно master
    git log develop..master --oneline # Если и здесь пусто, значит ветки полностью совпадают по истории.

git branch -d backup_origin_main
git branch -d backup_origin_master
git branch -d backup_origin_develop

git checkout develop
git checkout -b feature/xyz
# работа, коммиты
git push -u origin feature/xyz
# потом PR / merge feature/xyz -> develop


git checkout master
git merge --no-ff develop
git tag -a v0.4.5 -m "Release v0.4.5"
git push origin master --tags
``` 

## Pull request merged → Transition issue to Done
### 1. Обновить основную ветку

Перейти в `main` и скачать последние изменения:

``` bash
git checkout main
git pull origin main
```

### 2. Создать короткую feature-ветку

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

Сначала на GitHub:

``` bash
git push origin --delete feature/db
```
Потом локально:

``` bash
git branch -d feature/<name>
```