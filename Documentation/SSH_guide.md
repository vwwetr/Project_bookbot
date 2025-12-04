### Генерация ключа:
```ssh-keygen -t ed25519 -C "ansible" -f ~/.ssh/ansible_id_ed25519```

**В ansible.cfg:**
```yml
ssh_args                        = -F /Users/vwwetr/.ssh/config
private_key_file                =/Users/vwwetr/.ssh/ansible_id_ed25519
```
### Времянка, чтобы ходить с хоста:
**В ~/.ssh/config (На местере, указываем все хосты, к которым подключаемся):**
```sh
Host *
AddKeysToAgent yes                # Автоматически добавлять ключи в агент
UseKeychain yes                   # macOS: хранить passphrase в системном keychain
ServerAliveInterval 60            # поддерживать сессию активной
ServerAliveCountMax 3
TCPKeepAlive yes
IdentitiesOnly yes                # использовать только явно указанные ключи
StrictHostKeyChecking accept-new  # автоматически доверять новым хостам
LogLevel VERBOSE                  # полезно при отладке
# Для каждой ноды кластера:
Host Database
    HostName 172.17.177.210
    User ansible
    IdentityFile ~/.ssh/ansible_ansible_id_ed25519
    PasswordAuthentication no
```
**Создаём пользователя с отдельной группой и bash как оболочкой:**
```sh
sudo useradd -m -s /bin/bash -U ansible
sudo passwd ansible
sudo chmod 700 /home/ansible/ 
```
**В sshd_config** #???
```sh
PasswordAuthentication no #???
PermitRootLogin no #???
```
**В /etc/sudoers.d/99-ansible:**
```sh
ansible ALL=(ALL) NOPASSWD: ALL #???
Defaults:ansible !requiretty #???
Defaults:ansible secure_path=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin #???
Defaults:ansible env_reset #???

# Не трогаем напрямую /etc/sudoers, а создаём отдельный файл, например:
/etc/sudoers.d/99-ansible.
# Внутри этого файла будет полный набор правил для пользователя ansible
# Файл должен быть с правами 0440 и валидироваться через visudo.

```

**Создаём папку для ключей и задаём права:**
```sh
sudo mkdir -p /home/ansible/.ssh
sudo touch /home/ansible/.ssh/authorized_keys
sudo chmod 700 /home/vwwetr/.ssh #
sudo chmod 600 /home/vwwetr/.ssh/authorized_keys #
sudo chown -R ansible:ansible /home/ansible/.ssh
```
**(Воркеры) Пушим публичный ключ на воркеры:**
```sh
ssh-copy-id -i id_rsa.pub vwwetr@172.17.176.211
```
**(Воркеры) Добавляем sudo без пароля:**
```sh
sudo usermod -aG sudo vwwetr
echo "vwwetr ALL=(ALL) NOPASSWD:ALL" | sudo tee /etc/sudoers.d/vwwetr > /dev/null
## Для CentOS:
sudo usermod -aG wheel vwwetr
echo "vwwetr ALL=(ALL) NOPASSWD:ALL" | sudo tee /etc/sudoers.d/vwwetr > /dev/null
sudo chmod 440 /etc/sudoers.d/vwwetr
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --reload
```
**Настраиваем права:**

**На ansible-master:**
```sh
sudo chmod 700 ~/keys
sudo chmod 600 ~/keys/id_rsa
```
**На воркерах:**
```sh
sudo chmod 755 /home/vwwetr
sudo chmod 700 /home/vwwetr/.ssh
sudo chmod 600 /home/vwwetr/.ssh/authorized_keys