// @flow
const {
	section, asm, out, BLOCKS, init,  STRUCTS, istruct,
	hex, exit, comment,  label, def_label, addrOf, deref,
	nullstr,  call, assign, assign_call, assign_mov, mov,
	jmp, ret
}  = require('./inc/macro');
// interfaces
const {
	TYPES,
	STRUCTS,
	CS_VREDRAW,
	CS_HREDRAW,
	CS_OWNDC,
	PFD_DRAW_TO_WINDOW,
	PFD_SUPPORT_OPENGL,
	PFD_DOUBLEBUFFER,
	PFD_TYPE_RGBA,
	PFD_MAIN_PLANE,
	CreateMutexA,
	GetModuleHandleA,
	OIC_WINLOGO,
	IDC_ARROW,
	IMAGE_ICON,
	IMAGE_CURSOR,
	LR_DEFAULTSIZE,
	LR_SHARED,
	LoadImageA,
	RegisterClassExA,
	WS_EX_WINDOWEDGE,
	WS_EX_CLIENTEDGE,
	WS_EX_OVERLAPPEDWINDOW,
	WS_CLIPCHILDREN,
	WS_CLIPSIBLINGS,
	WS_VISIBLE,
	WS_OVERLAPPED,
	WS_CAPTION,
	WS_SYSMENU,
	WS_THICKFRAME,
	WS_MINIMIZEBOX,
	WS_MAXIMIZEBOX,
	WS_OVERLAPPEDWINDOW,
	CW_USEDEFAULT,
	CreateWindowExA,
	GetDC,
	ChoosePixelFormat,
	SetPixelFormat,
	PM_REMOVE,
	WM_QUIT,
	WM_ACTIVATE,
	WM_SYSCOMMAND,
	WM_CLOSE,
	WM_DESTROY,
	WM_KEYDOWN,
	WM_KEYUP,
	WM_SIZE,
	SC_SCREENSAVE,
	SC_MONITORPOWER,
	PeekMessageA,
	TranslateMessage,
	DispatchMessageA,
	SwapBuffers,
	DefWindowProcA,
	DestroyWindow,
	PostQuitMessage,
} = require('./inc/kernel32');
const {
	wglCreateContext,
	wglMakeCurrent,
	glClearColor,
	glClear,
} = require('./inc/opengl');

