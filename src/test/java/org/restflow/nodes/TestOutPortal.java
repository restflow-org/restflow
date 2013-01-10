package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.data.Inflow;
import org.restflow.data.SingleResourcePacket;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.NoopTraceRecorder;
import org.restflow.metadata.TraceRecorder;
import org.restflow.nodes.OutPortal;
import org.restflow.nodes.WorkflowNode;
import org.restflow.test.RestFlowTestCase;


public class TestOutPortal extends RestFlowTestCase {

	private WorkflowContext		_context;
	private OutPortal 			_portal;
	private TraceRecorder 		_recorder;
	private Map<String,Object> 	_requestedBindings;
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		_recorder = new MockTraceRecorder();

		_context = new WorkflowContextBuilder()
			.recorder(_recorder)
			.build();

		// create an output portal
		_portal = new OutPortal();
		_portal.setApplicationContext(_context);
		
		// prepare a map for specifying bindings
		_requestedBindings = new HashMap<String,Object>();		
	}
	
	private void _setThreeBindings() throws Exception {
		
		// set three bindings on the input port using setBindings()
		_requestedBindings.put("channel1", "/channel1/");		
		_requestedBindings.put("channel2", "/channel2/");		
		_requestedBindings.put("channel3", "/channel3/");		
		_portal.setInflows(_requestedBindings);
		_portal.elaborate();
		_portal.configure();
	}

	private void _setInputs() throws Exception {

		// set values for bindings using three distinct types
		_portal.setInputPacket("channel1", new SingleResourcePacket(1));
		_portal.setInputPacket("channel2", new SingleResourcePacket(3.45));
		_portal.setInputPacket("channel3", new SingleResourcePacket("A string"));
	}

	
	public void testSetBindings() throws Exception {

		// check that there are initially no bindings on an output port
		Collection<Inflow> beforeBindings = _portal.getNodeInflows();
		assertEquals(0, beforeBindings.size());
		
		// set the portal bindings
		_setThreeBindings();
		
		// verify that there are three bindings
		Collection<Inflow> afterBindings = _portal.getNodeInflows();
		assertEquals(3, afterBindings.size());
	}
	
	public void testGetBindings() throws Exception {
		
		// set the port bindings
		_setThreeBindings();
		
		// verify that the three bindings are those that were requested
		Map<String,Inflow> bindings = _portal.getLabelInflowMap();
		assertEquals("/channel1", bindings.get("channel1").getBinding());
		assertEquals("/channel2", bindings.get("channel2").getBinding());
		assertEquals("/channel3", bindings.get("channel3").getBinding());
	}
	
	public void testSetInputValue() throws Exception {

		// set the portal bindings
		_setThreeBindings();

		// initialize the portal with these bindings
		_portal.initialize();
		
		// set value for each port channel
		_setInputs();
	}
	
	public void testTrigger() throws Exception {
		
		// set the portal bindings
		_setThreeBindings();

		// initialize the portal with these bindings
		_portal.initialize();

		// set values for bindings using three distinct types
		_setInputs();
		
		// test that no firings have occurred yet
		assertEquals(0, ((MockTraceRecorder)_recorder).firingCount);
		
		// trigger the portal
		assertEquals(ChangedState.TRUE,_portal.trigger());
		
		// test that one firing has been recorded
		assertEquals(1, ((MockTraceRecorder)_recorder).firingCount);

		// verify that the three values set by label are bound correctly
		SingleResourcePacket val = (SingleResourcePacket)_portal.getOutputPacket("channel1");
		Object contents = val.getResource().getData();
		assertEquals(1, contents);
		assertEquals(3.45, ((SingleResourcePacket)_portal.getOutputPacket("channel2")).getResource().getData());
		assertEquals("A string", ((SingleResourcePacket)_portal.getOutputPacket("channel3")).getResource().getData());
	}
	
	private class MockTraceRecorder extends NoopTraceRecorder {

		public int firingCount = 0;
		
		public void recordStepCompleted(WorkflowNode node) {
			firingCount ++;
		}
	}
}

	
