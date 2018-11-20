; build window
extern GetModuleHandleA
extern CreateMutexA
extern LoadImageA
extern RegisterClassExA
extern CreateWindowExA
extern ShowWindow
extern UpdateWindow

; main loop
extern PeekMessageA
extern TranslateMessage
extern DispatchMessageA
extern DefWindowProcA
extern PostQuitMessage

; shutdown/cleanup
extern LocalFree
extern ExitProcess

; error handling
extern SetLastError
extern GetLastError
extern FormatMessageA
extern GetStdHandle
extern LocalSize
extern WriteFile

section .data align=16
GetLastError__errCode: dd 0
GetLastError__msgLen: dd 0
GetLastError__msgBuf: dq 0
Console__stderr_nStdHandle: dd 0
Console__stdout_nStdHandle: dd 0
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
CreateWindow__hwnd: dq 0
CreateWindow__title: db "OpenGL Demo",0
ShowWindow__result: dd 0
UpdateWindow__result: dd 0

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

padding: times 4 db 0 ; not sure why padding is needed here but must find out proper alignment: db 0
dot: db "."
PeekMessage_hasMsgs: dd 0
nWndProc__hWnd: dq 0
nWndProc__uMsg: dq 0
nWndProc__wParam: dq 0
nWndProc__lParam: dq 0
nWndProc__return: dq 0

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
mov qword [CreateWindow__hwnd], rax ; return HWND
add rsp, 104 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword edx, 1 ; 2nd: int  nCmdShow
mov qword rcx, [CreateWindow__hwnd] ; 1st: HWND hWnd
call ShowWindow
mov dword [ShowWindow__result], eax ; return BOOL
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov qword rcx, [CreateWindow__hwnd] ; 1st: HWND hWnd
call UpdateWindow
mov dword [UpdateWindow__result], eax ; return BOOL
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

Loop:
call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 48 ; allocate shadow space
mov dword [rsp + 32], 1 ; 5th: UINT wRemoveMsg = PM_REMOVE
mov dword r9d, 0 ; 4th: UINT wMsgFilterMax
mov dword r8d, 0 ; 3rd: UINT wMsgFilterMin
mov qword rdx, [CreateWindow__hwnd] ; 2nd: HWND hWnd
mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
call PeekMessageA
mov dword [PeekMessage_hasMsgs], eax ; return BOOL
add rsp, 48 ; deallocate shadow space
call near GetLastError__epilogue_check

cmp dword [PeekMessage_hasMsgs], 0 ; zero messages
je near Loop
cmp dword [IncomingMessage_1.message], 0x12 ; WM_QUIT
jne near ..@Loop__processMessage
; MS __fastcall x64 ABI
sub rsp, 48 ; allocate shadow space
mov dword [rsp + 32], 0 ; 5th: LPOVERLAPPED lpOverlapped
mov dword r9d, Console__bytesWritten ; 4th: LPDWORD lpNumberOfBytesWritten
mov dword r8d, 1 ; 3rd: DWORD nNumberOfBytesToWrite
mov dword edx, dot ; 2nd: LPCVOID lpBuffer
mov dword ecx, [Console__stdout_nStdHandle] ; 1st: HANDLE hFile
call WriteFile
add rsp, 48 ; deallocate shadow space

mov ecx, 0 ; UINT uExitCode
jmp near Exit

..@Loop__processMessage:
call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
call TranslateMessage
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov qword rcx, IncomingMessage_1 ; 1st: LPMSG lpMsg
call DispatchMessageA
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

je near Loop

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
call near GetLastError__prologue_reset
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword ecx, 0 ; 1st: int nExitCode
call PostQuitMessage
add rsp, 40 ; deallocate shadow space
call near GetLastError__epilogue_check

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
mov dword [rsp + 48], 0 ; 7th: va_list *Arguments
mov dword [rsp + 40], 0 ; 6th: DWORD nSize
mov dword [rsp + 32], GetLastError__msgBuf ; 5th: LPSTR lpBuffer
mov dword r9d, 0x400 ; 4th: DWORD dwLanguageId = LANG_USER_DEFAULT, SUBLANG_DEFAULT
mov dword r8d, [GetLastError__errCode] ; 3rd: DWORD dwMessageId
mov dword edx, 0 ; 2nd: LPCVOID lpSource
mov dword ecx, 0x1300 ; 1st: DWORD dwFlags = FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS
call FormatMessageA
add rsp, 64 ; deallocate shadow space

; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword ecx, [GetLastError__msgBuf] ; 1st: HLOCAL hMem
call LocalSize
mov dword [GetLastError__msgLen], eax ; return 
add rsp, 40 ; deallocate shadow space
; MS __fastcall x64 ABI
sub rsp, 48 ; allocate shadow space
mov dword [rsp + 32], 0 ; 5th: LPOVERLAPPED lpOverlapped
mov dword r9d, Console__bytesWritten ; 4th: LPDWORD lpNumberOfBytesWritten
mov dword r8d, [GetLastError__msgLen] ; 3rd: DWORD nNumberOfBytesToWrite
mov dword edx, [GetLastError__msgBuf] ; 2nd: LPCVOID lpBuffer
mov dword ecx, [Console__stdout_nStdHandle] ; 1st: HANDLE hFile
call WriteFile
add rsp, 48 ; deallocate shadow space

; cleanup
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
mov dword ecx, [GetLastError__msgBuf] ; 1st: _Frees_ptr_opt_ HLOCAL hMem
call LocalFree
add rsp, 40 ; deallocate shadow space
mov ecx, [GetLastError__errCode] ; UINT uExitCode
jmp near Exit

Exit:
; MS __fastcall x64 ABI
sub rsp, 40 ; allocate shadow space
call ExitProcess
add rsp, 40 ; deallocate shadow space


