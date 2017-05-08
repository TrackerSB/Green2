'Check parameter count
If WScript.Arguments.Count < 1 Then
    WScript.Echo "Parameter holding the version to set is missing"
    WScript.Quit
End If

'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute """" & WScript.FullName & """", """" & WScript.ScriptFullName & """ /elevate " & WScript.Arguments(0), "", "runas", 1
  WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
With oWS
    programFilesPath = .ExpandEnvironmentStrings("%ProgramFiles%") & "\Green2"
    menuEntryFolderPath = .SpecialFolders("AllUsersPrograms") & "\Green2"
    configFolderPath = .ExpandEnvironmentStrings("%AppData%") & "\Green2"
    oldProgramFilesPath = .ExpandEnvironmentStrings("%ProgramFiles%") & "\Grün2_Mitgliederverwaltung"
    oldMenuEntryFolderPath = .SpecialFolders("AllUsersPrograms") & "\Grün2_Mitgliederverwaltung"
    oldConfigFolderPath = .ExpandEnvironmentStrings("%AppData%") & "\Grün2_Mitgliederverwaltung"
End With

'Get the directory of the install script (May not be the current directory)
downloadedDir = Split(WScript.ScriptFullName, WScript.ScriptName)(0)

Set jarExec = oWS.Exec("java -jar " & downloadedDir & "PreferencesHelper.jar")
registryBasePath = jarExec.StdOut.ReadLine
registrySubkeyPath = jarExec.StdOut.ReadLine

Set fso = CreateObject("Scripting.FileSystemObject")
With fso
    'Delete files which are not needed for Windows
    .DeleteFile downloadedDir & "*.sh"
    .DeleteFile downloadedDir & "*.desktop"

    'Delete start menu entries and program files of previous versions
    If .FolderExists(oldProgramFilesPath) Then
        .DeleteFolder oldProgramFilesPath
    End If
    If .FolderExists(oldMenuEntryFolderPath) Then
        .DeleteFolder oldMenuEntryFolderPath
    End If

    'Move configurations to new folder
    If .FolderExists(oldConfigFolderPath) Then
        If NOT .FolderExists(configFolderPath) Then
            .CreateFolder(configFolderPath)
        End If
        .CopyFile oldConfigFolderPath & "\*.*", configFolderPath
        .DeleteFolder oldConfigFolderPath
    End If

    'Create program folder if needed
    If NOT .FolderExists(programFilesPath) Then
        .CreateFolder(programFilesPath)
    End If


    'Move lib folder
    libpath = programFilesPath & "\lib"
    If NOT .FolderExists(libpath) Then
        .CreateFolder(libpath)
    End If
    .CopyFile downloadedDir & "lib\*.*", libpath
    .DeleteFolder downloadedDir & "lib"
    
    'Move licences folder
    licencesPath = programFilesPath & "\licenses"
    If NOT .FolderExists(licencesPath) Then
        .CreateFolder(licencesPath)
    End If
    .CopyFile downloadedDir & "licenses\*.*", licencesPath
    .DeleteFolder downloadedDir & "licenses"
    
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

'Set version in registry
oWS.RegWrite registryBasePath & registrySubkeyPath & "\version", WScript.Arguments(1), "REG_SZ"

'Create file to show success
fso.CreateTextFile(downloadedDir & "installed")