package org.restflow.beans;

public class PrintAndForwarder {

	public Object input, output;

	public void step() throws InterruptedException {
	      output = input;
	      System.out.println(output);
	}
}