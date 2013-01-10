package org.restflow.actors;

import org.restflow.metadata.TraceRecorder;

public class WorkflowBuilder extends SubworkflowBuilder {
	
	public Workflow build() throws Exception {
		
		Workflow workflow = super.build();
		
		workflow.elaborate();
		
		TraceRecorder recorder = _context.getTraceRecorder();
		recorder.recordWorkflowGraph(workflow);
		
		return workflow;
	}
}
