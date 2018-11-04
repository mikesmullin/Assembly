; print to console (windows 64-bit)
; i like this one because its simple,
; it uses only kernel32.dll (the minimum dependencies; no msvcrt)
; and no fancy macros required, even

extern GetStdHandle
extern WriteFile
extern ExitProcess

section .data
nStdHandle: dd 0
lpNumberOfBytesWritten: dd 0
str: db "Mike is the best!",10
strlen: equ $-str

section .text
global main
main:
mov rbp, rsp; for correct debugging

; http://msdn.microsoft.com/en-us/library/windows/desktop/ms683231.aspx
; HANDLE WINAPI GetStdHandle(
;   _In_  DWORD nStdHandle
; );
mov ecx, -11 ; STD_OUTPUT_HANDLE
call GetStdHandle
mov [nStdHandle], eax

; http://msdn.microsoft.com/en-us/library/windows/desktop/aa365747.aspx
; BOOL WINAPI WriteFile(
;   _In_         HANDLE hFile,
;   _In_         LPCVOID lpBuffer,
;   _In_         DWORD nNumberOfBytesToWrite,
;   _Out_opt_    LPDWORD lpNumberOfBytesWritten,
;   _Inout_opt_  LPOVERLAPPED lpOverlapped
; );
mov ecx, [nStdHandle] ; hFile (result from GetStdHandle)
mov edx, str        ; lpBuffer
mov r8d, strlen     ; nNumberOfBytesToWrite
mov r9d, lpNumberOfBytesWritten ; lpNumberOfBytesWritten
push 0              ; lpOverlapped
call WriteFile

; ExitProcess(0)
xor ecx, ecx
call ExitProcess

