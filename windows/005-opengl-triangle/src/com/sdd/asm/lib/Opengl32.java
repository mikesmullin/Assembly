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

	public enum GlMask implements BitField
	{
		GL_COLOR_BUFFER_BIT(0x00004000),
		GL_DEPTH_BUFFER_BIT(0x00000100);

		public final int value;
		GlMask(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}

	public static Proc glClear(
		final GlMask[] mask
	) {
		return new Proc(Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glClear", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, bitField(mask).comment("GLbitfield mask")));
			}});
	}

	public static Proc glGetError() {
		return new Proc(Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glGetError", true)),
			returnVal(DWORD, "GLenum errCode"));
	}
	
	public enum GlString implements BitField
	{
		GL_VENDOR(0x1F00),
		GL_RENDERER(0x1F01),
		GL_VERSION(0x1F02);

		public final int value;
		GlString(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}	
	
	public static Proc glGetString(
		final GlString name
	) {
		return new Proc(
//			Macros::__ms_fastcall_64_w_glGetError,
			deref(label(Scope.GLOBAL, "glGetString", true)),
			new ArrayList<SizedOp>(){{
				add(width(QWORD, bitField(name).comment("GLenum name")));
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

	/**
	 * Windows opengl32.dll only exports OpenGL 1.1, 
	 * the newer API calls are only accessible as extensions,
	 * so we use wglGetProcAddress() to find them.
	 */
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
	public static Proc glCreateShader(
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
	public static Proc glShaderSource(
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
	public static Proc glCompileShader(
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
	public static Proc glGetShaderiv(
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

	public static Proc glCreateProgram() {
		return new Proc(
			Macros::__ms_fastcall_64_w_glGetError,
			deref(label(Scope.GLOBAL, "glCreateProgram", true)),
			returnVal(QWORD, "GLuint")
		);
	}
	
	public static Proc glAttachShader(
		final Label program,
		final Label shader
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glAttachShader", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(program)).comment("GLuint program")));
				add(width(DWORD, oper(deref(shader)).comment("GLuint shader")));
			}});
	}

	public static Proc glBindAttribLocation(
		final Label program,
		final int index,
		final Label name
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glBindAttribLocation", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(program)).comment("GLuint program")));
				add(width(DWORD, oper(index).comment("GLuint index")));
				add(width(QWORD, oper(addrOf(name)).comment("const GLchar *name")));
			}});
	}

	public static Proc glLinkProgram(
		final Label program
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glLinkProgram", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(program)).comment("GLuint program")));
			}});
	}

	public enum GlGetProgramIvPName implements BitField
	{
		GL_LINK_STATUS(0x8B82);
		
		public final int value;
		GlGetProgramIvPName(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}
	
	public static Proc glGetProgramiv(
		final Label program,
		final GlGetProgramIvPName pname,
		final Label params

	) {
		return new Proc(
			Macros::__ms_fastcall_64, // returns void; manually check params for 0 to indicate glError
			deref(label(Scope.GLOBAL, "glGetProgramiv", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(program)).comment("GLuint program")));
				add(width(DWORD, bitField(pname).comment("GLenum pname")));
				add(width(QWORD, oper(addrOf(params)).comment("GLint *params")));
			}});
	}

	public static Proc glGetProgramInfoLog(
		final Label program,
		final int maxLength, // size of output string allocation
		final Label length, // mutated input; length of output string
		final Label infoLog // mutated input; the output string
	) {
		return new Proc(
			Macros::__ms_fastcall_64, // must manually check length for error
			deref(label(Scope.GLOBAL, "glGetProgramInfoLog", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(program)).comment("GLuint program")));
				add(width(DWORD, oper(maxLength).comment("GLsizei maxLength")));
				add(width(QWORD, oper(addrOf(length)).comment("GLsizei *length")));
				add(width(QWORD, oper(addrOf(infoLog)).comment("GLchar *infoLog")));
			}});
	}

	public static Proc glGetAttribLocation(
		final Label program,
		final Label name
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glGetAttribLocation", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(program)).comment("GLuint program")));
				add(width(QWORD, oper(addrOf(name)).comment("const GLchar *name")));
			}},
			returnVal(DWORD, "GLuint"));
	}

	// TODO: there is an overload verison of this function acceping a 2nd param
	public static Proc glEnableVertexAttribArray(
		final Label index
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glEnableVertexAttribArray", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(index)).comment("GLuint index")));
			}});
	}

	public static Proc glUseProgram(
		final Label program
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glUseProgram", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(program)).comment("GLuint program")));
			}});
	}

	public static Proc glGenBuffers(
		final int n,
		final Label buffers
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glGenBuffers", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(n).comment("GLsizei n")));
				add(width(QWORD, oper(addrOf(buffers)).comment("GLuint * buffers")));
			}});
	}
	
	public enum GlTarget implements BitField
	{
		GL_ARRAY_BUFFER(0x8892),
		GL_ELEMENT_ARRAY_BUFFER(0x8893);

		public final int value;
		GlTarget(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}

	public static Proc glBindBuffer(
		final GlTarget target,
		final Label buffer
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glBindBuffer", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, bitField(target).comment("GLenum target")));
				add(width(QWORD, oper(addrOf(buffer)).comment("GLuint buffer")));
			}});
	}

	public enum GlUsage implements BitField
	{
//		GL_STREAM_DRAW
//		GL_STREAM_READ
//		GL_STREAM_COPY
		GL_STATIC_DRAW(0x88E4);
//		GL_STATIC_READ
//		GL_STATIC_COPY
//		GL_DYNAMIC_DRAW
//		GL_DYNAMIC_READ
//		GL_DYNAMIC_COPY
			
		public final int value;
		GlUsage(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}
	
	public static Proc glBufferData(
		final GlTarget target,
		final int sizeInBytes,
		final Label data,
		final GlUsage usage
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glBufferData", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, bitField(target).comment("GLenum target")));
				add(width(QWORD, oper(sizeInBytes).comment("GLsizeiptr size")));
				add(width(QWORD, oper(addrOf(data)).comment("const GLvoid * data")));
				add(width(DWORD, bitField(usage).comment("GLenum usage")));
			}});
	}

	// see: https://www.khronos.org/opengl/wiki/OpenGL_Type
	public enum GlLength implements BitField
	{
//		GL_BYTE(0x1400),
//		GL_UNSIGNED_BYTE(0x1401),
//		GL_SHORT(0x1402),
		GL_UNSIGNED_SHORT(0x1403),
//		GL_INT(0x1404),
//		GL_UNSIGNED_INT(0x1405),
		GL_FLOAT(0x1406);
//		GL_HALF_FLOAT(),
//		GL_DOUBLE(),
//		GL_FIXED(0x140C),
//		GL_INT_2_10_10_10_REV(),
//		GL_UNSIGNED_INT_2_10_10_10_REV(),
//		GL_UNSIGNED_INT_10F_11F_11F_REV();

		public final int value;
		GlLength(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}

	public static Proc glVertexAttribPointer(
		final Label index,
		final int size,
		final GlLength type,
		final GlType normalized,
		final int stride,
		final int pointer
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glVertexAttribPointer", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, oper(deref(index)).comment("GLuint index")));
				add(width(QWORD, oper(size).comment("GLint size")));
				add(width(QWORD, bitField(type).comment("GLenum type")));
				add(width(DWORD, bitField(normalized).comment("GLboolean normalized")));
				add(width(DWORD, oper(stride).comment("GLsizei stride")));
				add(width(DWORD, oper(pointer).comment("const GLvoid * pointer")));
			}});
	}

	public enum GlCapability implements BitField
	{
//		GL_BLEND(),
//		GL_CLIP_DISTANCE(),
//		GL_COLOR_LOGIC_OP(),
//		GL_CULL_FACE(),
//		GL_DEBUG_OUTPUT(),
//		GL_DEBUG_OUTPUT_SYNCHRONOUS(),
//		GL_DEPTH_CLAMP(),
		GL_DEPTH_TEST(0x0B71);
//		GL_DITHER(),
//		GL_FRAMEBUFFER_SRGB(),
//		GL_LINE_SMOOTH(),
//		GL_MULTISAMPLE(),
//		GL_POLYGON_OFFSET_FILL(),
//		GL_POLYGON_OFFSET_LINE(),
//		GL_POLYGON_OFFSET_POINT(),
//		GL_POLYGON_SMOOTH(),
//		GL_PRIMITIVE_RESTART(),
//		GL_PRIMITIVE_RESTART_FIXED_INDEX(),
//		GL_RASTERIZER_DISCARD(),
//		GL_SAMPLE_ALPHA_TO_COVERAGE(),
//		GL_SAMPLE_ALPHA_TO_ONE(),
//		GL_SAMPLE_COVERAGE(),
//		GL_SAMPLE_SHADING(),
//		GL_SAMPLE_MASK(),
//		GL_SCISSOR_TEST(),
//		GL_STENCIL_TEST(),
//		GL_TEXTURE_CUBE_MAP_SEAMLESS(),
//		GL_PROGRAM_POINT_SIZE();
		
		public final int value;
		GlCapability(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}

	public static Proc glEnable(
		final GlCapability cap
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glEnable", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, bitField(cap).comment("GLenum cap")));
			}});
	}


		
	public enum GlCompare implements BitField
	{
//		GL_NEVER(),
		GL_LESS(0x0201);
//		GL_EQUAL(),
//		GL_LEQUAL(),
//		GL_GREATER(),
//		GL_NOTEQUAL(),
//		GL_GEQUAL(),
//		GL_ALWAYS();

		public final int value;
		GlCompare(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}
	
	public static Proc glDepthFunc(
		final GlCompare func
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glDepthFunc", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, bitField(func).comment("GLenum func")));
			}});
	}

	public enum GlDrawMode implements BitField
	{
//		GL_POINTS(0x0000),
//		GL_LINES(0x0001),
//		GL_LINE_LOOP(0x0002),
//		GL_LINE_STRIP(0x0003),
		GL_TRIANGLES(0x0004);
//		GL_TRIANGLE_STRIP(0x0005),
//		GL_TRIANGLE_FAN(0x0006),
//		GL_LINE_STRIP_ADJACENCY(),
//		GL_LINES_ADJACENCY(),
//		GL_TRIANGLE_STRIP_ADJACENCY(),
//		GL_TRIANGLES_ADJACENCY(),
//		GL_PATCHES();

		public final int value;
		GlDrawMode(final int value) { this.value = value; }
		public String getName() { return name(); }
		public int getValue() { return value; }
	}

	public static Proc glDrawElements(
		final GlDrawMode mode,
		final int count,
		// Must be one of GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT.
		final GlLength type,
		final int indicies
	) {
		return new Proc(
			Macros::__ms_fastcall_64,
			deref(label(Scope.GLOBAL, "glDrawElements", true)),
			new ArrayList<SizedOp>(){{
				add(width(DWORD, bitField(mode).comment("GLenum mode")));
				add(width(QWORD, oper(count).comment("GLsizei count")));
				add(width(QWORD, bitField(type).comment("GLenum type")));
				add(width(DWORD, oper(indicies).comment("const GLvoid * indices")));
			}});
	}
}