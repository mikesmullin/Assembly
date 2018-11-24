package com.sdd.asm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sdd.asm.util.Utils;
import static com.sdd.asm.lib.Kernel32.*;

/**
 * Preprocessor.
 */
public class Macros
{
	public static void _assert(final String reason)
	{
		System.err.println(reason);
		System.exit(1);
	}
	
	public static String BUILD_PATH = "build/";
	public static String OUT_FILE;
	private static String section = "preprocessor";
	private static HashMap<String,StringBuilder> sections = new HashMap<>();
	static
	{
		sections.put("proprocessor", new StringBuilder());
		sections.put(".data", new StringBuilder());
		sections.put(".text", new StringBuilder());
	}
	
	public static void section(final String name)
	{
		section = name;
	}
	
	public static void asm(final String... s)
	{
		StringBuilder sb = sections.get(section);
		if (null == sb)
		{
			sb = new StringBuilder();
			sections.put(section, sb);
		}
		sb.append(String.join("\n", s)).append("\n");
	}

	private static final ArrayList<String> sectionOrder = new ArrayList<>();
	static {
		sectionOrder.add("preprocessor");
		sectionOrder.add(".data");
		sectionOrder.add(".text");
	}
	public static void out()
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

	// TODO: make sure labels are unique document-wide (ie. including label()), not just in .data section
	private static HashSet<String> definedDataLabels = new HashSet<>();

	// TODO: make a Label type that takes a Scope, and pass one Label here instead of both String and Scope
	public static void data(
		final String label,
		final int times,
		final Size size,
		final Operand val,
		final String comment
	) {
//		if(Scope.NORMAL == scope)
		if (definedDataLabels.contains(label)) return;
		definedDataLabels.add(label);
		sections.get(".data")
			.append(label)
			.append(": ")
			.append(times > 1 ? "times "+ times +" " : "")
			.append(size.d)
			.append(" ")
			.append(val)
			.append(null == comment ? "": " ; "+ comment)
			.append("\n");
	}

	public static void data(
		final String name,
		final Size size,
		final Operand val,
		final String comment
	)
	{
		data(name, 1, size, val, comment);
	}

	public static void data(final String name, final Size size, final String val) {
		data(name, 1, size, operand(val), null);
	}

	public static void data(final String name, final Size size, final float val) {
		data(name, 1, size, operand(val), null);
	}
	
	public static void data(final String name, final Size size, final int val) {
		data(name, 1, size, operand(val), null);
	}

	public static void data(final String name, final Size size, final boolean val) {
		data(name, 1, size, operand(val), null);
	}

	public static void data(final String name, final int times, final Size size)
	{
		data(name, times, size, operand(0), null);
	}

	public static void data(final String name, final Size size)
	{
		data(name, 1, size, operand(0), null);
	}
	
	public static HashMap<String,StringBuilder> blocks = new HashMap<>();
	static {
		blocks.put("INIT", new StringBuilder());
		blocks.put("PROCS", new StringBuilder());
	}
	public static String block(final String name)
	{
		return blocks.get(name).toString();
	}
	
	private static ArrayList<Runnable> initializers = new ArrayList<>();
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
	
	public static class Type
	{
		public final Size size;
		public final String name;
		public Type(final String name, final Size size)
		{
			this.name = name;
			this.size = size;
		}
	}
	
	public static final Type BYTE  = new Type("BYTE",  Size.BYTE);
	public static final Type WORD  = new Type("WORD",  Size.WORD);
	public static final Type DWORD = new Type("DWORD", Size.DWORD);
	public static final Type QWORD = new Type("QWORD", Size.QWORD);

	public static class Struct {
		public final String name;
		public Struct(final String name)
		{
			this.name = name;
		}
		
		public LinkedHashMap<String, StructType> fields = new LinkedHashMap<>();
	}

	public interface Callback<T> {
		T run();
	}

	public interface Callback1<T1,T2> {
		T2 call(T1 a);
	}
	
	public interface Callback2<T1,T2,T3> {
		T3 call(T1 a, T2 b);
	}

	public enum PretendValue
	{
		NULL,
		FALSE,
		TRUE,
		FLOAT,
		STRING
	}
	public static class Operand
	{
		public PretendValue type;
		public String str;
		public String comment;

