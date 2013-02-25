package org.restflow.actors;

import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restflow.actors.Actor.ActorFSM;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;


/**
 * This class is thread safe.  Its superclass is thread safe, and all the mutable fields 
 * it adds are synchronized on the instance.  
 * 
 * The template is a special input needed by the actor; the key to this input is defined
 * in the static definition below  All other inputs defined by the workflow enter the
 * model and are available to the template engine.
 */

@ThreadSafe()
public class GroovyTemplateActor extends AbstractActor {
	public static final String TEMPLATE_INPUTKEY = "_template";
	
	@GuardedBy("this") protected Log logger;

    
	public GroovyTemplateActor() {
		super();
		
		synchronized(this) {
			logger = LogFactory.getLog(getClass());
		}
	}
	
	@Override
	public synchronized Object clone() throws CloneNotSupportedException {
		GroovyTemplateActor theClone = (GroovyTemplateActor) super.clone();
		theClone.logger = LogFactory.getLog(getClass());
		return theClone;
	}
	
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		_state = ActorFSM.PROPERTIES_SET;
	}
	
	public void elaborate() throws Exception {
		super.elaborate();
		_state = ActorFSM.ELABORATED;
	}
	
	public synchronized void configure() throws Exception {
		super.configure();		
		if ( _outputSignature.size() != 1 ) {
			throw new Exception("GroovyTemplateActor must have exactly one output.");
		}
		_state = ActorFSM.CONFIGURED;
	}
	
	public synchronized void initialize() throws Exception {
		super.initialize();
		_state = ActorFSM.INITIALIZED;		
	}
	
	@Override
	public synchronized void step() throws Exception {
		super.step();

		SimpleTemplateEngine engine = new SimpleTemplateEngine();

		Map<String, Object> model = new HashMap<String, Object>();

		for (String key : _inputSignature.keySet()) {
			model.put(key, _inputValues.get(key));
		}
		bindSpecial(model);
		bindConstants(model);

		String template = (String) model.get(TEMPLATE_INPUTKEY);

		Writable template1 = engine.createTemplate(template).make(model);
		String view = template1.toString();

		for (String key : _outputSignature.keySet()) {
			_outputValues.put(key, view);
		}

		_state = ActorFSM.STEPPED;
	}

	public synchronized void wrapup() throws Exception {	
		super.wrapup();
		_state = ActorFSM.WRAPPED_UP;
	}
	
	public synchronized void dispose() throws Exception {
		super.dispose();
		_state = ActorFSM.DISPOSED;
	}
	
	private synchronized void bindSpecial(Map<String,Object> binding) {
		binding.put("_inputs", _inputValues);
		binding.put("_states", _stateVariables);
		binding.put("_outputs", _outputSignature.keySet());
		binding.put("_status" , _actorStatus);
		binding.put("_type", _variableTypes);	
		binding.put("_this" , this);
		binding.put("STEP", _actorStatus.getStepCount());
	}
	

	private synchronized void bindConstants(Map<String,Object> model) {
		for (String name: _constants.keySet()) {
			Object value = _constants.get(name);
			model.put(name,value);
		}
	}

	
}
