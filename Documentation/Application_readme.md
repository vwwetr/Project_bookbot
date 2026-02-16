
## 📂 Проектная структура

```sh
learningbot/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/learningbot/
    │   │   ├── LearningBotApplication.java
    │   │   ├── config/TelegramBotConfig.java
    │   │   ├── controller/HealthController.java
    │   │   ├── domain/Resource.java
    │   │   ├── dto/{ResourceRequestDto.java, ResourceResponseDto.java}
    │   │   ├── repository/ResourceRepository.java
    │   │   ├── service/ResourceService.java
    │   │   └── telegram/LearningBot.java
    │   └── resources/application.yml
    └── test/java/com/learningbot/LearningBotTests.java
```

## 🧩 Среда и инструменты

* **Java:** 25 (Temurin / OpenJDK ARM64)
* **Spring Boot:** 3.5.7 (совместима с JDK 25)
* **Maven:** ≥3.9
* **PostgreSQL:** 16 

## Building:

```sh
brew install maven 
mvn clean package -DskipTests
java -jar target/learningbot-1.0.0.jar
```

## Переменные окружения:
```bash
nano .env.local 

export SPRING_PROFILES_ACTIVE=local
export TELEGRAM_BOT_USERNAME=
export TELEGRAM_BOT_TOKEN=
export SPRING_DATASOURCE_URL=
export SPRING_DATASOURCE_USERNAME=
export SPRING_DATASOURCE_PASSWORD=

chmod 600 .env.local 
```
## Запуск:
```bash
source .env.local && mvn -DskipTests spring-boot:run
```

## Логика взаимодейтсвия с базой данных
    ✅ Бот должен запращивать у пользователя: "Введите название ресурса:"
    ✅ Бот должен запрашивать у пользователя: "Введите автора в формате "Фамилия И.О. или введите "Без автора":"
    ✅ Бот должен запрашивать у пользователя: "Введите раздел, к которому нужно отнести ресурс (IT, Health, Finance, Lifestyle, Network, Spiritual):"
    ✅ Бот должен запрашивать у пользователя: "Введите формат ресурса (Book, Article, Video, Audio):"
    ✅ Бот должен запрашивать у пользователя: "Введите доступное вам время на изучение, в мин. (15, 30, 60, 90, 120):"
    ✅ Бот должен запрашивать у пользователя: "Введите ссылку на ресурс или введите "Нет URL":"
    ✅ При добавлении уже имеющегося в БД источника, боту необходимо выводить "Этот ресурс уже есть в базе!"
        ✅ Изначально, по логике приложения, дубли отсекались только при совпадении title/author/section/format. В последствии, в логику приложения была внесена корректирвока, и дубли стали отсекаться сразу по названию.
    ✅ При запросе ресурса по времени изучения, в случае отсутсвия любых ресурсов, боту необходимо выводить "Ресусрсов по этому времени еще нет в базе!"
        ✅ В последтсвии, при запросе исчточника, реализован подход "Сначала уточняется раздел источника, затем свободное время);
    🔘 В приложении необходимо будет ограничить добавление ресурса при его наличии в БД:
        ✅ Используй метод нормализации в сервисном слое (Service Layer), чтобы централизованно проверять наличие ресурса перед сохранением.
    🔘 Возможно, вместо "Введите" нужно указывать "Выберите" и давать пользователю выбрать самому посредством интерфейса телеграм.

## Нормализация и диакритика
В логике бота / Spring Boot:
    - при вставке книги поле title сохраняется как есть;
    - нормализация выполняется в приложении: lower + trim + схлопывание пробелов + удаление диакритики;
    - при проверке «есть ли уже такая книга» используется нормализованное значение;
    - это гарантирует, что строки, которые отличаются только регистром, пробелами и диакритикой, считаются дублями.
Пример:
    - "  Jósé   Müller " -> "jose muller"

