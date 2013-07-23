package org.restflow.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.restflow.actors.AbstractActorRunner;
import org.restflow.actors.Actor;
import org.restflow.actors.ActorRunner;
import org.restflow.actors.Workflow;
import org.restflow.data.ControlProtocol;
import org.restflow.data.Inflow;
import org.restflow.data.LogProtocol;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.data.Protocol;
import org.restflow.data.PublishedResource;
import org.restflow.data.Sequences;
import org.restflow.data.SingleResourcePacket;
import org.restflow.enums.ChangedState;
import org.restflow.exceptions.ActorException;
import org.restflow.exceptions.IllegalWorkflowSpecException;
import org.restflow.exceptions.NodeDeclarationException;
import org.restflow.exceptions.RestFlowException;
import org.restflow.metadata.TraceRecorder;
import org.restflow.metadata.WrapupResult;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

// TODO make endFlowOnNull and endFlowOnNoOutput properties of outflows rather than nodes

/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public class ActorWorkflowNode extends AbstractWorkflowNode {
	
	/**********************************
	 *  private configuration fields  *
	 **********************************/
	
	@GuardedBy("this")	private Actor _actor;
	@GuardedBy("this")	private boolean _endFlowOnNull;
	@GuardedBy("this")	private boolean _endFlowOnNoOutput;
	@GuardedBy("this")	private boolean _repeatValues;
	@GuardedBy("this")	private boolean _endFlowOnActorException;
	@GuardedBy("this")	private boolean _exitOnActorException;
	@GuardedBy("this")	private boolean _concurrent;
	@GuardedBy("this")	private int _maxConcurrency;
	@GuardedBy("this")	private boolean _ordered;
	@GuardedBy("this")	private boolean _validateInflowNames;
	@GuardedBy("this")	private Map<String,Object> _initialValues;	
	@GuardedBy("this")	private Map<String,Object> _constants;		
	@GuardedBy("this")	private Map<String,List<Object>> _sequences;			
	@GuardedBy("this")	private String _nestedUriPrefixTemplate;

	/**************************
	 *  private state fields  *
	 **************************/
	
	@GuardedBy("this")	private Sequences _parameterSequence;
	@GuardedBy("this")	private int _stepCount;
	@GuardedBy("this")	private ActorCompletionService _actorCompletionService;

	/*****************
	 *  constructor  *
	 *****************/
	
	public ActorWorkflowNode() {
		
		super();
		
		synchronized(this) {
			_endFlowOnNull = false;
			_endFlowOnNoOutput = false;
			_repeatValues = false;
			_stepCount = 0;
			_endFlowOnActorException = false;
			_exitOnActorException = false;
			_concurrent = false;
			_ordered = true;
			_maxConcurrency = 1;
			_actorCompletionService = null;
			_validateInflowNames = true;
			_initialValues = new HashMap<String,Object>();
			_constants = new HashMap<String,Object>();			
			_sequences = new HashMap<String,List<Object>>();
			_nestedUriPrefixTemplate = null;
		}
	}
	
	
	/***************************
	 *  configuration setters  *
	 ***************************/	
	
	public synchronized void setActor(Actor actor) {
		_actor = actor;
	}

	public synchronized void setEndFlowOnActorException(boolean endFlowOnActorException) {
		_endFlowOnActorException = endFlowOnActorException;
	}
	
	public synchronized void setExitOnActorException(boolean exitOnException) {
		_exitOnActorException = exitOnException;
	}

	public synchronized void setEndFlowOnNoOutput(boolean endFlowOnNoOutput) {
		_endFlowOnNoOutput= endFlowOnNoOutput;
	}

	public synchronized void setEndFlowOnNull(boolean endFlowOnNull) {
		_endFlowOnNull = endFlowOnNull;
	}

	public synchronized void setMaxConcurrency(int newValue) {
		_maxConcurrency = newValue;
		_concurrent = (_maxConcurrency > 1);
	}	


	public synchronized void setRepeatValues(boolean repeatValues) {
		_repeatValues = repeatValues;
	}
		
	public synchronized void setOrdered(boolean value) {
		_ordered = value;
	}
	
	public synchronized void setValidateInflowNames(boolean validate) {
		_validateInflowNames = validate;
	}

	
	
	public void setInitialValues(Map<String, Object> initialValues) {
		_initialValues = initialValues;
	}


	public void setConstants(Map<String, Object> constants) {
		_constants = constants;
	}


	public void setSequences(Map<String, List<Object>> sequences) {
		_sequences = sequences;
	}

	public synchronized void setNestedUriPrefix(String prefix) throws Exception {
		_nestedUriPrefixTemplate = prefix;		
	}

	/***************************
	 *  configuration getters  *
	 ***************************/
	
	public synchronized Actor getActor() {
		return _actor;
	}

	public synchronized Actor actor() {
		return getActor();
	}

	public synchronized int getMaxConcurrentSteps() { 
		if (_concurrent) {
			return _maxConcurrency;
		} else {
			return 1;
		}
	}

	public synchronized boolean inputIsOptional(String label) {		
		return _actor.hasOptionalInput(label);
	}
	
	/*************************
	 *  instance life cycle  
	 * @throws Exception *
	 *************************/
		
	private synchronized void _assertActorDataInputs(Collection<String> nodeInflowNames, 
			boolean validateInflowNames) throws Exception {	
		
		Collection<String> inputNames = _actor.getInputNames();
		
		// ensure that all node inflows and parameters correspond to actor
		// input variables if any input variables are declared
		if (validateInflowNames && inputNames.size() > 0) { 
		
			for (String nodeInputLabel : nodeInflowNames) {
				if (! inputNames.contains(nodeInputLabel)) {
					throw new NodeDeclarationException("Actor " + _actor + 
							" does not accept input " + "variable \'" +  
							nodeInputLabel + "'.");
				}
			}
			
		// otherwise add implicit input variables
		} else {
			for (String nodeInputLabel : nodeInflowNames) {
				_actor.addImplicitInput(nodeInputLabel);
			}
		}
	}

	private synchronized void _assertActorOutputs(Collection<String> nodeOutflowNames) throws RestFlowException {	

		Collection<String> outputNames = _actor.getOutputNames();
		
		// ensure that all node outflows correspond to actor output 
		// variables if any input variables are declared
		if (outputNames.size() > 0) { 
		
			for (String outputName : nodeOutflowNames) {

				if (! outputNames.contains(outputName)) {
					
					if (outputName.equals("__log__")) {
						
						_actor.addLogOutput();
						
					} else {
						
						throw new NodeDeclarationException("Actor " + _actor + 
								" does not produce output " + "variable \'" +  
								outputName + "\' named by node " + 
								getName() + ".");
					}
				}
			}	
			
		// otherwise add implicit output variables
		} else {
			
			for (String nodeOutput : nodeOutflowNames) {
				_actor.addImplicitOutput(nodeOutput);
			}
		}
	}
	
	
	
	private void _configureParameters() throws IllegalWorkflowSpecException {
		_parameterSequence = new Sequences();
		_parameterSequence.setSequence(_sequences);
		_parameterSequence.setRepeatValues(_repeatValues);
		_parameterSequence.configure();
	}
	
	public void elaborate() throws Exception {

		_constructInflows();
		_constructOutflows();
		
		_configureParameters();

		if (_actor == null) throw new Exception("must set actor property");
		
		if (_nestedUriPrefixTemplate != null) { 
			if (_actor instanceof Workflow) {
				((Workflow)_actor).setUriPrefix(_nestedUriPrefixTemplate);
			} else {
				throw new Exception("A URI prefix may only be applied to nodes invoking subworkflows.");
			}
		}
		
		_actor.setNode(this);
		
		Collection<String> dataInputNames = new LinkedList<String>();
		Collection<String> controlInputNames = new LinkedList<String>();
		for (Map.Entry<String, Inflow> entry : _inflows.entrySet()) {
			Inflow inflow = entry.getValue();
			String label = entry.getKey();
			if (inflow.getProtocol() instanceof ControlProtocol) {
				controlInputNames.add(label);
			} else {
				dataInputNames.add(label);
			}
		}
		dataInputNames.addAll( _parameterSequence.allParameterNames());
//		dataInputNames.addAll( _initialValues.keySet() );
		dataInputNames.addAll( _constants.keySet() );
		
		_assertActorDataInputs(dataInputNames, _validateInflowNames);
		_assertActorControlInputs(controlInputNames);
		_assertActorOutputs(_dataOutflowNames);
		
		_stepsOnce = _stepsOnce || ( ! _actor.isStateful()  && _inflows.size() == 0 && _parameterSequence.maxSequenceLength() <= 1);
		
		_elaborateOutflows();
		
		for (Outflow outflow : _outflows.values()) {
			Protocol protocol = outflow.getProtocol();
			if (protocol instanceof LogProtocol) {
				_actor.setLogProtocol((LogProtocol)protocol);
			}
		}

		_actor.elaborate();
	}
	
	
	public void configure() throws Exception {
		
		super.configure();
		
		_actor.configure();

		// TODO move this block to configure()
		if (_concurrent && ! _actor.isCloneable()) {
			throw new IllegalWorkflowSpecException("Uncloneable actor " + _actor + " cannot be used within concurrent node " + this);
		}
		
		// TODO move this statement to configure()
//		_stepsOnce = _stepsOnce || ( ! _actor.isStateful()  && _inflows.size() == 0 && _parameterSequence.maxSequenceLength() <= 1);
		
		_configureActorCompletionService();
	}

	public synchronized void initialize() throws Exception {
		
		super.initialize();

		_parameterSequence.initialize();
		
		_applyBootParameters();
		_applyConstantParameters();
		
		_actorCompletionService.start();

		_stepCount = 0;
		_actor.setStepCount(_stepCount);
		
		_actor.initialize();
	}
	
	public void reset() throws Exception {
		super.reset();
		_actor.reset();
	}


	public synchronized boolean readyForInputPacket(String label) throws Exception {
		return _actor.readyForInput(label);
	}	

	public ChangedState trigger() throws Exception {
		
		ChangedState triggered = startTrigger();
	
		if (triggered == ChangedState.TRUE) {
			finishTrigger();
		}
		
		return triggered;
	}

	public synchronized ChangedState startTrigger() throws Exception {
		
		// do nothing if the node has finished
		if ( isNodeFinished() ) return ChangedState.FALSE;
		
		// output end-of-streams if node is done stepping
		if ( _checkDoneStepping() == DoneStepping.TRUE) {
			_actorCompletionService.shutdown();
			return ChangedState.TRUE;
		}
		
		// do nothing if not all required inputs have arrived
		if (!_allInputsStaged()) return ChangedState.FALSE;
		
		if (! _applySequenceValuesToInflows()) {
			_actorCompletionService.shutdown();
			return ChangedState.TRUE;
		}

		_clearInflows(false);
		_stepCount++;
		_actor.setStepCount(_stepCount);
		_variables.put("STEP",_stepCount);
		_variables.put("RUN",_actor.getRunCount());

		_actor.resetInputEnables();
		
		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		Long stepID = recorder.recordStepStarted(this);
		_variables.put("STEP_ID", stepID);
		
		_actorCompletionService.submit(_actor, new HashMap<String,Object>(_variables));
		
		return ChangedState.TRUE;
	}
	
	@Override
	public synchronized ChangedState manualStart() throws Exception {
		
		// output end-of-streams if node is done stepping
		if ( _checkDoneStepping() == DoneStepping.TRUE) {
			_actorCompletionService.shutdown();
			return ChangedState.TRUE;
		}
		
		// do nothing if not all required inputs have arrived
		if (!_allInputsStaged()) throw new Exception( "Node " + getName() + " does not have all necessary inputs");
		
		if (! _applySequenceValuesToInflows()) {
			_actorCompletionService.shutdown();
			return ChangedState.TRUE;
		}

//		_clearInflows(false);
		_stepCount++;
		_actor.setStepCount(_stepCount);
		_variables.put("STEP",_stepCount);
		_variables.put("RUN",_actor.getRunCount());

		_actor.resetInputEnables();
		_clearInflows(false);

		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		recorder.recordStepStarted(this);
		
		_actorCompletionService.submit(_actor, new HashMap<String,Object>(_variables));
		
		return ChangedState.TRUE;
	}	
	
	public void finishTrigger() throws Exception {
		
		// do nothing if the node has finished
		if ( isNodeFinished() ) return;
		
		ActorRunner actorRunner = _actorCompletionService.take();

		synchronized(this) {
			
			if (actorRunner == AbstractActorRunner.EndOfActorRunners) {
				_sendEndOfStreamPackets();
				_flagNodeFinished();
				return;
			}
	
			Actor actor = actorRunner.getActor();
			Map<String,Object> variables = actorRunner.getVariables();
			
			Long stepID = (Long) variables.get("STEP_ID");
			
			@SuppressWarnings("rawtypes")
			Class caughtExceptionSuperClass = null;
			
			Exception caughtException = actorRunner.getException();
			if (caughtException != null) {
				caughtExceptionSuperClass = _caughtExceptions.getClosestSuperclass(caughtException.getClass());
				
				if (caughtExceptionSuperClass == null) {
					if (_exitOnActorException) {
						System.err.println("Actor " + _actor + " threw exception:");
						caughtException.printStackTrace(System.err);
						System.exit(1);
					} else if (_endFlowOnActorException) {
						_actorCompletionService.shutdown();
						_sendEndOfStreamPackets();
						_flagNodeFinished();
						return;
					} else {
						throw new ActorException(_actor, caughtException);
					}
				} 
			}
	
//			TraceRecorder recorder = _workflowContext.getTraceRecorder();
//			recorder.recordStepCompleted(this);
//	
			if (_stepsOnce) _flagDoneStepping();
			
			// store all actor outputs in variables table
			for (String label : _actor.getOutputNames()) {
				Object value = actor.getOutputValue(label);
				if (value != null) {
					variables.put(label, value);
				} else {
					variables.remove(label);
				}
			}
		
			if (caughtExceptionSuperClass != null) {
				Outflow outflow = _exceptionOutflows.get(caughtExceptionSuperClass);
				outflow.createAndSendPacket(caughtException, variables, stepID);
			} else {
				_sendOutputPackets(actor, variables, stepID);
			}
			
			TraceRecorder recorder = _workflowContext.getTraceRecorder();
			recorder.recordStepCompleted(this);	
		}
	}
	
	@Override
	public boolean outputsReady() throws Exception {

		boolean enabledOutputsReady = false;
		
		for (Map.Entry<String,Outflow> outflowEntry : _outflows.entrySet()) {

			String label = outflowEntry.getKey();
			Outflow outflow = outflowEntry.getValue();
			
			if (outflow.packetReady()) {
				enabledOutputsReady = true;
				break;
			}
		}
		
		return enabledOutputsReady;
	}
	
	public synchronized WrapupResult wrapup() throws Exception {
		WrapupResult wrapupResult = super.wrapup();
		_actor.wrapup();
		return wrapupResult;	
	}

	public synchronized void dispose() throws Exception {
		super.dispose();
		_actor.dispose();
		_actorCompletionService.shutdown();
	}

	/***********************
	 *  protected methods  *
	 ***********************/

	// TODO: Move to AbstractWorkflowNode?
	protected synchronized boolean _applySequenceValuesToInflows() throws Exception {
		Map<String,Object > sequenceBundle = _parameterSequence.assembleNextSequenceBundle();
		
		for (Map.Entry<String, Object> entry : sequenceBundle.entrySet() ) {
			String label = entry.getKey();
			Object value = entry.getValue();
			
			if (value == null) {
				return false;
			}
			Inflow inflow = _inflows.get( label) ;
			if (inflow != null) {
				inflow.setInputPacket(new SingleResourcePacket( value ));
			}
			_actor.setInputValue( label, value);
			_variables.put(label,value);
		}

		return true;
	}

	
	protected synchronized void _handleEndOfStream(String label, Packet token) throws Exception {
				
		if (_actor.hasOptionalInput(label)) {
			
			// Is this line needed?
			_actor.setInputValue(label, null);
			
			_variables.remove(label);
		}
	}	

	protected synchronized void _loadInputPacket(String label, Packet inputPacket)
			throws Exception {

		super._loadInputPacket(label, inputPacket);
		
		Protocol protocol = inputPacket.getProtocol();
		Inflow inflow = _inflows.get(label);
		PublishedResource resource = inputPacket.getResource(inflow.getPacketBinding());
		
		Object packetPayload = null;
		if (resource != null) {
			packetPayload = protocol.loadResourcePayload(resource,  _actor, label);
		}
		
		_loadInputValue(label, packetPayload);
	}


	protected synchronized void _loadInputValue(String label, Object value) throws Exception {
		_actor.setInputValue(label, value);
	}
	
	
	protected void _loadVariableValue(String name, Object value) throws Exception {		
		_loadInputValue(name, value);
	}

	
	/**
	 * Call after trigger to send outputs that are ready. Depending on the
	 * node's configuration, the stream may abruptly
	 * 
	 * @param actor
	 * @param variables
	 * @param stepID 
	 * @throws Exception
	 */
	protected synchronized void _sendOutputPackets(Actor actor, Map<String, Object> variables, Long stepID) throws Exception {
		
		for (String outflowLabel : _dataOutflowNames) {
			
			Outflow outflow = _outflows.get(outflowLabel);
						
			if (actor.outputEnabled(outflowLabel)) {
				
				Object value = actor.getOutputValue(outflowLabel);
					
				if (value == null) {
					
					if (_endFlowOnNull) {
						_flagDoneStepping();
						outflow.sendPacket(EndOfStreamPacket, stepID);
						return;
					}
				}	
	
				outflow.createAndSendPacket(value, variables, stepID);
			
			} else {
				
				if (_endFlowOnNoOutput) {
					_flagDoneStepping();
					outflow.sendPacket(EndOfStreamPacket, null);
					return;
				}
			}
		}
		
		return;
	}

	/*********************
	 *  private methods  *
	 *********************/
	
	// TODO: Move to AbstractWorkflowNode?
	private synchronized void _applyBootParameters() throws Exception {
		
		for (String label : _initialValues.keySet() ) {
			Object value = _initialValues.get(label);
			Inflow inflow = _inflows.get(label);
			if (inflow != null) {
				inflow.setInputPacket(new SingleResourcePacket(value));
			}
			_actor.setInputValue(label, value);
			_variables.put(label,value);
		}
		
	}

	private synchronized void _applyConstantParameters() throws Exception {
		
		for (String label : _constants.keySet() ) {
			Object value = _constants.get(label);
			Inflow inflow = _inflows.get(label);
			if (inflow != null) {
				inflow.setInputPacket(new SingleResourcePacket(value));
			}
			_actor.setInputValue(label, value);
			_variables.put(label,value);
		}
		
	}	
	
	private synchronized void _assertActorControlInputs(Collection<String> nodeControlInputs) throws NodeDeclarationException {
		
		Collection<String> actorInputLabels = _actor.getInputNames();
		
		for (String nodeInputLabel : nodeControlInputs) {
			if (actorInputLabels.contains(nodeInputLabel)) {
				throw new NodeDeclarationException("Control protocol may not be used on " + getName() + " inflow '" + nodeInputLabel + "', " +
						"because actor " + _actor + " has an input with the same name.");			}
		}
	}
	
	private void _configureActorCompletionService() {
		
		// TODO move starting of actor completion service to configure?
		if (getMaxConcurrentSteps() > 1) {
			if (_ordered) {
				_actorCompletionService = new OrderedActorCompletionService(getMaxConcurrentSteps()+1);
			} else {
				_actorCompletionService = new UnorderedActorCompletionService(getMaxConcurrentSteps()+1);				
			}
		} else {
			_actorCompletionService = new SynchronousActorCompletionService();
		}	
	}
	
	@Override
	protected WrapupResult _checkInflowsForUnconsumedData() {

		List<WrapupResult.UnusedDataRecord> unusedDataRecordList = 
			new LinkedList<WrapupResult.UnusedDataRecord>();

		for (Inflow inflow : _inflows.values()) {
			
			Packet packet = inflow.getInputPacket();
			
			if (packet != null) {
				
				List<Packet> unusedPackets = new ArrayList<Packet>(1);
				unusedPackets.add(packet);
				
				unusedDataRecordList.add(
						new WrapupResult.UnusedDataRecord(
							this.getName(),
							inflow.getLabel(),
							"inflow",
							unusedPackets
						)
				);
			}
			
		}
		
		return new WrapupResult(unusedDataRecordList);
	}

}