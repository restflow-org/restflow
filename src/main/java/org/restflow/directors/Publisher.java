package org.restflow.directors;

import org.restflow.actors.Workflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.WorkflowNode;


public interface Publisher {

	void setWorkflow(Workflow workflow);

	void publish(Outflow outflow, Packet outputPacket) throws Exception;
	
	void subscribe(WorkflowNode node, String label, Outflow binding) throws Exception ;

	boolean flushPacketToNode(WorkflowNode node) throws Exception;

	void clearPacketBuffers();

	WrapupResult wrapup() throws Exception;
}
