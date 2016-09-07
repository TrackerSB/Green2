@echo off
chcp 1252

set ProgramFilesPath=%ProgramFiles%\Grün2_Mitgliederverwaltung\
set AppDataPath=%AppData%\Grün2_Mitgliederverwaltung\
set MenuEntryFolderPath=%AppData%\Microsoft\Windows\Start Menu\Programs\Grün2 Mitgliederverwaltung\

if not exist "%AppDataPath%" (
    mkdir "%AppDataPath%"
)

move /y "%~dp0Grün2.VisualElementsManifest.xml" "%AppDataPath%"
del "%~dp0*.sh"
del "%~dp0*.desktop"

if not exist "%ProgramFilesPath%" (
    move /y "%~dp0Grün2.conf" "%AppDataPath%"

    call :getAdminrights

    rem "install"
    mkdir "%ProgramFilesPath%"
    move /y "%~dp0*" "%ProgramFilesPath%"
    rem moving directories between drives is not possible
    xcopy /y "%~dp0lib" "%ProgramFilesPath%lib\"
    rd /s /q "%~dp0lib"

    rem create startmenuentries
    rem call :createLink "Grün2 starten" "%ProgramFilesPath%" "Grün2_Launcher.jar"
    rem call :createLink "Grün2 deinstallieren" "%ProgramFilesPath%" "uninstall.bat"
    rem call :createLink "Grün2 konfigurieren" "%AppDataPath%" "Grün2.conf"
	cscript /nologo createLink.vbs "Grün2 starten" "%ProgramFilesPath%" "Grün2_Launcher.jar"
	cscript /nologo createLink.vbs "Grün2 deinstallieren" "%ProgramFilesPath%" "uninstall.bat"
    cscript /nologo createLink.vbs "Grün2 konfigurieren" "%AppDataPath%" "Grün2.conf"

    echo.
    echo Programm wurde installiert
) else (
    del "%~dp0Grün2.conf"

    call :getAdminrights

    rem "update"
    move /y "%~dp0*" "%ProgramFilesPath%"
    rem moving directories between drives is not possible
    xcopy /y "%~dp0lib" "%ProgramFilesPath%lib\"
    rd /s /q "%~dp0lib"
    
    echo.
    echo Programm wurde aktualisiert
)

goto:ENDE

rem createLink <linkname> <workingdir> <file of workingdir>
:createLink
set SCRIPT="%TEMP%\%RANDOM%-%RANDOM%-%RANDOM%-%RANDOM%.vbs"

echo Set oWS = WScript.CreateObject("WScript.Shell") >> %SCRIPT%
echo sLinkFile = "%MenuEntryFolderPath%%~1.lnk" >> %SCRIPT%
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> %SCRIPT%
echo oLink.TargetPath = "%~2%~3" >> %SCRIPT%
echo oLink.WorkingDirectory = "%~2" >> %SCRIPT%
echo oLink.IconLocation = "%ProgramFilesPath%icon.ico" >> %SCRIPT%
echo oLink.Save >> %SCRIPT%

cscript /nologo %SCRIPT%
del %SCRIPT%
goto:eof


:getAdminrights
REM  --> Check for permissions
>nul 2>&1 "%SYSTEMROOT%\system32\icacls.exe" "%SYSTEMROOT%\system32\config\system"

set ADMINSCRIPT="%TEMP%\%RANDOM%-%RANDOM%-%RANDOM%-%RANDOM%.vbs"
REM --> If error flag set, we do not have admin.
if '%ERRORLEVEL%' NEQ '0' (
    echo Set UAC = CreateObject^("Shell.Application"^) > %ADMINSCRIPT%
    set params = %*:"=""
    echo UAC.ShellExecute "cmd.exe", "/c %~s0 %params%", "", "runas", 1 >> %ADMINSCRIPT%

    cscript /nologo %ADMINSCRIPT%
    del %ADMINSCRIPT%
)
goto:eof


:ENDE