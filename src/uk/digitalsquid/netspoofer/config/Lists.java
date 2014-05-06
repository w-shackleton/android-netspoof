package uk.digitalsquid.netspoofer.config;

import java.util.ArrayList;

public final class Lists {
	private Lists() {}

	public static final <T> ArrayList<T> singleton(T x) {
		ArrayList<T> result = new ArrayList<T>();
		result.add(x);
		return result;
	}
}
