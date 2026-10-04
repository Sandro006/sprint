@echo off
setlocal

echo ==========================================
echo        CREATION DE gogo.jar
echo ==========================================

REM ==========================================
REM CONFIGURATION
REM ==========================================

set "SRC_DIR=D:\alefas4\main-main\main-main\src"
set "TEST_DIR=D:\alefas4\FrontController-Test"
set "LIB_DIR=%TEST_DIR%\lib"
set "WEB_LIB_DIR=%TEST_DIR%\src\main\webapp\WEB-INF\lib"
set "TEMP_DIR=D:\alefas4\main-main\main-main\temp"

REM Tomcat 10.1 requis (framework en jakarta.*, incompatible avec Tomcat 9)
set "TOMCAT_DIR=C:\Program Files\Apache Software Foundation\Tomcat 10.1"

set "OUTPUT_JAR=%LIB_DIR%\gogo.jar"

REM ==========================================
REM VERIFICATIONS
REM ==========================================

echo.
echo Source :
echo %SRC_DIR%
echo.
echo Destination :
echo %OUTPUT_JAR%

if not exist "%SRC_DIR%" (
    echo.
    echo [ERREUR] Le dossier source n'existe pas.
    pause
    exit /b 1
)

if not exist "%TOMCAT_DIR%\lib\jakarta.servlet-api.jar" (
    if not exist "%TOMCAT_DIR%\lib\servlet-api.jar" (
        echo.
        echo [ERREUR] Tomcat introuvable ou incomplet :
        echo %TOMCAT_DIR%
        pause
        exit /b 1
    )
)

where javac > nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERREUR] javac introuvable dans le PATH.
    pause
    exit /b 1
)

if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
if not exist "%WEB_LIB_DIR%" mkdir "%WEB_LIB_DIR%"

REM ==========================================
REM COMPILATION
REM ==========================================

echo.
echo [1/4] Compilation du framework...

if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"

dir /s /b "%SRC_DIR%\*.java" > "%TEMP_DIR%\sources.txt"

javac -cp "%TOMCAT_DIR%\lib\*;%LIB_DIR%\*" -d "%TEMP_DIR%" @"%TEMP_DIR%\sources.txt"

if errorlevel 1 (
    echo.
    echo [ERREUR] Compilation echouee.
    pause
    exit /b 1
)

echo [OK] Compilation reussie.

REM ==========================================
REM CREATION DU JAR
REM ==========================================

echo.
echo [2/4] Creation de gogo.jar...

if exist "%OUTPUT_JAR%" del /q "%OUTPUT_JAR%"
jar -cf "%OUTPUT_JAR%" -C "%TEMP_DIR%" .

if errorlevel 1 (
    echo.
    echo [ERREUR] Impossible de creer gogo.jar.
    pause
    exit /b 1
)

rmdir /s /q "%TEMP_DIR%"

REM ==========================================
REM SYNCHRONISATION VERS LE PROJET DE TEST
REM (sans ca, le WAR embarque un gogo.jar obsolete)
REM ==========================================

echo.
echo [3/4] Synchronisation vers le projet de test...

copy /y "%OUTPUT_JAR%" "%WEB_LIB_DIR%\gogo.jar" > nul
REM Plus de dependance Gson : JSON gere par util.JsonUtil maison

REM ==========================================
REM VERIFICATION
REM ==========================================

echo.
echo [4/4] Verification du JAR...

for /f %%c in ('jar tf "%WEB_LIB_DIR%\gogo.jar" ^| find /c ".class"') do set NBCLASS=%%c
echo Classes dans gogo.jar : %NBCLASS%

if "%NBCLASS%"=="0" (
    echo.
    echo [ERREUR] gogo.jar est VIDE. Deploiement annule.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo gogo.jar cree et synchronise avec succes !
echo ==========================================
echo.
echo Fichiers a jour :
echo - %OUTPUT_JAR%
echo - %WEB_LIB_DIR%\gogo.jar
echo.
echo Etape suivante : lance deploy.bat dans
echo %TEST_DIR%
echo.
pause
