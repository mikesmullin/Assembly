; GOAL: Render OpenGL spinning 3d cube animation
; 
extern SetLastError
extern GetLastError
extern FormatMessageA
extern WriteFile
extern ExitProcess
extern GetStdHandle
extern CreateMutexA
extern GetModuleHandleA
extern LoadImageA
extern RegisterClassExA
extern CreateWindowExA
extern GetDC
extern ChoosePixelFormat
extern SetPixelFormat
extern LoadLibraryA
extern GetProcAddress
extern PeekMessageA
extern TranslateMessage
extern DispatchMessageA
extern SwapBuffers
extern DefWindowProcA
extern DestroyWindow
extern PostQuitMessage

section .data align=16
FormatMessage__buffer: times 256 db 0
GetLastError__errCode: dd 0
FormatString: db "Macros:795: yes this is an error",10,0
FormatMessage__length: dd 0
printf__success: dd 0
FormatString_1: db "Macros:799: you just saw an error",10,0
ExitProcess__code: dd 0
glGetError__code: dd 0
FormatString_2: db "glError %1!.8llX!",10,10,0
FormatString_3: db "Macros:852: Exiting as instructed",10,0
WriteFile__bytesWritten: dd 0
Console__stderr_nStdHandle: dd 0
Console__stdout_nStdHandle: dd 0
Generic__success: dq 0
__message_trace: times 8 dq 0
Generic__uuid: db "07b62314-d4fc-4704-96e8-c31eb378d815",0
CreateMutexA__handle: dq 0
GetModuleHandleA__hModule: dq 0
CreateWindow__icon: dq 0
CreateWindow__cursor: dq 0

; struct
MainWindow: ; instanceof tagWNDCLASSEXA
MainWindow.cbSize: dd 0x50 ; UINT = 80
MainWindow.style: dd 0x23 ; = 35 = CS_OWNDC | CS_VREDRAW | CS_HREDRAW UINT
MainWindow.lpfnWndProc: dq WndProc ; WNDPROC
MainWindow.cbClsExtra: dd 0 ; int = NULL
MainWindow.cbWndExtra: dd 0 ; int = NULL
MainWindow.hInstance: dq GetModuleHandleA__hModule ; HINSTANCE
MainWindow.hIcon: dq CreateWindow__icon ; HICON
MainWindow.hCursor: dq CreateWindow__cursor ; HCURSOR
MainWindow.hbrBackground: dq 0 ; HBRUSH = NULL
MainWindow.lpszMenuName: dq 0 ; LPCSTR = NULL
MainWindow.lpszClassName: dq Generic__uuid ; LPCSTR
MainWindow.hIconSm: dq 0 ; HICON = NULL

CreateWindow__atom_name: dq 0
CreateWindow__title: db "OpenGL Demo",0
CreateWindow__hWnd: dq 0
GetDC__hDC: dq 0

; struct
PixelFormat: ; instanceof PIXELFORMATDESCRIPTOR
PixelFormat.nSize: dw 0x28 ; sizeof(struct) = 40
PixelFormat.nVersion: dw 0x1 ; = 1 (magic constant)
PixelFormat.dwFlags: dd 0x25 ; = 37 = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER
PixelFormat.iPixelType: db 0 ; = PFD_TYPE_RGBA
PixelFormat.cColorBits: db 0x18 ; = 24 (24-bit color depth)
PixelFormat.cRedBits: db 0 ; (not used)
PixelFormat.cRedShift: db 0 ; (not used)
PixelFormat.cGreenBits: db 0 ; (not used)
PixelFormat.cGreenShift: db 0 ; (not used)
PixelFormat.cBlueBits: db 0 ; (not used)
PixelFormat.cBlueShift: db 0 ; (not used)
PixelFormat.cAlphaBits: db 0 ; (no alpha buffer)
PixelFormat.cAlphaShift: db 0 ; (not used)
PixelFormat.cAccumBits: db 0 ; (no accumulation buffer)
PixelFormat.cAccumRedBits: db 0 ; (not used)
PixelFormat.cAccumGreenBits: db 0 ; (not used)
PixelFormat.cAccumBlueBits: db 0 ; (not used)
PixelFormat.cAccumAlphaBits: db 0 ; (not used)
PixelFormat.cDepthBits: db 0x20 ; = 32 (32-bit z-buffer)
PixelFormat.cStencilBits: db 0 ; (no stencil buffer)
PixelFormat.cAuxBuffers: db 0 ; (no auxiliary buffer)
PixelFormat.iLayerType: db 0 ; = PFD_MAIN_PLANE
PixelFormat.bReserved: db 0 ; (not used)
PixelFormat.dwLayerMask: dd 0 ; (not used)
PixelFormat.dwVisibleMask: dd 0 ; (not used)
PixelFormat.dwDamageMask: dd 0 ; (not used)

