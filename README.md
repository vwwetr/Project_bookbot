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
sudo usermod -aG wheel vwwetr
echo "vwwetr ALL=(ALL) NOPASSWD:ALL" | sudo tee /etc/sudoers.d/vwwetr > /dev/null
sudo chmod 440 /etc/sudoers.d/vwwetr
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --reload
```