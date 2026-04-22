@echo off
echo ============================================
echo   SmartDispatch - Compiling Project...
echo ============================================

cd /d "%~dp0"

set MYSQL_JAR=..\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar
set JAVAFX_LIB=..\javafx-sdk-21.0.6\lib
set CP=%MYSQL_JAR%;%JAVAFX_LIB%\javafx.base.jar;%JAVAFX_LIB%\javafx.controls.jar;%JAVAFX_LIB%\javafx.graphics.jar

if exist out rmdir /s /q out
mkdir out

echo Compiling...
javac -cp "%CP%" -d out src\com\project\model\*.java src\com\project\database\*.java src\com\project\graph\*.java src\com\project\algorithm\*.java src\com\project\service\*.java src\com\project\ui\DashboardApp.java src\com\project\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo *** COMPILATION FAILED ***
    pause
    exit /b 1
)

echo.
echo ============================================
echo   Compilation Successful! Running Demo...
echo ============================================
echo.

java -cp "out;%MYSQL_JAR%" com.project.service.DemoRunner

echo.
pause
