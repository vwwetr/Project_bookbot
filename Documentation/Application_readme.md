
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
brew install minikube
# Установить и открытть docker
# mvn spring-boot:build-image  -Dspring-boot.build-image.imageName=learningbot:0.6.0
# docker images | grep learningbot
# minikube image load learningbot:0.6.0 
minikube start
brew install helm
helm repo add hashicorp https://helm.releases.hashicorp.com # Добавляет репозиторий Helm (нужен VPN)
helm repo update # Обновляет индекс репозиториев Helm
kubectl create namespace vault # Создает namespace vault
helm install vault hashicorp/vault -n vault # Устанавливает Vault в namespace vault
kubectl -n vault get pods
kubectl -n vault exec -it vault-0 -- vault operator init -key-shares=1 -key-threshold=1 # Инициализация хранилища
# Saving Unseal key and Initial Root Token
kubectl -n vault exec -it vault-0 -- vault operator unseal <UNSEAL_KEY> # "Распечатка" хранилища
kubectl -n vault exec -it vault-0 -- vault login <ROOT_TOKEN>
kubectl -n vault exec -it vault-0 -- vault secrets enable -path=secret kv-v2 # включим KV v2 (если ещё не включён)
kubectl -n vault exec -it vault-0 -- vault kv get secret/learningbot
# Включаем k8s auth vault и настраиваем роль для приложения
kubectl -n vault exec -it vault-0 -- vault auth enable kubernetes
# Создадим сервис аккаунт для приложения
kubectl create namespace learningbot
kubectl -n learningbot create serviceaccount learningbot-sa
# Нужно дать Vault доступ к Kubernetes API (для проверки ServiceAccount токенов).
kubectl create clusterrolebinding vault-tokenreview-binding \
  --clusterrole=system:auth-delegator \
  --serviceaccount=vault:vault
# Получи token reviewer JWT от serviceaccount vault
TOKEN=$(kubectl -n vault create token vault) && echo $K8S_CA
K8S_HOST=$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}') && echo $K8S_HOST
K8S_CA=$(kubectl config view --raw --minify --flatten -o jsonpath='{.clusters[0].cluster.certificate-authority-data}' | base64 -d) && echo $K8S_CA
# Добавляем значения (какие?)
kubectl -n vault exec -it vault-0 -- sh -c 'vault write auth/kubernetes/config \
  token_reviewer_jwt="$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)" \
  kubernetes_host="'"$K8S_HOST"'" \
  kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt'
# Создать policy чтобы бот читал secret/learningbot.
kubectl -n vault exec -it vault-0 -- sh -c 'cat > /tmp/learningbot-policy.hcl <<EOF
path "secret/data/application" {
  capabilities = ["read"]
}
path "secret/data/learningbot" {
  capabilities = ["read"]
}
EOF
vault policy write learningbot /tmp/learningbot-policy.hcl'
# Создать role чтобы бот читал secret/learningbot.
kubectl -n vault exec -it vault-0 -- vault write auth/kubernetes/role/learningbot \
  bound_service_account_names=learningbot-sa \
  bound_service_account_namespaces=learningbot \
  policies=learningbot \
  ttl=1h
# Настройка Yandex KMS (секреты, чтобы открыть секреты, чтобы задеплоить приложение)
# Настрой платежный метод в личном кабинете, иначе ничего не заработает!!!
https://yandex.cloud/ru/marketplace/products/yc/vault-yckms-k8s?utm_source=chatgpt.com&utm_referrer=https%3A%2F%2Fchatgpt.com%2F # Беслатное решение hashivault+yandex KMS
https://yandex.cloud/ru/docs/cli/quickstart#macos_1 # Установка клиента
yc config list 
yc iam service-account create --name learningbot-vault --description "Vault KMS auto-unseal" # создать сервисный аккаунт для Vault (будет ходить в KMS)
yc resource-manager cloud add-access-binding b1ggeev2p3mh1dnjablu --role kms.admin --subject userAccount:ajepa6novnepn1a6bkce 
yc kms symmetric-key create --name learningbot-kms-key --default-algorithm aes-256 # создаём симметричный ключ, которым Vault будет шифровать данные для auto‑unseal.
yc resource-manager folder list-access-bindings <Folder_ID> 
yc resource-manager folder add-access-binding <Folder_ID>  --role kms.admin --subject userAccount:<user_ID> 
yc kms symmetric-key list --folder-id <Folder_ID>
export KMS_KEY_ID=<key_id>
yc iam service-account list # проверяем наличие аккаунта
export VAULT_SA_ID=<sa_id>
yc kms symmetric-key add-access-binding --id "$KMS_KEY_ID" --role kms.keys.encrypterDecrypter --subject serviceAccount:"$VAULT_SA_ID" # выдаём SA минимальную роль на конкретный ключ, чтобы Vault мог шифровать/расшифровывать.
yc iam key create --service-account-id "$VAULT_SA_ID" --output /tmp/vault-sa-key.json #  ключ сервисного аккаунта (JSON) для доступа Vault к KMS (Что делаем: генерируем key‑file, который Vault будет использовать для обращения к Yandex KMS)
kubectl -n vault get pods
kubectl -n vault create secret generic vault-sa-key \
  --from-file=sa-key.json=/tmp/vault-sa-key.json
