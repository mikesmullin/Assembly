.include "defines.h"

.text
.globl	_start
_start:
	sub	$12,%esp

//	socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);
	movl	$AF_INET,(%esp)
	movl	$SOCK_STREAM,4(%esp)
	movl	$IPPROTO_TCP,8(%esp)

	movl	$SYS_socketcall,%eax
	movl	$SYS_socketcall_socket,%ebx
	movl	%esp,%ecx
	int	$0x80

	movl 	$SYS_exit,%eax
	xorl 	%ebx,%ebx
	int 	$0x80
	ret
