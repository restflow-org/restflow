package org.restflow.beans;

public class IntegerDivider {

	public int numerator, divisor, quotient, remainder;

	public void step() throws InterruptedException {
	   quotient = numerator / divisor;
	   remainder = numerator % divisor;
	}
}