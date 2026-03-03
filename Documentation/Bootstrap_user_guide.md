## 1. Настрой bare metal:

Установи на локальную тачку (macOS / Linux):

- Vagrant 2.4.9;
- VirtualBox 7.2.4 (r170995);
- Ansible 2.19.3;
- Python 3.13.11;
- Jinja 3.1.6;
- pyyaml 6.0.3;
- Docker 4.47.0 (206054);
---

## 2. Подними виртуальные машины через Vagrant:

Из корня репозитория:

```bash
vagrant up
```

После этого будут подняты виртуальные машины, доступные по SSH под стандартным пользователем `vagrant` (ключ и настройки создаёт Vagrant автоматически).

---

### 3. Подними Jenkins в докере

### 4. Сбилди и подними Jenkins host-agent ноду

## 3. Сгенерируй SSH-ключ для пользователя `ansible`

На локальной машине:

```bash
ssh-keygen -t ed25519 -f ~/.ssh/id_ed25519_ansible -C "ansible"
```

Будут созданы файлы:

- приватный ключ: `~/.ssh/id_ed25519_ansible`
- публичный ключ: `~/.ssh/id_ed25519_ansible.pub`


## 3. Настрой локальный ssh config:

### ========== Ansible servers ==========
### Доступ к нодам стенда под пользователем ansible
Host 172.17.177.*
  User ansible
  IdentityFile /home/vwwetr/.ssh/ansible_id_ed25519
  #### для учебного стенда можно отключить проверку хост-ключей:
  StrictHostKeyChecking no
  UserKnownHostsFile /dev/null

---

**Вместо этого шага будет настройка jenkins сredentionals**
## 4. Создай ansible vault и добавь в него публичный ключ

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

**Вместо этого шага будет запуск пайплайна в Jenkins**
## 5. Один раз выполни плейбук с тегом [users] (создание пользователя `ansible`)

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

Далее:

```bash
git clone https://github.com/vwwetr/Project_bookbot
cd Project_bookbot
```