#!/bin/bash -x

#Delete system preferences
IFS=$'\n' read -ra preferencesDirParts <<< $(java -jar PreferencesHelper.jar delete)
preferencesBaseDir=${preferencesDirParts[0]}
preferencesSubDir=${preferencesDirParts[1]}
preferencesDir="$preferencesBaseDir$preferencesSubDir"
sudo rm -r "$preferencesDir"

#Delete application shortcuts
sudo rm /usr/share/applications/Green2_*.desktop

#Delete folders
sudo rm -r "/opt/Green2"
rm -r "$HOME/.Green2/"
