package com.sdd.asm.demo.opengl;

import java.util.HashMap;

import static com.sdd.asm.Macros.*;
import static com.sdd.asm.Macros.Scope.*;
import static com.sdd.asm.Macros.Size.*;
import static com.sdd.asm.Macros.Register.Name.*;
import static com.sdd.asm.Macros.Compare.*;
// interfaces
import static com.sdd.asm.util.Utils.*;
import static com.sdd.asm.lib.Kernel32.*;
import static com.sdd.asm.lib.User32.*;
import static com.sdd.asm.lib.User32.WindowMessage.*;
import static com.sdd.asm.lib.User32.WindowExtendedStyle.*;
import static com.sdd.asm.lib.User32.WindowStyle.*;
import static com.sdd.asm.lib.Gdi32.*;
import static com.sdd.asm.lib.Gdi32.PixelFormatFlags.*;
import static com.sdd.asm.lib.Gdi32.PixelFormatLayerType.*;
import static com.sdd.asm.lib.Gdi32.PixelFormatPixelType.*;
import static com.sdd.asm.lib.Opengl32.*;
import static com.sdd.asm.lib.Opengl32.GlGetShaderIvPName.*;
import static com.sdd.asm.lib.Opengl32.GlShaderType.*;
import static com.sdd.asm.lib.Opengl32.GlType.*;
import static com.sdd.asm.lib.Opengl32.GlGetProgramIvPName.*;
import static com.sdd.asm.lib.Opengl32.GlTarget.*;
import static com.sdd.asm.lib.Opengl32.GlUsage.*;
import static com.sdd.asm.lib.Opengl32.GlLength.*;
import static com.sdd.asm.lib.Opengl32.GlDrawMode.*;
import static com.sdd.asm.lib.Opengl32.GlMask.*;
import static com.sdd.asm.lib.Opengl32.GlCapability.*;
import static com.sdd.asm.lib.Opengl32.GlCompare.*;
import static com.sdd.asm.lib.Opengl32.GlString.*;

public class Main
{
	public static void main(final String[] args) 
	{
		build();
		writeToDisk();
	}
	
	private static String VERTEX_SHADER_SRC = join(
		"#version 400"
		,"in vec2 position;" // the position of the point
		,"void main() {" // pre-built function
		,"  gl_Position = vec4(position, 0., 1.);" // 0. is the z, and 1 is w
		,"}");
	
	private static String FRAGMENT_SHADER_SRC = join(
		"#version 400"
		,"void main() {"
		,"  gl_FragColor = vec4(1.,0.,0., 1.);"
		,"}");
	
	private static Label newShader(
		final String src,
		final GlShaderType type
	) {
		final Label shader;
		final Label sources = data(label(GLOBAL, "glShaderSource__sources"), BYTE, nullstr(src));
		final Label sourcesArray = data(label(GLOBAL, "glShaderSource__sources_array"), QWORD, sources.toString());
		final Label lengths = data(label(GLOBAL, "glShaderSource__lengths"), DWORD, Integer.toString(src.length()));
		final Label success = data(label(GLOBAL, "glCompileShader__success"), DWORD);
		final Label handleError = label(LOCAL, "newShader__handleError");
		final Label done = label(LOCAL, "newShader__done");
		final int BUFFER_SIZE = 256;
		final Label buffer = data(label(GLOBAL, "glGetShaderInfoLog_buffer"), BUFFER_SIZE, BYTE);
		final Label bufferlen = data(label(GLOBAL, "glGetShaderInfoLog_buffer_len"), DWORD);
		asm(
			assign_call(shader = label(GLOBAL, "glCreateShader__shader"), glCreateShader(type)),
			call(glShaderSource(shader, 1, sourcesArray, lengths)),
			call(glCompileShader(shader)),
			assign_mov(DWORD, success, oper(0)), // reset error to zero
			call(glGetShaderiv(shader, GL_COMPILE_STATUS, success)),
			jmp_if(DWORD, oper(deref(success)), NOT_EQUAL, bitField(GL_TRUE), handleError),
			jmp(done),
			
			def_label(handleError),
//			call(glGetShaderiv(shader, GL_INFO_LOG_LENGTH, success)),
			call(glGetShaderInfoLog(shader, BUFFER_SIZE, bufferlen, buffer)),
			// TODO: not sure why i can't pass buffer to FormatStringA here
			//       it works everywhere else. something strange about it.
			Console.log("GL Shader Compiler Error:"),
			// NOTE: errors are typically printed with a prefix like 0(3) 
			//       which seems to mean line 3, char 0. though char 0 is often
			//       not specific enough, the line number is probably all most ppl need.
			//       i could be wrong about 0 indicating the character position.
			assign_call(success, WriteToPipe.stdout_without_fail_check(buffer, bufferlen)),
			exit(oper(1)),
			def_label(done));
		return shader;
	}
	
