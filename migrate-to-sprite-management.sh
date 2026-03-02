#!/bin/bash
# Migration script для реорганизации в sprite-management

set -e  # Exit on error

echo "🔧 Starting sprite-management migration..."

# 1. Создаём структуру
echo "📁 Creating sprite-management structure..."
mkdir -p sprite-management/compositor
mkdir -p sprite-management/prompt-scanner

# 2. Перемещаем compositor (lpc-sprite-compositor)
echo "🚚 Moving compositor files..."
if [ -d "lpc-sprite-compositor" ]; then
    # Копируем содержимое
    cp -r lpc-sprite-compositor/src sprite-management/compositor/ 2>/dev/null || true
    cp lpc-sprite-compositor/README.md sprite-management/compositor/ 2>/dev/null || true
    
    # Обновляем package name в Java файлах
    find sprite-management/compositor/src -name "*.java" -type f -exec sed -i '' 's/package ru.lifegame.lpc.compositor/package ru.lifegame.sprite.compositor/g' {} \;
    find sprite-management/compositor/src -name "*.java" -type f -exec sed -i '' 's/import ru.lifegame.lpc.compositor/import ru.lifegame.sprite.compositor/g' {} \;
    
    echo "✅ Compositor moved"
else
    echo "⚠️  lpc-sprite-compositor not found"
fi

# 3. Перемещаем prompt-scanner (lpc-generator)
echo "🚚 Moving prompt-scanner files..."
if [ -d "lpc-generator" ]; then
    # Копируем содержимое
    cp -r lpc-generator/src sprite-management/prompt-scanner/ 2>/dev/null || true
    cp lpc-generator/*.md sprite-management/prompt-scanner/ 2>/dev/null || true
    
    # Обновляем package name в Java файлах
    find sprite-management/prompt-scanner/src -name "*.java" -type f -exec sed -i '' 's/package ru.lifegame.lpc/package ru.lifegame.sprite.scanner/g' {} \;
    find sprite-management/prompt-scanner/src -name "*.java" -type f -exec sed -i '' 's/import ru.lifegame.lpc/import ru.lifegame.sprite.scanner/g' {} \;
    
    echo "✅ Prompt-scanner moved"
else
    echo "⚠️  lpc-generator not found"
fi

# 4. Подтягиваем изменения из GitHub
echo "📥 Pulling changes from GitHub..."
git pull origin feature/lpc-sprites-integration

# 5. Удаляем старые папки
echo "🗑️  Removing old directories..."
rm -rf lpc-sprite-compositor 2>/dev/null || true
rm -rf lpc-generator 2>/dev/null || true

echo "✅ Migration completed!"
echo ""
echo "📋 Next steps:"
echo "1. Review changes: git status"
echo "2. Commit: git add -A && git commit -m 'refactor: reorganize to sprite-management structure'"
echo "3. Push: git push origin feature/lpc-sprites-integration"
echo "4. Build: mvn clean install"

echo ""
echo "🎯 New structure:"
echo "sprite-management/"
echo "├── compositor/         # Local PNG compositor"
echo "└── prompt-scanner/     # Prompt scanning utilities"
echo ""