### Предполагаемые доработки:
- Зафиксируй JDK 25 в сборке ✅
- Maven: maven-compiler-plugin → release=25 (или toolchains), чтобы не было “собралось на одном JDK, упало на другом”. ✅
- Используй JRE-образ для рантайма (а не JDK)
- Рекомендация по умолчанию: ```eclipse-temurin:25-jre-jammy``` (glibc-база, меньше сюрпризов в эксплуатации). ✅
- Сделай multi-stage Docker build:
    - Build stage на eclipse-temurin:25-jdk-..., runtime stage на eclipse-temurin:25-jre-....
    - Закрепи версию образа и/или digest
- В проде не полагайся на “плавающие” теги без контроля обновлений.
- Прогони smoke/integration тесты именно на Java 25
- Риски чаще будут не в Spring Boot, а в сторонних зависимостях/агентах/нативных либах. (Это типовой вопрос и на практике всплывает именно здесь.)

## Журнал эксплуатации:
1. К вариавнтам URL ресурса нужно в приложение доабить: Яндекс.Книги, ICloud, AppleMusic, Ibooks, Ресурс отсутсвует, Другое (при выборе этого варианта запросить у пользователя "Введите свой вариант:") и все это предлагать в вариантах при добавлении источника; 
    ✅ Исправлено;
2. Необходимо предоставлять ресурс сначала по разделу, к которому он относится, а уже после этого - по свободному времени на изучение;
    ✅ Исправлено;
3. Возможно, есть смысл добавить в базу данных возможность фиксировать главу/страницу, на котором было закончено изучение (возможно, с указанием времени последнего обращения к источнику)

