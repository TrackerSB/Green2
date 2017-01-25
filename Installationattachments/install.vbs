'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute """" & WScript.FullName & """", """" & WScript.ScriptFullName & """ /elevate", "", "runas", 1
  WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Green2"
menuEntryFolderPath = oWS.SpecialFolders("AllUsersPrograms") & "\Green2"

'Get the directory of the install script (May not be the current directory)
downloadedDir = Split(WScript.ScriptFullName, WScript.ScriptName)(0)

'Delete files which are not needed for Windows
fso.DeleteFile downloadedDir & "*.sh"
fso.DeleteFile downloadedDir & "*.desktop"

'Create program folder if needed
If NOT fso.FolderExists(programFilesPath) Then
	fso.CreateFolder(programFilesPath)
End If

'Copy createLink.vbs to temp
tempCreateLink = oWS.ExpandEnvironmentStrings("%TEMP%") & "\createLink.vbs"
With fso
    'Move lib folder
    libpath = programFilesPath & "\lib"
    If NOT .FolderExists(libpath) Then
        .CreateFolder(libpath)
    End If
    .CopyFile downloadedDir & "lib\*.*", libpath
    .DeleteFolder downloadedDir & "lib"
    
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