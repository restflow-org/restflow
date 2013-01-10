package org.restflow;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import net.jcip.annotations.GuardedBy;

import org.restflow.data.ProtocolRegistry;
import org.restflow.metadata.BasicTraceRecorder;
import org.restflow.metadata.FileSystemMetadataManager;
import org.restflow.metadata.MetadataManager;
import org.restflow.metadata.Trace;
import org.restflow.metadata.TraceRecorder;
import org.restflow.metadata.VolatileMetadataManager;
import org.restflow.metadata.WritableTrace;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;


/**
 * This class is thread safe under the assumption that the superclass is thread safe.
 * It's mutable fields are synchronized on the corresponding instance.  The contents
 * of the resource map passed to the constructor is copied to an internal map, and the
 * map itself is never published.  The File object passed to setRunDirectory is used
 * only to extract the absolute path of the File and it's parent, both of which are stored 
 * and subsequently published as strings.
 */
public class WorkflowContext extends GenericApplicationContext {

	// final private fields
	@GuardedBy("this") private String 					_baseDirectoryPath;
	@GuardedBy("this") private final Map<String,Object> _dataStore;
	@GuardedBy("this") private MetadataManager 			_metadataManager;
	@GuardedBy("this") private final Map<String,String>	_importMappings;	
	@GuardedBy("this") private String 					_runDirectoryPath;
	@GuardedBy("this") private final TraceRecorder 		_traceRecorder;
	@GuardedBy("this") private Hashtable<String, Object> _properties;
	@GuardedBy("this") private String 					_workflowDefinitionString;
	@GuardedBy("this") private String 					_workflowDefinitionPath;

	// mutable private fields
	@GuardedBy("this") private ProtocolRegistry 		_protocolRegistry;
	
	protected WorkflowContext(
			MetadataManager 	metadataManager, 
			Properties 			properties, 
			Map<String,String>  importMappings,
			TraceRecorder		traceRecorder,
			Map<String,Object> 	dataStore,
			ProtocolRegistry 	protocolRegistry,
			File 				runDirectory,
			String				workflowDefinitionString,
			String				workflowDefinitionPath) throws Exception {

		super();
		
		synchronized(this) {
			
			_workflowDefinitionString = workflowDefinitionString;
			_workflowDefinitionPath = workflowDefinitionPath;
			
			_properties = new Hashtable<String,Object>();
			if (properties != null) {
				for (Entry<Object, Object> entry : properties.entrySet()) {
					_properties.put((String)entry.getKey(), entry.getValue());
				}
			}

			_importMappings = new HashMap<String,String>();
			if (importMappings != null) {
				_importMappings.putAll(importMappings);
			}
			
			// configure the trace recorder, creating one if neeeded
			if (traceRecorder != null) {
				_traceRecorder = traceRecorder;
			} else {
				_traceRecorder = new BasicTraceRecorder();
			}

			if (metadataManager != null) {
				setMetadataManager(metadataManager);
			} else {
				_metadataManager = new VolatileMetadataManager();
			}
			
			_traceRecorder.setApplicationContext(this);
			
			_dataStore = dataStore;
			if (_dataStore != null) {
				_traceRecorder.setDataStore(_dataStore);
			}

			if (protocolRegistry != null) {
				_protocolRegistry = protocolRegistry;
			} else {
				throw new Exception("Must provide workflow context with a protocol registry");
			}

			if (runDirectory != null) {
				setRunDirectory(runDirectory);
			}
		}
	}
	
	public Object getProperty(String key) {
		
		Object value = null;
		
		// first look for the key in the local context properties
		value = _properties.get(key);
		
		if (value == null) {

			// then check the JVM properties
			value = System.getProperty(key);
			if (value == null) {
				
				// finally look among OS environment variables
				value = System.getenv(key);
			}
		}

		// value will be null if key not found
		return value;
	}
	
	
	public synchronized void setMetadataManager(MetadataManager manager) throws Exception {
		
		_metadataManager = manager;

		if (_metadataManager instanceof FileSystemMetadataManager) {
			((FileSystemMetadataManager)_metadataManager).createProductsFileStream();
		}
	}
	
	@Override
	public synchronized Resource getResource(String location) {
		
		int schemeEndLocation = location.indexOf(":");
		if (schemeEndLocation>0) {
			String locationScheme = location.substring(0, schemeEndLocation );

			String mappedLocation = _importMappings.get(locationScheme);		
			if ( mappedLocation != null) {
				location = location.replaceFirst(locationScheme+":", mappedLocation);
			}
		}

		return super.getResource(location);
	}

	public synchronized void setProtocolRegistry(ProtocolRegistry registry) {
		_protocolRegistry = registry;
	}
	
	public synchronized TraceRecorder getTraceRecorder() {
		return _traceRecorder;
	}

	public synchronized Map<String,Object> getDataStore() {
		return _dataStore;
	}

	public synchronized String getRunDirectoryPath() {
		return _runDirectoryPath;
	}

	public synchronized String getBaseDirectoryPath() {
		return _baseDirectoryPath;
	}
	
	public synchronized MetadataManager getMetaDataManager() {
		return _metadataManager;
	}
	
	public synchronized Map<String, String> getImportMappings() {
		return _importMappings;
	}
	
	public synchronized ProtocolRegistry getProtocolRegistry() {
		return _protocolRegistry;
	}
	
	public void setRunDirectory(File runDirectory) {
		_runDirectoryPath = runDirectory.getAbsolutePath();
		_baseDirectoryPath = new File(_runDirectoryPath).getParentFile().getAbsolutePath();
	}
	
	public static String getUserTemporaryDirectoryPath() {
		 return System.getProperty("java.io.tmpdir");
	}
	
	public String getWorkflowDefinitionString() {
		return _workflowDefinitionString;
	}
	
	public String getWorkflowDefinitionPath() {
		return _workflowDefinitionPath;
	}

	
	public WritableTrace getWritableTrace() {
		return ((BasicTraceRecorder)_traceRecorder).getWritableTrace();
	}
	
	public Trace getTrace() throws Exception {
		Trace trace = _metadataManager.getRunMetadata(_traceRecorder).getTrace();
		return trace;
	}

}