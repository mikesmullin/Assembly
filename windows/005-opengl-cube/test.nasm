; GOAL: Render OpenGL spinning 3d cube animation
; 
; TODO: could make it so i don't check for GetLastError unless a proc returns 0
; TODO: output to log file in relative path instead of showing console window
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
GetLastError__errCode: dd 0
glGetError__code: dd 0
Console__stderr_nStdHandle: dd 0
Console__stdout_nStdHandle: dd 0
FormatMessage__tmpReturnBuffer: times 256 db 0
FormatMessage__tmpReturnBufferLength: dd 0
glGetError__str: db "glError %1!.8llX!",10,0
Console__bytesWritten: dd 0
; generic reusable uuid any time an api function wants a string identifier
Generic__uuid: db "07b62314-d4fc-4704-96e8-c31eb378d815",0
CreateMutexA__handle: dq 0
GetModuleHandleA__hModule: dq 0
CreateWindow__icon: dq 0
CreateWindow__cursor: dq 0

; struct
MainWindow_1: ; instanceof tagWNDCLASSEXA
MainWindow_1.cbSize dd 80 ; UINT
MainWindow_1.style dd 0x23 ; UINT = CS_OWNDC | CS_VREDRAW | CS_HREDRAW
MainWindow_1.lpfnWndProc dq WndProc ; WNDPROC
MainWindow_1.cbClsExtra dd 0 ; int
MainWindow_1.cbWndExtra dd 0 ; int
MainWindow_1.hInstance dq GetModuleHandleA__hModule ; HINSTANCE
MainWindow_1.hIcon dq CreateWindow__icon ; HICON
MainWindow_1.hCursor dq CreateWindow__cursor ; HCURSOR
MainWindow_1.hbrBackground dq 0 ; HBRUSH
MainWindow_1.lpszMenuName dq 0 ; LPCSTR
MainWindow_1.lpszClassName dq Generic__uuid ; LPCSTR
MainWindow_1.hIconSm dq 0 ; HICON

CreateWindow__title: db "OpenGL Demo",0
CreateWindow__atom_name: dq 0
CreateWindow__hWnd: dq 0
GetDC__hDC: dq 0

; struct
PixelFormat_1: ; instanceof PIXELFORMATDESCRIPTOR
PixelFormat_1.nSize dw 40 ; word sizeof(struct)
PixelFormat_1.nVersion dw 1 ; word (magic constant)
PixelFormat_1.dwFlags dd 0x25 ; dword = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER
PixelFormat_1.iPixelType db 0 ; byte = PFD_TYPE_RGBA
PixelFormat_1.cColorBits db 24 ; byte (24-bit color depth)
PixelFormat_1.cRedBits db 0 ; byte (not used)
PixelFormat_1.cRedShift db 0 ; byte (not used)
PixelFormat_1.cGreenBits db 0 ; byte (not used)
PixelFormat_1.cGreenShift db 0 ; byte (not used)
PixelFormat_1.cBlueBits db 0 ; byte (not used)
PixelFormat_1.cBlueShift db 0 ; byte (not used)
PixelFormat_1.cAlphaBits db 0 ; byte (no alpha buffer)
PixelFormat_1.cAlphaShift db 0 ; byte (not used)
PixelFormat_1.cAccumBits db 0 ; byte (no accumulation buffer)
PixelFormat_1.cAccumRedBits db 0 ; byte (not used)
PixelFormat_1.cAccumGreenBits db 0 ; byte (not used)
PixelFormat_1.cAccumBlueBits db 0 ; byte (not used)
PixelFormat_1.cAccumAlphaBits db 0 ; byte (not used)
PixelFormat_1.cDepthBits db 32 ; byte (32-bit z-buffer)
PixelFormat_1.cStencilBits db 0 ; byte (no stencil buffer)
PixelFormat_1.cAuxBuffers db 0 ; byte (no auxiliary buffer)
PixelFormat_1.iLayerType db 0 ; byte = PFD_MAIN_PLANE
PixelFormat_1.bReserved db 0 ; byte (not used)
PixelFormat_1.dwLayerMask dd 0 ; dword (not used)
PixelFormat_1.dwVisibleMask dd 0 ; dword (not used)
PixelFormat_1.dwDamageMask dd 0 ; dword (not used)

ChoosePixelFormat__format: dd 0
SetPixelFormat__success: dd 0
LoadLibraryA__opengl32: db "opengl32.dll",0
LoadLibraryA__opengl32_hModule: dq 0
wglCreateContext: dq 0
GetProcAddress__wglCreateContext: db "wglCreateContext",0
wglMakeCurrent: dq 0
GetProcAddress__wglMakeCurrent: db "wglMakeCurrent",0
glClearColor: dq 0
GetProcAddress__glClearColor: db "glClearColor",0
glClear: dq 0
GetProcAddress__glClear: db "glClear",0
glGetError: dq 0
GetProcAddress__glGetError: db "glGetError",0
wglCreateContext__ctx: dq 0
wglMakeCurrent__success: dd 0
F_0: dq 0x00000000
F_1: dq 0x3f800000

