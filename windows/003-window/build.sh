#!/usr/bin/env bash

# NOTICE: You'll need to update all paths below to match your system.

# You may choose to install some dependencies:
# - Microsoft Windows 10 (64-bit)
# - Microsoft Visual Studio Community Edition (latest or 2017)
# - NASM (latest or v2.14)
# - PellesC (latest or polink v9.00.3)
# - Visual Studio Code (my choice for editing in this case)
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
# I use NASM because its better documented and more open/free/modern, likely to survive.
"C:\Users\Mike\AppData\Local\bin\NASM\nasm.exe" -f win64 "F:\Desktop\tmp\winasm-2\Assembly\windows\003-window\test.nasm" -l "F:\Desktop\tmp\winasm-2\Assembly\windows\003-window\test.lst" -o "F:\Desktop\tmp\winasm-2\Assembly\windows\003-window\test.obj"

# I experimented with a few different linkers (polink, link.exe, golink, gcc, ld)
# but ld ended up being the simplest overall.
# For a long time, I was able to compile by linking the .dll but eventually there
# was one external api procedure which would crash the linker, so I had to switch
# to .lib files. I could have used the ones provided by VSStudio VC++ or MASM32,
# but PelleC was close at hand so I went with it.
#ld -s test.obj "C:\WINDOWS\System32\kernel32.dll" "C:\WINDOWS\System32\user32.dll" -o "F:\Desktop\tmp\winasm-2\Assembly\windows\003-window\test.exe"
#ld -s test.obj -L"C:\Program Files\PellesC\Lib\Win64" kernel32.lib user32.lib -o "F:\Desktop\tmp\winasm-2\Assembly\windows\003-window\test.exe"
ld -s test.obj "C:\Program Files\PellesC\Lib\Win64\kernel32.lib" "C:\Program Files\PellesC\Lib\Win64\user32.lib" -o "F:\Desktop\tmp\winasm-2\Assembly\windows\003-window\test.exe"
#polink /ENTRY:main /SUBSYSTEM:WINDOWS /LIBPATH:"C:\Program Files\PellesC\Lib\Win64" kernel32.lib user32.lib test.obj

# In the beginning it was fun to see the dumpbin output to check that my imports
# and exports were not unnecessarily bloated, and to read the disassembly, but
# then I discovered the NASM .lst file which ended up being better most times,
# as it will show the original source line and line number next to the assembled
# Relative Virtual Addresses (RVAs), which is handy for setting debug breakpoints.
#"C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\bin\amd64\dumpbin.exe" -disasm test.exe
#"C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\bin\amd64\dumpbin.exe" -section:.data -rawdata:8 test.exe
#"C:\Program Files (x86)\Windows Kits\10\Debuggers\x64\windbg.exe" test.exe

# I also like to see the output file size on disk, in bytes.
ls -l test.exe

