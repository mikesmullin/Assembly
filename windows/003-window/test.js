// goal: try to display an empty window?

const fs = require('fs');
const path = require('path');
const _ = require('lodash');
const [,,OUT_FILE] = process.argv;
let section = 'preprocessor';
const sections = { preprocessor: '', '.data': '', '.text': '' };
const _var = (name, size='b', val=0) =>
	sections['.data'] += `${name}: d${size} ${val}\n`;
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
	// TODO: change background value to 0 when OpenGL Context is ready
	hbrBackground: { type: TYPES.HBRUSH, default: 5 },
	lpszMenuName: { type: TYPES.LPCSTR, default: 0 },
	lpszClassName: { type: TYPES.LPCSTR },
	hIconSm: { type: TYPES.HICON, default: 0 },
}};

STRUCTS.tagMSG = { name: 'tagMSG', data: {
	hwnd: { type: { ...TYPES.HWND, size: 8 } },
  message: { type: { ...TYPES.UINT, size: 8 }  },
  wParam: { type: { ...TYPES.WPARAM, size: 8 }  },
  lParam: { type: { ...TYPES.LPARAM, size: 8 }  },
  time: { type: { ...TYPES.DWORD, size: 8 }  },
	'pt.x': { type: { ...TYPES.DWORD, size: 8 }  },
	'pt.y': { type: { ...TYPES.DWORD, size: 8 }  },
  lPrivate: { type: { ...TYPES.DWORD, size: 8 }  },
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
























const build = () => {
	section = 'preprocessor';
	asm('; build window');
	asm('extern GetModuleHandleA');
	asm('extern CreateMutexA');
	asm('extern LoadImageA');
	asm('extern RegisterClassExA');
	asm('extern CreateWindowExA');
	asm('extern ShowWindow');
	asm('extern UpdateWindow');
	asm('extern SetForegroundWindow');
	asm('extern SetFocus');

	asm('\n; main loop');
	asm('extern PeekMessageA');
	asm('extern TranslateMessage');
	asm('extern DispatchMessageA');
	asm('extern DefWindowProcA');
	asm('extern PostQuitMessage');
	
	asm(`\n; shutdown/cleanup`);
	asm('extern LocalFree');
	asm('extern ExitProcess');

	asm(`\n; error handling`);
	asm('extern SetLastError');
	asm('extern GetLastError');
	asm('extern FormatMessageA');
	asm('extern GetStdHandle');
	asm('extern LocalSize');
	asm('extern WriteFile');

	section = '.text';
	asm('global main');
	asm('main:\n');
	
	asm(BLOCKS.INIT);

	// generic reusable uuid any time an api function wants a string identifier
	_var('Generic__uuid', 'b', '"e44d7545-f9df-418e-bc37-11ad4535d32f",0');

	// verify the window is not open twice
	_var('CreateMutexA__handle', 'q');
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
	_var('GetModuleHandleA__hModule', 'q');
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
	_var('CreateWindow__icon', 'q');
	_var('CreateWindow__cursor', 'q');
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
	_var('CreateWindow__atom_name', 'q');
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
	_var('CreateWindow__hwnd', 'q');
	_var('CreateWindow__title', 'b', '"OpenGL Demo",0');
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
		ret: { value: '[CreateWindow__hwnd]', size: 'qword', comment: 'HWND' },
	}));

	_var('ShowWindow__result', 'd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'ShowWindow',
		args: [
			{ value: '[CreateWindow__hwnd]', size: 'qword', comment: 'HWND hWnd' },
			{ value: '1', size: 'dword', comment: 'int  nCmdShow' },
		],
		ret: { value: '[ShowWindow__result]', size: 'dword', comment: 'BOOL' },
	}));

	_var('UpdateWindow__result', 'd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'UpdateWindow',
		args: [
			{ value: '[CreateWindow__hwnd]', size: 'qword', comment: 'HWND hWnd' },
		],
		ret: { value: '[UpdateWindow__result]', size: 'dword', comment: 'BOOL' },
	}));

	_var('SetForegroundWindow__result', 'd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'SetForegroundWindow',
		args: [
			{ value: '[CreateWindow__hwnd]', size: 'qword', comment: 'HWND hWnd' },
		],
		ret: { value: '[SetForegroundWindow__result]', size: 'dword', comment: 'BOOL' },
	}));

	_var('SetFocus__result', 'd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'SetFocus',
		args: [
			{ value: '[CreateWindow__hwnd]', size: 'qword', comment: 'HWND hWnd' },
		],
		ret: { value: '[SetFocus__result]', size: 'dword', comment: 'BOOL' },
	}));

	const IncomingMessage = istruct('IncomingMessage', STRUCTS.tagMSG, {
		hwnd: 0,
		message: 0,
		wParam: 0,
		lParam: 0,
		time: 0,
		'pt.x': 0,
		'pt.y': 0,
		lPrivate: 0,
	});	

	
	// TODO: remove these after debugging
	_var('padding: times 4 db 0 ; not sure why padding is needed here but must find out proper alignment');
	_var('dot', 'b', '"."');
	_var('dash', 'b', '"-"');
	
	asm('Loop:');
	const PM_REMOVE = 0x0001;
	_var('PeekMessage_hasMsgs', 'd');
	asm(__ms_64_fastcall_w_error_check({ proc: 'PeekMessageA',
		args: [
			{ value: IncomingMessage, size: 'qword', comment: 'LPMSG lpMsg' },
			{ value: '[CreateWindow__hwnd]', size: 'qword', comment: 'HWND hWnd' },
			{ value: 0, size: 'dword', comment: 'UINT wMsgFilterMin' },
			{ value: 0, size: 'dword', comment: 'UINT wMsgFilterMax' },
			{ value: PM_REMOVE, size: 'dword', comment: 'UINT wRemoveMsg = PM_REMOVE' },
		],
		ret: { value: '[PeekMessage_hasMsgs]', size: 'dword', comment: 'BOOL' },
	}));
	const WM_QUIT = 0x0012;
	asm('cmp dword [PeekMessage_hasMsgs], 0 ; zero messages');
	asm('je near Loop');

	// debug trace
	asm(printf(FormatString('PeekMessage_msgIdFormatString',
		`\nMessage received:\n`+
		`  hwnd: %I64X\n`+
		`  message: %I64u\n`+
		`  wParam: %I64X\n`+
		`  lParam: %I64X\n`+
		`  time: %I64X\n`+
		`  pt.x: %I64u\n`+
		`  pt.y: %I64u\n`+
		`  lPrivate: %I64u\n`,
		8,
		IncomingMessage), Asm.Console.log));

	asm(`cmp dword [${IncomingMessage}.message], ${hex(WM_QUIT)} ; WM_QUIT`);
	asm('jne near ..@Loop__processMessage');
	asm(Asm.Console.log('dash', 1));
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
	asm('je near Loop');

	const WM_ACTIVATE = 0x0006;
	const WM_SYSCOMMAND = 0x0112;
	const WM_CLOSE = 0x0010;
	// const WM_DESTROY = 0x0002;
	const WM_KEYDOWN = 0x0100;
	const WM_KEYUP = 0x0101;
	const WM_SIZE = 0x0005;
	// see: https://github.com/tpn/winsdk-10/blob/master/Include/10.0.10240.0/um/WinUser.h#L1951
	const SC_SCREENSAVE = 0x0F140;
	const SC_MONITORPOWER = 0x0F170;
	asm('\nWndProc:');
	_var('nWndProc__hWnd', 'q');
	_var('nWndProc__uMsg', 'q');
	_var('nWndProc__wParam', 'q');
	_var('nWndProc__lParam', 'q');
	_var('nWndProc__return', 'q');
	asm('mov qword [nWndProc__hWnd], rcx');
	asm('mov qword [nWndProc__uMsg], rdx');
	asm('mov qword [nWndProc__wParam], r8');
	asm('mov qword [nWndProc__lParam], r9');
	//TODO: print every WindProc msg that gets dispatched during average program cycle
	//asm(Console.log(nWndProc__uMsg

	// switch uMsg
	asm(`cmp rdx, ${hex(WM_ACTIVATE)}`);
	asm('je near WndProc__WM_Activate');
	asm(`cmp rdx, ${hex(WM_SYSCOMMAND)}`);
	asm('je near WndProc__WM_SysCommand');
	asm(`cmp rdx, ${hex(WM_CLOSE)}`);
	asm('je near WndProc__WM_Close');
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
	asm(Asm.Console.log('dot', 1));
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

	// asm(exit(0));

	asm(BLOCKS.PROCS);
};
































