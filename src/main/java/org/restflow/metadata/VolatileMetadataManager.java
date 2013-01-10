package org.restflow.metadata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.restflow.reporter.Reporter;
import org.restflow.util.StdoutRecorder;


public class VolatileMetadataManager extends BaseMetadataManager {

	@Override
	public String getRunDirectory() {
		return null;
	}

	@Override
	public void setRunDirectory(String uniqueWorkingDirectory) {}

	@Override
	public void storeReportDefinitions(Map<String, Reporter> reports)
			throws IOException {
	}

	@Override
	public void storeWorkflowInputs(Map<String, Object> inputBindings)
			throws Exception {
	}

	@Override
	public StdoutRecorder buildStdoutRecorder(boolean suppressTerminalOutput) throws FileNotFoundException {
		if (_stdoutRecorder != null) return _stdoutRecorder;
		
		_stdoutRecorder = new StdoutRecorder(suppressTerminalOutput);
		return _stdoutRecorder;
	}
	
	/**
	 * Allows an active instance of the MetaDataManager to return a copy of the current metadata.
	 * 
	 * @return
	 * @throws Exception
	 */
	@Override
	public RunMetadata getRunMetadata(TraceRecorder recorder) throws Exception {
		
		RunMetadata metadata = new RunMetadata();
		metadata.setRunDirectory(getRunDirectory());
		metadata.putProcessProperties(_runMetadata.getProcessProperties());
		metadata.getOutputValues().putAll(_runMetadata.getOutputValues());		
		
		Trace trace = recorder.getReadOnlyTrace();
		metadata.setTrace(trace);
		
		metadata.setActorState(_runMetadata.getActorState());
		return metadata;
	}

	@Override
	public void writeToLog(String logName, String message) {
	}

	@Override
	public void writeToProductsFile(String message) {
	}
}
