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

# Remove program folder and shortcuts of previous versions and this version
$SudoCommand -c "rm -r /opt/Grün2_Mitgliederverwaltung;
rm -r $ProgramFolder;
rm -r $MenuEntryFolder/Grün_*;"

# Move configurations of previous versions
$SudoCommand -c "mkdir $ConfigDir;
mv $HOME/.Grün2_Mitgliederverwaltung/* $ConfigDir;
rm -r $HOME/.Grün2_Mitgliederverwaltung;"

# Create program folder
$SudoCommand -c "mkdir $ProgramFolder;
mv -f $TempDir/*.desktop $MenuEntryFolder/;
mv -f $TempDir/* $ProgramFolder/;
rm $ProgramFolder/*.xml;
rm $ProgramFolder/*.bat;
rm $ProgramFolder/*.vbs;
touch $TempDir/installed;"

echo "Green2 was installed/updated"
