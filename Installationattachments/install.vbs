'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute """" & WScript.FullName & """", """" & WScript.ScriptFullName & """ /elevate", "", "runas", 1
  WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Green2"
menuEntryFolderPath = oWS.SpecialFolders("AllUsersPrograms") & "\Green2"
configFolderPath = oWS.ExpandEnvironmentStrings("%AppData%") & "\Green2"
oldProgramFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Grün2_Mitgliederverwaltung"
oldMenuEntryFolderPath = oWS.SpecialFolders("AllUsersPrograms") & "\Grün2_Mitgliederverwaltung"
oldConfigFolderPath = oWS.ExpandEnvironmentStrings("%AppData%") & "\Grün2_Mitgliederverwaltung"

'Get the directory of the install script (May not be the current directory)
downloadedDir = Split(WScript.ScriptFullName, WScript.ScriptName)(0)

'Delete files which are not needed for Windows
fso.DeleteFile downloadedDir & "*.sh"
fso.DeleteFile downloadedDir & "*.desktop"

'Delete start menu entries and program files of previous versions
If fso.FolderExists(oldProgramFilesPath) Then
    fso.DeleteFolder oldProgramFilesPath
End If
If fso.FolderExists(oldMenuEntryFolderPath) Then
    fso.DeleteFolder oldMenuEntryFolderPath
End If

'Move configurations to new folder
If fso.FolderExists(oldConfigFolderPath) Then
    If NOT fso.FolderExists(configFolderPath) Then
        fso.CreateFolder(configFolderPath)
    End If
    fso.CopyFile oldConfigFolderPath & "\*.*", configFolderPath
    fso.DeleteFolder oldConfigFolderPath
End If

'Create program folder if needed
If NOT fso.FolderExists(programFilesPath) Then
	fso.CreateFolder(programFilesPath)
End If

With fso
    'Move lib folder
    libpath = programFilesPath & "\lib"
    If NOT .FolderExists(libpath) Then
        .CreateFolder(libpath)
    End If
    .CopyFile downloadedDir & "lib\*.*", libpath
    .DeleteFolder downloadedDir & "lib"
    
    'Move licences folder
    licencesPath = programFilesPath & "\lib"
    If NOT .FolderExists(licencesPath) Then
        .CreateFolder(licencesPath)
    End If
    .CopyFile downloadedDir & "licences\*.*", licencesPath
    .DeleteFolder downloadedDir & "licences"
    
    'Move files (including this file itself)
    .CopyFile downloadedDir & "*.*", programFilesPath, True
    .DeleteFile downloadedDir & "*.*"
End With

'Create start menu entries
createLink "Grün2 starten", programFilesPath, "Launcher.jar"
createLink "Grün2 deinstallieren", programFilesPath, "uninstall.vbs"
createLink "Grün2 konfigurieren", programFilesPath, "ConfigurationDialog.jar"

Sub createLink(linkname, workingDir, fileOfWorkingDir)
    'create start menu entry folder if needed
    If NOT fso.FolderExists(menuEntryFolderPath) Then
        fso.CreateFolder(menuEntryFolderPath)
    End If

    'create the link itself
    sLinkFile = menuEntryFolderPath & "\" & linkname & ".lnk"
    With oWS.CreateShortcut(sLinkFile)
        .TargetPath = workingDir & "\" & fileOfWorkingDir
        .WorkingDirectory = workingDir
        .IconLocation = programFilesPath & "\icon.ico"
        .Save
    End With
End Sub

'Create file to show success
fso.CreateTextFile(downloadedDir & "installed")