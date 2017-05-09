#!/bin/sh

#Delete system preferences
IFS=$'\n' read -ra preferencesDirParts <<< $(java -jar PreferencesHelper.jar delete)
preferencesBaseDir = ${preferencesDirParts[0]}
preferencesSubDir = ${preferencesDirParts[1]}
preferencesDir = preferencesBaseDir + preferencesSubDir
sudo rm -r "$preferencesDir"

#Delete folders
sudo rm -r "/opt/Green2"
rm -r "$HOME/.Green2"
