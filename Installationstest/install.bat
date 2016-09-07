@echo off
chcp 1252

set ProgramFilesPath=%ProgramFiles%\Grün2_Mitgliederverwaltung\
set AppDataPath=%AppData%\Grün2_Mitgliederverwaltung\
set MenuEntryFolderPath=%AppData%\Microsoft\Windows\Start Menu\Programs\Grün2 Mitgliederverwaltung\

if not exist "%AppDataPath%" (
    mkdir "%AppDataPath%"
)

if not exist "%ProgramFilesPath%" (
    mkdir "%ProgramFilesPath%"
)

cscript /nologo "%~dp0install.vbs"

goto :eof


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
goto :eof