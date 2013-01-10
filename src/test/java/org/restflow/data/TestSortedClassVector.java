package org.restflow.data;

import java.util.LinkedList;
import java.util.List;

import org.restflow.data.SortedClassVector;
import org.restflow.exceptions.ActorException;
import org.restflow.exceptions.IllegalWorkflowSpecException;
import org.restflow.exceptions.NullOutputException;
import org.restflow.exceptions.RestFlowException;
import org.restflow.test.RestFlowTestCase;


@SuppressWarnings("serial")
public class TestSortedClassVector extends RestFlowTestCase {

	public void testAdd_OrderOne() {
		
		SortedClassVector list = new SortedClassVector();
		assertEquals(0,list.size());
		
		list.add(String.class);
		assertEquals(1,list.size());
		assertEquals(String.class, list.get(0));
		
		list.add(Double.class);
		assertEquals(String.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		assertEquals(2,list.size());
		
		list.add(Object.class);
		assertEquals(String.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		assertEquals(Object.class, list.get(2));
		assertEquals(3,list.size());

		list.add(Exception.class);
		assertEquals(4,list.size());
		assertEquals(String.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		assertEquals(Exception.class, list.get(2));
		assertEquals(Object.class, list.get(3));

		list.add(NullPointerException.class);
		assertEquals(5,list.size());
		assertEquals(String.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		assertEquals(NullPointerException.class, list.get(2));
		assertEquals(Exception.class, list.get(3));
		assertEquals(Object.class, list.get(4));
	}
	

	public void testAdd_OrderTwo() {
		
		SortedClassVector list = new SortedClassVector();
		assertEquals(0,list.size());
		
		list.add(String.class);
		assertEquals(1,list.size());
		assertEquals(String.class, list.get(0));
		
		list.add(Double.class);
		assertEquals(String.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		assertEquals(2,list.size());

		list.add(NullPointerException.class);
		assertEquals(3,list.size());
		assertEquals(String.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		assertEquals(NullPointerException.class, list.get(2));
		
		list.add(Exception.class);
		assertEquals(4,list.size());
		assertEquals(String.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		assertEquals(NullPointerException.class, list.get(2));
		assertEquals(Exception.class, list.get(3));

		list.add(Object.class);
		assertEquals(5,list.size());
		assertEquals(String.class, list.get(0));
		assertEquals(Double.class, list.get(1));
		assertEquals(NullPointerException.class, list.get(2));
		assertEquals(Exception.class, list.get(3));
		assertEquals(Object.class, list.get(4));
	}
	
	public void testAdd_OrderThree() {
		
		SortedClassVector list = new SortedClassVector();
		
		list.add(Object.class);
		list.add(Exception.class);
		list.add(String.class);		
		list.add(NullPointerException.class);
		list.add(Double.class);
		
		assertEquals(5,list.size());
		assertEquals(NullPointerException.class, list.get(0));
		assertEquals(Exception.class, list.get(1));
		assertEquals(String.class, list.get(2));
		assertEquals(Double.class, list.get(3));
		assertEquals(Object.class, list.get(4));
	}

	public void testAdd_Exceptions() {
		
		SortedClassVector list = new SortedClassVector();
		
		list.add(Exception.class);
		list.add(NullPointerException.class);
		list.add(RestFlowException.class);
		list.add(ActorException.class);
		list.add(ClassNotFoundException.class);
		list.add(IllegalWorkflowSpecException.class);
		list.add(NullOutputException.class);
		
		assertEquals(7,list.size());

		assertEquals(NullPointerException.class, list.get(0));
		assertEquals(NullOutputException.class, list.get(1));
		assertEquals(ActorException.class, list.get(2));
		assertEquals(IllegalWorkflowSpecException.class, list.get(3));
		assertEquals(RestFlowException.class, list.get(4));
		assertEquals(ClassNotFoundException.class, list.get(5));
		assertEquals(Exception.class, list.get(6));
	}
	
	@SuppressWarnings("rawtypes")
	public void testAddAll_Exception() {
		
		List<Class> list = new LinkedList<Class>();
		SortedClassVector classList = new SortedClassVector();
		
		list.add(Exception.class);
		list.add(NullPointerException.class);
		list.add(RestFlowException.class);
		list.add(ActorException.class);
		list.add(ClassNotFoundException.class);
		list.add(IllegalWorkflowSpecException.class);
		list.add(NullOutputException.class);
		
		classList.addAll(list);
		
		assertEquals(7,classList.size());
		assertEquals(NullPointerException.class, classList.get(0));
		assertEquals(NullOutputException.class, classList.get(1));
		assertEquals(ActorException.class, classList.get(2));
		assertEquals(IllegalWorkflowSpecException.class, classList.get(3));
		assertEquals(RestFlowException.class, classList.get(4));
		assertEquals(ClassNotFoundException.class, classList.get(5));
		assertEquals(Exception.class, classList.get(6));
	}
	
	
	public void testGetClosestSuperclass_ExactMatches() {
		
		SortedClassVector list = new SortedClassVector();
		
		list.add(Exception.class);
		list.add(NullPointerException.class);
		list.add(RestFlowException.class);
		list.add(ActorException.class);
		list.add(ClassNotFoundException.class);
		list.add(IllegalWorkflowSpecException.class);
		list.add(NullOutputException.class);
		
		assertEquals(Exception.class, list.getClosestSuperclass(Exception.class));
		assertEquals(NullPointerException.class, list.getClosestSuperclass(NullPointerException.class));
		assertEquals(RestFlowException.class, list.getClosestSuperclass(RestFlowException.class));
		assertEquals(ActorException.class, list.getClosestSuperclass(ActorException.class));
		assertEquals(ClassNotFoundException.class, list.getClosestSuperclass(ClassNotFoundException.class));
		assertEquals(IllegalWorkflowSpecException.class, list.getClosestSuperclass(IllegalWorkflowSpecException.class));
		assertEquals(NullOutputException.class, list.getClosestSuperclass(NullOutputException.class));
	}

	
	public void testGetClosestSuperclass_SuperclassMatches() {
		
		SortedClassVector list = new SortedClassVector();
		
		list.add(Exception.class);
		list.add(NullPointerException.class);
		list.add(RestFlowException.class);
		list.add(ActorException.class);
		
		assertEquals(Exception.class, list.getClosestSuperclass(ClassNotFoundException.class));
		assertEquals(ActorException.class, list.getClosestSuperclass(NullOutputException.class));
		assertEquals(RestFlowException.class, list.getClosestSuperclass(IllegalWorkflowSpecException.class));
	}
	
	
	public void testGetClosestSuperclass_Misses() {
		
		SortedClassVector list = new SortedClassVector();
		
		list.add(RestFlowException.class);
		list.add(ActorException.class);
		list.add(IllegalWorkflowSpecException.class);
		list.add(NullOutputException.class);

		assertNull(list.getClosestSuperclass(NullPointerException.class));
		assertNull(list.getClosestSuperclass(Exception.class));
		assertNull(list.getClosestSuperclass(ClassNotFoundException.class));
	}
}

	
	