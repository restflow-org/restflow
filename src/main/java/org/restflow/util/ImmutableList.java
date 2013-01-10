package org.restflow.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ImmutableList<T> implements List<T> {
	
	private final List<T> _backing;

	/* constructor for wrapping an existing list */
	public ImmutableList(List<T> backing) {
		_backing = backing;
	}
	
	/* constructor for creating a permanently empty list */
	public ImmutableList() {
		_backing = new ArrayList<T>();
	}
	
	// accessors (allowed)
	public int size() {return _backing.size();}
	public boolean isEmpty() {return _backing.isEmpty();}
	public boolean contains(Object o) {return _backing.contains(o);}
	public Iterator<T> iterator() {return _backing.iterator();}
	public Object[] toArray() {return _backing.toArray();}
	public <T> T[] toArray(T[] a) {return _backing.toArray(a);}
	public boolean containsAll(Collection<?> c) {return _backing.containsAll(c);}
	public T get(int index) {return _backing.get(index);}
	public int indexOf(Object o) {return _backing.indexOf(o);}
	public int lastIndexOf(Object o) {return _backing.lastIndexOf(o);}
	public ListIterator<T> listIterator() {return _backing.listIterator();}
	public ListIterator<T> listIterator(int index) {return _backing.listIterator(index);}
	public List<T> subList(int fromIndex, int toIndex) {return _backing.subList(fromIndex, toIndex);}

	// mutators (disallowed)
	public boolean add(T e) {throw new UnsupportedOperationException();}
	public void add(int index, T element)  {throw new UnsupportedOperationException();}
	public boolean addAll(Collection<? extends T> c) {throw new UnsupportedOperationException();}
	public boolean addAll(int index, Collection<? extends T> c) {throw new UnsupportedOperationException();}
	public void clear() {throw new UnsupportedOperationException();}
	public boolean remove(Object o) {throw new UnsupportedOperationException();}
	public boolean removeAll(Collection<?> c) {throw new UnsupportedOperationException();}
	public boolean retainAll(Collection<?> c) {throw new UnsupportedOperationException();}
	public T set(int index, T element) {throw new UnsupportedOperationException();}
	public T remove(int index) {throw new UnsupportedOperationException();}
}