kubectl -n vault get secret vault-sa-key
kubectl get ns | rg vault
helm -n <NAMESPACE> list
# подготовить Helm values для Vault с Yandex KMS (Что делаем: задаём seal "yandexcloudkms" и монтируем JSON‑ключ из секрета vault-sa-key):
cat > /tmp/vault-values.yaml <<EOF
server:
  extraConfig: |
    ui = true
    seal "yandexcloudkms" {
      kms_key_id = "$KMS_KEY_ID"
      service_account_key_file = "/etc/vault/yc/sa-key.json"
    }
  extraVolumes:
    - type: secret
      name: vault-sa-key
      path: /etc/vault/yc
EOF
# Обновить конфиг Helm:
kubectl delete mutatingwebhookconfiguration vault-agent-injector-cfg
helm upgrade vault hashicorp/vault -n vault -f /tmp/vault-values.yaml
# перезапустить Vault, чтобы он подхватил новый seal (Что делаем: перезапускаем vault-0 и проверяем статус.):
kubectl -n vault delete pod vault-0
```
## Диагностика кластера
### Диагностика Hashicorp Vault
```bash
# Login:
kubectl -n vault exec -it vault-0 -- sh
kubectl -n vault exec -it vault-0 -- vault status
# или так:
export VAULT_ADDR=http://127.0.0.1:8200
kubectl -n vault port-forward svc/vault 8200:8200
# Диагностика:
vault status
vault login <ROOT_TOKEN>
vault secrets list
vault secrets list -detailed -format=json
vault kv list secret/
vault kv list -mount=secret
vault kv get secret/learningbot
kubectl -n vault get pods
kubectl get cm -n vault
```
### Диагностика и работат с minikube
```bash
Вообще есть вот тут: https://kubernetes.io/ru/docs/reference/kubectl/cheatsheet/
minikube status
minikube image ls
# Если вдруг надо вручную удалить образ из куба:
minikube ssh -- docker ps | rg learningbot
minikube ssh -- docker rm -f 8d14c218f3bc
minikube image rm docker.io/library/learningbot:latest 
minikube status
minikube start
#---
kubectl cluster-info
kubectl config current-context
kubectl get nodes
kubectl get pods -A
kubectl get pods -n learningbot
kubectl -n learningbot get deploy,po,svc
kubectl -n learningbot rollout status deploy/learningbot
kubectl -n learningbot logs deploy/learningbot --tail=200 #затем в другом окне:
    curl -s localhost:8080/actuator/health
kubectl -n learningbot logs learningbot-5695bd559-sng6p
# Или через HTTP (внутри кластера)
kubectl -n vault exec -it vault-0 -- sh -c \
'curl -s -H "X-Vault-Token: <ROOT_TOKEN>" http://127.0.0.1:8200/v1/secret/data/learningbot | jq'
kubectl -n vault exec -it vault-0 -- vault auth list
kubectl -n vault exec -it vault-0 -- sh
#или так:
kubectl -n vault port-forward svc/vault 8200:8200
kubectl get configmap learningbot-config -n learningbot
kubectl get secret learningbot-secret -n learningbot
kubectl describe configmap learningbot-config -n learningbot
kubectl describe secret learningbot-secret -n learningbot
kubectl get configmap learningbot-config -n learningbot -o yaml
kubectl delete configmap learningbot-config -n learningbot
```
### Ноды, namespacr, ресурсы
```sh
kubectl get nodes -o wide
kubectl describe node <node>
kubectl get pods -A -o wide
kubectl get svc -A
kubectl get endpoints -A
kubectl get namespaces
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
## Диагностика перед деплоем
```bash
kubectl get ns learningbot
kubectl -n learningbot get sa learningbot-sa
kubectl -n vault port-forward svc/vault 8200:8200
vault login <root_token>
kubectl -n vault exec -it vault-0 -- vault read auth/kubernetes/role/learningbot
vault kv get secret/learningbot
vault secrets list -detailed | grep secret # должен быть kv2
```
## Деплой
```bash
kubectl create secret generic learningbot-secret \
  --from-env-file=.env.local \
  -n learningbot \
  --dry-run=client -o yaml | kubectl apply -f -
kubectl delete pod --all -n learningbot
mvn -DskipTests package
docker build -t learningbot:latest .
minikube image load learningbot:latest # Возможно, при пересборке образа имеет смысл добавлять теги --pull или --overwrite
kubectl create namespace learningbot
kubectl apply -f ./k8s/learningbot-deploy.yaml
kubectl apply -f ./k8s/learningbot-service.yaml
kubectl -n learningbot get pods  
```

## Траблисы
1. Не обновляется image в minikube при `minikube image load learningbot:latest`
  - Ты собираешь learningbot:latest снова и снова. Для Kubernetes тег — не гарантия обновления, это просто имя.
  - Если imagePullPolicy не Always, kubelet не будет пытаться “обновить” latest, если образ уже есть локально. Он просто использует то, что уже лежит на ноде.
  - minikube image load должен доставить новый образ в runtime Minikube, но на практике:
      - overwrite поведение зависит от флагов/версии, и есть известные кейсы, когда “перезагрузка” образа с тем же тегом ведёт себя не так, как ожидают, особенно если образ “в употреблении” (или Pod ещё не успел завершиться).
  - **Решение:** `minikube image load --daemon learningbot:latest --overwrite && kubectl -n learningbot rollout restart deployment/<deployment-name>`