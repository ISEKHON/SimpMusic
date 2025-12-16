@echo off
echo ================================================
echo   YouTube Music Player GUI - Launcher
echo ================================================
echo.
echo Starting GUI application...
echo.

cd ..
call gradlew.bat :myjavaapp:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ================================================
    echo   Error: Build or run failed!
    echo ================================================
    echo.
    echo Try:
    echo   1. Sync Gradle: File ^> Sync Project with Gradle Files
    echo   2. Clean build: gradlew.bat clean build
    echo   3. Make sure JavaFX is downloaded
    echo.
    pause
    exit /b 1
)

echo.
echo GUI closed.
pause

