package com.sdd.asm.demo.opengl;

import java.util.HashMap;

import static com.sdd.asm.Macros.*;
import static com.sdd.asm.Macros.Scope.*;
import static com.sdd.asm.Macros.Size.*;
import static com.sdd.asm.Macros.Register.Name.*;
import static com.sdd.asm.Macros.Compare.*;
// interfaces
import static com.sdd.asm.lib.Kernel32.*;
import static com.sdd.asm.lib.User32.*;
import static com.sdd.asm.lib.Gdi32.*;
import static com.sdd.asm.lib.Opengl32.*;
import static com.sdd.asm.lib.Opengl32.GlParam.*;
import static com.sdd.asm.lib.Opengl32.GlShaderType.*;

public class Main
{
	public static void main(final String[] args) 
	{
		build();
		writeToDisk();
	}
	
	private static String VERTEX_SHADER_SRC = 
		"attribute vec2 position;\n" + // the position of the point
		"void main(void) {\n" + // pre-built function
		"  gl_Position = vec4(position, 0., 1.);\n" + // 0. is the z, and 1 is w
		"}";
	
	private static String FRAGMENT_SHADER_SRC =
		"precision mediump float;\n" +
		"void main(void) {\n" +
		"  gl_FragColor = vec4(0.,0.,0., 1.);\n" + // black color
		"}";
	