; struct
IncomingMessage_1: ; instanceof tagMSG
IncomingMessage_1.hwnd dq 0 ; HWND
IncomingMessage_1.message dd 0 ; UINT
IncomingMessage_1.wParam dq 0 ; WPARAM
IncomingMessage_1.lParam dq 0 ; LPARAM
IncomingMessage_1.time dd 0 ; dword
IncomingMessage_1.pt.x dd 0 ; dword
IncomingMessage_1.pt.y dd 0 ; dword
IncomingMessage_1.lPrivate dd 0 ; dword

PeekMessage_hasMsgs: dd 0
SwapBuffers__success: dd 0
WndProc__hWnd: dq 0
WndProc__uMsg: dq 0
WndProc__wParam: dq 0
WndProc__lParam: dq 0
WndProc__return: dq 0

section .text align=16
global main
main:

; get pointers to stdout/stderr pipes
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, -12 ; 1st: DWORD nStdHandle = STD_ERROR_HANDLE
call GetStdHandle
    mov dword [Console__stderr_nStdHandle], eax ; return 
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, -11 ; 1st: DWORD nStdHandle = STD_OUTPUT_HANDLE
call GetStdHandle
    mov dword [Console__stdout_nStdHandle], eax ; return 
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check


; verify the window is not open twice
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword r8d, Generic__uuid ; 3rd: LPCSTR lpName
    mov dword edx, 1 ; 2nd: BOOL bInitialOwner
    mov dword ecx, 0 ; 1st: LPSECURITY_ATTRIBUTES lpMutexAttributes
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
    mov dword ecx, 0 ; 1st: LPCSTR lpModuleName
call GetModuleHandleA
    mov qword [GetModuleHandleA__hModule], rax ; return HMODULE *phModule
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

; load references to the default icons for new windows
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 56 ; allocate shadow space
    mov dword [rsp + 40], 0x8040 ; 6th: UINT fuLoad
    mov dword [rsp + 32], 0 ; 5th: int cy
    mov dword r9d, 0 ; 4th: int cx
    mov dword r8d, 1 ; 3rd: UINT type
    mov dword edx, 32517 ; 2nd: LPCSTR name
    mov dword ecx, 0 ; 1st: HINSTANCE hInst
call LoadImageA
    mov qword [CreateWindow__icon], rax ; return HANDLE
    add rsp, 56 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 56 ; allocate shadow space
    mov dword [rsp + 40], 0x8040 ; 6th: UINT fuLoad
    mov dword [rsp + 32], 0 ; 5th: int cy
    mov dword r9d, 0 ; 4th: int cx
    mov dword r8d, 2 ; 3rd: UINT type
    mov dword edx, 32512 ; 2nd: LPCSTR name
    mov dword ecx, 0 ; 1st: HINSTANCE hInst
call LoadImageA
    mov qword [CreateWindow__cursor], rax ; return HANDLE
    add rsp, 56 ; deallocate shadow space
    call GetLastError__epilogue_check

; begin creating the main local application window
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, MainWindow_1 ; 1st: WNDCLASSEXA *Arg1
call RegisterClassExA
    mov qword [CreateWindow__atom_name], rax ; return HANDLE
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 104 ; allocate shadow space
    mov qword [rsp + 88], 0 ; 12th: LPVOID lpParam
    mov qword [rsp + 80], GetModuleHandleA__hModule ; 11th: HINSTANCE hInstance
    mov qword [rsp + 72], 0 ; 10th: HMENU hMenu
    mov qword [rsp + 64], 0 ; 9th: HWND hWndParent
    mov dword [rsp + 56], 480 ; 8th: int nHeight
    mov dword [rsp + 48], 640 ; 7th: int nWidth
    mov dword [rsp + 40], 0x80000000 ; 6th: int Y
    mov dword [rsp + 32], 0x80000000 ; 5th: int X
    mov qword r9, 0x16cf0000 ; 4th: DWORD dwStyle
    mov qword r8, CreateWindow__title ; 3rd: LPCSTR lpWindowName
    mov qword rdx, Generic__uuid ; 2nd: LPCSTR lpClassName
    mov qword rcx, 768 ; 1st: DWORD dwExStyle
call CreateWindowExA
    mov qword [CreateWindow__hWnd], rax ; return HANDLE
    add rsp, 104 ; deallocate shadow space
    call GetLastError__epilogue_check

