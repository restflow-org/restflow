package org.restflow.beans;

import java.util.Iterator;
import java.util.Map;

import org.restflow.actors.ActorStatus;



/**
 * 
 * Takes a java Map and iterates over the EntrySet, publishing
 * the key and value (item) for each Entry.  It also publishes
 * a value 'last' to let the downstream actors know that the
 * stream is ending.
 * 
 * @author scottm
 *
 */
public class MapDisassembler {

	Object _item;
	String _key;
	boolean _last;

	private Map<String,Object> _map;
	private Iterator<Map.Entry<String,Object>> _iterator;
	private ActorStatus _actorStatus;
	
	public void setStatus(ActorStatus actorStatus) {
		_actorStatus = actorStatus;
	}
	
	public void initialize() {
		_iterator = null;
		_actorStatus.enableInput("map");
	}
	
	public Map<String, Object> getMap() {
		return _map;
	}

	public void setMap(Map<String, Object> map) {
		_map = map;
	}

	public void step() {
		if (_iterator == null) {
			_iterator = _map.entrySet().iterator();
		}

		if (_iterator.hasNext()) {

			Map.Entry<String,Object> entry = (Map.Entry<String,Object>) _iterator.next();

			_item = entry.getValue();
			_key  = entry.getKey();
			_last = !_iterator.hasNext();

			if (_last == true) {
				_iterator = null;
				_actorStatus.enableInputs();
			} else {
				_actorStatus.disableInputs();
			}
		}	
	}
	
	public Object getItem() {
		return _item;
	}
	
	public void setItem(Object item) {
		_item = item;
	}

	public String getKey() {
		return _key;
	}

	public void setKey(String key) {
		_key = key;
	}

	public Boolean getLast() {
		return _last;
	}

	public boolean isLast() {
		return _last;
	}

	public void setLast(boolean last) {
		_last = last;
	}

	public Iterator<Map.Entry<String, Object>> getIterator() {
		return _iterator;
	}

	public void setIterator(Iterator<Map.Entry<String, Object>> iterator) {
		_iterator = iterator;
	}
	
}