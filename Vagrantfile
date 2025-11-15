# -*- mode: ruby -*-
# vi: set ft=ruby :

# Nodes parameters
box_image     = "bento/centos-stream-10"
box_version   = "202510.26.0"
private_host  = "172.17.177."
limit_memory  = "1024"
limit_cpus    = 1

Vagrant.configure("2") do |config|
  config.vm.box_check_update = false

  nodes = ["Database", "NginxTech", "NginxUi", "OpenSearch", "Monitoring"]

  nodes.each_with_index do |name, i|
    ip_address = "#{private_host}#{210 + i}"

    config.vm.define name do |node|
      node.vm.hostname = name
      node.vm.box = box_image
      node.vm.box_version = box_version
      node.vm.network :private_network, ip: ip_address

      node.vm.provision "shell", inline: "echo hello from #{name} node!"

      node.vm.provider "virtualbox" do |vb|
        vb.gui = false
        vb.memory = limit_memory
        vb.cpus = limit_cpus
      end
    end
  end
end