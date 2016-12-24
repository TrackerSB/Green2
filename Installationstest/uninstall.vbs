'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute """" & WScript.FullName & """", """" & WScript.ScriptFullName & """ /elevate", "", "runas", 1
  WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Grün2_Mitgliederverwaltung"
menuEntryFolderPath = oWS.SpecialFolders("AllUsersPrograms") & "\Grün2 Mitgliederverwaltung"
versionFilePath = oWS.ExpandEnvironmentStrings("%AppData%") & "\Grün2_Mitgliederverwaltung\version.txt"

'Delete folder but configs (but version file)
If fso.FileExists(versionFilePath) Then
    fso.DeleteFile(versionFilePath)
End If
If fso.FolderExists(programFilesPath) Then
    fso.DeleteFolder programFilesPath
End If
If fso.FolderExists(menuEntryFolderPath) Then
    fso.DeleteFolder menuEntryFolderPath
End If