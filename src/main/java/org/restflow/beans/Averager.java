package org.restflow.beans;

import java.util.List;

public class Averager {

	private List<Number> _valueList;
	private Double _average;

	public void setValueList(List<Number> values) {
		_valueList = values;
	}

	public void computeAverage() {

		double sum = 0;
		int count = 0;
		
		for (Number value : _valueList) {
			sum = sum + value.doubleValue();
			count ++;
		}
		
		if (count > 0) {
			_average = sum / count;
		} else {
			_average = null;
		}
	}

	public Double getAverage() {
		return _average;
	}	
}