@echo off
chcp 65001 >nul
echo ========================================
echo   Life of T - Rebuild and Run
echo ========================================
echo.

echo [1/3] Pulling updates from Git...
git pull origin dev/0.1.0
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: Git pull failed!
    echo Check your internet connection or Git status.
    pause
    exit /b 1
)
echo ✅ Git pull completed
echo.

echo [2/3] Building project with Maven...
echo This may take 20-40 seconds...
echo.
mvn clean install -DskipTests=true
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: Maven build failed!
    echo Check the errors above.
    pause
    exit /b 1
)
echo ✅ Build completed
echo.

echo [3/3] Starting application...
echo.
echo 🚀 Opening at http://localhost:8080
echo.
echo Press Ctrl+C to stop the server
echo ========================================
echo.
java -jar application/target/life-of-t.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ ERROR: Application failed to start!
    pause
    exit /b 1
)