	private static void build()
	{
		final Label MainWindow, PixelFormat, IncomingMessage; // struct instances
		asm(comment("GOAL: Render OpenGL spinning 3d cube animation", ""));
		init();

		section(".text");
		final Label main = label(EXPORT, "main");
		final Label uuid = label(GLOBAL, "Generic__uuid");
		final Label mutex = label(GLOBAL, "CreateMutexA__handle");
		final Label process = label(GLOBAL, "GetModuleHandleA__hModule");
		final Label icon = label(GLOBAL,"CreateWindow__icon");
		final Label cursor = label(GLOBAL, "CreateWindow__cursor");
		final Label atom = label(GLOBAL, "CreateWindow__atom_name");
		final Label title = label(GLOBAL, "CreateWindow__title");
		final Label window = label(GLOBAL, "CreateWindow__hWnd");
		final Label ctx2d = label(GLOBAL, "GetDC__hDC");
		final Label pixelFormat = label(GLOBAL, "ChoosePixelFormat__format");
		final Label success = label(GLOBAL, "Generic__success");
		data(success, QWORD); // shared/reused; make room for largest case
		final Label ctx = label(GLOBAL, "wglCreateContext__ctx");
		final Label Loop = label(GLOBAL, "Loop");
		final Label Render = label(LOCAL, "Render");
		final Label ProcessMessage = label(LOCAL, "Loop__processMessage");
		final Label WndProc = label(GLOBAL, "WndProc");
		final Label WndProc_return_zero = label(GLOBAL, "WndProc__return");
		final Label hWnd = label(GLOBAL, "WndProc__hWnd");
		final Label uMsg = label(GLOBAL, "WndProc__uMsg");
		final Label wParam = label(GLOBAL, "WndProc__wParam");
		final Label lParam = label(GLOBAL, "WndProc__lParam");
		final Label WM_SysCommand = label(LOCAL, "WM_SysCommand");
		final Label WM_Close = label(LOCAL, "WM_Close");
		final Label WM_Destroy = label(LOCAL, "WM_Destroy");
		final Label WM_Default = label(LOCAL, "default");
		final Label glString = label(GLOBAL, "glString");
		final Label msgTrace = label(GLOBAL, "__message_trace");
		final Label shutdown = label(GLOBAL, "Generic__shutdown");
		final Label label_dont_clear = label(LOCAL, "dont_clear");
		data(msgTrace, 8, QWORD);
		data(shutdown, DWORD);
		asm(
		def_label(main),
			block("INIT"),

			comment("verify the window is not open twice"),
			assign_call(mutex, CreateMutexA(
				0,
				true,
				// generic reusable uuid any time an api function wants a string identifier
				addrOf(data(uuid, BYTE, nullstr("07b62314-d4fc-4704-96e8-c31eb378d815"))))),

			comment(
				"get a pointer to this process for use with api functions which require it ",
				"Note that as of 32-bit Windows, an instance handle (HINSTANCE), such as the",
				"application instance handle exposed by system function call of WinMain, and",
				"a module handle (HMODULE) are the same thing."),
			assign_call(process, GetModuleHandleA(Null())),
	
			comment("load references to the default icons for new windows"),
			// TODO: could finish separating out bitFields cleanly throughout
			assign_call(icon, LoadImageA(Null(), oper(OIC_WINLOGO), IMAGE_ICON, 
				0, 0, LR_SHARED | LR_DEFAULTSIZE)),
	
			assign_call(cursor, LoadImageA(Null(), oper(IDC_ARROW), IMAGE_CURSOR,
				0, 0, LR_SHARED | LR_DEFAULTSIZE)),
	
			comment("begin creating the main local application window"),
			assign_call(atom, RegisterClassExA(
				addrOf(MainWindow = istruct("MainWindow", tagWNDCLASSEXA, new HashMap<String, Operand>(){{
					put("style", oper( CS_OWNDC | CS_VREDRAW | CS_HREDRAW)
						.comment("= CS_OWNDC | CS_VREDRAW | CS_HREDRAW"));
					put("hInstance", oper(addrOf(process)));
					// NOTICE: the name used there has to be the same as the one used for CreateWindow
					put("lpszClassName", oper(addrOf(uuid)));
					put("lpfnWndProc", oper(addrOf(WndProc)));
					put("hIcon", oper(addrOf(icon)));
					put("hCursor", oper(addrOf(cursor)));
				}})))),
	
			assign_call(window, CreateWindowExA(
				list(WS_EX_WINDOWEDGE),
				addrOf(uuid),
				addrOf(data(title, BYTE, nullstr("OpenGL Demo"))),
				list(WS_OVERLAPPED, WS_CAPTION, WS_SYSMENU, 
					WS_THICKFRAME, WS_MINIMIZEBOX, WS_MAXIMIZEBOX,
					WS_VISIBLE, WS_CLIPCHILDREN, WS_CLIPSIBLINGS),
				CW_USEDEFAULT,
				CW_USEDEFAULT,
				640,
				480,
				Null(),
				Null(),
				addrOf(process),
				Null()
			)),

			comment("begin creating the OpenGL context"),
			assign_call(ctx2d, GetDC(deref(window))),
	
			// TODO: if full screen: ChangeDisplaySettings, ShowCursor
			assign_call(pixelFormat, ChoosePixelFormat(
				deref(ctx2d),
				addrOf(PixelFormat = istruct("PixelFormat", PIXELFORMATDESCRIPTOR, new HashMap<String, Operand>(){{
					put("dwFlags", bitField(PFD_DRAW_TO_WINDOW, PFD_SUPPORT_OPENGL, PFD_DOUBLEBUFFER));
					put("iPixelType", bitField(PFD_TYPE_RGBA));
					// not sure i care to make this based on system capability
					put("cColorBits", oper(24).comment("color depth"));
					put("cAlphaBits", oper(0).comment("no alpha buffer")); 
					put("cAccumBits", oper(0).comment("no accumulation buffer"));
					put("cDepthBits", oper(32).comment("z-buffer"));
					put("cStencilBits", oper(0).comment("no stencil buffer"));
					put("cAuxBuffers", oper(0).comment("no auxiliary buffer"));
					put("iLayerType", bitField(PFD_MAIN_PLANE));
				}})))),
	
			assign_call(success, SetPixelFormat(
				deref(ctx2d),
				deref(pixelFormat),
				addrOf(PixelFormat)
			)),
	
			dllimport("opengl32"
				,"wglCreateContext"
				,"wglMakeCurrent"
				,"glClearColor"
				,"glClear"
				,"glGetError"
				,"glGetString"
				,"wglGetProcAddress"
				,"glEnable"
				,"glDepthFunc"
				,"glDrawElements"
//				,"glViewport"
//				,"glFlush"
			),
			
			assign_call(ctx, wglCreateContext(deref(ctx2d))),
	
			assign_call(success, wglMakeCurrent(deref(ctx2d), deref(ctx))),

			assign_call(glString, glGetString(GL_VENDOR)),
			Console.log( "GL_VENDOR: %1", glString),
			assign_call(glString, glGetString(GL_RENDERER)),
			Console.log( "GL_RENDERER: %1", glString),
			assign_call(glString, glGetString(GL_VERSION)),
			Console.log( "GL_VERSION: %1", glString),
			// TODO: could abort if api version compatibility is too low

			glimport(
				"glCreateShader"
				,"glShaderSource"
				,"glCompileShader"
				,"glGetShaderiv"
				,"glGetShaderInfoLog"
				,"glCreateProgram"
				,"glBindAttribLocation"
				,"glAttachShader"
				,"glLinkProgram"
				,"glGetProgramiv"
				,"glGetProgramInfoLog"
				,"glGetAttribLocation"
				,"glEnableVertexAttribArray"
				,"glUseProgram"
				,"glGenBuffers"
				,"glBindBuffer"
				,"glBufferData"
				,"glVertexAttribPointer"
			)
		);
		
		final Label vshader = newShader(VERTEX_SHADER_SRC, GL_VERTEX_SHADER);
		final Label fshader = newShader(FRAGMENT_SHADER_SRC, GL_FRAGMENT_SHADER);
		final Label handleProgramError = label(GLOBAL, "glGetProgramiv__handleError");
		final Label glProgramDone = label(GLOBAL, "glGetProgramiv__done");
		final int glProgram_BUFFER_SIZE = 256;
		final Label pbuffer = data(label(GLOBAL, "glGetProgramInfoLog_buffer"), glProgram_BUFFER_SIZE, BYTE);
		final Label pbufferlen = data(label(GLOBAL, "glGetProgramInfoLog_buffer_len"), DWORD);
		final Label program = label(GLOBAL, "glProgram__instance");
		final Label positionAttr = data(label(GLOBAL, "glProgram__attribute"), BYTE, nullstr("position"));
		final Label positionIdx = label(GLOBAL, "glProgram__attribute_idx");
		// TODO: for both strings and labels just make an anon label generator for convenience
		final Label plabel2 = label(LOCAL, "glProgram__label2");
		final Label triangleVerticesBuffer = data(label(GLOBAL, "glBuffers__triangleVerticesBuffer"), DWORD);
		final Label triangleFacesBuffer = data(label(GLOBAL, "glBuffers__triangleFacesBuffer"), DWORD);
		final Label verticesFloat32Array = data(label(GLOBAL, "glBuffers__verticesFloat32Array"), DWORD, 
			"-0.9, -0.9"+
			", 0.9, -0.9"+
			", 0.9, 0.9");
		final Label facesUint16Array = data(label(GLOBAL, "glBuffers__facesUint16Array"), WORD, 
			"0, 1, 2");
		asm(
			assign_call(program, glCreateProgram()),
			call(glBindAttribLocation(program, 0, positionAttr)),
			call(glAttachShader(program, vshader)),
			call(glAttachShader(program, fshader)),
			call(glLinkProgram(program)),
			
			call(glGetProgramiv(program, GL_LINK_STATUS, success)),
			jmp_if(DWORD, oper(deref(success)), NOT_EQUAL, bitField(GL_TRUE), handleProgramError),
			jmp(glProgramDone),

			def_label(handleProgramError),
			call(glGetProgramInfoLog(program, glProgram_BUFFER_SIZE, pbufferlen, pbuffer)),
			Console.log("GL Program Linker Error:"),
			assign_call(success, WriteToPipe.stdout_without_fail_check(pbuffer, pbufferlen)),
			exit(oper(1)),
			def_label(glProgramDone),
			
			assign_call(positionIdx, glGetAttribLocation(program, positionAttr)),
			jmp_if(DWORD, oper(deref(positionIdx)), NOT_EQUAL, oper(-1), plabel2),
			Console.log("Missing attribute: position"),
			exit(oper(1)),
			
			def_label(plabel2),
			call(glEnableVertexAttribArray(positionIdx)),
			call(glUseProgram(program)),
			
			// vertices
			call(glGenBuffers(1, triangleVerticesBuffer)),
			call(glBindBuffer(GL_ARRAY_BUFFER, triangleVerticesBuffer)),
			call(glBufferData(GL_ARRAY_BUFFER, 6*(32/8), verticesFloat32Array, GL_STATIC_DRAW)),
			
			// faces
			call(glGenBuffers(1, triangleFacesBuffer)),
			call(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, triangleFacesBuffer)),
			call(glBufferData(GL_ELEMENT_ARRAY_BUFFER, 3*(16/8), facesUint16Array, GL_STATIC_DRAW)),
			
			// describe how to walk the data type of the position vertex attribute 
			// as found in the triangle vertices buffer
			call(glVertexAttribPointer(positionIdx, 2, GL_FLOAT, 
				GL_FALSE, 2*(32/8), 0)),
			
//			call(glEnable(GL_DEPTH_TEST)), // only draw nearest pixels
//			call(glDepthFunc(GL_LESS)),
		
			call(glClearColor(0.1f, 0.1f, 0.1f, 1.0f)),
	
		def_label(Loop),
			assign_call(success, PeekMessageA(
				addrOf(IncomingMessage = istruct("IncomingMessage", tagMSG)),
				Null(), // process messages for all windows of this thread, we will only have one anyway
				0,
				0,
				PM_REMOVE
			)),
	
			comment("if zero messages, skip handling messages"),
			jmp_if(DWORD, oper(deref(success)), EQUAL, oper(0), Render),
			
			jmp_if(DWORD, oper(deref(IncomingMessage.get("message"))), NOT_EQUAL, bitField(WM_QUIT), ProcessMessage),
	
			exit(oper(0)),
	
		def_label(ProcessMessage),
//			mov(QWORD, oper(QWORD, A), oper(deref(IncomingMessage.get("hwnd")))),
//			mov(QWORD, oper(deref(msgTrace).offset(8*0)), oper(QWORD, A)),
//			mov(QWORD, oper(DWORD, A), oper(deref(IncomingMessage.get("message")))),
//			mov(DWORD, oper(deref(msgTrace).offset(8*1)), oper(DWORD, A)),
//			mov(QWORD, oper(QWORD, A), oper(deref(IncomingMessage.get("wParam")))),
//			mov(QWORD, oper(deref(msgTrace).offset(8*2)), oper(QWORD, A)),
//			mov(QWORD, oper(QWORD, A), oper(deref(IncomingMessage.get("lParam")))),
//			mov(QWORD, oper(deref(msgTrace).offset(8*3)), oper(QWORD, A)),
//			mov(DWORD, oper(DWORD, A), oper(deref(IncomingMessage.get("pt.x")))),
//			mov(DWORD, oper(deref(msgTrace).offset(8*4)), oper(DWORD, A)),
//			mov(DWORD, oper(DWORD, A), oper(deref(IncomingMessage.get("pt.y")))),
//			mov(DWORD, oper(deref(msgTrace).offset(8*5)), oper(DWORD, A)),
//			mov(DWORD, oper(DWORD, A), oper(deref(IncomingMessage.get("lPrivate")))),
//			mov(DWORD, oper(deref(msgTrace).offset(8*6)), oper(DWORD, A)),
//			trace("Message received:\n"+
//				"  hwnd: %1!.16llX!\n"+
//				"  message: %2!.4llX!\n"+
//				"  wParam: %3!.16llX!\n"+
//				"  lParam: %4!.16llX!\n"+
//				"  time: %5!.16llX!\n"+
//				"  pt.x: %6!lu!\n"+
//				"  pt.y: %7!lu!\n"+
//				"  lPrivate: %8!.8llX!\n", msgTrace),
			call(TranslateMessage(addrOf(IncomingMessage))),
			call(DispatchMessageA(addrOf(IncomingMessage))),

		def_label(Render),
			// when the app is shutting down, abort this loop
			jmp_if(DWORD, oper(deref(shutdown)), EQUAL, oper(true), Loop),
			
			call(glClear(list(GL_COLOR_BUFFER_BIT/*, GL_DEPTH_BUFFER_BIT*/))),

			call(glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_SHORT, 0)),
			
