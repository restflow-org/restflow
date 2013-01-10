package org.restflow.data;

import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.data.Protocol;
import org.restflow.metadata.TraceRecorder;
import org.restflow.nodes.AbstractWorkflowNode;
import org.restflow.nodes.WorkflowNode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;


/*
 *  This class is thread safe because its configuration fields are final and access to
 *  its mutable state is synchronized on the instance of this class.
*/
@ThreadSafe()
public class Outflow implements ApplicationContextAware {
	
	/* immutable configuration fields */
	private final WorkflowNode _node;
	private final String _label;
	private final UriTemplate _uriTemplate;
	private final boolean _isDefaultUri;
	private final Protocol _protocol;
	private boolean _usePathSuffix;
	
	/* instance state variables */
	@GuardedBy("this")	private boolean _outputPacketReady ;
	@GuardedBy("this")	private Packet _outputPacket ;
	@GuardedBy("this")	private long _tokenCount;
	private WorkflowContext _workflowContext;
	private boolean _hasReceivers;
	private long _eventCount;
	
	public Outflow(WorkflowNode node, String label, UriTemplate uriTemplate, 
			boolean isDefaultUri, Protocol protocol) throws Exception {

		_node = node;
		_label = label;
		_uriTemplate = uriTemplate;
		_isDefaultUri = isDefaultUri;
		_protocol = protocol;
		_hasReceivers = true;
		_eventCount = 0;
	}

	public void setHasReceivers(boolean value) {
		_hasReceivers = value;
	}
	
	public void elaborate() throws Exception {
		_protocol.validateOutflowUriTemplate(_node, _label, _uriTemplate);
		
	}
	
	public synchronized void setApplicationContext(ApplicationContext context) throws BeansException {
		_workflowContext = (WorkflowContext)context;
	}
	
	public String toString() {
		return _label;
	}
	
	public String getLabel() { return _label; }
	
	public String getBinding() { return _uriTemplate.getExpression(); }
	

	public void configure() throws Exception {
		
		if ( !_node.stepsOnce() && _uriTemplate.getVariableCount() == 0 && !_protocol.supportsSuffixes()) {
			throw new Exception("URI template for outflow " + this + " must include at least one variable.");
		}

//		_usePathSuffix = _uriTemplate.getVariableCount() == 0 && !_node.stepsOnce();

	}
	
	public synchronized void initialize() throws Exception {
		_outputPacket = null;
		_outputPacketReady = false;
		
		// TODO make this statement work correctly in configure()
		_usePathSuffix = _uriTemplate.getVariableCount() == 0 && !_node.stepsOnce();
	
		_tokenCount = 0;
	}
	
	public synchronized boolean packetReady() {
		return _outputPacketReady;
	}

	public synchronized void createAndSendPacket(Object value, Map<String,Object> metadata, Long stepID) throws Exception {

		String[] variableNames = _uriTemplate.getVariableNames();
		Object[] variableValues = new Object[variableNames.length];
		
		Uri uri = getExpandedUri(metadata, variableNames, variableValues);
		
		// have the protocol create the packet and store resources as needed
		Packet packet = _protocol.createPacket(value, uri, _uriTemplate, variableValues, stepID);

		// send the packet if any was created
		if (packet != null) {
			sendPacket(packet, stepID);
		}
	}

	
	public synchronized Uri getExpandedUri(Map<String,Object> metadata, String[] variableNames, Object[] variableValues) throws Exception {

		String pathSuffix = "";	
		if (_usePathSuffix) {
			pathSuffix = "/" + ++_tokenCount;
		}

		return _uriTemplate.getExpandedUri(metadata, variableValues, _node.getUriPrefix(), pathSuffix);
	}

	public synchronized void sendPacket(Packet packet, Long stepID) throws Exception {
		_outputPacket = packet;
		_outputPacketReady = true;
		_recordPacketSent(stepID);
	}
	
	public synchronized long getEventCount() {
		return _eventCount;
	}
	
	public synchronized Packet peek() {
		return _outputPacket;
	}

	public synchronized Packet get() throws Exception {
		
		if (! _outputPacketReady) {
			throw new Exception("Request for packet on empty outflow '" + this + "' on node " + _node);
		}
		
		Packet outputPacket = _outputPacket;
		
		_outputPacket = null;
		_outputPacketReady = false;
		return outputPacket;
	}
	
	public void clear() {
		_outputPacket = null;
		_outputPacketReady = false;		
	}

	public String getDataflowBinding() {
		return _uriTemplate.getReducedPath();
	}	
	
	private synchronized void _recordPacketSent(Long stepID) throws Exception {
		
		_eventCount++;

		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		
		recorder.recordPacketSent(this, _outputPacket, stepID);	
		
		if (_outputPacket == null) {
			System.out.println("****** OUTPUT PACKET IS NULL ************");
			Thread.dumpStack();
			System.exit(1);
		} else if (_outputPacket.getMetadataKeys() == null) {
			System.out.println("****** METADATA KEYS IS NULL ************");
			Thread.dumpStack();
			System.exit(1);
		}

		int variableCount = _outputPacket.getMetadataKeys().length;

		//TODO Remove scheme from metadata URIs
		//TODO Add tests for nested workflows using file scheme in subworkflows
//		for (int i = 0; i < variableCount; i++) {
//			String name = _outputPacket.getMetadataKeys()[i];
//			Object value = _outputPacket.getMetadataValues()[i];
//			String id = _node.getNodeName() + "." + _label  + _tokenCount + "@" + name;
//			recorder.recordMetadataSent(_node, name, value, id);
//		}
	}

	public WorkflowNode getNode() {
		return _node;
	}


	public Protocol getProtocol() {
		return _protocol;
	}


	public UriTemplate getUriTemplate() {
		return _uriTemplate;
	}

	public boolean isDefaultUri() {
		return _isDefaultUri;
	}

	public boolean hasReceivers() {
		return _hasReceivers;
	}
}
