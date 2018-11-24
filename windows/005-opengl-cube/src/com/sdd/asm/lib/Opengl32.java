package com.sdd.asm.lib;

import com.sdd.asm.Macros;

import java.util.ArrayList;

import static com.sdd.asm.Macros.*;

/**
 * Microsoft Windows opengl32.dll APIs
 *
 * Responsible for all 3d graphics.
 *
 * see: https://www.khronos.org/registry/OpenGL/api/GLES2/gl2.h
 */
public class Opengl32
{
	public static Proc wglCreateContext(
		final String Arg1
	) {
		return new Proc("[wglCreateContext]",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(Arg1, Size.QWORD, "HDC Arg1"));
			}},
			new ValueSizeComment(Size.QWORD, "HGLRC"));
	}

	public static Proc wglMakeCurrent(
		final String hdc,
		final String hglrc
	) {
		return new Proc("[wglMakeCurrent]",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(hdc, Size.QWORD, "HDC"));
				add(new ValueSizeComment(hglrc, Size.QWORD, "HGLRC"));
		}},
		new ValueSizeComment(Size.DWORD, "BOOL"));
	}

	public static Proc glClearColor(
		final float red,
		final float green,
		final float blue,
		final float alpha
	) {
		return new Proc(Macros::__ms_fastcall_64, "[glClearColor]",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(red, Size.QWORD, "GLclampf red"));
				add(new ValueSizeComment(green, Size.QWORD, "GLclampf green"));
				add(new ValueSizeComment(blue,  Size.QWORD, "GLclampf blue"));
				add(new ValueSizeComment(alpha,  Size.QWORD, "GLclampf alpha"));
			}});
	}

	public static final int GL_COLOR_BUFFER_BIT = 0x00004000;

	public static final Proc glClear(
		final int mask
	) {
		return new Proc(Macros::__ms_fastcall_64, "[glClear]",
			new ArrayList<ValueSizeComment>(){{
				add(new ValueSizeComment(mask, Size.DWORD, "GLbitfield mask"));
			}});
	}
}