package org.restflow.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.restflow.WorkflowContext;
import org.restflow.actors.Workflow;
import org.restflow.data.ControlProtocol;
import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.data.Protocol;
import org.restflow.data.ProtocolRegistry;
import org.restflow.data.PublishedResource;
import org.restflow.data.SingleResourcePacket;
import org.restflow.data.SortedClassVector;
import org.restflow.data.Uri;
import org.restflow.data.UriTemplate;
import org.restflow.enums.ChangedState;
import org.restflow.exceptions.IllegalWorkflowSpecException;
import org.restflow.exceptions.RestFlowException;
import org.restflow.metadata.TraceRecorder;
import org.restflow.metadata.WrapupResult;
import org.restflow.util.ImmutableMap;
import org.restflow.util.ImmutableSet;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This class is thread safe.  All of its mutable fields are synchronized on the instance.
 */
@ThreadSafe()
public abstract class AbstractWorkflowNode implements WorkflowNode, 
	ApplicationContextAware, BeanNameAware, InitializingBean {
	
	/**********************************
	 *  private configuration fields  *
	 **********************************/
	
	@GuardedBy("this")	private String _beanName;	
	@GuardedBy("this")	private String _name;	
	@GuardedBy("this")	private String _qualifiedName;
	@GuardedBy("this")	private boolean _isHidden;
	@GuardedBy("this")	private boolean _enableLog;
	@GuardedBy("this")	private String _uriPrefix;
	@GuardedBy("this")	private Map<String,Object> _inflowConfiguration;
	@GuardedBy("this")	private Map<String,String> _outflowConfiguration;
	@GuardedBy("this")	private Map<String, String> _exceptionConfiguration;

	/***********************************
	 *  protected configuration fields *
	 ***********************************/
	@GuardedBy("this")	protected Map<String,Inflow> _inflows;
	@GuardedBy("this")	protected Map<String,Outflow> _outflows;
	@GuardedBy("this")	protected Map<Class<? extends Exception>,Outflow> _exceptionOutflows;
	@GuardedBy("this")	protected Set<String> _dataOutflowNames;
	@GuardedBy("this")	protected SortedClassVector _caughtExceptions;

	/**************************
	 *  private state fields  *
	 **************************/
	@GuardedBy("this")	private int _receivedEosCount;
	@GuardedBy("this")	private boolean _allEosSent;

	/***************************
	 *  protected state fields *
	 ***************************/
	@GuardedBy("this")	private DoneStepping _doneStepping;
	@GuardedBy("this")	private NodeFinished _isFinished;
	@GuardedBy("this")	protected boolean _stepsOnce;
	@GuardedBy("this")	protected Map<String,Object> _variables;
	
	protected WorkflowContext _workflowContext;
	protected Workflow _workflow;
	
	/***************************
	 *  public static fields   *
	 ***************************/
	public static final PublishedResource 	EndOfStreamResource;
	public static final Packet 				EndOfStreamPacket;

	/***************************
	 *   static initializers   *
	 ***************************/
	static {
		String endOfStreamValue 	= "EndOfStream";
		Uri endOfStreamUri 			= new Uri("/EndOfStream");
		EndOfStreamResource 		= new PublishedResource(endOfStreamValue, endOfStreamUri);
		EndOfStreamPacket 			= new SingleResourcePacket(EndOfStreamResource);
	}
	
	/***************************
	 *  private static fields  *
	 ***************************/
	private static final ImmutableMap<String,Object> EMPTY_STRING_OBJECT_MAP = new ImmutableMap<String,Object>();
	private static final ImmutableMap<String,String> EMPTY_STRING_STRING_MAP = new ImmutableMap<String,String>();


	/*****************
	 *  constructor  *
	 *****************/

	public AbstractWorkflowNode() {

		// initialize instance fields in synchronize block 
		// to insure visibility to other threads
		synchronized(this) {
			_isHidden = false;
			_enableLog = false;
			_receivedEosCount = 0;
			_uriPrefix = "";
			_inflowConfiguration = EMPTY_STRING_OBJECT_MAP;
			_outflowConfiguration = EMPTY_STRING_STRING_MAP;
			_exceptionConfiguration = EMPTY_STRING_STRING_MAP;
			_isFinished = NodeFinished.UNINITIALIZED;
			_doneStepping = DoneStepping.UNINITIALIZED;
			_allEosSent = false;
			_stepsOnce = false;
			_inflows = new Hashtable<String,Inflow>();
			_outflows = new Hashtable<String,Outflow>();
			_variables = new HashMap<String,Object>();
		}
	}
	
	/**************************
	 *  Configuration setters *
	 **************************/
	
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
	public synchronized void setName(String name) {
		_name = name;
	}

	public synchronized void setWorkflow(Workflow workflow) {
		_workflow = workflow;
	}
	
	public synchronized void registerInflow(String label, String dataflowBinding, String packetBinding, boolean receiveOnce) throws Exception {
	
		ProtocolRegistry registry = _workflowContext.getProtocolRegistry();
		
		UriTemplate uriTemplate = new UriTemplate(dataflowBinding);
		String scheme = uriTemplate.getScheme();
		Protocol protocol = registry.getProtocolForScheme(scheme);

		if (protocol == null) {
			throw new Exception("No protocol for scheme " + scheme + " on inflow " + label + " for node " + this);
		}

		Inflow inflow = new Inflow(this, label, uriTemplate, packetBinding, protocol, receiveOnce);
		_inflows.put(label, inflow);
	}


	public synchronized void registerInflow(String label, String binding, boolean receiveOnce) throws Exception {
		
		ProtocolRegistry registry = _workflowContext.getProtocolRegistry();
		UriTemplate uriTemplate = new UriTemplate(binding);
		String scheme = uriTemplate.getScheme();
		Protocol protocol = registry.getProtocolForScheme(uriTemplate.getScheme());
		
		if (protocol == null) {
			throw new Exception("No protocol for scheme '" + scheme + ":' on inflow " + label + " for node " + this);
		}
		
		Inflow inflow = new Inflow(this, label, uriTemplate, uriTemplate.getReducedPath(), protocol, receiveOnce);
		_inflows.put(label, inflow);
	}

	public synchronized void replaceInflow(String label, String binding) throws Exception {

		// preserve receiveOnce property value of replaced inflow
		Inflow oldInflow = _inflows.get(label);
		boolean receiveOnce = oldInflow.receiveOnce();
		registerInflow(label, binding, receiveOnce);
	}


	public synchronized Outflow registerOutflow(String label, String binding, boolean isDefaultUri) throws Exception {
	
		ProtocolRegistry registry = _workflowContext.getProtocolRegistry();

		UriTemplate uriTemplate = new UriTemplate(binding);
		String scheme = uriTemplate.getScheme();
		Protocol protocol = registry.getProtocolForScheme(scheme);
		
		if (protocol == null) {
			throw new Exception("No protocol for scheme " + scheme + " on outflow " + label + " for node " + this);
		}

		Outflow outflow = new Outflow(this, label, uriTemplate, isDefaultUri, protocol);
		outflow.setApplicationContext(_workflowContext);
		_outflows.put(label, outflow);
		
		return outflow;
	}


	public synchronized void setExceptions(Map<String, String> exceptionConfiguration) throws Exception {
		_exceptionConfiguration = exceptionConfiguration;
	}

	public final synchronized void setHidden(boolean hidden) {
		_isHidden = hidden;
	}


	public synchronized void setInflows(Map<String, Object> inflowConfiguration) throws Exception {
		
		_inflowConfiguration = inflowConfiguration;
	}


	public synchronized void setOutflows(Map<String, String> outflowConfiguration) throws Exception {
		_outflowConfiguration = outflowConfiguration;
	}

	public synchronized void setUriPrefix(String prefix) {
		_uriPrefix = prefix;
	}

	public synchronized void setEnableLog(boolean enable) {
		_enableLog = enable;
	}
	
	public synchronized void setStepsOnce(boolean stepsOnce) {
		_stepsOnce = stepsOnce;
	}
	
	public synchronized Set<String> getDataOuflowNames() {
		return new ImmutableSet<String>(_dataOutflowNames);
	}


	public synchronized Collection<String> getInflowLabels() {
		return _inflows.keySet();
	}


	public synchronized Map<String,Inflow> getInflows() {
		return new ImmutableMap<String,Inflow>(_inflows);
	}


	public synchronized Map<String,Inflow> getLabelInflowMap() {
		return getInflows();
	}


	public int getMaxConcurrentSteps() { 
		return 1;
	}

	public synchronized Collection<Inflow> getNodeInflows() {
		return _inflows.values();
	}


	/**************************
	 *  Configuration getters *
	 **************************/
	
	public synchronized String getName() {
		return _name;
	}

	public synchronized String getBeanName() {
		return _beanName;
	}

	public synchronized String getQualifiedName() {
		
		if (_qualifiedName == null) {
			
			if (_workflow == null) {
				
				_qualifiedName = _name;
			
			} else {
				
				StringBuffer buffer = new StringBuffer("." + _name);
				
				WorkflowNode parentNode =_workflow.getParentNode();
				
				if (parentNode == null) {
					buffer.insert(0, _workflow.getName());
				} else {
					buffer.insert(0, parentNode.getQualifiedName());
				}

				_qualifiedName = buffer.toString();
			}
		} 
		
		return _qualifiedName;
	}
	

	public synchronized Map<String, Outflow> getOutflows() {
		return new ImmutableMap<String, Outflow>(_outflows);
	}



	public synchronized String getUriPrefix() {
		return _uriPrefix;
	}


	public boolean inputIsOptional(String label) {
		return false;
	}

	public synchronized boolean isHidden() {
		return _isHidden;
	}	

	public synchronized boolean stepsOnce() {
		return _stepsOnce;
	}


	public synchronized String toString() {
		return _name;
	}


	/**************************
	 *     Node lifecycle     *
	 **************************/
	public void afterPropertiesSet() throws Exception {
		
		if (_workflowContext == null) {
			throw new Exception("Must provide a workflow context to node " + this);
		}
	}

	
	public void elaborate() throws Exception {
		_constructInflows();
		_constructOutflows();
		_elaborateOutflows();
		
	}
	
	protected void _elaborateOutflows() throws Exception {
		for (Outflow outflow : _outflows.values()) {
			outflow.elaborate();
		}
	}
	
	public void configure() throws Exception {	
		for (Outflow outflow : _outflows.values()) {
			outflow.configure();
		}
	}
	
	public void validate() throws Exception {	
	}

	public synchronized void initialize() throws Exception {
		_doneStepping = DoneStepping.FALSE;		
		_isFinished = NodeFinished.FALSE;
		_receivedEosCount = 0;
		_allEosSent = false;
		_variables.clear();
		_clearInflows(true);
		
		// reset state of inflows
		for (Inflow inflow : _inflows.values()) {
			inflow.initialize();
		}

		validate();
		
		// validate and reset state of outflows
		for (Outflow outflow : _outflows.values()) {
			outflow.initialize();
		}
	}
	
	public void reset() throws Exception {
	}
	
	public abstract boolean readyForInputPacket(String label) throws Exception;
	
	synchronized void writeValueToInflow(String label, Object value) throws Exception {
		 Packet packet = new SingleResourcePacket(value);
		 setInputPacket(label, packet);
	}
	
	public synchronized void setInputPacket(String label, Packet packet) throws Exception {

		TraceRecorder recorder = _workflowContext.getTraceRecorder();

		Inflow inflow = _inflows.get(label);

		if (packet == EndOfStreamPacket) {

			inflow.setEosReceived();
			_receivedEosCount++;
			
			if (allEosReceived() || (! inflow.receiveOnce() && ! inputIsOptional(label)) )  {
				_flagDoneStepping();
			}

			if (! inflow.receiveOnce()) { 
				_variables.remove(label);
				_handleEndOfStream(label, packet);
			}
			
			recorder.recordPacketReceived(inflow, packet);

		} else {

			String[] inflowVariableNames = _inflows.get(label).getVariableNames();
			String[] packetMetadataKeys = packet.getMetadataKeys();
			Object[] packetMetadataValues = packet.getMetadataValues();
			
			
			for (int i = 0; i < packetMetadataKeys.length; i++) {
				String inflowVariableName = inflowVariableNames[i];
				Object value = packetMetadataValues[i];
				_variables.put(inflowVariableName, value);
				_loadVariableValue(inflowVariableName, value);
			}

			
			if (inflow.getProtocol() instanceof ControlProtocol) {
				_loadControlPacket(label, packet);
			} else {
				_loadInputPacket(label, packet);
			}
		}
	}

	public abstract ChangedState trigger() throws Exception;
	
	public ChangedState startTrigger() throws Exception {
		return trigger();
	}

	public void finishTrigger() throws Exception {
	}

	public boolean outputsReady() throws Exception {

		for (Outflow outflow : _outflows.values()) {
			if (outflow.packetReady() == false)
				return false;
		}
		return true;
	}
	
	public synchronized Packet getOutputPacket(String label) throws Exception {
		
		Outflow outflow = _outflows.get(label);
		
		Packet packet = outflow.get();

		if (packet == null) {
			throw new Exception("Null packet found in outflow '" + outflow + "' on node " + this);
		}
		return packet;
	}

	public synchronized Object readValueFromOutflow(String label) throws Exception {
		SingleResourcePacket packet = (SingleResourcePacket)getOutputPacket(label);		
		PublishedResource resource =  packet.getResource();
		return resource.getData();
	}
	
	public synchronized Packet peekOutputPacket(String label) throws Exception {
		
		Outflow outflow = _outflows.get(label);
		
		Packet packet = outflow.peek();

		if (packet == null) {
			throw new Exception("Null packet found in outflow '" + outflow + "' on node " + this);
		}
		return packet;
	}	
	
	public synchronized boolean allEosReceived() {
		return _receivedEosCount == _inflows.size();
	}

	@Override
	public synchronized boolean allEosSent() {
		return _allEosSent;
	}

	@Override
	public WrapupResult wrapup() throws Exception {
		_isFinished 	= NodeFinished.UNINITIALIZED;
		_doneStepping 	= DoneStepping.UNINITIALIZED;
		WrapupResult wrapupResult =_checkInflowsForUnconsumedData();
		wrapupResult.addRecords(_checkOutflowsForUnsentData().getRecords());
		return wrapupResult;
	}

	protected WrapupResult _checkInflowsForUnconsumedData() {
		return new WrapupResult();
	}
	
	
	private WrapupResult _checkOutflowsForUnsentData() {

		List<WrapupResult.UnusedDataRecord> unusedDataRecordList = 
			new LinkedList<WrapupResult.UnusedDataRecord>();
		
		for (Outflow outflow : _outflows.values()) {
			if (outflow.hasReceivers() && outflow.packetReady()) {

				List<Packet> unusedPackets = new ArrayList<Packet>();
				unusedPackets.add(outflow.peek());
				
				unusedDataRecordList.add(
						new WrapupResult.UnusedDataRecord(
							this.getName(),
							outflow.getLabel(),
							"outflow",
							unusedPackets
						)
				);
			}
		}
			
		return new WrapupResult(unusedDataRecordList);
	}
	
	public void dispose() throws Exception {		
	}

	/**************************
	 * Comparator for sorting *
	 **************************/
	
	public synchronized int compareTo(WorkflowNode node) {
		return _name.compareTo(node.getName());
	}
	
	
	/**************************
	 *    Protected methods   *
	 **************************/
	
	protected synchronized boolean _allInputsStaged() throws Exception {
	
		for (Map.Entry<String, Inflow> entry : _inflows.entrySet()) {
			
			Inflow inflow = entry.getValue();
			String label = entry.getKey();
			if (!inflow.hasInputPacket() && readyForInputPacket(label)) return false;
		}
		
		return true;
	}


	protected synchronized void _clearInflows(boolean clearAll) {
		for (Inflow inflow : _inflows.values()) {
		
			if (clearAll || ! inflow.receiveOnce()) {
				inflow.clear();
			}
		}
	}


	protected void _handleEndOfStream(String label, Packet endOfStreamPacket) throws Exception {}


	protected synchronized void _loadInputPacket(String label, Packet inputPacket) throws Exception {

		Inflow inflow = _inflows.get(label);
		inflow.setInputPacket(inputPacket);
		PublishedResource resource = inputPacket.getResource(inflow.getPacketBinding());
		if (resource != null) {
			_variables.put(label, resource.getData());
		}

		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		recorder.recordPacketReceived(inflow, inputPacket);
	}


	protected void _loadInputValue(String label, Object value) throws Exception {
	}


	protected void _loadVariableValue(String inflowVariableName, Object value) throws Exception {
	}


	protected synchronized void _sendEndOfStreamPackets() throws Exception {
		
		for (Outflow outflow: _outflows.values()) {
			outflow.sendPacket(EndOfStreamPacket, null);
		}
		
		_allEosSent  = true;
	}


	private synchronized void _addOutflowConfiguration(Map<String,String> outflowConfiguration) throws Exception {
		
		for (Map.Entry<String, String> entry : outflowConfiguration.entrySet()) {
			String label = entry.getKey();
			String uri = entry.getValue();
			if (uri == null) {
				uri = _name + "." + label;
				registerOutflow(label, uri, true);
			} else {
				registerOutflow(label, uri, false);
			}
		}
	}

	protected synchronized void _constructInflows() throws Exception {
		
		int receiveOnceInflowCount = 0;
		_inflows = new HashMap<String,Inflow>();
		for (Map.Entry<String, Object> entry : _inflowConfiguration.entrySet()) {
			String label = entry.getKey();
			Object value = entry.getValue();
			
			String expression = null;
			Boolean receiveOnce = false;
			if (value instanceof Map) {
				Map inflowProperties = (Map)value;
				expression = (String) inflowProperties.get("expression");
				receiveOnce = (Boolean) inflowProperties.get("receiveOnce");
				if (receiveOnce == null) {
					receiveOnce = false;
				}
			} else {
				expression = (String) value;
			}
			if (expression == null) {
				throw new IllegalWorkflowSpecException("No expression provided for inflow '" 
						+ label + "' on node " + _name);
			}
			if (receiveOnce) receiveOnceInflowCount++; 
			registerInflow(label, expression, receiveOnce);
		}
		
		if (receiveOnceInflowCount == _inflows.size() && _inflows.size() >0) {
			throw new IllegalWorkflowSpecException("At least one inflow must not receive once on node " 
					+ _name);
		}
	}

	protected synchronized void _constructOutflows() throws Exception {
	
		_addOutflowConfiguration(_outflowConfiguration);
		_dataOutflowNames = new HashSet<String>(_outflowConfiguration.keySet());
		
		_exceptionOutflows = new Hashtable<Class<? extends Exception>,Outflow>();
		_addOutflowConfiguration(_exceptionConfiguration);
	
		_registerCaughtExceptions();
		
		if (_enableLog) {
			String uri = "log:/" + _name + "/log";
			registerOutflow("__log__", uri, true);			
			_dataOutflowNames.add("__log__");
		}
	}


	private synchronized void _loadControlPacket(String label, Packet inputPacket) throws Exception {
		Inflow inflow = _inflows.get(label);
		inflow.setInputPacket(inputPacket);
		
		TraceRecorder recorder = _workflowContext.getTraceRecorder();
		recorder.recordPacketReceived(inflow, inputPacket);
	}

	@SuppressWarnings({ "unchecked" })
	private synchronized void _registerCaughtExceptions() throws Exception {
		
		_caughtExceptions = new SortedClassVector();
		
		for (String exceptionClassName : _exceptionConfiguration.keySet()) {
			Class<? extends Exception> exceptionClass;
			try {
				exceptionClass = (Class<? extends Exception>) Class.forName(exceptionClassName);
			} catch (ClassNotFoundException e) {
				throw new Exception("Error registering actor exception handler for node " + this + 
					". No such class " + exceptionClassName);
			}
			_caughtExceptions.add(exceptionClass);
			Outflow outflow = _outflows.get(exceptionClassName);
			_exceptionOutflows.put(exceptionClass, outflow);
		}
	}
	
	
	/********************************
	 * public static helper methods *
	 ********************************/

	@Override
	public ChangedState manualStart() throws Exception {
		return trigger();
	}
	@Override
	public void manualFinish() throws Exception {
		finishTrigger();
	}	
	
	
	protected synchronized void _flagDoneStepping() {
		_doneStepping = DoneStepping.TRUE;
	}
	protected synchronized DoneStepping _checkDoneStepping() throws RestFlowException {
		if ( _doneStepping == DoneStepping.UNINITIALIZED) throw new RestFlowException("Node was not initialized properly.");		
		return _doneStepping;
	}
	
	protected synchronized void _flagNodeFinished() {
		_isFinished = NodeFinished.TRUE;
	}
	
	@Override
	public synchronized boolean isNodeFinished() throws RestFlowException {
		if ( _isFinished == NodeFinished.UNINITIALIZED) throw new RestFlowException("Node was not initialized properly.");
		return (_isFinished == NodeFinished.TRUE);
	}
	
	
}