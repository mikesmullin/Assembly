package com.sdd.asm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sdd.asm.util.Utils.*;
import static com.sdd.asm.lib.Kernel32.*;
import static com.sdd.asm.lib.Kernel32.FormatMessageFlags.*;
import static com.sdd.asm.lib.Opengl32.*;
import static com.sdd.asm.Macros.Scope.*;
import static com.sdd.asm.Macros.Size.*;
import static com.sdd.asm.Macros.Register.Name.*;
import static com.sdd.asm.Macros.Compare.*;

/**
 * Preprocessor.
 */
public class Macros
{
	/**
	 * Like `throw` keyword, causes compiler to print an error and abort.
	 */
	public static void _assert(final String reason)
	{
		System.err.println(reason);
		System.exit(1);
	}

	/**
	 * Directory where .nasm file will be output.
	 */
	public static String BUILD_PATH = "build/";

	/**
	 * Name of file which will hold the assembly instructions.
	 */
	public static String OUT_FILE;

	/**
	 * A list of sections, each holding compiled string instructions.
	 */
	private static HashMap<String,StringBuilder> sections = new HashMap<>();
	static
	{
		sections.put("proprocessor", new StringBuilder());
		sections.put(".data", new StringBuilder());
		sections.put(".text", new StringBuilder());
	}

	/**
	 * The current section, which `asm()` will write to.
	 */
	private static String section = "preprocessor";

	/**
	 * Change the current section.
	 */
	public static void section(final String name)
	{
		section = name;
	}

	/**
	 * Append string instruction to current `section`.
	 */
	public static void asm(final String... s)
	{
		StringBuilder sb = sections.get(section);
		if (null == sb)
		{
			sb = new StringBuilder();
			sections.put(section, sb);
		}
		sb.append(join(s));
	}

	/**
	 * Join every input string with a newline at the end.
	 */
	public static String join(final String... lines)
	{
		final StringBuilder out = new StringBuilder();
		for (final String line : lines)
		{
			out.append(line).append("\n");
		}
		return out.toString();
	}

	/**
	 * Some sections should appear in a particular order.
	 */
	private static final ArrayList<String> sectionOrder = new ArrayList<>();
	static {
		sectionOrder.add("preprocessor");
		sectionOrder.add(".data");
		sectionOrder.add(".text");
	}

