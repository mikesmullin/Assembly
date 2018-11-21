// TODO: could make it so i don't check for GetLastError unless a proc returns 0

const build = () => {
	section = 'preprocessor';
	asm(
`; GOAL: Render OpenGL Context (either blue screen or full-color spinning cube)\n`);

	asm('; build window');
	asm('extern GetModuleHandleA');
	asm('extern CreateMutexA');
	asm('extern LoadImageA');
	asm('extern RegisterClassExA');
	asm('extern CreateWindowExA');

	asm('; build opengl context');
	asm('extern GetDC');
	asm('extern ChoosePixelFormat');
	asm('extern SetPixelFormat');
	asm('extern LoadLibraryA');
	asm('extern GetProcAddress');

	asm('\n; main loop');
	asm('extern PeekMessageA');
	asm('extern TranslateMessage');
	asm('extern DispatchMessageA');
	asm('extern DefWindowProcA');
	asm('extern SwapBuffers');
	
	asm(`\n; shutdown/cleanup`);
	asm('extern DestroyWindow');
	asm('extern PostQuitMessage');
	asm('extern ExitProcess');

	asm(`\n; error handling`);
	asm('extern SetLastError');
	asm('extern GetLastError');
	asm('extern FormatMessageA');
	asm('extern GetStdHandle');
	asm('extern WriteFile');

	section = '.text';
	asm('global main');
	asm('main:');
	
	asm(BLOCKS.INIT);

	// generic reusable uuid any time an api function wants a string identifier
	_var('Generic__uuid', 'db', '"07b62314-d4fc-4704-96e8-c31eb378d815",0');

	// verify the window is not open twice
	_var('CreateMutexA__handle', 'dq');
	asm(__ms_64_fastcall_w_error_check({ proc: 'CreateMutexA',
		args: [
			{ value: 0, size: 'dword', comment: 'LPSECURITY_ATTRIBUTES lpMutexAttributes' },
			{ value: 1, size: 'dword', comment: 'BOOL bInitialOwner' },
			{ value: 'Generic__uuid', size: 'dword', comment: 'LPCSTR lpName' },
		],
		ret: { value: '[CreateMutexA__handle]', size: 'qword', comment: 'HANDLE' },
	}));

	// get a pointer to this process for use with api functions which require it
	// Note that as of 32-bit Windows, an instance handle (HINSTANCE), such as the
	// application instance handle exposed by system function call of WinMain, and
	// a module handle (HMODULE) are the same thing.
	_var('GetModuleHandleA__hModule', 'dq');
	asm(__ms_64_fastcall_w_error_check({ proc: 'GetModuleHandleA',
		ret: { value: '[GetModuleHandleA__hModule]', size: 'qword', comment: 'HMODULE *phModule' },
		args: [
			{ value: 0, size: 'dword', comment: 'LPCSTR lpModuleName' }
		]
	}));

	// load references to the default icons for new windows
	const OIC_WINLOGO = 32517;
	const IDC_ARROW = 32512;
	// see: https://github.com/tpn/winsdk-10/blob/master/Include/10.0.10240.0/um/WinUser.h#L10635
	const IMAGE_ICON = 1;
	const IMAGE_CURSOR = 2;
	const LR_DEFAULTSIZE = 0x00000040;
	const LR_SHARED = 0x00008000;
	// see: https://docs.microsoft.com/en-us/windows/desktop/api/winuser/nf-winuser-loadicona
	_var('CreateWindow__icon', 'dq');
	_var('CreateWindow__cursor', 'dq');
	asm(__ms_64_fastcall_w_error_check({ proc: 'LoadImageA',
		args: [
			{ value: 0, size: 'dword', comment: 'HINSTANCE hInst' },
			{ value: OIC_WINLOGO, size: 'dword', comment: 'LPCSTR name = OIC_WINLOGO' },
			{ value: IMAGE_ICON, size: 'dword', comment: 'UINT type = IMAGE_ICON' },
			{ value: 0, size: 'dword', comment: 'int cx' },
			{ value: 0, size: 'dword', comment: 'int cy' },
			{ value: hex(LR_SHARED | LR_DEFAULTSIZE), size: 'dword', comment: 'UINT fuLoad = LR_SHARED | LR_DEFAULTSIZE' },
		],
		ret: { value: '[CreateWindow__icon]', size: 'qword', comment: 'HANDLE' },
	}));
	asm(__ms_64_fastcall_w_error_check({ proc: 'LoadImageA',
		args: [
			{ value: 0, size: 'dword', comment: 'HINSTANCE hInst' },
			{ value: IDC_ARROW, size: 'dword', comment: 'LPCSTR name = IDC_ARROW' },
			{ value: IMAGE_CURSOR, size: 'dword', comment: 'UINT type = IMAGE_CURSOR' },
			{ value: 0, size: 'dword', comment: 'int cx' },
			{ value: 0, size: 'dword', comment: 'int cy' },
			{ value: hex(LR_SHARED | LR_DEFAULTSIZE), size: 'dword', comment: 'UINT fuLoad = LR_SHARED | LR_DEFAULTSIZE' },
		],
		ret: { value: '[CreateWindow__cursor]', size: 'qword', comment: 'HANDLE' },
	}));

	// create the main local application window
	const CS_VREDRAW = 0x0001; 
	const CS_HREDRAW = 0x0002;
	const CS_OWNDC   = 0x0020; 
	// see: https://docs.microsoft.com/en-us/windows/desktop/winmsg/window-class-styles

	const MainWindow = istruct('MainWindow', STRUCTS.tagWNDCLASSEXA, {
		style: {
			value: hex(CS_OWNDC | CS_VREDRAW | CS_HREDRAW),
			comment: '= CS_OWNDC | CS_VREDRAW | CS_HREDRAW'
		},
		hInstance: 'GetModuleHandleA__hModule',
		// NOTICE: the name used there has to be the same as the one used for CreateWindow
		lpszClassName: `Generic__uuid`,
		lpfnWndProc: 'WndProc',
		hIcon: 'CreateWindow__icon',
		hCursor: 'CreateWindow__cursor',
	});
	_var('CreateWindow__atom_name', 'dq');
	asm(__ms_64_fastcall_w_error_check({ proc: 'RegisterClassExA',
		args: [
			{ value: MainWindow, size: 'qword', comment: 'WNDCLASSEXA *Arg1' },
		],
		ret: { value: '[CreateWindow__atom_name]', size: 'qword' },
	}));

	const WS_EX_WINDOWEDGE = 0x00000100;
	const WS_EX_CLIENTEDGE = 0x00000200;
	const WS_EX_OVERLAPPEDWINDOW = WS_EX_WINDOWEDGE | WS_EX_CLIENTEDGE;
	// see: https://docs.microsoft.com/en-us/windows/desktop/winmsg/extended-window-styles
	const WS_CLIPCHILDREN  = 0x02000000;
	const WS_CLIPSIBLINGS  = 0x04000000;
	const WS_VISIBLE       = 0x10000000;
	const WS_OVERLAPPED    = 0x00000000;
	const WS_CAPTION       = 0x00C00000;
	const WS_SYSMENU       = 0x00080000;
	const WS_THICKFRAME    = 0x00040000;
	const WS_MINIMIZEBOX   = 0x00020000;
	const WS_MAXIMIZEBOX   = 0x00010000;
	const WS_OVERLAPPEDWINDOW = WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX;
	// see: https://docs.microsoft.com/en-us/windows/desktop/winmsg/window-styles
	const CW_USEDEFAULT    = 0x80000000;
	// see: https://github.com/tpn/winsdk-10/blob/master/Include/10.0.10240.0/um/WinUser.h#L4292
	_var('CreateWindow__hWnd', 'dq');
	_var('CreateWindow__title', 'db', '"OpenGL Demo",0');
	const WIDTH = 640;
	const HEIGHT = 480;
	asm(__ms_64_fastcall_w_error_check({ proc: 'CreateWindowExA',
		args: [
			{ value: WS_EX_OVERLAPPEDWINDOW, size: 'qword', comment: 'DWORD dwExStyle = WS_EX_OVERLAPPEDWINDOW' },
			// NOTICE: the name used there has to be the same as the one used for RegisterClass
			{ value: 'Generic__uuid' /*'CreateWindow__atom_name'*/, size: 'qword', comment: 'LPCSTR lpClassName' },
			{ value: 'CreateWindow__title', size: 'qword', comment: 'LPCSTR lpWindowName' },
			{ value: hex(WS_OVERLAPPEDWINDOW | WS_VISIBLE | WS_CLIPCHILDREN | WS_CLIPSIBLINGS),
				size: 'qword', comment: 'DWORD dwStyle = WS_OVERLAPPEDWINDOW | WS_VISIBLE | WS_CLIPCHILDREN | WS_CLIPSIBLINGS' },
			{ value: hex(CW_USEDEFAULT), size: 'dword', comment: 'int X' },
			{ value: hex(CW_USEDEFAULT), size: 'dword', comment: 'int Y' },
			{ value: WIDTH, size: 'dword', comment: 'int nWidth' },
			{ value: HEIGHT, size: 'dword', comment: 'int nHeight' },
			{ value: 0, size: 'qword', comment: 'HWND hWndParent' },
			{ value: 0, size: 'qword', comment: 'HMENU hMenu' },
			{ value: 'GetModuleHandleA__hModule', size: 'qword', comment: 'HINSTANCE hInstance' },
			{ value: 0, size: 'qword', comment: 'LPVOID lpParam' },
		],
		ret: { value: '[CreateWindow__hWnd]', size: 'qword', comment: 'HWND' },
	}));

	// TODO: if full screen:
		// ChangeDisplaySettings
		// ShowCursor
	
	_var('GetDC__hDC', 'dq');
	asm(__ms_64_fastcall_w_error_check({ proc: 'GetDC',
		args: [
			{ value: '[CreateWindow__hWnd]', size: 'qword', comment: 'HWND hWnd' }
		],
		ret: { value: '[GetDC__hDC]', size: 'qword', comment: 'HDC' },
	}));

	// pixel format constant values were hard to find
	// i found no official source, only second-hand sources:
	// see: http://www.bvbcode.com/code/wmex83ko-286062
	// see: http://programarts.com/cfree_en/wingdi_h.html
	// see: https://java-native-access.github.io/jna/4.2.0/constant-values.html#com.sun.jna.platform.win32.WinGDI.PFD_SUPPORT_OPENGL
	const PFD_DRAW_TO_WINDOW = 0x00000004;
	const PFD_SUPPORT_OPENGL = 0x00000020;
	const PFD_DOUBLEBUFFER = 0x00000001;
	const PFD_TYPE_RGBA = 0;
	const PFD_MAIN_PLANE = 0;
	// see: https://docs.microsoft.com/en-us/windows/desktop/api/wingdi/ns-wingdi-tagpixelformatdescriptor
	const PixelFormat = istruct('PixelFormat', STRUCTS.PIXELFORMATDESCRIPTOR, {
		dwFlags: {
			value: hex(PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER),
			comment: '= PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER',
		},
		iPixelType: { value: PFD_TYPE_RGBA, comment: '= PFD_TYPE_RGBA' },
		// TODO: need to probably query the screen for this (color-depth)
		cColorBits: { value: 24, comment: '(24-bit color depth)' },
		cAlphaBits: { value: 0, comment: '(no alpha buffer)' },
		cAccumBits: { value: 0, comment: '(no accumulation buffer)' },
		cDepthBits: { value: 32, comment: '(32-bit z-buffer)' },
		cStencilBits: { value: 0, comment: '(no stencil buffer)' },
		cAuxBuffers: { value: 0, comment: '(no auxiliary buffer)' },
		iLayerType: { value: PFD_MAIN_PLANE, comment: '= PFD_MAIN_PLANE' },
	});

	_var('ChoosePixelFormat__format', 'dd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'ChoosePixelFormat',
		args: [
			{ value: '[GetDC__hDC]', size: 'qword', comment: 'HDC hdc' },
			{ value: PixelFormat, size: 'qword', comment: 'PIXELFORMATDESCRIPTOR *ppfd' },
		],
		ret: { value: '[ChoosePixelFormat__format]', size: 'dword', comment: 'int' },
	}));

	_var('SetPixelFormat__success', 'dd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'SetPixelFormat',
		args: [
			{ value: '[GetDC__hDC]', size: 'qword', comment: 'HDC hdc' },
			{ value: '[ChoosePixelFormat__format]', size: 'dword', comment: 'int format' },
			{ value: PixelFormat, size: 'qword', comment: 'PIXELFORMATDESCRIPTOR *ppfd' },
		],
		ret: { value: '[SetPixelFormat__success]', size: 'dword', comment: 'BOOL' },
	}));

	// dynamically import opengl lib
	asm(__import('opengl32',
		'wglCreateContext',
		'wglMakeCurrent',
		'glClearColor',
		'glClear',
		'glGetError',
	));

	_var('wglCreateContext__ctx', 'dq');
	asm(__ms_64_fastcall_w_error_check({ proc: '[wglCreateContext]',
		args: [
			{ value: '[GetDC__hDC]', size: 'qword', comment: 'HDC Arg1' },
		],
		ret: { value: '[wglCreateContext__ctx]', size: 'qword', comment: 'HGLRC' },
	}));

	_var('wglMakeCurrent__success', 'dd');
	asm(__ms_64_fastcall_w_error_check({ proc: '[wglMakeCurrent]',
		args: [
			{ value: '[GetDC__hDC]', size: 'qword', comment: 'HDC' },
			{ value: '[wglCreateContext__ctx]', size: 'qword', comment: 'HGLRC' },
		],
		ret: { value: '[wglMakeCurrent__success]', size: 'dword', comment: 'BOOL' },
	}));
	
	// asm('xor rax, rax');
	// asm('xor rbx, rbx');
	// asm('xor rcx, rcx');
	// asm('xor rdx, rdx');
	// asm('xor r8, r8');
	// asm('xor r9, r9');
	// asm('push qword 0');
	// asm('push qword 0');
	// asm('push qword 0');
	// asm('push qword 0');
	// TODO: make a fastcall fn that also calls glGetError
	_var('onef', 'dq', '0x3f800000');
	_var('zerof', 'dq', '0');
	asm(__ms_64_fastcall/*_w_glGetError*/({ proc: '[glClearColor]',
		args: [
			{ float: 'onef', size: 'qword', comment: 'GLclampf alpha' },
			{ float: 'zerof', size: 'qword', comment: 'GLclampf blue' },
			{ float: 'onef', size: 'qword', comment: 'GLclampf green' },
			{ float: 'onef', size: 'qword', comment: 'GLclampf red' },
		],
	}));

	// glShadeModel
	// glClearColor
	// glClearDepth
	// glEnable
	// glDepthFunc
	// glHint

	// on resize:
	// glViewport
	// glMatrixMode
	// glLoadIdentity
	// gluPerspective
	// glMatrixMode
	// glLoadIdentity
	
	// on draw loop:
	// glClear
	// glLoadIdentity
	// glEnd

	// on shutdown:
	// if fullscreen:
		// ChangeDisplaySettings
		// ShowCursor
	// wglMakeCurrent
	// wglDeleteContext
	// ReleaseDC
	// FreeLibrary opengl
	// DestroyWindow




	const IncomingMessage = istruct('IncomingMessage', STRUCTS.tagMSG, {});	

	asm('Loop:');
	const PM_REMOVE = 0x0001;
	_var('PeekMessage_hasMsgs', 'dd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'PeekMessageA',
		args: [
			{ value: IncomingMessage, size: 'qword', comment: 'LPMSG lpMsg' },
			{ value: '[CreateWindow__hWnd]', size: 'qword', comment: 'HWND hWnd' },
			{ value: 0, size: 'dword', comment: 'UINT wMsgFilterMin' },
			{ value: 0, size: 'dword', comment: 'UINT wMsgFilterMax' },
			{ value: PM_REMOVE, size: 'dword', comment: 'UINT wRemoveMsg = PM_REMOVE' },
		],
		ret: { value: '[PeekMessage_hasMsgs]', size: 'dword', comment: 'BOOL' },
	}));
	const WM_QUIT = 0x0012;
	asm('cmp dword [PeekMessage_hasMsgs], 0 ; zero messages');
	asm('je near ..@Render');

	asm(`cmp dword [${IncomingMessage}.message], ${hex(WM_QUIT)} ; WM_QUIT`);
	asm('jne near ..@Loop__processMessage');
	asm(exit(0));

	asm('..@Loop__processMessage:');
	asm(__ms_64_fastcall_w_error_check({ proc: 'TranslateMessage',
		args: [
			{ value: IncomingMessage, size: 'qword', comment: 'LPMSG lpMsg' },
		],
	}));

	asm(__ms_64_fastcall_w_error_check({ proc: 'DispatchMessageA',
		args: [
			{ value: IncomingMessage, size: 'qword', comment: 'LPMSG lpMsg' },
		],
	}));

	asm('..@Render:');

	const GL_COLOR_BUFFER_BIT = 0x00004000;
	// see: https://www.khronos.org/registry/OpenGL/api/GLES2/gl2.h
	asm(__ms_64_fastcall/*_w_glGetError*/({ proc: '[glClear]',
		args: [
			{ value: GL_COLOR_BUFFER_BIT, size: 'dword', comment: 'GLbitfield mask' },
		],
	}));

	_var('SwapBuffers__success', 'dd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'SwapBuffers',
		args: [
			{ value: '[GetDC__hDC]', size: 'qword', comment: 'HDC Arg1' },
		],
		ret: { value: '[SwapBuffers__success]', size: 'dword', comment: 'BOOL' },
	}));
	asm('jmp near Loop');




	
	const WM_ACTIVATE = 0x0006;
	const WM_SYSCOMMAND = 0x0112;
	const WM_CLOSE = 0x0010;
	const WM_DESTROY = 0x0002;
	const WM_KEYDOWN = 0x0100;
	const WM_KEYUP = 0x0101;
	const WM_SIZE = 0x0005;
	// see: https://github.com/tpn/winsdk-10/blob/master/Include/10.0.10240.0/um/WinUser.h#L1951
	const SC_SCREENSAVE = 0x0F140;
	const SC_MONITORPOWER = 0x0F170;
	asm('\nWndProc:');
	_var('nWndProc__hWnd', 'dq');
	_var('nWndProc__uMsg', 'dq');
	_var('nWndProc__wParam', 'dq');
	_var('nWndProc__lParam', 'dq');
	_var('nWndProc__return', 'dq');
	asm('mov qword [nWndProc__hWnd], rcx');
	asm('mov qword [nWndProc__uMsg], rdx');
	asm('mov qword [nWndProc__wParam], r8');
	asm('mov qword [nWndProc__lParam], r9');

	// switch uMsg
	asm(`cmp rdx, ${hex(WM_ACTIVATE)}`);
	asm('je near WndProc__WM_Activate');
	asm(`cmp rdx, ${hex(WM_SYSCOMMAND)}`);
	asm('je near WndProc__WM_SysCommand');
	asm(`cmp rdx, ${hex(WM_CLOSE)}`);
	asm('je near WndProc__WM_Close');
	asm(`cmp rdx, ${hex(WM_DESTROY)}`);
	asm('je near WndProc__WM_Destroy');
	asm(`cmp rdx, ${hex(WM_KEYDOWN)}`);
	asm('je near WndProc__WM_KeyDown');
	asm(`cmp rdx, ${hex(WM_KEYUP)}`);
	asm('je near WndProc__WM_KeyUp');
	asm(`cmp rdx, ${hex(WM_SIZE)}`);
	asm('je near WndProc__WM_Size');
	asm('..@WndProc__default:');
	asm(__ms_64_fastcall_w_error_check({ proc: 'DefWindowProcA',
		args: [
			{ value: '[nWndProc__hWnd]', size: 'qword' },
			{ value: '[nWndProc__uMsg]', size: 'qword' },
			{ value: '[nWndProc__wParam]', size: 'qword' },
			{ value: '[nWndProc__lParam]', size: 'qword' },
		],
		ret: { value: '[nWndProc__return]', size: 'qword' },
	}));
	asm('mov qword rax, [nWndProc__return]');
	asm('ret');
	asm('WndProc__WM_Activate:');
	asm('xor eax, eax');
	asm('ret');
	asm('WndProc__WM_SysCommand:');
	asm('mov ebx, [nWndProc__wParam]');
	asm(`cmp ebx, ${hex(SC_SCREENSAVE)}`);
	asm('je near ..@return_zero');
	asm(`cmp ebx, ${hex(SC_MONITORPOWER)}`);
	asm('je near ..@return_zero');
	asm('jmp near ..@WndProc__default');
	asm('..@return_zero:');
	asm('xor eax, eax');
	asm('ret');
	asm('WndProc__WM_Close:');
	asm(__ms_64_fastcall_w_error_check({ proc: 'DestroyWindow', args: [
		{ value: '[CreateWindow__hWnd]', size: 'qword', comment: 'HWND hWnd' },
	]}));
	asm('xor eax, eax');
	asm('ret');
	asm('WndProc__WM_Destroy:');
	asm(__ms_64_fastcall_w_error_check({ proc: 'PostQuitMessage', args: [
		{ value: 0, size: 'dword', comment: 'int nExitCode' },
	]}));
	asm('xor eax, eax');
	asm('ret');
	asm('WndProc__WM_KeyDown:');
	asm('xor eax, eax');
	asm('ret');
	asm('WndProc__WM_KeyUp:');
	asm('xor eax, eax');
	asm('ret');
	asm('WndProc__WM_Size:');
	asm('xor eax, eax');
	asm('ret');

	asm(BLOCKS.PROCS);
};


























