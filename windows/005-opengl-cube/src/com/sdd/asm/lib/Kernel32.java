package com.sdd.asm.lib;

import com.sdd.asm.Macros;

import java.util.ArrayList;
import static com.sdd.asm.Macros.*;
import static com.sdd.asm.Macros.Size.*;

/**
 * Microsoft Windows kernel32.dll APIs
 *
 * General operating system and process information.
 */
public class Kernel32
{
	public static Proc CreateMutexA(
		final int lpMutexAttributes,
		final boolean bInitialOwner,
		final LabelReference lpName
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL, "CreateMutexA", true))),
			new ArrayList<SizedOp>() {{
				add(width(DWORD, oper(lpMutexAttributes).comment("LPSECURITY_ATTRIBUTES lpMutexAttributes")));
				add(width(DWORD, oper(bInitialOwner).comment("BOOL bInitialOwner")));
				add(width(DWORD, oper(lpName).comment("LPCSTR lpName")));
			}},
			returnVal(QWORD, "HANDLE"));
	}
	
	public static Proc GetModuleHandleA(
		final Operand lpModuleName
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"GetModuleHandleA", true))),
			new ArrayList<SizedOp>() {{
				add(width(DWORD, lpModuleName.comment("LPCSTR lpModuleName")));
			}},
			returnVal(QWORD, "HMODULE *phModule"));
	}

	public static Proc ExitProcess(
		final LabelReference code
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"ExitProcess", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(code).comment("UINT uExitCode")));
			}});
	}
	
	public static Proc SetLastError(
		final int dwErrCode
	) {
		return new Proc(Macros::__ms_fastcall_64,
			addrOf(extern(label(Scope.GLOBAL,"SetLastError", true))),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(dwErrCode).comment("DWORD dwErrCode")));
			}});
	}
	
	public static Proc GetLastError() {
		return new Proc(Macros::__ms_fastcall_64,
			addrOf(extern(label(Scope.GLOBAL,"GetLastError", true))),
			returnVal(DWORD, "DWORD dwErrCode"));
	}
	
	public static final int FORMAT_MESSAGE_FROM_SYSTEM = 0x00001000;
	public static final int FORMAT_MESSAGE_IGNORE_INSERTS = 0x00000200;
	public static final int FORMAT_MESSAGE_ARGUMENT_ARRAY = 0x00002000;
	public static final int FORMAT_MESSAGE_FROM_STRING = 0x00000400;
	public static final int LANG_USER_DEFAULT__SUBLANG_DEFAULT = 0x0400;

	/**
	 * Similar to libc printf()
	 * 
	 * see: https://docs.microsoft.com/en-us/windows/desktop/api/winbase/nf-winbase-formatmessagea#remarks
	 */
	public static Proc FormatMessageA(
		final int dwFlags,
		final Operand lpSource,
		final Operand dwMessageId,
		final int dwLanguageId,
		final LabelReference lpBuffer,
		final int nSize,
		final Operand Arguments
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"FormatMessageA", true))),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(dwFlags).comment("DWORD dwFlags")));
				add(width(DWORD, lpSource.comment("LPCVOID lpSource")));
				add(width(DWORD, dwMessageId.comment("DWORD dwMessageId")));
				add(width(DWORD, oper(dwLanguageId).comment("DWORD dwLanguageId")));
				add(width(QWORD, oper(lpBuffer).comment("LPSTR lpBuffer")));
				add(width(QWORD, oper(nSize).comment("DWORD nSize")));
				add(width(QWORD, Arguments.comment("va_list *Arguments")));
			}},
			returnVal(DWORD, "DWORD TCHARs written"));
	}

	public static final int STD_OUTPUT_HANDLE = -11;
	public static final int STD_ERROR_HANDLE  = -12;
	
	/**
	 * Get hFile pointer to one of STDIN, STDOUT, STDERR,
	 * 
	 * see: https://docs.microsoft.com/en-us/windows/console/getstdhandle
	 */
	public static Proc GetStdHandle(
		final int nStdHandle
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"GetStdHandle", true))),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(nStdHandle).comment("DWORD nStdHandle")));
			}},
			returnVal(DWORD, "HANDLE"));
	}
	
	public static Proc WriteFile(
		final Label hFile,
		final Label lpBuffer,
		final Label nNumberOfBytesToWrite,
		final Label lpNumberOfBytesWritten,
		final Operand lpOverlapped
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"WriteFile", true))),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(hFile)).comment("HANDLE hFile" )));
				add(width(DWORD, oper(addrOf(lpBuffer)).comment("LPCVOID lpBuffer")));
				add(width(DWORD, oper(deref(nNumberOfBytesToWrite)).comment("DWORD nNumberOfBytesToWrite")));
				add(width(DWORD, oper(addrOf(lpNumberOfBytesWritten)).comment("LPDWORD lpNumberOfBytesWritten")));
				add(width(DWORD, lpOverlapped.comment("LPOVERLAPPED lpOverlapped")));
			}},
			returnVal(DWORD, "BOOL"));
	}

	public static Proc LocalSize(
		final Label handle
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"LocalSize", true))),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(handle)).comment("HLOCAL hMem" )));
			}},
			returnVal(DWORD, "SIZE_T"));
	}

	public static Proc LoadLibraryA(
		final Label lpLibFileName
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"LoadLibraryA", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(addrOf(lpLibFileName)).comment("LPCSTR lpLibFileName")));
			}},
			returnVal(QWORD, "HMODULE"));
	}

	public static Proc GetProcAddress(
		final Label hModule,
		final Label lpProcName
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"GetProcAddress", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(deref(hModule)).comment("HMODULE hModule")));
				add(width(DWORD, oper(addrOf(lpProcName)).comment("LPCSTR lpProcName")));
			}},
			returnVal(QWORD, "FARPROC"));
	}
}