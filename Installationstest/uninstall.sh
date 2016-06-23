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

$SudoCommand -c "rm -r /opt/Grün2_Mitgliederverwaltung;
rm $MenuEntryFolder/Grün2_*;
rm -r $HOME/.Grün2_Mitgliederverwaltung"
