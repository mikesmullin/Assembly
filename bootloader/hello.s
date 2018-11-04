# -----------------------------------------------------------------------------
# A 64-bit Linux standalone program that writes "Hello, World" to the console
# using system calls only.  The program does not need to link with any external
# libraries at all.
#
#   see also: http://cs.lmu.edu/~ray/notes/linuxsyscalls/
#
# System calls used:
#   1: write(fileid, bufferAddress, numberOfBytes)
#   60: exit(returnCode)
#
# Assemble:
#     gcc -c hello.s
# Link:
#     ld hello.o (to produce a.out)
#     (or) ld -o hello hello.o (to produce hello)
#
# Or, you can assemble and link in one step:
#     gcc -nostdlib hello.s
#
# The symbol _start is the default entry point for ld.
# -----------------------------------------------------------------------------

        .global _start

        .text
_start:
        # write(1, message, 14)
        mov     $1, %rax                # system call 1 is write
        mov     $1, %rdi                # file handle 1 is stdout
        mov     $message, %rsi          # address of string to output
        mov     $14, %rdx               # number of bytes
        syscall                         # invoke operating system to do the write

        # exit(0)
        mov     $60, %rax               # system call 60 is exit
        xor     %rdi, %rdi              # we want return code 0
        syscall                         # invoke operating system to exit
message:
        .ascii  "Hello, world!\n"
