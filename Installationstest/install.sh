#!/bin/sh

TempDir=$(dirname $0)
ConfigDir="$HOME/.Grün2_Mitgliederverwaltung"
ProgramFolder="/opt/Grün2_Mitgliederverwaltung"

mkdir "$ConfigDir"
sudo mkdir $ProgramFolder

# Move config file
cp $TempDir/Grün2.conf $ConfigDir/

# Move program itself
sudo cp -r $TempDir/* $ProgramFolder/

# Remove xml and bat files which are only used in windows
sudo rm $ProgramFolder/*.xml
sudo rm $ProgramFolder/*.bat

echo "Grün2 wurde installiert/aktualisiert"
