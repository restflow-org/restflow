package org.restflow.beans;

public class ConstantSource {

	private Object _value;

	public void setValue(Object value) {
		_value = value;
	}

	public Object getValue() {
		return _value;
	}
}