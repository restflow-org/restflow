package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.data.SingleResourcePacket;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.NoopTraceRecorder;
import org.restflow.nodes.InPortal;
import org.restflow.nodes.WorkflowNode;
import org.restflow.test.RestFlowTestCase;


public class TestInPortal extends RestFlowTestCase {

	private WorkflowContext 	_context;
	private InPortal 			_portal;
	private MockTraceRecorder 	_recorder;
	private Map<String,String> 	_requestedBindings;
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		_recorder = new MockTraceRecorder();

		_context = new WorkflowContextBuilder()
			.recorder(_recorder)
			.build();

		// create an input portal
		_portal = new InPortal();
		_portal.setApplicationContext(_context);

		// prepare a map for specifying bindings
		_requestedBindings = new HashMap<String,String>();		
	}
	
	private void _setThreeBindings() throws Exception {
		
		// set three bindings on the input port using setBindings()
		_requestedBindings.put("channel1", "/channel1/");		
		_requestedBindings.put("channel2", "/channel2/");		
		_requestedBindings.put("channel3", "/channel3/");		
		_portal.setOutflows(_requestedBindings);
		_portal.elaborate();
		_portal.configure();
	}

	private void _setValues() throws Exception {

		// set values for bindings using three distinct types
		_portal.setInputValue("channel1", 1 );
		_portal.setInputValue("channel2", 3.45 );
		_portal.setInputValue("channel3", "A string" );
	}

	
	public void testSetBindings() throws Exception {

		// check that there are initially no bindings on an input portal
		Map<String,Outflow> beforeBindings = _portal.getOutflows();
		assertEquals(0, beforeBindings.size());
		
		// set the portal bindings
		_setThreeBindings();
		
		// verify that there are three bindings
		Map<String,Outflow> afterBindings = _portal.getOutflows();
		assertEquals(3, afterBindings.size());
	}
	
	public void testGetBindings() throws Exception {
		
		// set the portal bindings
		_setThreeBindings();
		
		// verify that the three bindings are those that were requested
		Map<String,Outflow> outflows = _portal.getOutflows();
		assertEquals("/channel1", outflows.get("channel1").getBinding());
		assertEquals("/channel2", outflows.get("channel2").getBinding());
		assertEquals("/channel3", outflows.get("channel3").getBinding());
	}
	
	public void testSetInputValueForLabel() throws Exception {

		// set the portal bindings
		_setThreeBindings();

		// initialize the portal with these bindings
		_portal.initialize();
		
		// set value for each portal channel
		_setValues();
	}
	
	public void testGetOutputValue() throws Exception {

		// set the port bindings
		_setThreeBindings();

		// initialize the port with these bindings
		_portal.initialize();

		// set values for bindings using three distinct types
		_setValues();
		
		_portal.trigger();
		
		// verify that the three values set by label are bound correctly
		Object value = ((SingleResourcePacket)_portal.getOutputPacket("channel1")).getResource().getData();
		assertEquals(1, value);
		assertEquals(3.45, ((SingleResourcePacket)_portal.getOutputPacket("channel2")).getResource().getData());
		assertEquals("A string", ((SingleResourcePacket)_portal.getOutputPacket("channel3")).getResource().getData());
	}
	
	public void testTrigger() throws Exception {
		
		// set the port bindings
		_setThreeBindings();

		// initialize the port with these bindings
		_portal.initialize();

		// set values for bindings using three distinct types
		_setValues();
		
		// test that mock trace recorder is empty
		assertEquals(0, _recorder.outputsByBinding.size());
		
		// test that no firings have occurred yet
		assertEquals(0, _recorder.firingCount);
		
		// trigger the port
		assertEquals(ChangedState.TRUE,_portal.trigger());
		
		// test that one firing has been recorded
		assertEquals(1, _recorder.firingCount);

		// test that there are now three output records
		int size = _recorder.outputsByBinding.size();
		assertEquals(3, size);
		
		// check the contents of the output records
		assertEquals(1, _recorder.outputsByBinding.get("/channel1") );
		assertEquals(3.45, _recorder.outputsByBinding.get("/channel2"));
		assertEquals("A string", _recorder.outputsByBinding.get("/channel3") );
	}
	
	public void testOneShotPerRunProperty() throws Exception {

		// initialize the portal with no bindings
		_portal.initialize();
		
		// test that no firings have occurred yet
		assertEquals(0, _recorder.firingCount);
		
		// trigger the port
		assertEquals(ChangedState.TRUE, _portal.trigger());
		
		// test that one firing has been recorded
		assertEquals(1, _recorder.firingCount);
		
		// try to trigger the portal again
//		assertFalse(_portal.trigger());

		// test that no additional firing occurred
		assertEquals(1, _recorder.firingCount);
	
		// reinitialize portal to enable further firings
		_portal.initialize();
		
		// test that the previous firing is still on record
		assertEquals(1, _recorder.firingCount);
		
		// trigger the portal
		assertEquals(ChangedState.TRUE,_portal.trigger());
		
		// test that an additional firing has been recorded
		assertEquals(2, _recorder.firingCount);		
	}
	
	private class MockTraceRecorder extends NoopTraceRecorder {

		public int firingCount = 0;
		public Map<String,Object> outputsByBinding = new HashMap<String,Object>();
		
		public void recordPacketSent(Outflow outflow, Packet packet, Long stepID) throws Exception {
			outputsByBinding.put( ((SingleResourcePacket)packet).getResource().getUri().toString(), 
					((SingleResourcePacket)packet).getResource().getData());
		}

		public void recordStepCompleted(WorkflowNode node) {
			firingCount ++;
		}
	}
}
