package com.sdd.asm.lib;

import java.util.ArrayList;
import static com.sdd.asm.Macros.*;

/**
 * Microsoft Windows user32.dll APIs
 *
 * Generally in charge of basic windowing and user input events.
 * 
 * see: https://docs.microsoft.com/en-us/windows/desktop/winmsg/window-class-styles
 * see: https://docs.microsoft.com/en-us/windows/desktop/winmsg/extended-window-styles
 * see: https://github.com/tpn/winsdk-10/blob/master/Include/10.0.10240.0/um/WinUser.h#L10635
 * see: https://docs.microsoft.com/en-us/windows/desktop/api/winuser/nf-winuser-loadicona
 */
public class User32
{
	public static final Type UINT = new Type("UINT", Size.DWORD);
	public static final Type WNDPROC = new Type("WNDPROC", Size.QWORD);
	public static final Type _int = new Type("int", Size.DWORD);
	public static final Type HINSTANCE = new Type("HINSTANCE", Size.QWORD);
	public static final Type HICON = new Type("HICON", Size.QWORD);
	public static final Type HCURSOR = new Type("HCURSOR", Size.QWORD);
	public static final Type HBRUSH = new Type("HBRUSH", Size.QWORD);
	public static final Type LPCSTR = new Type("LPCSTR", Size.QWORD);

	public static final Type HWND = new Type("HWND", Size.QWORD);
	// public static final Type POINT = new Type("POINT", Size.QWORD);
	public static final Type WPARAM = new Type("WPARAM", Size.QWORD);
	public static final Type LPARAM = new Type("LPARAM", Size.QWORD);

	public static final int CS_VREDRAW = 0x0001;
	public static final int CS_HREDRAW = 0x0002;
	public static final int CS_OWNDC   = 0x0020;

	public static Struct tagWNDCLASSEXA = new Struct("tagWNDCLASSEXA") {{
		fields.put("cbSize", new StructType(UINT));
		fields.put("style", new StructType(UINT));
		fields.put("lpfnWndProc", new StructType(WNDPROC));
		fields.put("cbClsExtra", new StructType(_int, operand(null)));
		fields.put("cbWndExtra", new StructType(_int, operand(null)));
		fields.put("hInstance", new StructType(HINSTANCE));
		fields.put("hIcon", new StructType(HICON, operand(null)));
		fields.put("hCursor", new StructType(HCURSOR));
		fields.put("hbrBackground", new StructType(HBRUSH, operand(null))); // 0 is required for OpenGL Context
		fields.put("lpszMenuName", new StructType(LPCSTR, operand(null)));
		fields.put("lpszClassName", new StructType(LPCSTR));
		fields.put("hIconSm", new StructType(HICON, operand(null)));
	}};
	static
	{
		tagWNDCLASSEXA.fields.get("cbSize").defaultValue = operand(sizeof(tagWNDCLASSEXA));
	}

	public static final int OIC_WINLOGO = 32517;
	public static final int IDC_ARROW = 32512;
	public static final int IMAGE_ICON = 1;
	public static final int IMAGE_CURSOR = 2;
	public static final int LR_DEFAULTSIZE = 0x00000040;
	public static final int LR_SHARED = 0x00008000;

	public static Proc LoadImageA(
		final String hInst,
		final int name, // can be int or String; can make overload when that is needed
		final int type,
		final int cx,
		final int cy,
		final int fuLoad
	) {
		return new Proc("LoadImageA",
			new ArrayList<ValueSizeComment>() {{
				add(new ValueSizeComment(hInst, Size.DWORD, "HINSTANCE hInst"));
				add(new ValueSizeComment(name, Size.DWORD, "LPCSTR name"));
				add(new ValueSizeComment(type, Size.DWORD, "UINT type"));
				add(new ValueSizeComment(cx, Size.DWORD, "int cx"));
				add(new ValueSizeComment(cy, Size.DWORD, "int cy"));
				add(new ValueSizeComment(hex(fuLoad), Size.DWORD, "UINT fuLoad"));
			}},
			new ValueSizeComment(Size.QWORD, "HANDLE"));
	}

	public static Struct tagMSG = new Struct("tagMSG") {{
		fields.put("hwnd", new StructType(HWND, operand(0)));
		fields.put("message", new StructType(UINT, operand(0)));
		fields.put("wParam", new StructType(WPARAM, operand(0)));
		fields.put("lParam", new StructType(LPARAM, operand(0)));
		fields.put("time", new StructType(DWORD, operand(0)));
		fields.put("pt.x", new StructType(DWORD, operand(0)));
		fields.put("pt.y", new StructType(DWORD, operand(0)));
		fields.put("lPrivate", new StructType(DWORD, operand(0)));
	}};

	public static final int PM_REMOVE       = 0x0001;
	public static final int WM_QUIT         = 0x0012;
	public static final int WM_ACTIVATE     = 0x0006;
	public static final int WM_SYSCOMMAND   = 0x0112;
	public static final int WM_CLOSE        = 0x0010;
	public static final int WM_DESTROY      = 0x0002;
	public static final int WM_KEYDOWN      = 0x0100;
	public static final int WM_KEYUP        = 0x0101;
	public static final int WM_SIZE         = 0x0005;
	
	public static final int SC_SCREENSAVE   = 0x0F140;
	public static final int SC_MONITORPOWER = 0x0F170;
	
