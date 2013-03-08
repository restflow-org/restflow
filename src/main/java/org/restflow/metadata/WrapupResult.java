package org.restflow.metadata;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.restflow.data.Packet;
import org.restflow.data.PublishedResource;
import org.restflow.data.SingleResourcePacket;
import org.restflow.nodes.AbstractWorkflowNode;
import org.restflow.util.PortableIO;

import sun.org.mozilla.javascript.internal.Node;


public class WrapupResult {

	private final List<UnusedDataRecord> _unusedDataRecords;
	
	private static boolean _ignoreEndOfStreamPackets;
	
	static {
		synchronized(WrapupResult.class) {
			_ignoreEndOfStreamPackets = true;
		}
	}
	
	public static void setIgnoreEndOfStreamPackets(boolean value) {
		synchronized(WrapupResult.class) {
			_ignoreEndOfStreamPackets = value;
		}
	}
	
	public WrapupResult() {
		_unusedDataRecords = new LinkedList<UnusedDataRecord>();
	}
	
	public WrapupResult (List<UnusedDataRecord> recordList) {
		_unusedDataRecords = recordList;
	}
	
	public List<UnusedDataRecord> getRecords() {
		return _unusedDataRecords;
	}
	
	public void addRecords(List<UnusedDataRecord> recordList) {
		_unusedDataRecords.addAll(recordList);
	}
	
	public static String asString(List<UnusedDataRecord> recordList) throws Exception {

		Collections.sort(recordList);

		StringBuffer buffer = new StringBuffer();
		
		for (UnusedDataRecord record : recordList) {
			
			if (record.packets.size() > 0) {
	 			boolean multiplePackets = record.packets.size() > 1;
				
				buffer.append(
						record.packets.size() 						+ 
						(multiplePackets ? " packets" : " packet") 	+ 
						" in " 										+
						record.bufferType 							+ 
						" '" + record.label + "'" 					+ 
						" on node " 								+
						AbstractWorkflowNode.decorateNodeName(record.nodeName) + " with "			+
						(multiplePackets ? "URIs" : "URI") 
						
				);
				
				boolean isFirstPacket = true;
				for (Packet packet : record.packets) {
					if (multiplePackets && !isFirstPacket) {
						buffer.append(",");
					} else {
						isFirstPacket = false;
					}
					if (packet instanceof SingleResourcePacket) {
						PublishedResource resource = ((SingleResourcePacket) packet).getResource();
						buffer.append(" '" + resource.getUri() + "'");
					}
				}
				
				buffer.append(PortableIO.EOL);
			}			
		}
		
		return buffer.toString();
	}
	
	public static class UnusedDataRecord implements Comparable {
		public final String nodeName;
		public final String label;
		public final String bufferType;
		public final List<Packet> packets;
		
		public UnusedDataRecord(String nodeName, String label, 
				String bufferType, List<Packet> packets) {
			this.nodeName = nodeName;
			this.label = label;
			this.bufferType = bufferType;
			
			if (_ignoreEndOfStreamPackets) {
				
				this.packets = new LinkedList<Packet>();
				for (Packet packet : packets) {
					if (packet != AbstractWorkflowNode.EndOfStreamPacket) {
						this.packets.add(packet);
					}
				}
				
			} else {
				
				this.packets = packets;
			}
		}

		@Override
		public int compareTo(Object other) {
			UnusedDataRecord otherRecord = (UnusedDataRecord)other;
			String otherNodeLabelPair = otherRecord.nodeName + otherRecord.label + otherRecord.bufferType;
			String thisNodeLabelPair = this.nodeName + this.label + this.bufferType;
			return thisNodeLabelPair.compareTo(otherNodeLabelPair);
		}
	}
}
