@echo off
pause

)
    echo.
    echo   3. Check androidapp\build\outputs\logs for errors
    echo   2. Clean: gradlew.bat clean
    echo   1. Sync Gradle in Android Studio
    echo Try:
    echo.
    echo ================================================
    echo   ❌ BUILD FAILED
    echo ================================================
    echo.
) else (
    echo.
    echo Or open in Android Studio and click Run!
    echo.
    echo   2. Run: gradlew.bat :androidapp:installDebug
    echo   1. Connect Android device
    echo To install:
    echo.
    echo   androidapp\build\outputs\apk\debug\androidapp-debug.apk
    echo APK Location:
    echo.
    echo ================================================
    echo   ✅ BUILD SUCCESSFUL!
    echo ================================================
    echo.
if %ERRORLEVEL% EQU 0 (

call gradlew.bat :androidapp:assembleDebug
echo Building Android App...
echo.

call gradlew.bat --refresh-dependencies
echo Syncing Gradle...
cd ..

echo.
echo ================================================
echo   Building Android YouTube Music Player
echo ================================================

