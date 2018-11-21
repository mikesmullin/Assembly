; BABY'S FIRST WINDOW
;
; Will display a blank window which you can min/max/resize, move, and close.
; The output could be more minimalist if you remove all the debug traces,
; but I decided to leave them in so future self would have a point of reference.
;
; I like the techniques used here because there are no dependency libraries,
; not even the C Standard lib (ie. MSVCRT). It just uses what Windows gives all
; programs by default with KERNEL32.DLL.
;
; The other thing you'll notice is test.js is effectively a NASM preprocessor
; alternative using modern JavaScript syntax (and utility libraries like lodash!)
; It is more efficient and less repetitive. I could have used any higher-level
; language to achieve this effect, but Node.JS just seemed close at hand.
; I am happy with the results and will probably continue using the approach,
; because its teaching me [by comparison] how the assembler works, and helping
; me look for optimizations to the whole process of writing code at this layer.
; Not to mention better support for these languages by modern IDEs!
;
; Build steps:
;
; node test.js # will overwrite test.nasm
; # update paths in build.sh to match your system and environment
; sh build.sh # will use NASM + LD to compile test.obj and test.exe
; cdb test.exe # nice command-line windows debugger
;

; build window
extern GetModuleHandleA
extern CreateMutexA
extern LoadImageA
extern RegisterClassExA
extern CreateWindowExA

; main loop
extern PeekMessageA
extern TranslateMessage
extern DispatchMessageA
extern DefWindowProcA

; shutdown/cleanup
extern DestroyWindow
extern PostQuitMessage
extern ExitProcess

; error handling
extern SetLastError
extern GetLastError
extern FormatMessageA
extern GetStdHandle
extern WriteFile

section .data align=16
GetLastError__errCode: dd 0
Console__stderr_nStdHandle: dd 0
Console__stdout_nStdHandle: dd 0
FormatMessage__tmpReturnBuffer: times 256 db 0
FormatMessage__tmpReturnBufferLength: dd 0
Console__bytesWritten: dd 0
Generic__uuid: db "e44d7545-f9df-418e-bc37-11ad4535d32f",0
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
MainWindow_1.hbrBackground dq 5 ; HBRUSH
MainWindow_1.lpszMenuName dq 0 ; LPCSTR
MainWindow_1.lpszClassName dq Generic__uuid ; LPCSTR
MainWindow_1.hIconSm dq 0 ; HICON

CreateWindow__atom_name: dq 0
CreateWindow__hWnd: dq 0
CreateWindow__title: db "OpenGL Demo",0

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
debug_trace_4: db "PeekMessageA has messages for CreateWindow__hWnd %1!.16llX!",10,0
__trace_array: times 8 dq 0
PeekMessage_msgIdFormatString: db 10,"Message received:",10,"  hwnd: %1!.16llX!",10,"  message: %2!.4llX!",10,"  wParam: %3!.16llX!",10,"  lParam: %4!.16llX!",10,"  time: %5!.16llX!",10,"  pt.x: %6!lu!",10,"  pt.y: %7!lu!",10,"  lPrivate: %8!.8llX!",10,0
debug_trace_1: db "WM_QUIT received by main Loop.",10,0
debug_trace_5: db "TranslateMessage",10,0
debug_trace_5a: db "DispatchMessageA",10,0
nWndProc__hWnd: dq 0
nWndProc__uMsg: dq 0
nWndProc__wParam: dq 0
nWndProc__lParam: dq 0
nWndProc__return: dq 0
debug_trace_2: db "WM_CLOSE received by WndProc.",10,0
debug_trace_7: db "DestroyWindow sent",10,0
debug_trace_3: db "WM_DESTROY received by WndProc.",10,0
debug_trace_8: db "PostQuitMessage sent",10,0

section .text align=16
global main
main:

; get pointers to stdout/stderr pipes
call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword ecx, -12 ; 1st: DWORD nStdHandle = STD_ERROR_HANDLE
    call GetStdHandle
