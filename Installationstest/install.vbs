Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
Set args = WScript.Arguments
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Gr�n2_Mitgliederverwaltung\"
appDataPath = oWS.ExpandEnvironmentStrings("%AppData%") & "\Gr�n2_Mitgliederverwaltung\"

'Create program folder if needed
If NOT fso.FolderExists(programFilesPath) Then
	fso.CreateFolder(programFilesPath)
End If

'Copy createLink.vbs to temp
tempCreateLink = oWS.ExpandEnvironmentStrings("%TEMP%") & "createLink.vbs"

'Move files

'Create start menu entries
oWS.Run "tempCreateLink ""Gr�n2 starten"" programFilesPath Gr�n2_Launcher.jar"
oWS.Run "tempCreateLink ""Gr�n2 deinstallieren"" programFilesPath uninstall.bat"
oWS.Run "tempCreateLink ""Gr�n2 konfigurieren"" appDataPath Gr�n2.conf"