package org.restflow.beans;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.Actor;
import org.restflow.actors.Workflow;
import org.restflow.data.ProtocolRegistry;
import org.restflow.data.Uri;
import org.restflow.directors.Director;
import org.restflow.metadata.MetadataManager;
import org.restflow.metadata.TraceRecorder;

import org.restflow.yaml.spring.YamlBeanDefinitionReader;

/**
 * Provides a bean for loading workflow & actor definitions from within a 
 * running workflow.
 * 
 * @author scottm
 *
 */
public class ActorLoader  {

	WorkflowContext _context;
	MetadataManager _metadataMgr;
	
	//inputs
	private String _restflowFile;
	private String _actorName;
	private String _runDirectory;
	private Map<String, String> _importMap;
	private Director _director;
	
	//output
	private Actor _actor;
	

	public void initialize() throws Exception {
		_director = null;
		_context = null;
		_importMap = null;
	}
	
	// step
	public Actor load() throws Exception {
		
		try {
			
			if (_context == null) {
				_createNewContext();
			}
			TraceRecorder traceRecorder = _context.getTraceRecorder();			

			_actor = (Actor) _context.getBean(_actorName);

			if (_actor instanceof Workflow) {
				Workflow w = (Workflow) _actor;
				if (_director != null) {
					w.setDirector(_director);
				}
			}

			ProtocolRegistry protocolRegistry = 
				(ProtocolRegistry)_context.getBean("DefaultProtocolRegistry");
			if (protocolRegistry != null) {
				_context.setProtocolRegistry(protocolRegistry);
			}
			
			_actor.elaborate();
			// store workflow graph in trace DB if provided and not running a standalone actor
			if (_actor instanceof Workflow) {
				traceRecorder.recordWorkflowGraph((Workflow)_actor);
			}			
			//_actor.configure();
			//_actor.initialize();

			return _actor;

		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw e;
		}
	}

	private void _createNewContext() throws Exception, ClassNotFoundException {

		if ( _importMap == null) _importMap = new HashMap<String,String>();
		if ( !_importMap.containsKey("workspace")) {
			_importMap.put("workspace", Uri.extractParent(_restflowFile));
		}		

		if (_runDirectory == null ) {
			_context = new WorkflowContextBuilder()
				.importMappings(_importMap)
				.build();
		} else {
			_context = new WorkflowContextBuilder()
			.importMappings(_importMap)
			.runDirectory(new File(_runDirectory))
			.build();			
		}
			
		YamlBeanDefinitionReader rdr = new YamlBeanDefinitionReader(_context);
		rdr.registerBeanDefinitions(_restflowFile);
		_context.refresh();

		String actorBeanNames[] = _context.getBeanNamesForType(
				Class.forName("org.restflow.actors.Actor"));

		if (actorBeanNames.length == 0) {
			throw new Exception(
					"Must have at least one Actor defined in workflow script.");
		}
	}
	
	//getters & setters
	
	public String getRestflowFile() {
		return _restflowFile;
	}
	
	public void setRestflowFile(String restflowFile) {
		_restflowFile = restflowFile;
	}
	
	public String getActorName() {
		return _actorName;
	}
	
	public void setActorName(String actorName) {
		_actorName = actorName;
	}
	
	public Map<String, String> getImportMap() {
		return _importMap;
	}
	
	public void setImportMap(Map<String, String> importMap) {
		this._importMap = importMap;
	}
	
	public Actor getActor() {
		return _actor;
	}
	public void setActor(Actor actor) {
		this._actor = actor;
	}

	public Director getDirector() {
		return _director;
	}

	public void setDirector(Director director) {
		_director = director;
	}

	public WorkflowContext getContext() {
		return _context;
	}

	public void setContext(WorkflowContext context) {
		_context = context;
	}

	public MetadataManager getMetadataMgr() {
		return _metadataMgr;
	}

	public void setMetadataMgr(MetadataManager metadataMgr) {
		_metadataMgr = metadataMgr;
	}

	public String getRunDirectory() {
		return _runDirectory;
	}

	public void setRunDirectory(String runDirectory) {
		_runDirectory = runDirectory;
	}
	
	
	
}
