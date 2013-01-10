package org.restflow.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImmutableSet<T> implements Set<T> {
	
	private final Set<T> _backing;

	@SuppressWarnings("rawtypes")
	public static final ImmutableSet EMPTY_SET = new ImmutableSet<Object>();
	
	/* constructor for wrapping an existing set */
	public ImmutableSet(Set<T> backing) {
		_backing = backing;
	}
	
	/* constructor for creating a permanently empty set */
	public ImmutableSet() {
		_backing = new HashSet<T>();
	}
	
	// accessors (allowed)
	public int size() {return _backing.size();}
	public boolean isEmpty() {return _backing.isEmpty();}
	public boolean contains(Object o) {return _backing.contains(o);}
	public Iterator<T> iterator() {return _backing.iterator();}
	public Object[] toArray() {return _backing.toArray();}
	public <T> T[] toArray(T[] a) {return _backing.toArray(a);}
	public boolean containsAll(Collection<?> c) {return _backing.containsAll(c);}

	// mutators (disallowed)
	public boolean add(T e) {throw new UnsupportedOperationException();}
	public boolean addAll(Collection<? extends T> c) {throw new UnsupportedOperationException();}
	public void clear() {throw new UnsupportedOperationException();}
	public boolean removeAll(Collection<?> c) {throw new UnsupportedOperationException();}
	public boolean remove(Object o) {throw new UnsupportedOperationException();}
	public boolean retainAll(Collection<?> c) {throw new UnsupportedOperationException();}
}