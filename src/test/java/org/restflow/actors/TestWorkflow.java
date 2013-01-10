package org.restflow.actors;

import java.util.ArrayList;
import java.util.List;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.Actor.ActorFSM;
import org.restflow.directors.Director;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.TraceRecorder;
import org.restflow.nodes.AbstractWorkflowNode;
import org.restflow.nodes.WorkflowNode;
import org.restflow.test.RestFlowTestCase;


public class TestWorkflow extends RestFlowTestCase {

	private Director 				_director;
	private WorkflowContext 		_context;
	private TraceRecorder			_recorder;
	
	public void setUp() throws Exception {
		super.setUp();
		_context = new WorkflowContextBuilder().build();
		_recorder = _context.getTraceRecorder();
		_director = new PublishSubscribeDirector();
		_director.setApplicationContext(_context);
		_director.afterPropertiesSet();
	}
	
	public void testWorkflowLifecycle_EmptyWorkflow() throws Exception {		

		Workflow workflow = new Workflow();
		workflow.setApplicationContext(_context);
		workflow.setDirector(_director);

		List<WorkflowNode> nodes = new ArrayList<WorkflowNode>();
		workflow.setNodes(nodes);
		
		assertEquals(ActorFSM.CONSTRUCTED, workflow._state);

		workflow.afterPropertiesSet();
		assertEquals(ActorFSM.PROPERTIES_SET, workflow._state);
		
		workflow.elaborate();
		workflow.configure();
		
		_recorder.recordWorkflowGraph(workflow);
		
		assertEquals(ActorFSM.CONFIGURED, workflow._state);
		assertEquals(0, workflow.getNodes().size());
		assertEquals(0, workflow.getSinks().size());

		workflow.initialize();
		assertEquals(ActorFSM.INITIALIZED, workflow._state);

		workflow.step();
		assertEquals(ActorFSM.STEPPED, workflow._state);

		workflow.wrapup();
		assertEquals(ActorFSM.WRAPPED_UP, workflow._state);
		
		workflow.dispose();
		assertEquals(ActorFSM.DISPOSED, workflow._state);
	}
	
	public void testWorkflowLifecycle_OneNodeWorkflow() throws Exception {		

		Workflow workflow = new Workflow();
		workflow.setApplicationContext(_context);
		workflow.setDirector(_director);
		
		List<WorkflowNode> nodes = new ArrayList<WorkflowNode>();
		WorkflowNode node = new TestNode();
		nodes.add(node);
		workflow.setNodes(nodes);
		
		assertEquals(ActorFSM.CONSTRUCTED, workflow._state);

		workflow.afterPropertiesSet();
		assertEquals(ActorFSM.PROPERTIES_SET, workflow._state);
		
		workflow.elaborate();
		workflow.configure();
		
		_recorder.recordWorkflowGraph(workflow);
		
		assertEquals(ActorFSM.CONFIGURED, workflow._state);
		assertEquals(1, workflow.getNodes().size());
		assertEquals(1, workflow.getSinks().size());

		workflow.initialize();
		assertEquals(ActorFSM.INITIALIZED, workflow._state);

		workflow.step();
		assertEquals(ActorFSM.STEPPED, workflow._state);

		workflow.wrapup();
		assertEquals(ActorFSM.WRAPPED_UP, workflow._state);
		
		workflow.dispose();
		assertEquals(ActorFSM.DISPOSED, workflow._state);
	}
	
	public void testClone_EmptyWorkflow() throws Exception {		

		Workflow workflow = new Workflow();
		workflow.setApplicationContext(_context);
		workflow.setDirector(_director);

		assertEquals(ActorFSM.CONSTRUCTED, workflow._state);
		
		List<WorkflowNode> nodes = new ArrayList<WorkflowNode>();
		workflow.setNodes(nodes);
		
		workflow.afterPropertiesSet();
		assertEquals(ActorFSM.PROPERTIES_SET, workflow._state);
		
		workflow.elaborate();
		workflow.configure();
		assertEquals(ActorFSM.CONFIGURED, workflow._state);
		assertEquals(0, workflow.getNodes().size());
		assertEquals(0, workflow.getSinks().size());

		workflow.initialize();
		assertEquals(ActorFSM.INITIALIZED, workflow._state);

		Workflow clone = (Workflow)workflow.clone();
		assertNotSame(workflow, clone);
		assertEquals(ActorFSM.INITIALIZED, workflow._state);
		assertEquals(ActorFSM.CONSTRUCTED, clone._state);
	}
	
	
	public void testClone_OneNodeWorkflow() throws Exception {		

		Workflow workflow = new Workflow();
		workflow.setApplicationContext(_context);
		workflow.setDirector(_director);

		assertEquals(ActorFSM.CONSTRUCTED, workflow._state);
		
		List<WorkflowNode> nodes = new ArrayList<WorkflowNode>();
		WorkflowNode node = new TestNode();
		nodes.add(node);
		workflow.setNodes(nodes);
		
		workflow.afterPropertiesSet();
		assertEquals(ActorFSM.PROPERTIES_SET, workflow._state);
		
		workflow.elaborate();
		workflow.configure();
		assertEquals(ActorFSM.CONFIGURED, workflow._state);
		assertEquals(1, workflow.getNodes().size());
		assertEquals(1, workflow.getSinks().size());

		workflow.initialize();
		assertEquals(ActorFSM.INITIALIZED, workflow._state);

		Workflow clone = (Workflow)workflow.clone();
		assertNotSame(workflow, clone);
		assertEquals(ActorFSM.INITIALIZED, workflow._state);
		assertEquals(ActorFSM.CONSTRUCTED, clone._state);
	}
		
	
	public static class TestNode extends AbstractWorkflowNode {
	
		public boolean readyForInputPacket(String label) throws Exception {
			return false;
		}
	
		public ChangedState trigger() throws Exception {
			return null;
		}
	}	
}
