package org.restflow.data;

import org.restflow.nodes.WorkflowNode;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe.  Its configuration fields are final and, except for _node, 
 * refer to immutable objects. Access to its mutable state fields is synchronized on
 * the corresponding instance of this class.
 */
@ThreadSafe()
public class Inflow implements Comparable<Inflow> {
	
	/* immutable configuration fields */
	private final WorkflowNode _node;
	private final String _label;
	private final UriTemplate _uriTemplate;
	private final Protocol _protocol;
	private final String _packetBindingExpression;
	private final boolean _receiveOnce;	

	/* instance state variables */
	@GuardedBy("this")	private Packet _inputPacket;
	@GuardedBy("this")	private boolean _eosReceived;
	@GuardedBy("this")	private long _eventCount;
	
	public Inflow(WorkflowNode node, String label, UriTemplate uriTemplate, String packetBinding,
			Protocol protocol, boolean receiveOnce) throws Exception {
		
		_node = node;
		_label = label;
		_uriTemplate = uriTemplate;
		_packetBindingExpression = packetBinding;
		_protocol = protocol;
		_receiveOnce = receiveOnce;
		_eventCount = 0;

		_protocol.validateInflowUriTemplate(uriTemplate, _node);
	}
	
	public String getLabel() { return _label;}
	
	public String getBinding() { return _uriTemplate.getExpression(); }
	
	public WorkflowNode getNode() {
		return _node;
	}

	public String getDataflowBinding() {
		return _uriTemplate.getReducedPath();
	}

	public String getPacketBinding() {
		return _packetBindingExpression;
	}
	
	public String[] getVariableNames() {
		return _uriTemplate.getVariableNames();
	}

	public String getPath() {
		return _uriTemplate.getPath();
	}

	public Protocol getProtocol() {
		return _protocol;
	}

	public UriTemplate getUriTemplate() {
		return _uriTemplate;
	}

	public boolean receiveOnce() {
		return _receiveOnce;
	}

	public int compareTo(Inflow otherInflow) {
		String thisNameLabelPair = _label + _node.getName();
		String otherNameLabelPair = otherInflow._label + otherInflow._node.getName();
		return thisNameLabelPair.compareTo(otherNameLabelPair);
	}

	public synchronized void initialize() {
		_eosReceived = false;
		_inputPacket = null;
	}
	
	public synchronized boolean eosReceived() {
		return _eosReceived;
	}

	public synchronized void setEosReceived() {
		_eventCount++;
		_eosReceived = true;
	}

	public synchronized void setInputPacket(Packet packet) {
		_inputPacket = packet;
		_eventCount++;
	}
	
	public synchronized Packet getInputPacket() {
		return _inputPacket;
	}
	
	public synchronized void clear() {
		_inputPacket = null;
	}
	
	public synchronized boolean hasInputPacket() {
		return _inputPacket != null;
	}
	
	public synchronized long getEventCount() {
		return _eventCount;
	}
}