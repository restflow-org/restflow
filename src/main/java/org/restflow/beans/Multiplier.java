package org.restflow.beans;

public class Multiplier {

	private int _a;
	private int _b;
	private int _product;

	public void setA(int a) {
		_a = a;
	}

	public void setB(int b) {
		_b = b;
	}

	public int getProduct() {
		return _product;
	}
	
	public void multiply() {
		_product = _a * _b;
	}
}