// supporting preprocessor-esque macro functions

const fs = require('fs');
const path = require('path');
const _ = require('lodash');
const [,,OUT_FILE] = process.argv;
let section = 'preprocessor';
const sections = { preprocessor: '', '.data': '', '.text': '' };
const _var = (name, size='db', val=0) =>
	sections['.data'] += `${name}: ${size} ${val}\n`;
const asm = s => {
	sections[section] += s + "\n";
};
const out = () => {
	fs.writeFileSync(
		path.join(__dirname, OUT_FILE||'test.nasm'),
		_.map(sections, (text, section) =>
			('preprocessor' !== section ? `section ${section} align=16\n` : '')+
			`${text}\n`).join(''));
};
const BLOCKS = { INIT: '', PROCS: '' };
let _initializers = [];
const onready = cb => _initializers.push(cb);
const init = () => _initializers.forEach(cb=>cb());

const TYPES = {};
const STRUCTS = {};

TYPES.BYTE = { name: 'byte', size: 1 };
TYPES.WORD = { name: 'word', size: 2 };
TYPES.DWORD = { name: 'dword', size: 4 };
TYPES.QWORD = { name: 'qword', size: 8 };

TYPES.UINT = { name: 'UINT', size: 4 };
TYPES.WNDPROC = { name: 'WNDPROC', size: 8 };
TYPES.int = { name: 'int', size: 4 };
TYPES.HINSTANCE = { name: 'HINSTANCE', size: 8 };
TYPES.HICON = { name: 'HICON', size: 8 };
TYPES.HCURSOR = { name: 'HCURSOR', size: 8 };
TYPES.HBRUSH = { name: 'HBRUSH', size: 8 };
TYPES.LPCSTR = { name: 'LPCSTR', size: 8 };

