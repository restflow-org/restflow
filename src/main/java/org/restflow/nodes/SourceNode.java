package org.restflow.nodes;

import org.restflow.WorkflowContext;
import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Protocol;
import org.restflow.data.ProtocolReader;
import org.restflow.data.UriTemplate;
import org.restflow.enums.ChangedState;
import org.restflow.metadata.TraceRecorder;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public class SourceNode extends AbstractWorkflowNode {
	
	@GuardedBy("this") private ProtocolReader 	_protocolReader;
	@GuardedBy("this") private UriTemplate    	_uriTemplate;
	@GuardedBy("this") private Outflow			_outflow;
	
	private static final String _outflowLabel = "outflow";
	
	/**************************
	 *     Node lifecycle     
	 * @throws Exception *
	 **************************/
	
	// TODO Replace this method with a call to SourceNodeBuilder
	public static SourceNode CreateSourceNodeForInflow(String nodeName,
			Inflow inflow, WorkflowContext _workflowContext) throws Exception {

		SourceNode sourceNode = new SourceNode();

		sourceNode.setName(nodeName);
		sourceNode.setApplicationContext(_workflowContext);
//		sourceNode.setHidden(true);

		Protocol inflowProtocol = inflow.getProtocol();
		sourceNode.setProtocolReader(inflowProtocol.getNewProtocolReader());

		UriTemplate uriTemplate = inflow.getUriTemplate();
		String outflowPath = uriTemplate.getPath();
		String outflowUri = outflowPath;
		sourceNode.setUriTemplate(uriTemplate);

		sourceNode.registerOutflow(_outflowLabel, outflowUri, false);
		inflow.getNode().replaceInflow(inflow.getLabel(), outflowPath);
		
		sourceNode.configure();
		
		return sourceNode;
	}

	public synchronized Outflow registerOutflow(String label, String binding, boolean isDefaultUri) throws Exception {
		
		if (_outflow != null) {
			throw new Exception("Cannot register more than one outflow on a source node.");
		}
		
		_outflow = super.registerOutflow(label, binding, isDefaultUri);

		return _outflow;
	}
	
	public synchronized void setProtocolReader(ProtocolReader reader) {
		_protocolReader = reader;
		_stepsOnce = reader.stepsOnce();
	}
	
	
	public synchronized void setResourcePath(String path) {
		_uriTemplate = new UriTemplate(path);
	}
	
	public synchronized void setUriTemplate(UriTemplate uriTemplate) {
		_uriTemplate = uriTemplate;
	}
	
	public synchronized boolean readyForInputPacket(String label) throws Exception {
		throw new Exception("A source node does not take inflows.");
	}
	
	public synchronized ChangedState trigger() throws Exception {

		if ( isNodeFinished() ) {

			return ChangedState.FALSE;

		} else if (_checkDoneStepping() == DoneStepping.TRUE ) {
				
			_sendEndOfStreamPackets();
			_flagNodeFinished();
			return ChangedState.TRUE;
			
		} else {
		
			Object value = _protocolReader.getExternalResource(_uriTemplate.getPath());
			
			if (value == null) {
			
				_flagDoneStepping();

			} else {
				
				// inform the trace recorder that the portal fired
				TraceRecorder recorder = _workflowContext.getTraceRecorder();

				recorder.recordStepStarted(this);
				
				_outflow.createAndSendPacket(value, _variables, null);

				recorder.recordStepCompleted(this);
			}
	
			return ChangedState.TRUE;
		}
	}
	
	public synchronized Outflow getOutflow() {
		return _outflow;
	}

	public synchronized String getOutflowExpression() {
		return _outflow.getDataflowBinding();
	}
	
	@Override
	public void initialize() throws Exception {
		super.initialize();
		_protocolReader.initialize();
	}

}