const wglCreateContext = (
	Arg1/*:string*/,
) => ({
	proc: '[wglCreateContext]',
	args: [
		{ value: Arg1, size: 'qword', comment: 'HDC Arg1' },
	],
	ret: { size: 'qword', comment: 'HGLRC' },
});

const wglMakeCurrent = (
	hdc/*:string*/,
	hglrc/*:string*/
) => ({
	proc: '[wglMakeCurrent]',
	args: [
		{ value: hdc, size: 'qword', comment: 'HDC' },
		{ value: hglrc, size: 'qword', comment: 'HGLRC' },
	],
	ret: { size: 'dword', comment: 'BOOL' },
});

const glClearColor = (
	red/*:number*/,
	green/*:number*/,
	blue/*:number*/,
	alpha/*:number*/
) => ({
	convention: __ms_64_fastcall,
	proc: '[glClearColor]',
	args: [
		{ float: makefloat(red), size: 'qword', comment: 'GLclampf red' },
		{ float: makefloat(green), size: 'qword', comment: 'GLclampf green' },
		{ float: makefloat(blue),  size: 'qword', comment: 'GLclampf blue' },
		{ float: makefloat(alpha),  size: 'qword', comment: 'GLclampf alpha' },
	],
});

const GL_COLOR_BUFFER_BIT = 0x00004000;
// see: https://www.khronos.org/registry/OpenGL/api/GLES2/gl2.h

const glClear = (
	mask/*:number*/,
) => ({
	convention: __ms_64_fastcall,
	proc: '[glClear]',
	args: [
		{ value: mask, size: 'dword', comment: 'GLbitfield mask' },
	],
});

module.exports = {
	wglCreateContext,
	wglMakeCurrent,
	glClearColor,
	glClear,
};