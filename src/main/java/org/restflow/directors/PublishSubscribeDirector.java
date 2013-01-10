package org.restflow.directors;

import java.util.List;

import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.enums.ChangedState;
import org.restflow.enums.WorkflowModified;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.Contract;


public class PublishSubscribeDirector extends AbstractDirector {

	///////////////////////////////////////////////////////////////////////////
	////                    private instance fields                        ////

	private Publisher 	_publisher;
	
	
	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public PublishSubscribeDirector() {
		super();
		_state = DirectorFSM.CONSTRUCTED;
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

		_publisher = new BufferedPublisher();		
		_publisher.setWorkflow(_workflow);

		// subscribe all workflow nodes for their input bindings
		for (Inflow inflow : _inflowToOutflowsMap.keySet()) {
			List<Outflow> outflows = _inflowToOutflowsMap.get(inflow);
			for (Outflow outflow: outflows) {
				_publisher.subscribe(inflow.getNode(), inflow.getLabel(), outflow);
			}
		}
		
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

	public void run() throws Exception {
		
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.WRAPPED_UP);

		super.run();
		
		boolean workflowRunning;
		
		_publisher.clearPacketBuffers();

		_state = DirectorFSM.RUNNING;
		
		do {
			workflowRunning = false;

			// loop over workflow nodes
			for (WorkflowNode node : _nodes) {
								
				if (node.trigger() == ChangedState.TRUE) {
					_publishNodeOutputPackets(node);
					workflowRunning = true;
				} else if (_publisher.flushPacketToNode(node)) {
					workflowRunning = true;
				}			
			}
		} while (workflowRunning);

		_state = DirectorFSM.RAN;
	}
	
	public WrapupResult wrapup() throws Exception {
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.RAN);
		super.wrapup();
		WrapupResult result = _publisher.wrapup();
		_state = DirectorFSM.WRAPPED_UP; 
		return result;
	}	

	public void dispose() throws Exception {
		super.dispose();
		Contract.requires(_state == DirectorFSM.WRAPPED_UP);
		_state = DirectorFSM.DISPOSED;
	}

	///////////////////////////////////////////////////////////////////////////
	////             publisher callback methods                         ////
	
	public boolean nodeReadyForPacketOnLabel(WorkflowNode node, String label) throws Exception {
		Contract.requires(_state == DirectorFSM.RUNNING);
		return node.readyForInputPacket(label);
	}
	
	public void update(WorkflowNode node, String label, Packet packet)
			throws Exception {

		Contract.requires(_state == DirectorFSM.RUNNING);
		
		// apply the value to the receiving node
		node.setInputPacket(label, packet);
		
		// attempt to step the receiving node
		ChangedState nodeStepped = node.trigger();

		// publish the node's outputs if it stepped
		if (nodeStepped == ChangedState.TRUE) {
			 _publishNodeOutputPackets(node);
		}			
	}
	
	///////////////////////////////////////////////////////////////////////////
	////                   private helper methods                          ////

	private void _publishNodeOutputPackets(WorkflowNode node) throws Exception {

		Contract.requires(_state == DirectorFSM.RUNNING);

		// TODO: redefine node.getOutflows() to return list of outflows, not the original map
		for (Outflow outflow : node.getOutflows().values()) {
			if (outflow.packetReady()) {
				Packet outputPacket = node.getOutputPacket(outflow.getLabel());
				if (outputPacket != null ) {
					_publisher.publish(outflow, outputPacket);
				}
			}
		}
	}
}
