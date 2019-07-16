; -------------------------------
; Start
 
 
  !define PRODUCT "Maven Unbound"
  !define MUI_FILE "unbound"
  !define VERSION "1.0.0"
  !define MUI_BRANDINGTEXT "Maven Unbound 1.0.0"
  CRCCheck On
 
  !include "${NSISDIR}\Contrib\Modern UI\System.nsh"
 
 
;---------------------------------
;General
 
;  OutFile "unbound-1.0.0.exe"
  ShowInstDetails "nevershow"
  ShowUninstDetails "nevershow"
  ;SetCompressor "bzip2"
 
;  !define MUI_ICON "icon.ico"
;  !define MUI_UNICON "icon.ico"
;  !define MUI_SPECIALBITMAP "Bitmap.bmp"
 
 
;--------------------------------
;Folder selection page
 
  InstallDir "$PROGRAMFILES\${PRODUCT}"
 
 
;--------------------------------
;Modern UI Configuration
 
  !define MUI_WELCOMEPAGE  
  !define MUI_LICENSEPAGE
  !define MUI_DIRECTORYPAGE
  !define MUI_ABORTWARNING
  !define MUI_UNINSTALLER
  !define MUI_UNCONFIRMPAGE
  !define MUI_FINISHPAGE  
 
 
;--------------------------------
;Language
 
  !insertmacro MUI_LANGUAGE "English"
 
 
;-------------------------------- 
;Modern UI System
 
;  !insertmacro MUI_SYSTEM 
 
 
;--------------------------------
;Data
 
  LicenseData "../LICENSE"
 
 
;-------------------------------- 
;Installer Sections     
Section "Install"
 
  ;Add files
  SetOutPath "$INSTDIR"
 
  File "PathEd.exe"
  File "..\\LICENSE"
  File "..\\mvnu.bat"
  File "..\\target\\unbound-1.0.0-exec.jar"
 
  ; Add the installation folder to the system PATH -> to enable grr.exe
  ExecWait '$INSTDIR\PathEd.exe add "$INSTDIR"' ; put the path in quotes because of possible spaces
 
  ; write uninstall information to the registry
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "DisplayName" "${PRODUCT} (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}" "UninstallString" "$INSTDIR\Uninstall.exe"
 
  WriteUninstaller "$INSTDIR\Uninstall.exe"
 
SectionEnd
 
 
;--------------------------------    
;Uninstaller Section  
Section "Uninstall"

  ; Remove the installation folder from the system PATH -> was required for grr.exe
  ExecWait '$INSTDIR\PathEd.exe remove "$INSTDIR"' 
 
  ; Delete Files 
  RMDir /r "$INSTDIR\*.*"    
 
  ; Remove the installation directory
  RMDir "$INSTDIR"
 
  ; Delete Uninstaller And Unistall Registry Entries
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\${PRODUCT}"
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT}"  
 
SectionEnd
 
 
;--------------------------------    
;MessageBox Section
 
 
;Function that calls a messagebox when installation finished correctly
Function .onInstSuccess
  MessageBox MB_OK "You have successfully installed ${PRODUCT}. Use the desktop icon to start the program."
FunctionEnd
 
 
Function un.onUninstSuccess
  MessageBox MB_OK "You have successfully uninstalled ${PRODUCT}."
FunctionEnd
 
 
;eof
