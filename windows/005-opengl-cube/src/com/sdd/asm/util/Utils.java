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
}
