package org.restflow.metadata;

import java.util.Hashtable;
import java.util.Map;

public class ActorState implements Cloneable {

	private int stepCount;
	private Map<String,Boolean> inputEnableMap;
	private Map<String,Boolean> outputEnableMap;
	private Map<String,Boolean> outputStreamClosedMap;
	private Map<String,Object> stateValues;
	
	public ActorState() {
			stepCount = 0;
			inputEnableMap = new Hashtable<String, Boolean>();
			outputEnableMap = new Hashtable<String, Boolean>();
			outputStreamClosedMap = new Hashtable<String,Boolean>();
	}

	public int getStepCount() {
		return stepCount;
	}

	public void setStepCount(int stepCount) {
		this.stepCount = stepCount;
	}

	public Map<String, Boolean> getInputEnableMap() {
		return inputEnableMap;
	}

	public void setInputEnableMap(Map<String, Boolean> inputEnableMap) {
		this.inputEnableMap = inputEnableMap;
	}

	public Map<String, Boolean> getOutputEnableMap() {
		return outputEnableMap;
	}

	public void setOutputEnableMap(Map<String, Boolean> outputEnableMap) {
		this.outputEnableMap = outputEnableMap;
	}
	
	// TODO No references to this method.  Needed?
	public Map<String, Boolean> getOutputStreamClosedMap() {
		return outputStreamClosedMap;
	}

	// TODO No references to this method.  Needed?
	public void setOutputStreamClosedMap(Map<String, Boolean> outputStreamClosedMap) {
		this.outputStreamClosedMap = outputStreamClosedMap;
	}

	public Map<String, Object> getStateValues() {
		return stateValues;
	}

	public void setStateValues(Map<String, Object> stateValues) {
		this.stateValues = stateValues;
	}
	
	
	
}
