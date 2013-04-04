package org.restflow.actors;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.restflow.data.InputSignatureElement;
import org.restflow.util.PortableIO;
import org.restflow.util.PortableIO.StreamSink;
import org.yaml.snakeyaml.Yaml;


public abstract class AugmentedScriptActor extends ScriptActor {

	static final protected String  _scriptOutputDelimiter = "__END_OF_SCRIPT_OUTPUT__";
	
	public abstract ActorScriptBuilder getNewScriptBuilder();
	public abstract String getScriptRunCommand() ;

	public synchronized void configure() throws Exception {
		
		super.configure();
	
		if (_configureScript != null && !_configureScript.trim().isEmpty()) {

			// augment the configure script
			String augmentedConfigureScript = getAugmentedConfigureScript();
			
			// run the augmented configure script
			String yamlOutput = _runAugmentedScript(augmentedConfigureScript);
			
			// update the actor state based on the augmented script output
			Map<String,Object> binding = _parseAugmentedScriptYamlOutput(yamlOutput);
			_updateStateVariables(binding);
		}

		_state = ActorFSM.CONFIGURED;
	}
	
	
	protected String getAugmentedConfigureScript() {
		
		ActorScriptBuilder augmentedScriptBuilder = getNewScriptBuilder();
		
		_appendScriptHeader(augmentedScriptBuilder, "configure");
		_appendOriginalScript(augmentedScriptBuilder, _configureScript);
		
		return augmentedScriptBuilder.toString();
	}
	
	
	@Override
	public synchronized void initialize() throws Exception {
	
		super.initialize();
		
		if (_initializeScript != null && !_initializeScript.trim().isEmpty()) {
			
			// augment the initialize script
			String augmentedInitializeScript = _getAugmentedInitializeScript();
			
			// run the augmented initialize script
			String yamlOutput = _runAugmentedScript(augmentedInitializeScript);
			
			// update the actor state based on the augmented script output
			Map<String,Object> scriptOutputs = _parseAugmentedScriptYamlOutput(yamlOutput);
			_updateInputOutputControlVariables(scriptOutputs);
			_updateStateVariables(scriptOutputs);
		}
		
		_state = ActorFSM.INITIALIZED;		
	}
	
	protected String _getAugmentedInitializeScript() throws Exception {
		
		ActorScriptBuilder augmentedScriptBuilder = getNewScriptBuilder();
		
		_appendScriptHeader(augmentedScriptBuilder, "initialize");
		_appendInputControlFunctions(augmentedScriptBuilder);
		_appendOutputControlFunctions(augmentedScriptBuilder);
		_appendActorSettingInitializers(augmentedScriptBuilder);
		_appendActorStateVariableInitializers(augmentedScriptBuilder, true);
		_appendActorInputVariableInitializers(augmentedScriptBuilder);
		_appendOriginalScript(augmentedScriptBuilder, _initializeScript);
		_appendOriginalScriptOutputDelimiter(augmentedScriptBuilder);
		_appendStateVariableYamlRenderers(augmentedScriptBuilder);
		_appendInputControlVariableYamlRenderers(augmentedScriptBuilder);
		_appendOutputControlVariableYamlRenderers(augmentedScriptBuilder);
		
		return augmentedScriptBuilder.toString();
	}
	
	public synchronized void step() throws Exception {
		
		super.step();

		if (_stepScript != null && !_stepScript.trim().isEmpty()) {

			// augment the step script
			String augmentedStepScript = getAugmentedStepScript();

			// run the augmented step script
			String yamlOutput = _runAugmentedScript(augmentedStepScript);
			
			// update the actor state based on the augmented script output
			Map<String,Object> scriptOutputs = _parseAugmentedScriptYamlOutput(yamlOutput);
			_updateInputOutputControlVariables(scriptOutputs);
			_updateOutputVariables(scriptOutputs);			
			_updateStateVariables(scriptOutputs);
		}
	}
	
