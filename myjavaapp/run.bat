@echo off
pause
echo Done!
echo.

)
    exit /b 1
    pause
    echo.
    echo   3. Check that Gradle sync completed
    echo   2. Make sure you have Java 17+ installed
    echo   1. Run 'gradlew.bat clean' first
    echo Try:
    echo.
    echo ============================================
    echo   Error: Build or run failed!
    echo ============================================
    echo.
if %ERRORLEVEL% NEQ 0 (

call gradlew.bat :myjavaapp:run

echo.
echo Building and running the app...

cd ..

echo.
echo ========================================
echo   My Java YouTube Music App - Launcher
echo ========================================
REM Quick start script for myjavaapp

