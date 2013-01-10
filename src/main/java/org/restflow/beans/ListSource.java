package org.restflow.beans;

import java.util.List;

public class ListSource {

	private List<Object> _values;

	public void setInputList(List<Object> values) {
		_values = values;
	}

	public List<Object> getOutputList() {
		return _values;
	}
}