	public String getAugmentedStepScript() throws Exception {
		
		ActorScriptBuilder augmentedScriptBuilder = getNewScriptBuilder();
		
		_appendScriptHeader(augmentedScriptBuilder, "step");
		_appendInputControlFunctions(augmentedScriptBuilder);
		_appendOutputControlFunctions(augmentedScriptBuilder);
		_appendActorSettingInitializers(augmentedScriptBuilder);
		_appendActorStateVariableInitializers(augmentedScriptBuilder, true);
		_appendActorInputVariableInitializers(augmentedScriptBuilder);
		_appendStepDirectoryEntryCommand(augmentedScriptBuilder);
		_appendOriginalScript(augmentedScriptBuilder, _stepScript);
		_appendOriginalScriptOutputDelimiter(augmentedScriptBuilder);
		_appendOutputVariablesYamlRenderers(augmentedScriptBuilder);
		_appendStateVariableYamlRenderers(augmentedScriptBuilder);
		_appendInputControlVariableYamlRenderers(augmentedScriptBuilder);
		_appendOutputControlVariableYamlRenderers(augmentedScriptBuilder);
		
		return augmentedScriptBuilder.toString();
	}
	
	@Override
	public synchronized void wrapup() throws Exception {
		
		super.wrapup();
		
		if (_wrapupScript != null && !_wrapupScript.trim().isEmpty()) {
			
			// augment the wrapup script
			String augmentedWrapupScript = _getAugmentedWrapupScript();
			
			// run the augmented wrapup script
			_runAugmentedScript(augmentedWrapupScript);
		}
		
		_state = ActorFSM.WRAPPED_UP;
	}

	protected String _getAugmentedWrapupScript() throws Exception {
		
		ActorScriptBuilder augmentedScriptBuilder = getNewScriptBuilder();
		
		_appendScriptHeader(augmentedScriptBuilder, "wrapup");
		_appendActorSettingInitializers(augmentedScriptBuilder);
		_appendActorStateVariableInitializers(augmentedScriptBuilder, false);
		_appendOriginalScript(augmentedScriptBuilder, _wrapupScript);
		
		return augmentedScriptBuilder.toString();
	}
	
	@Override
	public synchronized void dispose() throws Exception {
		
		super.dispose();
		
		if (_disposeScript != null && !_disposeScript.trim().isEmpty()) {
			
			// augment the dispose script
			String augmentedDisposeScript = _getAugmentedDisposeScript();
			
			// run the augmented dispose script
			_runAugmentedScript(augmentedDisposeScript);
		}
		
		_state = ActorFSM.DISPOSED;
	}
	
	protected String _getAugmentedDisposeScript() throws Exception {
		
		ActorScriptBuilder augmentedScriptBuilder = getNewScriptBuilder();
		
		_appendScriptHeader(augmentedScriptBuilder, "dispose");
		_appendActorSettingInitializers(augmentedScriptBuilder);
		_appendActorStateVariableInitializers(augmentedScriptBuilder, false);
		_appendOriginalScript(augmentedScriptBuilder, _disposeScript);

		return augmentedScriptBuilder.toString();
	}
	
	protected void _appendScriptHeader(ActorScriptBuilder script, String scriptType) {
		script.appendComment("AUGMENTED " + scriptType.toUpperCase() + " SCRIPT FOR ACTOR " + this.getFullyQualifiedName())
		  	  .appendBlankLine();
	}
	
	protected void _appendInputControlFunctions(ActorScriptBuilder script) {
		if (!_inputSignature.isEmpty()) {
			script.appendInputControlFunctions()
			  	  .appendBlankLine();
		}
	}
	
	protected void _appendOutputControlFunctions(ActorScriptBuilder script) {
		if (!_outputSignature.isEmpty()) {
			script.appendOutputControlFunctions()
			  	  .appendBlankLine();
		}
	}

