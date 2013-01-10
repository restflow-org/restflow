package org.restflow.metadata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.restflow.actors.Actor;
import org.restflow.reporter.Reporter;
import org.restflow.util.StdoutRecorder;


public interface MetadataManager  {

	/** Defines a directory on the file system in which the workflow is run */
	public void setRunDirectory(String runDirectory)  throws Exception ;
	public String getRunDirectory();
	
	/**
	 * The inputs to the workflow are stored as part of a run's metadata
	 * 
	 * @param inputBindings
	 * @throws Exception
	 */
	public void storeWorkflowInputs(Map<String,Object> inputBindings) throws Exception;
	
	
	/**
	 * Store info host and pid of the workflow process.
	 * 
	 * @throws Exception
	 */
	public void storeProcessInfo() throws Exception;
	
	public void storeReportDefinitions(Map<String,Reporter> reports) throws IOException;
	
	/**
	 * 
	 * A recording of the stdout can be stored as part of the run metadata.
	 * Leave the implementation of the storage to the MetaDataManager.
	 * 
	 * @param suppressTerminalOutput
	 * @return
	 * @throws FileNotFoundException
	 */
	public StdoutRecorder buildStdoutRecorder(boolean suppressTerminalOutput) throws FileNotFoundException;	
	
	
	/**
	 * 
	 * After a workflow run, store the outputs of the workflow
	 * 
	 * @param outputValues
	 * @throws Exception
	 */
	public void storeWorkflowOutputs(Actor actor) throws Exception;
	
	public String getStdoutRecording() throws Exception;
	
	public String getStderrRecording() throws Exception;

	public Reporter lookupReport(String reportName) throws Exception;	
	
	public RunMetadata getRunMetadata(TraceRecorder recorder) throws Exception;
	
	public void writeToLog(String logName, String message);

	public void writeToProductsFile(String message);
}
