package org.restflow.metadata;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.restflow.actors.Actor;
import org.restflow.data.PublishedResource;
import org.restflow.data.SingleResourcePacket;
import org.restflow.reporter.Reporter;
import org.restflow.reporter.YamlErrorReporter;
import org.restflow.util.PortableIO;
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
	public void storeWorkflowOutputs(Actor workflow, Map<String, Object> outputBindings) throws Exception {
		
		Map<String,Object> outputValues = workflow.getFinalOutputs();
		
		if (outputBindings != null) {
			for (Map.Entry<String, Object> binding : outputBindings.entrySet()) {
				String resourceName = binding.getKey();
				Object resourceValue = outputValues.get(resourceName);
				String outputFilePath = (String)binding.getValue();
				
				if (outputFilePath.equals("-")) {
					if (resourceValue instanceof File) {
						File publishedFile = (File)resourceValue;
						String fileContents = FileUtils.readFileToString(publishedFile);
						System.out.print(fileContents);
					} else {
						System.out.print(String.valueOf(resourceValue));
					}
				} else {
					if (resourceValue instanceof File) {
						File publishedFile = (File)resourceValue;
						File outputFile = new File(outputFilePath);
						FileUtils.copyFile(publishedFile, outputFile);
					} else {
						File outputFile = new File(outputFilePath);
						FileUtils.writeStringToFile(outputFile, String.valueOf(resourceValue));
					}
				}
			}
		}
		
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
