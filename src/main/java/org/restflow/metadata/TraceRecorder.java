package org.restflow.metadata;

import java.sql.SQLException;
import java.util.Map;

import org.restflow.actors.Workflow;
import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.nodes.WorkflowNode;
import org.springframework.context.ApplicationContextAware;



public interface TraceRecorder extends ApplicationContextAware {
	Trace getReadOnlyTrace() throws SQLException;
	void recordWorkflowGraph(Workflow workflow) throws Exception;
	void recordPacketCreated(Packet packet, Long stepID) throws Exception;
	void recordPacketSent(Outflow outflow, Packet packet, Long stepID) throws Exception;
	void recordPacketReceived(Inflow inflow, Packet packet) throws Exception;
	void recordState(WorkflowNode node, String label, Packet packet);
	long recordStepStarted(WorkflowNode node) throws Exception;
	void recordStepCompleted(WorkflowNode node) throws Exception;
	void setDataStore(Map<String, Object> store);
	void close() throws SQLException;
	void recordWorkflowRunStarted() throws Exception;
	void recordWorkflowRunCompleted() throws Exception;
	public void recordWorkflowInputEvent(String inputName, Object value) throws Exception;
	public void recordWorkflowOutputEvent(String inputName, Object value) throws Exception;
}
