package org.restflow.directors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.restflow.data.Inflow;
import org.restflow.data.InflowToOutflowsMap;
import org.restflow.data.Outflow;
import org.restflow.directors.GroovyDirector.NodeInput;


public class OutflowSubscriptions {
	/* private fields */
	private Map<Outflow,List<NodeInput>> _outflowToSubscriptionsMap;
	
	/*
	private void _recordSubscriptions() throws Exception {
		
		// initialize the mapping between node outflows and node:inflow pairs subscribing to them
		_outflowToSubscriptionsMap = new HashMap<Outflow,Map<WorkflowNode, String>>();
	
		// loop over nodes in the expanded workflow graph
		for (Inflow inflow : _dataflows.keySet()) {

			Outflow outflow = _dataflows.get(inflow);
			_registerReceiver(inflow.getNode(), inflow.getLabel(), outflow);				
		}
	}*/
	
	public OutflowSubscriptions( InflowToOutflowsMap inflowToOutflowsMap) throws Exception {
		
		// initialize the mapping between node outflows and node:inflow pairs subscribing to them
		_outflowToSubscriptionsMap = new HashMap<Outflow,List<NodeInput>>();
	
		// loop over nodes in the expanded workflow graph
		for (Inflow inflow : inflowToOutflowsMap.keySet() ) {

			List<Outflow> outflows = inflowToOutflowsMap.get(inflow);
			for (Outflow outflow: outflows) {
				NodeInput nodeInput = new NodeInput(inflow.getNode(),inflow.getLabel());
				
				addOutflowSubscription(outflow,nodeInput);				
			}
		}
	}
	


	/*
	private void _registerReceiver(WorkflowNode node, String label, Outflow outflow) throws Exception {

		Map<WorkflowNode, String> subscriptionsForOutflow = 
			_outflowToSubscriptionsMap.get(outflow);
		
		if (subscriptionsForOutflow == null) {
			subscriptionsForOutflow = new HashMap<WorkflowNode, String>();
			_outflowToSubscriptionsMap.put(outflow, subscriptionsForOutflow);
		}
		
		subscriptionsForOutflow.put(node, label);
	}*/
	
	private void addOutflowSubscription(Outflow outflow, NodeInput nodeInput) throws Exception {

		List<NodeInput> subscriptionsForOutflow = findAllNodeInputsBoundToOutflow(outflow);
		
		if (subscriptionsForOutflow == null) {
			subscriptionsForOutflow = new Vector<NodeInput>();
			_outflowToSubscriptionsMap.put(outflow, subscriptionsForOutflow);
		}
		
		subscriptionsForOutflow.add( nodeInput);
	}
	
	public List<NodeInput> findAllNodeInputsBoundToOutflow(Outflow outflow ) {
		return _outflowToSubscriptionsMap.get(outflow );
	}		
}