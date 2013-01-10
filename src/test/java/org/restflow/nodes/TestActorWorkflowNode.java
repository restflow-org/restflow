package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.JavaActor;
import org.restflow.data.ControlProtocol;
import org.restflow.exceptions.IllegalWorkflowSpecException;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.test.RestFlowTestCase;


public class TestActorWorkflowNode extends RestFlowTestCase {

	public void testSetInflows_MatchedInflowAndInputLabels_DataProtocol() throws Exception {
		
		WorkflowContext context = new WorkflowContextBuilder()
			.scheme("control", new ControlProtocol())
			.build();

		TestActorBean bean = new TestActorBean();
		
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("input", null);
		
		JavaActor actor = new JavaActor();
		actor.setName("TestActor");
		actor.setWrappedBean(bean);
		actor.setInputs(inputs);
		actor.setApplicationContext(context);
		actor.afterPropertiesSet();
		
		ActorWorkflowNode node = new ActorWorkflowNode();
		node.setName("TestNode");
		node.setActor(actor);
		node.setApplicationContext(context);
			
		Map<String, Object> inflowConfiguration = new HashMap<String,Object>();
		inflowConfiguration.put("input", "/input");
		
		node.setInflows(inflowConfiguration);
		node.elaborate();
		node.configure();
		node.initialize();		
	}

	public void testSetInflows_MismatchedInflowAndInputLabels_DataProtocol() throws Exception {
		
		WorkflowContext context = new WorkflowContextBuilder()
			.scheme("control", new ControlProtocol())
			.build();

		TestActorBean bean = new TestActorBean();
		
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("input", null);
		
		JavaActor actor = new JavaActor();
		actor.setName("TestActor");
		actor.setWrappedBean(bean);
		actor.setInputs(inputs);
		actor.setApplicationContext(context);
		actor.afterPropertiesSet();
		
		ActorWorkflowNode node = new ActorWorkflowNode();
		node.setName("TestNode");
		node.setActor(actor);
		node.setApplicationContext(context);
			
		Map<String, Object> inflowConfiguration = new HashMap<String,Object>();
		inflowConfiguration.put("inputTwo", "/input");
		
		node.setInflows(inflowConfiguration);
		
		String message = null;
		try {
			node.elaborate();
		} catch(IllegalWorkflowSpecException e) {
			message = e.getMessage();
		}
		assertEquals("Actor TestNode.TestActor does not accept input variable 'inputTwo'.", message);
	}

	public void testSetInflows_MismatchedInflowAndInputLabels_ControlProtocol() throws Exception {
		
		WorkflowContext context = new WorkflowContextBuilder()
		.scheme("control", new ControlProtocol())
		.build();

		TestActorBean bean = new TestActorBean();
		
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("input", null);
		
		JavaActor actor = new JavaActor();
		actor.setName("TestActor");
		actor.setWrappedBean(bean);
		actor.setInputs(inputs);
		actor.setApplicationContext(context);
		actor.afterPropertiesSet();
		
		ActorWorkflowNode node = new ActorWorkflowNode();
		node.setName("TestNode");
		node.setActor(actor);
		node.setApplicationContext(context);
		
		Map<String, Object> inflowConfiguration = new HashMap<String,Object>();
		inflowConfiguration.put("controlOne", "control:/inputTwo");
		
		node.setInflows(inflowConfiguration);
		node.elaborate();
		node.configure();
		node.initialize();
	}

	public void testSetInflows_MatchedInflowAndInputLabels_ControlProtocol() throws Exception {
		
		WorkflowContext context = new WorkflowContextBuilder()
		.scheme("control", new ControlProtocol())
		.build();

		TestActorBean bean = new TestActorBean();
		
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("input", null);
		
		JavaActor actor = new JavaActor();
		actor.setName("TestActor");
		actor.setWrappedBean(bean);
		actor.setInputs(inputs);
		actor.setApplicationContext(context);
		actor.afterPropertiesSet();
		
		ActorWorkflowNode node = new ActorWorkflowNode();
		node.setName("TestNode");
		node.setActor(actor);
		node.setApplicationContext(context);

		Map<String, Object> inflowConfiguration = new HashMap<String,Object>();
		inflowConfiguration.put("input", "control:/input");
		
		node.setInflows(inflowConfiguration);
		
		String message = null;
		try {
			node.elaborate();
		} catch(IllegalWorkflowSpecException e) {
			message = e.getMessage();
		}
		assertEquals("Control protocol may not be used on TestNode inflow 'input', " +
				"because actor TestNode.TestActor has an input with the same name.", message);
	}	
	
	private class TestActorBean {}
}