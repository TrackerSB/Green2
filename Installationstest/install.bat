@echo off
chcp 1252

set ProgramFilesPath=%ProgramFiles%\Gr�n2_Mitgliederverwaltung\
set AppDataPath=%AppData%\Gr�n2_Mitgliederverwaltung\
set MenuEntryFolderPath=%AppData%\Microsoft\Windows\Start Menu\Programs\Gr�n2 Mitgliederverwaltung\

if not exist "%AppDataPath%" (
    mkdir "%AppDataPath%"
)

if not exist "%ProgramFilesPath%" (
    mkdir "%ProgramFilesPath%"
)

cscript /nologo "%~dp0install.vbs"

goto :eof