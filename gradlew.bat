@echo off
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle 9.2+ is required. Install Gradle and add it to PATH, then run: gradle build
  exit /b 1
)
gradle %*
