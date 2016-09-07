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