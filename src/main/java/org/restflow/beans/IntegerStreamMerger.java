package org.restflow.beans;

import org.restflow.actors.ActorStatus;

public class IntegerStreamMerger {

	private Integer _a;
	private Integer _b;
	private Integer _c;
	private ActorStatus _actorStatus;
	
	public void setStatus(ActorStatus actorStatus) {
		_actorStatus = actorStatus;
	}
	
	public void initialize() {
		_actorStatus.enableInput("a");
		_actorStatus.enableInput("b");
	}
	
	public void setA(Integer a) {
		_a = a;
	}

	public Integer getA() {
		return _a;
	}
	
	public void setB(Integer b) {
		_b = b;
	}
	
	public Integer getB() {
		return _b;
	}

	public void selectLowerValue() {
		
		// if only a is null set result to b and clear b
		if (_a == null) {
			_c = _b;
			_actorStatus.enableInput("b");
			return;
		}
		
		// if only b is null set result to a and clear a
		if (_b == null) {
			_c = _a;
			_actorStatus.enableInput("a");
			return;
		} 
		
		// if a is less than b set result to a and clear a
		else if (_a < _b) {
			_c = _a;
			_actorStatus.enableInput("a");
			return;
		} 
		
		// if b is less than a set result to b and clear b
		if ( _b < _a) {
			_c = _b;
			_actorStatus.enableInput("b");
			return;
		}

		// if a and b are equal set result to a and clear a
		_c = _a;
		_actorStatus.enableInput("a");

	}

	public Integer getC() {		
		return _c;
	}
}