#!/bin/bash

set -a
source .env
set +a

echo "APP_USER=$APP_USER"

if [ ! -d "app/resources/knowledge_base" ] || [ -z "$(ls -A app/resources/knowledge_base)" ]; then
    echo "Copying files to app/resources/knowledge_base"
    mkdir -p app/resources/knowledge_base
    cp -r app/initial_knowledge_base/* app/resources/knowledge_base
fi

mvn clean install -DskipTests=true
docker build --build-arg APP_USER=$APP_USER -t stock-price-prediction-bot .
docker build -t stock-price-prediction-app ./app
docker-compose up