package com.sdd.asm.lib;

import java.util.ArrayList;
import static com.sdd.asm.Macros.*;
import static com.sdd.asm.Macros.Size.*;

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
	public static final CustomType UINT = new CustomType(DWORD, "UINT");
	public static final CustomType WNDPROC = new CustomType(QWORD, "WNDPROC");
	public static final CustomType INT = new CustomType(DWORD, "int");
	public static final CustomType HINSTANCE = new CustomType(QWORD, "HINSTANCE");
	public static final CustomType HICON = new CustomType(QWORD, "HICON");
	public static final CustomType HCURSOR = new CustomType(QWORD, "HCURSOR");
	public static final CustomType HBRUSH = new CustomType(QWORD, "HBRUSH");
	public static final CustomType LPCSTR = new CustomType(QWORD, "LPCSTR");

	public static final CustomType HWND = new CustomType(QWORD, "HWND");
	// public static final StructType POINT = new StructTypeQWORD("POINT");
	public static final CustomType WPARAM = new CustomType(QWORD, "WPARAM");
	public static final CustomType LPARAM = new CustomType(QWORD, "LPARAM");

	public static final int CS_VREDRAW = 0x0001;
	public static final int CS_HREDRAW = 0x0002;
	public static final int CS_OWNDC   = 0x0020;

	public static Struct tagWNDCLASSEXA = new Struct("tagWNDCLASSEXA")
		.put("cbSize", UINT)
		.put("style", UINT)
		.put("lpfnWndProc", WNDPROC)
		.put("cbClsExtra", INT, Null())
		.put("cbWndExtra", INT, Null())
		.put("hInstance", HINSTANCE)
		.put("hIcon", HICON)
		.put("hCursor", HCURSOR)
		.put("hbrBackground", HBRUSH, Null()) // 0 is required for OpenGL Context
		.put("lpszMenuName", LPCSTR, Null())
		.put("lpszClassName", LPCSTR)
		.put("hIconSm", HICON, Null());
	static
	{
		tagWNDCLASSEXA.fields.get("cbSize")
			.defaultValue(oper(sizeof(tagWNDCLASSEXA)));
	}

	public static final int OIC_WINLOGO = 32517;
	public static final int IDC_ARROW = 32512;
	public static final int IMAGE_ICON = 1;
	public static final int IMAGE_CURSOR = 2;
	public static final int LR_DEFAULTSIZE = 0x00000040;
	public static final int LR_SHARED = 0x00008000;

	public static Proc LoadImageA(
		final Operand hInst,
		final Operand name, // can be int or String; can make overload when that is needed
		final int type,
		final int cx,
		final int cy,
		final int fuLoad
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"LoadImageA", true))),
			new ArrayList<SizedOp>() {{
				add(width(DWORD, hInst.comment("HINSTANCE hInst")));
				add(width(DWORD, name.comment("LPCSTR name")));
				add(width(DWORD, oper(type).comment("UINT type")));
				add(width(DWORD, oper(cx).comment("int cx")));
				add(width(DWORD, oper(cy).comment("int cy")));
				add(width(DWORD, oper(fuLoad).comment("UINT fuLoad")));
			}},
			width(QWORD, placeholder("HANDLE")));
	}
	
	public static Struct tagMSG = new Struct("tagMSG")
		.put("hwnd", HWND, oper(0))
		.put("message", UINT, oper(0))
		.put("wParam", WPARAM, oper(0))
		.put("lParam", LPARAM, oper(0))
		.put("time", DWORD, oper(0))
		.put("pt.x", DWORD, oper(0))
		.put("pt.y", DWORD, oper(0))
		.put("lPrivate", DWORD, oper(0));

	public static final int PM_REMOVE = 0x0001;

	public enum WindowMessage implements BitField
	{
		WM_QUIT         (0x0012),
		WM_ACTIVATE     (0x0006),
		WM_SYSCOMMAND   (0x0112),
		WM_CLOSE        (0x0010),
		WM_DESTROY      (0x0002),
		WM_NCDESTROY    (0x0082),
		WM_KEYDOWN      (0x0100),
		WM_KEYUP        (0x0101),
		WM_SIZE         (0x0005);
		
		public final int value;
		WindowMessage(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}
	
	public static final int SC_SCREENSAVE   = 0x0F140;
	public static final int SC_MONITORPOWER = 0x0F170;
	
	public static Proc PeekMessageA(
		final LabelReference lpMsg,
		final Operand hWnd,
		final int wMsgFilterMin,
		final int wMsgFilterMax,
		final int wRemoveMsg
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"PeekMessageA", true))),
			new ArrayList<SizedOp>() {{
				add(width(QWORD, oper(lpMsg).comment("LPMSG lpMsg")));
				add(width(QWORD, hWnd.comment("HWND hWnd")));
				add(width(DWORD, oper(wMsgFilterMin).comment("UINT wMsgFilterMin")));
				add(width(DWORD, oper(wMsgFilterMax).comment("UINT wMsgFilterMax")));
				add(width(DWORD, oper(wRemoveMsg).comment("UINT wRemoveMsg")));
			}},
			returnVal(DWORD, "BOOL"));
	}
	
	public static Proc RegisterClassExA(
		final LabelReference Arg1
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"RegisterClassExA", true))),
			new ArrayList<SizedOp>() {{
				add(width(QWORD, oper(Arg1).comment("WNDCLASSEXA *Arg1")));
			}},
			returnVal(QWORD, "HANDLE"));
	}

	public enum WindowExtendedStyle implements BitField
	{
		WS_EX_WINDOWEDGE (0x00000100);

		public final int value;
		WindowExtendedStyle(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}

	public enum WindowStyle implements BitField
	{
		WS_CLIPCHILDREN (0x02000000),
		WS_CLIPSIBLINGS (0x04000000),
		WS_VISIBLE      (0x10000000),
		WS_OVERLAPPED   (0x00000000),
		WS_CAPTION      (0x00C00000),
		WS_SYSMENU      (0x00080000),
		WS_THICKFRAME   (0x00040000),
		WS_MINIMIZEBOX  (0x00020000),
		WS_MAXIMIZEBOX  (0x00010000);
		
		public final int value;
		WindowStyle(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}
	
	public static final int CW_USEDEFAULT    = 0x80000000;

	public static Proc CreateWindowExA(
		final WindowExtendedStyle[] dwExStyle,
		final LabelReference lpClassName,
		final LabelReference lpWindowName,
		final WindowStyle[] dwStyle,
		final int x,
		final int y,
		final int nWidth,
		final int nHeight,
		final Operand hWndParent,
		final Operand hMenu,
		final LabelReference hInstance,
		final Operand lpParam
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"CreateWindowExA", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, bitField(dwExStyle).comment("DWORD dwExStyle")));
				// NOTICE: the name used there has to be the same as the one used for RegisterClass
				add(width(QWORD, oper(lpClassName).comment("LPCSTR lpClassName")));
				add(width(QWORD, oper(lpWindowName).comment("LPCSTR lpWindowName")));
				add(width(QWORD, bitField(dwStyle).comment("DWORD dwStyle")));
				add(width(DWORD, oper(x).comment("int X")));
				add(width(DWORD, oper(y).comment("int Y")));
				add(width(DWORD, oper(nWidth).comment("int nWidth")));
				add(width(DWORD, oper(nHeight).comment("int nHeight")));
				add(width(QWORD, hWndParent.comment("HWND hWndParent")));
				add(width(QWORD, hMenu.comment("HMENU hMenu")));
				add(width(QWORD, oper(hInstance).comment("HINSTANCE hInstance")));
				add(width(QWORD, lpParam.comment("LPVOID lpParam")));
			}},
			returnVal(QWORD, "HANDLE"));
	}

	public static Proc TranslateMessage(
		final LabelReference lpMsg
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"TranslateMessage", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(lpMsg).comment("LPMSG lpMsg")));
			}});
	}

	public static Proc DispatchMessageA(
		final LabelReference lpMsg
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"DispatchMessageA", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(lpMsg).comment("LPMSG lpMsg")));
			}});
	}

	public static Proc DefWindowProcA(
		final LabelReference hWnd,
		final LabelReference Msg,
		final LabelReference wParam,
		final LabelReference lParam
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"DefWindowProcA", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(hWnd).comment("HWND hWnd")));
				add(width(QWORD, oper(Msg).comment("UINT Msg")));
				add(width(QWORD, oper(wParam).comment("WPARAM wParam")));
				add(width(QWORD, oper(lParam).comment("LPARAM lParam")));
			}},
			width(QWORD, placeholder("LRESULT")));
	}

	public static Proc DestroyWindow(
		final LabelReference hWnd
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"DestroyWindow", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(hWnd).comment("HWND hWnd")));
			}});
	}

	public static Proc PostQuitMessage(
		final int nExitCode
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"PostQuitMessage", true))),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(nExitCode).comment("int nExitCode")));
			}});
	}
}