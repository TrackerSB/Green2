'Check number of parameters
If WScript.Arguments.Count < 2 Then
    WScript.Echo "There are parameters missing: 0=HKCU of Green2, 1=delete configs true/false"
End If

'Request admin rights
If Not WScript.Arguments.Named.Exists("elevate") Then
    CreateObject("Shell.Application").ShellExecute """" & WScript.FullName & """", """" & WScript.ScriptFullName & """ " & WScript.Arguments(0) & " " & WScript.Arguments(1) & " /elevate", "", "runas", 1
    WScript.Quit
End If

Set oWS = CreateObject("WScript.Shell")
With oWS
    programFilesPath = .ExpandEnvironmentStrings("%ProgramFiles%") & "\Green2"
    menuEntryFolderPath = .SpecialFolders("AllUsersPrograms") & "\Green2"
    appDataPath = .ExpandEnvironmentStrings("%AppData%") & "\Green2"
    versionFilePath = appDataPath & "\version.txt" 'Still legacy reason
    
    'Delete Registry keys
    .RegDelete WScript.Arguments(0) & "\"
End With

'Delete folder but configs
Set fso = CreateObject("Scripting.FileSystemObject")
With fso
    If .FileExists(versionFilePath) Then 'Still legacy reason
        .DeleteFile(versionFilePath)
    End If
    If StrComp(WScript.Arguments(1), "true", 1) = 0 Then
        If .FolderExists(appDataPath) Then
            .DeleteFolder appDataPath
        End If
    End If
    If .FolderExists(programFilesPath) Then
        .DeleteFolder programFilesPath
    End If
    If .FolderExists(menuEntryFolderPath) Then
        .DeleteFolder menuEntryFolderPath
    End If
End With

'Get the directory of the uninstall script (May not be the current directory)
downloadedDir = Split(WScript.ScriptFullName, WScript.ScriptName)(0)