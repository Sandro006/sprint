@echo off
setlocal enabledelayedexpansion

set PROJECT=sprint-2
set TOMCAT=C:\Program Files\xampp\tomcat\

:: Vérification que Tomcat existe
if not exist "%TOMCAT%" (
    echo ERREUR : Tomcat introuvable dans %TOMCAT%
    pause
    exit /b 1
)

echo ==========================
echo Compilation...
echo ==========================

:: Nettoyage
if exist build rmdir /S /Q build
mkdir build

:: Compilation de tous les fichiers .java
dir /s /B src\*.java > sources.txt
javac -cp "%TOMCAT%\lib\*" -d build @sources.txt
del sources.txt

if errorlevel 1 (
    echo ERREUR DE COMPILATION
    pause
    exit /b 1
)
echo ==========================
echo Creation du JAR...
echo ==========================

cd build
jar cvf ..\%PROJECT%.jar *
cd ..

echo JAR cree : %PROJECT%.jar
echo.
echo ==========================
echo Deploiement...
echo ==========================

:: Nettoyage du déploiement précédent
if exist "%TOMCAT%\webapps\%PROJECT%" (
    rmdir /S /Q "%TOMCAT%\webapps\%PROJECT%"
)

:: Création de la structure
mkdir "%TOMCAT%\webapps\%PROJECT%\WEB-INF\classes"

:: Copie des classes compilées
xcopy build\* "%TOMCAT%\webapps\%PROJECT%\WEB-INF\classes" /E /I /Y

:: Copie du web.xml
if exist WEB-INF\web.xml (
    copy WEB-INF\web.xml "%TOMCAT%\webapps\%PROJECT%\WEB-INF" /Y
) else (
    echo ATTENTION : web.xml introuvable
)

echo.
echo ==========================
echo DEPLOIEMENT TERMINE
echo ==========================
echo.
echo Tester :
echo http://localhost:8080/%PROJECT%/users
echo.

pause
endlocal