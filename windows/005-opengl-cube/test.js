// @flow

const build = () => {
	section = 'preprocessor';
	asm(
		comment(
			'GOAL: Render OpenGL spinning 3d cube animation',
			'',
			`TODO: could make it so i don't check for GetLastError unless a proc returns 0`,
			'TODO: output to log file in relative path instead of showing console window'),
	);

	init();

	section = '.data';
	asm(comment('generic reusable uuid any time an api function wants a string identifier'));
	assign('global', 'Generic__uuid', 'db', nullstr('07b62314-d4fc-4704-96e8-c31eb378d815'));

	section = '.text';
	asm(
		def_label('global', 'main'),

		BLOCKS.INIT,
		
		comment('verify the window is not open twice'),
		assign_call('global', 'CreateMutexA__handle',
			CreateMutexA(null, true, addrOf('Generic__uuid'))),

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
	);

	// TODO: maybe use the stack for this
	// TODO: also, define these within the RegisterClassExA invocation,
	//       like a struct wth type completion ideally
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

	const WIDTH = 640;
	const HEIGHT = 480;
	assign('global', 'CreateWindow__title', 'db', nullstr('OpenGL Demo'));
	asm(
		comment('begin creating the main local application window'),
		assign_call('global', 'CreateWindow__atom_name',
			RegisterClassExA(MainWindow)),

		assign_call('global', 'CreateWindow__hWnd',
			CreateWindowExA(
				WS_EX_OVERLAPPEDWINDOW,
				addrOf('Generic__uuid'),
				addrOf('CreateWindow__title'),
				WS_OVERLAPPEDWINDOW | WS_VISIBLE | WS_CLIPCHILDREN | WS_CLIPSIBLINGS,
				CW_USEDEFAULT,
				CW_USEDEFAULT,
				WIDTH,
				HEIGHT,
				null,
				null,
				addrOf('GetModuleHandleA__hModule'),
				null
			)),

			comment('begin creating the OpenGL context'),
			assign_call('global', 'GetDC__hDC',
				GetDC(deref('CreateWindow__hWnd'))),
	);

	// TODO: if full screen:
		// ChangeDisplaySettings
		// ShowCursor

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

	asm(
		assign_call('global', 'ChoosePixelFormat__format',
			ChoosePixelFormat(deref('GetDC__hDC'), PixelFormat)),

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
	);

	const IncomingMessage = istruct('IncomingMessage', STRUCTS.tagMSG, {});

	asm(
		def_label('normal', 'Loop'),

		assign_call('global', 'PeekMessage_hasMsgs',
			PeekMessageA(
				addrOf(IncomingMessage),
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







	// GetSystemMetrics ; ie. screen dimensions

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



















// supporting preprocessor-esque macro functions

const fs = require('fs');
const path = require('path');
const _ = require('lodash');
const [,,OUT_FILE] = process.argv;
let section = 'preprocessor';
const sections = { preprocessor: '', '.data': '', '.text': '' };
const _var = (name, size='db', val=0) =>
	sections['.data'] += `${name}: ${size} ${val}\n`;
const asm = (...s) => sections[section] += s.join("\n")+"\n";
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

/**
 * Windows 64-bit uses Microsoft fastcall
 * First four params are passed via registers (RCX, RDX, R8, R9), and the remainder on stack (before shadow space)
 * Caller also responsible to pad 'shadow space' of 4 x 64-bit registers (32 bytes) prior to the call,
 * and to unwind the shadow space after the call has returned.
 */
const __ms_64_fastcall = ({ proc, ret, args=[] }) => {
	if ('['!==proc[0]) extern(proc);
	let out = '    ; MS __fastcall x64 ABI\n';
	// allocate ms fastcall shadow space
	const shadow = Math.max(40, (
		args.length + // number of args
		1 // mystery padding; its what VC++ x64 does
		) * 8 // bytes
	);
	out += `    sub rsp, ${shadow} ; allocate shadow space\n`;

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
					`    mov qword rax, ${arg.float}\n` +
					`    movq ${registers[i][arg.size]}, rax ; ${pos}${arg.comment||''}\n`;
				}
			}
		else { // integer operands
			if (null == arg.value) arg.value = 0; // NULL
			else if (true === arg.value) arg.value = 1; // TRUE
			else if (false === arg.value) arg.value = 0; // FALSE
			registers = [
				{ dword: 'ecx', qword: 'rcx' },
				{ dword: 'edx', qword: 'rdx' },
				{ dword: 'r8d', qword: 'r8'  },
				{ dword: 'r9d', qword: 'r9'  },
			];
			const mov = `mov ${arg.size}`;
			if (i>3) out += `    ${mov} [rsp + ${i * 8}], ${arg.value} ; ${pos}${arg.comment||''}\n`;
			else out += `    ${mov} ${registers[i][arg.size]}, ${arg.value} ; ${pos}${arg.comment||''}\n`;
		}
	}
	
	out += `call ${proc}\n`;

	// handle return var, if provided
	if (null != ret) {
		out += `    mov ${ret.size} ${ret.value}, ${{dword: 'eax', qword: 'rax'}[ret.size]} ; return ${ret.comment||''}\n`;
	}

	// deallocate ms fastcall shadow space
	out += `    add rsp, ${shadow} ; deallocate shadow space\n`;
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
	out += `    call GetLastError__prologue_reset\n`;
	out += __ms_64_fastcall(o);
	out += `    call GetLastError__epilogue_check\n`;
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

const dllimport = (library, ...procs) => {
	_var(`LoadLibraryA__${library}`, 'db', `"${library}.dll",0`) 
	_var(`LoadLibraryA__${library}_hModule`, 'dq');
	let out = '';
	out += comment('dynamically load library at runtime') +"\n" +
		__ms_64_fastcall_w_error_check({ proc: 'LoadLibraryA',
			args: [
				{ value: `LoadLibraryA__${library}`, size: 'qword', comment: 'LPCSTR lpLibFileName' },
			],
			ret: { value: `[LoadLibraryA__${library}_hModule]`, size: 'qword', comment: 'HMODULE' },
		}) + "\n";
	for (const proc of procs) {
		_var(proc, 'dq');
		_var(`GetProcAddress__${proc}`, 'db', `"${proc}",0`);
		out += __ms_64_fastcall_w_error_check({ proc: 'GetProcAddress',
			args: [
				{ value: `[LoadLibraryA__${library}_hModule]`, size: 'qword', comment: 'HMODULE hModule' },
				{ value: `GetProcAddress__${proc}`, size: 'dword', comment: 'LPCSTR lpProcName' },
			],
			ret: { value: `[${proc}]`, size: 'qword', comment: 'FARPROC' },
		}) + "\n";
	}
	return out;
};



const comment = (...s) => s.map(c=>`; ${c}`).join('\n');
const _externs = new Set();
const extern = (...modules) => {
	const oldSection = section;
	section = 'preprocessor';
	for (const module of modules) {
		if (_externs.has(module)) continue;
		asm(`extern ${module}`);
		_externs.add(module);
	}
	section = oldSection;
};
const label = (scope, name) => {
	let out = '';
	if ('global' === scope) out += `global ${name}\n`;
	if ('local' === scope) out += '..@';
	out += `${name}`;
	return out;
}
const def_label = (scope, name) => `${label(scope, name)}:`;
const _default = (base, key, value) => {
	if (null != base && null == base[key]) base[key] = value;
};
const addrOf = label => label;
const deref  = label => `[${label}]`;
const escapeString = s => {
	let lastType = `code'; // or 'char'\n`;
	let a = [];
	const chars = s.split('');
	for (const char of chars) {
		const code = char.charCodeAt(0);
		if (code < 32 || 34 === code || code > 126) {
			a.push(code);
			lastType = 'code';
		}
		else {
			if ('char' === lastType) a[a.length-1] += char;
			else a.push(char);
			lastType = 'char';
		}
	}
	return a.map(sym=>'string'===typeof sym ? `"${sym}"` : sym).join(',');
};
const nullstr = s => escapeString(s) + ',0';
const makefloat = f => {
	const buf = Buffer.allocUnsafe(4);
	buf.writeFloatBE(f, 0);
	assign('global', 'F_'+f,  'dq', '0x'+buf.toString('hex'));
	return deref('F_'+f);
};

const call = procOpts => {
	_default(procOpts, 'convention', __ms_64_fastcall_w_error_check);
	return procOpts.convention(procOpts);
};
const SIZE_WORD_ABBREV = {
	'qword': 'dq',
	'dword': 'dd',
	'byte':  'db',
};
const _defined_globals = new Set();
const assign = (scope, label, size, value) => {
	if ('local' === scope) {
		// TODO: use stack for this case
		// todo: local vars have state like name, scope, type, size, and namespace
	}
	else if ('global' === scope) {
		if (_defined_globals.has(label)) return;
		_var(label, size, value);
		_defined_globals.add(label);
	}
};
const assign_call = (scope, label, procOpts) => {
	_.set(procOpts, 'ret.value', `[${label}]`);
	assign(scope, label, SIZE_WORD_ABBREV[_.get(procOpts, 'ret.size', 'dword')]);
	 return call(procOpts);
};
const assign_mov = (scope, label, size, rm) => {
	assign(scope, label, SIZE_WORD_ABBREV[size]);
	return `mov ${size} ${deref(label)}, ${rm}`;
};
const mov = (size, rm, label) =>
	`mov ${size} ${rm}, ${label}`;
const __if = (size, a, cmp, b, label) =>
	// e, z: equal
	// ne, nz: not equal
	// g: greater than (signed int)
	// ge: greater than, or equal (signed int)
	// l: less than (signed int)
	// le: less than, or equal (signed int)
	// a: greater than (unsigned int)
	// ae: greater than, or equal (unsigned int)
	// b: less than (unsigned int)
	// be: less than, or equal (unsigned int)
	// o: overflow
	// no: not overflow
	// s: signed
	// ns: not signed
	`cmp ${size} ${a}, ${b}\n`+
	`j${cmp} near ${label}`;
const jmp = label => `jmp near ${label}`;
const ret = (size, v) => {
	let out = '';
	if (null == v || 0 === v) {
		out += 'xor rax, rax ; return NULL\n';
	}
	else {
		out += mov(size, {dword: 'eax', qword: 'rax'}[size], v)+' ; return\n';
	}
	out += 'ret';
	return out;
};


// TODO: make it so args passed to functions appear in comments as string values
// like TRUE, FALSE, NULL, and BIT_FLAGS | OR_JOINED






// interfaces

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

const CreateMutexA = (
	lpMutexAttributes/*:number*/,
	bInitialOwner/*:number*/,
	lpName/*:string*/
) => ({
	proc: 'CreateMutexA',
	args: [
		{ value: lpMutexAttributes, size: 'dword', comment: 'LPSECURITY_ATTRIBUTES lpMutexAttributes' },
		{ value: bInitialOwner, size: 'dword', comment: 'BOOL bInitialOwner' },
		{ value: lpName, size: 'dword', comment: 'LPCSTR lpName' },
	],
	ret: { size: 'qword', comment: 'HANDLE' },
});

const GetModuleHandleA = (
	lpModuleName/*:number*/
) => ({
	proc: 'GetModuleHandleA',
	args: [
		{ value: lpModuleName, size: 'dword', comment: 'LPCSTR lpModuleName' }
	],
	ret: { size: 'qword', comment: 'HMODULE *phModule' },
});

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

const SwapBuffers = (
	Arg1/*:string*/,
) => ({
	proc: 'SwapBuffers',
	args: [
		{ value: Arg1, size: 'qword', comment: 'HDC Arg1' },
	],
	ret: { size: 'dword', comment: 'BOOL' },
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


build();
out();
console.log('done.');






// TODO: use the stack for all variables? like in macros build a stack frame reference
// so that important offsets are remembered and reusable with rsp offsets whether
// or not the code JMP or CALL several procs down the line
// but it may run into problems cuz it'd have to work inside a remote call to 
// same thread like WndProc
// maybe the one reserved .data is a base pointer
// i could probably figure out the rest from there statically at compile time
// but that would make all calls even register calls require two memory lookups
// which would be slower
// better think about this more

// TODO: get values showing up suffixed to comments again like = SOME_BIT_FLAG