package org.restflow.beans;

public class SystemTime {

	long _time;
	
	public void create() {
		_time = System.currentTimeMillis();
	}

	public long getTime() {
		return _time;
	}

	public void setTime(long time) {
		_time = time;
	}


	
}