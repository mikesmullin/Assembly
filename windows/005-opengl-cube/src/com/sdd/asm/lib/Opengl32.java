package com.sdd.asm.lib;

import com.sdd.asm.Macros;

import java.util.ArrayList;

import static com.sdd.asm.Macros.*;
import static com.sdd.asm.Macros.Scope.*;
import static com.sdd.asm.Macros.Size.*;

/**
 * Microsoft Windows opengl32.dll APIs
 *
 * Responsible for all 3d graphics.
 *
 * see: https://www.khronos.org/registry/OpenGL/api/GLES2/gl2.h
 * see: https://www.opengl.org/archives/resources/faq/technical/extensions.htm
 * see: http://docs.gl/gl4/glCreateShader
 * see: https://stackoverflow.com/a/6562159
 */
public class Opengl32
{
	public static Proc wglCreateContext(
		final LabelReference Arg1
	) {
		return new Proc(
			deref(label(Scope.GLOBAL, "wglCreateContext", true)),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(Arg1).comment("HDC Arg1")));
			}},
			returnVal(QWORD, "HGLRC"));
	}

	public static Proc wglMakeCurrent(
		final LabelReference hdc,
		final LabelReference hglrc
	) {
		return new Proc(
			deref(label(Scope.GLOBAL, "wglMakeCurrent", true)),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(hdc).comment("HDC")));
				add(width(QWORD, oper(hglrc).comment("HGLRC")));
			}},
			returnVal(DWORD, "BOOL"));
	}

	public static Proc glClearColor(
		final float red,
		final float green,
		final float blue,
		final float alpha
	) {
		return new Proc(Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glClearColor", true)),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(red).comment("GLclampf red")));
				add(width(QWORD, oper(green).comment("GLclampf green")));
				add(width(QWORD, oper(blue).comment("GLclampf blue")));
				add(width(QWORD, oper(alpha).comment("GLclampf alpha")));
			}});
	}

	public static final int GL_COLOR_BUFFER_BIT = 0x00004000;

	public static final Proc glClear(
		final int mask
	) {
		return new Proc(Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glClear", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(mask).comment("GLbitfield mask")));
			}});
	}

	public static final Proc glGetError() {
		return new Proc(Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glGetError", true)),
			returnVal(DWORD, "GLenum errCode"));
	}
	
	public static final int GL_VERSION = 0x1F02;
	
	public static Proc glGetString(
		final Operand name
	) {
		return new Proc(
//			Macros::__ms_fastcall_64_w_glGetError,
			deref(label(Scope.GLOBAL, "glGetString", true)),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, name.comment("GLenum name")));
			}},
			returnVal(QWORD, "GLubyte* WINAPI"));
	}

	public static Proc wglGetProcAddress(
		final Label functionName
	) {
		return new Proc(
			deref(label(Scope.GLOBAL, "wglGetProcAddress", true)),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, oper(addrOf(functionName)).comment("LPCSTR Arg1")));
			}},
			returnVal(QWORD, "PROC"));
	}
	
	public static String glimport(final String... procs)
	{
		String out = join(
			comment("dynamically load GL extensions at runtime"));
		for (final String proc : procs)
		{
			final Label procName = label(GLOBAL, "wglGetProcAddress__"+ proc, true);
			final Label procAddr = label(GLOBAL, proc, true);
			data(procName, BYTE, nullstr(proc));
			data(procAddr, QWORD);
			out += join(
				assign_call(procAddr, wglGetProcAddress(procName)));
		}
		return out;
	}
	
	public enum GlShaderType implements BitField
	{
		GL_FRAGMENT_SHADER(0x8B30),
		GL_VERTEX_SHADER(0x8B31);

		public final int value;
		GlShaderType(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}

	/**
	 * Create a shader object
	 * see: https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glCreateShader.xml
	 */
	public static final Proc glCreateShader(
		final GlShaderType shaderType
	) {
		return new Proc(
			Macros::__ms_fastcall_64_w_glGetError,
			deref(label(Scope.GLOBAL, "glCreateShader", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, bitField(shaderType).comment("GLenum shaderType")));
			}},
			returnVal(DWORD, "GLuint"));
	}

	/**
	 * Replace the OpenGL Shading Language (GLSL) source code for a given a shader object.
	 * 
	 * see: https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glShaderSource.xml
	 * see: https://stackoverflow.com/a/22100410
	 * see: https://en.wikipedia.org/wiki/OpenGL_Shading_Language
	 * see: http://glslsandbox.com/
	 */
	public static final Proc glShaderSource(
		final Label shader,
		final int count,
		final Label sources,
		final Label lengths
	) {
		return new Proc(
			Macros::__ms_fastcall_64_w_glGetError,
			deref(label(Scope.GLOBAL, "glShaderSource", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(shader)).comment("GLuint shader")));
				add(width(DWORD, oper(count).comment("GLsizei count")));
				add(width(QWORD, oper(addrOf(sources)).comment("const GLchar * const *string")));
				add(width(QWORD, oper(addrOf(lengths)).comment("const GLint *length")));
			}});
	}

	/**
	 * Compile a shader object
	 * see: https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glCompileShader.xml
	 */
	public static final Proc glCompileShader(
		final Label shader
	) {
		return new Proc(
			Macros::__ms_fastcall_64_w_glGetError,
			deref(label(Scope.GLOBAL, "glCompileShader", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(shader)).comment("GLuint shader")));
			}});
	}
	
	public enum GlGetShaderIvPName implements BitField
	{
		GL_COMPILE_STATUS(0x8B81),
		GL_INFO_LOG_LENGTH(0x8B84);

		public final int value;
		GlGetShaderIvPName(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}

	public enum GlType implements BitField
	{
		GL_FALSE(0),
		GL_TRUE(1);

		public final int value;
		GlType(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}
	
	/**
	 * Return a parameter from a shader object
	 * see: https://www.khronos.org/registry/OpenGL-Refpages/es2.0/xhtml/glGetShaderiv.xml
	 */
	public static final Proc glGetShaderiv(
		final Label shader,
		final GlGetShaderIvPName pname,
		final Label params
		
	) {
		return new Proc(
			Macros::__ms_fastcall_64, // returns void; manually check params for 0 to indicate glError
			deref(label(Scope.GLOBAL, "glGetShaderiv", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(shader)).comment("GLuint shader")));
				add(width(DWORD, bitField(pname).comment("GLenum pname")));
				add(width(QWORD, oper(addrOf(params)).comment("GLint *params")));
			}});
	}

	public static Proc glGetShaderInfoLog(
		final Label shader,
		final int maxLength, // size of output string allocation
		final Label length, // mutated input; length of output string
		final Label infoLog // mutated input; the output string
	) {
		return new Proc(
			Macros::__ms_fastcall_64, // must manually check length for error
			deref(label(Scope.GLOBAL, "glGetShaderInfoLog", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(shader)).comment("GLuint shader")));
				add(width(DWORD, oper(maxLength).comment("GLsizei maxLength")));
				add(width(QWORD, oper(addrOf(length)).comment("GLsizei *length")));
				add(width(QWORD, oper(addrOf(infoLog)).comment("GLchar *infoLog")));
			}});
	}
}