const hex = n => '0x'+ n.toString(16);

const ___english_ordinal = i => {
	const s = i.toString();
	const n = parseInt(s.substr(-1), 10);
	return s + (1===n && i !== 11 ? 'st' : 2===n && i !== 12 ? 'nd' : 3===n && i !== 13 ? 'rd' : 'th');
};

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

	const registers = [
		{ dword: 'ecx', qword: 'rcx' },
		{ dword: 'edx', qword: 'rdx' },
		{ dword: 'r8d', qword: 'r8'  },
		{ dword: 'r9d', qword: 'r9'  },
	];

	for (let i=args.length-1; i>-1; i--) {
		const arg = args[i];
		const pos = `${___english_ordinal(i+1)}: `;
		if (i>3) out += `mov ${arg.size} [rsp + ${i * 8}], ${arg.value} ; ${pos}${arg.comment||''}\n`;
		else out += `mov ${arg.size} ${registers[i][arg.size]}, ${arg.value} ; ${pos}${arg.comment||''}\n`;
	}
	
	out += `call ${proc}\n`;

	// handle return var, if provided
	if (null != ret) {
		out += `mov ${ret.size} ${ret.value}, ${{dword: 'eax', qword: 'rax'}[ret.size]} ; return ${ret.comment||''}\n`;
	}

	// deallocate ms fastcall shadow space
	out += `add rsp, ${shadow} ; deallocate shadow space\n`;
	return out;
};

