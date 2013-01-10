package org.restflow.data;

import java.util.List;
import java.util.Vector;

import net.jcip.annotations.NotThreadSafe;

/* This class is not thread safe. It also is not used at present. */

@NotThreadSafe()
public class InputAccrue {

	private Integer num;
	private List<Object> values = new Vector<Object>();
	
	public InputAccrue(Integer num) {
		super();
		this.num = num;
	}

	public void addValue(Object value) {
		values.add(value);
	}
	
	public void clearValues() {
		values = new Vector<Object>();
	}
	
	
	public boolean readyForInput() {
		return ( values.size() < num);
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public List<Object> getValues() {
		return values;
	}

	public void setValues(List<Object> values) {
		this.values = values;
	}
	
	
	
}