	protected void _appendOutputVariableInitializers(ActorScriptBuilder script) throws Exception {
		if (!_outputSignature.isEmpty()) {
			script.appendComment("initialize actor outputs to null");
			for (String name : _outputSignature.keySet()) {
				script.appendLiteralAssignment(name, null, _variableTypes.get(name), false, _outputSignature.get(name).isNullable());
			}
			script.appendBlankLine();
		}
	}

	protected void _appendActorStateVariableInitializers(ActorScriptBuilder script, boolean hideInputs) throws Exception {
		if (!_stateVariables.isEmpty()) {
			script.appendComment("initialize actor state variables");
			Set<String> stateNames = new HashSet<String>(_stateVariables.keySet());
			if (hideInputs) {
				stateNames.removeAll(_inputSignature.keySet());
			}
			for (String key : stateNames) {
				InputSignatureElement input = (InputSignatureElement)(_inputSignature.get(key));
				boolean nullable = input != null && input.isNullable();
				script.appendLiteralAssignment(key, _stateVariables.get(key), _variableTypes.get(key), true, nullable);
			}
			script.appendBlankLine();
		}
	}
	
	protected void _appendActorInputVariableInitializers(ActorScriptBuilder script) throws Exception {
		if (!_inputSignature.isEmpty()) {
			script.appendComment("initialize actor input variables");			
			Set<String> inputNames = _inputSignature.keySet();
			for (String key : inputNames) {
				script.appendLiteralAssignment(key, _inputValues.get(key), _variableTypes.get(key), false, _inputSignature.get(key).isNullable());
			}
			script.appendBlankLine();
		}
	}
	
	protected void _appendActorSettingInitializers(ActorScriptBuilder script) throws Exception {
		if (!_constants.isEmpty()) {
			script.appendComment("initialize actor setting");
			Set<String> settingNames = _constants.keySet();
			for (String key : settingNames) {
				script.appendLiteralAssignment(key, _constants.get(key), _variableTypes.get(key), false, false);
			}
			script.appendBlankLine();
		}
	}

	protected void _appendStepDirectoryEntryCommand(ActorScriptBuilder script) {
		if (_actorStatus.getStepDirectory() != null) {
			script.appendComment("change working directory to actor step directory")
				  .appendChangeDirectory(_actorStatus.getStepDirectory().toString())
				  .appendBlankLine();
		}
	}
	
	protected void _appendOriginalScript(ActorScriptBuilder script,String originalScript) {
		script.appendComment("BEGINNING OF ORIGINAL SCRIPT")
			  .appendBlankLine()
			  .appendCode(originalScript)
			  .appendBlankLine()
			  .appendComment("END OF ORIGINAL SCRIPT")
			  .appendBlankLine();
	}
	
	protected void _appendOriginalScriptOutputDelimiter(ActorScriptBuilder script) {
		script.appendComment("signal end of output from original script")
			  .appendPrintStringStatement(_scriptOutputDelimiter)
			  .appendBlankLine();
	}
	
	protected void _appendOutputVariablesYamlRenderers(ActorScriptBuilder script) {
		if (! _outputSignature.isEmpty()) {
			script.appendComment("render output variables as yaml");
			for (String name : _outputSignature.keySet()) {
			    script.appendVariableYamlPrintStatement(name, _variableTypes.get(name));
			}
			script.appendBlankLine();
		}
	}

	protected void _appendStateVariableYamlRenderers(ActorScriptBuilder script) {
		if (!_stateVariables.isEmpty()) {
			script.appendComment("render state variables as yaml");
			for (String name : _stateVariables.keySet()) {
				script.appendVariableYamlPrintStatement(name, _variableTypes.get(name));
			}
			script.appendBlankLine();
		}
	}

	protected void _appendInputControlVariableYamlRenderers(ActorScriptBuilder script) {
		if (!_inputSignature.isEmpty()) {
			script.appendComment("render actor input control variables as yaml");
			for (String name : new String[]{ "enabledInputs", "disabledInputs"}) {
			    script.appendNonNullStringYamlPrintStatement(name);
			}
			script.appendBlankLine();
		}
	}
	
