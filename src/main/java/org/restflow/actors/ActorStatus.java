package org.restflow.actors;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.restflow.metadata.ActorState;


import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public class ActorStatus implements Cloneable {

	@GuardedBy("this") private int 					_stepCount;
	@GuardedBy("this") private String 				_callType;
	@GuardedBy("this") private Map<String,Boolean> 	_inputEnableMap;
	@GuardedBy("this") private Map<String,Boolean> 	_outputEnableMap;
	@GuardedBy("this") private Map<String,Boolean> 	_outputStreamClosedMap;
	@GuardedBy("this") private File 				_stepDirectory;

	public ActorStatus() {
		
		synchronized(this) {
			_stepCount = 0;
			_inputEnableMap = new Hashtable<String, Boolean>();
			_outputEnableMap = new Hashtable<String, Boolean>();
			_outputStreamClosedMap = new Hashtable<String,Boolean>();
		}
	}
	
	public synchronized Object clone() throws CloneNotSupportedException {
		ActorStatus theClone = (ActorStatus) super.clone();
		
		theClone._inputEnableMap = new Hashtable<String,Boolean>(_inputEnableMap);
		theClone._outputEnableMap = new Hashtable<String,Boolean>(_outputEnableMap);
		theClone._outputStreamClosedMap = new Hashtable<String,Boolean>(_outputStreamClosedMap);
		
		return theClone;
	}

	
	public synchronized void setStepDirectory(File directory) {
		_stepDirectory = directory;
	}
	
	public synchronized File getStepDirectory() {
		return _stepDirectory;
	}
	
	public synchronized void setReadyForInput(String label, Boolean value) {
		_inputEnableMap.put(label, value);
	}

	public synchronized void enableInputs() {
		for (String label : _inputEnableMap.keySet()) {
			_inputEnableMap.put(label, true);
		}
	}

	public synchronized void enableOutputs() {
		for (String label : _outputEnableMap.keySet()) {
			_outputEnableMap.put(label, true);
		}
	}
	
	public synchronized void disableInputs() {
		for (String label : _inputEnableMap.keySet()) {
			_inputEnableMap.put(label, false);
		}
	}
	
	public synchronized void disableOutputs() {
		for (String label : _outputEnableMap.keySet()) {
			_outputEnableMap.put(label, false);
		}
	}	
	
	public synchronized void enableInput(String label) {
		_inputEnableMap.put(label, true);
	}
	
	public synchronized void disableInput(String label) {
		_inputEnableMap.put(label, false);
	}

	public synchronized void enableOutput(String label) {
		_outputEnableMap.put(label, true);
	}
	
	public synchronized void disableOutput(String label) {
		_outputEnableMap.put(label, false);
	}	

	// TODO Make sure there is an input enable flag for each input
	public synchronized boolean getInputEnable(String label) {
		Boolean inputReady = _inputEnableMap.get(label);
		return (inputReady == null || inputReady);
	}
	
	public synchronized void setOutputEnable(String label, Boolean value) {
		_outputEnableMap.put(label, value);
	}

	// TODO Make sure there is an output enable flag for each output
	public synchronized boolean getOutputEnable(String label) {
		Boolean ready = _outputEnableMap.get(label);
		return (ready == null || ready);
	}
	
	public synchronized void setOutputStreamClosed(String label) {
		_outputStreamClosedMap.put(label, true);
	}
	
	public synchronized boolean outputStreamClosed(String label) {
		return _outputStreamClosedMap.get(label);
	}
	
	public synchronized int getStepCount() {
		return _stepCount;
	}
	
	public synchronized int getStepCountIndex() {
		return _stepCount - 1;
	}

	public synchronized String getCallType() {
		return _callType;
	}

	public synchronized void setCallType(String callType) {
		_callType = callType;
	}

	public synchronized void setStepCount(int count) {
		_stepCount = count;
	}
	
	public synchronized ActorState copyState() {
		ActorState state = new ActorState();
		state.setStepCount( _stepCount );

		Map<String,Boolean> inputEnableMap = new HashMap<String,Boolean>();
		for ( String key : _inputEnableMap.keySet() ) {
			inputEnableMap.put(key, _inputEnableMap.get(key).booleanValue() );
		}
		state.setInputEnableMap(inputEnableMap);
		
		Map<String,Boolean> outputEnableMap = new HashMap<String,Boolean>();
		for ( String key : _outputEnableMap.keySet() ) {
			outputEnableMap.put(key, _outputEnableMap.get(key).booleanValue() );
		}
		state.setOutputEnableMap(outputEnableMap);		
		
		return state;
	}		
}
