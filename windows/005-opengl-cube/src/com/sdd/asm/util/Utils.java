package com.sdd.asm.util;

public class Utils
{
	public static <T extends Object> T orEquals(T... candidates)
	{
		for (final T candidate : candidates)
		{
			if (null != candidate) return candidate;
		}
		return null;
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
}
