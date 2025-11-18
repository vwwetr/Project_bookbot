# Project_bookbot
Telegram learning JSB bot pet project.

## 📘 Java
```sh
brew install maven 
mvn clean package -DskipTests
java -jar target/learningbot-1.0.0.jar
```

## CentOS nodes setting up
```sh
# Пользователи и права:
sudo usermod -aG wheel vwwetr
echo "vwwetr ALL=(ALL) NOPASSWD:ALL" | sudo tee /etc/sudoers.d/vwwetr > /dev/null
sudo chmod 440 /etc/sudoers.d/vwwetr
# Firewall:
sudo dnf isntall -y # Обновить актуальный Firewall
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --list-all # Отобразить список, к каким сервисам уже предоставлен доступ через брандмауэр (Как это происходит в delivery?)
sudo dnf install -y cockpit
sudo systemctl enable --now cockpit.socket # Установка и немедленный запуск cockpit
sudo firewall-cmd --reload
#SSH - есть у vagrant при взаимодейтсвии с localhost, нужно будет прокидывать 
```