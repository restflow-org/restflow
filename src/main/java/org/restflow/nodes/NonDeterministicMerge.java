package org.restflow.nodes;

import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.data.PublishedResource;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.TraceRecorder;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public class NonDeterministicMerge extends AbstractWorkflowNode {

	@GuardedBy("this")	private Object _inputValue;	
	@GuardedBy("this")	private boolean _inputReceived;
	
	/**************************
	 *     Node lifecycle     
	 **************************/
	
	public NonDeterministicMerge() {
		super();
	}
	
	public synchronized void initialize() throws Exception {
		super.initialize();
		
		_inputReceived = false;
		_inputValue = null;
	}
	
	public synchronized boolean readyForInputPacket(String label) throws Exception {

		Inflow inflow = _inflows.get(label);
		return ! _inputReceived && ! inflow.eosReceived();
	}
	
	public boolean inputIsOptional(String label) {
		return true;
	}
	
	protected synchronized void _loadInputPacket(String label, Packet inputPacket) throws Exception {	
		super._loadInputPacket(label, inputPacket);
		handleInput(label, inputPacket);
	}
	
	private synchronized void handleInput(String label, Packet inputPacket) throws Exception {
		Inflow inflow = _inflows.get(label);
		inflow.setInputPacket(inputPacket);
		PublishedResource resource = inputPacket.getResource(inflow.getPacketBinding());
		_inputValue = resource.getData();
		_inputReceived = true;
	}

	protected void _handleEndOfStream(String label, Packet endOfStreamPacket) throws Exception {
	}
	
	public synchronized ChangedState trigger() throws Exception {
		
		if ( isNodeFinished() ) return ChangedState.FALSE;

		// output end-of-streams if node is done stepping
		if (_checkDoneStepping() == DoneStepping.TRUE) {
			_sendEndOfStreamPackets();
			_flagNodeFinished();
			return ChangedState.TRUE;
		}
		
		if (! _inputReceived) return ChangedState.FALSE;
		
		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		recorder.recordStepStarted(this);
		
		_sendOutput();
		_inputValue = null;
		_inputReceived = false;
		
		// inform the trace recorder that the portal fired
		recorder.recordStepCompleted(this);

		return ChangedState.TRUE;
	}

	private synchronized void _sendOutput() throws Exception {
		
		for (Outflow outflow : _outflows.values()) {
			outflow.createAndSendPacket(_inputValue, _variables, null);
		}
	}	
}