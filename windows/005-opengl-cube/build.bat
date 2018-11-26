@echo off
REM NOTICE: You'll need to update all paths below to match your system.

REM You may choose to install some dependencies:
REM - Microsoft Windows 10 (64-bit)
REM - Microsoft Visual Studio Community Edition (latest or 2017)
REM - NASM (latest or v2.14)
REM - PellesC (latest or polink v9.00.3)
REM - IntelliJ (my choice for editing in this case)
REM - Cygwin, or MinGW shell

REM This script format assumes you're running it from MinGW terminal (ie. VSCode)
REM but the Ubuntu on Windows shell might work, as well.

REM i keep these here because i like a quick copy-paste into shell to load my dev env
REM Cygwin/MinGW:
REM export PATH="${PATH}:/c/Program Files (x86)/Microsoft Visual Studio 14.0/VC/bin/amd64/"
REM export PATH="${PATH}:/c/Program Files (x86)/Windows Kits/10/Debuggers/x64/"
REM export PATH="${PATH}:/c/Users/Mike/AppData/Local/bin/NASM/"
REM export PATH="${PATH}:/c/Program Files/PellesC/Bin/"
REM cmd.exe:
REM set PATH=%PATH%;C:\Program Files (x86)\Windows Kits\10\Debuggers\x64\


echo clean old build output
del "build\test.exe" "build\test.obj" "build\test.lst"
if %errorlevel% neq 0 exit

echo use NASM to compile intermediary binary (*.obj)
"C:\Users\Mike\AppData\Local\bin\NASM\nasm.exe" ^
	-f win64 "build\test.nasm" ^
	-l "build\test.lst" ^
	-o "build\test.obj"
if %errorlevel% neq 0 exit

echo statically link binary and external libraries into final (PE) executable (*.exe)
"C:\Program Files\mingw-w64\x86_64-6.1.0-posix-seh-rt_v5-rev0\mingw64\bin\ld.exe" ^
  -s "build\test.obj" ^
	"C:\Program Files\PellesC\Lib\Win64\kernel32.lib" ^
	"C:\Program Files\PellesC\Lib\Win64\user32.lib" ^
	"C:\Program Files\PellesC\Lib\Win64\gdi32.lib" ^
	-o "build\test.exe"
if %errorlevel% neq 0 exit

echo list output file size, in bytes
dir build

echo run the program
"build\test.exe"

echo errorlevel: %errorlevel%