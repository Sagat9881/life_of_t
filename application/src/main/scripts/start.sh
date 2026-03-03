#!/bin/bash
echo "================================================"
echo "  Life of T — Жизнь Татьяны"
echo "  Уютный life-sim симулятор"
echo "================================================"
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "ОШИБКА: Java не найдена! Установите Java 21+"
    echo "Скачать: https://adoptium.net/"
    exit 1
fi

JAR_FILE="life-of-t-application-0.1.0-SNAPSHOT.jar"

echo "[1/3] Генерация ассетов..."
java -jar "$JAR_FILE" --generate-assets --spring.main.web-application-type=none 2>/dev/null || true

echo "[2/3] Запуск сервера..."
java -jar "$JAR_FILE" &
SERVER_PID=$!

echo "[3/3] Ожидание запуска..."
for i in $(seq 1 30); do
    sleep 2
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        break
    fi
    if ! kill -0 $SERVER_PID 2>/dev/null; then
        echo "ОШИБКА: Сервер не запустился"
        exit 1
    fi
done

echo ""
echo "================================================"
echo "  Игра запущена!"
echo "  http://localhost:8080"
echo "================================================"

# Open browser
if command -v xdg-open &> /dev/null; then
    xdg-open http://localhost:8080
elif command -v open &> /dev/null; then
    open http://localhost:8080
fi

echo ""
echo "Для остановки нажмите Ctrl+C"
trap "kill $SERVER_PID 2>/dev/null; exit 0" INT TERM
wait $SERVER_PID
