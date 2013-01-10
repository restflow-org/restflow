package org.restflow.actors;

import groovy.lang.MissingPropertyException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restflow.actors.Actor.ActorFSM;
import org.restflow.data.Outflow;
import org.restflow.util.Contract;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.yaml.snakeyaml.Yaml;


public class TclActor extends AbstractActor {

	// delimiter for separating script output from YAML block
	public final String OUTPUT_DELIMITER = "__END_TCL_SCRIPT_OUTPUT__";
	public final String ERROR_DELIMITER = "__END_TCL_SCRIPT_ERROR__";
	public final String END_STEP_DELIMITER = "__END_STEP__";
	
	private String initialize;
	String step;
	private String wrapup;
	private String dispose;
	private String _configure;
	
	String _stepWrapper;
	
	private Boolean _persistentShell = false;
	private Boolean _enterEventLoop = false;
	Process _process;
	PrintStream _ps;
	InputStream _is;
	InputStreamReader _isr;
	BufferedReader _br;

	
	
	protected final Log logger = LogFactory.getLog(getClass());

	public TclActor() {
		super();
	}

	public void afterPropertiesSet() throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		super.afterPropertiesSet();
		_state = ActorFSM.PROPERTIES_SET;
	}
	
	public void elaborate() throws Exception {
		super.elaborate();
		_state = ActorFSM.ELABORATED;
	}
	
	public void configure() throws Exception {
		super.configure();
		if ( getConfigure() != null ) {
			runScript(null,getConfigure());			
		}
		//_actorStatus.setCallType("preWorkflow");
		_state = ActorFSM.CONFIGURED;
	}
	
	@Override
	public void initialize() throws Exception {
		super.initialize();

		if (getInitialize() != null) {
			try {
			Map<String,Object> outputs = runScript(null,getInitialize());
			_storeStateValues(outputs); //initialization creates state, but not output
			} catch (Exception e) {
				throw e;
			} finally {
				_state = ActorFSM.INITIALIZED;
			}
			
		}
		
		_state = ActorFSM.INITIALIZED;
	}

	@Override
	public void step() throws Exception {

		super.step();
		
		Map<String,Object> outputs;
		try {
			outputs = runScript(getStepWrapper(),getStep());
		} catch (Exception e) {
			throw e;
		} finally {
			_state = ActorFSM.STEPPED;
		}
		
		_storeStateValues(outputs);		
		_storeOutputValues(outputs);
		
	}

	@Override
	public void wrapup() throws Exception {
		super.wrapup();

		if (getWrapup() != null) {
			runScript(null,getWrapup());
		}
		
		_state = ActorFSM.WRAPPED_UP;	
	}

	@Override
	public void dispose() throws Exception {
		super.dispose();

		if (getDispose() != null) {
			runScript(null,getDispose());
		}
		
		_state = ActorFSM.DISPOSED;	
	}

	
	public synchronized Map<String, Object> runScript(String wrapper, String script) throws Exception {
		// ////////////////////////////////////////////////////////////////
		// Code for running a Tcl script, passing the script input
		// variables by prepending the step script with set commands and
		// retrieving script outputs by appending print commands that
		// render output variables in YAML.

		// prepare script fragment to copy input values to shell variables
		

		String globals = "";
		for (String key : _outputSignature.keySet()) {
			//get the outputs into the global namespace
			globals += "set "+ key +" \"null\"\n";
		}

		String inputVars = "";
		for (String key : _constants.keySet()) {
			inputVars += "set " + key + " \"" + _constants.get(key)
					+ "\"\n";
		}
		
		for (String key : _stateVariables.keySet()) {
			inputVars += "set " + key + " \"" + _stateVariables.get(key)
					+ "\"\n";
		}

		for (String key : _inputSignature.keySet()) {
			inputVars += "set " + key + " \"" + _inputValues.get(key) + "\"\n";
		}

		if ( _actorStatus.getStepDirectory() != null) {
		 inputVars += "cd " + _actorStatus.getStepDirectory() + "\n";
		}

		// prepare script fragment to render output variables in YAML
		String outputVars = "puts \"" + OUTPUT_DELIMITER + "\"\n";

		for (String name : _stateVariables.keySet()) {
			outputVars += "puts " + "\"" + name + ": \\\"$" + name + "\\\"\"\n";
		}

		for (String name : new String[] { "enabledInputs", "disabledInputs",
				"enabledOutputs", "disabledOutputs" }) {
			outputVars += "puts " + "\"" + name + ": \\\"$_status(" + name + ")\\\"\"\n";
		}

		for (String name : _outputSignature.keySet()) {
			outputVars += "outputIfExists " + name + "\n";
		}

		String outputIfExists = 
			"proc outputIfExists { outputVar } {\n" +
				"global $outputVar\n" +
				"if [info exists $outputVar] {\n" +
					"puts \"${outputVar}: \\\"[set $outputVar]\\\"\"\n" +
				"}\n" +
			"}\n";

		String doScriptProc =
			"proc __doScript {} { uplevel 1 {\n" +
			"if {[catch {" + script + "} err] } {\n" +
			"puts " + ERROR_DELIMITER + "\n" +
			"puts $errorInfo\n" +
			"}\n" +
			" } " +
		"}\n";
		
		String doWrappedScriptProc = "";
		if ( wrapper != null) {
			doWrappedScriptProc =
				"proc __doWrappedScript {} { uplevel 1 {\n" +
					"if {[catch {" + wrapper + "} err] } {\n" +
						"puts " + ERROR_DELIMITER + "\n" +
						"puts $errorInfo\n" +
						"}\n" +
					" } " +
				"}\n";
		}
		
		String functions = 
			"set _status(stepCount) " + _actorStatus.getStepCount() + "\n" +
			"set _status(stepCountIndex) " + _actorStatus.getStepCountIndex() + "\n" +		    
			"set _status(stepDirectory) " + _actorStatus.getStepDirectory() + "\n" +
			"set _status(disabledOutputs) {}\n" +
			"set _status(enabledInputs) {}\n" +
			"set _status(disabledInputs) {}\n" +
			"set _status(enabledOutputs) {}\n" +
			"proc disableOutput { output } { global _status\n append _status(disabledOutputs) \" $output\"}\n" +
			"proc enableInput { input } { global _status\n append _status(enabledInputs) \" $input\"}\n" +
			"proc disableInput { input } { global _status\n append _status(disabledInputs) \" $input\"}\n" +
			"proc enableOutput { output} { global _status\n append _status(enabledOutputs) \" $output\"}\n" +
			bindDirectories() +
			doScriptProc +
			doWrappedScriptProc +
			outputIfExists;

		String vwait ="";
		if ( _enterEventLoop ) {
			vwait = 
				"global __EXIT__\n"+
				"vwait __EXIT__\n";
		}

		String chooseScript = "";
		if (wrapper != null) {
			chooseScript = "__doWrappedScript\n";
		} else {
			chooseScript = "__doScript\n";			
		}
		
		// bracket step script with data binding script fragments
		String fullScript =
			globals +
			functions + 
			inputVars + "\n" + 
			chooseScript + 
			vwait +
			outputVars + "\n" +
			"puts " + END_STEP_DELIMITER + "\n";

		logger.debug(fullScript);

		if (_process == null || !_persistentShell ) {			
			
			// run tcl and feed the script to it through standard input
			String cmd[] = new String[] { "sh", "-c", "tclsh" };
			ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);

			if (_inputValues.containsKey("env")) {
				Map<String, String> env = pb.environment();
				env.putAll((Map<String, String>) _inputValues.get("env"));
			}

			if (_actorStatus.getStepDirectory() != null) {
				pb.directory(_actorStatus.getStepDirectory());
			}
			
			_process = pb.start();
			_ps = new PrintStream(_process.getOutputStream());
			_isr = new InputStreamReader(_process.getInputStream());
			_br = new BufferedReader(_isr);			
		}
			
		_ps.print(fullScript);
		
		if ( !_persistentShell ) {			
			_ps.close();
		} else {
			_ps.flush();
		}
		
		// capture the standard output from the run of the script
		// output the stdout as it is streamed
		String line;

		String completeOutput = "";
		String errorMessage = "";
		
		boolean scriptDone = false;
		boolean scriptError = false;
		while ((line = _br.readLine()) != null) {
			//System.out.println(getBeanName() + ":" + line);
			if (line.contains(END_STEP_DELIMITER) ) {
				break;
			}

			if (line.contains( OUTPUT_DELIMITER )) {
				scriptDone = true;
				continue;
			}

			if (line.contains(ERROR_DELIMITER)) {
				scriptError = true;
				continue;
			}

			if ( scriptError && !scriptDone ) {
				errorMessage = errorMessage + line + "\n";
				continue;
			}

			if ( scriptDone && !scriptError ) {
				completeOutput = completeOutput + line + "\n";
				continue;
			}
			
			if (scriptDone || scriptError) continue;
			
			System.out.println(line);
		}

		if (scriptError) {
			throw new Exception(errorMessage);
		}

		// parse the yaml block and save output variable values in map
		Map<String, Object> outputs = new HashMap<String, Object>();

		Yaml yaml = new Yaml();
		Map<String, String> outputMap = (Map<String, String>) yaml
				.load(completeOutput);
		
		if (outputMap == null) {
			logger.debug("no output");
			outputMap = new HashMap<String,String>();
		}
		
		for (String key : outputMap.keySet()) {
			String value = outputMap.get(key);
			if (value.equals("null")) {
				outputs.put(key, null);
			} else {
				String elementType = _variableTypes.get(key);
				if (elementType !=null && elementType.equals("File") ) {
					File file = new File(_actorStatus.getStepDirectory(),value);
					if ( ! file.exists()) {
						//TODO investigate this decision
						//maybe they gave us an absolute path...
						file =  new File(value);
					}
					outputs.put(key, file );
				} else {
					outputs.put(key, value);
				}
			}
		}

		String enabledInputs = outputMap.get("enabledInputs");
		if (enabledInputs != null) {
			for (String name : enabledInputs.split(" ")) {
				_actorStatus.enableInput(name);
			}
		}
		String enabledOutputs = outputMap.get("enabledOutputs");
		if (enabledOutputs != null) {
			for (String name : enabledOutputs.split(" ")) {
				_actorStatus.enableOutput(name);
			}
		}
		String disabledInputs = outputMap.get("disabledInputs");
		if (disabledInputs != null) {
			for (String name : disabledInputs.split(" ")) {
				_actorStatus.disableInput(name);
			}
		}
		String disabledOutputs = outputMap.get("disabledOutputs");
		if (disabledOutputs != null) {
			for (String name : disabledOutputs.split(" ")) {
				_actorStatus.disableOutput(name);
			}
		}

		return outputs;
	}

	private synchronized void _storeStateValues(Map<String, Object> binding) {

		for (String label : _stateVariables.keySet()) {
			Object value = binding.get(label);
			_stateVariables.put(label, value);
		}
	}

	private synchronized void _storeOutputValues(Map<String, Object> binding)
			throws Exception {

		for (String label : _outputSignature.keySet()) {

			if (_actorStatus.getOutputEnable(label)) {

				Object value;

				try {
					value = binding.get(label);
				} catch (MissingPropertyException e) {
					throw new Exception("Actor " + this
							+ " did not output a value for " + label);
				}

				if (value instanceof File && !((File) value).exists()) {
					value = null;
				}

				_storeOutputValue(label, value);
			}
		}
	}

	private synchronized String bindDirectories() {
		
		String bindDirectories ="";
		String runDirectoryPath = _workflowContext.getRunDirectoryPath();
		if (runDirectoryPath != null) {

			bindDirectories = "set _runDir " + runDirectoryPath + "\n";
		
			for (Outflow outflow: _node.getOutflows().values()) {
				String nestedUriPrefix = _node.getUriPrefix();
				String localPath = outflow.getUriTemplate().getPath();
				String absolutePath = runDirectoryPath + "/" + nestedUriPrefix + "/" + localPath + "/";
				bindDirectories += "set _outflowDirectory(" + outflow.getLabel()+") " + absolutePath +"\n";
			}
		}	
		return bindDirectories;
	}
	
	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public synchronized ApplicationContext getApplicationContext() {
		return _workflowContext;
	}

	public String getInitialize() {
		return initialize;
	}

	public void setInitialize(String initialize) {
		this.initialize = initialize;
	}

	public String getWrapup() {
		return wrapup;
	}

	public void setWrapup(String wrapup) {
		this.wrapup = wrapup;
	}

	public String getDispose() {
		return dispose;
	}

	public void setDispose(String dispose) {
		this.dispose = dispose;
	}

	
	public Boolean getPersistentShell() {
		return _persistentShell;
	}

	public void setPersistentShell(Boolean persistentShell) {
		_persistentShell = persistentShell;
	}

	public Boolean getActivateEventLoop() {
		return _enterEventLoop;
	}

	public void setActivateEventLoop(Boolean enterEventLoop) {
		_enterEventLoop = enterEventLoop;
	}

	public String getStepWrapper() {
		return _stepWrapper;
	}

	public void setStepWrapper(String stepWrapper) {
		_stepWrapper = stepWrapper;
	}

	public String getConfigure() {
		return _configure;
	}

	public void setConfigure(String configure) {
		_configure = configure;
	}

	
	
}