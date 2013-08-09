package org.restflow.actors;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restflow.data.LogProtocol;
import org.restflow.data.LogProtocol.Logger;
import org.restflow.util.Contract;

import net.jcip.annotations.GuardedBy;

public abstract class ScriptActor extends AbstractActor {
	
    @GuardedBy("this") protected Log 	logger;
    @GuardedBy("this") protected Logger _logger;
    @GuardedBy("this") LogProtocol 		_logProtocol;
    @GuardedBy("this") protected String _configureScript;
    @GuardedBy("this") protected String _initializeScript;
    @GuardedBy("this") protected String _stepScript;
    @GuardedBy("this") protected String _wrapupScript;
    @GuardedBy("this") protected String _disposeScript;
    @GuardedBy("this") protected String _scriptExtension;
    
    public  enum OutputStreamMode { DISCARD, DELAYED, IMMEDIATE }; 
    
    protected OutputStreamMode _stdoutMode;
    protected OutputStreamMode _stderrMode;
    
	public ScriptActor() {
		super();		
		synchronized(this) {
			logger = LogFactory.getLog(getClass());
			_stdoutMode = OutputStreamMode.DELAYED;
		    _stderrMode = OutputStreamMode.DELAYED;
		    _scriptExtension = "txt";
		}
	}
	
	public synchronized Object clone() throws CloneNotSupportedException {
		ScriptActor theClone = (ScriptActor) super.clone();
		theClone.logger = LogFactory.getLog(getClass());
		return theClone;
	}

	public synchronized void setStep(String script) {
		_stepScript = script;
	}

	public synchronized void setWrapup(String script) {
		_wrapupScript = script;
	}

	public synchronized void setDispose(String script) {
		_disposeScript = script;
	}

	public synchronized void setInitialize(String script) {
		_initializeScript = script;
	}

	public synchronized void setConfigure(String script) {
		_configureScript = script;
	}

	public synchronized void setStdoutMode(String mode) throws Exception {
		_stdoutMode = _parseOutputMode(mode);
	}

	public synchronized void setStderrMode(String mode) throws Exception {
		_stderrMode = _parseOutputMode(mode);
	}

	private OutputStreamMode _parseOutputMode(String mode) throws Exception {
		if (mode.equals("discard")) {
			return OutputStreamMode.DISCARD;			
		} else  if (mode.equals("delayed")) {
			return OutputStreamMode.DELAYED;			
		} else if (mode.equals("immediate")) {
			return OutputStreamMode.IMMEDIATE;	
		} else {
			throw new Exception("Parse mode string must be one of discard, delayed, or immedate.");
		}
	}
	
	public synchronized void afterPropertiesSet() throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		super.afterPropertiesSet();
		_state = ActorFSM.PROPERTIES_SET;
	}

	public synchronized void elaborate() throws Exception {
		super.elaborate();
		_state = ActorFSM.ELABORATED;
	}
	
	public synchronized void setLogProtocol(LogProtocol protocol) {
		_logProtocol = protocol;
	}
	
	public synchronized void configure() throws Exception {
		super.configure();
		if (_logProtocol != null) {
			_logger = new Logger(_logProtocol);
		}	
		_actorStatus.setCallType("configure");
	}
	
	public synchronized void initialize() throws Exception {
		super.initialize();
		_actorStatus.setCallType("initialize");		
	}

	public synchronized void step() throws Exception {
		super.step();
		_actorStatus.setCallType("step");
	}
	
	public synchronized void wrapup() throws Exception {
		super.wrapup();
		_actorStatus.setCallType("wrapup");
	}
	
	public synchronized void dispose() throws Exception {		
		super.dispose();
		_actorStatus.setCallType("dispose");
	}

	protected synchronized void _updateStateVariables(Map<String,Object> variables) {
		for (String label : _stateVariables.keySet()) {
			Object value = variables.get(label);
			_stateVariables.put(label, value);
		}
	}
		
	protected synchronized void _updateOutputVariables(Map<String,Object> variables) throws Exception {
		
		for (String label : _outputSignature.keySet()) {

			if (_actorStatus.getOutputEnable(label)) {
				
				if (!variables.containsKey(label)) {
					throw new Exception("Actor " + this + " did not output a value for " + label);
				}	
			
				Object value = variables.get(label);				
				if (value instanceof File && ! ((File)value).exists()) {
					value = null;	
				}
				
				_storeOutputValue(label, value);
			}
		}
	}
}