mov dword [Console__stderr_nStdHandle], eax ; return 
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword ecx, -11 ; 1st: DWORD nStdHandle = STD_OUTPUT_HANDLE
    call GetStdHandle
mov dword [Console__stdout_nStdHandle], eax ; return 
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check


call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword r8d, Generic__uuid ; 3rd: LPCSTR lpName
mov dword edx, 1 ; 2nd: BOOL bInitialOwner
mov dword ecx, 0 ; 1st: LPSECURITY_ATTRIBUTES lpMutexAttributes
    call CreateMutexA
mov qword [CreateMutexA__handle], rax ; return HANDLE
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword ecx, 0 ; 1st: LPCSTR lpModuleName
    call GetModuleHandleA
mov qword [GetModuleHandleA__hModule], rax ; return HMODULE *phModule
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 56 ; allocate shadow space
mov dword [rsp + 40], 0x8040 ; 6th: UINT fuLoad = LR_SHARED | LR_DEFAULTSIZE
mov dword [rsp + 32], 0 ; 5th: int cy
mov dword r9d, 0 ; 4th: int cx
mov dword r8d, 1 ; 3rd: UINT type = IMAGE_ICON
mov dword edx, 32517 ; 2nd: LPCSTR name = OIC_WINLOGO
mov dword ecx, 0 ; 1st: HINSTANCE hInst
    call LoadImageA
mov qword [CreateWindow__icon], rax ; return HANDLE
add rsp, 56 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 56 ; allocate shadow space
mov dword [rsp + 40], 0x8040 ; 6th: UINT fuLoad = LR_SHARED | LR_DEFAULTSIZE
mov dword [rsp + 32], 0 ; 5th: int cy
mov dword r9d, 0 ; 4th: int cx
mov dword r8d, 2 ; 3rd: UINT type = IMAGE_CURSOR
mov dword edx, 32512 ; 2nd: LPCSTR name = IDC_ARROW
mov dword ecx, 0 ; 1st: HINSTANCE hInst
    call LoadImageA
mov qword [CreateWindow__cursor], rax ; return HANDLE
add rsp, 56 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov qword rcx, MainWindow_1 ; 1st: WNDCLASSEXA *Arg1
    call RegisterClassExA
mov qword [CreateWindow__atom_name], rax ; return 
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
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
mov qword r9, 0x16cf0000 ; 4th: DWORD dwStyle = WS_OVERLAPPEDWINDOW | WS_VISIBLE | WS_CLIPCHILDREN | WS_CLIPSIBLINGS
mov qword r8, CreateWindow__title ; 3rd: LPCSTR lpWindowName
mov qword rdx, Generic__uuid ; 2nd: LPCSTR lpClassName
mov qword rcx, 768 ; 1st: DWORD dwExStyle = WS_EX_OVERLAPPEDWINDOW
    call CreateWindowExA
mov qword [CreateWindow__hWnd], rax ; return HWND
add rsp, 104 ; deallocate shadow space
call near GetLastError__epilogue_check

Loop:
call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 48 ; allocate shadow space
mov dword [rsp + 32], 1 ; 5th: UINT wRemoveMsg = PM_REMOVE
mov dword r9d, 0 ; 4th: UINT wMsgFilterMax
mov dword r8d, 0 ; 3rd: UINT wMsgFilterMin
mov qword rdx, [CreateWindow__hWnd] ; 2nd: HWND hWnd
mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
    call PeekMessageA
mov dword [PeekMessage_hasMsgs], eax ; return BOOL
add rsp, 48 ; deallocate shadow space
call near GetLastError__epilogue_check

cmp dword [PeekMessage_hasMsgs], 0 ; zero messages
je near Loop
; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], CreateWindow__hWnd ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, debug_trace_4 ; 2nd: LPCVOID lpSource
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

