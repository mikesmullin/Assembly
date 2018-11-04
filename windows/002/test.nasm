; this is a cute minimalist MessageBox and LoadLibrary opengl32.dll example

extern LoadLibraryA
extern MessageBoxA
extern ExitProcess

section .data
LIB01: db "opengl32.dll",0
MSG01: db "Hello",0
;TMP01: times 20 db 0

section .text
main:
; Critical software requirement of Windows:
; must align the stack to a multiple of 16 bytes
; TODO: find out if this has to be done at the entry of every call, or just this one
and RSP, 0FFFFFFFFFFFFFFF0h
;sub RSP, 32 ; 32 bytes of shadow space

; x64 uses __fastcall calling convention:
; The first two DWORD or smaller arguments that are found in the argument list 
; from left to right are passed in ECX and EDX registers; all other arguments are
; passed on the stack from right to left.

;xor rax,rax
mov rcx, LIB01
call LoadLibraryA
;mov [TMP01], rax

.DisplayMessageBox:
xor   rcx, rcx      ; 1st parameter
lea   rdx, [MSG01]  ; 2nd parameter
lea   R8, [MSG01]   ; 3rd parameter
mov   R9, 0         ; 4th parameter
call  MessageBoxA

;add RSP, 32 ; undo 32 bytes of shadow space

;exit:
xor rcx, rcx
call ExitProcess
;jmp exit

global main