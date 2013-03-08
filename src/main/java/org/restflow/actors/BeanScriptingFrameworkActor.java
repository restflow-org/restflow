package org.restflow.actors;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@NotThreadSafe()
public class BeanScriptingFrameworkActor extends AbstractActor {

    protected final Log logger = LogFactory.getLog(getClass());

    private ApplicationContext applicationContext;
    
	private String preWorkflow;
	private String initialize;
	private String step;
	private String wrapup;
	
	private String wrapperScript;

	private Script compiledWrapperScript;
	private Script compiledPreWorkflowScript;
	private Script compiledInitializeScript;
	private Script compiledStepScript;
	private Script compiledWrapupScript;
	
	private boolean _compiled = false;
	
	public void precompile() {
		
		if (! _compiled) {
			
			if (wrapperScript != null) {
				compiledWrapperScript = new GroovyShell().parse(wrapperScript);
			}
			
			if (preWorkflow != null) { 
				compiledPreWorkflowScript = new GroovyShell().parse(preWorkflow);
			}
			
			_compiled = true;
		}
	}

	public void configure() throws Exception {
		super.configure();
	}
	
	@Override
	public synchronized void initialize() throws Exception {
	
		if (initialize != null) {

			_actorStatus.setCallType("initialize");
			
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");

			bindStateVariables(engine);
			bindLatestInputs(engine);
			bindConstants(engine);
			bindSpecial(engine);
			
			try {
				engine.eval( initialize );
			} catch (ScriptException ex) {
				logger.error("Runtime error in JavaScript script of actor " + 
						getName() + " of "+ getQualifiedParentNodeName() + " during " + 
						_actorStatus.getCallType() );
				throw new RuntimeException(ex);
			}
			
			_actorStatus = (ActorStatus)engine.get("_status");

			for (String key : engine.getBindings(ScriptContext.ENGINE_SCOPE).keySet() ) {
				_outputValues.put(key, engine.get(key));
			}
			
			
			for (String label : _stateVariables.keySet()) {
				Object value = engine.get(label);
				_stateVariables.put(label, value);
			}		
		}
	}
	
	@Override
	public synchronized void step() throws Exception {
		
		super.step();

		if (step != null) {

			_actorStatus.setCallType("step");

			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");

			bindStateVariables(engine);
			bindLatestInputs(engine);
			bindConstants(engine);
			bindSpecial(engine);
			
			try {
				engine.eval( step );
			} catch (ScriptException ex) {
				logger.error("Runtime error in JavaScript script of actor " + 
						getName() + " of "+ getQualifiedParentNodeName() + " during " + 
						_actorStatus.getCallType() );
				throw new RuntimeException(ex);
			}
			

			_actorStatus = (ActorStatus)engine.get("_status");
			_outputValues = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			
			for (String label : _stateVariables.keySet()) {
				Object value = engine.get(label);
				_stateVariables.put(label, value);
			}	
			
		}
	}

	@Override
	public synchronized void wrapup() throws Exception {
		
		if (wrapup != null) {

			_actorStatus.setCallType("wrapup");
			
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");

			bindStateVariables(engine);
			bindLatestInputs(engine);
			bindConstants(engine);
			bindSpecial(engine);
			
			try {
				engine.eval( wrapup );
			} catch (ScriptException ex) {
				logger.error("Runtime error in JavaScript script of actor " + 
						getName() + " of "+ getQualifiedParentNodeName() + " during " + 
						_actorStatus.getCallType() );
				throw new RuntimeException(ex);
			}
			

			_actorStatus = (ActorStatus)engine.get("_status");
			_outputValues = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			
			for (String label : _stateVariables.keySet()) {
				Object value = engine.get(label);
				_stateVariables.put(label, value);
			}	

			
		}
	}
	

	public void runGroovyScript(String script, Binding binding) {
		
		
	}

	private synchronized void bindSpecial(ScriptEngine engine) {
		engine.put("_inputs", _inputValues);
		engine.put("_states", _stateVariables);
		engine.put("_outputs", _outputSignature.keySet());
		engine.put("_status" , _actorStatus);
		//binding.setVariable("_statusMap" , _actorStatus.buildRpcObject());		
		engine.put("_this" , this);
	}

	private synchronized void bindConstants(ScriptEngine engine) {
		for (String name: _constants.keySet()) {
			Object value = _constants.get(name);
			engine.put(name,value);
		}
	}

	private synchronized void bindStateVariables(ScriptEngine engine) {
		for (String name: _stateVariables.keySet()) {
			Object value = _stateVariables.get(name);
			engine.put(name,value);
		}
	}	
	
	private synchronized void bindLatestInputs(ScriptEngine engine) {
		//override any previous state with the latest inputs.
		for (String varName: _inputSignature.keySet()) {
			engine.put( varName, _inputValues.get(varName) );
		}
	}

	public synchronized void copyValuesToSubordinate (BeanScriptingFrameworkActor actor) throws Exception {
	      actor.setStep( getStep() );
	      actor.setInitialize( getInitialize() );
	      actor.setWrapup( getWrapup() );
	      actor.setName( getName() + "_subordinate");
	      actor.setStateful(isStateful());
//	      actor.setInputs(_inputs);
//	      actor.setOutputs(_outputs);
	      actor.initialize();
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getWrapup() {
		return wrapup;
	}

	public void setWrapup(String wrapup) {
		this.wrapup = wrapup;
	}

	public String getInitialize() {
		return initialize;
	}

	public void setInitialize(String initialize) {
		this.initialize = initialize;
	}

	public String getPreWorkflow() {
		return preWorkflow;
	}

	public void setPreWorkflow(String preWorkflow) {
		this.preWorkflow = preWorkflow;
	}

	public String getScriptWrapper() {
		return wrapperScript;
	}

	public void setWrapperScript(String script) {
		this.wrapperScript = script;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		//_actorStatus.setApplicationContext(applicationContext);
	}
}