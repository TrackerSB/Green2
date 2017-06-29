#!/bin/bash -x

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

#Delete system preferences (legacy since 2u13)
IFS=$'\n' read -ra preferencesDirParts <<< $(java -jar PreferencesHelper.jar delete)
preferencesBaseDir=${preferencesDirParts[0]}
preferencesSubDir=${preferencesDirParts[1]}
preferencesDir="$preferencesBaseDir$preferencesSubDir"
$SudoCommand -c "rm -r \"$preferencesDir\";

#Delete application shortcuts
rm /usr/share/applications/Green2_*.desktop;

#Delete folders
rm -r \"/opt/Green2\""
rm -r "$HOME/.Green2/"