TYPES.HWND = { name: 'HWND', size: 8 };
// TYPES.POINT = { name: 'POINT', size: 8 };
TYPES.WPARAM = { name: 'WPARAM', size: 8 };
TYPES.LPARAM = { name: 'LPARAM', size: 8 };

STRUCTS.tagWNDCLASSEXA = { name: 'tagWNDCLASSEXA', data: {
	cbSize: { type: TYPES.UINT, default: ()=>
		sizeof(STRUCTS.tagWNDCLASSEXA) },
	style: { type: TYPES.UINT },
	lpfnWndProc: { type: TYPES.WNDPROC },
	cbClsExtra: { type: TYPES.int, default: 0 },
	cbWndExtra: { type: TYPES.int, default: 0 },
	hInstance: { type: TYPES.HINSTANCE },
	hIcon: { type: TYPES.HICON, default: 0 },
	hCursor: { type: TYPES.HCURSOR },
	hbrBackground: { type: TYPES.HBRUSH, default: 0 }, // 0 is required for OpenGL Context
	lpszMenuName: { type: TYPES.LPCSTR, default: 0 },
	lpszClassName: { type: TYPES.LPCSTR },
	hIconSm: { type: TYPES.HICON, default: 0 },
}};

STRUCTS.tagMSG = { name: 'tagMSG', data: {
	hwnd: { type: TYPES.HWND, default: 0 },
  message: { type: TYPES.UINT, default: 0 },
  wParam: { type: TYPES.WPARAM, default: 0 },
  lParam: { type: TYPES.LPARAM, default: 0 },
  time: { type: TYPES.DWORD, default: 0 },
	'pt.x': { type: TYPES.DWORD, default: 0 },
	'pt.y': { type: TYPES.DWORD, default: 0 },
  lPrivate: { type: TYPES.DWORD, default: 0 },
}};

