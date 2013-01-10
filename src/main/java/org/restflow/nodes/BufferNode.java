package org.restflow.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.restflow.data.Inflow;
import org.restflow.data.InflowQueue;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.TraceRecorder;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;


/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public class BufferNode extends AbstractWorkflowNode {

	/**************************
	 *  Private member data   *
	 **************************/
	
	@GuardedBy("this")	private Inflow _inflow;
	@GuardedBy("this")	private Outflow _outflow;
	@GuardedBy("this")	private InflowQueue _packetQueue;

	public BufferNode() {
		super();
	}
	
	/**************************
	 *  Public factories      *
	 **************************/
	
	public static BufferNode createHiddenBufferNode(String bufferedNodeName, 
			String bufferedInflowLabel) {

		// create a new buffer node
		BufferNode node = new BufferNode();
		
		synchronized(node) {

			// assign it a name
			node.setName("BufferNode-for-" + bufferedNodeName + "-" + bufferedInflowLabel);
			
			// hide the node from the trace recorder
			node.setHidden(true);
			
			// create the inflows and outflows maps because there 
			// will be no calls to setInflows and setOutflows
			node._inflows = new HashMap<String,Inflow>();
			node._outflows = new HashMap<String,Outflow>();

			node._packetQueue = new InflowQueue(bufferedNodeName, bufferedInflowLabel);
		}
	
		// return the new node
		return node;
	}

	public String getBufferedNodeName() {
		return _packetQueue.getNodeName();
	}
	
	public String getBufferedInflowLabel() {
		return _packetQueue.getLabel();
	}
	
	/**************************
	 * Configuration Methods  *
	 **************************/
	
	public synchronized void registerInflow(String label, String binding, boolean receiveOnce) throws Exception {
		
		// register the inflow with the superclass
		super.registerInflow(label, binding, receiveOnce);
		
		// check that no more than one inflow has been registered
		if (_inflows.size() > 1) {
			throw new Exception("Cannot register more than inflow for a buffer node.");
		}
		
		// keep a reference to the inflow
		_inflow = _inflows.get(label);
	}

	public synchronized Outflow registerOutflow(String label, String binding, boolean isDefaultUri) throws Exception {

		// register the outflow with the superclass
		_outflow = super.registerOutflow(label, binding, false);
		
		// check that no more than one outflow has been registered
		if (_outflows.size() > 1) {
			throw new Exception("Cannot register more than outflow for a buffer node.");
		}
		
		return _outflow;
	}	
	
	/**************************
	 *     Node lifecycle     *
	 **************************/
	
	public synchronized void initialize() throws Exception {

		// let superclass perform its initializations
		super.initialize();
		
		_packetQueue.clear();
	}

	public synchronized void validate() throws Exception {
		
		// validate the superclass
		super.validate();

		// TODO Find why commented lines below throw exceptions for DoubleNestedWorkflow
		
		// enforce an inflow count of 1
		if (_inflows.size() != 1) { 
//			throw new Exception("Buffer node must have one inflow registered.");
		}

		// enforce an outflow count of 1
		if (_outflows.size() != 1) { 
//			throw new Exception("Buffer node must have one outflow registered.");
		}
	}
	
	public synchronized boolean readyForInputPacket(String label) {
		
		// report that the node is ready for input packets
		// until an end of stream packet has been received 
		return !_inflow.eosReceived();
	}

	protected synchronized void _handleEndOfStream(String label, Packet endOfStreamPacket) throws Exception {
		
		// store the end of stream packet in the queue
		_packetQueue.add(endOfStreamPacket);
	}
	
	protected synchronized void _loadInputPacket(String label, Packet inputPacket) throws Exception {

		// let the superclass process the input packet
		super._loadInputPacket(label, inputPacket);

		// store the input packet in the queue
		_packetQueue.add(inputPacket);
	}
	
	public synchronized ChangedState trigger() throws Exception {
		
		// step the node if there is a packet in the queue and no packet in the outflow
		if (_packetQueue.size() > 0 && !_outflow.packetReady()) {

			// step the node
			_step();

			// report that the node stepped
			return ChangedState.TRUE;
			
		} else {
			
			// report that node did not step
			return ChangedState.FALSE;
		}
	}
	
	private synchronized void _step() throws Exception {

		// record the step
		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		recorder.recordStepStarted(this);
	
		// send the packet at the head of the queue to the outflow
		_outflow.sendPacket(_packetQueue.get(0), null);
		
		// remove the packet from the queue
		_packetQueue.remove(0);
		
		recorder.recordStepCompleted(this);
	}

	public int getQueueSize() {
		return _packetQueue.size();
	}
	
	public List<Packet> getQueueContents() {
		return new ArrayList<Packet>(_packetQueue);
	}
}