'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute """" & WScript.FullName & """", """" & WScript.ScriptFullName & """ /elevate", "", "runas", 1
  WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Grün2_Mitgliederverwaltung"
menuEntryFolderPath = oWS.SpecialFolders("AllUsersPrograms") & "\Grün2 Mitgliederverwaltung"

'Delete folder but configs
If fso.FolderExists(programFilesPath) Then
    fso.DeleteFolder programFilesPath
End If
If fso.FolderExists(menuEntryFolderPath) Then
    fso.DeleteFolder menuEntryFolderPath
End If