STRUCTS.PIXELFORMATDESCRIPTOR = { name: 'PIXELFORMATDESCRIPTOR', data: {
  nSize: { type: TYPES.WORD, default: ()=>
		({ value: sizeof(STRUCTS.PIXELFORMATDESCRIPTOR), comment: 'sizeof(struct)' }) },
  nVersion: { type: TYPES.WORD, default: { value: 1, comment: '(magic constant)' }},
  dwFlags: { type: TYPES.DWORD, default: 0 },
  iPixelType: { type: TYPES.BYTE, default: 0 },
  cColorBits: { type: TYPES.BYTE, default: 0 },
  cRedBits: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cRedShift: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cGreenBits: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cGreenShift: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cBlueBits: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cBlueShift: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cAlphaBits: { type: TYPES.BYTE, default: 0 },
  cAlphaShift: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cAccumBits: { type: TYPES.BYTE, default: 0 },
  cAccumRedBits: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cAccumGreenBits: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cAccumBlueBits: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cAccumAlphaBits: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  cDepthBits: { type: TYPES.BYTE, default: 0 },
  cStencilBits: { type: TYPES.BYTE, default: 0 },
  cAuxBuffers: { type: TYPES.BYTE, default: 0 },
  iLayerType: { type: TYPES.BYTE, default: 0 },
  bReserved: { type: TYPES.BYTE, default: { value: 0, comment: '(not used)' } },
  dwLayerMask: { type: TYPES.DWORD, default: { value: 0, comment: '(not used)' } },
  dwVisibleMask: { type: TYPES.DWORD, default: { value: 0, comment: '(not used)' } },
  dwDamageMask: { type: TYPES.DWORD, default: { value: 0, comment: '(not used)' } },
}};