mov qword rax, [IncomingMessage_1.hwnd]
mov qword [__trace_array + 0], rax
mov dword eax, [IncomingMessage_1.message]
mov dword [__trace_array + 8], eax
mov qword rax, [IncomingMessage_1.wParam]
mov qword [__trace_array + 16], rax
mov qword rax, [IncomingMessage_1.lParam]
mov qword [__trace_array + 24], rax
mov dword eax, [IncomingMessage_1.pt.x]
mov dword [__trace_array + 32], eax
mov dword eax, [IncomingMessage_1.pt.y]
mov dword [__trace_array + 40], eax
mov dword eax, [IncomingMessage_1.lPrivate]
mov dword [__trace_array + 48], eax
; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], __trace_array ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, PeekMessage_msgIdFormatString ; 2nd: LPCVOID lpSource
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

cmp dword [IncomingMessage_1.message], 0x12 ; WM_QUIT
jne near ..@Loop__processMessage
; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], 0 ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, debug_trace_1 ; 2nd: LPCVOID lpSource
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

je near Loop
..@Loop__processMessage:
; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], 0 ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, debug_trace_5 ; 2nd: LPCVOID lpSource
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

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
    call TranslateMessage
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], 0 ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, debug_trace_5a ; 2nd: LPCVOID lpSource
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

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
    call DispatchMessageA
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

jmp near Loop

WndProc:
mov qword [nWndProc__hWnd], rcx
mov qword [nWndProc__uMsg], rdx
mov qword [nWndProc__wParam], r8
mov qword [nWndProc__lParam], r9
cmp rdx, 0x6
je near WndProc__WM_Activate
cmp rdx, 0x112
je near WndProc__WM_SysCommand
cmp rdx, 0x10
je near WndProc__WM_Close
cmp rdx, 0x100
je near WndProc__WM_KeyDown
cmp rdx, 0x101
je near WndProc__WM_KeyUp
cmp rdx, 0x5
je near WndProc__WM_Size
..@WndProc__default:
call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov qword r9, [nWndProc__lParam] ; 4th: 
mov qword r8, [nWndProc__wParam] ; 3rd: 
mov qword rdx, [nWndProc__uMsg] ; 2nd: 
mov qword rcx, [nWndProc__hWnd] ; 1st: 
    call DefWindowProcA
mov qword [nWndProc__return], rax ; return 
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

mov qword rax, [nWndProc__return]
ret
WndProc__WM_Activate:
xor eax, eax
ret
WndProc__WM_SysCommand:
mov ebx, [nWndProc__wParam]
cmp ebx, 0xf140
je near ..@return_zero
cmp ebx, 0xf170
je near ..@return_zero
jmp near ..@WndProc__default
..@return_zero:
xor eax, eax
ret
WndProc__WM_Close:
; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], 0 ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, debug_trace_2 ; 2nd: LPCVOID lpSource
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

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov qword rcx, [CreateWindow__hWnd] ; 1st: HWND hWnd
    call DestroyWindow
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], 0 ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, debug_trace_7 ; 2nd: LPCVOID lpSource
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

xor eax, eax
ret
WndProc__WM_Destroy:
; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], 0 ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, debug_trace_3 ; 2nd: LPCVOID lpSource
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

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword ecx, 0 ; 1st: int nExitCode
    call PostQuitMessage
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

; MS __fastcall x64 ABI
sub rsp, 64 ; allocate shadow space
mov qword [rsp + 48], 0 ; 7th: va_list *Arguments
mov qword [rsp + 40], 256 ; 6th: DWORD nSize
mov qword [rsp + 32], FormatMessage__tmpReturnBuffer ; 5th: LPSTR lpBuffer
mov dword r9d, 0x0 ; 4th: DWORD dwLanguageId
mov dword r8d, 0 ; 3rd: DWORD dwMessageId
mov dword edx, debug_trace_8 ; 2nd: LPCVOID lpSource
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

xor eax, eax
ret
WndProc__WM_KeyDown:
xor eax, eax
ret
WndProc__WM_KeyUp:
xor eax, eax
ret
WndProc__WM_Size:
xor eax, eax
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
jmp near Exit

Exit:
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
    call ExitProcess
add rsp, 40 ; deallocate shadow space