; begin creating the OpenGL context
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [CreateWindow__hWnd] ; 1st: HWND hWnd
call GetDC
    mov qword [GetDC__hDC], rax ; return HDC
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rdx, PixelFormat_1 ; 2nd: PIXELFORMATDESCRIPTOR *ppfd
    mov qword rcx, [GetDC__hDC] ; 1st: HDC hdc
call ChoosePixelFormat
    mov dword [ChoosePixelFormat__format], eax ; return int
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword r8, PixelFormat_1 ; 3rd: PIXELFORMATDESCRIPTOR *ppfd
    mov dword edx, [ChoosePixelFormat__format] ; 2nd: int format
    mov qword rcx, [GetDC__hDC] ; 1st: HDC hdc
call SetPixelFormat
    mov dword [SetPixelFormat__success], eax ; return BOOL
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

; dynamically load library at runtime
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, LoadLibraryA__opengl32 ; 1st: LPCSTR lpLibFileName
call LoadLibraryA
    mov qword [LoadLibraryA__opengl32_hModule], rax ; return HMODULE
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__wglCreateContext ; 2nd: LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st: HMODULE hModule
call GetProcAddress
    mov qword [wglCreateContext], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__wglMakeCurrent ; 2nd: LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st: HMODULE hModule
call GetProcAddress
    mov qword [wglMakeCurrent], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__glClearColor ; 2nd: LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st: HMODULE hModule
call GetProcAddress
    mov qword [glClearColor], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__glClear ; 2nd: LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st: HMODULE hModule
call GetProcAddress
    mov qword [glClear], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword edx, GetProcAddress__glGetError ; 2nd: LPCSTR lpProcName
    mov qword rcx, [LoadLibraryA__opengl32_hModule] ; 1st: HMODULE hModule
call GetProcAddress
    mov qword [glGetError], rax ; return FARPROC
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check


    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [GetDC__hDC] ; 1st: HDC Arg1
call [wglCreateContext]
    mov qword [wglCreateContext__ctx], rax ; return HGLRC
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rdx, [wglCreateContext__ctx] ; 2nd: HGLRC
    mov qword rcx, [GetDC__hDC] ; 1st: HDC
call [wglMakeCurrent]
    mov dword [wglMakeCurrent__success], eax ; return BOOL
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rax, [F_1]
    movq xmm3, rax ; 4th: GLclampf alpha
    mov qword rax, [F_1]
    movq xmm2, rax ; 3rd: GLclampf blue
    mov qword rax, [F_0]
    movq xmm1, rax ; 2nd: GLclampf green
    mov qword rax, [F_0]
    movq xmm0, rax ; 1st: GLclampf red
call [glClearColor]
    add rsp, 40 ; deallocate shadow space

Loop:
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 1 ; 5th: UINT wRemoveMsg
    mov dword r9d, 0 ; 4th: UINT wMsgFilterMax
    mov dword r8d, 0 ; 3rd: UINT wMsgFilterMin
    mov qword rdx, [CreateWindow__hWnd] ; 2nd: HWND hWnd
    mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
call PeekMessageA
    mov dword [PeekMessage_hasMsgs], eax ; return BOOL
    add rsp, 48 ; deallocate shadow space
    call GetLastError__epilogue_check

; if zero messages, skip handling messages
cmp dword [PeekMessage_hasMsgs], 0
je near ..@Render
; 
; exit if message is WM_QUIT
cmp dword [IncomingMessage_1.message], 0x12
jne near ..@Loop__processMessage
mov ecx, 0 ; UINT uExitCode
call Exit

..@Loop__processMessage:
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
call TranslateMessage
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
call DispatchMessageA
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

..@Render:
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 16384 ; 1st: GLbitfield mask
call [glClear]
    add rsp, 40 ; deallocate shadow space

    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [GetDC__hDC] ; 1st: HDC Arg1
call SwapBuffers
    mov dword [SwapBuffers__success], eax ; return BOOL
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

