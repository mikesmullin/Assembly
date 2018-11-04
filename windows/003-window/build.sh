#!/usr/bin/env bash
export PATH="${PATH}:/c/Program Files (x86)/Microsoft Visual Studio 14.0/VC/bin/amd64/"
export PATH="${PATH}:/c/Program Files (x86)/Windows Kits/10/Debuggers/x64/"
export PATH="${PATH}:/c/Users/Mike/AppData/Local/bin/NASM/"
export PATH="${PATH}:/c/Program Files/PellesC/Bin/"
# set PATH=%PATH%;C:\Program Files (x86)\Windows Kits\10\Debuggers\x64\
rm -f a.exe test.obj
"C:\Users\Mike\AppData\Local\bin\NASM\nasm.exe" -f win64 "F:\Desktop\tmp\winasm-2\003-opengl\test.nasm" -l "F:\Desktop\tmp\winasm-2\003-opengl\test.lst" -o "F:\Desktop\tmp\winasm-2\003-opengl\test.obj"
#ld -s test.obj "C:\WINDOWS\System32\kernel32.dll" "C:\WINDOWS\System32\user32.dll" -o "F:\Desktop\tmp\winasm-2\003-opengl\test.exe"
#ld -s test.obj -L"C:\Program Files\PellesC\Lib\Win64" kernel32.lib user32.lib -o "F:\Desktop\tmp\winasm-2\003-opengl\test.exe"
ld -s test.obj "C:\Program Files\PellesC\Lib\Win64\kernel32.lib" "C:\Program Files\PellesC\Lib\Win64\user32.lib" -o "F:\Desktop\tmp\winasm-2\003-opengl\test.exe"
#"C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\bin\amd64\dumpbin.exe" -disasm test.exe
#"C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\bin\amd64\dumpbin.exe" -section:.data -rawdata:8 test.exe
#"C:\Program Files (x86)\Windows Kits\10\Debuggers\x64\windbg.exe" test.exe
ls -l test.exe

#polink /ENTRY:main /SUBSYSTEM:WINDOWS /LIBPATH:"C:\Program Files\PellesC\Lib\Win64" kernel32.lib user32.lib test.obj