			assign_call(success, SwapBuffers(deref(ctx2d))),
			
			jmp(Loop),
	
		def_label(WndProc),
			comment("move local registers to local shadow space to preserve them"),
			assign_mov(QWORD, hWnd, oper(QWORD, C)),
			assign_mov(QWORD, uMsg, oper(QWORD, D)),
			assign_mov(QWORD, wParam, oper(QWORD, R8)),
			assign_mov(QWORD, lParam, oper(QWORD, R9)),
//			mov(QWORD, oper(QWORD, A), oper(deref(hWnd))),
//			mov(QWORD, oper(deref(msgTrace).offset(8*0)), oper(QWORD, A)),
//			mov(DWORD, oper(DWORD, A), oper(deref(uMsg))),
//			mov(DWORD, oper(deref(msgTrace).offset(/*8*1*/8)), oper(DWORD, A)),
//			mov(QWORD, oper(QWORD, A), oper(deref(wParam))),
//			mov(QWORD, oper(deref(msgTrace).offset(8*2)), oper(QWORD, A)),
//			mov(QWORD, oper(QWORD, A), oper(deref(lParam))),
//			mov(QWORD, oper(deref(msgTrace).offset(8*3)), oper(QWORD, A)),
//			trace(join(
//				"WndProc called:"
//				,"  hwnd: %1!.16llX!",
//				,"  message: %2!.4llX!"
//				,"  wParam: %3!.16llX!"
//				,"  lParam: %4!.16llX!"
//				), msgTrace),
//			mov(QWORD, oper(QWORD, D), oper(deref(uMsg))),
			