	public static Proc PeekMessageA(
		final String lpMsg,
		final String hWnd,
		final int wMsgFilterMin,
		final int wMsgFilterMax,
		final int wRemoveMsg
	) {
		return new Proc("PeekMessageA",
			new ArrayList<ValueSizeComment>() {{
				add(new ValueSizeComment(lpMsg, Size.QWORD, "LPMSG lpMsg"));
				add(new ValueSizeComment(hWnd, Size.QWORD, "HWND hWnd"));
				add(new ValueSizeComment(wMsgFilterMin, Size.DWORD, "UINT wMsgFilterMin"));
				add(new ValueSizeComment(wMsgFilterMax, Size.DWORD, "UINT wMsgFilterMax"));
				add(new ValueSizeComment(wRemoveMsg, Size.DWORD, "UINT wRemoveMsg"));
			}},
			new ValueSizeComment(Size.DWORD, "BOOL"));
	}
	
	public static Proc RegisterClassExA(
		final String Arg1
	) {
		return new Proc("RegisterClassExA",
			new ArrayList<ValueSizeComment>() {{
				add(new ValueSizeComment(Arg1, Size.QWORD, "WNDCLASSEXA *Arg1"));
			}},
			new ValueSizeComment(Size.QWORD, "HANDLE"));
	}

	public static final int WS_EX_WINDOWEDGE = 0x00000100;
	public static final int WS_EX_CLIENTEDGE = 0x00000200;
	public static final int WS_EX_OVERLAPPEDWINDOW = WS_EX_WINDOWEDGE | WS_EX_CLIENTEDGE;
	
	public static final int WS_CLIPCHILDREN  = 0x02000000;
	public static final int WS_CLIPSIBLINGS  = 0x04000000;
	public static final int WS_VISIBLE       = 0x10000000;
	public static final int WS_OVERLAPPED    = 0x00000000;
	public static final int WS_CAPTION       = 0x00C00000;
	public static final int WS_SYSMENU       = 0x00080000;
	public static final int WS_THICKFRAME    = 0x00040000;
	public static final int WS_MINIMIZEBOX   = 0x00020000;
	public static final int WS_MAXIMIZEBOX   = 0x00010000;
	public static final int WS_OVERLAPPEDWINDOW = WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX;
	
	public static final int CW_USEDEFAULT    = 0x80000000;

	public static Proc CreateWindowExA(
		final int dwExStyle,
		final String lpClassName,
		final String lpWindowName,
		final int dwStyle,
		final int x,
		final int y,
		final int nWidth,
		final int nHeight,
		final String hWndParent,
		final String hMenu,
		final String hInstance,
		final String lpParam
	) {
		return new Proc("CreateWindowExA",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(dwExStyle, Size.QWORD, "DWORD dwExStyle"));
				// NOTICE: the name used there has to be the same as the one used for RegisterClass
				add(new ValueSizeComment(lpClassName, Size.QWORD, "LPCSTR lpClassName"));
				add(new ValueSizeComment(lpWindowName, Size.QWORD, "LPCSTR lpWindowName"));
				add(new ValueSizeComment(hex(dwStyle), Size.QWORD, "DWORD dwStyle"));
				add(new ValueSizeComment(hex(x), Size.DWORD, "int X"));
				add(new ValueSizeComment(hex(y), Size.DWORD, "int Y"));
				add(new ValueSizeComment(nWidth, Size.DWORD, "int nWidth"));
				add(new ValueSizeComment(nHeight, Size.DWORD, "int nHeight"));
				add(new ValueSizeComment(hWndParent, Size.QWORD, "HWND hWndParent"));
				add(new ValueSizeComment(hMenu, Size.QWORD, "HMENU hMenu"));
				add(new ValueSizeComment(hInstance, Size.QWORD, "HINSTANCE hInstance"));
				add(new ValueSizeComment(lpParam, Size.QWORD, "LPVOID lpParam"));
			}},
			new ValueSizeComment(Size.QWORD, "HANDLE"));
	}

	public static Proc TranslateMessage(
		final String lpMsg
	) {
		return new Proc("TranslateMessage",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(lpMsg, Size.QWORD, "LPMSG lpMsg"));
			}});
	}

	public static Proc DispatchMessageA(
		final String lpMsg
	) {
		return new Proc("DispatchMessageA",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(lpMsg, Size.QWORD, "LPMSG lpMsg"));
			}});
	}

	public static Proc DefWindowProcA(
		final String hWnd,
		final String Msg,
		final String wParam,
		final String lParam
	) {
		return new Proc("DefWindowProcA",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(hWnd, Size.QWORD, "HWND hWnd"));
				add(new ValueSizeComment(Msg, Size.QWORD, "UINT Msg"));
				add(new ValueSizeComment(wParam, Size.QWORD, "WPARAM wParam"));
				add(new ValueSizeComment(lParam, Size.QWORD, "LPARAM lParam"));
			}},
			new ValueSizeComment(Size.QWORD, "LRESULT"));
	}

	public static Proc DestroyWindow(
		final String hWnd
	) {
		return new Proc("DestroyWindow",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(hWnd, Size.QWORD, "HWND hWnd"));
			}});
	}

	public static Proc PostQuitMessage(
		final int nExitCode
	) {
		return new Proc("PostQuitMessage",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(nExitCode, Size.DWORD, "int nExitCode"));
			}});
	}
}