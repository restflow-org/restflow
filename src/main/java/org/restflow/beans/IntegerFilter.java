package org.restflow.beans;

import org.restflow.actors.ActorStatus;

public class IntegerFilter {

	private int _min;
	private int _max;
	private int _input;
	private int _output;
	private ActorStatus _actorStatus;
	
	public void setStatus(ActorStatus actorStatus) {
		_actorStatus = actorStatus;
	}
	
	public void setMin(int min) {
		_min = min;
	}

	public void setMax(int max) {
		_max = max;
	}

	public void setInput(int input) {
		_input = input;
	}
	
	public void step() {
		if (_input >= _min && _input <= _max) {
			_output = _input;
		} else {
			_actorStatus.setOutputEnable("output", false);			
		}
	}
	public Integer getOutput() {
		return _output;
	}
}