Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
Set args = WScript.Arguments
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Grün2_Mitgliederverwaltung\"
appDataPath = oWS.ExpandEnvironmentStrings("%AppData%") & "\Grün2_Mitgliederverwaltung\"

'Create program folder if needed
If NOT fso.FolderExists(programFilesPath) Then
	fso.CreateFolder(programFilesPath)
End If

'Copy createLink.vbs to temp
tempCreateLink = oWS.ExpandEnvironmentStrings("%TEMP%") & "createLink.vbs"

'Move files

'Create start menu entries
oWS.Run "tempCreateLink ""Grün2 starten"" programFilesPath Grün2_Launcher.jar"
oWS.Run "tempCreateLink ""Grün2 deinstallieren"" programFilesPath uninstall.bat"
oWS.Run "tempCreateLink ""Grün2 konfigurieren"" appDataPath Grün2.conf"