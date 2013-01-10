package org.restflow.actors;

public class CloneableBean implements Cloneable {
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}