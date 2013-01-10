package org.restflow.beans;

import org.restflow.actors.ActorStatus;

public class Ramp {

	private int _initial;
	private int _step;
	private int _max;
	private int _value;
	private boolean _first;
	private ActorStatus _actorStatus;

	public void setInitial(int i) { _initial = i; }
	public void setStep(int s) { _step = s; }
	public void setValue(int v) {_value = v; }
	public void setMax(int m) { _max = m; }
	public int getValue() { return _value; }

	public void setStatus(ActorStatus actorStatus) {
		_actorStatus = actorStatus;
	}
	
	public void setFirst(boolean b) {
		_first = b;
	}
	
	public boolean getFirst() {
		return _first;
	}
	
	public void initialize() {
		_first = true;
	}

	public void step() {
		
		if (_first) {
			_value = _initial;
			_first = false; 
		} else {
			_value += _step;
		}
		
		if (_value > _max) {
			_actorStatus.setOutputEnable("value", false);			
		}
	}
}