package org.restflow.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restflow.exceptions.IllegalWorkflowSpecException;
import org.restflow.util.ImmutableMap;

import net.jcip.annotations.GuardedBy;

//TODO rename to Sequences
public class Sequences {
	@GuardedBy("this")	private int _index;
	@GuardedBy("this")	private Map<String,List<Object>> _sequence;
	@GuardedBy("this")	private boolean _repeatValues;
	
	public Sequences() {
		super();
	
		synchronized(this) {
			_sequence = new HashMap<String,List<Object>>();
			_index = 0;
			_repeatValues = false;
		}
		
	}
	
	public synchronized Map<String, List<Object>> getSequence() {
		return new ImmutableMap<String, List<Object>>(_sequence);
	}
	
	public synchronized void setSequence(Map<String, List<Object>> sequence) {
		this._sequence = sequence;
	}
	
	
	public synchronized  boolean getRepeatValues() {
		return _repeatValues;
	}

	public synchronized  void setRepeatValues( boolean repeatValues) {
		_repeatValues = repeatValues;
	}

	public synchronized  Set<String> allParameterNames() {
		return _sequence.keySet();
	}
	
	public synchronized  void initialize() {
		_index=0;
	}
	
	public synchronized Map<String, Object> assembleNextSequenceBundle() {
		
		Map<String,Object> sequenceBundle = new HashMap<String,Object>();
		for (String label : _sequence.keySet()  ) {
			List<Object> namedSequence = _sequence.get(label);
			if ( _index < namedSequence.size() ) {
				Object value = namedSequence.get(_index);
				sequenceBundle.put(label, value);				
			} else {
				sequenceBundle.put(label, null);				
			}
		}
		
		_index++;
		int max = maxSequenceLength();
		if ( _repeatValues && max != 0 ) _index = _index % max;
		return sequenceBundle;
	}	
	
	public synchronized int maxSequenceLength() {

		int max =0;
		for (List<Object> namedSequence : _sequence.values()  ) {
			if ( max < namedSequence.size() ) {
				max = namedSequence.size();
			}
		}
		
		return max;
	}	
	
	public synchronized void configure () throws IllegalWorkflowSpecException {

		if ( _repeatValues == false) return;
		
		Set<Integer> sizes = new HashSet<Integer>();
		for (List<Object> namedSequence : _sequence.values()  ) {
			sizes.add(namedSequence.size());
		}

		if (sizes.size() != 1) {
			throw new IllegalWorkflowSpecException("cannot repeat sequences of varying length");
		}
		
	}	

	
}
