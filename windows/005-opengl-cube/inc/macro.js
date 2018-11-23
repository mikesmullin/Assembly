// preprocessor macros
const fs = require('fs');
const path = require('path');
const _ = require('lodash');
const [,,OUT_FILE] = process.argv;
let _section = 'preprocessor';
const section = name => _section = name;
const sections = { preprocessor: '', '.data': '', '.text': '' };
const _var = (name, size='db', val=0) =>
	sections['.data'] += `${name}: ${size} ${val}\n`;
const asm = (...s) => sections[_section] += s.join("\n")+"\n";
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
	const oldSection = _section;
	_section = 'preprocessor';
	for (const module of modules) {
		if (_externs.has(module)) continue;
		asm(`extern ${module}`);
		_externs.add(module);
	}
	_section = oldSection;
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
	return label;
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

module.exports = {
	_section, asm, out, BLOCKS, onready, init, TYPES, STRUCTS, istruct,
	sizeof, hex, __ms_64_fastcall, __ms_64_fastcall_w_error_check,
	__ms_64_fastcall_w_glGetError, exit, Asm,
	printf, GetErrorMessage, FormatString,
	comment, extern, label, def_label, addrOf, deref,
	nullstr, makefloat, call, assign, assign_call, assign_mov, mov,
	jmp, ret,
};