const instance_counter = {};
const istruct = (name, struct, values) => {
	if (null == instance_counter[struct.name]) instance_counter[struct.name] = 0;
	instance_counter[struct.name]++;
	const label = `${name}_${instance_counter[struct.name]}`;
	sections['.data'] +=
		`\n; struct\n`+
		`${label}: ; instanceof ${struct.name}\n`;
	for (const k of Object.keys(struct.data)) {
		let possibleValue = _.get(values, k, _.get(struct.data, [k, 'default']));
		if ('function' === typeof possibleValue) possibleValue = possibleValue();
		if (null == possibleValue) throw new Error(`struct ${struct.name} ${label}.${k} is missing a required value.`);
		let value, comment;
		if ('object' === typeof possibleValue) {
			({ value, comment } = possibleValue);
		} else value = possibleValue;
		sections['.data'] +=
			`${label}.${k} `+
			`d${{1:'b', 2:'w', 4:'d', 8:'q'}[struct.data[k].type.size]} `+
			`${value} `+
			`; ${struct.data[k].type.name}${comment ? ' '+comment : ''}\n`;
	}
	sections['.data'] += `\n`;
	return label;
};
const sizeof = struct => {
	return _.map(struct.data, field =>
		field.type.size
	).reduce((sum,n)=>
		sum+=n,0);
};


