#!/bin/bash
# Фиксит package names в prompt-scanner после миграции

set -e

echo "🔧 Fixing package names in prompt-scanner..."

# 1. Исправляем package declarations в model
find sprite-management/prompt-scanner/src/main/java/ru/lifegame/lpc/model -name "*.java" -type f -exec sed -i 's/^package ru.lifegame.lpc.model;/package ru.lifegame.sprite.scanner.model;/g' {} \;

echo "✅ Fixed model package names"

# 2. Исправляем package declarations в остальных файлах
find sprite-management/prompt-scanner/src/main/java/ru/lifegame/lpc/extractor -name "*.java" -type f -exec sed -i 's/^package ru.lifegame.lpc.extractor;/package ru.lifegame.sprite.scanner.extractor;/g' {} \;
find sprite-management/prompt-scanner/src/main/java/ru/lifegame/lpc/scanner -name "*.java" -type f -exec sed -i 's/^package ru.lifegame.lpc.scanner;/package ru.lifegame.sprite.scanner.scanner;/g' {} \;
find sprite-management/prompt-scanner/src/main/java/ru/lifegame/lpc/selenium -name "*.java" -type f -exec sed -i 's/^package ru.lifegame.lpc.selenium;/package ru.lifegame.sprite.scanner.selenium;/g' {} \;
find sprite-management/prompt-scanner/src/main/java/ru/lifegame/lpc/url -name "*.java" -type f -exec sed -i 's/^package ru.lifegame.lpc.url;/package ru.lifegame.sprite.scanner.url;/g' {} \;
find sprite-management/prompt-scanner/src/main/java/ru/lifegame/lpc/config -name "*.java" -type f -exec sed -i 's/^package ru.lifegame.lpc.config;/package ru.lifegame.sprite.scanner.config;/g' {} \;

echo "✅ Fixed other package names"

# 3. Исправляем корневые файлы
find sprite-management/prompt-scanner/src/main/java/ru/lifegame/lpc -maxdepth 1 -name "*.java" -type f -exec sed -i 's/^package ru.lifegame.lpc;/package ru.lifegame.sprite.scanner;/g' {} \;

echo "✅ Fixed root package names"

# 4. Исправляем все import statements
find sprite-management/prompt-scanner/src/main/java -name "*.java" -type f -exec sed -i 's/import ru\.lifegame\.lpc\./import ru.lifegame.sprite.scanner./g' {} \;

echo "✅ Fixed import statements"

# 5. Аналогично для compositor
echo "🔧 Fixing package names in compositor..."

find sprite-management/compositor/src/main/java/ru/lifegame/lpc -name "*.java" -type f -exec sed -i 's/^package ru.lifegame.lpc.compositor;/package ru.lifegame.sprite.compositor;/g' {} \;
find sprite-management/compositor/src/main/java -name "*.java" -type f -exec sed -i 's/import ru\.lifegame\.lpc\.compositor\./import ru.lifegame.sprite.compositor./g' {} \;

echo "✅ Fixed compositor package names"

echo ""
echo "✅ Package names fixed!"
echo ""
echo "📋 Next steps:"
echo "1. Commit: git add -A && git commit -m 'refactor: fix package names after sprite-management migration'"
echo "2. Push: git push origin feature/lpc-sprites-integration"
echo "3. Build: mvn clean install -DskipTests"
echo ""