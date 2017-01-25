#!/bin/bash

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
ConfigDir="$HOME/.Green2"
ProgramFolder="/opt/Green2"
MenuEntryFolder="/usr/share/applications"

# Create program folder
$SudoCommand -c "mkdir $ProgramFolder;
mv -f $TempDir/*.desktop $MenuEntryFolder/;
mv -f $TempDir/* $ProgramFolder/;
rm $ProgramFolder/*.xml;
rm $ProgramFolder/*.bat;
rm $ProgramFolder/*.vbs;
touch $TempDir/installed;"

echo "Grün2 wurde installiert/aktualisiert"
