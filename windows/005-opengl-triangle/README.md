# OpenGL Triangle in x86-64 Windows Assembly (with Java Assembler Preprocessor)

Instead of defining MASM or NASM macros, I utilized the strong-typing of Java and
IDE support of IntelliJ to output my assembly code to NASM for machine encoding,
and then to GCC for linking in Windows PE executable format.

The release binary is **~10Kb** (with alignment padding)
and has no dependencies other than `KERNEL32.DLL`, `USER32.DLL`, `GDI32.DLL`
(statically linked; the basic libs required by all Windows apps)
and `OPENGL32.DLL` (ships with Windows, and dynamically linked at runtime).

- No GLU, GLEW, GLFW, etc. frameworks.
- No libC (a.k.a. MSVCRT.DLL from MSVC++).
- Not even NASM macros are used. :open_mouth:

Just **~1400 lines** of well-commented x86-64 Windows ASM. :muscle:

### Screenshot

![Screenshot](https://i.imgur.com/yNUeNVR.png)

## Build

- Build [Main.java](src/com/sdd/asm/demo/opengl/Main.java)
- Execute [build.bat](build.bat)
- Inspect [test.nasm](build/test.nasm) or [test.lst](build/test.lst)
- Run [test.exe](build/test.exe)