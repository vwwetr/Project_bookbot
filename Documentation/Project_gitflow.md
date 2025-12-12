## Lightweight Git Workflow + Jira Integration description

- Этот проект использует упрощенный Git workflow: ветка разработки `develop`, и прод ветка `master`. Без фичей, хотфиксов и релизов, без тегов, но с комментариями в коммитах. 
- В обозначении версиий репозиториев используются базовые метрики семантического версионирования;
    - Версии присваиваются в следующем порядке:
        - `v.0.0.1`: HotFix, bugfix, patch, etc.;
        - `v.0.1.0`: Minor (for example, "build PostgreSQL database);
        - `v.1.0.0`: Major (for example: first project succes build)
- Удаленный GitHub репозиторий интегрирован с Jira (канбан проекта), которая автоматически закрывает задачи при merge Pull Request благодаря настроенной автоматизации.
    - Настройки автоматизации: см. `./Documentation/Develop commits automation with tasks.png` и `./Documentation/Develop commits automation with tasks.png`

### Git config settings
- ~/.gitconfig - общий конфиг для всех локальных репозиториев конкретного пользователя
``` bash
git config --global init.defaultBranch master       # Установить название ветки по умолчанию для новых репозиториев "master", вместо стандартной main
git config --global user.name "vwwetr"              # Добавляем имя пользователя
git config --global user.email "vwwetr@gmail.com"   # Добавляем почту (которую указали в GitHub!)
``` 
### Github and connection settings
- Создаем репозиторий;
- ВАЖНО: НЕ ставить галочки на:
    - “Initialize this repository with a README” (если остави, он создаст файл и сразу создаст ветку main.)
    - .gitignore (если что, потом запушим)
    - License
    - В таком состоянии первая запушенная ветка станет default.
- Настраиваем SSH:
    - Генерируем ключ: `ssh-keygen -t ed25519 -C "vwwetr@github"`
    - Запускаем ssh-agent (на Mac): `eval "$(ssh-agent -s)"` (Обычно запущен, но мало ли.)
    - Добавляем ключик в ссх-агента`ssh-add ~/.ssh/id_ed25519`
    - Копируем ключик из `cat ~/.ssh/id_ed25519.pub` и добавляем в GitHub аккаунт;
    - Проверяем соединение с репозиторием:`ssh -T git@github.com`

### Git init and connect to GitHub (given in exeption order):
``` bash
git init                                                        # Создаём репозиторий
git checkout -b master                                          # Явно создаем master
# .gitignore, README и т.п. — по желанию
git add --all                                                   # Добавить все изменения в stage
git commit -m "Initial commit"                                  # Создать коммит с комментарием
git remote add origin git@github.com:vwwetr/Project_bookbot.git # Привязываемся к удаленному репозиторию
git push -u origin master                                       # Отправить изменения master в удаленный репозиторий и создать связь с отслеживанием между локальным и удаленным репозиторием
git checkout -b develop master                                  # Создаем ветку от master для разработки
git push -u origin develop                                      # Отправить изменения develop в удаленный репозиторий и создать связь с отслеживанием между локальным и удаленным репозиторием
git branch -a                                                   # Отобразить лоакальные и удаленные ветки
git rev-parse develop                                           # Вывести хэш коммита, на который в данный момент указывает ветка develop.
``` 

### Typical work scenario in `develop` branch (before `master` release)
- Перейти в `develop`;
- Внести необходимые изменения (например, закрыли задачу в Jira с тегом PBB-1);
    
- Далее:
``` bash
git add --all
git commit -m "PBB-1 v.0.4.0 Builded PostgreSQL database"
git push
```

### Typical release scenario in `master` by pull request (for example, version will be `1.0.0`)
- Убедиться, что все задачи для релиза завершены и закоммичены в `develop`;
- Запушить актуальное состояние `develop`:
```bash
git checkout develop
git push
```
- На GitHub открыть Pull Request: base: master ← compare: develop;
- После ревью и проверки — выполнить merge PR в master;
- Пометить релиз тегом (например, v1.0.0) и обновить статус задач в Jira.
    - Порядок присвоения версий - см. `Lightweight Git Workflow + Jira Integration description` в шапке этого readme.
