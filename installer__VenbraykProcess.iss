#define MyAppName "Venbrayk Process"
#define MyAppVersion "0.1.0"
#define MyAppPublisher "Venbrayk Technology Solutions"
#define MyAppExeName "Venbrayk.Process.Controller.exe"

[Setup]
AppId={{1C02BCB2-6375-4F85-BA13-9305D95B6E31}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\Venbrayk Process
DefaultGroupName={#MyAppName}
OutputDir=..\artifacts
OutputBaseFilename=Venbrayk-Process-Setup-v0.1.0
Compression=lzma2/ultra64
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=lowest
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
SetupLogging=yes
UninstallDisplayName={#MyAppName}

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Files]
Source: "..\publish\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Criar atalho na área de trabalho"; GroupDescription: "Atalhos adicionais:"

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Abrir o Venbrayk Process"; Flags: nowait postinstall skipifsilent

