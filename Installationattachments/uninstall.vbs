'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute """" & WScript.FullName & """", """" & WScript.ScriptFullName & """ /elevate", "", "runas", 1
  WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Green2"
menuEntryFolderPath = oWS.SpecialFolders("AllUsersPrograms") & "\Green2"
versionFilePath = oWS.ExpandEnvironmentStrings("%AppData%") & "\Green2\version.txt"

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