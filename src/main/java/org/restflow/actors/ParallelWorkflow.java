package org.restflow.actors;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.WorkflowRunner.YamlStream;
import org.restflow.data.ProtocolRegistry;
import org.restflow.metadata.BasicTraceRecorder;
import org.restflow.metadata.TraceRecorder;
import org.restflow.util.Contract;

import ssrl.yaml.spring.YamlBeanDefinitionReader;

public class ParallelWorkflow extends Workflow {
	
	private boolean _runEnabled = false;
	
	public synchronized Object clone() throws CloneNotSupportedException {

		Contract.requires(_state == ActorFSM.INITIALIZED);

		// call the superclass clone method
		ParallelWorkflow theClone = (ParallelWorkflow)super.clone();
		
		theClone._state = ActorFSM.INITIALIZED;

		// return the clone
		return theClone;
	}

	
	public void enableRun() {
		_runEnabled = true;
	}
	
	public synchronized void step() throws Exception {
		
		if (_runEnabled) {
			super.step();
			return;
		}
		
		_runCount++;

		String workflowDefinitionString = _workflowContext.getWorkflowDefinitionString();
		String workflowDefinitionPath = _workflowContext.getWorkflowDefinitionPath();
		
		File runDirectory = null;
		String runDirectoryPath = _workflowContext.getRunDirectoryPath();
		if (runDirectoryPath != null) {
			runDirectory = new File(runDirectoryPath);
		}
		
		BasicTraceRecorder parentRecorder = (BasicTraceRecorder)_workflowContext.getTraceRecorder();
		TraceRecorder recorder = new BasicTraceRecorder(parentRecorder);
		
		((BasicTraceRecorder)(recorder)).setWritableTrace(_workflowContext.getWritableTrace());
		
		WorkflowContext context = new WorkflowContextBuilder()
			.workflowDefinitionString(workflowDefinitionString)
			.workflowDefinitionPath(workflowDefinitionPath)
			.runDirectory(runDirectory)
			.recorder(recorder)
			.metadataManager(_workflowContext.getMetaDataManager())
			.build();
				
		// create a yaml bean reader
		YamlBeanDefinitionReader rdr = new YamlBeanDefinitionReader(context);

		// register bean definitions from the provided definition path or stream
		if (workflowDefinitionPath != null) {
			rdr.registerBeanDefinitions(workflowDefinitionPath);
		} else {
			InputStream stream = new YamlStream(workflowDefinitionString);
			rdr.loadBeanDefinitions(stream, "-");
		}
		
		// read the bean definitions
		context.refresh();
		
		// load the protocol registry bean
		ProtocolRegistry protocolRegistry = 
			(ProtocolRegistry)context.getBean("DefaultProtocolRegistry");
		if (protocolRegistry != null) {
			context.setProtocolRegistry(protocolRegistry);
		}
		
		// load all Actor beans
		String actorBeanNames[] = context.getBeanNamesForType(Class.forName("org.restflow.actors.Actor"));
		if (actorBeanNames.length == 0) {
			throw new Exception("Must have at least one Actor defined in workflow script.");
		}

		ParallelWorkflow workflow = (ParallelWorkflow) context.getBean(_name);
		workflow.enableRun();
		
		// set node name of workflow so that its name those of its child nodes match 
		// the names of the corresponding nodes in the trace DB
//		workflow._nodeName = this._nodeName;
		
		workflow.elaborate();

		// copy uri prefix of the parallel workflow to the workflow instance
		workflow.setUriPrefix(_runUriPrefix.getExpression());

		workflow.configure();
		workflow.initialize();

		workflow._runCount = _runCount;
		workflow._node = _node;
		workflow.setStepCount(_actorStatus.getStepCount());

		// load workflow with input values
		for (Map.Entry<String,Object> entry : _inputValues.entrySet()) {
			String label = entry.getKey();
			Object value = entry.getValue();
			workflow.setInputValue(label, value);
		}
		
		workflow.step();

		// store the workflow output values
		for (String outputName: _outputSignature.keySet()) {
			Object outputValue = workflow.getOutputValue(outputName);
			_storeOutputValue(outputName, outputValue);
		}
	}
	
	public synchronized void dispose() throws Exception {		
		Contract.requires(_state == ActorFSM.WRAPPED_UP);
		_state = ActorFSM.DISPOSED;
	}
}