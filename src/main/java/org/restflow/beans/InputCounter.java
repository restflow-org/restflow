package org.restflow.beans;

public class InputCounter {

	private int _count;

	public void initialize() {
		_count = 0;
	}
	
	public void setInput(String input) {
		_count++;
	}

	public int getCount() {
		return _count;
	}
	
	public void setCount(int c) {
		_count = c;
	}
}