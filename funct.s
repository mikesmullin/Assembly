.section .data

.equ SYS_WRITE, 1
.equ SYS_EXIT, 60
.equ STDOUT, 1

_startStr:
   .ascii "in _start\n\0"
functStr:
   .ascii "in functOne\n\0"

.section .text

.type functOne, @function
functOne:
                          # begin procedure prolog
   pushq %rbp             # save the base pointer
   movq  %rsp, %rbp       # make the stack pointer the base pointer
                          # end procedure prolog
   movq  $SYS_WRITE, %rax # mov WRITE(4) into eax
   movq  $12, %rdx        # length of the string
   movq  $functStr, %rsi  # address of our string
   movq  $STDOUT, %rdi    # writing to stdout
   syscall                # call the kernel
                          # begin procedure epilog
   movq  %rbp, %rsp       # restore the stack pointer
   popq  %rbp             # restore the base pointer
   ret

.globl _start
_start:
   nop                    # so our breakpoint will break in gdb
   movq $SYS_WRITE, %rax  # mov WRITE(4) into eax
   movq $10, %rdx         # length of the string
   movq $_startStr, %rsi  # address of our string
   movq $STDOUT, %rdi     # writing to stdout
   syscall                # call the kernel
   call functOne          # call functOne
   movq $SYS_WRITE, %rax  # mov WRITE(4) into eax
   movq $10, %rdx         # length of the string
   movq $_startStr, %rsi  # address of our string
   movq $STDOUT, %rdi     # writing to stdout
   syscall                # call the kernel
   movq $SYS_EXIT, %rax   # mov EXIT(1) into eax
   movq $0, %rdi          # 0 is the return value
   syscall                # call the kernel
