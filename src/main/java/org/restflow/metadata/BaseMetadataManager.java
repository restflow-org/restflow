package org.restflow.metadata;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;

import org.restflow.actors.Actor;
import org.restflow.reporter.Reporter;
import org.restflow.reporter.YamlErrorReporter;
import org.restflow.util.StdoutRecorder;


public abstract class BaseMetadataManager implements MetadataManager {

	protected StdoutRecorder _stdoutRecorder;	
	protected RunMetadata _runMetadata;
	
	public BaseMetadataManager() {
		super();
		_runMetadata = new RunMetadata();
	}

	@Override
	public String getStdoutRecording() throws Exception {
		return _stdoutRecorder.getStdoutRecording();
	}

	@Override
	public String getStderrRecording() throws Exception {
		return _stdoutRecorder.getStderrRecording();
	}

	
	@Override
	public void storeWorkflowOutputs(Actor workflow) throws Exception {
		
		Map<String,Object> outputValues = workflow.getFinalOutputs();
		
		outputValues.put("restflow-stdout", getStdoutRecording());

		_runMetadata.setOutputValues(outputValues);
		
		ActorState state = workflow.getFinalState();
		_runMetadata.setActorState(state);
	}		
	
	


	@Override
	public void storeReportDefinitions(Map <String,Reporter> reports) throws IOException {
		_runMetadata.setReporters( reports );
	}

	
	@Override
	public Reporter lookupReport(String reportName) throws Exception {
		
		Reporter reporter = _runMetadata.getReporter(reportName); 
		
		if (reporter != null) {
			return reporter;
		} else {
			return new YamlErrorReporter(_runMetadata);
		}
	}

	public void storeProcessInfo() throws Exception {
		addPidToModel();
	}
	
	protected void addPidToModel() {
		String pidAndHost = ManagementFactory.getRuntimeMXBean().getName();
		String[] splitPidAndHost = pidAndHost.split("@");
		_runMetadata.putProcessProperty("pid", splitPidAndHost[0]);
		_runMetadata.putProcessProperty("host", splitPidAndHost[1]);
	}	
}