jmp near Loop
WndProc:
; move local registers to local shadow space to preserve them
mov qword [WndProc__hWnd], rcx
mov qword [WndProc__uMsg], rdx
mov qword [WndProc__wParam], r8
mov qword [WndProc__lParam], r9
; switch(uMsg) {
cmp qword rdx, 0x6
je near ..@WndProc__WM_Activate
cmp qword rdx, 0x112
je near ..@WndProc__WM_SysCommand
cmp qword rdx, 0x10
je near ..@WndProc__WM_Close
cmp qword rdx, 0x2
je near ..@WndProc__WM_Destroy
cmp qword rdx, 0x100
je near ..@WndProc__WM_KeyDown
cmp qword rdx, 0x101
je near ..@WndProc__WM_KeyUp
cmp qword rdx, 0x5
je near ..@WndProc__WM_Size
..@WndProc__default:
; default window procedure handles messages for us
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword r9, [WndProc__lParam] ; 4th: LPARAM lParam
    mov qword r8, [WndProc__wParam] ; 3rd: WPARAM wParam
    mov qword rdx, [WndProc__uMsg] ; 2nd: UINT Msg
    mov qword rcx, [WndProc__hWnd] ; 1st: HWND hWnd
call DefWindowProcA
    mov qword [WndProc__return], rax ; return 
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

mov qword rax, [WndProc__return] ; return
ret
..@WndProc__WM_Activate:
xor rax, rax ; return NULL
ret
..@WndProc__WM_SysCommand:
mov dword ebx, [WndProc__wParam]
cmp dword ebx, 0xf140
je near ..@return_zero
cmp dword ebx, 0xf170
je near ..@return_zero
jmp near ..@WndProc__default
..@return_zero:
xor rax, rax ; return NULL
ret
..@WndProc__WM_Close:
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov qword rcx, [CreateWindow__hWnd] ; 1st: HWND hWnd
call DestroyWindow
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

xor rax, rax ; return NULL
ret
..@WndProc__WM_Destroy:
    call GetLastError__prologue_reset
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0 ; 1st: int nExitCode
call PostQuitMessage
    add rsp, 40 ; deallocate shadow space
    call GetLastError__epilogue_check

xor rax, rax ; return NULL
ret
..@WndProc__WM_KeyDown:
xor rax, rax ; return NULL
ret
..@WndProc__WM_KeyUp:
xor rax, rax ; return NULL
ret
..@WndProc__WM_Size:
xor rax, rax ; return NULL
ret

GetLastError__prologue_reset:
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
    mov dword ecx, 0 ; 1st: DWORD dwErrCode
call SetLastError
    add rsp, 40 ; deallocate shadow space
ret

GetLastError__epilogue_check:
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
call GetLastError
    mov dword [GetLastError__errCode], eax ; return 
    add rsp, 40 ; deallocate shadow space
cmp rax, 0
jne ..@error
ret

..@error:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], 0 ; 7th: va_list *Arguments
    mov qword [rsp + 40], 256 ; 6th: DWORD nSize
    mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
    mov dword r9d, 0x400 ; 4th: DWORD dwLanguageId
    mov dword r8d, [GetLastError__errCode] ; 3rd: DWORD dwMessageId
    mov dword edx, 0 ; 2nd: LPCVOID lpSource
    mov dword ecx, 0x1200 ; 1st: DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__tmpReturnBufferLength], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th: LPOVERLAPPED lpOverlapped
    mov dword r9d, Console__bytesWritten ; 4th: LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__tmpReturnBufferLength] ; 3rd: DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__tmpReturnBuffer ; 2nd: LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st: HANDLE hFile
call WriteFile
    add rsp, 48 ; deallocate shadow space

mov ecx, [GetLastError__errCode] ; UINT uExitCode
call Exit

GetLastError__epilogue_glGetError:
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
call [glGetError]
    mov dword [glGetError__code], eax ; return GLenum
    add rsp, 40 ; deallocate shadow space
cmp eax, 0
jne ..@glError
ret

..@glError:
    ; MS __fastcall x64 ABI
    sub rsp, 64 ; allocate shadow space
    mov qword [rsp + 48], glGetError__code ; 7th: va_list *Arguments
    mov qword [rsp + 40], 256 ; 6th: DWORD nSize
    mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
    mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
    mov dword r8d, 0 ; 3rd: DWORD dwMessageId
    mov dword edx, glGetError__str ; 2nd: LPCVOID lpSource
    mov dword ecx, 0x2400 ; 1st: DWORD dwFlags
call FormatMessageA
    mov dword [FormatMessage__tmpReturnBufferLength], eax ; return DWORD TCHARs written
    add rsp, 64 ; deallocate shadow space

    ; MS __fastcall x64 ABI
    sub rsp, 48 ; allocate shadow space
    mov dword [rsp + 32], 0 ; 5th: LPOVERLAPPED lpOverlapped
    mov dword r9d, Console__bytesWritten ; 4th: LPDWORD lpNumberOfBytesWritten
    mov dword r8d, [FormatMessage__tmpReturnBufferLength] ; 3rd: DWORD nNumberOfBytesToWrite
    mov dword edx, FormatMessage__tmpReturnBuffer ; 2nd: LPCVOID lpBuffer
    mov dword ecx, [Console__stdout_nStdHandle] ; 1st: HANDLE hFile
call WriteFile
    add rsp, 48 ; deallocate shadow space
mov ecx, [glGetError__code] ; UINT uExitCode
call Exit

Exit:
    ; MS __fastcall x64 ABI
    sub rsp, 40 ; allocate shadow space
call ExitProcess
    add rsp, 40 ; deallocate shadow space

ret
jmp near Exit

