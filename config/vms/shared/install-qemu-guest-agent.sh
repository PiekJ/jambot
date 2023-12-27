#!/bin/bash

sudo apt update && sudo apt -y install qemu-guest-agent

sudo systemctl enable qemu-guest-agent
sudo systemctl start qemu-guest-agent