const build = () => {
	let MainWindow, PixelFormat, IncomingMessage; // struct instances
	asm(comment('GOAL: Render OpenGL spinning 3d cube animation', ''));
	init();

	section('.text');
	asm(
		def_label('global', 'main'),
		BLOCKS.INIT,
		
		comment('verify the window is not open twice'),
		assign_call('global', 'CreateMutexA__handle',
			CreateMutexA(
				null,
				true,
				// generic reusable uuid any time an api function wants a string identifier
				addrOf(assign('global', 'Generic__uuid', 'db', nullstr('07b62314-d4fc-4704-96e8-c31eb378d815'))))),

		comment(
			'get a pointer to this process for use with api functions which require it ',
			'Note that as of 32-bit Windows, an instance handle (HINSTANCE), such as the',
			'application instance handle exposed by system function call of WinMain, and',
			'a module handle (HMODULE) are the same thing.'),
		assign_call('global', 'GetModuleHandleA__hModule',
			GetModuleHandleA(null)),

		comment('load references to the default icons for new windows'),
		assign_call('global', 'CreateWindow__icon',
			LoadImageA(null, OIC_WINLOGO, IMAGE_ICON, null, null, LR_SHARED | LR_DEFAULTSIZE)),

		assign_call('global', 'CreateWindow__cursor',
			LoadImageA(null, IDC_ARROW, IMAGE_CURSOR, null, null, LR_SHARED | LR_DEFAULTSIZE)),

		comment('begin creating the main local application window'),
		assign_call('global', 'CreateWindow__atom_name',
			// TODO: maybe use the stack for this
			// TODO: also, define these within the RegisterClassExA invocation,
			//       like a struct wth type completion ideally
			RegisterClassExA(MainWindow = istruct('MainWindow', STRUCTS.tagWNDCLASSEXA, {
				style: hex(CS_OWNDC | CS_VREDRAW | CS_HREDRAW),
				hInstance: 'GetModuleHandleA__hModule',
				// NOTICE: the name used there has to be the same as the one used for CreateWindow
				lpszClassName: `Generic__uuid`,
				lpfnWndProc: 'WndProc',
				hIcon: 'CreateWindow__icon',
				hCursor: 'CreateWindow__cursor',
			}))),

		assign_call('global', 'CreateWindow__hWnd',
			CreateWindowExA(
				WS_EX_OVERLAPPEDWINDOW,
				addrOf('Generic__uuid'),
				addrOf(assign('global', 'CreateWindow__title', 'db', nullstr('OpenGL Demo'))),
				WS_OVERLAPPEDWINDOW | WS_VISIBLE | WS_CLIPCHILDREN | WS_CLIPSIBLINGS,
				CW_USEDEFAULT,
				CW_USEDEFAULT,
				640, // width
				480, // height
				null,
				null,
				addrOf('GetModuleHandleA__hModule'),
				null
			)),

			comment('begin creating the OpenGL context'),
			assign_call('global', 'GetDC__hDC',
				GetDC(deref('CreateWindow__hWnd'))),
	);

	// TODO: if full screen: ChangeDisplaySettings, ShowCursor
	asm(
		assign_call('global', 'ChoosePixelFormat__format',
			ChoosePixelFormat(
				deref('GetDC__hDC'),
				PixelFormat = istruct('PixelFormat', STRUCTS.PIXELFORMATDESCRIPTOR, {
					dwFlags: hex(PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER),
					iPixelType: { value: PFD_TYPE_RGBA, comment: '= PFD_TYPE_RGBA' },
					// not sure i care to make this based on system capability
					cColorBits: { value: 24, comment: '(24-bit color depth)' },
					cAlphaBits: { value: 0, comment: '(no alpha buffer)' },
					cAccumBits: { value: 0, comment: '(no accumulation buffer)' },
					cDepthBits: { value: 32, comment: '(32-bit z-buffer)' },
					cStencilBits: { value: 0, comment: '(no stencil buffer)' },
					cAuxBuffers: { value: 0, comment: '(no auxiliary buffer)' },
					iLayerType: { value: PFD_MAIN_PLANE, comment: '= PFD_MAIN_PLANE' },
				}))),

		assign_call('global', 'SetPixelFormat__success',
			SetPixelFormat(
				deref('GetDC__hDC'),
				deref('ChoosePixelFormat__format'),
				PixelFormat
			)),
	
		dllimport('opengl32',
			'wglCreateContext',
			'wglMakeCurrent',
			'glClearColor',
			'glClear',
			'glGetError',
		),

		assign_call('global', 'wglCreateContext__ctx',
			wglCreateContext(deref('GetDC__hDC'))),

		assign_call('global', 'wglMakeCurrent__success',
			wglMakeCurrent(deref('GetDC__hDC'), deref('wglCreateContext__ctx'))),

		call(
			glClearColor(0, 0, 1, 1)),
		
		def_label('normal', 'Loop'),

		assign_call('global', 'PeekMessage_hasMsgs',
			PeekMessageA(
				addrOf(IncomingMessage = istruct('IncomingMessage', STRUCTS.tagMSG, {})),
				deref('CreateWindow__hWnd'),
				null,
				null,
				PM_REMOVE
			)),
		
		comment('if zero messages, skip handling messages'),
		__if('dword', deref('PeekMessage_hasMsgs'), 'e', 0,
			'..@Render'),

		comment('','exit if message is WM_QUIT'),
		__if('dword', deref(`${IncomingMessage}.message`), 'ne', hex(WM_QUIT),
			'..@Loop__processMessage'),
		
		exit(0),
		
		def_label('local', 'Loop__processMessage'),
		
		call(TranslateMessage(addrOf(IncomingMessage))),
		call(DispatchMessageA(addrOf(IncomingMessage))),
	
		def_label('local', 'Render'),

		call(glClear(GL_COLOR_BUFFER_BIT)),

		assign_call('global', 'SwapBuffers__success',
			SwapBuffers(deref('GetDC__hDC'))),

		jmp('Loop'),

		def_label('normal', 'WndProc'),
		comment('move local registers to local shadow space to preserve them'),
		assign_mov('global', 'WndProc__hWnd',   'qword', 'rcx'),
		assign_mov('global', 'WndProc__uMsg',   'qword', 'rdx'),
		assign_mov('global', 'WndProc__wParam', 'qword', 'r8'),
		assign_mov('global', 'WndProc__lParam', 'qword', 'r9'),

		comment('switch(uMsg) {'),
		__if('qword', 'rdx', 'e', hex(WM_ACTIVATE), label('local', 'WndProc__WM_Activate')),
		__if('qword', 'rdx', 'e', hex(WM_SYSCOMMAND), label('local', 'WndProc__WM_SysCommand')),
		__if('qword', 'rdx', 'e', hex(WM_CLOSE), label('local', 'WndProc__WM_Close')),
		__if('qword', 'rdx', 'e', hex(WM_DESTROY), label('local', 'WndProc__WM_Destroy')),
		__if('qword', 'rdx', 'e', hex(WM_KEYDOWN), label('local', 'WndProc__WM_KeyDown')),
		__if('qword', 'rdx', 'e', hex(WM_KEYUP), label('local', 'WndProc__WM_KeyUp')),
		__if('qword', 'rdx', 'e', hex(WM_SIZE), label('local', 'WndProc__WM_Size')),
		def_label('local', 'WndProc__default'),
		comment('default window procedure handles messages for us'),
		assign_call('global', 'WndProc__return',
			DefWindowProcA(
				deref('WndProc__hWnd'),
				deref('WndProc__uMsg'),
				deref('WndProc__wParam'),
				deref('WndProc__lParam')
			)),
		ret('qword', deref('WndProc__return')),

		def_label('local', 'WndProc__WM_Activate'),
		ret(null),

		def_label('local', 'WndProc__WM_SysCommand'),
		mov('dword', 'ebx', deref('WndProc__wParam')),
		__if('dword', 'ebx', 'e', hex(SC_SCREENSAVE), label('local', 'return_zero')),
		__if('dword', 'ebx', 'e', hex(SC_MONITORPOWER), label('local', 'return_zero')),
		jmp(label('local', 'WndProc__default')),
		def_label('local', 'return_zero'),
		ret(null),

		def_label('local', ('WndProc__WM_Close')),
		call(DestroyWindow(deref('CreateWindow__hWnd'))),
		ret(null),

		def_label('local', 'WndProc__WM_Destroy'),
		call(PostQuitMessage(0)),
		ret(null),

		def_label('local', 'WndProc__WM_KeyDown'),
		ret(null),

		def_label('local', 'WndProc__WM_KeyUp'),
		ret(null),

		def_label('local', 'WndProc__WM_Size'),
		ret(null),

		BLOCKS.PROCS,
	);
};

build();
out();
console.log('done.');