package com.sdd.asm.lib;

import java.util.ArrayList;
import static com.sdd.asm.Macros.*;

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

	public static Struct PIXELFORMATDESCRIPTOR = new Struct("PIXELFORMATDESCRIPTOR") {{
		fields.put("nSize", new StructType(WORD, operand(0, "sizeof(struct)")));
		fields.put("nVersion", new StructType(WORD, operand(1, "(magic constant)")));
		fields.put("dwFlags", new StructType(DWORD, operand(0)));
		fields.put("iPixelType", new StructType(BYTE, operand(0)));
		fields.put("cColorBits", new StructType(BYTE, operand(0)));
		fields.put("cRedBits", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cRedShift", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cGreenBits", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cGreenShift", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cBlueBits", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cBlueShift", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cAlphaBits", new StructType(BYTE, operand(0)));
		fields.put("cAlphaShift", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cAccumBits", new StructType(BYTE, operand(0)));
		fields.put("cAccumRedBits", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cAccumGreenBits", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cAccumBlueBits", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cAccumAlphaBits", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("cDepthBits", new StructType(BYTE, operand(0)));
		fields.put("cStencilBits", new StructType(BYTE, operand(0)));
		fields.put("cAuxBuffers", new StructType(BYTE, operand(0)));
		fields.put("iLayerType", new StructType(BYTE, operand(0)));
		fields.put("bReserved", new StructType(BYTE, operand(0, "(not used)")));
		fields.put("dwLayerMask", new StructType(DWORD, operand(0, "(not used)")));
		fields.put("dwVisibleMask", new StructType(DWORD, operand(0, "(not used)")));
		fields.put("dwDamageMask", new StructType(DWORD, operand(0, "(not used)")));
	}};
	static
	{
		PIXELFORMATDESCRIPTOR.fields.get("nSize").defaultValue.str =
			Integer.toString(sizeof(PIXELFORMATDESCRIPTOR));
	}

	public static Proc GetDC(
		final String hWnd
	) {
		return new Proc("GetDC",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(hWnd, Size.QWORD, "HWND hWnd"));
			}},
			new ValueSizeComment(Size.QWORD, "HDC"));
	}
	
	public static Proc ChoosePixelFormat(
		final String hdc,
		final String ppfd
	) {
		return new Proc("ChoosePixelFormat",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(hdc, Size.QWORD, "HDC hdc"));
				add(new ValueSizeComment(ppfd, Size.QWORD, "PIXELFORMATDESCRIPTOR *ppfd"));
			}},
			new ValueSizeComment(Size.DWORD, "int"));
	}

	public static Proc SetPixelFormat(
		final String hdc,
		final String format,
		final String ppfd
	) {
		return new Proc("SetPixelFormat",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(hdc, Size.QWORD, "HDC hdc"));
				add(new ValueSizeComment(format, Size.DWORD, "int format"));
				add(new ValueSizeComment(ppfd, Size.QWORD, "PIXELFORMATDESCRIPTOR *ppfd"));
			}},
			new ValueSizeComment(Size.DWORD, "BOOL"));
	}

	public static Proc SwapBuffers(
		final String Arg1
	) {
		return new Proc("SwapBuffers",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(Arg1, Size.QWORD, "HDC Arg1"));
			}},
			new ValueSizeComment(Size.DWORD, "BOOL"));
	}
}
