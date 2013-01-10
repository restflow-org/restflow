package org.restflow.util;

public class BitPattern {
	
	public static boolean includes(int pattern, int value) {
		return (pattern & value) != 0;
	}
}