package org.restflow.data;

import java.util.Collection;
import java.util.Vector;

import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  Its superclass is synchronized, it adds no fields,
 * and the methods it overrides or adds are synchronized.
 * 
 * @author Timothy McPhillips
 *
 */
@ThreadSafe()
@SuppressWarnings({"rawtypes", "unchecked", "serial" })
public class SortedClassVector extends Vector<Class> {

	@Override
	public synchronized boolean add(Class c) {

		// iterate over the classes currently in the sorted list
		for (int i = 0; i < size(); i++) {
			
			// access the next class in the list
			Class t = this.get(i);
			
			// add the new class before the current class if the latter is a superclass of the new class
			if (t.isAssignableFrom(c)) {
				add(i, c);
				return true;
			}
		}
		// add the new class at the end of the list because no superclasses were found
		super.add(c);
		
		return true;
	}
	
	@Override
	public synchronized boolean addAll(Collection<? extends Class> collection) {

		for (Class t : collection) {
			this.add(t);
		}
		
		return true;
	}
	
	
	public synchronized Class getClosestSuperclass(Class c) {
		
		for (Class t : this) {		
			if (t.isAssignableFrom(c)) {
				return t;
			}		
		}
	
		return null;
	}
}
