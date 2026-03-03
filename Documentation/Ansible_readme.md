## Ansible роли в проекте: 
**Для идентификации нод смотри инветраь hosts.ini**
- Users 
    - cluster_users (Целевые ноды: Все)
- database
    - pg_install (Целевые ноды: db)
    - db_create (Целевые ноды: db)
    - db_backup (Целевые ноды: db)
- K8s 
    - app_k8s (Целевые ноды: App_localhost)

## Настройка пользователя Ansible на локальной машине:

```sh
sudo sysadminctl -addUser ansible -fullName "Ansible" -password 'ansible' -admin
echo "ansible ALL=(ALL) NOPASSWD: ALL" | sudo tee /etc/sudoers.d/ansible
id ansible
sudo install -d -m 700 -o ansible -g staff /Users/ansible/.ssh
sudo sh -c 'cat /Users/vwwetr/.ssh/ansible_id_ed25519.pub >> /Users/ansible/.ssh/authorized_keys'
sudo chown ansible:staff /Users/ansible/.ssh/authorized_keys
sudo chmod 600 /Users/ansible/.ssh/authorized_keys
ssh -i /Users/vwwetr/.ssh/ansible_id_ed25519 ansible@127.0.0.1
```

## Топ CLI команды Ansible
```sh
ansible all -m ping # вызов модуля ping и проверка подключения
ansible all -m setup # отображает факты сервера (аппаратная архитектура, название системы, IP адреса, состтояние памяти и др.)
ansible-inventory --list # отобразить содержимое invventory
ansible-inventory --graph # отобразить дерево inventory
ansible-galaxy init app_k8s # Create role 
ansible-vault create vault.yml 
ansible-vault encrypt vault.yml # Кодирование волта в base64
ansible-vault decrypt vault.yml # Декодирование волта из base64
ansible-playbook bookbot.yml -u vagrant --private-key=~/.vagrant.d/insecure_private_key --tags users --ask-vault-pass #
ansible-playbook bookbot.yml --ask-vault-pass
```