	/**
	 * Write all sections to file on disk.
	 */
	public static void writeToDisk()
	{
		if (null == OUT_FILE) OUT_FILE = "test.nasm";
		try
		{
			final BufferedWriter writer = new BufferedWriter(
				new FileWriter(BUILD_PATH + OUT_FILE));

			for (final String name : sectionOrder)
			{
				final StringBuilder text = sections.get(name);
				if (null == text) continue;
				if (!"preprocessor".equals(name))
				{
					writer.append("section " + name + " align=16\n");
				}
				writer.append(text).append("\n");
			}
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public enum Scope
	{
		/**
		 * NASM global
		 * causes the named label to be exported to the symbol table
		 */
		EXPORT,
		/**
		 * NASM [non-local] label
		 * a normal label that can be referenced from anywhere
		 */
		GLOBAL,
		/**
		 * NASM local label
		 * a label that can be referenced relative to a non-local label
		 * (ie. ".field" referenced as "Struct.field")
		 */
		RELATIVE,
		/**
		 * NASM special prefix label
		 */
		LOCAL,
		/**
		 * NASM special label
		 * (ie. `..start` defines entry point)
		 */
		SPECIAL
	}

	private static HashSet<String> definedLabels = new HashSet<>();
	private static HashMap<String,AtomicInteger> labelInstanceCounter = new HashMap<>();
	
	public static class Label
	{
		public Scope scope;
		public String name;

		// for internal use only
		Label() {}

		/**
		 * Make a new temporary label for use with struct members. 
		 */
		public Label get(final String field) {
			final Label label = new Label();
			label.scope = this.scope;
			label.name = name +"."+ field;
			return label;
		}

		/**
		 * A label reference in NASM syntax.
		 * see: https://www.nasm.us/doc/nasmdoc3.html
		 */
		public String toString()
		{
			switch (scope)
			{
				case EXPORT:
					return name;
				case GLOBAL:
					return name;
				case RELATIVE:
					return "." + name;
				case LOCAL:
					return "..@" + name;
				case SPECIAL:
					return ".." + name;
				default:
					_assert("Invalid scope");
					return "";
			}
		}
	}

	public static Label label(final Scope scope, final String name)
	{
		final Label r = new Label();
		r.scope = scope;
		if (null == labelInstanceCounter.get(name))
		{
			labelInstanceCounter.put(name, new AtomicInteger());
		}
		final int id = labelInstanceCounter.get(name).getAndIncrement();
		r.name = id > 0 ? name +"_"+ id : name;
		return r;
	}

	public static Label label(final Scope scope, final String name, final boolean singleton)
	{
		final Label r = new Label();
		r.scope = scope;
		if (null == labelInstanceCounter.get(name))
		{
			labelInstanceCounter.put(name, new AtomicInteger());
		}
		final int id = labelInstanceCounter.get(name).get();
		r.name = id > 0 ? name +"_"+ id : name;
		return r;
	}
	
	/**
	 * A label definition in NASM syntax.
	 */
	public static String def_label(final Label label)
	{
		String out = "";
		if (EXPORT == label.scope)
		{
			out += "global " + label + "\n";

		}
		out += label + ":";
		return out;
	}

	private enum LabelReferenceType
	{
		ADDRESS_OF,
		DEREFERENCE
	}

	public static class LabelReference
	{
		public final Label label;
		public final LabelReferenceType type;
		public int offset = 0;
		private LabelReference(final Label label, final LabelReferenceType type)
		{
			this.label = label;
			this.type = type;
		}

		public String toString()
		{
			switch(type)
			{
				case ADDRESS_OF:
					return label.toString();
				case DEREFERENCE:
					return "[" + label + (offset > 0 ? " + "+ offset : offset < 0 ? " - "+ offset : "") +"]";
				default:
					_assert("Invalid type!");
					return "";
			}
		}
		
		public LabelReference offset(final int offset)
		{
			this.offset = offset;
			return this;
		}
	}
	
	public static class Register
	{
		public enum Name
		{
			A, B, C, D, SP, BP, SI, DI,
			R8, R9, R10, R11, R12, R13, R14, R15,
			XMM0, XMM1, XMM2, XMM3;
			
			public String toString()
			{
				return this.name().toLowerCase();
			}
		}
		
		public final Size size;
		public final Name name;
		
		public Register(final Register.Name name, final Size size)
		{
			this.size = size;
			this.name = name;
		}
		
		public String toString()
		{
			if (Name.A == name || Name.B == name || Name.C == name || Name.D == name) {
				switch (size) {
					case BYTE: return name+"l";
					case WORD: return name+"x";
					case DWORD: return "e"+name+"x";
					case QWORD: return "r"+name+"x";
				}
			}
			else if (Name.SP == name || Name.BP == name || Name.SI == name || Name.DI == name) {
				switch (size) {
					case BYTE: return name+"l";
					case WORD: return name+"";
					case DWORD: return "e"+name;
					case QWORD: return "r"+name;
				}
			}
			else if (Name.R8 == name || Name.R9 == name || Name.R10 == name || Name.R11 == name ||
					Name.R12 == name || Name.R13 == name || Name.R14 == name || Name.R15 == name) {
				switch (size) {
					case BYTE: return name+"b";
					case WORD: return name+"w";
					case DWORD: return name+"d";
					case QWORD: return name+"";
				}
			}
			else if (Name.XMM0 == name || Name.XMM1 == name || Name.XMM2 == name || Name.XMM3 == name) {
				switch (size) {
					case QWORD: return name+"l";
					case DQWORD: return name+"";
				}
			}
			_assert("Invalid register!");
			return "";
		}
	}
	
	/**
	 * An operand;
	 * either a register, immediate, or memory reference.
	 */
	public static class Operand
	{
		public String value;
		public Register reg;
		public LabelReference rm = null;
		public String comment = "";
		public boolean isFloat = false;

		public Operand setValue(final Operand value)
		{
			this.value = value.value;
			return this;
		}
		
		public Operand comment(final String comment)
		{
			this.comment = joinUnlessEmpty(this.comment, " ", comment);
			return this;
		}
		
		public String toString()
		{
			return this.value;
		}
		
		public Operand clone()
		{
			final Operand r = new Operand();
			r.value = this.value;
			r.reg = this.reg;
			r.rm = this.rm;
			r.comment = this.comment;
			r.isFloat = this.isFloat;
			return r;
		}
	}

	public static Operand Null()
	{
		final Operand r = new Operand();
		r.value = "0";
		r.comment = "= NULL";
		return r;
	}
	
	public static Operand placeholder(final String comment)
	{
		final Operand r = new Operand();
		r.comment = comment;
		return r;
	}

	public static Operand oper(final Size size, final Register.Name name)
	{
		final Operand r = new Operand();
		r.reg = new Register(name, size);
		r.value = r.reg.toString();
		return r;
	}
		
	public static Operand oper(final LabelReference value)
	{
		final Operand r = new Operand();
		if (null == value)
		{
			return Null();
		}
		r.rm = value;
		r.value = value.toString();
		return r;
	}
	
	public static Operand oper(final boolean value)
	{
		final Operand r = new Operand();
		r.value = value ? "1" : "0";
		r.comment = value ? "= TRUE" : "= FALSE";
		return r;
	}

	public static Operand oper(final int value)
	{
		final Operand r = new Operand();
		r.value = 0 == value ? "0" : hex(value);
		r.comment = 0 == value ? "" : "= "+ value;
		return r;
	}

	public static Operand oper(final float value)
	{
		final Operand r = new Operand();
		r.rm = deref(staticFloatToMemory(value));
		r.value = r.rm.toString();
		r.isFloat = true;
		return r;
	}
	
	/**
	 * The address of a given label in NASM syntax.
	 * A pointer.
	 */
	public static LabelReference addrOf(final Label label)
	{
		if (null == label) return null;
		return new LabelReference(label, LabelReferenceType.ADDRESS_OF);
	}

	/**
	 * The contents of a given label in NASM syntax.
	 * A dereferenced pointer.
	 */
	public static LabelReference deref(final Label label)
	{
		if (null == label) return null; 
		return new LabelReference(label, LabelReferenceType.DEREFERENCE);
	}

	/**
	 * Defines a .data variable which can be read and/or written to later,
	 * and returns a label to be referenced repeatedly throughout the document.
	 */
	public static Label data(
		final Label label,
		final int times,
		final Size size,
		final String val,
		final String comment
	) {
		// silently avoid re-defining
		if (!definedLabels.contains(label.toString())) {
			definedLabels.add(label.toString());
			sections.get(".data")
				.append(label)
				.append(": ")
				.append(times > 1 ? "times " + times + " " : "")
				.append(size.d)
				.append(" ")
				.append(val)
				.append((null == comment || "".equals(comment)) ? "" : (" ; " + comment.trim()))
				.append("\n");
		}
		return label;
	}

	/**
	 * Append a variable reference to the .data section.
	 */
	public static Label data(
		final Label label,
		final Size size,
		final String val,
		final String comment
	) {
		data(label, 1, size, val, comment);
		return label;
	}

	public static Label data(final Label label, final Size size, final String val) {
		return data(label, 1, size, val, null);
	}

	public static Label data(final Label label, final int times, final Size size)
	{
		return data(label, times, size, "0", null);
	}

	public static Label data(final Label label, final Size size)
	{
		return data(label, 1, size, "0", null);
	}

	/**
	 * A default set of block which can be used by the document.
	 */
	public static HashMap<String,StringBuilder> blocks = new HashMap<>();
	static {
		// things which occur at the beginning of the document
		blocks.put("INIT", new StringBuilder());
		// things which occur at the end of the document
		blocks.put("PROCS", new StringBuilder());
	}

	/**
	 * A collection of arbitrarily named sections of the document. 
	 * Like the block keyword from Jade/Pug templates.
	 */
	public static String block(final String name)
	{
		return blocks.get(name).toString();
	}
	
	private static ArrayList<Runnable> initializers = new ArrayList<>();

	/**
	 * Delays execution until after all macros and their data have been established.
	 */
	public static void onready(final Runnable cb)
	{
		initializers.add(cb);
	}
	
	public static void init()
	{
		for (final Runnable cb : initializers)
		{
			cb.run();
		}
	}

	public static class Struct {
		public final String name;
		public Struct(final String name)
		{
			this.name = name;
		}
		
		public LinkedHashMap<String, CustomType> fields = new LinkedHashMap<>();

		public Struct put(
			final String key,
			final CustomType type,
			final Operand defaultValue
		) {
			fields.put(key, type.clone().defaultValue(defaultValue));
			return this;
		}
		
		public Struct put(
			final String key,
			final Size size,
			final Operand defaultValue
		) {
			fields.put(key, new CustomType(size, "")
				.defaultValue(defaultValue));
			return this;
		}

		public Struct put(
			final String key, 
			final CustomType type
		) {
			fields.put(key, type.clone());
			return this;
		}
	}
	
	/**
	 * Instantiate a copy of given structure. 
	 */
	public static Label istruct(
		final String name,
		final Struct struct,
		final HashMap<String, Operand> values
	) {
		final Label label = label(GLOBAL, name);
		sections.get(".data")
			.append("\n; struct\n")
			.append(label).append(": ; instanceof ").append(struct.name).append("\n");
		for (final String k : struct.fields.keySet())
		{
			final Operand given = values.get(k);
			final Operand _default = struct.fields.get(k).op; 
			final Operand value;
			if (null == given)
			{
				value = _default;
			}
			else
			{
				value = given;
				given.comment(_default.comment);
			}
			
			if (null == value)
			{
				_assert("struct " + struct.name + " " + label + "." + k + " is missing a required op.");
				return null;
			}
			data(
				label(GLOBAL, label +"."+ k),
				struct.fields.get(k).size,
				value.toString(),
				/*struct.fields.get(k).size.name() +" "+*/
				value.comment);
		}
		sections.get(".data").append("\n");
		return label;
	}
	
	public static Label istruct(
		final String name,
		final Struct struct
	) {
		return istruct(name, struct, new HashMap<>());
	}

	/**
	 * Count the width of a struct in bytes.
	 */
	public static int sizeof(final Struct struct)
	{
		int size = 0;
		for (final String key : struct.fields.keySet())
		{
			final CustomType type = struct.fields.get(key);
			size += type.size.bytes;
		}
		
		return size;
	}

	/**
	 * Convert unsigned integer to hexadecimal string.
	 */
	public static String hex(final int n)
	{
		return "0x"+ Integer.toHexString(n);
	}

	/**
	 * Convert decimal to English like 1 -> "1st" 
	 */
	public static String englishOrdinal(final int i)
	{
		final String s = Integer.toString(i);
		final int n = Integer.parseInt(s.substring(s.length() - 1), 10);
		return s + (1 == n && i != 11 ? "st" : 2 == n && i != 12 ? "nd" : 3 == n && i != 13 ? "rd" : "th");
	}

	/**
	 * Windows 64-bit uses Microsoft fastcall
	 * First four params are passed via registers (RCX, RDX, R8, R9), and the remainder on stack (before shadow space)
	 * Caller also responsible to pad 'shadow space' of 4 x 64-bit registers (32 bytes) prior to the call,
	 * and to unwind the shadow space after the call has returned.
	 */
	public static String __ms_fastcall_64(final Proc procOpts)
	{
		String out = join(
		"    ; MS __fastcall x64 ABI");
			
		// allocate ms fastcall shadow space
		int shadowSpace = Math.max(40, ( 
				procOpts.args.size() + // number of args
				1 // mystery padding; its what VC++ x64 does
			) * 8 // bytes
		);
		out += join(
			"    sub rsp, "+ shadowSpace +" ; allocate shadow space");
		
		for (int i=procOpts.args.size()-1; i>-1; i--)
		{
			final SizedOp arg = procOpts.args.get(i);
			final String pos = englishOrdinal(i+1) + " ";
			final String register;
			if (arg.op.isFloat)
			{
				if      (0 == i && QWORD == arg.size) register = "xmm0";
				else if (1 == i && QWORD == arg.size) register = "xmm1";
				else if (2 == i && QWORD == arg.size) register = "xmm2";
				else if (3 == i && QWORD == arg.size) register = "xmm3";
				else {
					_assert("more than 4 float args not supported right now");
					return "";
				}
				out += join(
					"    mov qword rax, "+ arg.op,
					"    movq "+ register +", rax ; "+ pos + arg.op.comment);
			}
			else // integer operands
			{
				if      (0 == i && DWORD == arg.size) register = "ecx";
				else if (1 == i && DWORD == arg.size) register = "edx";
				else if (2 == i && DWORD == arg.size) register = "r8d";
				else if (3 == i && DWORD == arg.size) register = "r9d";
				else if (0 == i && QWORD == arg.size) register = "rcx";
				else if (1 == i && QWORD == arg.size) register = "rdx";
				else if (2 == i && QWORD == arg.size) register = "r8";
				else if (3 == i && QWORD == arg.size) register = "r9";
				else {
					out += join(
						"    mov "+ arg.size.toString().toLowerCase() +
							" [rsp + "+ (i * 8) +"], "+ arg.op +
							" ; "+ pos + arg.op.comment);
					continue;
				}
				out += join(
					"    mov "+ arg.size.toString().toLowerCase() +
					" "+ register +", "+ arg.op +" ; "+ pos + arg.op.comment);
			}
		}
		
		out += join(
			"call "+ procOpts.proc);
		
		// handle return var, if provided
		if (null != procOpts.ret) {
			final String register;
			if (DWORD == procOpts.ret.size) register = "eax";
			else if (QWORD == procOpts.ret.size) register = "rax";
			else {
				_assert("unsupported return type width");
				return "";
			}
			out += join(
				"    mov "+ procOpts.ret.size.toString().toLowerCase() +" "+
				procOpts.ret.op + ", " + register + " ; return "+ 
				procOpts.ret.op.comment);
		}
	
		// deallocate ms fastcall shadow space
		out += join(
			"    add rsp, "+ shadowSpace +" ; deallocate shadow space");
		return out;
	}

	private static final Label error_prologue =
		label(GLOBAL, "GetLastError__prologue_reset");
	private static final Label error_epilogue =
		label(GLOBAL, "GetLastError__epilogue_check");
	private static final Label error_lookup =
		label(GLOBAL, "GetLastError__epilogue_lookup");
	private static final Label error_code =
		label(GLOBAL, "GetLastError__errCode");
	private static final Label handle_error =
		label(LOCAL, "error");
	static {
		onready(()->{
			blocks.get("PROCS").append(join(
				// ensure last error is 0
				// invoked before calling a function which may or may not have lasterror support
				// makes us more confident calling GetLastError after a procedure runs to ensure 
				// it was ok (if everything is still 0)
				def_label(error_prologue),
				call(SetLastError(0)),
				"ret\n",
		
				def_label(error_epilogue),
				// last command must have returned 0x0 in RAX, indicating an error occurred
				jmp_if(DWORD, oper(DWORD, A), EQUAL, oper(0), error_lookup),
				"ret\n",
				def_label(error_lookup),
				assign_call(error_code, GetLastError()),
				jmp_if(DWORD, oper(DWORD, A), NOT_EQUAL, oper(0), handle_error),
				"ret\n",
		
				def_label(handle_error),
				printf(GetErrorMessage(deref(error_code)),
					// avoid recursively checking for errors
					WriteToPipe::stdout_without_fail_check), // TODO: use stderr--except it lags in intellij :(
				exit(oper(deref(error_code)))));
		});
	}
	/**
	 * Calling convention.
	 * Also check whether GetLastError was set.
	 */
	public static String __ms_fastcall_64_w_error_check(final Proc proc)
	{
		return join(
			"    call GetLastError__prologue_reset",
			__ms_fastcall_64(proc).trim(),
			"    call GetLastError__epilogue_check");
	}

	// TODO: patterns to glError checking:
	//       - always check at end of call, in a loop until it returns 0
	//       - only check if nonzero return value
	//       - only check if nonzero mutated input param
	//       - must run a special error lookup fn like GetShaderiv or GetProgramiv to find error
	//       should make a single function with overloads that can handle all these scenarios
	private static final Label gl_error_epilogue =
		label(GLOBAL, "glGetError__epilogue_check");
	public static final Label gl_error_lookup =
		label(LOCAL, "glGetError__lookup");
	private static final Label gl_handle_error =
		label(LOCAL, "glGetError__handle");
	private static final Label gl_error_code =
		label(GLOBAL, "glGetError__code");
	static {
		onready(()->{
			blocks.get("PROCS").append(join(
				def_label(gl_error_epilogue),
				// last command must have returned 0x0 in RAX, indicating an error occurred
				jmp_if(DWORD, oper(DWORD, A), EQUAL, oper(0), gl_error_lookup),
				"ret\n",
				
				// TODO: Thus, glGetError should always be called in a loop, 
				//  until it returns GL_NO_ERROR, if all error flags are to be reset.
				
				def_label(gl_error_lookup),
				assign_call(gl_error_code, glGetError()),
				jmp_if(DWORD, oper(DWORD, A), NOT_EQUAL, oper(0), gl_handle_error),
				"ret\n",

				def_label(gl_handle_error),
				Console.error("glError %1!.8llX!\n", gl_error_code),
				exit(oper(deref(gl_error_code)))));
		});
	}
	/**
	 * Calling convention.
	 * Also check if GL error was triggered. 
	 */
	public static String __ms_fastcall_64_w_glGetError(final Proc proc)
	{
		return __ms_fastcall_64(proc) +
			"    call "+ gl_error_epilogue +"\n";
	}

	private static final Label exit_code = label(GLOBAL, "ExitProcess__code");
	private static final Label exit_label = label(GLOBAL, "Exit");
	static
	{
		onready(() -> {
			data(exit_code, DWORD);
			blocks.get("PROCS").append(join(
				def_label(exit_label),
				Console.log("shutdown complete."),
				call(ignoreError(ExitProcess(deref(exit_code)))),
				// the following _should_ be unnecessary if correctly exits
				"ret",
				jmp(exit_label)));
		});
	}
	/**
	 * Set exit code and jump to the label for handling process shutdown.
	 */
	public static String exit(final Operand code)
	{
		String out = "";
		if (null == code.rm)
		{
			out += join(
				assign_mov(DWORD, exit_code, code)
			);
		}
		else {
			out += join(
				mov(DWORD, oper(DWORD, A), code),
				assign_mov(DWORD, exit_code, oper(DWORD, A))
			);
		}
		
		return out += join(
			// important to call vs. jmp so it appears in stack traces.
			// especially since exiting is a common response
			// to an error which you might want to debug!
			call(exit_label));
	}
	
	private static final int FORMAT_BUFFER_SIZE = 256;
	private static final Label format_buffer = label(GLOBAL, "FormatMessage__buffer");
	private static final Label format_length = label(GLOBAL, "FormatMessage__length");
	private static final Label printf_success = label(GLOBAL, "printf__success");
	static
	{
		data(format_buffer, FORMAT_BUFFER_SIZE, BYTE);
	}
	public static String printf(final Proc proc, final Callback2<Label,Label,Proc> printerCb)
	{
		return join(
			assign_call(format_length, proc),
			assign_call(printf_success,
				printerCb.call(format_buffer, format_length)));
	}

	public static Proc GetErrorMessage(
		final LabelReference dwMessageId
	) {
		// avoid recursively checking for errors
		return ignoreError(FormatMessageA(
			bitField(FORMAT_MESSAGE_FROM_SYSTEM,
				FORMAT_MESSAGE_IGNORE_INSERTS),
			Null(),
			oper(dwMessageId),
			bitField(LANG_USER_DEFAULT__SUBLANG_DEFAULT),
			addrOf(format_buffer),
			FORMAT_BUFFER_SIZE,
			Null()));
	}

	/**
	 * Prepare a call that will return a buffer and buffer length.
	 * Meant primarily for use with `printf()` function/
	 */
	public static Proc FormatString(
		final String format,
		final Label arrayPtr
	) {
		final Label label = label(GLOBAL, "FormatString");
		data(label, BYTE, nullstr(format));
		return FormatMessageA(
			bitField(FORMAT_MESSAGE_ARGUMENT_ARRAY,
				FORMAT_MESSAGE_FROM_STRING),
			oper(addrOf(label)),
			Null(),
			Null(),
			addrOf(format_buffer),
			FORMAT_BUFFER_SIZE,
			oper(addrOf(arrayPtr)));
	};

	/**
	 * Invokes LoadLibraryA once and GetProcAddress for each method you want to
	 * reference from that library.
	 */
	public static String dllimport(final String library, final String... procs)
	{
		final Label libName = label(GLOBAL, "LoadLibraryA__"+ library);
		final Label libAddr = label(GLOBAL, "LoadLibraryA__"+ library +"_hModule");
		data(libName, BYTE, nullstr(library +".dll"));
		data(libAddr, QWORD);
		String out = join(
			comment("dynamically load library at runtime"),
			assign_call(libAddr, LoadLibraryA(libName)));
		
		for (final String proc : procs)
		{
			final Label procName = label(GLOBAL, "GetProcAddress__"+ proc, true);
			final Label procAddr = label(GLOBAL, proc, true);
			data(procName, BYTE, nullstr(proc));
			data(procAddr, QWORD);
			out += join(assign_call(procAddr, GetProcAddress(libAddr, procName)));
		}
		return out;
	}

	static
	{
		onready(()->{
			data(WriteToPipe.bytesWritten, DWORD);
			blocks.get("INIT").append(join(
				comment("get pointers to stdout/stderr pipes"),
				assign_call(WriteToPipe.stderr, GetStdHandle(STD_ERROR_HANDLE)),
				assign_call(WriteToPipe.stdout, GetStdHandle(STD_OUTPUT_HANDLE))));
		});
	}
	
	/**
	 * Uses Windows Kernel32.dll WriteFile to print to STDOUT or STDERR.
	 * Useful when debugging.
	 */
	public static class WriteToPipe
	{
		static final Label bytesWritten =
			label(GLOBAL, "WriteFile__bytesWritten");
		static final Label stderr =
			label(GLOBAL, "Console__stderr_nStdHandle");
		static final Label stdout =
			label(GLOBAL, "Console__stdout_nStdHandle");

		public static Proc stdout(final Label str, final Label len)
		{
			return WriteFile(
				stdout,
				str,
				len,
				bytesWritten,
				Null());
		}

		public static Proc stdout_without_fail_check(final Label str, final Label len)
		{
			return ignoreError(stdout(str, len));
		}
		
		public static Proc stderr(final Label str, final Label len)
		{
			return WriteFile(
				stderr,
				str,
				len,
				bytesWritten,
				Null());
		}
		
		public static Proc stderr_without_fail_check(final Label str, final Label len)
		{
			return ignoreError(stderr(str, len));
		}
	}
	
	public static class Console
	{
		public static String log(final String format, final Label arrayptr)
		{
			return printf(ignoreError(FormatString( format+"\n", arrayptr)),
				WriteToPipe::stdout_without_fail_check);
		}
		
		public static String log(final String msg)
		{
			return log(msg, null);
		}
		
		public static String error(final String format, final Label arrayptr)
		{
			return printf(ignoreError(FormatString( format+"\n", arrayptr)),
				WriteToPipe::stderr_without_fail_check);
		}
	}
	
	public static String trace(final String msg)
	{
		final StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
		return Console.log(stack.getFileName().replace(".java","")+":"+stack.getLineNumber()+": "+ msg);
	}
	
	public static String trace(final String msg, final Label arrayptr)
	{
		final StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
		return Console.log(stack.getFileName().replace(".java","")+":"+stack.getLineNumber()+": "+ msg, arrayptr);
	}
	
	public static Proc ignoreError(final Proc proc) {
		proc.convention = Macros::__ms_fastcall_64;
		return proc;
	}

	/**
	 * Adds an extern reference at the top of the document.
	 * Only one per module; subsequent calls will fail silently.
	 */
	private static HashSet<String> externs = new HashSet<>();
	public static Label extern(final Label label)
	{
		final String oldSection = section;
		section = "preprocessor";
		if (!externs.contains(label.name))
		{
			asm("extern "+ label.name);
			externs.add(label.name);
		}
		section = oldSection;
		return label;
	}

	/**
	 * A comment in NASM syntax.
	 */
	public static String comment(final String... comments)
	{
		return "; "+ String.join("\n; ", comments);
	}
	
	private enum EscapeStringType
	{
		CODE,
		SAFE_STRING
	}
	private static class Code
	{
		int code;
	}
	private static class SafeString
	{
		StringBuilder sb = new StringBuilder();
	}

	/**
	 * Internal data structure defines a procedure in terms of calling convention,
	 * a collection of labels, parameters, operand/address sizes, code comments,
	 * and a return op.
	 *
	 * Serialized into NASM syntax by the defined calling convention callback,
	 * when passed to an instruction like `call()`.
	 */
	public static class Proc
	{
		public Callback1<Proc,String> convention;
		public LabelReference proc;
		public ArrayList<SizedOp> args;
		public SizedOp ret;

		public Proc(
			final LabelReference proc,
			final ArrayList<SizedOp> args,
			final SizedOp ret
		) {
			this.proc = proc;
			this.args = args;
			this.ret = ret;
		}

		public Proc(
			final Callback1<Proc,String> convention,
			final LabelReference proc,
			final SizedOp ret
		) {
			this.convention = convention;
			this.proc = proc;
			this.args = new ArrayList<>();
			this.ret = ret;
		}

		public Proc(
			final LabelReference proc,
			final SizedOp ret
		) {
			this.proc = proc;
			this.args = new ArrayList<>();
			this.ret = ret;
		}

		public Proc(final LabelReference proc)
		{
			this.proc = proc;
			this.args = new ArrayList<>();
		}

		public Proc(final LabelReference proc, final ArrayList<SizedOp> args)
		{
			this.proc = proc;
			this.args = args;
		}

		public Proc(
			final Callback1<Proc,String> convention,
			final LabelReference proc,
			final ArrayList<SizedOp> args
		) {
			this.convention = convention;
			this.proc = proc;
			this.args = args;
		}

		public Proc(
			final Callback1<Proc,String> convention,
			final LabelReference proc,
			final ArrayList<SizedOp> args,
			final SizedOp ret
		) {
			this.convention = convention;
			this.proc = proc;
			this.args = args;
			this.ret =ret;
		}
	}

	/**
	 * Internal data structure symbolizing the various operand/address sizes
	 * supported by NASM.
	 */
	public enum Size
	{
		BYTE(1, "db"),    // 8-bit
		WORD(2, "dw"),    // 16-bit
		DWORD(4, "dd"),   // 32-bit
		QWORD(8, "dq"),   // 64-bit
		DQWORD(16, "dt"); // 128-bit+ (extended precision)

		public final int bytes;
		public final String d;
		Size(final int bytes, final String d)
		{
			this.bytes = bytes;
			this.d = d;
		}
		
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}

	public static class SizedOp
	{
		public Operand op;
		public Size size;
	}
	
	public static SizedOp width(final Size size, final Operand value)
	{
		final SizedOp r = new SizedOp(); 
		r.op = value;
		r.size = orEquals(size, DWORD);
		return r;
	}
	
	public static SizedOp returnVal(final Size size, final String comment)
	{
		return width(size, placeholder(comment));
	}
	
	public static class CustomType
	{
		public final Size size;
		public Operand op;
		public CustomType(final Size size, final String comment)
		{
			this.size = size;
			this.op = placeholder(comment);
		}

		public CustomType defaultValue(final Operand value)
		{
			final Operand op = value.clone();
			op.comment = joinUnlessEmpty(this.op.comment, " ", op.comment);
			this.op = op;
			return this;
		}
		
		private CustomType(final Size size, final Operand op) {
			this.size = size;
			this.op = op;
		}
		
		public CustomType clone()
		{
			return new CustomType(size, op.clone());
		}
	}
	
	/**
	 * Escape a string for use as data in NASM syntax.
	 */
	public static String escapeString(final String s)
	{
		EscapeStringType lastType = EscapeStringType.CODE;
		final ArrayList<Object> a = new ArrayList<>();
		final String[] chars = s.split("");
		for (final String _char : chars)
		{
			int code = _char.charAt(0);
			if (code < 32 || 34 == code || code > 126)
			{
				final Code _code = new Code();
				_code.code = code;
				a.add(_code);
				lastType = EscapeStringType.CODE;
			}
			else
			{
				if (EscapeStringType.SAFE_STRING == lastType)
				{
					((SafeString) a.get(a.size()-1)).sb.append(_char);
				}
				else
				{
					SafeString ss = new SafeString();
					ss.sb.append(_char);
					a.add(ss);
				}
				lastType = EscapeStringType.SAFE_STRING;
			}
		}
		ArrayList<String> out = new ArrayList<>();
		for (final Object o : a)
		{
			if (o instanceof SafeString)
			{
				out.add(((SafeString) o).sb.insert(0, "\"").append("\"").toString());
			}
			else
			{
				out.add(Integer.toString(((Code) o).code));
			}
		}
		return String.join(",", out);
	}

	/**
	 * The libc variable-length string .data structure.
	 * Ends a string definition with a NUL \0 character.
	 */
	public static String nullstr(final String s)
	{
		return escapeString(s) + ",0";
	}

	/**
	 * Defines float as .data in memory for quick-reference by SIMD instructions
	 * and easy reuse, since MMX/SSE instructions typically don't accept immediate
	 * operands.
	 */
	private static int floatArrayCount = 0;
	public static Label staticFloatToMemory(final float... floats)
	{
		String value = "";
		for (final float f : floats)
		{
			if (!"".equals(value)) value += ",";
			value += "0x"+Integer.toHexString(Float.floatToIntBits(f));
		}
		final String name = 1 == floats.length ? ""+floats[0] : "Array"+ (floatArrayCount++);
		final Label label = label(GLOBAL, ("F"+ name).replace(".","_"), true);
		data(label, QWORD, value);
		return label;
	}

	/**
	 * `CALL` instruction in NASM syntax.
	 */
	public static String call(final Proc procOpts)
	{
		if (null == procOpts.convention) 
		{
			procOpts.convention = Macros::__ms_fastcall_64_w_error_check;
		}
		return procOpts.convention.call(procOpts);
	}

	public static String call(final Label label)
	{
		return join("call "+ label);
	}
	
	/**
	 * Performs an assignment of the return op from a procedure call.
	 */
	public static String assign_call(
		final Label label,
		final Proc procOpts
	) {
		if (null != procOpts.ret) {
			procOpts.ret.op.value = deref(label).toString();
			data(label, orEquals(procOpts.ret.size, DWORD));
		}
		return call(procOpts);
	}

	/**
	 * Performs an assignment of a given op to memory at the given label.
	 */
	public static String assign_mov(
		final Size size,
		final Label label,
		final Operand src
	) {
		data(label, size);
		return mov(size, oper(deref(label)), src);
	}

	/**
	 * x86 `MOV` instruction in NASM syntax.
	 */
	public static String mov(
		final Size size,
		final Operand dst,
		final Operand src
	) {
		return "mov "+ size +" "+ dst +", "+ src + formatComment(dst, src);
	}

	/**
	 * The various x86 conditions allowed by `JMP`, in reference to last `CMP`.
	 */
	public enum Compare
	{
		EQUAL("E"), ZERO("Z"), // equal
		NOT_EQUAL("NE"), NOT_ZERO("NZ"), // not equal
		GREATER("G"), // greater than (signed int)
		GREATER_EQUAL("GE"), // greater than, or equal (signed int)
		LESSER("L"), // less than (signed int)
		LESSER_EQUAL("LE"), // less than, or equal (signed int)
		ABOVE("A"), // greater than (unsigned int)
		ABOVE_EQUAL("AE"), // greater than, or equal (unsigned int)
		BELOW("B"), // less than (unsigned int)
		BELOW_EQUAL("BE"), // less than, or equal (unsigned int)
		OVERFLOW("O"),
		NOT_OVERFLOW("NO"),
		SIGNED("S"),
		NOT_SIGNED("NS");
		
		public String abbrev;
		Compare(final String abbrev)
		{
			this.abbrev = abbrev;
		}
	}

	/**
	 * Conditional near-jump in NASM syntax.
	 */
	public static String jmp_if(
		final Size size,
		final Operand a,
		final Compare cmp,
		final Operand b,
		final Label label
	)
	{
		return "cmp "+ size.toString().toLowerCase() +" "+ a +", "+ b + formatComment(a,b) +"\n"+
			"j"+ cmp.abbrev.toLowerCase() +" near "+ label;
	}
	
	public static String formatComment(final Operand a, final Operand b)
	{
		final String comment = joinUnlessEmpty(a.comment, " ", b.comment);
		return isEmpty(comment) ? "" : " ; "+ comment;
	}

	public interface BitField
	{
		String getName();
		int getValue();
	}
	public static Operand bitField(final BitField... flags)
	{
		int value = 0;
		String comment = "";
		for (final BitField flag : flags)
		{
			value |= flag.getValue();
			comment = joinUnlessEmpty(comment, " | ", flag.getName());
		}
		return oper(value).comment(comment);
	}
	
	/**
	 * Near-jump in NASM syntax.
	 */
	public static String jmp(final Label label)
	{
		return "jmp near "+ label;
	}

	/**
	 * Return from `CALL`, with return op., in NASM syntax.
	 */
	public static String ret(final SizedOp vsc)
	{
		final StringBuilder out = new StringBuilder();
		if ("0" == vsc.op.toString())
		{
			out.append("xor rax, rax ; return NULL\n");
		}
		else
		{
			if (vsc.op.isFloat) {
				_assert("returning float not supported");
				return "";
			}
			vsc.op.comment = "return "+ vsc.op.comment;
			out.append(mov(vsc.size, oper(vsc.size, A), vsc.op)).append("\n");
		}
		out.append("ret");
		return out.toString();
	}
}
