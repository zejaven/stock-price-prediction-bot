FROM openjdk:17
WORKDIR /
COPY ./target/*.jar stock-price-prediction-bot.jar
EXPOSE 8080