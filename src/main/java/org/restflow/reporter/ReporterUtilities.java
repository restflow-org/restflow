package org.restflow.reporter;

import org.restflow.metadata.FileSystemMetadataManager;
import org.restflow.metadata.RunMetadata;


public class ReporterUtilities {

	
	/**
	 * Looks into a workflow run directory, loads the serialized (yaml) report definitions and then checks 
	 * to see if the requested report is available.  If the report is available, return the definition.
	 * 
	 * @author scottm
	 *
	 */
	static public Reporter loadReporterFromRunDirectory(String directory, String reporterName) throws Exception {
		
		// Get the reporters associated with the workflow that was run in this directory
		RunMetadata metadata = FileSystemMetadataManager.restoreMetadata(directory);
		
		if (metadata.getReporterCount() == 0) {
			return new YamlErrorReporter(metadata);
		}
		
		// attempt to load the requested reporter
		Reporter reporter = metadata.getReporter(reporterName);
			
		// create a yaml error reporter if the requested reporter was not found
		if (reporter == null) {
			metadata.getRestoreErrors().add("Report '" + reporterName + "' not defined in " + directory);
			reporter = new YamlErrorReporter(metadata);
		}
			
		// fill out the reporter model with run metadata
		reporter.decorateModel( metadata );
		
		// return the reporter
		return reporter;	
	}
	
}
