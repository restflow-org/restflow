package org.restflow.reporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.restflow.metadata.RunMetadata;



/**
 * Implements most of the basic functionality of a Reporter --mostly related
 * to building the model for the report.
 *   
 * Does not implement 'renderReport', which should be implemented
 * in a subclass.
 * 
 * @author scottm
 *
 */
abstract public class BaseReporter implements Reporter {

	String 				_description;
	Boolean 			_includeStdout;	
	Boolean 			_includeWorkflowInputs;
	Map<String,String> 	_reportInflows;
	protected List<String> 		_reportGenerationErrors;
	protected Map<String, Object> _reportModel;	
	Map<String, Object> _tools;		
	
	
	public BaseReporter() {
		_includeStdout = false;	
		_includeWorkflowInputs = false;
		_reportGenerationErrors = new Vector<String>();
		_reportModel = new HashMap<String,Object>();	
		_tools = new HashMap<String,Object>();
	}

	@Override
	public String getDescription() {
		return _description;
	}

	@Override
	public void setDescription(String description) {
		_description = description;
	}

	@Override
	public Boolean getIncludeWorkflowInputs() {
		return _includeWorkflowInputs;
	}

	@Override
	public void setIncludeWorkflowInputs(Boolean includeWorkflowInputs) {
		_includeWorkflowInputs = includeWorkflowInputs;
	}

	@Override
	public Map<String, String> getInflows() {
		return _reportInflows;
	}
	
	@Override
	public void setInflows(Map<String, String> reportBindings) {
		_reportInflows = reportBindings;
	}

	public Boolean getIncludeStdout() {
		return _includeStdout;
	}

	public void setIncludeStdout(Boolean includeStdout) {
		_includeStdout = includeStdout;
	}

	@Override
	public void decorateModel(RunMetadata metadata) {
		
		if (_tools.size() > 0) {
			_reportModel.put("tools", _tools);
		}
		
		// add information about metadata store and process details to model
		Map<String,Object> meta = new HashMap<String,Object>();
		meta.putAll(metadata.getMetadataStorageProperties());
		meta.putAll( metadata.getProcessProperties() );
		_reportModel.put("meta",meta);
			
		if ( getIncludeWorkflowInputs()) { 
			_reportModel.put("inputs", metadata.getInputValues() );
		}

		addProductsToModel( metadata );

		if ( getIncludeStdout()) {
			_reportModel.put("stdout", metadata.getStdout() );
			_reportModel.put("stderr", metadata.getStderr() );				
		}
		
		_reportModel.put("outputs", metadata.getOutputValues());

		if (metadata.getRestoreErrors().size() > 0) {
			_reportModel.put("errors", metadata.getRestoreErrors());
		}	

		_reportModel.put("trace", metadata.getTrace());
	}
	

	/**
	 * Adds the Products file the workflow (typically stored in metadata/products.yaml).
	 * This will be a map of uri variables to publish values.
	 * 
	 * adds the following keys to the model: 'products' 
	 * 
	 * @param runDirectory
	 * @throws IOException
	 */
	private void addProductsToModel(RunMetadata metadata) {
				
		Map<String,Object> productValueMap = metadata.getProductValueMap();				

		_reportModel.put("products", productValueMap);
		
		if (_reportInflows == null) return;
		for (String var : _reportInflows.keySet()) {
			_reportModel.put(var, productValueMap.get(_reportInflows.get(var)));
		}
		
	}
	
	public void addToModel(String key, Object value) {
		_reportModel.put(key, value);
	}

	public Map<String, Object> getTools() {
		return _tools;
	}

	public void setTools(Map<String, Object> tools) {
		_tools = tools;
	}

	@Override
	public void renderReport( ) throws Exception {
		System.out.print(getReport());
	}
}
