package org.restflow.data;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

@SuppressWarnings("serial")
public class ConsumableObjectStore extends HashMap<String,Object> {
	
	public Object take(String key) {
		Object o = super.get(key);
		super.remove(key);
		return o;
	}

	public boolean removeValue(Object value) throws Exception {
		
		for (Map.Entry<String,Object> entry : entrySet()) {
			if (entry.getValue() == value) {
				super.remove(entry.getKey());
				return true;
			}
		}
		
		throw new Exception("Store does not contain value " + value);
	}
	
}