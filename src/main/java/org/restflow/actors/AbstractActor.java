package org.restflow.actors;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.restflow.WorkflowContext;
import org.restflow.data.InputSignatureElement;
import org.restflow.data.LogProtocol;
import org.restflow.data.OutputSignatureElement;
import org.restflow.exceptions.IllegalWorkflowSpecException;
import org.restflow.exceptions.NullInputException;
import org.restflow.exceptions.NullOutputException;
import org.restflow.exceptions.RestFlowException;
import org.restflow.metadata.ActorState;
import org.restflow.metadata.TraceRecorder;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.util.Contract;
import org.restflow.util.ImmutableList;
import org.restflow.util.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;



/**
 * This class is the default base class for all implementations of the Actor
 * interface. 
 * 
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.  However,
 * care must be taken when subclassing this class to maintain thread safety.  In particular, the various
 * protected Maps and other mutable field values must not be published.
 */
@ThreadSafe()
public abstract class AbstractActor implements Actor, BeanNameAware, ApplicationContextAware, 
												Cloneable, InitializingBean {
	
	///////////////////////////////////////////////////////////////////////////
	////                    private instance fields                        ////
	
	@GuardedBy("this") private Boolean 	_cloneable;
	@GuardedBy("this") private boolean 	_stateful;
	@GuardedBy("this") private String 	_stepDirectoryFileSystem;
	@GuardedBy("this") private int 		_stepOfScratchDirectory;
	@GuardedBy("this") private boolean 	_usesStepDirectory;
		
	///////////////////////////////////////////////////////////////////////////////////////////
	////                protected singleton instance fields                                ////

	@GuardedBy("this") protected ActorStatus 				_actorStatus;
	@GuardedBy("this") protected String 					_beanName;	
	@GuardedBy("this") protected String 					_name;
	@GuardedBy("this") protected ActorWorkflowNode 			_node;
	@GuardedBy("this") protected ActorFSM 					_state;
	@GuardedBy("this") protected File 						_stepDirectory;
	@GuardedBy("this") protected WorkflowContext 			_workflowContext;
	protected TraceRecorder _recorder;

	///////////////////////////////////////////////////////////////////////////////////////////
	////                protected collection instance fields                               ////

	@GuardedBy("this") protected Map<String,Object> 				_constants;
	@GuardedBy("this") protected Map<String,Object> 				_defaultInputValues;
	@GuardedBy("this") protected Map<String,InputSignatureElement> 	_inputSignature;
	@GuardedBy("this") protected Map<String,Object> 				_inputValues;
	@GuardedBy("this") protected Map<String,OutputSignatureElement> _outputSignature;
	@GuardedBy("this") protected Map<String,Object> 				_outputValues;
	@GuardedBy("this") protected Map<String,String> 				_variableTypes;
	@GuardedBy("this") protected Map<String,Object> 				_stateVariables;
	
	private String _scratchDirectoryPrefix;
	protected int _runCount; 	// step count since latest call of configure() or reset(),
								//   unlike 'step' which is step count last call to configure(), reset(), or initialize()


	///////////////////////////////////////////////////////////////////////////
	////                  private final class fields                       ////

	protected static final ImmutableMap<String,Object> EMPTY_STRING_OBJECT_MAP = 
			new ImmutableMap<String,Object>();
	
	protected static final ImmutableList<String> EMPTY_STRING_LIST = new ImmutableList<String>();
	
	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	
	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public AbstractActor() {

		synchronized(this) {

			// initialize singleton instance fields
			_actorStatus 			 = new ActorStatus();
			_name 				 = "";
			_cloneable 				 = null;
			_stateful 				 = false;
			_stepDirectory 			 = null;
			_stepDirectoryFileSystem = "";
			_stepOfScratchDirectory  = 0;
			_usesStepDirectory 		 = false;
			
			// initialize collection instance fields
			_stateVariables 		= EMPTY_STRING_OBJECT_MAP;
			_constants	 			= EMPTY_STRING_OBJECT_MAP;
			_inputValues 			= new HashMap<String, Object>();
			_outputValues 			= new HashMap<String, Object>();
			_defaultInputValues		= new HashMap<String, Object>();
			_inputSignature 		= new LinkedHashMap<String,InputSignatureElement>();
			_outputSignature 		= new LinkedHashMap<String,OutputSignatureElement>();
			_variableTypes 			= new Hashtable<String,String>();
			
			// initialize contract state machine
			_state = ActorFSM.CONSTRUCTED;
		}
	}
	
	/**
	 * Clones this AbstractActor. It uses the clone method of the superclass 
	 * (Object) to shallow-copy all primitive fields.  It then performs a deep copy of
	 * the collection fields and specifically clones the actor status field.
	 * 
	 * @throws 	CloneNotSupportedException if the superclass clone() method or 
	 * 			ActorStatus.clone() throws it.
	 */
	public synchronized Object clone() throws CloneNotSupportedException {

		Contract.requires(_state == ActorFSM.INITIALIZED);

		// call the superclass clone method
		AbstractActor theClone = (AbstractActor) super.clone();
		
		// perform deep copies of collection fields
		theClone._constants 			= new HashMap<String,Object>(_constants);
		theClone._defaultInputValues 	= new HashMap<String,Object>(_defaultInputValues);
		theClone._inputSignature 		= new HashMap<String,InputSignatureElement>(_inputSignature);
		theClone._inputValues 			= new HashMap<String,Object>(_inputValues);
		theClone._outputSignature 		= new HashMap<String,OutputSignatureElement>(_outputSignature);
		theClone._outputValues 			= new HashMap<String,Object>(_outputValues);
		theClone._variableTypes 		= new HashMap<String,String>(_variableTypes);
		theClone._stateVariables 		= new HashMap<String,Object>(_stateVariables);

		// specifically clone the actor status object
		theClone._actorStatus = (ActorStatus) _actorStatus.clone();
		
		return theClone;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	///     actor configuration setters -- PROPERTIES_UNSET state only     ////
	
	public synchronized void setApplicationContext(ApplicationContext context) throws BeansException {
		_workflowContext = (WorkflowContext)context;
	}

	public void setBeanName(String beanName) {		

		// save the namespace qualified name of this actor
		_beanName = beanName;

		// extract and save the unqualified name of this actor
		int finalDotPosition = beanName.lastIndexOf('.');
		if (finalDotPosition == -1) {
			setName(_beanName);
		} else {
			setName(_beanName.substring(finalDotPosition + 1));
		}		
	}
	
	public String getBeanName() {
		return _beanName;
	}

	public synchronized void setName(String name) {
		_name = name;		
	}

	public synchronized void setCloneable(boolean newValue) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_cloneable = newValue;
	}

	/**
	 * Provide method for controlling what the step/scratch directory looks like.  Some systems
	 * may require restrictions on the directory structure naming.
	 * 
	 * Leave blank for default behavior.
	 * Set to 'is9660' to replace the . (often found in node names) with _
	 * 
	 * @param stepDirectoryFileSystem
	 */
	public synchronized void setStepDirectoryFileSystem(String stepDirectoryFileSystem) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_stepDirectoryFileSystem = stepDirectoryFileSystem;
	}
	
	///////////////////////////////////////////////////////////////////////////
	///   actor configuration setters -- PROPERTES_UNSET or UNCONFIGURED   ////

	public synchronized void setNode(ActorWorkflowNode node) {
//		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		_node = node;
	}
		
	public synchronized final void setStateful(boolean newValue) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		_stateful = newValue;
	}

	@Override
	public synchronized void setState(Map<String, Object> state) throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		_stateVariables = state;
		if (_stateVariables.size() > 0) {
			setStateful(true);
		}
	}
	
	public synchronized void setUsesStepDirectory(boolean usesScratchDirectory) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		_usesStepDirectory = usesScratchDirectory;
	}
	
	public synchronized void setSettings(Map<String, Object> constants) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		_constants = constants;
	}

	public synchronized void setTypes(Map<String, String> types) throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		_variableTypes.putAll(types);
	}

	
	public synchronized void setInputs(Map<String, Object> inputs) throws Exception {

		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);

		for (String label : inputs.keySet()) {
			Object value = inputs.get(label);
			_addInputToSignature(label, value);
		}
	}
	
	public synchronized void setOutputs(Map<String, Object> outputs) throws Exception {

		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		
		for (String label : outputs.keySet()) {
			Object value = outputs.get(label);
			_addOutputToSignature(label, value);
		}
	}

	
	///////////////////////////////////////////////////////////////////////////
	///    actor configuration mutators -- UNCONFIGURED state only         ////

	public void addImplicitInput(String inputName) throws RestFlowException {
		
		Contract.requires(_state == ActorFSM.PROPERTIES_SET);

		if (! _inputSignature.containsKey(inputName)) {
			_addInputToSignature(inputName, null);				
		} else {
			throw new IllegalWorkflowSpecException("Cannot add implicit input '" + inputName +
					"' to actor " + this + ".");
		}
	}
	
	public void addImplicitOutput(String outputName) throws RestFlowException {
		
		Contract.requires(_state == ActorFSM.PROPERTIES_SET);

		if (! _outputSignature.containsKey(outputName)) {			
			_addOutputToSignature(outputName, null);
		} else {
			throw new IllegalWorkflowSpecException("Cannot add implicit output " + outputName +
					" to actor " + this + ".");
		}
	}
	
	public void addLogOutput() {

		Contract.requires(_state == ActorFSM.PROPERTIES_SET);		
		_addOutputToSignature("__log__", null);
	}
	
	///////////////////////////////////////////////////////////////////////////
	///                   configuration getters                            ////

	public synchronized String getName() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _name;
	}

	public synchronized String getNodeName() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _node.getQualifiedName();
	}

	public synchronized boolean isStateful() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _stateful;
	}
	
	public Collection<String> getInputNames() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _inputSignature.keySet();
	}
	
	public Collection<String> getOutputNames() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _outputSignature.keySet();
	}

	public boolean hasOptionalInput(String name) {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		InputSignatureElement input = _inputSignature.get(name);
		return input != null && input.isOptional();
	}
	
	public String getInputType(String name) {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		InputSignatureElement input = _inputSignature.get(name);
		return input.getType();
	}

	public String getInputLocalPath(String name) {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		InputSignatureElement input = _inputSignature.get(name);
		return input.getLocalPath();
	}
	
	@Override
	public String getInputDescription(String name) {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		InputSignatureElement input = _inputSignature.get(name);
		return input.getDescription();
	}
	
	@Override
	public Object getDefaultInputValue(String name) {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		InputSignatureElement input = _inputSignature.get(name);
		return input.getDefaultValue();
	}

	public synchronized String getStepDirectoryFileSystem() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _stepDirectoryFileSystem;
	}

	// The cloneable property declares whether an actor can 
	// be run cloned and run concurrently with other clones
	// with the same node.  If the property is not declared then
	// the actor is assumed to be cloneable only if it is not
	// stateful.  
	public synchronized boolean isCloneable() {
		
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);

		if (_cloneable != null) {
			return _cloneable;
		} else {
			return ! _stateful;
		}
	}
	
	public synchronized String getFullyQualifiedName() {
		
		if (_node == null) return _name;
		
		
		String nodeName = _node.getQualifiedName();
		if (nodeName == null || nodeName.equals("")) {
			return _name;
		} 
		
		return nodeName + "[" + _name + "]";
	}
	
	public synchronized String toString() {
		if (_node == null) {
			return _name;
		} else {
			return _node.getQualifiedName() + "." + _name;
		}
	}
	
	public ActorFSM state() {
		return _state;
	}
	
	///////////////////////////////////////////////////////////////////////////
	////               public actor lifecycle methods                      ////
	
	public void afterPropertiesSet() throws Exception {
		
		Contract.requires(_state == ActorFSM.CONSTRUCTED);

		if (_workflowContext == null) {
			throw new Exception("Must provide a workflow context to actor " + this +
					" of node " + _node);
		}
	}
	
	public void setLogProtocol(LogProtocol protocol) {
	}

	public void elaborate() throws Exception {
//		Contract.requires(_state == ActorFSM.PROPERTIES_SET);
		_recorder = _workflowContext.getTraceRecorder();
	}
	
	public void configure()throws Exception {
//		Contract.requires(_state == ActorFSM.ELABORATED);
		_runCount = 0;
	}
	
	public void initialize() throws Exception {
		
//		Contract.requires(_state == ActorFSM.CONFIGURED || _state == ActorFSM.WRAPPED_UP);

		for (Map.Entry<String, Object> entry : _defaultInputValues.entrySet()) {
			
			String label = entry.getKey();
			
			// assign default value only if a value has not been assigned by the node
			if (_actorStatus.getInputEnable(label)) {
				this.setInputValue(label, entry.getValue());
			}
		}

		_stepOfScratchDirectory = 0;
		
		resetInputEnables();
	}
	
	public synchronized void reset() throws Exception {
		_runCount = 0;
	}

	// This method is called only on top-level workflows (and solitary actors) from WorkflowRunner.run()
	@Override
	public synchronized void loadInputValues(Map<String, Object> inputBindings) throws Exception {
		
		Contract.requires(_state == ActorFSM.INITIALIZED);

		if (inputBindings != null) {
			for (String name :  _inputSignature.keySet()) {
				Object value = inputBindings.get(name);
				if (value != null) {
					setInputValue(name, value);
				}
			}
		}
	
		// TODO write test that exercises this code at the top-level actor or workflow level
//		for (String name : _inputSignature.keySet()) {
//			if ( _inputValues.get(name) == null) {
//				throw new WorkflowMissingInputException(name, _beanName);
//			}
//		}
	}
		
	public synchronized void loadStateValues(Map<String, Object> states) {
		
		Contract.requires(_state == ActorFSM.INITIALIZED);
		
		if (states.size() > 0) {
			_stateVariables.putAll(states);
		}
	}
	
	public synchronized boolean readyForInput(String label) throws Exception {
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		return _actorStatus.getInputEnable(label);
	}

	public synchronized boolean outputEnabled(String label) throws Exception {
//		Contract.requires(_state == ActorFSM.STEPPED);
		return _actorStatus.getOutputEnable(label);
	}
	
	// shorthand method for setting inputs on workflows (and actors) from Java code using builders
	public void set(String label, Object value) throws Exception {
		setInputValue(label, value);
	}
	
	public synchronized void setInputValue(String label, Object value) throws Exception {
		
		Contract.requires(_state == ActorFSM.CONFIGURED || _state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED || _state == ActorFSM.WRAPPED_UP);

		_inputValues.put(label, value);

		InputSignatureElement signatureElement = _inputSignature.get(label);
		
		if (signatureElement == null) {
			
			if (_constants.containsKey(label)) {
				throw new RestFlowException("Attempt to reassign value of setting '" + label + "' " +
											"on actor " + this);
			}
			
		} else {
		
			if (value == null && !signatureElement.isNullable()) {
				throw new NullInputException(this, label);
			}
		
			_actorStatus.disableInput(label);
		}
	}
	
	protected synchronized void _abstractActorStep() throws Exception {

		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);

		_runCount++;

		resetInputEnables();
		
		for (String label: _outputSignature.keySet()) {
			boolean defaultOutputEnable = _outputSignature.get(label).getDefaultOutputEnable();
			_actorStatus.setOutputEnable(label, defaultOutputEnable);
		}

		if (_usesStepDirectory) {
			_actorStatus.setStepDirectory(_getCurrentStepDirectory());	
		}	
	}
	
	public synchronized void step() throws Exception {
		_abstractActorStep();
	}


	@Override
	public synchronized void setStepCount(int count) {
//		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		_actorStatus.setStepCount(count);
	}
	
	public synchronized void resetInputEnables() {

		Contract.disallows(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.DISPOSED);

		for (String label: _inputSignature.keySet()) {
			boolean defaultInputEnable = _inputSignature.get(label).getDefaultInputEnable();
			_actorStatus.setReadyForInput(label, defaultInputEnable);
		}
	}

	public File getNextStepDirectory() throws Exception {
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		return _createStepScratchDirectoryIfNeeded(1);		
	}
	
	public synchronized Object get(String label) throws Exception {
		return getOutputValue(label);
	}
	
	
	public synchronized Object getOutputValue(String label) throws Exception {
//		Contract.requires(_state == ActorFSM.STEPPED);
		return _outputValues.get(label);
	}

	public synchronized Object getStateValue(String label) throws Exception {
		return _stateVariables.get(label);
	}
	
	public void wrapup() throws Exception {
//		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
	}

	public void dispose() throws Exception {
//		Contract.requires(_state == ActorFSM.WRAPPED_UP);
	}
	

	public Map<String,Object> getFinalOutputs() throws Exception {
		
		Contract.requires(_state == ActorFSM.DISPOSED);

		HashMap<String,Object> outputValues = new HashMap<String,Object>();
	
		for (String outputLabel : _outputSignature.keySet()) {
			outputValues.put(outputLabel, _outputValues.get(outputLabel));
		}
		
		return outputValues;
	}
	
	
	@Override
	public ActorState getFinalState() throws Exception {
		
		Contract.requires(_state == ActorFSM.DISPOSED);
		
		ActorState state = _actorStatus.copyState();
		
		Map <String,Object> stateValues = new HashMap<String,Object>();
		for (String label : _stateVariables.keySet()) {
			stateValues.put(label, _outputValues.get(label));
		}
		state.setStateValues(stateValues);
		
		return state;
	}	
	
	///////////////////////////////////////////////////////////////////////////
	////             protected actor lifecycle methods                     ////

	protected synchronized void _storeOutputValue(String label, Object value) throws Exception {	

		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		
		if (value == null && !_outputSignature.get(label).isNullable()) {
			throw new NullOutputException(this, label);
		}
		
		_outputValues.put(label, value);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	////                   private helper methods                          ////
	
	@SuppressWarnings("rawtypes")
	private void _addInputToSignature(String label, Object value) throws RestFlowException {
				
		InputSignatureElement inputElement = new InputSignatureElement(label);
		
		if (value != null) {
			
			if (!(value instanceof Map)) {
				throw new RestFlowException(
						"Configuration for input '" + label + 
						"' on actor " + this + " must be a map");
			}
			
			String localPath = (String) ((Map)value).get("path");
			if (localPath != null) {
				inputElement.setLocalPath(localPath);
			}
			
			Object defaultValue = ((Map)value).get("default");
			if(defaultValue != null) {
				inputElement.setDefaultValue(defaultValue);
				_defaultInputValues.put(label, defaultValue);
			}
							
			String description = (String) ((Map)value).get("description");
			if (description != null) {
				inputElement.setDescription(description);
			}
			
			String typeName = (String) ((Map)value).get("type");
			if (typeName != null) {					
				_variableTypes.put(label,typeName);
				inputElement.setType(typeName);
				if (typeName.contains("List")) {
					inputElement.setIsList();
				}
			}
			
			Boolean nullable = (Boolean) ((Map)value).get("nullable");
			if (nullable != null && nullable) {
				inputElement.setIsNullable();
			}				

			Boolean optional = (Boolean) ((Map)value).get("optional");
			if (optional != null && optional) {
				inputElement.setIsOptional();
			}				
			
			Boolean defaultReadiness = (Boolean) ((Map)value).get("defaultReadiness");
			if (defaultReadiness != null) {
				inputElement.setDefaultInputEnable(defaultReadiness);
			}
		}

		_inputSignature.put(label, inputElement);
	}
	
	@SuppressWarnings("rawtypes")
	private synchronized void _addOutputToSignature(String label, Object value) {

		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		
		OutputSignatureElement outputElement = new OutputSignatureElement(label);
		
		if (value != null && value instanceof Map) {
			
			String typeName = (String) ((Map)value).get("type");
			if (typeName != null) {		
				_variableTypes.put(label,typeName);
			}

			Boolean nullable = (Boolean) ((Map)value).get("nullable");
			if (nullable != null && nullable) {
				outputElement.setIsNullable();
			}				
		}

		_outputSignature.put(label, outputElement);
		
	}

	private File _getCurrentStepDirectory() throws Exception {
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		return _createStepScratchDirectoryIfNeeded(0);
	}

	private synchronized File _createStepScratchDirectoryIfNeeded(int delta) throws Exception {

		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);

		String runDirectoryPath = _workflowContext.getRunDirectoryPath();
		
		if (runDirectoryPath == null) {
			throw new Exception("No working directory exists for containing scratch directory.");
		}
	
		int step = _actorStatus.getStepCount() + delta;
		String uriPrefix = "";
		if (_node != null) {
			uriPrefix = _node.getUriPrefix();
		}
		
		if (_stepOfScratchDirectory < step || !uriPrefix.equals(_scratchDirectoryPrefix)) {
			String nodeName;
			if (_node != null) {
				nodeName = _node.getQualifiedName();
			} else {
				nodeName = "top";
			}
			if (_stepDirectoryFileSystem.equals("iso9660")) {
				//iso9660 file systems do not allow . in directory structure
				nodeName = nodeName.replaceAll("\\.", "_");
			}
			String scratchDirectory = runDirectoryPath + "/scratch/" + uriPrefix +  "/" + nodeName + "_" + step;
			_stepDirectory = new File(scratchDirectory);
			_stepDirectory.mkdirs();
			_stepOfScratchDirectory = step;
			_scratchDirectoryPrefix = uriPrefix;
		}
		
		return _stepDirectory;
	}
	
	public int getRunCount() {
		return _runCount;
	}
}

