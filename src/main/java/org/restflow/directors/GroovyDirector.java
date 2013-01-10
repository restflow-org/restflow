package org.restflow.directors;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.GuardedBy;

import org.apache.commons.logging.Log;
import org.restflow.data.MultiResourcePacket;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.data.PublishedResource;
import org.restflow.data.SingleResourcePacket;
import org.restflow.directors.Director.DirectorFSM;
import org.restflow.enums.WorkflowModified;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.Contract;


public class GroovyDirector extends AbstractDirector {
    
	/* private fields */
    private OutflowSubscriptions _outflowSubscriptions;
	private String 				 _schedule;

    /* protected fields */
	@GuardedBy("this") protected Log logger;

	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public GroovyDirector() {
		super();
		_outflowSubscriptions = null;
		_state = DirectorFSM.CONSTRUCTED;
	}
	
	///////////////////////////////////////////////////////////////////////////
	///     actor configuration setters -- PROPERTIES_UNSET state only     ////

	public void setSchedule(String script) {
		Contract.requires(_state == DirectorFSM.CONSTRUCTED);
		this._schedule = script;
	}

	
	///////////////////////////////////////////////////////////////////////////
	///                   configuration getters                            ////

	public String getSchedule() {
		Contract.disallows(_state == DirectorFSM.CONSTRUCTED);
		return _schedule;
	}

	///////////////////////////////////////////////////////////////////////////
	////              public director lifecycle methods                    ////
	
	public void afterPropertiesSet() {
		Contract.requires(_state == DirectorFSM.CONSTRUCTED);
		super.afterPropertiesSet();
		_state = DirectorFSM.PROPERTIES_SET;
	}
	
	public WorkflowModified elaborate() throws Exception {
		
		Contract.requires(_state == DirectorFSM.PROPERTIES_SET);
		super.elaborate();

		if (_schedule == null) {
			throw new Exception ("Must set the director's schedule script.");
		}

		_outflowSubscriptions = new OutflowSubscriptions(_inflowToOutflowsMap);
		
		_state = DirectorFSM.ELABORATED;
		
		return WorkflowModified.FALSE;
	}
	
	public void configure() throws Exception {
		super.configure();
		_state = DirectorFSM.CONFIGURED;
	}

	
	public void initialize() throws Exception {
		Contract.requires(_state == DirectorFSM.CONFIGURED || _state == DirectorFSM.WRAPPED_UP);
		super.initialize();
		_state = DirectorFSM.INITIALIZED;
	}

	public synchronized void run() throws Exception {

		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.RAN);
		super.run();

		_state = DirectorFSM.RUNNING;

		Binding binding = new Binding();
		_bindNodes(binding);
		
		GroovyShell sh = new GroovyShell( binding );

		Script compiledScript = sh.parse( _schedule );
		
		compiledScript.run();

		_state = DirectorFSM.RAN;
	}

	public WrapupResult wrapup() throws Exception {
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.RAN);
		super.wrapup();
		_state = DirectorFSM.WRAPPED_UP;
		return new WrapupResult();
	}
	
	public void dispose() throws Exception {
		super.dispose();
		Contract.requires(_state == DirectorFSM.WRAPPED_UP);
		_state = DirectorFSM.DISPOSED;
	}

	///////////////////////////////////////////////////////////////////////////
	////                   private helper methods                          ////

	private void _bindNodes(Binding binding) {
		
		Contract.requires(_state == DirectorFSM.RUNNING);

		// inner loop over nodes in the workflow graph
		for (WorkflowNode node : _nodes) {
			NodeWrapperForGroovyDSL gnode = new NodeWrapperForGroovyDSL(node,this);
			binding.setVariable( node.getName(), gnode);
		}
		binding.setVariable("director",this);
	}

	private boolean _publishOutputs(WorkflowNode node) throws Exception {

		Contract.requires(_state == DirectorFSM.RUNNING);

		boolean published = false;

		Map<String, Outflow> outflows = node.getOutflows();

		// loop over all outputs from the node by output label
		for (Outflow outflow : outflows.values()) {

			if (outflow.packetReady()) {
				// get the subscriptions for the uri
				List<NodeInput> subscribedNodeInputs = _outflowSubscriptions.findAllNodeInputsBoundToOutflow( outflow );
	
				// return immediately if there are not subscribers for the uri
				if ( subscribedNodeInputs == null ) continue;
	
				// get the object output to that binding
				Packet packet = ((ActorWorkflowNode)node).peekOutputPacket(outflow.getLabel());
	
				for (NodeInput nodeInput: subscribedNodeInputs ){
					nodeInput.getNode().setInputPacket( nodeInput.getInputLabel(), packet);
					published = true;
				}
			}
		}
		
		return published;
	}

	
	private boolean _allReceiversReady(Map<WorkflowNode, String> subscriptions) throws Exception {

		Contract.requires(_state == DirectorFSM.RUNNING);

		// make sure all receivers are ready
		for (WorkflowNode node : subscriptions.keySet()) {
			
			String label = subscriptions.get(node);
		
			if (node.readyForInputPacket(label) == false) {
				return false;
			}
		}

		return true;
	}
	
	
	private static class PacketWrapperForGroovyDSL {
		
		Packet packet;
		boolean outputEnabled;

		
		public PacketWrapperForGroovyDSL(Packet packet, boolean outputEnabled) {
			super();
			this.packet = packet;
			this.outputEnabled = outputEnabled;
		}


		public Object getValue() throws Exception {
			if (packet == null) return null;
			
			if (packet instanceof MultiResourcePacket) {
				Map<String,Object> outputs = new HashMap<String,Object>();
				for (PublishedResource resource : ((MultiResourcePacket)packet).getResources()) {
					outputs.put( resource.getKey(), resource);
				}
				return outputs;
			}
			
			PublishedResource resource = ((SingleResourcePacket)packet).getResource();
			return resource.getData();			
		}
		
		public Object getReady() throws Exception {	
			return outputEnabled;
		}
	}

	private static class NodeWrapperForGroovyDSL {
		
		WorkflowNode _node;
		GroovyDirector _director;
		public NodeWrapperForGroovyDSL(WorkflowNode node, GroovyDirector director) {
			super();
			_node = node;
			this._director = director;
		}
		
		public void publish() throws Exception {
			_director._publishOutputs(_node);
		}
		
		public void step() throws Exception {
			//_node.trigger();
			_node.manualStart();
			_node.manualFinish();
			if ( _node.outputsReady() ) publish();
		}
		
		public Map<String,Object> getOut() throws Exception {
			Map<String,Object> outputs = new HashMap<String,Object>();
			for (Outflow outflow : _node.getOutflows().values() ) {
				String label = outflow.getLabel();
				Packet packet = outflow.peek();				
				outputs.put(label, new PacketWrapperForGroovyDSL( packet, ((ActorWorkflowNode)_node).getActor().outputEnabled(label) ) );
			}
			return outputs;
		}
		
	}
	
	public static class NodeInput {
		
		private WorkflowNode node;
		private String inputLabel;
		
		public NodeInput(WorkflowNode node, String inputLabel) {
			super();
			this.node = node;
			this.inputLabel = inputLabel;
		}
		public WorkflowNode getNode() {
			return node;
		}
		public void setNode(WorkflowNode node) {
			this.node = node;
		}
		public String getInputLabel() {
			return inputLabel;
		}
		public void setInputLabel(String inputLabel) {
			this.inputLabel = inputLabel;
		}

	}
}