		public Operand(final PretendValue type, final String str, final String comment)
		{
			this.type = type;
			this.str = str;
			this.comment = comment;
		}
		
		public String toString()
		{
			return str;
		}
	}

	public static Operand operand(final String value, final String comment)
	{
		return new Operand(
			null == value ? PretendValue.NULL : PretendValue.STRING,
			null == value ? "0" : value,
			null == value && (null == comment || "" == comment) ? "= NULL" : comment);
	}

	public static Operand operand(final String value)
	{
		return operand(
			value,
			"");
	}

	public static Operand operand(final float value)
	{
		return new Operand(
			PretendValue.FLOAT,
			makeFloat(value),
			"");
	}

	public static Operand operand(final boolean type)
	{
		return new Operand(
			type ? PretendValue.TRUE : PretendValue.FALSE,
			type ? "1" : "0",
			type ? "= TRUE" : "= FALSE");
	}

	public static Operand operand(final int value, final String comment)
	{
		return new Operand(
			PretendValue.STRING,
			Long.toString(value),
			comment);
	}

	public static Operand operand(final int value)
	{
		return operand(
			value,
			"");
	}

	public static class StructType
	{
		public final Type type;
		public Operand defaultValue;
		
		public StructType(final Type type, final Operand defaultValue)
		{
			this.type = type;
			this.defaultValue = defaultValue;
		}

		public StructType(final Type type)
		{
			this.type = type;
		}
	}
	
	private static HashMap<String,AtomicInteger> instanceCounter = new HashMap<>();
	public static String istruct(
		final String name,
		final Struct struct,
		final HashMap<String, Operand> values
	) {
		if (null == instanceCounter.get(struct.name)) 
		{
			instanceCounter.put(struct.name, new AtomicInteger());
		}
		instanceCounter.get(struct.name).getAndIncrement();
		final String label = name +"_"+ instanceCounter.get(struct.name).get();
		sections.get(".data")
			.append("\n; struct\n")
			.append(label).append(": ; instanceof ").append(struct.name).append("\n");
		for (final String k : struct.fields.keySet())
		{
			Operand value = Utils.orEquals(
				values.get(k),
				struct.fields.get(k).defaultValue);
			if (null == value)
			{
				_assert("struct " + struct.name + " " + label + "." + k + " is missing a required value.");
				return null;
			}
			data(
				label +"."+ k,
				struct.fields.get(k).type.size,
				value,
				struct.fields.get(k).type.name +" "+ value.comment);
		}
		sections.get(".data").append("\n");
		return label;
	}
	
	public static String istruct(
		final String name,
		final Struct struct
	) {
		return istruct(name, struct, new HashMap<>());
	}
	
	
	public static int sizeof(final Struct struct)
	{
		int size = 0;
		for (final String key : struct.fields.keySet())
		{
			final StructType type = struct.fields.get(key);
			size += type.type.size.bytes;
		}
		
		return size;
	}

	public static String hex(final int n)
	{
		return "0x"+ Integer.toHexString(n);
	}

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
		if (!"[".equals(procOpts.proc.substring(0, 1)))
		{
			extern(procOpts.proc);
		}
		final StringBuilder out = new StringBuilder();
		out.append("    ; MS __fastcall x64 ABI\n");
		// allocate ms fastcall shadow space
		int shadowSpace = Math.max(40, ( 
				procOpts.args.size() + // number of args
				1 // mystery padding; its what VC++ x64 does
			) * 8 // bytes
		);
		out.append("    sub rsp, ").append(shadowSpace)
			.append(" ; allocate shadow space\n");
		
