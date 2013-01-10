package org.restflow.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImmutableMap<K,V> implements Map<K,V> {
	
	private final Map<K,V> _backing;
	
	/* constructor for wrapping an existing map */
	public ImmutableMap(Map<K,V> backing) {
		_backing = backing;
	}
	
	/* constructor for creating a permanently empty map */
	public ImmutableMap() {
		_backing = new HashMap<K,V>();
	}
	
	// accessors (allowed)
	public int size() {return _backing.size();}
	public boolean isEmpty() {return _backing.isEmpty();}
	public boolean containsKey(Object key) {return _backing.containsKey(key);}
	public boolean containsValue(Object value) {return _backing.containsValue(value);}
	public V get(Object key) {return _backing.get(key);}
	public Set<K> keySet() {return _backing.keySet();}
	public Collection<V> values() {return _backing.values();}
	public Set<Map.Entry<K,V>> entrySet() {return _backing.entrySet();}
	
	// mutators (disallowed)
	public V put(Object key, Object value) {throw new UnsupportedOperationException();}
	public V remove(Object key) {throw new UnsupportedOperationException();}
	public void putAll(Map<? extends K, ? extends V> m) {throw new UnsupportedOperationException();}
	public void clear() {throw new UnsupportedOperationException();}
}