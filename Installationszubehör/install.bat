@echo off
chcp 1252

if not exist "%AppData%\Gr�n2_Mitgliederverwaltung" (
    mkdir "%AppData%\Gr�n2_Mitgliederverwaltung"
)

if not exist "%ProgramFiles%\Gr�n2_Mitgliederverwaltung" (
    xcopy /s /y "%~dp0Gr�n2.conf" "%AppData%\Gr�n2_Mitgliederverwaltung"
    xcopy /s /y "%~dp0Gr�n2.VisualElementsManifest.xml" "%AppData%\Gr�n2_Mitgliederverwaltung"

    call :getAdminrights

    ::"install"
    mkdir "%ProgramFiles%\Gr�n2_Mitgliederverwaltung"
    xcopy /s /y "%~dp0*" "%ProgramFiles%\Gr�n2_Mitgliederverwaltung\"
    del "%ProgramFiles%\Gr�n2_Mitgliederverwaltung\Gr�n2.VisualElementsManifest.xml"

    ::create startmenuentries
    mkdir "%AppData%\Microsoft\Windows\Start Menu\Programs\Gr�n2 Mitgliederverwaltung"
    call :createLink "Gr�n2 starten" "%ProgramFiles%\Gr�n2_Mitgliederverwaltung" "Gr_n2_Launcher.jar"
    call :createLink "Gr�n2 deinstallieren" "%ProgramFiles%\Gr�n2_Mitgliederverwaltung" "uninstall.bat"
    call :createLink "Gr�n2 konfigurieren" "%AppData%\Gr�n2_Mitgliederverwaltung" "Gr�n2.conf"

    echo Programm wurde installiert
) else (
    call :getAdminrights

    xcopy /s /y "%~dp0*" "%ProgramFiles%\Gr�n2_Mitgliederverwaltung"
    move /y "%ProgramFiles%\Gr�n2_Mitgliederverwaltung\Gr�n2.VisualElementsManifest.xml" "%AppData%\Gr�n2_Mitgliederverwaltung"
    echo.
    echo Programm wurde aktualisiert
)
del "%ProgramFiles%\Gr�n2_Mitgliederverwaltung\Gr�n2.conf"

goto:ENDE


:--------------------------------------

:createLink
set SCRIPT="%TEMP%\%RANDOM%-%RANDOM%-%RANDOM%-%RANDOM%.vbs"

echo Set oWS = WScript.CreateObject("WScript.Shell") >> %SCRIPT%
echo sLinkFile = "%AppData%\Microsoft\Windows\Start Menu\Programs\Gr�n2 Mitgliederverwaltung\%~1.lnk" >> %SCRIPT%
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> %SCRIPT%
echo oLink.TargetPath = "%~2\%~3" >> %SCRIPT%
echo oLink.WorkingDirectory = "%~2" >> %SCRIPT%
echo oLink.IconLocation = "%ProgramFiles%\Gr�n2_Mitgliederverwaltung\icon.ico" >> %SCRIPT%
echo oLink.Save >> %SCRIPT%

cscript /nologo %SCRIPT%
del %SCRIPT%
goto:eof


:getAdminrights
REM  --> Check for permissions
>nul 2>&1 "%SYSTEMROOT%\system32\icacls.exe" "%SYSTEMROOT%\system32\config\system"

REM --> If error flag set, we do not have admin.
if '%errorlevel%' NEQ '0' (
    echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
    set params = %*:"=""
    echo UAC.ShellExecute "cmd.exe", "/c %~s0 %params%", "", "runas", 1 >> "%temp%\getadmin.vbs"

    "%temp%\getadmin.vbs"
    del "%temp%\getadmin.vbs"
)
goto:eof

:ENDE