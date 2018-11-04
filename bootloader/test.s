  .globl  example
  .type  example, @function
example:
  pushq  %rbp
  movq  %rsp, %rbp
  movl  %edi, -4(%rbp)
  movl  %esi, -8(%rbp)
  movl  -8(%rbp), %eax
  movl  -4(%rbp), %edx
  addl  %edx, %eax
  addl  $3, %eax
  popq  %rbp
  ret
.LFE0:
  .size  example, .-example
  .section  .rodata
.LC0:
  .string  "%i\n"

  .globl  main
  .type  main, @function
main:
  pushq  %rbp
  movq  %rsp, %rbp
  movl  $2, %esi
  movl  $1, %edi
  call  example
  movl  %eax, %edx
  movl  $.LC0, %eax
  movl  %edx, %esi
  movq  %rax, %rdi
  movl  $0, %eax
  call  printf
  movl  $0, %eax
  popq  %rbp
  ret