	protected void _appendOutputControlVariableYamlRenderers(ActorScriptBuilder script) {
		if (! _outputSignature.isEmpty()) {
			script.appendComment("render actor output control variables as yaml");
			for (String name : new String[]{ "enabledOutputs", "disabledOutputs" }) {
			    script.appendNonNullStringYamlPrintStatement(name);
			}
			script.appendBlankLine();
		}
	}
	
	protected synchronized String _runAugmentedScript(String augmentedScript) throws Exception {

		String runcommand = getScriptRunCommand();
		
		StreamSink[] outputs = PortableIO.runProcess(
									runcommand, 
					  				augmentedScript,
					  				new String[]{}, 
					  				_actorStatus.getStepDirectory()
		  						 );
		
		// capture the standard output from the run of the script
		String completeStdout = outputs[0].toString();
		  
		String completeStderr = outputs[1].toString();
		if (!completeStderr.isEmpty()) {
			System.err.println(	">>>>>>>>>>>>>>>>>>>> Error running augmented actor script >>>>>>>>>>>>>>>>>>>>>>"	);
			System.err.print  (	augmentedScript																		);
			System.err.println(	"-------------------------------- Error message ---------------------------------"	);
			System.err.print  (	completeStderr																		);
			System.err.println(	"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"	);
		}
		
		// find the beginning of the script output delimiter
		int delimiterStart = completeStdout.lastIndexOf(_scriptOutputDelimiter);
		
		String scriptStdout;
		String yamlOutput;
		
		if (delimiterStart == -1) {
			scriptStdout = completeStdout;
			yamlOutput = "";
		} else {
			scriptStdout = completeStdout.substring(0, delimiterStart);
			int yamlStart = delimiterStart + _scriptOutputDelimiter.length();
			yamlOutput = completeStdout.substring(yamlStart);
		}
		
		System.out.print(scriptStdout);
		
		return yamlOutput;
	}
	
	private synchronized Map<String,Object> _parseAugmentedScriptYamlOutput(String yamlOutput) {
		
		Map<String,Object> binding = new HashMap<String,Object>();
		  
		// parse the yaml block and save output variable values in map
		Yaml yaml = new Yaml();
		Map<String,Object> outputMap = (Map<String,Object>) yaml.load(yamlOutput);
		if (outputMap != null) {
			for (Map.Entry<String,Object> entry : outputMap.entrySet()) { 
				String key = entry.getKey();
				Object value = entry.getValue();
				Object variableType = _variableTypes.get(key);
				if (value != null && value.equals("null")) {
					binding.put(key, null);
				} else if (variableType != null && variableType.equals("File")) {
			    	binding.put(key, new File(_actorStatus.getStepDirectory(), value.toString()));
			    } else {
			    	binding.put(key, value);
			    }
			}
		}
		
		return binding;
	}
	
	private synchronized void _updateInputOutputControlVariables(Map<String,Object> binding) {
		
		String enabledInputs = (String) binding.get("enabledInputs");
		if (enabledInputs != null) {
			for (String name : enabledInputs.split(" ")) {
				_actorStatus.enableInput(name);
			}
		}
		  
		String enabledOutputs = (String) binding.get("enabledOutputs");
		if (enabledOutputs != null) {
			for (String name : enabledOutputs.split(" ")) {
				_actorStatus.enableOutput(name);
			}
		}
		  
		String disabledInputs = (String) binding.get("disabledInputs");
		if (disabledInputs != null) {
			for (String name : disabledInputs.split(" ")) {
				_actorStatus.disableInput(name);
			}
		}
		  
		String disabledOutputs = (String) binding.get("disabledOutputs");
		if (disabledOutputs != null) {
			for (String name : disabledOutputs.split(" ")) {
				_actorStatus.disableOutput(name);
			}
		}
	}
}
