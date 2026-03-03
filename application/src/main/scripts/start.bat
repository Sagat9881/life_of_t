@echo off
echo ================================================
echo   Life of T - Жизнь Татьяны
echo   Уютный life-sim симулятор
echo ================================================
echo.

REM Check Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ОШИБКА: Java не найдена! Установите Java 21+
    echo Скачать: https://adoptium.net/
    pause
    exit /b 1
)

echo [1/3] Генерация ассетов...
java -jar life-of-t-application-0.1.0-SNAPSHOT.jar --generate-assets --spring.main.web-application-type=none 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Предупреждение: Генерация ассетов завершилась с ошибкой
)

echo [2/3] Запуск сервера...
start /B java -jar life-of-t-application-0.1.0-SNAPSHOT.jar

echo [3/3] Ожидание запуска...
:wait_loop
timeout /t 2 /nobreak >nul
curl -s http://localhost:8080/api/game/start >nul 2>&1
if %ERRORLEVEL% NEQ 0 goto wait_loop

echo.
echo ================================================
echo   Игра запущена! Открываю браузер...
echo   http://localhost:8080
echo ================================================
start http://localhost:8080
echo.
echo Для остановки нажмите Ctrl+C или закройте это окно
pause
