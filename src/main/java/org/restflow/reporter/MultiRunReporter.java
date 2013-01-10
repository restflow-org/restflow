package org.restflow.reporter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.restflow.metadata.FileSystemMetadataManager;
import org.restflow.metadata.RunMetadata;
import org.yaml.snakeyaml.Yaml;



/**
 * 
 * Given a base directory and a reportName, will look into each
 * of the sub directories for a report definition and if available
 * run the report on the directory.
 * 
 * @author scottm
 *
 */
public class MultiRunReporter {

	private File _baseDir;
	private String _reportName;

	public MultiRunReporter(String baseDirectory, String reportName) {
		super();
		_baseDir = new File(baseDirectory);
		_reportName = reportName;
	}

	public void renderReport() throws Exception {

		String[] dirs =_baseDir.list(DirectoryFileFilter.INSTANCE);
		
		 for ( int i = 0; i < dirs.length; i++ ) {
			 String runDir = _baseDir+ "/"+ dirs[i];
		     System.out.println("---");
		     try {
		    	 Reporter reporter = ReporterUtilities.loadReporterFromRunDirectory(runDir, _reportName);
		    	 
		    	 RunMetadata metadata = FileSystemMetadataManager.restoreMetadata(runDir);
		    	 reporter.decorateModel(metadata);
		    	 reporter.renderReport();
		     } catch (Exception e) {
		    	 HashMap<String,Object> model = new HashMap<String,Object>();
		    	 List<String> errors = new Vector<String>();
		    	 errors.add(e.getMessage());
		    	 model.put("errors",errors);
		    	 Yaml yaml = new Yaml();
		    	 System.out.print(yaml.dump(model));
		     }
		 }
	}

}
