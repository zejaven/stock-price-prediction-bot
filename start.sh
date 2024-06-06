mvn clean install -DskipTests=true
docker build -t stock-price-prediction-bot .
docker build -t stock-price-prediction-app ./app
docker-compose up