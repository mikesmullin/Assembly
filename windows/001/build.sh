#!/usr/bin/env bash
export PATH="${PATH}:/c/Program Files (x86)/Microsoft Visual Studio 14.0/VC/bin/amd64/"
export PATH="${PATH}:/c/Program Files (x86)/Windows Kits/10/Debuggers/x64/"
# set PATH=%PATH%;C:\Program Files (x86)\Windows Kits\10\Debuggers\x64\
rm -f a.exe test.obj
"C:\Users\Mike\AppData\Local\bin\NASM\nasm.exe" -f win64 "F:\Desktop\tmp\winasm-2\001\test.nasm" && \
ld -s test.obj "C:\WINDOWS\System32\kernel32.dll" && \
#gcc test.obj
"C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\bin\amd64\dumpbin.exe" -disasm a.exe
#"C:\Program Files (x86)\Windows Kits\10\Debuggers\x64\windbg.exe" a.exe
ls -l a.exe