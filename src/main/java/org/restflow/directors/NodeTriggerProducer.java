package org.restflow.directors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.restflow.data.Packet;
import org.restflow.enums.ChangedState;
import org.restflow.nodes.WorkflowNode;


public class NodeTriggerProducer implements Runnable {

	private final WorkflowNode _node;
	private final Map<String, BlockingQueue<Packet>> _labelToQueueMap = new HashMap<String, BlockingQueue<Packet>>();
	private final MTDataDrivenDirector _director;
	private final Object _activeTriggerMonitor = new Object();
	private final int _maxConcurrentTriggers;
	private volatile boolean _isRunning = true;
	
	private int _activeTriggers = 0;
	
	public NodeTriggerProducer(WorkflowNode node, MTDataDrivenDirector director) {
		_node = node;
		_director = director;
		_maxConcurrentTriggers = _node.getMaxConcurrentSteps();
	}
	
	Map<String, BlockingQueue<Packet>> getLabelToQueueMap() {
		return _labelToQueueMap;
	}
	
	private void _incrementActiveTriggers() {
		synchronized(_activeTriggerMonitor) {
			_activeTriggers++;
			_activeTriggerMonitor.notifyAll();
		}
	}

	public void decrementActiveTriggers() {
		synchronized(_activeTriggerMonitor) {
			_activeTriggers--;
			_activeTriggerMonitor.notifyAll();
		}
	}

	public boolean waitForTriggerCompletion() throws InterruptedException {
		
		synchronized(_activeTriggerMonitor) {
			if (_activeTriggers < _maxConcurrentTriggers) {
				return true;
			} else {
				_activeTriggerMonitor.wait(100);	
				return _activeTriggers < _maxConcurrentTriggers;
			}
		}
	}

	
	public boolean waitForActiveTrigger() throws InterruptedException {
		
		synchronized(_activeTriggerMonitor) {
			if (_activeTriggers > 0) {
				return true;
			} else {
				_activeTriggerMonitor.wait(100);
				return _activeTriggers > 0;
			}
		}
	}
	
	public void run() {

		_isRunning = true;
		
		try {
			
			while (!_director.isHalted() && ! _node.isNodeFinished() && !_node.allEosSent()) {
				
				if (! waitForTriggerCompletion()) {
					continue;
				}
				
				ChangedState triggerStarted = ChangedState.FALSE;
				
				try {
					triggerStarted = _node.startTrigger();
				} catch (Exception e) {
					_director.halt(e, this);
					continue;
				}

				if (triggerStarted == ChangedState.TRUE) {
					_incrementActiveTriggers();
				}

				
				if ( triggerStarted == ChangedState.FALSE &&  !_node.isNodeFinished() ) {
					_getInputs();
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		
		_isRunning = false;
	}
	
	public void queueInputPacket(String label, Packet packet) throws Exception {
	
		if (packet == null) {
			throw new Exception("Attempt to queue null input packet on node " + _node);
		}
		
		// System.out.println("Node " + _node + " queueing packet " + ((SingleResourcePacket)packet).getResource());
		
		synchronized(this) {
			
			BlockingQueue<Packet> queueForLabel = _labelToQueueMap.get(label);
			
			if (queueForLabel == null) {
				queueForLabel = new LinkedBlockingQueue<Packet>();
				_labelToQueueMap.put(label,queueForLabel);
			}
			
			queueForLabel.put(packet);
			this.notifyAll();
		}
	}

	private void _getInputs() throws Exception {
		synchronized (this) {
			
			if (!_director.isHalted()  && !_processInputPackets() && !_node.allEosReceived()) {
				this.wait();
			}
		}
	}
	
	private boolean _processInputPackets() throws Exception {
		
		boolean packetReceived = false;

		for (String label : _labelToQueueMap.keySet()) {
			if (_node.readyForInputPacket(label) == true) {
				BlockingQueue<Packet> queue = _labelToQueueMap.get(label);
				if (queue.size() > 0) {
					Packet packet = queue.take();
					_node.setInputPacket(label, packet);
					packetReceived = true;
				}
			}
		}

		return packetReceived;
	}
	
	public String toString() {
		return "NodeThreadRunner for " + _node.getName();
	}

	public synchronized boolean isRunning() {
		return _isRunning || _activeTriggers > 0;
	}
}