## Контейнеризация приложения:
```sh
mvn -DskipTests package # Из директории /learningbot
brew install minikube
brew install kubectl
minikube start --driver=docker
docker build --no-cache -t learningbot:6dcebd2 . # 6dcebd2 - хэш последнего удачного коммита
kubectl create namespace learningbot --dry-run=client -o yaml | kubectl apply -f - # Гарантирует наличие namespace learningbot без прерывания скрипта при повторном запуске
kubectl -n learningbot create serviceaccount learningbot-sa
kubectl create secret generic learningbot-secret \
  --from-env-file=.env.local \
  -n learningbot \
  --dry-run=client -o yaml | kubectl apply -f -
minikube image load --overwrite=true learningbot:6dcebd2 # Возможно, при пересборке образа имеет смысл добавлять теги --pull или --overwrite
kubectl apply -f ./k8s/learningbot-deploy.yaml
kubectl apply -f ./k8s/learningbot-service.yaml
kubectl get ns learningbot
kubectl -n learningbot get pods
kubectl -n learningbot get sa learningbot-sa
```
## Диагностика кластера
### Диагностика и работа с minikube
```bash
Вообще есть вот тут: https://kubernetes.io/ru/docs/reference/kubectl/cheatsheet/
minikube status
minikube image ls
# Если вдруг надо вручную удалить образ из куба:
minikube ssh -- docker ps | rg learningbot
minikube ssh -- docker rm -f 4ece1c57c975
minikube status
minikube start
#---
kubectl cluster-info
kubectl config current-context
kubectl get nodes
kubectl get pods -A # Показать поды во всех неймспейсах
kubectl get pods -n learningbot
kubectl -n learningbot get deploy,po,svc
kubectl -n learningbot rollout status deploy/learningbot
kubectl -n learningbot logs deploy/learningbot --tail=200 #затем в другом окне:
    curl -s localhost:8080/actuator/health
kubectl -n learningbot logs learningbot-5695bd559-sng6p
# Или через HTTP (внутри кластера)
#или так:
kubectl get configmap learningbot-config -n learningbot
kubectl get secret learningbot-secret -n learningbot
kubectl describe configmap learningbot-config -n learningbot
kubectl describe secret learningbot-secret -n learningbot
kubectl get configmap learningbot-config -n learningbot -o yaml
kubectl delete configmap learningbot-config -n learningbot
minikube delete # Удалить кластер
rm -rf ~/.minikube/cache/preloaded-tarball
kubectl scale deploy/learningbot --replicas=0 -n learningbot # Останавливает деплоймент learningbot в namespace learningbot, выставляя 0 реплик (поды не будут пересоздаваться)
```
### Ноды, namespacr, ресурсы
```sh
kubectl get nodes -o wide
kubectl describe node <node>
kubectl get pods -A -o wide
kubectl get svc -A
kubectl get endpoints -A
kubectl get namespaces
kubectl delete pod learningbot-6857c8b8cd-x2zjv -n learningbot
```
### События и описания объектов
```sh
kubectl get events -A --sort-by=.metadata.creationTimestamp
kubectl describe pod <pod> -n <ns>
kubectl describe deploy <deploy> -n <ns>
kubectl describe svc <svc> -n <ns>
```
### Логи приложений
```sh
minikube logs
minikube logs --problems
kubectl -n <ns> logs <pod>
kubectl -n <ns> logs -f <pod>
kubectl -n <ns> logs --previous <pod>
kubectl -n <ns> logs deploy/<deploy>
```
### Системные компоненты
```sh
kubectl -n kube-system get pods
kubectl -n kube-system logs deploy/coredns
kubectl -n kube-system logs -l k8s-app=kube-proxy
kubectl -n kube-system logs -l component=kube-apiserver
kubectl -n kube-system logs -l component=kube-controller-manager
kubectl -n kube-system logs -l component=kube-scheduler
```
### Kubelet и container runtime на ноде
```sh
minikube ssh -- "sudo journalctl -u kubelet -xe"
minikube ssh -- "sudo journalctl -u containerd -xe"
```
### Проверка API health
```sh
kubectl get --raw='/readyz?verbose'
kubectl get --raw='/livez?verbose'
```
### Ресурсы (если установлен metrics-server)
```sh
kubectl top nodes
kubectl top pods -A
```
## Работа с докером
```bash
docker ps
docker rm
docker rmi
docker image ls
docker build --no-cache -t learningbot:tag .
```
## Траблисы и их решение
1. Не обновляется image в minikube при `minikube image load learningbot:latest`
  - Ты собираешь learningbot:latest снова и снова. Для Kubernetes тег — не гарантия обновления, это просто имя.
  - Если imagePullPolicy не Always, kubelet не будет пытаться “обновить” latest, если образ уже есть локально. Он просто использует то, что уже лежит на ноде.
  - minikube image load должен доставить новый образ в runtime Minikube, но на практике:
      - overwrite поведение зависит от флагов/версии, и есть известные кейсы, когда “перезагрузка” образа с тем же тегом ведёт себя не так, как ожидают, особенно если образ “в употреблении” (или Pod ещё не успел завершиться).
  - **Решение:** `minikube image load --daemon learningbot:latest --overwrite && kubectl -n learningbot rollout restart deployment/<deployment-name>`
2. Pod пересоздаётся и падает с `ImagePullBackOff`/`InvalidImageName`
  - Под удаляешь, а он возвращается — значит им управляет контроллер (ReplicaSet/Deployment), а не “кэш”.
  - `InvalidImageName` часто возникает из‑за плейсхолдера вроде `learningbot:<sha>` или тега с недопустимыми символами.
  - `ImagePullBackOff` появляется, когда образ не найден/недоступен в registry или имя указано неверно.
  - **Решение:** найти владельца RS, исправить `image` на валидный тег/диджест и применить манифест.
    - Проверка владельца: `kubectl get pod <pod> -n learningbot -o=jsonpath='{.metadata.ownerReferences[0].kind}{" "}{.metadata.ownerReferences[0].name}{"\n"}'`
    - Остановка пересоздания при необходимости: `kubectl scale deploy/<name> --replicas=0 -n learningbot`
    - Обновление образа: `kubectl set image deploy/<name> <container>=learningbot:<tag> -n learningbot && kubectl rollout status deploy/<name> -n learningbot`
