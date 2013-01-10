package org.restflow.beans;

import java.util.List;
import java.util.Map;

import org.restflow.metadata.FileSystemMetadataManager;
import org.restflow.metadata.RunMetadata;
import org.restflow.metadata.Trace;


/**
 * Provides a bean for loading a Metadata structure from within a running workflow.
 * 
 * @author scottm
 *
 */
public class FileSystemMetadataLoader  {

	//inputs
	private String _directory;
	private String _run;
	
	//output
	private RunMetadata _metadata;
	
	// step
	public RunMetadata load() throws Exception {
		try {
			_metadata = FileSystemMetadataManager.restoreMetadata( _directory + "/" + _run);
			return _metadata;

		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * The input values which initiated the workflow. 
	 */
	public Map<String,Object> getRunInputValues() {
		return _metadata.getInputValues(); 
	}
	
	/*The stdout generated from the workflow's run*/
	public String getRunStdout() {
		return _metadata.getStdoutText(); 
	}

	/*The stderr generated from the workflow's run*/
	public String getRunStderr() {
		return _metadata.getStderr(); 
	}

	/**
	 * The output values the workflow produced. 
	 */
	public Map<String,Object> getRunOutputValues() {
		return _metadata.getOutputValues(); 
	}
	
	/**
	 * Errors generated when the metadata was loaded. 
	 */
	public List<String> getRestoreErrors() {
		return _metadata.getRestoreErrors(); 
	}
	
	/**
	 * Get trace data from workflow run. 
	 */
	public Trace getRunTrace() {
		return _metadata.getTrace(); 
	}
	
	public String getDirectory() {
		return _directory;
	}

	public void setDirectory(String directory) {
		this._directory = directory;
	}

	public String getRun() {
		return _run;
	}

	public void setRun(String run) {
		this._run = run;
	}

	public RunMetadata getMetadata() {
		return _metadata;
	}

	public void setMetadata(RunMetadata metadata) {
		this._metadata = metadata;
	}

	
	
	
	
	
}
