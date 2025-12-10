# -*- mode: ruby -*-
# vi: set ft=ruby :

# Отключаем автогенерацию уникального SSH-ключа для каждой VM:
# config.ssh.insert_key = false 
# При значении false Vagrant НЕ вставляет свой ключ в машину и использует общий
# insecure_private_key из ~/.vagrant.d для пользователя vagrant (удобно для стендов,
# но небезопасно для прод-окружения)
# Все порты (по хорошему) нужно отсюда убрать и прописать в bookbot.yml

# Nodes parameters
box_image     = "bento/centos-stream-10"
box_version   = "202510.26.0"
private_host  = "192.168.56." # Не меняй подсеть!
limit_memory  = "1024"
limit_cpus    = 1
Vagrant.configure("2") do |config|
  config.ssh.insert_key = false 
  config.vm.box_check_update = false
  nodes = ["Database", "NginxTech", "NginxUi", "Logging", "Monitoring"]

  nodes.each_with_index do |name, i|
    ip_address = "#{private_host}#{210 + i}"

    config.vm.define name do |node|
      node.vm.hostname = name
      node.vm.box = box_image
      node.vm.box_version = box_version
      node.vm.network :private_network, ip: ip_address

      # Автоматическое открытие порта SSH на ноде через firewalld
      node.vm.provision "shell", inline: <<-SHELL
        sudo systemctl enable firewalld
        sudo systemctl start firewalld
        # SSH:
        sudo firewall-cmd --permanent --add-port=22/tcp
        # PostgreSQL:
        sudo firewall-cmd --permanent --add-port=5432/tcp
        # Nginx (HTTP/HTTPS):
        sudo firewall-cmd --permanent --add-port=80/tcp
        sudo firewall-cmd --permanent --add-port=443/tcp
        # Jenkins:
        sudo firewall-cmd --permanent --add-port=8080/tcp
        # Prometheus/Grafana:
        sudo firewall-cmd --permanent --add-port=9090/tcp   # Prometheus
        sudo firewall-cmd --permanent --add-port=3000/tcp   # Grafana
        # Zabbix:
        sudo firewall-cmd --permanent --add-port=10050/tcp  # Zabbix agent
        sudo firewall-cmd --permanent --add-port=10051/tcp  # Zabbix server
        # OpenSearch + Dashboard
        sudo firewall-cmd --permanent --add-port=9200/tcp   # OpenSearch HTTP
        sudo firewall-cmd --permanent --add-port=9300/tcp   # OpenSearch transport
        sudo firewall-cmd --permanent --add-port=5601/tcp   # OpenSearch Dashboards
        sudo firewall-cmd --permanent --add-port=24224/tcp
        # Node-Exporter: typical ports or exporter metrics 
        sudo firewall-cmd --permanent --add-port=9100/tcp
        # K8s
        sudo firewall-cmd --permanent --add-port=6443/tcp        # Kubernetes API server
        sudo firewall-cmd --permanent --add-port=2379-2380/tcp   # etcd
        sudo firewall-cmd --permanent --add-port=10250/tcp       # kubelet
        sudo firewall-cmd --permanent --add-port=10251/tcp       # kube-scheduler
        sudo firewall-cmd --permanent --add-port=10252/tcp       # kube-controller-manager

        # # Centos:
        # sudo dnf -y install dnf-plugins-core
        # sudo dnf config-manager \
        #   --setopt=baseos.baseurl=https://mirror.stream.centos.org/10-stream/BaseOS/x86_64/os/ \
        #   --save
        # sudo dnf config-manager --setopt=baseos.metalink= --save
        # sudo dnf clean all
        # sudo rm -rf /var/cache/dnf/*
        # # Checking:
        sudo firewall-cmd --reload
        sudo firewall-cmd --list-ports
        sudo timedatectl set-timezone Europe/Moscow
      SHELL

      node.vm.provision "shell", inline: "echo hello from #{name} node!"

      node.vm.provider "virtualbox" do |vb|
        vb.gui = false
        vb.memory = limit_memory
        vb.cpus = limit_cpus
      end
    end
  end
end
