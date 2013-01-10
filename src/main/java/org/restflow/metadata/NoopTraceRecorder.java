package org.restflow.metadata;

import java.sql.SQLException;
import java.util.Map;

import org.restflow.actors.Workflow;
import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.nodes.WorkflowNode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import net.jcip.annotations.Immutable;


/**
 * This class has no state and is thus thread safe.
 */
@Immutable()
public class NoopTraceRecorder implements TraceRecorder {
	
	@Override public long recordStepStarted(WorkflowNode node) {return 0;}
	@Override public void recordStepCompleted(WorkflowNode node) {}
	@Override public void recordPacketReceived(Inflow inflow, Packet packet) {}
	@Override public void recordState(WorkflowNode node, String label, Packet packet) {}
	@Override public void setDataStore(Map<String, Object> store) {}
	@Override public void recordPacketCreated(Packet packet, Long stepID) {}
	@Override public void recordPacketSent(Outflow outflow, Packet packet, Long stepID)throws Exception {}
	@Override public void setApplicationContext(ApplicationContext context) throws BeansException {}
	@Override public void recordWorkflowGraph(Workflow workflow) throws SQLException {}
	@Override public void close() throws SQLException {}
	@Override public Trace getReadOnlyTrace() throws SQLException {return null;}
	@Override public void recordWorkflowRunStarted() throws Exception {}
	@Override public void recordWorkflowRunCompleted() throws Exception {}
	@Override public void recordWorkflowInputEvent(String inputName, Object value) throws Exception {}
	@Override public void recordWorkflowOutputEvent(String inputName, Object value) throws Exception {}
}
