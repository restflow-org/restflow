package org.restflow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.restflow.data.Protocol;
import org.restflow.data.ProtocolRegistry;
import org.restflow.metadata.MetadataManager;
import org.restflow.metadata.TraceRecorder;


public class WorkflowContextBuilder {

	private Map<String,Object> 		_dataStore;
	private Map<String,String> 		_importMappings = new HashMap<String,String>();
	private MetadataManager 		_metadataManager;
	private ProtocolRegistry 		_protocolRegistry;
	private Map<String, Protocol>	_protocols = new HashMap<String,Protocol>();
	private TraceRecorder			_recorder;
	private Properties				_properties = new Properties();
	private File 					_runDirectory;
	private String					_workflowDefinitionString;
	private String					_workflowDefinitionPath;
	
	public WorkflowContextBuilder protocolRegistry(ProtocolRegistry registry) {
		_protocolRegistry = registry;
		return this;
	}

	public WorkflowContextBuilder scheme(String scheme, Protocol protocol) {
		_protocols.put(scheme, protocol);
		return this;
	}
	
	public WorkflowContextBuilder property(String key, Object value) {
		_properties.put(key, value);
		return this;
	}

	public WorkflowContextBuilder properties(Map<String,Object> properties) {
		if (properties != null) {
			_properties.putAll(properties);
		}
		return this;
	}

	public WorkflowContextBuilder properties(Properties properties) {
		if (properties != null) {
			_properties.putAll(properties);
		}
		return this;
	}
	
	public WorkflowContextBuilder importMapping(String key, String value) {
		_importMappings.put(key, value);
		return this;
	}

	public WorkflowContextBuilder importMappings(Map<String,String> mappings) {
		if (mappings != null) {
			_importMappings.putAll(mappings);
		}
		return this;
	}

	public WorkflowContextBuilder runDirectory(File runDirectory) {
		_runDirectory = runDirectory;
		return this;
	}
	

	public WorkflowContextBuilder metadataManager(MetadataManager metadataManager) {
		_metadataManager = metadataManager;
		return this;
	}

	public WorkflowContextBuilder recorder(TraceRecorder recorder) {
		_recorder = recorder;
		return this;
	}	
	
	public WorkflowContextBuilder store(Map<String,Object> store) {
		_dataStore = store;
		return this;
	}


	public WorkflowContextBuilder workflowDefinitionString(String workflowDefinitionString) {
		_workflowDefinitionString = workflowDefinitionString;
		return this;
	}

	public WorkflowContextBuilder workflowDefinitionPath(String workflowDefinitionPath) {
		_workflowDefinitionPath = workflowDefinitionPath;
		return this;
	}
	
	public WorkflowContext build() throws Exception {
		
		if (_protocolRegistry == null) {
			_protocolRegistry = new ProtocolRegistry();
		}
		
		// create a new WorkflowContext
		WorkflowContext context = new WorkflowContext(
				_metadataManager,
				_properties,
				_importMappings, 
				_recorder,
				_dataStore,
				_protocolRegistry,
				_runDirectory,
				_workflowDefinitionString,
				_workflowDefinitionPath
		);
		
		// fill a protocol registry with given protocols if provided
		if (!_protocols.isEmpty()) {
			_protocolRegistry.setProtocols(_protocols);
		}
		
		for (Protocol protocol : _protocolRegistry.values()) {
			protocol.setApplicationContext(context);
		}
		
	return context;
	}
}