		for (int i=procOpts.args.size()-1; i>-1; i--)
		{
			final ValueSizeComment arg = procOpts.args.get(i);
			final String pos = englishOrdinal(i+1) + ": ";
			final String register;
			if (PretendValue.FLOAT == arg.value.type)
			{
				if      (0 == i && Size.QWORD == arg.size) register = "xmm0";
				else if (1 == i && Size.QWORD == arg.size) register = "xmm1";
				else if (2 == i && Size.QWORD == arg.size) register = "xmm2";
				else if (3 == i && Size.QWORD == arg.size) register = "xmm3";
				else {
					_assert("more than 4 float args not supported right now");
					return "";
				}
				out.append("    mov qword rax, ").append(arg.value).append("\n")
					.append("    movq ").append(register).append(", rax ; ").append(pos).append(arg.comment).append("\n");
			}
			else // integer operands
			{
				if      (0 == i && Size.DWORD == arg.size) register = "ecx";
				else if (1 == i && Size.DWORD == arg.size) register = "edx";
				else if (2 == i && Size.DWORD == arg.size) register = "r8d";
				else if (3 == i && Size.DWORD == arg.size) register = "r9d";
				else if (0 == i && Size.QWORD == arg.size) register = "rcx";
				else if (1 == i && Size.QWORD == arg.size) register = "rdx";
				else if (2 == i && Size.QWORD == arg.size) register = "r8";
				else if (3 == i && Size.QWORD == arg.size) register = "r9";
				else {
					out.append("    mov ").append(arg.size.toString().toLowerCase())
						.append("[rsp + ").append(i * 8).append("], ")
						.append(arg.value)
						.append(" ; ").append(pos).append(arg.comment).append("\n");
					continue;
				}
				out.append("    mov ").append(arg.size.toString().toLowerCase())
					.append(" ").append(register).append(", ").append(arg.value)
					.append("; ").append(pos).append(arg.comment).append("\n");
			}
		}
		
		out.append("call ").append(procOpts.proc).append("\n");
		
		// handle return var, if provided
		if (null != procOpts.ret) {
			final String register;
			if (Size.DWORD == procOpts.ret.size) register = "eax";
			else if (Size.QWORD == procOpts.ret.size) register = "rax";
			else {
				_assert("unsupported return type size");
				return "";
			}
			out.append("    mov ").append(procOpts.ret.size.toString().toLowerCase()).append(" ")
				.append(procOpts.ret.value).append(", ").append(register).append(" ; return ")
				.append(procOpts.ret.comment).append("\n");
		}
	
