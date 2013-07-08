package org.restflow.metadata;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.restflow.reporter.Reporter;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;


/* This class holds references to all metadata maintained for a run of a workflow.  
 * It is created by a WorkflowRunner when a workflow is run in the first place,
 * and by a MetadataManager when the metadata is restored later from a run directory.
 */
public class RunMetadata {

	/** Properties associated with the persistence of the metadata.
	/*  Could include last modified time, etc. The definition of the keywords 
	 *  and contents of the map is left to the
	 *  MetaDataManager implementations. 
	 */
	private Map<String,Object> _metadataStorageProperties = new LinkedHashMap<String, Object>();

	/** The directory associated with the run */
	private String _runDirectory;	
	
	/** The reports associated with the workflow that was run */
	private Map<String,Reporter> _reporters;
	
	/** Properties associated with operating system process that ran the workflow */
	
	private Map<String,Object> _processProperties = new LinkedHashMap<String,Object>();
	
	/** The data products of the run as a mapping from URI to value */
	private ProductValueMap _productValueMap;
	
	/** The data products of the run represented as a String in YAML format */
	private String _productsYamlString;

	/** A record of all errors that occurred during restoration of the metadata */
	private List<String> _restoreErrors = new Vector<String>();
	
	/** A record of the text sent to stdout during the run */
	private String _stdoutText;

	/** A record of the text sent to stderr during the run */
	private String _stderrText;

	/** The values of workflow inputs provide to this run as a mapping input name to value */
	private Map<String,Object> _inputValues;	
	
	/** The outputs of the workflow run as a mapping from output name to value */
	private Map<String,Object> _outputValues = new LinkedHashMap<String,Object>();

	/** The final state of the workflow (or single actor) that was run */
	private ActorState _actorState;
	
	/** The trace of the actual workflow computation */
	private Trace _trace;
	
	// accessors for reporters
	public void setReporters(Map<String, Reporter> reporters) 		{ _reporters = reporters; }
	public Reporter getReporter(String name) 						{ return _reporters.get(name);  }	
	public int getReporterCount() 									{ return _reporters != null ? _reporters.size() : 0; }
	
	// accessors for run directory
	public void setRunDirectory(String runDirectory) 				{ _runDirectory = runDirectory; }
	public String getRunDirectory() 								{ return _runDirectory; }

	// accessors for process properties
	public void setProcessProperties(Map<String,Object> properties)	{ _processProperties = new HashMap<String,Object>(properties);}
	public void putProcessProperty(String key, Object value) 		{ _processProperties.put(key,value); }
	public void putProcessProperties(Map<String,Object> properties) { _processProperties.putAll(properties);}
	public Map<String, Object> getProcessProperties() 				{ return new HashMap<String,Object>(_processProperties); }
	
	// accessors for workflow products
	public void setProductsYamlString(String yaml) 					{ _productsYamlString = yaml; _productValueMap = new ProductValueMap(yaml); }
	public String getProductsYaml() 							{ return _productsYamlString; }
	public Map<String, Object> getProductValueMap() 				{ return _productValueMap; }
	
	// accessors for workflow stdout
	public void setStdoutText(String stdoutText) 					{_stdoutText = stdoutText; }
	public String getStdout() 									{ return _stdoutText; }
	
	// accessors for workflow stderr
	public void setStderr(String stderrText) 						{ _stderrText = stderrText; }
	public String getStderr() 										{ return _stderrText; }
	
	// accessors for workflow input values
	public void setInputValues(Map<String, Object> inputValues) 	{ _inputValues = inputValues; }
	public Map<String, Object> getInputValues() 					{ return _inputValues; }
	public Object getInputValue(String name)						{ return _inputValues.get(name);}

	// accessors for workflow output values
	public void setOutputValues(Map<String, Object> outputValues) 	{ _outputValues = outputValues; }
	public Map<String, Object> getOutputValues() 					{ return _outputValues; }
	public Object getOutputValue(String name)						{ return _outputValues.get(name); }
	
	public void setRestoreErrors(List<String> restoreErrors) 		{ _restoreErrors = restoreErrors; }
	public List<String> getRestoreErrors() 							{ return _restoreErrors; }
	
	public void setMetadataStorageProperty(String key, Object value){ _metadataStorageProperties.put(key, value); }
	public Map<String, Object> getMetadataStorageProperties()		{ return new HashMap<String,Object>(_metadataStorageProperties); }

	public void setActorState(ActorState actorState) 				{ _actorState = actorState; }
	public ActorState getActorState() 								{ return _actorState; }
	
	public void setTrace(Trace trace)	 							{ _trace = trace; }
	public Trace getTrace() 										{ return _trace; }
	
	//Class to map the products map, converting !file objects into String when requested
	private class ProductValueMap implements Map<String,Object> {

		private Map<String,Object> _productsObjectMap;
		
		public ProductValueMap(String yaml) {
		
			// parse the YAML to yield a Map<String,Object>, replacing each
			// value designated a !file with a File object with a path equal to that value
			Constructor constructor = new Constructor();
			constructor.addTypeDescription(new TypeDescription(File.class, "!file"));
			Yaml reader = new Yaml(constructor);
			
			_productsObjectMap = (Map<String,Object>)reader.load(yaml);
		}
		
		public Object get(Object key) {
			
			Object value = _productsObjectMap.get(key);
			
			if (value instanceof File) {
				File fileWithRelativePath = (File)value;
				String absolutePathToFile = _runDirectory + fileWithRelativePath.getPath();
				File actualFile = new File(absolutePathToFile);
				value = actualFile;
			}
			
			return value;
		}

		@Override public int size() {return _productsObjectMap.size(); }
		@Override public boolean isEmpty() { return _productsObjectMap.isEmpty(); }
		@Override public boolean containsKey(Object key) { return _productsObjectMap.containsKey(key); }
		@Override public Set<String> keySet() { return _productsObjectMap.keySet(); }
		
		@Override public boolean containsValue(Object value) {throw new UnsupportedOperationException();}
		@Override public Collection<Object> values() {throw new UnsupportedOperationException();}
		@Override public Set<java.util.Map.Entry<String, Object>> entrySet() { return _productsObjectMap.entrySet();}
		@Override public Object put(String key, Object value) { throw new UnsupportedOperationException();}
		@Override public Object remove(Object key) { throw new UnsupportedOperationException(); }
		@Override public void putAll(Map<? extends String, ? extends Object> m) { throw new UnsupportedOperationException(); }
		@Override public void clear() { _productsObjectMap.clear(); }
	}	
}
