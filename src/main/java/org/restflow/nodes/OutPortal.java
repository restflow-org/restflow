package org.restflow.nodes;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import org.restflow.data.Inflow;
import org.restflow.data.Packet;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.TraceRecorder;

import net.jcip.annotations.ThreadSafe;


/**
 * This class is thread safe. Its fields are final references to thread-safe objects.
 */
@ThreadSafe()
public class OutPortal extends AbstractWorkflowNode {

	private final Map<String, Boolean> _readyForInput = new Hashtable<String, Boolean>();
	private final Map<String, Packet> _outputPackets = new Hashtable<String, Packet>();
	
	/**************************
	 *     Node lifecycle     *
	 **************************/
	
	public OutPortal() {
		super();
		_stepsOnce = true;
	}
	
	public synchronized void initialize() throws Exception {	
		super.initialize();
		_setAllInputsReady();
	}

	public synchronized boolean readyForInputPacket(String label) throws Exception {
		return _readyForInput.get(label);
	}
	
	public synchronized void setInputPacket(String label, Packet packet) throws Exception {

		// store the input value in the relevant inflow object
		Inflow inflow = _inflows.get(label);
		inflow.setInputPacket(packet);
		
		// record receipt of input packet
		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		recorder.recordPacketReceived(inflow, packet);
		
		// indicate that no more input is needed for this label
		_readyForInput.put(label, false);
	}


	public synchronized ChangedState trigger() throws Exception {
		
		if ( (_checkDoneStepping() == DoneStepping.FALSE) && _allInputsStaged()) {
			
			_fire();
			return ChangedState.TRUE;			
			
		} else {
			
			return ChangedState.FALSE;
		}
	}

	public synchronized Packet getOutputPacket(String label) {
		
		return _outputPackets.get(label);
	}
	
	/**************************
	 *     Private methods    
	 * @throws SQLException *
	 **************************/
	
	private synchronized void _fire() throws Exception {

		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		recorder.recordStepStarted(this);
		
		// copy all input values to corresponding output values
		for (String label : _inflows.keySet()) {
			Inflow inflow = _inflows.get(label);
			Packet packet = inflow.getInputPacket();
			_outputPackets.put(label, packet);
		}			
		
		_setAllInputsReady();
		_clearInflows(false);

		recorder.recordStepCompleted(this);

		_flagDoneStepping();
		_flagNodeFinished();
	}

	private synchronized void _setAllInputsReady() {
		// indicate the portal is ready for more data
		for (String label : _inflows.keySet()) {
			Inflow inflow = _inflows.get(label);
			if (! inflow.eosReceived()) {
				_readyForInput.put(label, true);
			}
		}
	}

	public synchronized String getBindingForInflow(String label) {
		Inflow inflow = _inflows.get(label);
		String binding = inflow.getDataflowBinding();
		return binding;
	}	
}