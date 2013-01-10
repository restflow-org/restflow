package org.restflow.directors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.enums.WorkflowModified;
import org.restflow.metadata.WrapupResult;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.Contract;


public class MTDataDrivenDirector extends AbstractDirector {

	///////////////////////////////////////////////////////////////////////////
	////              private singleton instance fields                    ////

	private Exception 		 _actorException;
	private volatile boolean _haltRequested; // volatile ensures visibility across threads

	///////////////////////////////////////////////////////////////////////////
	////              private collection instance fields                   ////
	
	private Map<WorkflowNode, NodeTriggerConsumer> _nodeTriggerConsumers;
	private Map<WorkflowNode, NodeTriggerProducer> _nodeTriggerProducers;
	private List<Thread> 				   		   _threads;

	
	///////////////////////////////////////////////////////////////////////////
	////              public constructors and clone methods                ////

	/**
	 * Creates and initializes the fields of a new instance.
	 */
	public MTDataDrivenDirector() {
		super();
		_state = DirectorFSM.CONSTRUCTED;
	}
	
	///////////////////////////////////////////////////////////////////////////
	////              public director lifecycle methods                    ////
	
	public void afterPropertiesSet() {
		Contract.requires(_state == DirectorFSM.CONSTRUCTED);
		super.afterPropertiesSet();
		_state = DirectorFSM.PROPERTIES_SET;
	}
	
	
	public WorkflowModified elaborate() throws Exception {
		Contract.requires(_state == DirectorFSM.PROPERTIES_SET);
		super.elaborate();
		_state = DirectorFSM.ELABORATED;

		return WorkflowModified.FALSE;
	}
	
	public void configure() throws Exception {
		super.configure();
		_state = DirectorFSM.CONFIGURED;
	}

	
	public void initialize() throws Exception {
		
		Contract.requires(_state == DirectorFSM.CONFIGURED || _state == DirectorFSM.WRAPPED_UP);
		
		super.initialize();
		
		_state = DirectorFSM.INITIALIZED;
	}
	
	public void run() throws Exception {
		
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.WRAPPED_UP);
		
		super.run();
		
		_haltRequested = false;
		_actorException = null;
		
		_threads = new LinkedList<Thread>();

		_createNodeTriggerThreads();
		_createCommunicationChannels();
		
		_state = DirectorFSM.RUNNING;

		// start all of the node threads
		for (Thread thread : _threads) {
			thread.start();
		}

		// wait for all of the node threads
		for (Thread thread : _threads) {
			thread.join();
		}

		_state = DirectorFSM.RAN;

		if (_actorException != null) {
			throw _actorException;
		}
	}

	public WrapupResult wrapup() throws Exception {
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.RAN);
		super.wrapup();
		WrapupResult result = _checkPacketQueuesForUnusedData();
		_state = DirectorFSM.WRAPPED_UP;
		return result;
	}
	
	private WrapupResult _checkPacketQueuesForUnusedData() {
		
		List<WrapupResult.UnusedDataRecord> unusedDataRecordList =
			new LinkedList<WrapupResult.UnusedDataRecord>();
		
		for (Map.Entry<WorkflowNode, NodeTriggerProducer> nodeProducerMapEntry : _nodeTriggerProducers.entrySet()) {
			
			WorkflowNode node = nodeProducerMapEntry.getKey();
			NodeTriggerProducer triggerProducer = nodeProducerMapEntry.getValue();
			
			for (Map.Entry<String, BlockingQueue<Packet>> labelQueueMapEntry : triggerProducer.getLabelToQueueMap().entrySet()) {
				
				String label = labelQueueMapEntry.getKey();
				BlockingQueue<Packet> queue = labelQueueMapEntry.getValue();
				
				if  (queue.size() > 0) {
					List<Packet> packetList = new LinkedList<Packet>();
					queue.drainTo(packetList);
					unusedDataRecordList.add(
							new WrapupResult.UnusedDataRecord(
								node.getName(),
								label,
								"queue",
								packetList
							)
					);
				}
			}
		}
		
		return new WrapupResult(unusedDataRecordList);
	}
	
	public void dispose() throws Exception {
		super.dispose();
		Contract.requires(_state == DirectorFSM.WRAPPED_UP);
		_state = DirectorFSM.DISPOSED;
	}

	///////////////////////////////////////////////////////////////////////////
	////             child thread callback methods                         ////

	// Note that _haltRequested is volatile so that accesses to the variable do not need to be synchronized
	// to make its value visible across threads.  This method is synchronized so that _haltRequested and
	// _actorException are only set once per run of the director.
	public synchronized void halt(Exception exception, Runnable nodeThreadRunner) {
		
		Contract.requires(_state == DirectorFSM.RUNNING);

		// return immediatley if a halt has already been reqeusted
		if (_haltRequested) return;

		// record the request for a halt
		_haltRequested = true;
		
		// wake up all of the node threads so that the halt is sure to be noticed
		for (NodeTriggerProducer producer : _nodeTriggerProducers.values()) {
			if (producer != nodeThreadRunner) {
				synchronized(producer) {
					producer.notifyAll();
				}
			}
		}

		for (NodeTriggerConsumer consumer : _nodeTriggerConsumers.values()) {
			if (consumer != nodeThreadRunner) {
				synchronized(consumer) {
					consumer.notifyAll();
				}
			}
		}

		// save the actor exception provided by the thread requesting the halt
		_actorException  = exception;
	}

	public boolean isHalted() {
		
		Contract.requires(_state == DirectorFSM.RUNNING);

		return _haltRequested;
	}
	
	///////////////////////////////////////////////////////////////////////////
	////                   private helper methods                          ////
	
	private void _createCommunicationChannels() throws Exception {
	
			Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.WRAPPED_UP);
			
			for (Inflow inflow : _inflowToOutflowsMap.keySet()) {
				// set up an inter-thread communication channel corresponding to
				// each inflow
				for (Outflow outflow : _inflowToOutflowsMap.get(inflow)) {
	
					// look up the threads managing the source and receiver
					// nodes for the current inflow
					NodeTriggerConsumer consumer = _nodeTriggerConsumers
							.get(outflow.getNode());
					NodeTriggerProducer producer = _nodeTriggerProducers.get(inflow
							.getNode());
	
					// subscribe the receiver thread to the source thread for
					// this inflow
					consumer.registerReceiver(producer, outflow, inflow);
				}
			}
		}

	private void _createNodeTriggerThreads() {
		
		Contract.requires(_state == DirectorFSM.INITIALIZED || _state == DirectorFSM.WRAPPED_UP);

		// create a structure for storing mapping between nodes and threads
		_nodeTriggerProducers = new HashMap<WorkflowNode, NodeTriggerProducer>();
		_nodeTriggerConsumers = new HashMap<WorkflowNode, NodeTriggerConsumer>();

		// create a thread for each node and store in node-thread mapping
		for (WorkflowNode node : _nodes) {
			
			NodeTriggerProducer producer =  new NodeTriggerProducer(node, this);
			_nodeTriggerProducers.put(node, producer);
			Thread producerThread = new Thread(producer, node.getName());
			_threads.add(producerThread);

			NodeTriggerConsumer consumer =  new NodeTriggerConsumer(node, this, producer);
			_nodeTriggerConsumers.put(node, consumer);
			Thread consumerThread = new Thread(consumer, node.getName());
			_threads.add(consumerThread);
		}
	}
}