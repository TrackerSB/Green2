'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute WScript.FullName, WScript.ScriptFullName & " /elevate", "", "runas", 1
  WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Grün2_Mitgliederverwaltung"

'Delete files which are not needed for Windows
fso.DeleteFile "*.sh"
fso.DeleteFile "*.desktop"

'Create program folder if needed
If NOT fso.FolderExists(programFilesPath) Then
	fso.CreateFolder(programFilesPath)
End If

'Copy createLink.vbs to temp
tempCreateLink = oWS.ExpandEnvironmentStrings("%TEMP%") & "\createLink.vbs"
With fso
    .CopyFile "createLink.vbs", tempCreateLink, True
    
    'Move lib folder
    libpath = programFilesPath & "\lib"
    If NOT fso.FolderExists(libpath) Then
        fso.CreateFolder(libpath)
    End If
    fso.CopyFile "lib\*.*", libpath
    fso.DeleteFolder "lib"
    
    'Move files (including this file itself)
    .CopyFile "*.*", programFilesPath, True
    .DeleteFile "*.*"
End With

'Create start menu entries
oWS.Run tempCreateLink & " ""Grün2 starten"" """ & programFilesPath & """ Grün2_Launcher.jar"
oWS.Run tempCreateLink & " ""Grün2 deinstallieren"" """ & programFilesPath & """ uninstall.bat"
oWS.Run tempCreateLink & " ""Grün2 konfigurieren"" """ & programFilesPath & """ Grün2_config.jar"