ChoosePixelFormat__format: dd 0
LoadLibraryA__opengl32: db "opengl32.dll",0
LoadLibraryA__opengl32_hModule: dq 0
GetProcAddress__wglCreateContext: db "wglCreateContext",0
wglCreateContext: dq 0
GetProcAddress__wglMakeCurrent: db "wglMakeCurrent",0
wglMakeCurrent: dq 0
GetProcAddress__glClearColor: db "glClearColor",0
glClearColor: dq 0
GetProcAddress__glClear: db "glClear",0
glClear: dq 0
GetProcAddress__glGetError: db "glGetError",0
glGetError: dq 0
GetProcAddress__glGetString: db "glGetString",0
glGetString: dq 0
GetProcAddress__wglGetProcAddress: db "wglGetProcAddress",0
wglGetProcAddress: dq 0
wglCreateContext__ctx: dq 0
glString: dq 0
FormatString_4: db "GL_VERSION: %1",10,10,0
F0_0: dq 0x0
F1_0: dq 0x3f800000
FormatString_5: db "Main:216: check messages",10,0

; struct
IncomingMessage: ; instanceof tagMSG
IncomingMessage.hwnd: dq 0 ; HWND
IncomingMessage.message: dd 0 ; UINT
IncomingMessage.wParam: dq 0 ; WPARAM
IncomingMessage.lParam: dq 0 ; LPARAM
IncomingMessage.time: dd 0
IncomingMessage.pt.x: dd 0
IncomingMessage.pt.y: dd 0
IncomingMessage.lPrivate: dd 0

FormatString_6: db "Main:230: I am probably forgetting to free some memory",10,0
FormatString_7: db "Main:248: message received",10,0
FormatString_8: db "Main:258: translate it",10,0
FormatString_9: db "Main:260: dispatch it",10,0
FormatString_10: db "Main:262: done dispatching it",10,0
FormatString_11: db "Main:265: begin render",10,0
WndProc__hWnd: dq 0
WndProc__uMsg: dq 0
WndProc__wParam: dq 0
WndProc__lParam: dq 0
FormatString_12: db "Main:286: WndProc called:",10,"  message: %2!.4llX!",10,10,0
FormatString_13: db "Main:304: falling through",10,0
FormatString_14: db "Main:307: WM_Default DefWindowProcA",10,0
FormatString_15: db "Main:324: WM_Close pre - could prompt the user, but we don't care",10,0
FormatString_16: db "Main:326: WM_Close post - it must be the error happens about here",10,0
FormatString_17: db "Main:330: WM_Destroy pre",10,0
FormatString_18: db "Main:332: WM_Destroy post",10,0
FormatString_19: db "Main:336: WndProc return 0",10,0

section .text align=16
global main
main:
; get pointers to stdout/stderr pipes
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0xfffffff4 ; 1st = -12 DWORD nStdHandle
call GetStdHandle
    mov dword [Console__stderr_nStdHandle], eax ; return HANDLE
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0xfffffff5 ; 1st = -11 DWORD nStdHandle
call GetStdHandle
    mov dword [Console__stdout_nStdHandle], eax ; return HANDLE
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check


; verify the window is not open twice
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword r8d, Generic__uuid ; 3rd LPCSTR lpName
    mov dword edx, 1 ; 2nd = TRUE BOOL bInitialOwner
    mov dword ecx, 0 ; 1st LPSECURITY_ATTRIBUTES lpMutexAttributes
call CreateMutexA
    mov qword [CreateMutexA__handle], rax ; return HANDLE
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

