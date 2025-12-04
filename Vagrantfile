# -*- mode: ruby -*-
# vi: set ft=ruby :

# Отключаем автогенерацию уникального SSH-ключа для каждой VM:
# config.ssh.insert_key = false 
# При значении false Vagrant НЕ вставляет свой ключ в машину и использует общий
# insecure_private_key из ~/.vagrant.d для пользователя vagrant (удобно для стендов,
# но небезопасно для прод-окружения)

# Nodes parameters
box_image     = "bento/centos-stream-10"
box_version   = "202510.26.0"
private_host  = "172.17.177."
limit_memory  = "1024"
limit_cpus    = 1

Vagrant.configure("2") do |config|
  config.ssh.insert_key = false 
  config.vm.box_check_update = false
  nodes = ["Database", "NginxTech", "NginxUi", "OpenSearch", "Monitoring"]

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
        sudo firewall-cmd --permanent --add-service=ssh
        sudo firewall-cmd --permanent --add-port=22/tcp
        sudo firewall-cmd --reload
        sudo firewall-cmd --query-port=22/tcp
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
