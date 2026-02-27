@echo off
REM Script to remove deprecated api/hooks files

echo Removing deprecated api/hooks files...

del /F /Q src\api\hooks\useActions.ts 2>nul
del /F /Q src\api\hooks\useGameState.ts 2>nul

echo Done! Deprecated hooks removed.
echo All API logic is now in:
echo   - src\store\gameStore.ts
echo   - src\services\api.ts

pause
