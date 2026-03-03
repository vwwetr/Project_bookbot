# О проекте
Инфраструктурная реализация микросервисного приложения (Telegram learning bot) на JavaSpringBoot.

## Стэк:
- Application: Java Spring Boot;
- DB: PostgreSQL (psql, pg_dump / pg_restore);
- Локальные тесты: Bare metal + Vagrant + Virtual Box;
- Networks: Nginx (внешний бастион-хост) + Nginx (внутренний прокси для сервисов);
- IaC: Ansible, Kubernetes;
- CI/CD: Jenkins, Maven, Docker, GitHub;
- Logging: FluentBit, OpenSearch;
- Monitoring: Zabbix, Prometheus, Grafana;

## Работа с проектом
1) Выполни Bootstrap с настройкой bare metal, для дальнейшего деплоя архитектуры: `/Documentation/Bootstrap_user_guide.md`.
2) Установи и настрой Jenkins: `/Documentation/Jenkins_readme.md`;
- После выполнения инструкций выше можно взаимодействовать с приложением и его архитектурой.
- Документацию по работе с сервисами - смотри в разделе "Документация".

## Документация:
- Логическая схема проекта: `/Documentation/project_scheme.png` (или `project_scheme.drawio`);
- Для работы с ChatGPT используй готовые шаблоны PROMPT-запросов: `/Documentation/PROMPTS (GPT)`;
- Project git flow: `/Documentation/Project_gitflow.md`;
- Cluster Ops: `/Documentation/Cluster_Ops.md`;
- Application: `/Documentation/Application_readme.md`;
- Database: `/Documentation/Database_readme.md`;
- Ansible: `/Documentation/Ansible_readme.md`;
- Jenkins: `/Documentation/Jenkins_readme.md`;
    - Список плагинов для работы с Jenkins: `/Documentation/Jenkins_plugins.md`;
    - Настройка host-agent: `/Documentation/Jenkins_host-agent_settings.png`;
- Скрипт для сброса состояния виртуальных машин: `vagrant_destroy.sh`;