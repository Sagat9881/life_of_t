#!/bin/bash

echo "========================================"
echo "Life of T Demo - Full Rebuild"
echo "========================================"
echo ""

echo "[1/5] Cleaning old builds..."
mvn clean
if [ $? -ne 0 ]; then
    echo "ERROR: Maven clean failed!"
    exit 1
fi

echo ""
echo "[2/5] Deleting frontend cache..."
rm -rf frontend/node_modules
rm -rf frontend/dist
rm -rf frontend/.vite

echo ""
echo "[3/5] Building project with Maven..."
mvn install -DskipTests
if [ $? -ne 0 ]; then
    echo "ERROR: Maven build failed!"
    exit 1
fi

echo ""
echo "[4/5] Verifying demo JAR exists..."
if [ ! -f demo/target/life-of-t-demo.jar ]; then
    echo "ERROR: Demo JAR not found!"
    exit 1
fi

echo ""
echo "[5/5] Verifying demo EXE exists..."
if [ ! -f demo/target/life-of-t-demo.exe ]; then
    echo "WARNING: Demo EXE not found! Launch4j might have failed."
    echo "You can still run: java -jar demo/target/life-of-t-demo.jar"
fi

echo ""
echo "========================================"
echo "BUILD SUCCESSFUL!"
echo "========================================"
echo ""
echo "Run demo with:"
echo "  demo/target/life-of-t-demo.exe"
echo "  OR"
echo "  java -jar demo/target/life-of-t-demo.jar"
echo ""