	private static Label newShader(
		final String src,
		final GlShaderType type
	) {
		final Label shader = label(GLOBAL, "glCreateShader__shader");
		final Label sources = label(GLOBAL, "glShaderSource__sources");
		final Label lengths = label(GLOBAL, "glShaderSource__lengths");
		final Label error = label(GLOBAL, "glCompileShader__error");
		final Label errorLen = label(GLOBAL, "glCompileShader__errorLen");
		final Label handleError = label(LOCAL, "newShader__handleError");
		final Label done = label(LOCAL, "newShader__done");
		data(sources, BYTE, nullstr(src));
		data(lengths, DWORD, Integer.toString(src.length()));
		asm( 
			assign_call(shader, glCreateShader(type)),
			call(glShaderSource(shader, 1, sources, lengths)),
			call(glCompileShader(shader)),
			assign_mov(DWORD, error, oper(0)), // reset error to zero
			call(glGetShaderiv(shader, GL_COMPILE_STATUS, error)),
			jmp_if(DWORD, oper(deref(error)), EQUAL, oper(0), handleError),
			jmp(done),
			
			def_label(handleError),
			assign_call(errorLen, LocalSize(error)),
			call(Console.log(error, errorLen)),
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
		data(success, QWORD); // make room for largest possibility
		final Label ctx = label(GLOBAL, "wglCreateContext__ctx");
		final Label Loop = label(GLOBAL, "Loop");
		final Label Render = label(LOCAL, "Render");
		final Label ProcessMessage = label(LOCAL, "Loop__processMessage");
		final Label WndProc = label(GLOBAL, "WndProc");
		final Label hWnd = label(GLOBAL, "WndProc__hWnd");
		final Label uMsg = label(GLOBAL, "WndProc__uMsg");
		final Label wParam = label(GLOBAL, "WndProc__wParam");
		final Label lParam = label(GLOBAL, "WndProc__lParam");
		final Label WM_Activate = label(LOCAL, "WM_Activate");
		final Label WM_SysCommand = label(LOCAL, "WM_SysCommand");
		final Label WM_Close = label(LOCAL, "WM_Close");
		final Label WM_Destroy = label(LOCAL, "WM_Destroy");
		final Label WM_KeyDown = label(LOCAL, "WM_KeyDown");
		final Label WM_KeyUp = label(LOCAL, "WM_KeyUp");
		final Label WM_Size = label(LOCAL, "WM_Size");
		final Label WM_Default = label(LOCAL, "default");
		final Label WM_Zero = label(LOCAL, "return_zero");
		final Label glString = label(GLOBAL, "glString");
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
				WS_EX_WINDOWEDGE,
				addrOf(uuid),
				addrOf(data(title, BYTE, nullstr("OpenGL Demo"))),
				WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | 
					WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX |
					WS_VISIBLE | WS_CLIPCHILDREN | WS_CLIPSIBLINGS,
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
					put("dwFlags", oper(PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER)
						.comment("= PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER"));
					put("iPixelType", oper(PFD_TYPE_RGBA).comment("= PFD_TYPE_RGBA"));
					// not sure i care to make this based on system capability
					put("cColorBits", oper(24).comment("(24-bit color depth)"));
					put("cAlphaBits", oper(0).comment("(no alpha buffer)")); 
					put("cAccumBits", oper(0).comment("(no accumulation buffer)"));
					put("cDepthBits", oper(32).comment("(32-bit z-buffer)"));
					put("cStencilBits", oper(0).comment("(no stencil buffer)"));
					put("cAuxBuffers", oper(0).comment("(no auxiliary buffer)"));
					put("iLayerType", oper(PFD_MAIN_PLANE).comment("= PFD_MAIN_PLANE"));
				}})))),
	
			assign_call(success,
				SetPixelFormat(
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
//				,"glCreateShader"
//				,"glShaderSource"
//				,"glCompileShader"
//				,"glGetShaderiv"
			),
			
			assign_call(ctx, wglCreateContext(deref(ctx2d))),
	
			assign_call(success, wglMakeCurrent(deref(ctx2d), deref(ctx))),
	
			assign_call(glString, glGetString(oper(GL_VERSION).comment("GL_VERSION"))),
			printf(FormatString( "GL_VERSION: %1\n", glString), Console::log),
//			exit(oper(0)),
			
			call(glClearColor(0, 0, 1, 1)),
	
			def_label(Loop),
	
			assign_call(success, PeekMessageA(
				addrOf(IncomingMessage = istruct("IncomingMessage", tagMSG)),
				deref(window),
				0,
				0,
				PM_REMOVE
			)),
	
			comment("if zero messages, skip handling messages"),
			// TODO: make class ImmediateOrLabelReference which encapsulates all possible int, float, etc. inputs and converting them to strings
			jmp_if(DWORD, oper(deref(success)), EQUAL, oper(0), Render),
	
			comment("", "exit if message is WM_QUIT"),
			// TODO: teach istruct to make label members we can .get("field") which return LabelReference
			jmp_if(DWORD, oper(deref(IncomingMessage.get("message"))), NOT_EQUAL, oper(WM_QUIT), ProcessMessage),
	
			exit(oper(0)),
	
			def_label(ProcessMessage),
	
			call(TranslateMessage(addrOf(IncomingMessage))),
			call(DispatchMessageA(addrOf(IncomingMessage))),
	
			def_label(Render),
	
			call(glClear(GL_COLOR_BUFFER_BIT)),
	
			assign_call(success, SwapBuffers(deref(ctx2d))),
	
			jmp(Loop),
	
			def_label(WndProc),
			comment("move local registers to local shadow space to preserve them"),
			assign_mov(QWORD, hWnd, oper(QWORD, C)),
			assign_mov(QWORD, uMsg, oper(QWORD, D)),
			assign_mov(QWORD, wParam, oper(QWORD, R8)),
			assign_mov(QWORD, lParam, oper(QWORD, R9)),
	
			comment("switch(uMsg) {"),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, oper(WM_ACTIVATE), WM_Activate),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, oper(WM_SYSCOMMAND), WM_SysCommand),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, oper(WM_CLOSE), WM_Close),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, oper(WM_DESTROY), WM_Destroy),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, oper(WM_KEYDOWN), WM_KeyDown),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, oper(WM_KEYUP), WM_KeyUp),
			jmp_if(QWORD, oper(QWORD, D), EQUAL, oper(WM_SIZE), WM_Size),
			def_label(WM_Default),
			comment("default window procedure handles messages for us"),
			assign_call(success, DefWindowProcA(
				deref(hWnd),
				deref(uMsg),
				deref(wParam),
				deref(lParam)
			)),
			ret(width(QWORD, oper(deref(success)))),

			def_label(WM_Activate),
			ret(width(QWORD, Null())),

			def_label(WM_SysCommand),
			// TODO: finish consolidating mov, LabelReference, and bi-directional features
			mov(DWORD, oper(DWORD, B), oper(deref(wParam))),
			jmp_if(DWORD, oper(DWORD, B), EQUAL, oper(SC_SCREENSAVE), WM_Zero),
			jmp_if(DWORD, oper(DWORD, B), EQUAL, oper(SC_MONITORPOWER), WM_Zero),
			jmp(WM_Default),
			def_label(WM_Zero),
			ret(width(QWORD, Null())),
	
			def_label(WM_Close),
			call(DestroyWindow(deref(window))),
			ret(width(QWORD, Null())),
	
			def_label(WM_Destroy),
			call(PostQuitMessage(0)),
			ret(width(QWORD, Null())),
	
			def_label(WM_KeyDown),
			ret(width(QWORD, Null())),
	
			def_label(WM_KeyUp),
			ret(width(QWORD, Null())),

			def_label(WM_Size),
			ret(width(QWORD, Null())),

			block("PROCS")
		);
	}
}
