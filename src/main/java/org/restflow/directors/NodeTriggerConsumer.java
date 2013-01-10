package org.restflow.directors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.nodes.WorkflowNode;


public class NodeTriggerConsumer implements Runnable {

	private WorkflowNode _node;
	private MTDataDrivenDirector _director;
	private NodeTriggerProducer _producer;
	private Map<Outflow, Map<NodeTriggerProducer, List<String>>> _outflowToSubscriptionsMap =
		new HashMap<Outflow,Map<NodeTriggerProducer, List<String>>>();
	
	public NodeTriggerConsumer(WorkflowNode node, MTDataDrivenDirector director, NodeTriggerProducer producer) {
		_node = node;
		_director = director;
		_producer = producer;
	}
	
	public void run() {

		try {
	
			while (!_director.isHalted() && _producer.isRunning()) {
				
				if (! _producer.waitForActiveTrigger()) {
					// System.out.println("Consumer for node " + _node + " timed out waiting for active trigger.");

					
					continue;
				}
				
				try {
					_node.finishTrigger();
				} catch (Exception e) {
					_director.halt(e, this);
					continue;
				}

				_sendOutputPackets();

				_producer.decrementActiveTriggers();
			}
			
			// System.out.println("Consumer for node " + _node + " exited main loop.");
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
		
	private void _sendOutputPackets() throws Exception {
		
		// System.out.println("Consumer for node " + _node + " preparing to broadcast.");
		
		// loop over all outputs from the node by output label
		for (Outflow outflow : _node.getOutflows().values()) {

			if (outflow.packetReady()) {
			
				// get the subscriptions for the uri
				Map<NodeTriggerProducer, List<String>> subscriptionsForUri = _outflowToSubscriptionsMap.get(outflow);
	
				// return immediately if there are no subscribers for the uri
				if (subscriptionsForUri != null) {
	
					// get the object output to that binding
					Packet packet = _node.getOutputPacket(outflow.getLabel());
					
					// System.out.println("Consumer for node " + _node + " broadcasting packet " + ((SingleResourcePacket)packet).getResource());

					for (NodeTriggerProducer targetThread : subscriptionsForUri.keySet()) {
	
						List<String> labelList = subscriptionsForUri.get(targetThread);
	
						for(String targetLabel : labelList) {						
	
							targetThread.queueInputPacket(targetLabel, packet);						
						}
					}
				}
			}
		}
	}
		
	public void registerReceiver(NodeTriggerProducer producer, Outflow outflow, Inflow inflow) {
		
		Map<NodeTriggerProducer, List<String>> subscriptionsForUri = _outflowToSubscriptionsMap.get(outflow);
		
		if (subscriptionsForUri == null) {
			subscriptionsForUri = new HashMap<NodeTriggerProducer, List<String>>();
			_outflowToSubscriptionsMap.put(outflow, subscriptionsForUri);
		}
		
		List<String> labelList = subscriptionsForUri.get(producer);
		if (labelList == null) {
			labelList = new LinkedList<String>();
			subscriptionsForUri.put(producer,labelList);
		}
		
		labelList.add(inflow.getLabel());
	}

	public String toString() {
		return "NodeThreadRunnerTwo for " + _node.getName();
	}
}
