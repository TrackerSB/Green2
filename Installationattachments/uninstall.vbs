'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute """" & WScript.FullName & """", """" & WScript.ScriptFullName & """ /elevate", "", "runas", 1
  WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Green2"
menuEntryFolderPath = oWS.SpecialFolders("AllUsersPrograms") & "\Green2"
versionFilePath = oWS.ExpandEnvironmentStrings("%AppData%") & "\Green2\version.txt" 'Still legacy reason

'Delete folder but configs (but version file)
With fso
    If .FileExists(versionFilePath) Then 'Still legacy reason
        .DeleteFile(versionFilePath)
    End If
    If .FolderExists(programFilesPath) Then
        .DeleteFolder programFilesPath
    End If
    If .FolderExists(menuEntryFolderPath) Then
        .DeleteFolder menuEntryFolderPath
    End If
End With

'Get the directory of the uninstall script (May not be the current directory)
downloadedDir = Split(WScript.ScriptFullName, WScript.ScriptName)(0)