## Stock Price Prediction Expert System launch instructions

---

To launch this expert system you should:
1. Be a client of Tinkoff Investments: https://www.tinkoff.ru/invest/
2. Have a brokerage account
3. Issue at least read-only token in user account settings: https://www.tinkoff.ru/invest/settings/
4. Create a new bot using Telegram **@BotFather**: https://t.me/BotFather. After bot is created you will get Telegram bot token.
5. Find out your numeric user id in Telegram, you can use **@userinfobot**: https://t.me/userinfobot
6. Come up with an idea of your username and password to connect to the storage inside Docker container via sftp.
7. Create **hidden.ini** file in the **app/** folder and put Tinkoff API token and your Telegram user id there in the following format:
```
[DEFAULT]
API_TOKEN = {your_tinkoff_api_token}
ADMIN_CHAT_ID = {your_telegram_user_id}
```
8. Create **hidden.yml** file in **src/main/resources/** folder and put your username, Telegram bot token and Tinkoff API token there in the following format:
```
stock-price-prediction-bot:
  user: {your_username}
  token: {your_telegram_bot_token}
  tinkoff-api-token: {your_tinkoff_api_token}
```
9. Create **.env** file in the current folder and put your username and password that you came up with in the following format:
```
APP_USER={your_username}
APP_PASSWORD={your_password}
```
10. Install Docker and Docker Compose on the system and set corresponding system environment variables.
11. Install Maven on the system and set *mvn* environment variable.
12. Use **start.bat** to launch the application on Windows or **start.sh** to launch it on Linux.
13. You can connect to the storage inside Docker container via sftp with the following data:
```
Host: localhost
Port: 2222
User: {your_username}
Password: {your_password}
```
