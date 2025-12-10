# Get_started.md

## 1. Предварительные требования

На **локальной машине** (macOS / Linux) должны быть установлены:

- Vagrant
- Провайдер виртуальных машин (например, VirtualBox)
- Ansible

Далее:

```bash
git clone <URL_ТВОЕГО_РЕПОЗИТОРИЯ>
cd <ИМЯ_РЕПОЗИТОРИЯ>
```

---

## 2. Поднять виртуальные машины через Vagrant

Из корня репозитория:

```bash
vagrant up
```

После этого будут подняты виртуальные машины, доступные по SSH под стандартным пользователем `vagrant` (ключ и настройки создаёт Vagrant автоматически).

---

## 3. Сгенерировать SSH-ключ для пользователя `ansible`

На **control-host** (там, где запускается Ansible):

```bash
ssh-keygen -t ed25519 -f ~/.ssh/id_ed25519_ansible -C "ansible"
```

Будут созданы файлы:

- приватный ключ: `~/.ssh/id_ed25519_ansible`
- публичный ключ: `~/.ssh/id_ed25519_ansible.pub`

> Приватный ключ **нельзя** коммитить в репозиторий.

## 3.1 Настройка локального ssh config:

### ========== Ansible servers ==========
### Доступ к нодам стенда под пользователем ansible
Host 172.17.177.*
  User ansible
  IdentityFile /home/vwwetr/.ssh/ansible_id_ed25519
  #### для учебного стенда можно отключить проверку хост-ключей:
  StrictHostKeyChecking no
  UserKnownHostsFile /dev/null

---

## 4. Создать vault и добавить в него публичный ключ

- В директории `/Ansible/group_vars/all/` создай зашифрованный файл переменных vault.yml:

```bash
ansible-vault edit vault.yml - при запросе пароля дважды введи любой пароль (запомни его!)
```
- Добавь туда публичный ключ:
  - Вместо `ssh-ed25519 AAAA...` вставь строку целиком из файла `~/.ssh/id_ed25519_ansible.pub`. Далее:

```bash
Esc, wq!, Enter - выйти с сохранением
```
---

## 5. Один раз выполнить плейбук с тегом [users] (создание пользователя `ansible`)

Плейбук запустится под пользователем `vagrant` и:
- Создаст пользователя `ansible` на всех нодах;
- Настроит ему домашний каталог, shell, UID/GID;
- Добавит его публичный ключ в `authorized_keys`;
- Создаст `/etc/sudoers.d/99-ansible` с нужными правами.

Запуск (из директории /Ansible/):

```bash
ansible-playbook bookbot.yml -u vagrant --private-key=~/.vagrant.d/insecure_private_key --tags users --ask-vault-pass
```

Параметры:

- `-u vagrant` — подключение под пользователем `vagrant`;
- `--private-key=~/.vagrant.d/insecure_private_key` — стандартный Vagrant-ключ;
  - Он общий для всех нод, поднимаемых вагрантом, благодаря строчке `config.ssh.insert_key = false`
- `--tags users` - выполнить в плейбуке только джобу с тегом [users]
- `--ask-vault-pass` — запрос пароля для расшифровки `vault.yml`, чтобы прокинуть публичный ключ для пользователя Ansible, который хранится в vault.
