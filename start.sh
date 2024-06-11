#!/bin/bash

set -a
source .env
set +a

echo "APP_USER=$APP_USER"

mvn clean install -DskipTests=true
docker build --build-arg APP_USER=$APP_USER -t stock-price-prediction-bot .
docker build -t stock-price-prediction-app ./app
docker-compose up