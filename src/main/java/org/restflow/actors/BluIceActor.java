package org.restflow.actors;

import groovy.lang.MissingPropertyException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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


public class BluIceActor extends AbstractActor {

	// delimiter for separating script output from YAML block
	public final String START_DELIMITER = "__START_TCL_SCRIPT_OUTPUT__";	
	public final String OUTPUT_DELIMITER = "__END_TCL_SCRIPT_OUTPUT__";
	public final String ERROR_DELIMITER = "__END_TCL_SCRIPT_ERROR__";
	public final String END_STEP_DELIMITER = "__END_STEP__";
	
	private String initialize;
	String step;
	private String wrapup;
	private String dispose;
	private String _configure;
	private boolean _needsActive = true;
	
	String _stepWrapper;
	
	private Boolean _persistentShell = false;
	Process _process;
	PrintStream _ps;
	InputStream _is;
	InputStreamReader _isr;
	BufferedReader _br;

	protected final Log logger = LogFactory.getLog(getClass());

	public BluIceActor() {
		super();
	}

	public void afterPropertiesSet() throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		super.afterPropertiesSet();
		_state = ActorFSM.PROPERTIES_SET;
	}	
	
	final public String STATIC_FUNCTIONS =
		"namespace eval ::nScripts {"+"" +
		"proc disableOutput { output } { variable _status\n append _status(disabledOutputs) \" $output\"}\n" +
		"proc enableInput { input } { variable _status\n append _status(enabledInputs) \" $input\"}\n" +
		"proc disableInput { input } { variable _status\n append _status(disabledInputs) \" $input\"}\n" +
		"proc enableOutput { output} { variable _status\n append _status(enabledOutputs) \" $output\"}\n"+
		"}\n";
	
		
	public void elaborate() throws Exception {
		super.elaborate();
		_state = ActorFSM.ELABORATED;
	}
	
	public void configure() throws Exception {
		super.configure();
		
		startTclShell();
		submitRawCommand("package require BluIceShell\n");
		waitForResponse();
		submitRawCommand("enterEventHandleMode\n"); //don't wait for response...	
		
		//from now on, use the submitCommand method to communicate with BluIceShell 
		submitCommandAndWaitForResponse(STATIC_FUNCTIONS);
		
		//_ps.flush();
		
		//if ( getConfigure() != null ) {
		//	runScript(null,getConfigure());			
		//}
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

		submitCommand("exit\n");
		
		//if (getDispose() != null) {
		//	runScript(null,getDispose());
		//}
		
		_state = ActorFSM.DISPOSED;	
	}

	private synchronized void startTclShell() throws Exception {
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

	
	private String buildOutputVariableString () {
		String globals = "";
		for (String key : _outputSignature.keySet()) {
			//get the outputs into the global namespace
			globals += "set "+ key +" \"null\"\n";
		}
		
		globals += "variable ::nScripts::_status\n"+
		"set _status(disabledOutputs) {}\n" +
		"set _status(enabledInputs) {}\n" +
		"set _status(disabledInputs) {}\n" +
		"set _status(enabledOutputs) {}\n";
		
		globals += "set _status(stepCount) " + _actorStatus.getStepCount() + "\n";
		globals += "set _status(stepCountIndex) " + _actorStatus.getStepCountIndex() + "\n";		    
		globals += "set _status(stepDirectory) " + _actorStatus.getStepDirectory() + "\n";
		
		return globals;
	}

	private String buildConstantsString() {
		String constantVars = "";
		for (String key : _constants.keySet()) {
			constantVars += "set " + key + " \"" + _constants.get(key)
				+ "\"\n";
		}
		return constantVars;
	}
	
	private String buildStateVariablesString() {
		String stateVars = "";
		for (String key : _stateVariables.keySet()) {
			stateVars += "set " + key + " \"" + _stateVariables.get(key)
					+ "\"\n";
		}
		return stateVars;
	}
	
	private String buildInputVariablesString() {
		String inputVars = "";
		for (String key : _inputSignature.keySet()) {
			inputVars += "set " + key + " \"" + _inputValues.get(key) + "\"\n";
		}
		return inputVars;
	}
	
	private String buildOutputResultsScriptFragment() {
		String outputVars = "puts stdout \"" + OUTPUT_DELIMITER + "\"\n";

		for (String name : _stateVariables.keySet()) {
			outputVars += "puts stdout " + "\"" + name + ": \\\"$" + name + "\\\"\"\n";
		}		
		
		for (String name : _outputSignature.keySet()) {
			outputVars += "outputIfExists " + name + "\n";
		}

		for (String name : new String[] { "enabledInputs", "disabledInputs",
				"enabledOutputs", "disabledOutputs" }) {
			outputVars += "puts stdout " + "\"" + name + ": \\\"$_status(" + name + ")\\\"\"\n";
		}
		
		return outputVars;	
	}	

	private synchronized void submitRawCommand( String command) throws Exception {
		_ps.print(buildCatchEval(command));	
		_ps.println("\n");
		_ps.flush();
	}	
	
	private synchronized void submitCommand( String command) throws Exception {
		_ps.print(command);	
		_ps.write((char)24);
		_ps.println("\n");
		_ps.flush();
	}

	private synchronized void submitCommandAndWaitForResponse( String command) throws Exception {
		submitCommand(buildCatchEval(command));
		waitForResponse();
	}

	private void waitForResponse() throws IOException, Exception {
		String line;

		String completeOutput = "";
		String errorMessage = "";
		
		boolean scriptStarted = false;
		boolean scriptDone = false;
		boolean scriptError = false;
		while ((line = _br.readLine()) != null) {
			//System.out.println(line);
			if ( line.contains(START_DELIMITER) ) {
				scriptStarted=true;
				continue;
			}
			if (!scriptStarted) {
				//throw away anything in the stdout buffer that came before the start
				//of this step
				continue;
			}
			
			//System.out.println(getBeanName() + ":" + line);
			if (line.contains(END_STEP_DELIMITER) ) {
				break;
			}

			if (line.contains( OUTPUT_DELIMITER )) {
				scriptDone = true;
				break;
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
			
		}

		if (scriptError) {
			throw new Exception(errorMessage);
		}

		if (!scriptDone) {
			throw new Exception("Tcl script failed to run: " + errorMessage );
		}
	}

	
	private String buildCatchEval (String script) {
		String catchScript = 
			"puts stdout " + START_DELIMITER + "\n" +			
			"if {[catch {"+
            "eval {\n"
				+ script + "\n" +
			"}\n"+
            "puts stdout __END_TCL_SCRIPT_OUTPUT__\n"+
        "} err] } {"+
         "   puts stdout "+ERROR_DELIMITER+"\n"+
          "  global errorInfo\n"+
         "   puts stdout $errorInfo\n"+ 
         "puts stdout " + END_STEP_DELIMITER+" \n"+
        "}\n";
        
        return catchScript;
	}
	
	public String buildScript( String script) throws Exception {
		
		String fullScript = 
		"namespace eval ::nScripts {" +	
			buildOutputVariableString() + 
			bindDirectories() +
			buildConstantsString() + 
			buildStateVariablesString() + 
			buildInputVariablesString()+
			"connectToBeamlineFirstTimeSafe $beamline $nickName\n" +
			"assertDcssConnectionIsGood\n";
		
		if (_needsActive) {
			fullScript +=
			"assertClientIsActive\n" +	
			"set activeKey [::dcss getActiveKey]\n";
		};
					
		fullScript +=
			"set _sessionId [get_session_id]\n" +
			script + "\n" +
			buildOutputResultsScriptFragment() +			
		"}\n"+
		"puts stdout " + END_STEP_DELIMITER + "\n";

		//logger.error(fullScript);
		return buildCatchEval(fullScript);
	}
	
	
	
	public void submitScript( String script) throws Exception {
		submitCommand(buildScript(script));
	}
	
	public synchronized Map<String, Object> runScript(String wrapper, String script) throws Exception {
		// ////////////////////////////////////////////////////////////////
		// Code for running a Tcl script, passing the script input
		// variables by prepending the step script with set commands and
		// retrieving script outputs by appending print commands that
		// render output variables in YAML.

		// prepare script fragment to copy input values to shell variables
		// capture the standard output from the run of the script
		// output the stdout as it is streamed

		submitScript(script);
		
		String line;

		String completeOutput = "";
		String errorMessage = "";
		
		boolean scriptStarted = false;
		boolean scriptDone = false;
		boolean scriptError = false;
		while ((line = _br.readLine()) != null) {
			//System.out.println(line);
			if ( line.contains(START_DELIMITER) ) {
				scriptStarted=true;
				continue;
			}
			if (!scriptStarted) {
				//throw away anything in the stdout buffer that came before the start
				//of this step
				continue;
			}
			
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

	public boolean getNeedsActive() {
		return _needsActive;
	}

	public void setNeedsActive(boolean needsActive) {
		this._needsActive = needsActive;
	}

	
}