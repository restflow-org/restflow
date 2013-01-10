package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.enums.ChangedState;
import org.restflow.nodes.AbstractWorkflowNode;
import org.restflow.test.RestFlowTestCase;


public class TestAbstractWorkflowNode extends RestFlowTestCase {

	private WorkflowContext 		_context;
	private AbstractWorkflowNode 	_node;
	
	public void setUp() throws Exception {
		super.setUp();
		_context = new WorkflowContextBuilder().build();
		_node = new AbstractWorkflowNodeImp();
		_node.setApplicationContext(_context);
		_node.setName("TestNode");
}
	
	public void testSetExceptions_OneExceptionOutflow() throws Exception {
		
		Map<String,String> exceptionConfiguration = new HashMap<String,String>();
		exceptionConfiguration.put("java.lang.NullPointerException", "/nullPointerException");
		_node.setExceptions(exceptionConfiguration);
		_node.elaborate();
		_node.configure();
		
		assertEquals(1, _node.getOutflows().size());
		assertEquals(0, _node.getDataOuflowNames().size());
		
		assertEquals("/nullPointerException", _node.getOutflows().get("java.lang.NullPointerException").getBinding());
		assertEquals(1, _node._caughtExceptions.size());
	}

	public void testSetExceptions_ClassNotFound() throws Exception {
		
		Map<String,String> exceptionConfiguration = new HashMap<String,String>();
		exceptionConfiguration.put("NotAnException", "/notAnException");
		_node.setExceptions(exceptionConfiguration);

		Exception exception = null;
		try {
			_node.elaborate();
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("Error registering actor exception handler for node TestNode. " + 
						"No such class NotAnException", exception.getMessage());
				exception.getMessage();
	}

	
	private class AbstractWorkflowNodeImp extends AbstractWorkflowNode {
		public boolean readyForInputPacket(String label) throws Exception { return false; }
		public ChangedState trigger() throws Exception { return ChangedState.FALSE; }
	}
}
