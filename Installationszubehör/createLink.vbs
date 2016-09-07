'usage: createLink.vbs <linkname> <workingdir> <file of workingdir>

Set oWS = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
Set args = WScript.Arguments
programFilesPath = oWS.ExpandEnvironmentStrings("%ProgramFiles%") & "\Grün2_Mitgliederverwaltung"
menuEntryFolderPath = oWS.SpecialFolders("AllUsersPrograms") & "\Grün2 Mitgliederverwaltung"
workingDir = fso.GetAbsolutePathName(args(1)) & "\"

'create start menu entry folder if needed
If NOT fso.FolderExists(menuEntryFolderPath) Then
	fso.CreateFolder(menuEntryFolderPath)
End If

'create the link itself
sLinkFile = menuEntryFolderPath & "\" & args(0) & ".lnk"
Set oLink = oWS.CreateShortcut(sLinkFile)
oLink.TargetPath = workingDir & args(2)
oLink.WorkingDirectory = workingDir
oLink.IconLocation = programFilesPath & "\icon.ico"
oLink.Save