const hex = n => '0x'+ n.toString(16);

const ___english_ordinal = i => {
	const s = i.toString();
	const n = parseInt(s.substr(-1), 10);
	return s + (1===n && i !== 11 ? 'st' : 2===n && i !== 12 ? 'nd' : 3===n && i !== 13 ? 'rd' : 'th');
};

_var('__tmp_float', 'dq');
/**
 * Windows 64-bit uses Microsoft fastcall
 * First four params are passed via registers (RCX, RDX, R8, R9), and the remainder on stack (before shadow space)
 * Caller also responsible to pad 'shadow space' of 4 x 64-bit registers (32 bytes) prior to the call,
 * and to unwind the shadow space after the call has returned.
 */
const __ms_64_fastcall = ({ proc, ret, args=[] }) => {
	let out = '; MS __fastcall x64 ABI\n';
	// allocate ms fastcall shadow space
	const shadow = Math.max(40, (
		args.length + // number of args
		1 // mystery padding; its what VC++ x64 does
		) * 8 // bytes
	);
	out += `sub rsp, ${shadow} ; allocate shadow space\n`;

	for (let i=args.length-1; i>-1; i--) {
		const arg = args[i];
		const pos = `${___english_ordinal(i+1)}: `;
		let registers;
		if (null != arg.float) {
			registers = [
				{ qword: 'xmm0' },
				{ qword: 'xmm1' },
				{ qword: 'xmm2' },
				{ qword: 'xmm3' },
			];
			if (i<=3) { // NOTICE: my fn does not support more than 4 float args right now
				out +=
					`mov qword [__tmp_float], ${arg.float}\n` +
					`addsd ${registers[i][arg.size]}, [__tmp_float] ; ${pos}${arg.comment||''}\n`;
			}
		}
		else {
			registers = [
				{ dword: 'ecx', qword: 'rcx' },
				{ dword: 'edx', qword: 'rdx' },
				{ dword: 'r8d', qword: 'r8'  },
				{ dword: 'r9d', qword: 'r9'  },
			];
			const mov = `mov ${arg.size}`;
			if (i>3) out += `${mov} [rsp + ${i * 8}], ${arg.value} ; ${pos}${arg.comment||''}\n`;
			else out += `${mov} ${registers[i][arg.size]}, ${arg.value} ; ${pos}${arg.comment||''}\n`;
		}
	}
	
	out += `    call ${proc}\n`;

	// handle return var, if provided
	if (null != ret) {
		out += `mov ${ret.size} ${ret.value}, ${{dword: 'eax', qword: 'rax'}[ret.size]} ; return ${ret.comment||''}\n`;
	}

	// deallocate ms fastcall shadow space
	out += `add rsp, ${shadow} ; deallocate shadow space\n`;
	return out;
};

