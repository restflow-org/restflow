package org.restflow.actors;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.restflow.data.DirectProtocol;
import org.restflow.data.Outflow;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;


/**
 * This class is thread safe.  Its superclass is thread safe, and all the mutable fields 
 * it adds are synchronized on the instance.  The clone method marks the Groovy scripts
 * as uncompiled so that each clone will compile and run its own instances of the compiled 
 * versions of the scripts.
 */


@ThreadSafe()
public class GroovyActor extends ScriptActor {

    @GuardedBy("this") private String 		  	  _wrapperScript;
    @GuardedBy("this") private Map<String,Script> _compiledScripts;    
    
	public GroovyActor() {
		super();
		
		synchronized(this) {
			_compiledScripts = new HashMap<String, Script>();
		}
	}

	public synchronized Object clone() throws CloneNotSupportedException {
		GroovyActor theClone = (GroovyActor) super.clone();
		theClone._compiledScripts = new HashMap<String, Script>();
		return theClone;
	}
		
	public synchronized void setWrapperScript(String script) {
		_wrapperScript = script;
	}

	public synchronized void configure() throws Exception {
		
		super.configure();
	
		if (_configureScript != null) {

			Binding binding = new Binding();
	
			_bindConstants(binding);
			binding.setVariable("_outputs", EMPTY_STRING_LIST);
			bindSpecial(binding);
	
			runScript(_configureScript, binding);
	}
		
		_state = ActorFSM.CONFIGURED;
	}
	
	@Override
	public synchronized void initialize() throws Exception {
	
		super.initialize();
		
		if (_initializeScript != null) {
			
			Binding binding = new Binding();

			_bindConstants(binding);
			_bindStateVariables(binding);
			_bindInputs(binding);
			binding.setVariable("_outputs", EMPTY_STRING_LIST);
			bindSpecial(binding);

			runScript(_initializeScript, binding);
		}
		
		_state = ActorFSM.INITIALIZED;		
	}
	
	@Override
	public synchronized void step() throws Exception {
		
		super.step();

		if (_stepScript != null) {
			
			Binding binding = new Binding();

			_bindConstants(binding);
			_bindStateVariables(binding);
			_bindInputs(binding);
			binding.setVariable("_outputs", _outputSignature.keySet());
			bindSpecial(binding);
			bindDirectories(binding);

			try {
				runScript(_stepScript, binding);
			} catch (Exception e) {
				throw e;
			} finally {
				_state = ActorFSM.STEPPED;
			}
			
			if (_logger != null) {
				binding.setVariable("__log__", _logger.getMessages());
				_logger.clear();
			}
			
			_updateOutputVariables((Map<String,Object>)binding.getVariables());			
		}
	}

	
	@Override
	public synchronized void wrapup() throws Exception {
		
		super.wrapup();
		
		if (_wrapupScript != null) {
			
			Binding binding = new Binding();
			
			_bindConstants(binding);
			_bindStateVariables(binding);
			_bindInputs(binding);
			binding.setVariable("_outputs", EMPTY_STRING_LIST);
			bindSpecial(binding);
	
			runScript(_wrapupScript, binding);
		}
		
		_state = ActorFSM.WRAPPED_UP;
	}

	@Override
	public synchronized void dispose() throws Exception {
		
		super.dispose();
		
		if (_disposeScript != null) {
			
			Binding binding = new Binding();
	
			_bindConstants(binding);			
			_bindStateVariables(binding);
			_bindInputs(binding);
			binding.setVariable("_outputs", EMPTY_STRING_LIST);
			bindSpecial(binding);
	
			runScript(_disposeScript, binding);
		}
		
		_state = ActorFSM.DISPOSED;
	}
	
	protected synchronized void runScript(String script, Binding binding) throws Exception {
		
		runTheScript(script, binding);
		
		_actorStatus = (ActorStatus)binding.getVariable("_status");
		
		_updateStateVariables((Map<String,Object>)binding.getVariables());
	}

	protected synchronized void runTheScript(String script, Binding binding) throws Exception {
		
		Script groovyScript;
		
		if (_wrapperScript == null ) {
			groovyScript = _getCompiledGroovyScript(script);
		} else {
			groovyScript = _getCompiledGroovyScript(_wrapperScript);
		}
		
		binding.setVariable("_script", script);
		
		_executeGroovyScript(groovyScript, binding);	
	}
	
	private synchronized void _executeGroovyScript(Script script, Binding binding) throws Exception {
		
		script.setBinding(binding);
		
		try {
			script.run();
		} catch (Exception e) {
			logger.debug("Runtime error in groovy script of actor " + 
					getName() + " of "+ getNodeName() + " during " + 
					_actorStatus.getCallType() );
			throw e;
		}		
	}
	
	private synchronized Script _getCompiledGroovyScript(String script) {
		
		Script compiledScript = _compiledScripts.get(script);
		
		if (compiledScript == null) {
	        String scriptPrefix = "def log(message) { _logger.add(\"$message\"); };";
			String prefixedScript = scriptPrefix + script;
			compiledScript =  new GroovyShell().parse(prefixedScript);
			_compiledScripts.put(script, compiledScript);
		}
		
		return compiledScript;
	}
	
	private synchronized void bindSpecial(Binding binding) {
		
		binding.setVariable("_inputs", _inputValues);
		binding.setVariable("_logger", _logger);
		binding.setVariable("_states", _stateVariables);
		binding.setVariable("_status" , _actorStatus);
		binding.setVariable("_type", _variableTypes);
		binding.setVariable("_this" , this);
		binding.setVariable("STEP", _actorStatus.getStepCount());
	}
	
	private synchronized void bindDirectories(Binding binding) throws Exception {
		
		String runDirectoryPath = _workflowContext.getRunDirectoryPath();
		if (runDirectoryPath != null) {

			binding.setVariable("_runDir", runDirectoryPath);
			
			Map<String,String> outflowDirectoryMap = new HashMap<String,String>();
			binding.setVariable("_outflowDirectory", outflowDirectoryMap);
			
			if (_node == null) return; //It is possible that this is an actor running standalone, without a node
			
			for (Outflow outflow: _node.getOutflows().values()) {
				if (outflow.getProtocol() instanceof DirectProtocol) {
					String nestedUriPrefix = _node.getUriPrefix();
	//				String localPath = outflow.getUriTemplate().getPath();
					String localPath = outflow.getUriTemplate().getExpandedPath(binding.getVariables(), new Object[10]);
					String absolutePath = runDirectoryPath + "/" + nestedUriPrefix + "/" + localPath + "/";
					outflowDirectoryMap.put(outflow.getLabel(), absolutePath);
					File outflowDirectory = new File(absolutePath);
					outflowDirectory.mkdirs();
				}
			}
		}		
	}

	private synchronized void _bindConstants(Binding binding) {
		for (String name: _constants.keySet()) {
			Object value = _constants.get(name);
			binding.setVariable(name,value);
		}
	}

	private synchronized void _bindStateVariables(Binding binding) {
		for (String name: _stateVariables.keySet()) {
			Object value = _stateVariables.get(name);
			binding.setVariable(name, value);
		}
	}	
	
	private synchronized void _bindInputs(Binding binding) {
		for (String name: _inputSignature.keySet()) {
			binding.setVariable( name, _inputValues.get(name) );
		}
	}	
}