package com.sdd.asm.demo.opengl;
import java.util.HashMap;

import static com.sdd.asm.Macros.*;
// interfaces
import static com.sdd.asm.lib.Kernel32.*;
import static com.sdd.asm.lib.User32.*;
import static com.sdd.asm.lib.Gdi32.*;
import static com.sdd.asm.lib.Opengl32.*;

public class Main
{
	public static void main(final String[] args) 
	{
		build();
		out();
	}
	
	private static void build()
	{
		final String MainWindow, PixelFormat, IncomingMessage; // struct instances
		asm(comment("GOAL: Render OpenGL spinning 3d cube animation", ""));
		init();

		section(".text");
		asm(
			def_label(Scope.GLOBAL, "main"),
			block("INIT"),

			comment("verify the window is not open twice"),
			assign_call(Scope.NORMAL, "CreateMutexA__handle",
				CreateMutexA(
					0,
					true,
					// generic reusable uuid any time an api function wants a string identifier
					addrOf(assign(Scope.NORMAL, "Generic__uuid", Size.BYTE,
						nullstr("07b62314-d4fc-4704-96e8-c31eb378d815"))))),

			comment(
				"get a pointer to this process for use with api functions which require it ",
				"Note that as of 32-bit Windows, an instance handle (HINSTANCE), such as the",
				"application instance handle exposed by system function call of WinMain, and",
				"a module handle (HMODULE) are the same thing."),
			assign_call(Scope.NORMAL, "GetModuleHandleA__hModule",
				GetModuleHandleA(null)),
	
			comment("load references to the default icons for new windows"),
			assign_call(Scope.NORMAL, "CreateWindow__icon",
				LoadImageA(null, OIC_WINLOGO, IMAGE_ICON, 0, 0, LR_SHARED | LR_DEFAULTSIZE)),
	
			assign_call(Scope.NORMAL, "CreateWindow__cursor",
				LoadImageA(null, IDC_ARROW, IMAGE_CURSOR, 0, 0, LR_SHARED | LR_DEFAULTSIZE)),
	
			comment("begin creating the main local application window"),
			assign_call(Scope.NORMAL, "CreateWindow__atom_name",
				// TODO: maybe use the stack for this
				// TODO: also, define these within the RegisterClassExA invocation,
				//       like a struct wth type completion ideally
				RegisterClassExA(MainWindow = istruct("MainWindow", tagWNDCLASSEXA, new HashMap<String, Operand>(){{
					put("style", operand(hex(CS_OWNDC | CS_VREDRAW | CS_HREDRAW), "= CS_OWNDC | CS_VREDRAW | CS_HREDRAW"));
					put("hInstance", operand("GetModuleHandleA__hModule"));
					// NOTICE: the name used there has to be the same as the one used for CreateWindow
					put("lpszClassName", operand("Generic__uuid"));
					put("lpfnWndProc", operand("WndProc"));
					put("hIcon", operand("CreateWindow__icon"));
					put("hCursor", operand("CreateWindow__cursor"));
				}}))),
	
			assign_call(Scope.NORMAL, "CreateWindow__hWnd",
				CreateWindowExA(
					WS_EX_OVERLAPPEDWINDOW,
					addrOf("Generic__uuid"),
					addrOf(assign(Scope.NORMAL, "CreateWindow__title", Size.BYTE, nullstr("OpenGL Demo"))),
					WS_OVERLAPPEDWINDOW | WS_VISIBLE | WS_CLIPCHILDREN | WS_CLIPSIBLINGS,
					CW_USEDEFAULT,
					CW_USEDEFAULT,
					640,
					480,
					null,
					null,
					addrOf("GetModuleHandleA__hModule"),
					null
				)),
	
				comment("begin creating the OpenGL context"),
				assign_call(Scope.NORMAL, "GetDC__hDC",
					GetDC(deref("CreateWindow__hWnd"))),
	
			// TODO: if full screen: ChangeDisplaySettings, ShowCursor
			assign_call(Scope.NORMAL, "ChoosePixelFormat__format",
				ChoosePixelFormat(
					deref("GetDC__hDC"),
					PixelFormat = istruct("PixelFormat", PIXELFORMATDESCRIPTOR, new HashMap<String, Operand>(){{
						put("dwFlags", operand(hex(PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER), "= PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER"));
						put("iPixelType", operand(PFD_TYPE_RGBA, "= PFD_TYPE_RGBA"));
						// not sure i care to make this based on system capability
						put("cColorBits", operand(24, "(24-bit color depth)"));
						put("cAlphaBits", operand(0, "(no alpha buffer)")); 
						put("cAccumBits", operand(0, "(no accumulation buffer)"));
						put("cDepthBits", operand(32, "(32-bit z-buffer)"));
						put("cStencilBits", operand(0, "(no stencil buffer)"));
						put("cAuxBuffers", operand(0, "(no auxiliary buffer)"));
						put("iLayerType", operand(PFD_MAIN_PLANE, "= PFD_MAIN_PLANE"));
					}}))),
	
			assign_call(Scope.NORMAL, "SetPixelFormat__success",
				SetPixelFormat(
					deref("GetDC__hDC"),
					deref("ChoosePixelFormat__format"),
					PixelFormat
				)),
	
			dllimport("opengl32",
				"wglCreateContext",
				"wglMakeCurrent",
				"glClearColor",
				"glClear",
				"glGetError"
			),
	
			assign_call(Scope.NORMAL, "wglCreateContext__ctx",
				wglCreateContext(deref("GetDC__hDC"))),
	
			assign_call(Scope.NORMAL, "wglMakeCurrent__success",
				wglMakeCurrent(deref("GetDC__hDC"), deref("wglCreateContext__ctx"))),
	
			call(
				glClearColor(0, 0, 1, 1)),
	
			def_label(Scope.NORMAL, "Loop"),
	
			assign_call(Scope.NORMAL, "PeekMessage_hasMsgs",
				PeekMessageA(
					addrOf(IncomingMessage = istruct("IncomingMessage", tagMSG)),
					deref("CreateWindow__hWnd"),
					0,
					0,
					PM_REMOVE
				)),
	
			comment("if zero messages, skip handling messages"),
			jmp_if(Size.DWORD, deref("PeekMessage_hasMsgs"), Comparison.EQUAL, "0",
				"..@Render"),
	
			comment("", "exit if message is WM_QUIT"),
			jmp_if(Size.DWORD, deref(IncomingMessage +".message"), Comparison.NOT_EQUAL, hex(WM_QUIT),
				"..@Loop__processMessage"),
	
			exit(0),
	
			def_label(Scope.LOCAL, "Loop__processMessage"),
	
			call(TranslateMessage(addrOf(IncomingMessage))),
			call(DispatchMessageA(addrOf(IncomingMessage))),
	
			def_label(Scope.LOCAL, "Render"),
	
			call(glClear(GL_COLOR_BUFFER_BIT)),
	
			assign_call(Scope.NORMAL, "SwapBuffers__success",
				SwapBuffers(deref("GetDC__hDC"))),
	
			jmp("Loop"),
	
			def_label(Scope.NORMAL, "WndProc"),
			comment("move local registers to local shadow space to preserve them"),
			assign_mov(Scope.NORMAL, "WndProc__hWnd",   Size.QWORD, "rcx"),
			assign_mov(Scope.NORMAL, "WndProc__uMsg",   Size.QWORD, "rdx"),
			assign_mov(Scope.NORMAL, "WndProc__wParam", Size.QWORD, "r8"),
			assign_mov(Scope.NORMAL, "WndProc__lParam", Size.QWORD, "r9"),
	
			comment("switch(uMsg) {"),
			jmp_if(Size.QWORD, "rdx", Comparison.EQUAL, hex(WM_ACTIVATE), label(Scope.LOCAL, "WndProc__WM_Activate")),
			jmp_if(Size.QWORD, "rdx", Comparison.EQUAL, hex(WM_SYSCOMMAND), label(Scope.LOCAL, "WndProc__WM_SysCommand")),
			jmp_if(Size.QWORD, "rdx", Comparison.EQUAL, hex(WM_CLOSE), label(Scope.LOCAL, "WndProc__WM_Close")),
			jmp_if(Size.QWORD, "rdx", Comparison.EQUAL, hex(WM_DESTROY), label(Scope.LOCAL, "WndProc__WM_Destroy")),
			jmp_if(Size.QWORD, "rdx", Comparison.EQUAL, hex(WM_KEYDOWN), label(Scope.LOCAL, "WndProc__WM_KeyDown")),
			jmp_if(Size.QWORD, "rdx", Comparison.EQUAL, hex(WM_KEYUP), label(Scope.LOCAL, "WndProc__WM_KeyUp")),
			jmp_if(Size.QWORD, "rdx", Comparison.EQUAL, hex(WM_SIZE), label(Scope.LOCAL, "WndProc__WM_Size")),
			def_label(Scope.LOCAL, "WndProc__default"),
			comment("default window procedure handles messages for us"),
			assign_call(Scope.NORMAL, "WndProc__return",
				DefWindowProcA(
					deref("WndProc__hWnd"),
					deref("WndProc__uMsg"),
					deref("WndProc__wParam"),
					deref("WndProc__lParam")
				)),
			ret(new ValueSizeComment(deref("WndProc__return"), Size.QWORD, "")),

			def_label(Scope.LOCAL, "WndProc__WM_Activate"),
			ret(null),

			def_label(Scope.LOCAL, "WndProc__WM_SysCommand"),
			mov(Size.DWORD, "ebx", deref("WndProc__wParam"), ""),
			jmp_if(Size.DWORD, "ebx", Comparison.EQUAL, hex(SC_SCREENSAVE), label(Scope.LOCAL, "return_zero")),
			jmp_if(Size.DWORD, "ebx", Comparison.EQUAL, hex(SC_MONITORPOWER), label(Scope.LOCAL, "return_zero")),
			jmp(label(Scope.LOCAL, "WndProc__default")),
			def_label(Scope.LOCAL, "return_zero"),
			ret(null),
	
			def_label(Scope.LOCAL, "WndProc__WM_Close"),
			call(DestroyWindow(deref("CreateWindow__hWnd"))),
			ret(null),
	
			def_label(Scope.LOCAL, "WndProc__WM_Destroy"),
			call(PostQuitMessage(0)),
			ret(null),
	
			def_label(Scope.LOCAL, "WndProc__WM_KeyDown"),
			ret(null),
	
			def_label(Scope.LOCAL, "WndProc__WM_KeyUp"),
			ret(null),

			def_label(Scope.LOCAL, "WndProc__WM_Size"),
			ret(null),

			block("PROCS")
		);

	}
}
