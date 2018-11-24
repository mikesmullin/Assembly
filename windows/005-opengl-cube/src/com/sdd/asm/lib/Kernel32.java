package com.sdd.asm.lib;

import java.util.ArrayList;
import static com.sdd.asm.Macros.*;

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
		final String lpName
	) {
		return new Proc("CreateMutexA",
			new ArrayList<ValueSizeComment>() {{
				add(new ValueSizeComment(lpMutexAttributes, Size.DWORD, "LPSECURITY_ATTRIBUTES lpMutexAttributes"));
				add(new ValueSizeComment(bInitialOwner, Size.DWORD, "BOOL bInitialOwner"));
				add(new ValueSizeComment(lpName, Size.DWORD, "LPCSTR lpName"));
			}},
			new ValueSizeComment(Size.QWORD, "HANDLE")
		);
	}
	
	public static Proc GetModuleHandleA(final String lpModuleName)
	{
		return new Proc("GetModuleHandleA",
			new ArrayList<ValueSizeComment>() {{
				add(new ValueSizeComment(lpModuleName, Size.DWORD, "LPCSTR lpModuleName"));
			}},
			new ValueSizeComment(Size.QWORD, "HMODULE *phModule"));
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
		final String lpSource,
		final String dwMessageId,
		final int dwLanguageId,
		final String lpBuffer,
		final String nSize,
		final String Arguments
	) {
		return new Proc("FormatMessageA",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(hex(dwFlags), Size.DWORD, "DWORD dwFlags"));
				add(new ValueSizeComment(lpSource, Size.DWORD, "LPCVOID lpSource"));
				add(new ValueSizeComment(dwMessageId, Size.DWORD, "DWORD dwMessageId"));
				add(new ValueSizeComment(hex(dwLanguageId), Size.DWORD, "DWORD dwLanguageId"));
				add(new ValueSizeComment(lpBuffer, Size.QWORD, "LPSTR lpBuffer"));
				add(new ValueSizeComment(nSize, Size.QWORD, "DWORD nSize"));
				add(new ValueSizeComment(Arguments, Size.QWORD, "va_list *Arguments"));
			}},
			new ValueSizeComment(deref("FORMAT_MESSAGE_RETURN_BUFFER_LENGTH"), Size.DWORD, "DWORD TCHARs written"));
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
		return new Proc("GetStdHandle",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(nStdHandle, Size.DWORD, "DWORD nStdHandle" ));
			}},
			new ValueSizeComment(Size.DWORD, "HANDLE"));
	}

	public static Proc WriteFile(
		final String hFile,
		final String lpBuffer,
		final String nNumberOfBytesToWrite,
		final String lpNumberOfBytesWritten,
		final String lpOverlapped
	) {
		return new Proc("WriteFile",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(hFile, Size.DWORD, "HANDLE hFile" ));
				add(new ValueSizeComment(lpBuffer, Size.DWORD, "LPCVOID lpBuffer"));
				add(new ValueSizeComment(nNumberOfBytesToWrite, Size.DWORD, "DWORD nNumberOfBytesToWrite"));
				add(new ValueSizeComment(lpNumberOfBytesWritten, Size.DWORD, "LPDWORD lpNumberOfBytesWritten"));
				add(new ValueSizeComment(lpOverlapped, Size.DWORD, "LPOVERLAPPED lpOverlapped"));
			}},
			new ValueSizeComment(Size.DWORD, "BOOL"));
	}
}