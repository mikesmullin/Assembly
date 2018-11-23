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

const GetDC = (
	hWnd/*:string*/
) => ({
	proc: 'GetDC',
	args: [
		{ value: hWnd, size: 'qword', comment: 'HWND hWnd' }
	],
	ret: { size: 'qword', comment: 'HDC' },
});

const ChoosePixelFormat = (
	hdc/*:string*/,
	ppfd/*:number*/
) => ({
	proc: 'ChoosePixelFormat',
	args: [
		{ value: hdc, size: 'qword', comment: 'HDC hdc' },
		{ value: ppfd, size: 'qword', comment: 'PIXELFORMATDESCRIPTOR *ppfd' },
	],
	ret: { size: 'dword', comment: 'int' },
});

const SetPixelFormat = (
	hdc/*:string*/,
	format/*:number*/,
	ppfd/*:number*/
) => ({
	proc: 'SetPixelFormat',
	args: [
		{ value: hdc, size: 'qword', comment: 'HDC hdc' },
		{ value: format, size: 'dword', comment: 'int format' },
		{ value: ppfd, size: 'qword', comment: 'PIXELFORMATDESCRIPTOR *ppfd' },
	],
	ret: { size: 'dword', comment: 'BOOL' },
});

const SwapBuffers = (
	Arg1/*:string*/,
) => ({
	proc: 'SwapBuffers',
	args: [
		{ value: Arg1, size: 'qword', comment: 'HDC Arg1' },
	],
	ret: { size: 'dword', comment: 'BOOL' },
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