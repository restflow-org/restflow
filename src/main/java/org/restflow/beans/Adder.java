package org.restflow.beans;

public class Adder {

	private int _a;
	private int _b;
	private int _sum;

	public void setA(int a) {
		_a = a;
	}

	public void setB(int b) {
		_b = b;
	}

	public int getSum() {
		return _sum;
	}
	
	public void add() {
		_sum = _a + _b;
	}
}