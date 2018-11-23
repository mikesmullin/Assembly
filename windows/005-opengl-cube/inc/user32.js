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

const CS_VREDRAW = 0x0001;
const CS_HREDRAW = 0x0002;
const CS_OWNDC   = 0x0020;
// see: https://docs.microsoft.com/en-us/windows/desktop/winmsg/window-class-styles

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

const OIC_WINLOGO = 32517;
const IDC_ARROW = 32512;
// see: https://github.com/tpn/winsdk-10/blob/master/Include/10.0.10240.0/um/WinUser.h#L10635
const IMAGE_ICON = 1;
const IMAGE_CURSOR = 2;
const LR_DEFAULTSIZE = 0x00000040;
const LR_SHARED = 0x00008000;
// see: https://docs.microsoft.com/en-us/windows/desktop/api/winuser/nf-winuser-loadicona

const LoadImageA = (
	hInst/*:string*/,
	name/*:string*/,
	type/*:number*/,
	cx/*:number*/,
	cy/*:number*/,
	fuLoad/*:number*/
) => ({
	proc: 'LoadImageA',
	args: [
		{ value: hInst, size: 'dword', comment: 'HINSTANCE hInst' },
		{ value: name, size: 'dword', comment: 'LPCSTR name' },
		{ value: type, size: 'dword', comment: 'UINT type' },
		{ value: cx, size: 'dword', comment: 'int cx' },
		{ value: cy, size: 'dword', comment: 'int cy' },
		{ value: hex(fuLoad), size: 'dword', comment: 'UINT fuLoad' },
	],
	ret: { size: 'qword', comment: 'HANDLE' },
});

const PM_REMOVE       = 0x0001;
const WM_QUIT         = 0x0012;
const WM_ACTIVATE     = 0x0006;
const WM_SYSCOMMAND   = 0x0112;
const WM_CLOSE        = 0x0010;
const WM_DESTROY      = 0x0002;
const WM_KEYDOWN      = 0x0100;
const WM_KEYUP        = 0x0101;
const WM_SIZE         = 0x0005;
// see: https://github.com/tpn/winsdk-10/blob/master/Include/10.0.10240.0/um/WinUser.h#L1951
const SC_SCREENSAVE   = 0x0F140;
const SC_MONITORPOWER = 0x0F170;
const PeekMessageA = (
	lpMsg/*:string*/,
	hWnd/*:string*/,
	wMsgFilterMin/*:number*/,
	wMsgFilterMax/*:number*/,
	wRemoveMsg/*:number*/
) => ({
	proc: 'PeekMessageA',
	args: [
		{ value: lpMsg, size: 'qword', comment: 'LPMSG lpMsg' },
		{ value: hWnd, size: 'qword', comment: 'HWND hWnd' },
		{ value: wMsgFilterMin, size: 'dword', comment: 'UINT wMsgFilterMin' },
		{ value: wMsgFilterMax, size: 'dword', comment: 'UINT wMsgFilterMax' },
		{ value: wRemoveMsg, size: 'dword', comment: 'UINT wRemoveMsg' },
	],
	ret: { size: 'dword', comment: 'BOOL' },
});

const RegisterClassExA = (
	Arg1/*:string*/
) => ({
	proc: 'RegisterClassExA',
	args: [
		{ value: Arg1, size: 'qword', comment: 'WNDCLASSEXA *Arg1' },
	],
	ret: { size: 'qword', comment: 'HANDLE' },
});

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

const CreateWindowExA = (
	dwExStyle/*:number*/,
	lpClassName/*:string*/,
	lpWindowName/*:string*/,
	dwStyle/*:number*/,
	x/*:number*/,
	y/*:number*/,
	nWidth/*:number*/,
	nHeight/*:number*/,
	hWndParent/*:number*/,
	hMenu/*:number*/,
	hInstance/*:number*/,
	lpParam/*:number*/,
) => ({
	proc: 'CreateWindowExA',
	args: [
		{ value: dwExStyle, size: 'qword', comment: 'DWORD dwExStyle' },
		// NOTICE: the name used there has to be the same as the one used for RegisterClass
		{ value: lpClassName, size: 'qword', comment: 'LPCSTR lpClassName' },
		{ value: lpWindowName, size: 'qword', comment: 'LPCSTR lpWindowName' },
		{ value: hex(dwStyle), size: 'qword', comment: 'DWORD dwStyle' },
		{ value: hex(x), size: 'dword', comment: 'int X' },
		{ value: hex(y), size: 'dword', comment: 'int Y' },
		{ value: nWidth, size: 'dword', comment: 'int nWidth' },
		{ value: nHeight, size: 'dword', comment: 'int nHeight' },
		{ value: hWndParent, size: 'qword', comment: 'HWND hWndParent' },
		{ value: hMenu, size: 'qword', comment: 'HMENU hMenu' },
		{ value: hInstance, size: 'qword', comment: 'HINSTANCE hInstance' },
		{ value: lpParam, size: 'qword', comment: 'LPVOID lpParam' },
	],
	ret: { size: 'qword', comment: 'HANDLE' },
});

const TranslateMessage = (
	lpMsg/*:string*/,
) => ({
	proc: 'TranslateMessage',
	args: [
		{ value: lpMsg, size: 'qword', comment: 'LPMSG lpMsg' },
	],
});

const DispatchMessageA = (
	lpMsg/*:string*/,
) => ({
	proc: 'DispatchMessageA',
	args: [
		{ value: lpMsg, size: 'qword', comment: 'LPMSG lpMsg' },
	],
});

const DefWindowProcA = (
	hWnd/*:string*/,
	Msg/*:number*/,
	wParam/*:string*/,
	lParam/*:string*/
) => ({
	proc: 'DefWindowProcA',
	args: [
		{ value: hWnd, size: 'qword', comment: 'HWND hWnd' },
		{ value: Msg, size: 'qword', comment: 'UINT Msg' },
		{ value: wParam, size: 'qword', comment: 'WPARAM wParam' },
		{ value: lParam, size: 'qword', comment: 'LPARAM lParam' },
	],
	ret: { size: 'qword' },
});

const DestroyWindow = (
	hWnd/*:string*/
) => ({
	proc: 'DestroyWindow',
	args: [
		{ value: hWnd, size: 'qword', comment: 'HWND hWnd' },
	],
});

const PostQuitMessage = (
	nExitCode/*:number*/
) => ({
	proc: 'PostQuitMessage',
	args: [
		{ value: nExitCode, size: 'dword', comment: 'int nExitCode' },
	],
});

module.exports = {
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
};