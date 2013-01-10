package org.restflow.beans;

public class StringConcatenator {

	private String _stringOne;
	private String _stringTwo;

	public StringConcatenator() {
	}

	public void setStringOne(String s1) {
		_stringOne = s1;
	}

	public void setStringTwo(String s2) {
		_stringTwo = s2;
	}

	public String getConcatenatedString() {
		return _stringOne + _stringTwo;
	}
}