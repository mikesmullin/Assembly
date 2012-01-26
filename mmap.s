.include "defines.h"

.data
fd:
	.long 	0
fdlen:
	.long 	0
mappedptr:
	.long 	0

.text
.globl _start
_start:
	subl	$24,%esp	// to arrange args on the stack. without
				// push/pops

//	open($file, $O_RDONLY);
	movl	$SYS_open,%eax
	movl	32(%esp),%ebx	// argv[1] is at %esp+8+24
	xorl	%ecx,%ecx	// set %ecx to O_RDONLY, which = 0
	int 	$0x80

	test	%eax,%eax	// if return value < 0, exit
	js	exit

	movl	%eax,fd		// save fd

//	lseek($fd,0,$SEEK_END);
	movl	%eax,%ebx
	xorl	%ecx,%ecx	// set offset to 0
	movl	$SEEK_END,%edx
	movl	$SYS_lseek,%eax
	int	$0x80

	movl	%eax,fdlen	// save file length

	xorl	%edx,%edx

//	mmap(NULL,$fdlen,PROT_READ,MAP_SHARED,$fd,0);
	movl	%edx,(%esp)
	movl	%eax,4(%esp)
	movl	$PROT_READ,8(%esp)
	movl	$MAP_SHARED,12(%esp)
	movl	fd,%eax
	movl	%eax,16(%esp)
	movl	%edx,20(%esp) 

	movl	$SYS_mmap,%eax
	movl	%esp,%ebx
	int	$0x80

	movl	%eax,mappedptr	// save ptr
		
// 	write($STDOUT, $mappedptr, $fdlen);
	movl	$STDOUT,%ebx
	movl	%eax,%ecx
	movl	fdlen,%edx
	movl	$SYS_write,%eax
	int	$0x80

//	munmap($mappedptr, $fdlen);
	movl	mappedptr,%ebx
	movl	fdlen,%ecx
	movl	$SYS_munmap,%eax
	int	$0x80

//	close($fd);
	movl	fd,%ebx		// load file descriptor
	movl	$SYS_close,%eax
	int	$0x80
exit:
//	exit(0);
	movl	$SYS_exit,%eax
	xorl	%ebx,%ebx
	int	$0x80

	ret
