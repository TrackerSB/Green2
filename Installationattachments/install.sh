#!/bin/bash -x

if [[ $# -lt 1 ]]; then
    (>&2 echo "You have to specify the version to set after install.")
fi

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
$SudoCommand -c "mkdir -p $ConfigDir;
mv $HOME/.Grün2_Mitgliederverwaltung/* $ConfigDir;
rm -r $HOME/.Grün2_Mitgliederverwaltung;"

# Create program folder
$SudoCommand -c "mkdir -p $ProgramFolder;
mv -f $TempDir/*.desktop $MenuEntryFolder/;
mv -f $TempDir/* $ProgramFolder/;
rm $ProgramFolder/*.xml;
rm $ProgramFolder/*.bat;
rm $ProgramFolder/*.vbs;"

# Update version saved in Java preferences
IFS=$'\n' read -ra preferencesDirParts <<< $(java -jar PreferencesHelper.jar)
preferencesDir="${preferencesDirParts[0]}${preferencesDirParts[1]}"
preferencesPath="$preferencesDir/prefs.xml"
$SudoCommand -c "mkdir -p $preferencesDir;
echo '<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>
<!DOCTYPE map SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">
<map MAP_XML_VERSION=\"1.0\">
  <entry key=\"version\" value=\"$1\"/>
</map>' > $preferencesPath;"

# Create install successful file
$SudoCommand -c "touch $TempDir/installed;"

echo "Green2 was installed/updated"