			comment("switch(uMsg) {"),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, bitField(WM_ACTIVATE), WndProc_return_zero),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, bitField(WM_SYSCOMMAND), WM_SysCommand),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, bitField(WM_CLOSE), WM_Close),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, bitField(WM_DESTROY), WM_Destroy),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, bitField(WM_NCDESTROY), WndProc_return_zero),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, bitField(WM_KEYDOWN), WndProc_return_zero),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, bitField(WM_KEYUP), WndProc_return_zero),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, bitField(WM_SIZE), WndProc_return_zero),
		def_label(WM_Default),
			comment("default window procedure handles messages for us"),
			assign_call(success, ignoreError(DefWindowProcA(
				deref(hWnd),
				deref(uMsg),
				deref(wParam),
				deref(lParam)
			))),
			// only clear the error if the app is shutting down
			jmp_if(DWORD, oper(deref(shutdown)), NOT_EQUAL, oper(true), label_dont_clear),
			// it doesn't appear to be our fault, but during shutdown this
			// winapi proc returns an error about invalid window handle.
			// that's because the window handle existed when it entered
			// but was destroyed during the middle of the procedure.
			// so for now we just ignore errors from this function since
			// there's not too much we could do if there were any here 
			// anyway.
			call(SetLastError(0)),
		def_label(label_dont_clear),
			ret(width(QWORD, oper(deref(success)))),

		def_label(WM_SysCommand),
			mov(DWORD, oper(DWORD, B), oper(deref(wParam))),
			jmp_if(DWORD, oper(DWORD, B), EQUAL, oper(SC_SCREENSAVE), WndProc_return_zero),
			jmp_if(DWORD, oper(DWORD, B), EQUAL, oper(SC_MONITORPOWER), WndProc_return_zero),
			jmp(WM_Default),
	
		def_label(WM_Close),
			// set global shutdown flag so all loops know to abort
			mov(DWORD, oper(deref(shutdown)), oper(true)),
			call(DestroyWindow(deref(window))),
			jmp(WndProc_return_zero),
	
		def_label(WM_Destroy),
			call(PostQuitMessage(0)),
			jmp(WndProc_return_zero),

		def_label(WndProc_return_zero),
			ret(width(QWORD, Null())),

			block("PROCS")
		);
	}
}
