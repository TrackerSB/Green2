Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
Set args = WScript.Arguments
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Gr�n2_Mitgliederverwaltung\"
appDataPath = oWS.ExpandEnvironmentStrings("%AppData%") & "\Gr�n2_Mitgliederverwaltung\"

'Delete files which are not needed for Windows
'fso.DeleteFile "*.sh"
'fso.DeleteFile "*.desktop"

'Create program folder if needed
If NOT fso.FolderExists(programFilesPath) Then
	fso.CreateFolder(programFilesPath)
End If

'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute WScript.FullName, WScript.ScriptFullName & " /elevate", "", "runas", 1
  WScript.Quit
End If

'Copy createLink.vbs to temp
tempCreateLink = oWS.ExpandEnvironmentStrings("%TEMP%") & "\createLink.vbs"
With fso
    .CopyFile "createLink.vbs", tempCreateLink, True
    
    'Move files
    .CopyFile "*.*", programFilesPath, True
    '.DeleteFile "*.*"
End With

'Create start menu entries
oWS.Run tempCreateLink & " ""Gr�n2 starten"" """ & programFilesPath & """ Gr�n2_Launcher.jar"
oWS.Run tempCreateLink & " ""Gr�n2 deinstallieren"" """ & programFilesPath & """ uninstall.bat"
oWS.Run tempCreateLink & " ""Gr�n2 konfigurieren"" """ & appDataPath & """ Gr�n2.conf"