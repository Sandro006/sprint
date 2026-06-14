@echo off

set PROJECT=sprint-0-
set TOMCAT=C:\Program Files\Apache Software Foundation\Tomcat 9.0

echo ==========================
echo Compilation...
echo ==========================

if exist build rmdir /S /Q build

mkdir build

javac ^
-cp "%TOMCAT%\lib\*" ^
-d build ^
src\framework\*.java ^
src\controller\*.java

if errorlevel 1 (
    echo ERREUR DE COMPILATION
    pause
    exit
)

echo ==========================
echo Deploiement...
echo ==========================

if exist "%TOMCAT%\webapps\%PROJECT%" (
    rmdir /S /Q "%TOMCAT%\webapps\%PROJECT%"
)

mkdir "%TOMCAT%\webapps\%PROJECT%"
mkdir "%TOMCAT%\webapps\%PROJECT%\WEB-INF"
mkdir "%TOMCAT%\webapps\%PROJECT%\WEB-INF\classes"

xcopy build "%TOMCAT%\webapps\%PROJECT%\WEB-INF\classes" /E /I /Y

copy WEB-INF\web.xml ^
"%TOMCAT%\webapps\%PROJECT%\WEB-INF"

echo.
echo ==========================
echo DEPLOIEMENT TERMINE
echo ==========================
echo.
echo Tester :
echo http://localhost:8081/%PROJECT%/users
echo.

pause