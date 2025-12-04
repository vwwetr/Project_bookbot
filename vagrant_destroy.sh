#!/usr/bin/env zsh

vagrant halt -f;
vagrant destroy -f;
vagrant global-status --prune;
rm -rf .vagrant/