; get a pointer to this process for use with api functions which require it 
; Note that as of 32-bit Windows, an instance handle (HINSTANCE), such as the
; application instance handle exposed by system function call of WinMain, and
; a module handle (HMODULE) are the same thing.
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0 ; 1st = NULL LPCSTR lpModuleName
call GetModuleHandleA
    mov qword [GetModuleHandleA__hModule], rax ; return HMODULE *phModule
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

; load references to the default icons for new windows
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 56 ; allocate shadow space
    mov dword [rsp + 40], 0x8040 ; 6th = 32832 UINT fuLoad
    mov dword [rsp + 32], 0 ; 5th int cy
    mov dword r9d, 0 ; 4th int cx
    mov dword r8d, 0x1 ; 3rd = 1 UINT type
    mov dword edx, 0x7f05 ; 2nd = 32517 LPCSTR name
    mov dword ecx, 0 ; 1st = NULL HINSTANCE hInst
call LoadImageA
    mov qword [CreateWindow__icon], rax ; return HANDLE
    add rsp, 56 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 56 ; allocate shadow space
    mov dword [rsp + 40], 0x8040 ; 6th = 32832 UINT fuLoad
    mov dword [rsp + 32], 0 ; 5th int cy
    mov dword r9d, 0 ; 4th int cx
    mov dword r8d, 0x2 ; 3rd = 2 UINT type
    mov dword edx, 0x7f00 ; 2nd = 32512 LPCSTR name
    mov dword ecx, 0 ; 1st = NULL HINSTANCE hInst
call LoadImageA
    mov qword [CreateWindow__cursor], rax ; return HANDLE
    add rsp, 56 ; deallocate shadow space

    call GetLastError__epilogue_check

; begin creating the main local application window
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, MainWindow ; 1st WNDCLASSEXA *Arg1
call RegisterClassExA
    mov qword [CreateWindow__atom_name], rax ; return HANDLE
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 104 ; allocate shadow space
    mov qword [rsp + 88], 0 ; 12th = NULL LPVOID lpParam
    mov qword [rsp + 80], GetModuleHandleA__hModule ; 11th HINSTANCE hInstance
    mov qword [rsp + 72], 0 ; 10th = NULL HMENU hMenu
    mov qword [rsp + 64], 0 ; 9th = NULL HWND hWndParent
    mov dword [rsp + 56], 0x1e0 ; 8th = 480 int nHeight
    mov dword [rsp + 48], 0x280 ; 7th = 640 int nWidth
    mov dword [rsp + 40], 0x80000000 ; 6th = -2147483648 int Y
    mov dword [rsp + 32], 0x80000000 ; 5th = -2147483648 int X
    mov qword r9, 0x16cf0000 ; 4th = 382664704 WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX | WS_VISIBLE | WS_CLIPCHILDREN | WS_CLIPSIBLINGS DWORD dwStyle
    mov qword r8, CreateWindow__title ; 3rd LPCSTR lpWindowName
    mov qword rdx, Generic__uuid ; 2nd LPCSTR lpClassName
    mov qword rcx, 0x100 ; 1st = 256 WS_EX_WINDOWEDGE DWORD dwExStyle
call CreateWindowExA
    mov qword [CreateWindow__hWnd], rax ; return HANDLE
    add rsp, 104 ; deallocate shadow space

    call GetLastError__epilogue_check

; begin creating the OpenGL context
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [CreateWindow__hWnd] ; 1st HWND hWnd
call GetDC
    mov qword [GetDC__hDC], rax ; return HDC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rdx, PixelFormat ; 2nd PIXELFORMATDESCRIPTOR *ppfd
    mov qword rcx, [GetDC__hDC] ; 1st HDC hdc
call ChoosePixelFormat
    mov dword [ChoosePixelFormat__format], eax ; return int
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword r8, PixelFormat ; 3rd PIXELFORMATDESCRIPTOR *ppfd
    mov dword edx, [ChoosePixelFormat__format] ; 2nd int format
    mov qword rcx, [GetDC__hDC] ; 1st HDC hdc
call SetPixelFormat
    mov dword [Generic__success], eax ; return BOOL
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

