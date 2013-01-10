package org.restflow.metadata;

public class WorkflowModel {

	
	public static class Node {

		public final Long nodeID;
		public final Long parentID;
		public final String fullName;
		public final String localName;
		public final boolean hasChildren;
	
		public Node(Long nodeID, Long parentID, String fullName, String localName, boolean hasChildren) {
			this.nodeID 		= nodeID;
			this.parentID 		= parentID;
			this.fullName 		= fullName;
			this.localName 		= localName;
			this.hasChildren 	= hasChildren;
		}
	}	

	public static class Outflow {
		
		public final String nodeFullName;
		public final String nodeLocalName;
		public final String portName;
		public final String uriTemplate;
		
		public Outflow(
				String nodeFullName, 
				String nodeLocalName, 
				String portName, 
				String portUriTemplate ) {

			this.nodeFullName 		= nodeFullName;
			this.nodeLocalName 		= nodeLocalName;
			this.portName 			= portName;
			this.uriTemplate		= portUriTemplate;
		}
	}
	
	public static class Channel {
		
		public final String sendingNodeFullName;
		public final String sendingNodeLocalName;
		public final String sendingPortName;
		public final String sendingPortUriTemplate;
		public final String receivingNodeFullName;
		public final String receivingNodeLocalName;
		public final String receivingPortName;
		public final String receivingPortUriTemplate;
		
		public Channel(
				String sendingNodeFullName, 
				String sendingNodeLocalName, 
				String sendingPortName, 
				String sendingPortUriTemplate,
				String receivingNodeFullName, 
				String receivingNodeLocalName, 
				String receivingPortName, 
				String receivingPortUriTemplate ) {

			this.sendingNodeFullName 		= sendingNodeLocalName;
			this.sendingNodeLocalName 		= sendingNodeFullName;
			this.sendingPortName 			= sendingPortName;
			this.sendingPortUriTemplate		= sendingPortUriTemplate;
			this.receivingNodeFullName 		= receivingNodeFullName;
			this.receivingNodeLocalName 	= receivingNodeLocalName;
			this.receivingPortName 			= receivingPortName;
			this.receivingPortUriTemplate	= receivingPortUriTemplate;
		}
	}
}
