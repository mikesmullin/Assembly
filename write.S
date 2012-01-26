// ssize_t write(int fd, const void *buf, size_t count);
// write(fileno(stdout), "hello", 6);
.include "defines.h"

.data
hello:
	.string "hello world\n"
.text
.globl _start
_start:
	movl	$SYS_write,%eax	// SYS_write = 4
	movl	$STDOUT,%ebx	// fd = fileno(stdio)
	movl	$hello,%ecx	// buf = str
	movl	$12,%edx	// count = 0x6
	int	$0x80

	movl	$SYS_exit,%eax
	xorl	%ebx,%ebx
	int	$0x80
	ret