; dynamically load library at runtime
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, LoadLibraryA__opengl32 ; 1st LPCSTR lpLibFileName
call LoadLibraryA
    mov qword [LoadLibraryA__opengl32_hModule], rax ; return HMODULE
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__wglCreateContext ; 2nd LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st HMODULE hModule
call GetProcAddress
    mov qword [wglCreateContext], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__wglMakeCurrent ; 2nd LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st HMODULE hModule
call GetProcAddress
    mov qword [wglMakeCurrent], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__glClearColor ; 2nd LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st HMODULE hModule
call GetProcAddress
    mov qword [glClearColor], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__glClear ; 2nd LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st HMODULE hModule
call GetProcAddress
    mov qword [glClear], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__glGetError ; 2nd LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st HMODULE hModule
call GetProcAddress
    mov qword [glGetError], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__glGetString ; 2nd LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st HMODULE hModule
call GetProcAddress
    mov qword [glGetString], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__wglGetProcAddress ; 2nd LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st HMODULE hModule
call GetProcAddress
    mov qword [wglGetProcAddress], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check


    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [GetDC__hDC] ; 1st HDC Arg1
call [wglCreateContext]
    mov qword [wglCreateContext__ctx], rax ; return HGLRC
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rdx, [wglCreateContext__ctx] ; 2nd HGLRC
    mov qword rcx, [GetDC__hDC] ; 1st HDC
call [wglMakeCurrent]
    mov dword [Generic__success], eax ; return BOOL
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, 0x1f02 ; 1st = 7938 GL_VERSION GLenum name
call [glGetString]
    mov qword [glString], rax ; return GLubyte* WINAPI
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], glString ; 7th va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_4 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rax, [F1_0]
    movq xmm3, rax ; 4th GLclampf alpha
    mov qword rax, [F1_0]
    movq xmm2, rax ; 3rd GLclampf blue
    mov qword rax, [F0_0]
    movq xmm1, rax ; 2nd GLclampf green
    mov qword rax, [F0_0]
    movq xmm0, rax ; 1st GLclampf red
call [glClearColor]
    add rsp, 40 ; deallocate shadow space

Loop:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_5 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0x1 ; 5th = 1 UINT wRemoveMsg
    mov dword r9d, 0 ; 4th UINT wMsgFilterMax
    mov dword r8d, 0 ; 3rd UINT wMsgFilterMin
    mov qword rdx, 0 ; 2nd = NULL HWND hWnd
    mov qword rcx, IncomingMessage ; 1st LPMSG lpMsg
call PeekMessageA
    mov dword [Generic__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space

    call GetLastError__epilogue_check

; if zero messages, skip handling messages
cmp dword [Generic__success], 0
je near ..@Render
cmp dword [IncomingMessage.message], 0x12 ; = 18 WM_QUIT
jne near ..@Loop__processMessage
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_6 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


mov dword [ExitProcess__code], 0
call Exit


..@Loop__processMessage:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_7 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_8 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, IncomingMessage ; 1st LPMSG lpMsg
call TranslateMessage
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_9 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, IncomingMessage ; 1st LPMSG lpMsg
call DispatchMessageA
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_10 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


..@Render:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_11 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0x4000 ; 1st = 16384 GLbitfield mask
call [glClear]
    add rsp, 40 ; deallocate shadow space

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [GetDC__hDC] ; 1st HDC Arg1
call SwapBuffers
    mov dword [Generic__success], eax ; return BOOL
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

jmp near Loop
WndProc:
; move local registers to local shadow space to preserve them
mov qword [WndProc__hWnd], rcx
mov qword [WndProc__uMsg], rdx
mov qword [WndProc__wParam], r8
mov qword [WndProc__lParam], r9
mov dword eax, [WndProc__uMsg]
mov dword [__message_trace + 8], eax
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], __message_trace ; 7th va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_12 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