_var('GetLastError__errCode', 'dd');
onready(()=>{
	BLOCKS.PROCS += `\n`+
		// ensure last error is 0
		// invoked before calling a function which may or may not have lasterror support
		// makes us more confident calling GetLastError after a procedure runs to ensure 
		// it was ok (if everything is still 0)
		'GetLastError__prologue_reset:\n' +
		__ms_64_fastcall({ proc: 'SetLastError', args: [
			{ value: 0, size: 'dword', comment: 'DWORD dwErrCode' }]}) +
		'ret\n\n' +

		'GetLastError__epilogue_check:\n' +
		__ms_64_fastcall({ proc: 'GetLastError',
			ret: { value: '[GetLastError__errCode]', size: 'dword' }}) +
		'cmp rax, 0\n' +
		'jne ..@error\n' +
		'ret\n\n' +

		'..@error:\n' +
		printf(GetErrorMessage('[GetLastError__errCode]'), Asm.Console.log) +'\n'+
		exit('[GetLastError__errCode]');
});
const __ms_64_fastcall_w_error_check = o => {
	let out = '';
	out += `call GetLastError__prologue_reset\n`;
	out += __ms_64_fastcall(o);
	out += `call GetLastError__epilogue_check\n`;
	return out;
};
_var('glGetError__code', 'dd');
onready(()=>{
	BLOCKS.PROCS += `\n`+
		'GetLastError__epilogue_glGetError:\n' +
		__ms_64_fastcall({ proc: '[glGetError]',
			ret: { value: '[glGetError__code]', size: 'dword', comment: 'GLenum' },
		}) +
		'cmp eax, 0\n'+
		'jne ..@glError\n' +
		'ret\n\n' +

		'..@glError:\n' +
		printf(FormatString('glGetError__str', `"glError %1!.8llX!",10`, 'glGetError__code'), Asm.Console.log) +
		exit('[glGetError__code]');
});
const __ms_64_fastcall_w_glGetError = o => {
	let out = '';
	out += __ms_64_fastcall(o);
	out += `call GetLastError__epilogue_glGetError\n`;
	return out;
};

onready(()=>{
	BLOCKS.PROCS += `\n`+
		'Exit:\n' +
		__ms_64_fastcall({ proc: 'ExitProcess' }) +'\n'+
		// the following _should_ be unnecessary if correctly exits
		'ret' +'\n'+
		'jmp near Exit';
});
const exit = (code=0) =>
	`mov ecx, ${code} ; UINT uExitCode\n` +
	// important to call vs. jmp so it appears in stack traces.
	// especially since exiting is a common response
	// to an error which you might want to debug!
	'call Exit\n'; 



const Asm = {};

Asm.Console = {};
Asm.Console.STD_OUTPUT_HANDLE = -11;
Asm.Console.STD_ERROR_HANDLE  = -12;
_var('Console__stderr_nStdHandle', 'dd');
_var('Console__stdout_nStdHandle', 'dd');
onready(()=>{
	BLOCKS.INIT += `\n`+
		`; get pointers to stdout/stderr pipes\n`+
		__ms_64_fastcall_w_error_check({ proc: 'GetStdHandle', 
			args: [
				{ value: Asm.Console.STD_ERROR_HANDLE, size: 'dword', comment: 'DWORD nStdHandle = STD_ERROR_HANDLE' },
			],
			ret: { value: '[Console__stderr_nStdHandle]', size: 'dword' },
		}) +`\n`+
		__ms_64_fastcall_w_error_check({ proc: 'GetStdHandle', 
			args: [
				{ value: Asm.Console.STD_OUTPUT_HANDLE, size: 'dword', comment: 'DWORD nStdHandle = STD_OUTPUT_HANDLE' },
			],
			ret: { value: '[Console__stdout_nStdHandle]', size: 'dword' },
		}) +`\n`;
	_var('Console__bytesWritten', 'dd');
});
Asm.Console._base = (pipe, str, len) =>
	__ms_64_fastcall({ proc: 'WriteFile',
		args: [
			{ value: `[Console__std${pipe}_nStdHandle]`, size: 'dword', comment: 'HANDLE hFile' },
			{ value: str, size: 'dword', comment: 'LPCVOID lpBuffer' },
			{ value: len, size: 'dword', comment: 'DWORD nNumberOfBytesToWrite' },
			{ value: 'Console__bytesWritten', size: 'dword', comment: 'LPDWORD lpNumberOfBytesWritten' },
			{ value: 0, size: 'dword', comment: 'LPOVERLAPPED lpOverlapped' },
		]
	});
