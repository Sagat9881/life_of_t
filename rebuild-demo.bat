@echo off
echo ========================================
echo Life of T Demo - Full Rebuild
echo ========================================
echo.

echo [1/5] Cleaning old builds...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)

echo.
echo [2/5] Deleting frontend cache...
if exist frontend\node_modules rmdir /s /q frontend\node_modules
if exist frontend\dist rmdir /s /q frontend\dist
if exist frontend\.vite rmdir /s /q frontend\.vite

echo.
echo [3/5] Building project with Maven...
call mvn install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed!
    pause
    exit /b 1
)

echo.
echo [4/5] Verifying demo JAR exists...
if not exist demo\target\life-of-t-demo.jar (
    echo ERROR: Demo JAR not found!
    pause
    exit /b 1
)

echo.
echo [5/5] Verifying demo EXE exists...
if not exist demo\target\life-of-t-demo.exe (
    echo WARNING: Demo EXE not found! Launch4j might have failed.
    echo You can still run: java -jar demo\target\life-of-t-demo.jar
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo Run demo with:
echo   demo\target\life-of-t-demo.exe
echo   OR
echo   java -jar demo\target\life-of-t-demo.jar
echo.
pause
