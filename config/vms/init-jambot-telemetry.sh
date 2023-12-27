#!/bin/bash

sudo apt update

./shared/install-docker.sh
./shared/install-qemu-guest-agent.sh

# Setup SigNoz
cd ~
git clone -b main https://github.com/SigNoz/signoz.git && cd signoz/deploy/
sed -i~ -e '/^\s*hotrod:/,/locust$/d' docker/clickhouse-setup/docker-compose.yaml
docker compose -f docker/clickhouse-setup/docker-compose.yaml up -d
