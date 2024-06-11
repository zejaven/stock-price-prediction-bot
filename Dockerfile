FROM openjdk:17
WORKDIR /
ARG APP_USER
COPY ./target/*.jar stock-price-prediction-bot.jar
COPY ./src/main/resources/videos /home/${APP_USER}/resources/videos
EXPOSE 8080