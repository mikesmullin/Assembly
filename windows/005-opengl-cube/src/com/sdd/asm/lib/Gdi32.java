package com.sdd.asm.lib;

import java.util.ArrayList;
import static com.sdd.asm.Macros.*;
import static com.sdd.asm.Macros.Size.*;

/**
 * Microsoft Windows gdi32.dll APIs
 *
 * The point at which third-party graphics libraries intersect with Microsoft's
 * own crappy software-based graphics. Used as a little as possible, just to get
 * a few pointers and some surface area to draw in.
 * 
 * see: http://www.bvbcode.com/code/wmex83ko-286062
 * see: http://programarts.com/cfree_en/wingdi_h.html
 * see: https://java-native-access.github.io/jna/4.2.0/constant-values.html#com.sun.jna.platform.win32.WinGDI.PFD_SUPPORT_OPENGL
 * see: https://docs.microsoft.com/en-us/windows/desktop/api/wingdi/ns-wingdi-tagpixelformatdescriptor
 */
public class Gdi32
{
	
	public static final int PFD_DRAW_TO_WINDOW = 0x00000004;
	public static final int PFD_SUPPORT_OPENGL = 0x00000020;
	public static final int PFD_DOUBLEBUFFER = 0x00000001;
	public static final int PFD_TYPE_RGBA = 0;
	public static final int PFD_MAIN_PLANE = 0;

	public static Struct PIXELFORMATDESCRIPTOR = new Struct("PIXELFORMATDESCRIPTOR")
		.put("nSize", WORD, placeholder("sizeof(struct)"))
		.put("nVersion", WORD, oper(1).comment("(magic constant)"))
		.put("dwFlags", DWORD, oper(0))
		.put("iPixelType", BYTE, oper(0))
		.put("cColorBits", BYTE, oper(0))
		.put("cRedBits", BYTE, oper(0).comment("(not used)"))
		.put("cRedShift", BYTE, oper(0).comment("(not used)"))
		.put("cGreenBits", BYTE, oper(0).comment("(not used)"))
		.put("cGreenShift", BYTE, oper(0).comment("(not used)"))
		.put("cBlueBits", BYTE, oper(0).comment("(not used)"))
		.put("cBlueShift", BYTE, oper(0).comment("(not used)"))
		.put("cAlphaBits", BYTE, oper(0))
		.put("cAlphaShift", BYTE, oper(0).comment("(not used)"))
		.put("cAccumBits", BYTE, oper(0))
		.put("cAccumRedBits", BYTE, oper(0).comment("(not used)"))
		.put("cAccumGreenBits", BYTE, oper(0).comment("(not used)"))
		.put("cAccumBlueBits", BYTE, oper(0).comment("(not used)"))
		.put("cAccumAlphaBits", BYTE, oper(0).comment("(not used)"))
		.put("cDepthBits", BYTE, oper(0))
		.put("cStencilBits", BYTE, oper(0))
		.put("cAuxBuffers", BYTE, oper(0))
		.put("iLayerType", BYTE, oper(0))
		.put("bReserved", BYTE, oper(0).comment("(not used)"))
		.put("dwLayerMask", DWORD, oper(0).comment("(not used)"))
		.put("dwVisibleMask", DWORD, oper(0).comment("(not used)"))
		.put("dwDamageMask", DWORD, oper(0).comment("(not used)"));
	static
	{
		PIXELFORMATDESCRIPTOR.fields.get("nSize")
			.defaultValue(oper(sizeof(PIXELFORMATDESCRIPTOR)));
	}

	public static Proc GetDC(
		final LabelReference hWnd
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"GetDC", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(hWnd).comment( "HWND hWnd")));
			}},
			returnVal(QWORD, "HDC"));
	}
	
	public static Proc ChoosePixelFormat(
		final LabelReference hdc,
		final LabelReference ppfd
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"ChoosePixelFormat", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(hdc).comment("HDC hdc")));
				add(width(QWORD, oper(ppfd).comment("PIXELFORMATDESCRIPTOR *ppfd")));
			}},
			returnVal(DWORD, "int"));
	}

	public static Proc SetPixelFormat(
		final LabelReference hdc,
		final LabelReference format,
		final LabelReference ppfd
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"SetPixelFormat", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(hdc).comment("HDC hdc")));
				add(width(DWORD, oper(format).comment("int format")));
				add(width(QWORD, oper(ppfd).comment("PIXELFORMATDESCRIPTOR *ppfd")));
			}},
			returnVal(DWORD, "BOOL"));
	}

	public static Proc SwapBuffers(
		final LabelReference Arg1
	) {
		return new Proc(addrOf(extern(label(Scope.GLOBAL,"SwapBuffers", true))),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(Arg1).comment("HDC Arg1")));
			}},
			returnVal(DWORD, "BOOL"));
	}
}