		// deallocate ms fastcall shadow space
		out.append("    add rsp, ").append(shadowSpace).append(" ; deallocate shadow space\n");
		return out.toString();
	}

	static {
		data("GetLastError__errCode", Size.DWORD);
		onready(()->{
			blocks.get("PROCS").append("\n")
				// ensure last error is 0
				// invoked before calling a function which may or may not have lasterror support
				// makes us more confident calling GetLastError after a procedure runs to ensure 
				// it was ok (if everything is still 0)
				.append(def_label(Scope.NORMAL, "GetLastError__prologue_reset")).append("\n")
				.append(call(new Proc(Macros::__ms_fastcall_64, "SetLastError", 
					new ArrayList<ValueSizeComment>(){{
						add(new ValueSizeComment(0, Size.DWORD, "DWORD dwErrCode"));
					}})))
				.append("ret\n\n")
		
				.append(def_label(Scope.NORMAL, "GetLastError__epilogue_check")).append("\n")
				.append(call(new Proc(Macros::__ms_fastcall_64, "GetLastError",
					new ValueSizeComment(deref("GetLastError__errCode"), Size.DWORD))))
				.append(jmp_if(Size.DWORD, "eax", Comparison.NOT_EQUAL, "0", 
					label(Scope.LOCAL, "error")))
				.append("\nret\n\n")
		
				.append(def_label(Scope.LOCAL, "error")).append("\n")
				.append(printf(GetErrorMessage(deref("GetLastError__errCode")),
					// avoid recursively checking for errors
					(a,b)->setConvention(Macros::__ms_fastcall_64, Console.log(a,b)))).append("\n")
				.append(exit(deref("GetLastError__errCode")));
		});
	}
	public static String __ms_fastcall_64_w_error_check(final Proc proc)
	{
		return "    call GetLastError__prologue_reset\n" +
			__ms_fastcall_64(proc) +
			"    call GetLastError__epilogue_check\n";
	}
	
	static {
		data("glGetError__code", Size.DWORD);
		onready(()->{
			blocks.get("PROCS").append("\n"+
				def_label(Scope.NORMAL, "GetLastError__epilogue_glGetError") +"\n"+
				call(new Proc(Macros::__ms_fastcall_64, deref("glGetError"),
					new ValueSizeComment(deref("glGetError__code"), Size.DWORD, "GLenum"))) +
				jmp_if(Size.DWORD, "eax", Comparison.NOT_EQUAL, "0",
					label(Scope.LOCAL, "glError")) +"\n"+
				"ret\n\n" +

				def_label(Scope.LOCAL, "glError") +"\n"+
				printf(setConvention(Macros::__ms_fastcall_64, FormatString(
					"glGetError__str", 
					nullstr("glError %1!.8llX!\n"), 
					"glGetError__code")),
					// avoid recursively checking for errors
					(a,b)->setConvention(Macros::__ms_fastcall_64, Console.log(a,b))) +
				exit(deref("glGetError__code")));
		});
	}
	public static String __ms_fastcall_64_w_glGetError(final Proc proc)
	{
		return __ms_fastcall_64(proc) +
			"call "+ label(Scope.NORMAL, "GetLastError__epilogue_glGetError") +"\n";
	}

	static
	{
		onready(() -> {
			blocks.get("PROCS").append("\n"+
				"Exit:\n" +
				__ms_fastcall_64(new Proc("ExitProcess")) +"\n"+
				// the following _should_ be unnecessary if correctly exits
				"ret\n" +
				"jmp near Exit");
		});
	}
	public static String exit(final String code)
	{
		return "mov ecx, "+ code +" ; UINT uExitCode\n"+
		// important to call vs. jmp so it appears in stack traces.
		// especially since exiting is a common response
		// to an error which you might want to debug!
		"call Exit\n"; 
	}
	
	public static String exit(final int code)
	{
		return exit(Integer.toString(code));
	}

	static
	{
		onready(()->{
			blocks.get("INIT").append("\n"+
				comment("get pointers to stdout/stderr pipes") +"\n"+
				assign_call(Scope.NORMAL, "Console__stderr_nStdHandle",
					GetStdHandle(STD_ERROR_HANDLE)) +"\n"+
				assign_call(Scope.NORMAL, "Console__stdout_nStdHandle",
					GetStdHandle(STD_OUTPUT_HANDLE)) +"\n");
		});
		data("WriteFile__bytesWritten", Size.DWORD);
	}
	public static class Console
	{
		public static Proc log(final String str, final String len)
		{
			return WriteFile(
				deref("Console__stdout_nStdHandle"),
				str,
				len,
				"WriteFile__bytesWritten",
				null);
		}
		
		public static Proc error(final String str, final String len)
		{
			return WriteFile(
				deref("Console__stderr_nStdHandle"),
				str,
				len,
				"WriteFile__bytesWritten",
				null);
		}
	}
	
	public static Proc setConvention(
		final Callback1<Proc,String> convention,
		final Proc proc
	) {
		proc.convention = convention;
		return proc;
	}

	public static Proc GetErrorMessage(
		final String dwMessageId
	) {
		// avoid recursively checking for errors
		return setConvention(Macros::__ms_fastcall_64,
			FormatMessageA(
				FORMAT_MESSAGE_FROM_SYSTEM |
				FORMAT_MESSAGE_IGNORE_INSERTS,
				null,
				dwMessageId,
				LANG_USER_DEFAULT__SUBLANG_DEFAULT,
				"FormatMessage__buffer",
				"FormatMessage__length",
				null));
	}
	
	public static Proc FormatString(
		final String formatStringLabel,
		final String formatString,
		final String arrayPtr
	) {
		data(formatStringLabel, Size.BYTE, nullstr(formatString));
		return FormatMessageA(
			FORMAT_MESSAGE_ARGUMENT_ARRAY |
			FORMAT_MESSAGE_FROM_STRING,
			formatStringLabel,
			null,
			0,
			"FormatMessage__buffer",
			"FormatMessage__length",
			arrayPtr);
	};

	public static final int FORMAT_BUFFER_SIZE = 256;
	static
	{
		data("FormatMessage__buffer", FORMAT_BUFFER_SIZE, Size.BYTE);
	}
	public static String printf(final Proc proc, final Callback2<String,String,Proc> printerCb)
	{
		return assign_call(Scope.NORMAL, "FormatMessage__length", proc) +"\n"+
			assign_call(Scope.NORMAL, "printf__success", printerCb.call(
				"FormatMessage__buffer", // str
				deref("FormatMessage__length") // len
			));
	}

	public static String dllimport(final String library, final String... procs)
	{
		data("LoadLibraryA__"+ library, Size.BYTE, nullstr(library +".dll"));
		data("LoadLibraryA__"+ library +"_hModule", Size.QWORD);
		final StringBuilder out = new StringBuilder();
		out.append(comment("dynamically load library at runtime")).append("\n")
			.append(__ms_fastcall_64_w_error_check(new Proc("LoadLibraryA",
				new ArrayList<ValueSizeComment>() {{
					add(new ValueSizeComment("LoadLibraryA__"+ library, Size.QWORD, ""));
				}},
				new ValueSizeComment(deref("LoadLibraryA__"+ library +"_hModule"), Size.QWORD, "HMODULE"))
			)).append("\n");
			
		for (final String proc : procs)
		{
			data(proc, Size.QWORD);
			data("GetProcAddress__"+ proc, Size.BYTE, nullstr(proc));
			out.append(__ms_fastcall_64_w_error_check(new Proc("GetProcAddress",
				new ArrayList<ValueSizeComment>() {{
					add(new ValueSizeComment(deref("LoadLibraryA__"+ library +"_hModule"), Size.QWORD, "HMODULE hModule"));
					add(new ValueSizeComment("GetProcAddress__"+ proc, Size.DWORD, "LPCSTR lpProcName"));
				}},
				new ValueSizeComment(deref(proc), Size.QWORD, "FARPROC")
			))).append("\n");
		}
		return out.toString();
	}
	
	private static HashSet<String> externs = new HashSet<>();
	public static void extern(final String... modules)
	{
		final String oldSection = section;
		section = "preprocessor";
		for (final String module : modules)
		{
			if (externs.contains(module))
			{
				continue;
			}
			asm("extern "+ module);
			externs.add(module);
		}
		section = oldSection;
	}
	
	public static String comment(final String... comments)
	{
		return "; "+ String.join("\n; ", comments);
	}
	
	public enum Scope
	{
		GLOBAL,
		NORMAL,
		LOCAL
	}
	
	public static String label(final Scope scope, final String name)
	{
		if (Scope.GLOBAL == scope)
		{
			return("global "+ name +"\n"+
				name);
		}
		else if (Scope.NORMAL == scope)
		{
			return name;
		}
		else if (Scope.LOCAL == scope)
		{
			return "..@"+ name;
		}
		else
		{
			_assert("Invalid scope");
			return "";
		}
	}
	
	public static String def_label(final Scope scope, final String name)
	{
		return label(scope, name) + ":";
	}
	
	public static String addrOf(final String label)
	{
		return label;
	}

	public static String deref(final String label)
	{
		return "[" + label +"]";
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
	
	public static String nullstr(final String s)
	{
		return escapeString(s) + ",0";
	}
	
	public static String makeFloat(final float f)
	{
		final String label = ("F"+ f).replace(".","_");
		assign(Scope.NORMAL, label,  Size.QWORD, 
			"0x"+Integer.toHexString(Float.floatToIntBits(f)));
		return deref(label);
	}

	public static String call(final Proc procOpts)
	{
		if (null == procOpts.convention) 
		{
			procOpts.convention = Macros::__ms_fastcall_64_w_error_check;
		}
		return procOpts.convention.call(procOpts);
	}
	public static String assign(
		final Scope scope,
		final String label,
		final Size size,
		final String value
	) {
		data(label, size, value);
		return label;
	}

	public static String assign(
		final Scope scope,
		final String label,
		final Size size
	)
	{
		return assign(scope, label, size, "0");
	}
	
	public static class Proc
	{
		public Callback1<Proc,String> convention;
		public String proc;
		public ArrayList<ValueSizeComment> args;
		public ValueSizeComment ret;
		
		public Proc(
			final String proc,
			final ArrayList<ValueSizeComment> args,
			final ValueSizeComment ret
		) {
			this.proc = proc;
			this.args = args;
			this.ret = ret;
		}

		public Proc(
			final Callback1<Proc,String> convention,
			final String proc,
			final ValueSizeComment ret
		) {
			this.convention = convention;
			this.proc = proc;
			this.args = new ArrayList<>();
			this.ret = ret;
		}

		public Proc(
			final String proc,
			final ValueSizeComment ret
		) {
			this.proc = proc;
			this.args = new ArrayList<>();
			this.ret = ret;
		}
		
		public Proc(final String proc)
		{
			this.proc = proc;
			this.args = new ArrayList<>();
		}
		
		public Proc(final String proc, final ArrayList<ValueSizeComment> args)
		{
			this.proc = proc;
			this.args = args;
		}
		
		public Proc(
			final Callback1<Proc,String> convention,
			final String proc,
			final ArrayList<ValueSizeComment> args
		) {
			this.convention = convention;
			this.proc = proc;
			this.args = args;
		}
	}

	public enum Size
	{
		BYTE(1, "db"),
		WORD(2, "dw"),
		DWORD(4, "dd"),
		QWORD(8, "dq");
		
		public final int bytes;
		public final String d;
		Size(final int bytes, final String d)
		{
			this.bytes = bytes;
			this.d = d;
		}
	}
	
	public static class ValueSizeComment
	{
		Operand value;
		Size size;
		String comment;

		private void init(final Operand value, final Size size, final String comment)
		{
			this.value = value;
			this.size = Utils.orEquals(size, Size.DWORD);
			this.comment = Utils.orEquals(comment, "");
		}

		public ValueSizeComment(final String value, final Size size, final String comment)
		{
			init(operand(value), size, comment);
		}

		public ValueSizeComment(final float value, final Size size, final String comment)
		{
			init(operand(value), size, comment);
		}

		public ValueSizeComment(final int value, final Size size, final String comment)
		{
			init(operand(value), size, comment);
		}
		
		public ValueSizeComment(final boolean value, final Size size, final String comment)
		{
			init(operand(value), size, comment);
		}

		public ValueSizeComment(final Size size, final String comment)
		{
			init(null, size, comment);
		}
		
		public ValueSizeComment(final String value, final Size size)
		{
			init(operand(value), size, "");
		}
	}
	
	public static String assign_call(
		final Scope scope,
		final String label,
		final Proc procOpts
	) {
		if (null != procOpts.ret) {
			procOpts.ret.value = operand(deref(label));
			assign(
				scope,
				label,
				Utils.orEquals(procOpts.ret.size, Size.DWORD));
		}
		return call(procOpts);
	}

	public static String assign_mov(
		final Scope scope,
		final String label,
		final Size size,
		final String rm
	) {
		assign(scope, label, size);
		// TODO: make a mov2 or something that does reverse direction
		return "mov "+ size.toString().toLowerCase() +" "+ deref(label) +", "+ rm;
	}
	
	public static String mov(final Size size, final String rm, final String label, final String comment)
	{
		return "mov "+ size.toString().toLowerCase() +" "+ rm +", "+ label +"; "+ comment;
	}
	
	public enum Comparison
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
		Comparison(final String abbrev)
		{
			this.abbrev = abbrev;
		}
	}
	public static String jmp_if(
		final Size size,
		final String a,
		final Comparison cmp,
		final String b,
		final String label
	)
	{
		return "cmp "+ size.toString().toLowerCase() +" "+ a +", "+ b +"\n"+
			"j"+ cmp.abbrev.toLowerCase() +" near "+ label;
	}
	
	public static String jmp(final String label)
	{
		return "jmp near "+ label;
	}

	public static String ret(final ValueSizeComment val)
	{
		final StringBuilder out = new StringBuilder();
		if (null == val || "0" == val.value.toString())
		{
			out.append("xor rax, rax ; return NULL\n");
		}
		else
		{
			if (PretendValue.FLOAT == val.value.type) {
				_assert("returning float not supported");
				return "";
			}
			final String register;
			if (Size.DWORD == val.size) register = "eax";
			else if (Size.QWORD == val.size) register = "rax";
			else {
				_assert("unsupported return type size");
				return "";
			}
			out.append(mov(val.size, register, val.value.toString(), "return "+ val.comment)).append("\n");
		}
		out.append("ret");
		return out.toString();
	}
}
