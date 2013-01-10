package org.restflow.data;

import java.util.LinkedList;

@SuppressWarnings("serial")
public class InflowQueue extends LinkedList<Packet> {
	
	private final String _nodeName;
	private final String _label;
	
	public InflowQueue(String nodeName, String label) {
		_nodeName = nodeName;
		_label = label;
	}
	
	public String getNodeName() {
		return _nodeName;
	}
	
	public String getLabel() {
		return _label;
	}
}
