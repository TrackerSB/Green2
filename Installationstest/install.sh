#!/bin/sh

# Determine sudo command
KdeSudoExists=$(command -v kdesudo >/dev/null)
GkSudoExists=$(command -v gksudo >/dev/null)
if ! ($KdeSudoExists || $GkSudoExists)
then
    >&2 echo "You have to install kdesudo or gksudo"
    return 1
else
    if $KdeSudoExists
    then
        SudoCommand=kdesudo
    else
        SudoCommand=gksudo
    fi
fi

# Set variables
TempDir=$(dirname $0)
ConfigDir="$HOME/.Grün2_Mitgliederverwaltung"
ProgramFolder="/opt/Grün2_Mitgliederverwaltung"
MenuEntryFolder="/usr/share/applications"

# Move user settings
mkdir "$ConfigDir"
mv "$TempDir/Grün2.conf" $ConfigDir/

# Create program folder
$SudoCommand -c "mkdir $ProgramFolder;
mv $TempDir/*.desktop $MenuEntryFolder/;
cp -r $TempDir/* $ProgramFolder/;
rm $ProgramFolder/*.xml;
rm $ProgramFolder/*.bat;"

echo "Grün2 wurde installiert/aktualisiert"
