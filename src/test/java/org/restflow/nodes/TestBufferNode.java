package org.restflow.nodes;

import java.util.Map;
import java.util.HashMap;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.data.Outflow;
import org.restflow.data.SingleResourcePacket;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.NoopTraceRecorder;
import org.restflow.nodes.AbstractWorkflowNode;
import org.restflow.nodes.BufferNode;
import org.restflow.test.RestFlowTestCase;


public class TestBufferNode extends RestFlowTestCase {

	private WorkflowContext 	_context;
	private WorkflowContext 	_noopContext;
	
	public void setUp() throws Exception {
		super.setUp();
		
		_context = new WorkflowContextBuilder().build();
		
		_noopContext = new WorkflowContextBuilder()
			.recorder(new NoopTraceRecorder())
			.build();
	}
	
	
	public void testDefaultConstructor() throws Exception {
		
		// create a buffer node using the default constructor
		BufferNode node = new BufferNode();
		node.setApplicationContext(_context);

		// check that the node is not hidden by default
		assertFalse(node.isHidden());
	}
	
	public void testCreateHiddenBufferNode() throws Exception {

		// create a hidden buffer using the factory
		BufferNode node = BufferNode.createHiddenBufferNode("node", "label");
		node.setApplicationContext(_context);
		
		// check that the provided name was assigned
		assertEquals("BufferNode-for-node-label", node.getName());
		
		// check that the node is hidden
		assertTrue(node.isHidden());
		
		// check that an empty inflows map has been assigned
		assertNotNull(node.getInflows());
		assertEquals(0, node.getInflows().size());

		// check that an empty outflows map has been assigned
		assertNotNull(node.getOutflows());
		assertEquals(0, node.getOutflows().size());
	}
	
	public void testRegisterInflow() throws Exception {
		
		// create a buffer node using the default constructor
		BufferNode node = new BufferNode();
		node.setApplicationContext(_context);

		// register an inflow
		node.registerInflow("Input", "/input", false);
		assertEquals(1, node.getInflows().size());
		assertTrue(node.getInflows().containsKey("Input"));
		assertEquals("/input", node.getInflows().get("Input").getBinding());
		
		// trigger exception by registering a second inflow
		String errorMessage = null;
		try { node.registerInflow("AnotherInput", "/another", false); } catch(Exception e) { 
			errorMessage = e.getMessage(); 
		}
		assertEquals("Cannot register more than inflow for a buffer node.", errorMessage);
	}
	
	public void testRegisterOutflow() throws Exception {
		
		// create a buffer node using the default constructor
		BufferNode node = new BufferNode();
		node.setApplicationContext(_context);

		// register an outflow
		node.registerOutflow("Output", "/output", false);
		assertEquals(1, node.getOutflows().size());
		assertTrue(node.getOutflows().containsKey("Output"));
		assertEquals("/output", node.getOutflows().get("Output").getBinding());
		
		// trigger exception by registering a second inflow
		String errorMessage = null;
		try { node.registerOutflow("AnotherOutput", "/another", false); } catch(Exception e) { 
			errorMessage = e.getMessage(); 
		}
		assertEquals("Cannot register more than outflow for a buffer node.", errorMessage);
	}
	
//	public void testValidate() throws Exception {
//		
//		// create a hidden buffer using the factory
//		BufferNode node = BufferNode.createHiddenBufferNode("buffer");
//		node.setProtocolRegistry(_protocolRegistry);
//		
//		String errorMessage = null;		
//
//		try { node.validate(); } catch(Exception e) { 
//			errorMessage = e.getMessage(); 
//		}
//		assertEquals("Buffer node must have one inflow registered.", errorMessage);
//		
//		node.registerInflow("Input", "/input");
//		
//		try { node.validate(); } catch(Exception e) { 
//			errorMessage = e.getMessage(); 
//		}
//		assertEquals("Buffer node must have one outflow registered.", errorMessage);
//
//		node.registerOutflow("Input", "/input", false);
//
//		node.validate();
//	}
	
	public void testCustomBufferNode() throws Exception {
		
		// create a buffer node using the default constructor
		BufferNode node = new BufferNode();
		node.setApplicationContext(_context);

		Map<String,Object> inflows = new HashMap<String, Object>();
		inflows.put("CustomInput", "/custom/input");
		node.setInflows(inflows);
		
		Map<String,Object> outflows = new HashMap<String,Object>();
		outflows.put("CustomOutput", "/custom/output");
		node.setOutflows(outflows);
		
		node.configure();
		node.validate();
	}

	public void testLifeCycle() throws Exception {
				
		// create a hidden buffer using the factory
		BufferNode node = BufferNode.createHiddenBufferNode("node", "label");
		node.setApplicationContext(_noopContext);

		// register and inflow and outflow
		node.registerInflow("Input", "/input", false);
		node.registerOutflow("Output", "/output", false);
		
		// get handles to the inflow and outflow
		Outflow outflow = node.getOutflows().get("Output");
		
		// initialize the buffer node
		node.initialize();

		// check that node is ready for input
		assertTrue(node.readyForInputPacket("Input"));

		// check that the outflow has no packet
		assertFalse(outflow.packetReady());

		// check that the node is not ready to step 
		assertEquals( ChangedState.FALSE,node.trigger());
		
		// give the node one input packet
		node.setInputPacket("Input", new SingleResourcePacket(1));
		
		// check that the node is still ready for input
		assertTrue(node.readyForInputPacket("Input"));
		
		// trigger the node
		assertEquals(ChangedState.TRUE, node.trigger());
		
		// check that the outflow now has a packet
		assertTrue(outflow.packetReady());
		
		// check that the node cannot be triggered now
		assertEquals(ChangedState.FALSE,node.trigger());

		// check that the node still is ready for input
		assertTrue(node.readyForInputPacket("Input"));
		
		// read the first buffered packet from the node
		assertEquals(1, ((SingleResourcePacket)(node.getOutputPacket("Output"))).getResource().getData());
		
		// give the node two more packets
		node.setInputPacket("Input", new SingleResourcePacket(2));
		node.setInputPacket("Input", new SingleResourcePacket(3));

		// trigger the node
		assertEquals(ChangedState.TRUE,node.trigger());
		
		// check that node cannot step now
		assertEquals(ChangedState.FALSE,node.trigger());

		// read the second buffered packet from the node
		assertEquals(2, ((SingleResourcePacket)(node.getOutputPacket("Output"))).getResource().getData());
		
		// trigger the node
		assertEquals(ChangedState.TRUE, node.trigger());
		
		// read the third buffered packet from the node
		assertEquals(3, ((SingleResourcePacket)(node.getOutputPacket("Output"))).getResource().getData());
		
		// give the node and of stream packet
		node.setInputPacket("Input", AbstractWorkflowNode.EndOfStreamPacket);

		// check that node will accept no more input
		assertFalse(node.readyForInputPacket("Input"));

		// trigger the node
		assertEquals(ChangedState.TRUE,node.trigger());		
		
		// read the end of stream packet from the node
		assertEquals(AbstractWorkflowNode.EndOfStreamPacket, node.getOutputPacket("Output"));

		// check that node still will accept no more input
		assertFalse(node.readyForInputPacket("Input"));
	}
}