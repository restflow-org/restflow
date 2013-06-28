package org.restflow.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.restflow.data.Inflow;
import org.restflow.data.InflowToOutflowsMap;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.data.UriTemplate;
import org.restflow.data.WorkflowOutflows;
import org.restflow.directors.Director;
import org.restflow.enums.WorkflowModified;
import org.restflow.metadata.MetadataManager;
import org.restflow.metadata.RunMetadata;
import org.restflow.metadata.Trace;
import org.restflow.metadata.WrapupResult;
import org.restflow.metadata.WrapupResult.UnusedDataRecord;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.nodes.InPortal;
import org.restflow.nodes.OutPortal;
import org.restflow.nodes.SourceNode;
import org.restflow.nodes.WorkflowNode;
import org.restflow.reporter.Reporter;
import org.restflow.util.Contract;
import org.restflow.util.ImmutableList;
import org.restflow.util.ImmutableMap;
import org.restflow.util.PortableIO;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;


/**
 * This class is thread safe.  Its superclass is thread safe, and all the mutable fields 
 * it adds are synchronized on the instance.
 * 
 * This class should not be cloned (yet).
 */

@ThreadSafe()
public class Workflow extends AbstractActor {

	///////////////////////////////////////////////////////////////////////////
	////                 private singleton instance fields                 ////
	
	@GuardedBy("this") private Director 		_director;
	@GuardedBy("this") private Reporter 		_finalReporter;
	@GuardedBy("this") private Reporter 		_preambleReporter;

	///////////////////////////////////////////////////////////////////////////
	////                    private collection fields                      ////

	@GuardedBy("this") private InflowToOutflowsMap 		_inflowToOutflowsMap;
	@GuardedBy("this") private Map<String, InPortal>	_labelToInputPortMap;
	@GuardedBy("this") private Map<String, OutPortal> 	_labelToOutputPortMap;
	@GuardedBy("this") private List<WorkflowNode> 		_nodes;
	@GuardedBy("this") private Map<String,Reporter> 	_reports;
	@GuardedBy("this") private List<WorkflowNode> 		_sinks;
	
	List<UnusedDataRecord> _unusedDataRecords;
	
	private Map<String, WorkflowNode> _nameToNodeMap;
	protected UriTemplate _runUriPrefix;
	private int _prefixVariableCount;
	private long _nextNodeNumber = 0;
	private InPortal _implicitInportal = null;
	private OutPortal _implicitOutportal = null;
	

	private boolean 	_showPreambleReport = true;
	private boolean 	_showFinalReport = true;
	
	///////////////////////////////////////////////////////////////////////////
	////                  private final class fields                       ////

	private static final ImmutableList<WorkflowNode> EMPTY_WORKFLOW_NODE_LIST = 
			new ImmutableList<WorkflowNode>();
	private static final ImmutableMap<String,Reporter> EMPTY_STRING_REPORTER_MAP =
			new ImmutableMap<String,Reporter>();


	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	
	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public Workflow() {
		
		super();
		
		synchronized(this) {
			
			// initialize collection instance fields
			_inflowToOutflowsMap 	= new InflowToOutflowsMap();
			_labelToInputPortMap 	= new Hashtable<String, InPortal>();
			_labelToOutputPortMap 	= new Hashtable<String, OutPortal>();
			_nodes					= EMPTY_WORKFLOW_NODE_LIST;
			_reports				= EMPTY_STRING_REPORTER_MAP;
			_sinks 					= new LinkedList<WorkflowNode>();
			
			// initialize contract state machine
			_state = ActorFSM.CONSTRUCTED;
		}
	}
	
