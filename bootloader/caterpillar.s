  .globl  main
  .type main,@function
main:
  pushq  %rbp
  movq  %rsp, %rbp

  subq  $16, %rsp
  movq  $0x65746143, -16(%rbp)
  movq  $0x6c697072, -12(%rbp)
  movq  $0x0072616c, -8(%rbp)
  movq  $0x11111111, -4(%rbp)

  leaq  -16(%rbp), %rdi
  call  puts

  movl  $0, %eax
  leave
  ret