mov qword rdx, [WndProc__uMsg]
; switch(uMsg) {
cmp qword rdx, 0x6 ; = 6 WM_ACTIVATE
je near WndProc__return
cmp qword rdx, 0x112 ; = 274 WM_SYSCOMMAND
je near ..@WM_SysCommand
cmp qword rdx, 0x10 ; = 16 WM_CLOSE
je near ..@WM_Close
cmp qword rdx, 0x2 ; = 2 WM_DESTROY
je near ..@WM_Destroy
cmp qword rdx, 0x82 ; = 130 WM_NCDESTROY
je near WndProc__return
cmp qword rdx, 0x100 ; = 256 WM_KEYDOWN
je near WndProc__return
cmp qword rdx, 0x101 ; = 257 WM_KEYUP
je near WndProc__return
cmp qword rdx, 0x5 ; = 5 WM_SIZE
je near WndProc__return
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_13 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


..@default:
; default window procedure handles messages for us
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_14 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword r9, [WndProc__lParam] ; 4th LPARAM lParam
    mov qword r8, [WndProc__wParam] ; 3rd WPARAM wParam
    mov qword rdx, [WndProc__uMsg] ; 2nd UINT Msg
    mov qword rcx, [WndProc__hWnd] ; 1st HWND hWnd
call DefWindowProcA
    mov qword [Generic__success], rax ; return LRESULT
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0 ; 1st DWORD dwErrCode
call SetLastError
    add rsp, 40 ; deallocate shadow space

mov qword rax, [Generic__success] ; return 
ret
..@WM_SysCommand:
mov dword ebx, [WndProc__wParam]
cmp dword ebx, 0xf140 ; = 61760
je near WndProc__return
cmp dword ebx, 0xf170 ; = 61808
je near WndProc__return
jmp near ..@default
..@WM_Close:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_15 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [CreateWindow__hWnd] ; 1st HWND hWnd
call DestroyWindow
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_16 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


jmp near WndProc__return
..@WM_Destroy:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_17 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0 ; 1st int nExitCode
call PostQuitMessage
    add rsp, 40 ; deallocate shadow space

    call GetLastError__epilogue_check

    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_18 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


jmp near WndProc__return
WndProc__return:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_19 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


xor rax, rax ; return NULL
ret
GetLastError__prologue_reset:
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0 ; 1st DWORD dwErrCode
call SetLastError
    add rsp, 40 ; deallocate shadow space

ret

GetLastError__epilogue_check:
cmp dword eax, 0
je near GetLastError__epilogue_lookup
ret

GetLastError__epilogue_lookup:
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
call GetLastError
    mov dword [GetLastError__errCode], eax ; return DWORD dwErrCode
    add rsp, 40 ; deallocate shadow space

cmp dword eax, 0
jne near ..@error
ret

..@error:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0x400 ; 4th = 1024 LANG_USER_DEFAULT__SUBLANG_DEFAULT DWORD dwLanguageId
    mov dword r8d, [GetLastError__errCode] ; 3rd DWORD dwMessageId
    mov dword edx, 0 ; 2nd = NULL LPCVOID lpSource
    mov dword ecx, 0x1200 ; 1st = 4608 FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_1 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


mov dword eax, [GetLastError__errCode]
mov dword [ExitProcess__code], eax
call Exit


GetLastError__epilogue_glGetError:
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
call [glGetError]
    mov dword [glGetError__code], eax ; return GLenum errCode
    add rsp, 40 ; deallocate shadow space

cmp dword eax, 0
jne near ..@glError
ret

..@glError:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], glGetError__code ; 7th va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_2 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stderr_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


mov dword eax, [glGetError__code]
mov dword [ExitProcess__code], eax
call Exit


Exit:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th = NULL va_list *Arguments
    mov qword [rsp + 40], 0x100 ; 6th = 256 DWORD nSize
    mov qword [rsp + 32], FormatMessage__buffer ; 5th LPSTR lpBuffer
    mov dword r9d, 0 ; 4th = NULL DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd = NULL DWORD dwMessageId
    mov dword edx, FormatString_3 ; 2nd LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st = 9216 FORMAT_MESSAGE_ARGUMENT_ARRAY | FORMAT_MESSAGE_FROM_STRING DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__length], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th = NULL LPOVERLAPPED lpOverlapped
    mov dword r9d, WriteFile__bytesWritten ; 4th LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__length] ; 3rd DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__buffer ; 2nd LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st HANDLE hFile
call WriteFile
    mov dword [printf__success], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space


    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [ExitProcess__code] ; 1st UINT uExitCode
call ExitProcess
    add rsp, 40 ; deallocate shadow space

ret
jmp near Exit


