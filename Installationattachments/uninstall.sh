#!/bin/bash -x

if [ "$#" -lt 2 ]; then
    >&2 echo "You have to provide two arguments: 1. The preferences path; 2. true or false to specify whether to delete configurations"
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

# Delete application
$SudoCommand -c "
#Delete application shortcuts
rm /usr/share/applications/Green2_*.desktop;

#Delete folders
rm -r \"/opt/Green2\""

# Delete user preferences
rm -r "$1"
echo "HERE"
echo "$1"

# Possibly delete configurations
if [[ "$2" == "true" ]]; then
    echo "HERE 2"
    echo "$2"
    rm -r "$HOME/.Green2/"
fi
