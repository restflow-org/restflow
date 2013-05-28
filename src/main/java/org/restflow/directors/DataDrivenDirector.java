package org.restflow.directors;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.restflow.data.Inflow;
import org.restflow.data.InflowToOutflowsMap;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.enums.ChangedState;
import org.restflow.enums.WorkflowModified;
import org.restflow.nodes.NodeInput;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.BufferNode;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.Contract;


public class DataDrivenDirector extends AbstractDirector {

	///////////////////////////////////////////////////////////////////////////
	////                    private instance fields                        ////

	private OutflowSubscriptions _outflowSubscriptions;
	
	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public DataDrivenDirector() {
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

		Contract.requires(_state == DirectorFSM.PROPERTIES_SET || _state == DirectorFSM.MODIFIED);

		super.elaborate();

		if (_state == DirectorFSM.PROPERTIES_SET) {
		
			_insertBufferNodes();
			
			_state = DirectorFSM.MODIFIED;
			
			return WorkflowModified.TRUE;

		} else {
			
			// create subscriptions for inflows on corresponding outflows
			_outflowSubscriptions = new OutflowSubscriptions(_inflowToOutflowsMap);
		
			_state = DirectorFSM.ELABORATED;
			
			return WorkflowModified.FALSE;
		}
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
		
		_state = DirectorFSM.RUNNING;
		
		boolean workflowRunning;
		
		// execute outer loop of workflow run at least once
		do {

			// workflow has stopped unless one or more workflow nodes
			// steps or publishes its outputs within the inner loop below
			workflowRunning = false;
			
			// inner loop over nodes in the workflow graph
			for (WorkflowNode node : _nodes) {

				// attempt to step the current node
				if (node.trigger()== ChangedState.TRUE ) {
					workflowRunning = true;
				}
				
				// attempt to publish the current node's outputs
				// regardless of whether the node was just stepped above
				if (node.outputsReady() && _publishOutputs(node)) {
					workflowRunning = true;
				}
			}
			
		// repeat outer loop for as long as workflow is running
		} while (workflowRunning);
		
		_state = DirectorFSM.RAN;
	}

	public WrapupResult wrapup() throws Exception {
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.RAN);
		super.wrapup();
		WrapupResult result =_checkBufferNodesForUnusedData();
		_state = DirectorFSM.WRAPPED_UP;			
		return result;
	}
	
	private WrapupResult _checkBufferNodesForUnusedData() {
		
		List<WrapupResult.UnusedDataRecord> unusedDataRecordList = 
			new LinkedList<WrapupResult.UnusedDataRecord>();

		for (WorkflowNode node : _nodes) {
			if (node instanceof BufferNode) {
			
				BufferNode bufferNode = (BufferNode)node;
				int queueSize = bufferNode.getQueueSize();
				if  (queueSize > 0) {
					
					List<Packet> unusedPackets = bufferNode.getQueueContents();
					
					unusedDataRecordList.add(
							new WrapupResult.UnusedDataRecord(
									bufferNode.getName(),
									bufferNode.getBufferedInflowLabel(),
									"queue",
									unusedPackets
							)
					);
				}
			}
		}
		
		return new WrapupResult(unusedDataRecordList);
	}
	
	
	public void dispose() throws Exception {
		super.dispose();
		Contract.requires(_state == DirectorFSM.WRAPPED_UP);
		_state = DirectorFSM.DISPOSED;
	}
	
	///////////////////////////////////////////////////////////////////////////
	////                   private helper methods                          ////
	
	private boolean _allReceiversReady(List<NodeInput> subscriptions) throws Exception {
	
		Contract.requires(_state == DirectorFSM.RUNNING);
	
		// make sure all receivers are ready
		for (NodeInput nodeInput : subscriptions ) {
			WorkflowNode node = nodeInput.getNode();
			String inputLabel = nodeInput.getInputLabel();
			if (node.readyForInputPacket(inputLabel) == false) {
				return false;
			}
		}
	
		return true;
	}

	private void _insertBufferNodes() throws Exception {
	
		Contract.requires(_state == DirectorFSM.PROPERTIES_SET);
	
		InflowToOutflowsMap inflowToOutflowsMap = _workflow.getInflowToOutflowsMap();
		
		// loop over a sorted list of nodes in the workflow specification
		List<Inflow> inflows = new LinkedList<Inflow>(inflowToOutflowsMap.keySet());
		Collections.sort(inflows);
		for (Inflow inflow : inflows) {
			
			BufferNode bufferNode = BufferNode.createHiddenBufferNode(
					inflow.getNode().getName(),
					inflow.getLabel());
			_workflow.insertNodeBeforeInflow(bufferNode, inflow);
		}
	}

	private boolean _publishOutputs(WorkflowNode node) throws Exception {

		Contract.requires(_state == DirectorFSM.RUNNING);

		boolean published = false;

		Map<String, Outflow> outflows = node.getOutflows();

		// loop over all outputs from the node by output label
		for (Outflow outflow : outflows.values()) {

			if (outflow.packetReady()) {
				
				// get the subscriptions for the uri
				List<NodeInput> subscribedNodeInputs = _outflowSubscriptions
						.findAllNodeInputsBoundToOutflow(outflow);
	
				// return immediately if there are not subscribers for the uri
				if (subscribedNodeInputs != null
						&& _allReceiversReady(subscribedNodeInputs)) {
	
					// get the object output to that binding
					Packet packet = node.getOutputPacket(outflow.getLabel());
	
					for (NodeInput nodeInput : subscribedNodeInputs) {
						nodeInput.getNode().setInputPacket(
								nodeInput.getInputLabel(), packet);
						published = true;
					}
				}
			}
		}

		return published;
	}
}