#!/usr/bin/env bash
#set -x

# NOTICE: You'll need to update all paths below to match your system.

# You may choose to install some dependencies:
# - Microsoft Windows 10 (64-bit)
# - Microsoft Visual Studio Community Edition (latest or 2017)
# - NASM (latest or v2.14)
# - PellesC (latest or polink v9.00.3)
# - IntelliJ (my choice for editing in this case)
# - Cygwin, or MinGW shell

# This script format assumes you're running it from MinGW terminal (ie. VSCode)
# but the Ubuntu on Windows shell might work, as well.

# i keep these here because i like a quick copy-paste into shell to load my dev env
# Cygwin/MinGW:
export PATH="${PATH}:/c/Program Files (x86)/Microsoft Visual Studio 14.0/VC/bin/amd64/"
export PATH="${PATH}:/c/Program Files (x86)/Windows Kits/10/Debuggers/x64/"
export PATH="${PATH}:/c/Users/Mike/AppData/Local/bin/NASM/"
export PATH="${PATH}:/c/Program Files/PellesC/Bin/"
# cmd.exe:
# set PATH=%PATH%;C:\Program Files (x86)\Windows Kits\10\Debuggers\x64\

# clean old build out
rm -f test.exe test.obj

# use NASM to compile intermediary binary (*.obj)
"C:\Users\Mike\AppData\Local\bin\NASM\nasm.exe" \
	-f win64 "F:\Desktop\tmp\winasm-2\Assembly\windows\005-opengl-cube\build\test.nasm" \
	-l "F:\Desktop\tmp\winasm-2\Assembly\windows\005-opengl-cube\build\test.lst" \
	-o "F:\Desktop\tmp\winasm-2\Assembly\windows\005-opengl-cube\build\test.obj"

# statically link binary and external libraries into final (PE) executable (*.exe)
ld -s "F:\Desktop\tmp\winasm-2\Assembly\windows\005-opengl-cube\build\test.obj" \
	"C:\Program Files\PellesC\Lib\Win64\kernel32.lib" \
	"C:\Program Files\PellesC\Lib\Win64\user32.lib" \
	"C:\Program Files\PellesC\Lib\Win64\gdi32.lib" \
	-o "F:\Desktop\tmp\winasm-2\Assembly\windows\005-opengl-cube\build\test.exe"

# list output file size, in bytes
ls -l "F:\Desktop\tmp\winasm-2\Assembly\windows\005-opengl-cube\build\test.exe"

