package org.restflow.beans;

import org.restflow.actors.ActorStatus;

public class IntegerStreamMergerNoDuplicates {

	private Integer _a;
	private Integer _b;
	private Integer _c;
	
	private boolean _first;
	private int _last;

	private ActorStatus _actorStatus;
	
	public void setStatus(ActorStatus actorStatus) {
		_actorStatus = actorStatus;
	}
	
	public void initialize() {
		_actorStatus.enableInput("a");
		_actorStatus.enableInput("b");
		_first = true;
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

	public void setFirst(boolean b) {
		_first = b;
	}
	
	public boolean getFirst() {
		return _first;
	}
	
	public void setLast(int i) {
		_last = i;
	}
	
	public int getLast() {
		return _last;
	}
	
	public void selectLowerValue() {
		if ((_a == null) && (_b == null)) {
			_c = null;
			return;
		}
		if (_a == null) {
			_c = _b;
			_actorStatus.enableInput("b");
		} else if (_b == null) {
			_c = _a;
			_actorStatus.enableInput("a");
		} else if (_a.compareTo(_b) < 0) {
			_c = _a;
			_actorStatus.enableInput("a");
		} else if (_a.compareTo(_b) > 0) {
			_c = _b;
			_actorStatus.enableInput("b");
		} else if (_a.compareTo(_b) == 0) {
			_c = _a;
			_actorStatus.enableInput("a");
			_actorStatus.enableInput("b");
		}

		if (_first) {
			_last = _c.intValue();
			_first = false;
		} else {
			if (_c == null) {
				System.out.println("X");
			}
			if (_c.intValue() == _last) {
		        _actorStatus.disableOutput("c");			
			} else {
				_last = _c.intValue();
			}
		}
	}

	public Integer getC() {		
		return _c;
	}
}