_var('GetLastError__errCode', 'd');
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
	out += `call near GetLastError__prologue_reset\n`;
	out += __ms_64_fastcall(o);
	out += `call near GetLastError__epilogue_check\n`;
	return out;
};

onready(()=>{
	BLOCKS.PROCS += `\n`+
		'Exit:\n' +
		__ms_64_fastcall({ proc: 'ExitProcess' });
});
const exit = (code=0) =>
	`mov ecx, ${code} ; UINT uExitCode\n` +
	'jmp near Exit\n';



const Asm = {};

Asm.Console = {};
Asm.Console.STD_OUTPUT_HANDLE = -11;
Asm.Console.STD_ERROR_HANDLE  = -12;
_var('Console__stderr_nStdHandle', 'd');
_var('Console__stdout_nStdHandle', 'd');
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
	_var('Console__bytesWritten', 'd');
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

const FORMAT_MESSAGE_ALLOCATE_BUFFER = 0x00000100;
const FORMAT_MESSAGE_FROM_SYSTEM = 0x00001000;
const FORMAT_MESSAGE_IGNORE_INSERTS = 0x00000200;
const FORMAT_MESSAGE_ARGUMENT_ARRAY = 0x00002000;
const FORMAT_MESSAGE_FROM_STRING = 0x00000400;
const LANG_USER_DEFAULT__SUBLANG_DEFAULT = 0x0400;
_var('FormatMessage__tmpReturnBuffer', 'q');
_var('FormatMessage__tmpReturnBufferLength', 'd');
const FormatMessage = (dwFlags, label, dwMessageId, size, args) => {
	return __ms_64_fastcall({ proc: 'FormatMessageA', args: [
		{ value: hex(dwFlags), size: 'dword', comment: 'DWORD dwFlags' },
		{ value: label, size: 'dword', comment: 'LPCVOID lpSource' },
		{ value: dwMessageId, size: 'dword', comment: 'DWORD dwMessageId' },
		{ value: hex(LANG_USER_DEFAULT__SUBLANG_DEFAULT), size: 'dword',
			comment: 'DWORD dwLanguageId = LANG_USER_DEFAULT, SUBLANG_DEFAULT' },
		{ value: 'FormatMessage__tmpReturnBuffer', size: 'dword', comment: 'LPSTR lpBuffer' },
		{ value: size, size: 'dword', comment: 'DWORD nSize' },
		{ value: args, size: 'dword', comment: 'va_list *Arguments'},
	]})
}
const GetErrorMessage = (dwMessageId) => {
	return FormatMessage(
		FORMAT_MESSAGE_ALLOCATE_BUFFER |
		FORMAT_MESSAGE_FROM_SYSTEM |
		FORMAT_MESSAGE_IGNORE_INSERTS,
		0,
		dwMessageId,
		0,
		0);
};
const FormatString = (formatStringLabel, formatString, arraySize, arrayPtr) => {
	_var(formatStringLabel, 'b', `"${JSON.stringify(formatString)}",0`);
	return FormatMessage(
		FORMAT_MESSAGE_ALLOCATE_BUFFER |
		FORMAT_MESSAGE_ARGUMENT_ARRAY |
		FORMAT_MESSAGE_FROM_STRING,
		formatStringLabel,
		0,
		arraySize,
		arrayPtr);
};
const printf = (formatAsm, outputCb) => {
	return formatAsm +'\n'+

		__ms_64_fastcall({ proc: 'LocalSize',
			ret: { value: '[FormatMessage__tmpReturnBufferLength]', size: 'dword' },
			args: [
				{ value: '[FormatMessage__tmpReturnBuffer]', size: 'dword', comment: 'HLOCAL hMem' },
			]
		}) +

		outputCb('[FormatMessage__tmpReturnBuffer]', '[FormatMessage__tmpReturnBufferLength]') +

		'\n; cleanup\n' +
		__ms_64_fastcall({ proc: 'LocalFree', args: [
			{ value: '[FormatMessage__tmpReturnBuffer]', size: 'dword', comment: '_Frees_ptr_opt_ HLOCAL hMem'},
		]});
};

init();
build();
out();
console.log('done.');