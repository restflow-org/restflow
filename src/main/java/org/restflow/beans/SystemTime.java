package org.restflow.beans;

import java.util.Date;

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