package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.restflow.data.Outflow;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.TraceRecorder;

import net.jcip.annotations.ThreadSafe;


/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public class InPortal extends AbstractWorkflowNode {

	private final Map<String,Object> _inputValues = new HashMap<String,Object>();
	
	
	/**************************
	 *     Node lifecycle     *
	 **************************/
	
	public InPortal() {
		super();
		_stepsOnce = true;
	}
	
	public synchronized boolean readyForInputPacket(String label) throws Exception {
		throw new Exception("InPortal does not take inflows.");
	}
	
	public synchronized void setInputValue(String label, Object inputValue)
		throws Exception {
	
		_inputValues.put(label, inputValue);		
	}

	public synchronized ChangedState trigger() throws Exception {

		if ( isNodeFinished() ) {

			return ChangedState.FALSE;

		} else if (_checkDoneStepping() == DoneStepping.TRUE) {
				
			_sendEndOfStreamPackets();
			_flagNodeFinished();
			return ChangedState.TRUE;
			
		} else {

			// inform the trace recorder that the portal fired
			TraceRecorder recorder = _workflowContext.getTraceRecorder();
			recorder.recordStepStarted(this);

			// cause input portals to trigger only once per subworkflow run
			_flagDoneStepping();
	
			_sendOutputs();
			
			// inform the trace recorder that the portal fired
			recorder.recordStepCompleted(this);
	
			return ChangedState.TRUE;
		}
	}

	private synchronized void _sendOutputs() throws Exception {
		
		for (Entry<String, Object> entry  : _inputValues.entrySet()) {
			Outflow outflow = _outflows.get(entry.getKey());
			outflow.createAndSendPacket(entry.getValue(), _variables, null);
		}
	}	
}