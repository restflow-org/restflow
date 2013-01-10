package org.restflow.beans;

import org.restflow.actors.ActorStatus;

public class SerialAdder {

	private Integer _a;
	private Integer _b;
	private int _sum;

	private ActorStatus _actorStatus;
	
	public void setStatus(ActorStatus actorStatus) {
		_actorStatus = actorStatus;
	}
	
	public void initialize() {
		_actorStatus.setReadyForInput("addend", true);
	}

	public void setAddend(int addend) {
		if (_a == null) {
			_a = addend;
		} else {
			_b = addend;
			_actorStatus.disableInput("addend");
		}
	}

	public int getSum() {
		return _sum;
	}
	
	public void add() {
		_sum = _a + _b;
		_a = null;
		_b = null;
		_actorStatus.enableInput("addend");
	}
}