	public synchronized Object clone() throws CloneNotSupportedException {

		Contract.requires(_state == ActorFSM.INITIALIZED);

		// call the superclass clone method
		Workflow workflowClone = (Workflow)super.clone();
		
		// start clone in PROPERTIES_UNSET state so that properties can be assigned
		workflowClone._state = ActorFSM.CONSTRUCTED;

		// return the clone
		return workflowClone;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	///   workflow configuration setters -- PROPERTIES_UNSET state only    ////
	
	public synchronized void setFinalReporter(Reporter finalReporter) {
//		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_finalReporter = finalReporter;
	}

	public synchronized void setNodes(List<WorkflowNode> nodes) throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);	
		_nodes = new ArrayList<WorkflowNode>(nodes);
	}

	public synchronized void setPreambleReporter(Reporter preambleReporter) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_preambleReporter = preambleReporter;
	}
	
	public void setShowPreambleReport(boolean value) {
		_showPreambleReport = value;
	}

	public void setShowFinalReport(boolean value) {
		_showFinalReport = value;
	}

	public synchronized void setReports(Map<String, Reporter> reports) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		_reports = new HashMap<String, Reporter>(reports);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void setInputs(Map<String, Object> inputs) throws Exception {
		
		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		super.setInputs(inputs);

		Map<String,String> flows = null;
		
		for (Map.Entry<String,Object> entry : inputs.entrySet()) {
			String inputName = entry.getKey();
			Map<String,Object> inputProperties = (Map<String,Object>)entry.getValue();
			if (inputProperties != null ) {
				String flowExpression = (String) inputProperties.get("flow");
				if (flowExpression != null) {
					if (flows == null) {
						flows = new HashMap<String,String>();
					}
					flows.put(inputName, flowExpression);
				}
			}
		}
		
		if (flows != null) {
			_implicitInportal = new InPortal();
			_implicitInportal.setBeanName("WorkflowInportal");
			_implicitInportal.setOutflows(flows);
		}
	}
	
	public synchronized void setOutputs(Map<String, Object> outputs) throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		super.setOutputs(outputs);
		
		Map<String,Object> flows = null;

		for (Map.Entry<String,Object> entry : outputs.entrySet()) {
			String outputName = entry.getKey();
			Map<String,Object> outputProperties = (Map<String,Object>)entry.getValue();
			if (outputProperties != null && outputProperties instanceof Map) {
				@SuppressWarnings("unchecked")
				String flowExpression = (String) outputProperties.get("flow");
				if (flowExpression != null) {
					if (flows == null) {
						flows = new HashMap<String,Object>();
					}
					flows.put(outputName, flowExpression);
				}
			}
		}
	
		if (flows != null) {
			_implicitOutportal = new OutPortal();
			_implicitOutportal.setBeanName("WorkflowOutportal");
			_implicitOutportal.setInflows(flows);
		}
	}


	///////////////////////////////////////////////////////////////////////////
	///   actor configuration setters -- PROPERTES_UNSET or UNCONFIGURED   ////
	
	public synchronized void setDirector(Director director) {
		Contract.requires(_state == ActorFSM.CONSTRUCTED || _state == ActorFSM.PROPERTIES_SET);
		_director = director;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	///    actor configuration mutators -- UNCONFIGURED state only         ////
	
	public synchronized void insertNodeBeforeInflow(WorkflowNode newNode, Inflow inflow) throws Exception {
	
		Contract.requires(_state == ActorFSM.PROPERTIES_SET);
		
		newNode.setApplicationContext(_workflowContext);
		_nodes.add(newNode);
		
		String newNodeInflowBinding = inflow.getPath();
		String inflowPacketBinding = inflow.getPacketBinding();
		String newNodeOutflowBinding = newNode.getName() + newNodeInflowBinding;
		newNode.registerInflow("input", newNodeInflowBinding, false);
		newNode.registerOutflow("output", newNodeOutflowBinding, false);
		
//		newNode.configure();
		
		inflow.getNode().registerInflow(
				inflow.getLabel(), 
				newNodeOutflowBinding, 
				inflowPacketBinding, 
				inflow.receiveOnce());
	}

	/*************************/
	/* configuration getters */
	/*************************/
	
	public synchronized InflowToOutflowsMap getInflowToOutflowsMap() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return new InflowToOutflowsMap(_inflowToOutflowsMap);
	}

	public synchronized Director getDirector() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _director;
	}

	public synchronized List<WorkflowNode> getNodes() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return new ImmutableList<WorkflowNode>(_nodes);
	}

	public synchronized Reporter getFinalReporter() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _finalReporter;
	}

	public synchronized Reporter getPreambleReporter() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return _preambleReporter;
	}

	public synchronized Map<String, Reporter> getReports() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return new ImmutableMap<String, Reporter>(_reports);
	}

	public synchronized List<WorkflowNode> getSinks() {
		Contract.disallows(_state == ActorFSM.CONSTRUCTED);
		return new ImmutableList<WorkflowNode>(_sinks);
	}

	///////////////////////////////////////////////////////////////////////////
	////               public actor lifecycle methods                      ////
	
	public void afterPropertiesSet() throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		super.afterPropertiesSet();
		_state = ActorFSM.PROPERTIES_SET;
	}
	
	public void elaborate() throws Exception {
		
		Contract.requires(_state == ActorFSM.PROPERTIES_SET);

		super.elaborate();
		
		if (_director == null) {
			throw new Exception("No director specified for workflow " + this);
		}
		
		if (_node != null && !_node.stepsOnce() && _prefixVariableCount == 0) {
			throw new Exception("Nested workflows '" + getNodeName() + 
					"' requires a URI prefix with at least one variable unless they step only once.");
		}

		_nameToNodeMap = new HashMap<String,WorkflowNode>();
		
		if (_implicitInportal != null) {
			_nodes.add(_implicitInportal);
		}
		
		if (_implicitOutportal != null) {
			_nodes.add(_implicitOutportal);
		}

		// find a way to move this block after the workflow rewriting so that configure() is
		// called on all nodes including buffer nodes
		for (WorkflowNode node : _nodes) {
			
			// assign the workflow context to the node
			node.setApplicationContext(_workflowContext);
			
			// set workflow reference on node back to this
			node.setWorkflow(this);

			// assign a locally unique default node name if node has no name
			String nodeName = node.getName();
			if (nodeName == null || nodeName.trim().equals("")) {
				nodeName = "anonymous_node_" + ++_nextNodeNumber;
				node.setName(nodeName);
			}
			
			_nameToNodeMap.put(nodeName, node);
			
			if (node instanceof ActorWorkflowNode) {
				Actor actor = ((ActorWorkflowNode) node).actor();
				String actorName = actor.getName();
				
				if (actorName == null || actorName.trim().isEmpty()) {
					actorName = nodeName + "_actor";
					actor.setName(actorName);
				}
			}
			
			// propagate the steps-property from director to all nodes
			if (_director.nodesStepOnce()) {
				node.setStepsOnce(true);
			}
			
			node.elaborate();
		}

		_director.setWorkflow(this);

		while (  _analyzeWorkflowGraph() == WorkflowModified.TRUE || 
			     _director.elaborate()   == WorkflowModified.TRUE     ) {}
		
		
		
		// find a way to move this block after the workflow rewriting so that configure() is
		// called on all nodes including buffer nodes
		for (WorkflowNode node : _nodes) {
			
			// assign the workflow context to the node
			node.setApplicationContext(_workflowContext);
			
			// set workflow reference on node back to this
			node.setWorkflow(this);
		}

		
		_registerPortalInflowsAndOutflows();
		
		_state = ActorFSM.ELABORATED;
	}
	
	public synchronized void configure() throws Exception {
	
		Contract.requires(_state == ActorFSM.ELABORATED);

		super.configure();

		for (WorkflowNode node : _nodes) {
			node.configure();
		}

		_director.configure();		
		
		_state = ActorFSM.CONFIGURED;
	}

	public synchronized void initialize() throws Exception {

//		Contract.requires(_state == ActorFSM.CONFIGURED || _state == ActorFSM.WRAPPED_UP);
		
		super.initialize();
		
		_director.initialize();

		if (_node == null) {
			setStepCount(0);
		}
		
		_state = ActorFSM.INITIALIZED;
	}

	public synchronized void reset() throws Exception {
		
		super.reset();
		
		for (WorkflowNode node : _nodes) {
			node.reset();
		}
		
		Map<String, Object> dataStore = _workflowContext.getDataStore();
		if (dataStore != null) {
			dataStore.clear();
		}		
	}
	
	
	public synchronized void step() throws Exception {
		run();
	}

	public synchronized void run() throws Exception {

		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		
		super.step();
		
		if (_node == null) {
			int currentStepCount = _actorStatus.getStepCount();
			setStepCount(currentStepCount + 1);
		}
		
		if (_actorStatus.getStepCount() > 1 && _runUriPrefix == null && _node == null) {
			throw new Exception("May not rerun a workflow without reinitializing unless " +
					"a run prefix is provided.");
		}
		
		if (_node == null) {
			_recorder.recordWorkflowRunStarted();
		}
		
		String uriPrefixForNodes = _expandNestedUriPrefix();

		if (_showPreambleReport) {
			printPreambleReport();
		}
		
		for (WorkflowNode node : _nodes) {
			node.setUriPrefix(uriPrefixForNodes);
			node.initialize();
		}
		
		_stageDataToInPortals();

		_director.run();
		
		if (_node == null) {
			_recorder.recordWorkflowRunCompleted();
		}

		_unusedDataRecords = new LinkedList<UnusedDataRecord>();
		
		for (WorkflowNode node : _nodes) {
				WrapupResult nodeWrapupResult = node.wrapup();
				_unusedDataRecords.addAll(nodeWrapupResult.getRecords());
		}
		
		WrapupResult wrapupResult = _director.wrapup();
		_unusedDataRecords.addAll(wrapupResult.getRecords());
		
		String wrapupResultMessage = WrapupResult.asString(_unusedDataRecords);
		
		if (wrapupResultMessage.length() > 0) {
			System.err.print(
					"Warning:  Run " + _runCount + " of workflow " + 
					"'" + getFullyQualifiedName() + "'" + 
					" wrapped up with unused data packets:" + PortableIO.EOL +
					 wrapupResultMessage);
		}
		
		// store the outputs of the workflow
		for (String outputName: _outputSignature.keySet()) {
			Object outputValue = _getOutputValueFromOutportal(outputName);
			_storeOutputValue(outputName, outputValue);
			
			if (_node == null) {
				_recorder.recordWorkflowOutputEvent(outputName, outputValue);

			}

		}
		
		if (_showFinalReport) {
			printFinalReport();
		}

		_state = ActorFSM.STEPPED;
	}
	
	public synchronized Object getOutputValue(String label) throws Exception {
		return _outputValues.get(label);
	}

	public  Object get(String label) throws Exception {
		return getOutputValue(label);
	}
	
	private synchronized Object _getOutputValueFromOutportal(String label) throws Exception {
		
		OutPortal portal = _labelToOutputPortMap.get(label);
		
		String binding;
		try {
			binding = portal.getBindingForInflow(label);
		} catch (Exception e) {
			throw new Exception("Could not bind inflow '" + label + "' on Workflow '" + this.getName() +'"',e);
		}
			
		Packet outputPacket = portal.getOutputPacket(label);
		if (outputPacket == null ) return null;
		Object data;
		try {
			data = outputPacket.getResource(binding).getData();
		} catch (Exception e) {
			throw new Exception("Cannot publish collections of resources to workflow outputs.");
		}
		return data;
	}

	public synchronized void wrapup() throws Exception {
		
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);

		super.wrapup();
		
		_state = ActorFSM.WRAPPED_UP;
	}
	
	public synchronized void dispose() throws Exception {
		
		Contract.requires(_state == ActorFSM.WRAPPED_UP);

		super.dispose();
		
		for (WorkflowNode node : _nodes) {
			node.dispose();
		}
		
		_director.dispose();
		
		_state = ActorFSM.DISPOSED;
	}

	
	// TODO Why doesn't superclass implementation suffice?
	public Map<String,Object> getFinalOutputs() throws Exception {
		
		Contract.requires(_state == ActorFSM.DISPOSED);

		HashMap<String,Object> outputValues = new HashMap<String,Object>();
	
		for (String outputLabel : _outputSignature.keySet()) {
			outputValues.put(outputLabel, getOutputValue(outputLabel));
		}
		
		return outputValues;
	}
	
	
	private synchronized WorkflowModified _analyzeWorkflowGraph() throws Exception {
		
		Contract.requires(_state == ActorFSM.PROPERTIES_SET);
		
		WorkflowModified workflowUpdated = WorkflowModified.FALSE;
		List<WorkflowNode> newNodes = new LinkedList<WorkflowNode>();
		
		_inflowToOutflowsMap.clear();
		_sinks.clear();
		
		WorkflowOutflows workflowOutflows = new WorkflowOutflows(this);
		
		// loop over nodes to gather outflows
		for (WorkflowNode node: _nodes) {

			// detect sink nodes
			if (node.getOutflows().size() == 0) {
			
				_sinks.add(node);

			// analyze outflows for each non-sink
			} else {
				
					// loop over the node's outflows
				for (Outflow outflow: node.getOutflows().values()) {

					outflow.setHasReceivers(false);
					
					// get the outflow binding expression
					String outExpression = outflow.getDataflowBinding();
					
					// make sure the binding expression doesn't collide with another
					//if (outflowForExpression.containsKey(outExpression)) {
					//	throw new Exception("Multiple outflows writing to outflow expression " + 
					//			outExpression);
					//}
					
					// store the outflow expression for matching with inflows
					workflowOutflows.addOutflow(outExpression, outflow);
				}
			}
		}
		
		workflowOutflows.assertNoUriExtensionOfAnyOther();

		int sourceNodeCount = 0;


		// analyze inflows and match with outflows
		for (WorkflowNode node: _nodes) {
			
			Set<String> inputLabels = new HashSet<String>();

			// loop over the node's inflows
			for (Inflow inflow: node.getNodeInflows()) {

				// make sure the inflow label hasn't been used for a URI variable on the same node
				_validateInputLabel(node, inputLabels, inflow.getLabel());

				// get the inflow binding expression
				String inExpression = inflow.getDataflowBinding();
				
				// make sure the URI variables have not been used for inflow labels
				// or other URI template variables on the same node
				for (String variableName : inflow.getVariableNames()) {
					_validateInputLabel(node, inputLabels, (String)variableName);
				}
				
				// look up the corresponding outflow
				List<Outflow> outflows = workflowOutflows.get(inExpression);
				
				// make sure there is an outflow corresponding to the inflow
				if (outflows == null) {
					
					outflows = workflowOutflows.findOutflowsStartingWithExpression(inExpression);
				}
				
				if (outflows == null) {

					if (inflow.getProtocol().isExternallyResolvable()) {
							
						SourceNode sourceNode = SourceNode.CreateSourceNodeForInflow("Source" + ++sourceNodeCount, inflow, _workflowContext);
						workflowOutflows.addOutflow(sourceNode.getOutflowExpression(), sourceNode.getOutflow());
						newNodes.add(sourceNode);
						workflowUpdated = WorkflowModified.TRUE;
						
					} else {
					
						throw new Exception("No outflow matching inflow expression for node " + 
							node.getName() + ": " + inExpression);
					}
				} else {
					for (Outflow outflow : outflows) {
						outflow.setHasReceivers(true);
					}
				}
				
				// record the links between inflows and outflows
				_inflowToOutflowsMap.put(inflow, outflows);
			}
		}
		
		_nodes.addAll(newNodes);
		
		return workflowUpdated;
	}
	
	private synchronized String _expandNestedUriPrefix() throws Exception {
		
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		
		String expandedNestedUriPrefix;
		if (_runUriPrefix == null) {
		
			expandedNestedUriPrefix = "";
			
		} else {
			
			Map<String,Object> templateVariables = new HashMap<String,Object>(_inputValues);
			templateVariables.put("RUN", _runCount);
			templateVariables.put("STEP", _actorStatus.getStepCount());
			
			expandedNestedUriPrefix = _runUriPrefix.getExpandedUri(
					templateVariables, 
					new Object[templateVariables.size()], 
					"", 
					"").toString();
		}
		
		String nestedUriPrefix = "";
		
		if (_node == null) {
			
			if (expandedNestedUriPrefix.length() > 0) {
			
				nestedUriPrefix = expandedNestedUriPrefix;
			}
			
		} else {			
			
			nestedUriPrefix = _node.getUriPrefix();
			
			if (expandedNestedUriPrefix.isEmpty()) {
				nestedUriPrefix += "/" + this.getNodeName();
			} else {
				nestedUriPrefix += expandedNestedUriPrefix;
			}
		}

		return nestedUriPrefix;
	}

	private synchronized void _registerPortalInflowsAndOutflows() {
		
		Contract.requires(_state == ActorFSM.PROPERTIES_SET);
		
		for (WorkflowNode node : _nodes) {
	
			if (node instanceof InPortal) {
				InPortal portal = (InPortal) node;
				for (String label : portal.getOutflows().keySet()) {
					_labelToInputPortMap.put(label, portal);
				}
			} else if (node instanceof OutPortal) {
				OutPortal portal = (OutPortal) node;
				for (String label : portal.getInflowLabels()) {
					_labelToOutputPortMap.put(label, portal);
				}
			}
		}		
	}

	private synchronized void _stageDataToInPortals() throws Exception {
	
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		
		for (Map.Entry<String,InPortal> entry : _labelToInputPortMap.entrySet()) {
			String label = entry.getKey();
			InPortal portal = entry.getValue();
			Object value = _inputValues.get(label);
			portal.setInputValue(label, value);
			
			if (_node == null) {
				_recorder.recordWorkflowInputEvent(label, value);

			}
		}
	}

	private synchronized void _validateInputLabel(WorkflowNode node, Set<String> inputLabels, String label) throws Exception {

		Contract.requires(_state == ActorFSM.PROPERTIES_SET);
		
		// check that the new input label hasn't been used yet for this node
		if (!label.isEmpty() && inputLabels.contains(label)) {
			throw new Exception("Node " + node + " has multiple input sources with label " + label);
		}
		
		// remember this label for comparison with other labels
		inputLabels.add(label);
	}
	
	public WorkflowNode node(String name) {
		return _nameToNodeMap.get(name);
	}
	
	public Director director() {
		return _director;
	}

	public void setUriPrefix(String prefix) throws Exception {
		
		_runUriPrefix = new UriTemplate(prefix);

		if (_runUriPrefix.getScheme() != "") {
			throw new Exception("No scheme may be specified for nested uri prefixes: " + prefix);
		}
		
		_prefixVariableCount = _runUriPrefix.getVariableCount();
	}

	public String getReport(String name) throws Exception {
		
		MetadataManager mm = _workflowContext.getMetaDataManager();
		RunMetadata metadata = mm.getRunMetadata(_recorder);		
		Reporter reporter = _reports.get(name);
		reporter.decorateModel( metadata  );
		String report = reporter.getReport();
		return report;
	}
	
	public void printPreambleReport() throws Exception {
		if (_preambleReporter != null) {
			
			Trace trace = _recorder.getReadOnlyTrace();
			
			_preambleReporter.addToModel("workflow", this);
			_preambleReporter.addToModel("trace", trace);
			_preambleReporter.addToModel("inputs", _inputValues);
			_preambleReporter.addToModel("RUN", _runCount);
			_preambleReporter.addToModel("STEP", _actorStatus.getStepCount());
			_preambleReporter.renderReport();
		}
	}
	
	public void printFinalReport() throws Exception {
		if (_finalReporter != null) {
			MetadataManager metadataManager = _workflowContext.getMetaDataManager();
			_finalReporter.decorateModel(metadataManager.getRunMetadata(_workflowContext.getTraceRecorder()));
			_finalReporter.addToModel("workflow", this);
			_finalReporter.addToModel("inputs", _inputValues);
			_finalReporter.addToModel("outputs", _outputValues);
			_finalReporter.addToModel("RUN", _runCount);
			_finalReporter.addToModel("STEP", _actorStatus.getStepCount());
			_finalReporter.renderReport();
		}
	}

	public WorkflowNode getParentNode() {
		return _node;
	}
}