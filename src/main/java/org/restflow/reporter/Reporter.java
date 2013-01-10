package org.restflow.reporter;

import java.util.Map;

import org.restflow.metadata.RunMetadata;


/**
 * 
 * The Reporter interface is a view (i.e. report) of the model (i.e. results of a Workflow run).
 * The model can be decorated with various data regarding the run, such as stdout/stderr from the run.
 * 
 * 
 * @author scottm
 *
 */
public interface Reporter {

	public void renderReport( ) throws Exception;
	
	/**
	 * Description Allows the report definition to describe itself after being serialized in the
	 * metadata directory.
	 *  
	 */
	public void setDescription(String description);	
	public String getDescription();
	
	/**
	 * Influences the decoration of the model with the inputs values which initiated the run.
	 */	
	public void setIncludeWorkflowInputs(Boolean includeWorkflowInputs);	
	public Boolean getIncludeWorkflowInputs();

	/**
	 * Influences the decoration of the model with the stdout and stderr from a run.
	 */
	public Boolean getIncludeStdout();
	public void setIncludeStdout(Boolean includeWorkflowInputs);	
	
	/**
	 *  * 'inflows' can be defined which map a uri published during the run, to a key in the report model.
	 * This is then mapped to the uri's published value.  Basically this allows renaming of uri's to simple keys.
	 */
	public void setInflows(Map<String, String> inflows);		
	public Map<String, String> getInflows();

	/**
	 *  Some implementations may be able to access advanced utilities if they are provided in the mode.  For example,
	 *  a yaml parser can be provided to the groovy template reporter.
  	 * @param tools
  	 *  map of tool names to tool objects
	 */
	public void setTools(Map<String, Object> tools);	
	public Map<String, Object> getTools();
	
	public void decorateModel( RunMetadata metadata  );
	public void addToModel(String key, Object value);

	public String getReport() throws Exception;
	
}
