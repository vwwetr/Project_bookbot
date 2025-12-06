#Bash-скрипт для бэкапа БД

#!/usr/bin/env bash
exec >>/home/vagrant/pg_backup.log 2>>/home/vagrant/pg_backup.err
echo "==== $(date) STARTED by CRON as $(whoami) ===="
set -x
logger "[PG_BACKUP] Скрипт pg_backup.sh запущен пользователем $(whoami)"

export HOME=/home/vagrant # Если хочу шаблонизировать в jinja, то надо засунуть в какие-то переменные
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

PGPASSWORD='vagrant'; export PGPASSWORD # Если хочу шаблонизировать в jinja, то надо засунуть в какие-то переменные

# Хосты # Если хочу шаблонизировать в jinja, то надо засунуть в какие-то переменные
dbHost= 
backupHost=

# Параметры # Если хочу шаблонизировать в jinja, то надо засунуть в какие-то переменные
pathB=/mnt/backup
dbUser=
database=

dump_name="pgsql_$(TZ='Europe/Moscow' date '+%Y-%m-%d_%H-%M').dump.gz"
dump_path="/tmp/${dump_name}"

# Делаем дамп (custom) и сразу gzip
/usr/bin/pg_dump -h "$dbHost" -p 5432 -U "$dbUser" -d "$database" -Fc \
  | /bin/gzip > "$dump_path"

# Проверяем, что файл создан
[ -s "$dump_path" ] || { echo "Dump not created: $dump_path"; exit 1; }

# Убираем агент (используем указанный ключ с диска)
unset SSH_AUTH_SOCK SSH_AGENT_PID

# Копируем на бэкап-ноду (known_hosts должен быть прогрет заранее)
# Для диагностики можно добавить -vvv
scp -i /home/vagrant/.ssh/id_rsa \
    -o StrictHostKeyChecking=accept-new \
    /tmp/$dump_name vagrant@172.17.....:/mnt/backup/

rm -f "$dump_path"
unset PGPASSWORD
echo "==== $(date) FINISHED ===="


#var for ansible:
#source_dir: "./files/"
#destination_dir: "/opt/postgresql/sql/" #best practise DeOps