Asm.Console.log = (str, len) => Asm.Console._base('out', str, len);
Asm.Console.error = (str, len) => Asm.Console._base('err', str, len);

const FORMAT_MESSAGE_FROM_SYSTEM = 0x00001000;
const FORMAT_MESSAGE_IGNORE_INSERTS = 0x00000200;
const FORMAT_MESSAGE_ARGUMENT_ARRAY = 0x00002000;
const FORMAT_MESSAGE_FROM_STRING = 0x00000400;
const LANG_USER_DEFAULT__SUBLANG_DEFAULT = 0x0400;
const FormatMessage__tmpReturnBufferLength = 256;
_var('FormatMessage__tmpReturnBuffer', `times ${FormatMessage__tmpReturnBufferLength} db`);
_var('FormatMessage__tmpReturnBufferLength', `dd`);
// see also: https://docs.microsoft.com/en-us/windows/desktop/api/winbase/nf-winbase-formatmessagea#remarks
const FormatMessage = (dwFlags, label, dwMessageId, dwLanguageId, args) => {
	return __ms_64_fastcall({ proc: 'FormatMessageA',
		args: [
			{ value: hex(dwFlags), size: 'dword', comment: 'DWORD dwFlags' },
			{ value: label, size: 'dword', comment: 'LPCVOID lpSource' },
			{ value: dwMessageId, size: 'dword', comment: 'DWORD dwMessageId' },
			{ value: hex(dwLanguageId), size: 'dword', comment: 'DWORD dwLanguageId' },
			{ value: 'FormatMessage__tmpReturnBuffer', size: 'qword', comment: 'LPSTR lpBuffer' },
			{ value: FormatMessage__tmpReturnBufferLength, size: 'qword', comment: 'DWORD nSize' },
			{ value: args, size: 'qword', comment: 'va_list *Arguments'},
		],
		ret: { value: '[FormatMessage__tmpReturnBufferLength]', size: 'dword', comment: 'DWORD TCHARs written' },
	})
}
const GetErrorMessage = dwMessageId => {
	return FormatMessage(
		FORMAT_MESSAGE_FROM_SYSTEM |
		FORMAT_MESSAGE_IGNORE_INSERTS,
		0,
		dwMessageId,
		LANG_USER_DEFAULT__SUBLANG_DEFAULT,
		0);
};
const FormatString = (formatStringLabel, formatString, arrayPtr) => {
	_var(formatStringLabel, 'db', `${formatString},0`);
	return FormatMessage(
		FORMAT_MESSAGE_ARGUMENT_ARRAY |
		FORMAT_MESSAGE_FROM_STRING,
		formatStringLabel,
		0,
		0,
		arrayPtr);
};
const printf = (formatAsm, outputCb) => {
	return formatAsm +'\n'+
		outputCb('FormatMessage__tmpReturnBuffer', '[FormatMessage__tmpReturnBufferLength]');
};

const __import = (library, ...procs) => {
	_var(`LoadLibraryA__${library}`, 'db', `"${library}.dll",0`) 
	_var(`LoadLibraryA__${library}_hModule`, 'dq');
	let out = '';
	out += __ms_64_fastcall_w_error_check({ proc: 'LoadLibraryA',
		args: [
			{ value: `LoadLibraryA__${library}`, size: 'qword', comment: 'LPCSTR lpLibFileName' },
		],
		ret: { value: `[LoadLibraryA__${library}_hModule]`, size: 'qword', comment: 'HMODULE' },
	});
	for (const proc of procs) {
		_var(proc, 'dq');
		_var(`GetProcAddress__${proc}`, 'db', `"${proc}",0`);
		out += __ms_64_fastcall_w_error_check({ proc: 'GetProcAddress',
			args: [
				{ value: `[LoadLibraryA__${library}_hModule]`, size: 'qword', comment: 'HMODULE hModule' },
				{ value: `GetProcAddress__${proc}`, size: 'dword', comment: 'LPCSTR lpProcName' },
			],
			ret: { value: `[${proc}]`, size: 'qword', comment: 'FARPROC' },
		});
	}
	return out;
};


init();
build();
out();
console.log('done.');