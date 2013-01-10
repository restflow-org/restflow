package org.restflow.directors;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restflow.actors.Workflow;
import org.restflow.data.Inflow;
import org.restflow.data.InflowQueue;
import org.restflow.data.InflowToOutflowsMap;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.PortableIO;


public class BufferedPublisher implements Publisher {

	private Map<Outflow,Map<WorkflowNode, Map<String,InflowQueue>>> _outflowToSubscriptionsMap;
	protected InflowToOutflowsMap _inflowToOutflowsMap;
	private PublishSubscribeDirector _director;
	List<InflowQueue> _packetQueues;
	
	public void setWorkflow(Workflow workflow) {
		_inflowToOutflowsMap = workflow.getInflowToOutflowsMap();
		_director = (PublishSubscribeDirector)(workflow.getDirector());
		_outflowToSubscriptionsMap = 
			new HashMap<Outflow,Map<WorkflowNode, Map<String,InflowQueue>>>();
		_packetQueues = new LinkedList<InflowQueue>();
	}
	
	public void subscribe(WorkflowNode node, String label, Outflow outflow) throws Exception {

		// get the existing subscriptions for the given outflow and create a new
		// subscription-for-outflow map if needed
		Map<WorkflowNode, Map<String, InflowQueue>> subscriptionsForOutflow = 
			_outflowToSubscriptionsMap.get(outflow);
		if (subscriptionsForOutflow == null) {
			subscriptionsForOutflow = new HashMap<WorkflowNode, Map<String,InflowQueue>>();
			_outflowToSubscriptionsMap.put(outflow, subscriptionsForOutflow);
		}
		
		// get the map of buffers for the uri-node combination
		Map<String,InflowQueue> labelToBufferMap = subscriptionsForOutflow.get(node);
		if (labelToBufferMap == null) {		
			labelToBufferMap = new HashMap<String,InflowQueue>();
			subscriptionsForOutflow.put(node, labelToBufferMap);
		}
		
		// create the buffer for this uri and node pair, and save it in the 
		// uri to subscription map
		InflowQueue queue = new InflowQueue(node.getName(), label);
		labelToBufferMap.put(label, queue);
		_packetQueues.add(queue);
	}
	
	public void clearPacketBuffers() {
		
		for (InflowQueue queue : _packetQueues) {
			queue.clear();
		}
	}

	public boolean flushPacketToNode(WorkflowNode node) throws Exception {

		boolean packetSent = false;

		// get all of the input bindings for the node
		for (Inflow inflow : node.getNodeInflows()) {

			List<Outflow> outflows = _inflowToOutflowsMap
					.get(inflow);

			for (Outflow outflow : outflows) {
				// get the subscriptions for the uri
				Map<WorkflowNode, Map<String, InflowQueue>> subscriptionsForUri = _outflowToSubscriptionsMap
						.get(outflow);

				Map<String, InflowQueue> labelToBufferMapForUri = subscriptionsForUri
						.get(node);

				for (String label : labelToBufferMapForUri.keySet()) {
					// check if the node is ready for more input on this binding
					if (node.readyForInputPacket(label)) {

						// get the buffer for this node
						List<Packet> buffer = labelToBufferMapForUri.get(label);

						// check if there is any data in the buffer
						if (buffer != null && buffer.size() > 0) {

							// get the next value from the buffer
							Packet bufferedPacket = buffer.get(0);
							buffer.remove(0);

							// send the value to the node
							node.setInputPacket(label, bufferedPacket);

							// note that data was flushed to node
							packetSent = true;
						}
					}
				}
			}
		}

		return packetSent;
	}
	
	public void publish(Outflow outflow, Packet packet) throws Exception {
		
		// get the subscriptions for the uri
		Map<WorkflowNode, Map<String, InflowQueue>> subscriptionsForUri = 
			_outflowToSubscriptionsMap.get(outflow);
		
		// return immediately if there are not subscribers for the uri
		if (subscriptionsForUri == null) {
			return;
		}

		// loop over the subscribers and newly published value to
		// the list of value to send to each 
		for (WorkflowNode node : subscriptionsForUri.keySet()) {
			
			Map<String,InflowQueue> labelToBufferMapForUri =
				subscriptionsForUri.get(node);
			
			for (InflowQueue buffer : labelToBufferMapForUri.values()) {
				buffer.add(packet);
			}
		}

		// loop over the subscribers again and send each the value
		// published to them least recently 
		
		Set<WorkflowNode> buffers = subscriptionsForUri.keySet();
		List<WorkflowNode> list = new LinkedList<WorkflowNode>(buffers);
		Collections.sort(list);
		
		for (WorkflowNode node : list) {
			
			Map<String, InflowQueue> labelToBufferMapForUri = 
					subscriptionsForUri.get(node);

			for (String label : labelToBufferMapForUri.keySet()) {

				if (_director.nodeReadyForPacketOnLabel(node, label)) {

					List<Packet> buffer = labelToBufferMapForUri.get(label);
					packet = buffer.get(0);
					buffer.remove(0);
					_director.update(node, label, packet);
				}
			}
		}
	}

	@Override
	public WrapupResult wrapup() throws Exception {
		
		List<WrapupResult.UnusedDataRecord> unusedDataRecordList = 
			new LinkedList<WrapupResult.UnusedDataRecord>();
		
		for (InflowQueue queue: _packetQueues) {
			
			if (queue.size() > 0) {
				
				List<Packet> unusedPackets = new LinkedList<Packet>(queue);
				
				unusedDataRecordList.add(new WrapupResult.UnusedDataRecord(
							queue.getNodeName(),
							queue.getLabel(),
							"queue",
							unusedPackets));
			}
		}
		
		return new WrapupResult(unusedDataRecordList);
	}
}
