@echo off
setlocal EnableDelayedExpansion

for /f "usebackq tokens=*" %%a in (".env") do (
    set "line=%%a"
    if not "!line:~0,1!"=="#" (
        for /f "tokens=1* delims==" %%b in ("!line!") do (
            set "key=%%b"
            set "value=%%c"
            set "key=!key: =!"
            set "value=!value: =!"
            set "!key!=!value!"
        )
    )
)

echo APP_USER=%APP_USER%

mvn clean install -DskipTests=true ^
    && docker build --build-arg APP_USER=%APP_USER% -t stock-price-prediction-bot . ^
    && docker build -t stock-price-prediction-app ./app ^
    && docker-compose up

endlocal
