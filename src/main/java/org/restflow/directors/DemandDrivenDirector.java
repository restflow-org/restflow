package org.restflow.directors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.directors.Director.DirectorFSM;
import org.restflow.enums.WorkflowModified;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.Contract;


public class DemandDrivenDirector extends AbstractDirector {

	///////////////////////////////////////////////////////////////////////////
	////              private singleton instance fields                    ////

	private int 		_firingCount;

	
	///////////////////////////////////////////////////////////////////////////
	////              private collection instance fields                   ////

	private Map<WorkflowNode, Boolean> _nodeHasStepped;
	private List<WorkflowNode> 		   _sinks;

	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public DemandDrivenDirector() {
		super();
		
		_firingCount = 1;
		
		_state = DirectorFSM.CONSTRUCTED;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///   workflow configuration setters -- PROPERTIES_UNSET or UNCONFIGURED state only    ////
	
	public void setFiringCount(int firingCount) {
		Contract.requires(_state == DirectorFSM.CONSTRUCTED || _state == DirectorFSM.PROPERTIES_SET);
		_firingCount = firingCount;
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
		
		_sinks = _workflow.getSinks();
		_nodeHasStepped = new HashMap<WorkflowNode, Boolean>();
		
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
		
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.RAN);

		super.run();

		_state = DirectorFSM.RUNNING;
		
		for (int i = 0; i < _firingCount; i++) {
			_resetSteppingRecord();
			for (WorkflowNode node : _sinks) {
				_activateNode(node);
			}
		}

		_resetSteppingRecord();

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

	private void _activateNode(WorkflowNode node) throws Exception {
	
		Contract.requires(_state == DirectorFSM.RUNNING);
	
		// loop over all inflows to the node
		for (Inflow inflow : node.getNodeInflows()) {
			
			// look up outflow connected to inflow
			// TODO rename to upstreamOutflow
			List<Outflow> outflows = _inflowToOutflowsMap.get(inflow);
	
			for (Outflow outflow: outflows) {
				// look up the upstream node that publishes this outflow
				WorkflowNode upstreamNode = outflow.getNode();
	
				// activate the upstream node if it hasn't been already for this round
				if (!_nodeHasStepped.get(upstreamNode)) {
					_activateNode(upstreamNode);
					_nodeHasStepped.put(upstreamNode, true);
				}
	
				Packet packet = outflow.peek();
				
				if (packet != null) {
					node.setInputPacket(inflow.getLabel(), packet);
				}
			}
		}
		
		node.trigger();
	}

	private void _resetSteppingRecord() throws Exception {

		Contract.requires(_state == DirectorFSM.RUNNING);

		for (WorkflowNode node : _nodes) {
			_nodeHasStepped.put(node, false);
			for (Outflow outflow : node.getOutflows().values()) {
				outflow.clear();
			}
		}
	}
}
