package org.restflow.metadata;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Date;

import net.jcip.annotations.ThreadSafe;

import org.restflow.WorkflowContext;
import org.restflow.actors.Workflow;
import org.restflow.data.Inflow;
import org.restflow.data.MultiResourcePacket;
import org.restflow.data.Outflow;
import org.restflow.data.Packet;
import org.restflow.data.Protocol;
import org.restflow.data.PublishedResource;
import org.restflow.data.SingleResourcePacket;
import org.restflow.data.Uri;
import org.restflow.nodes.AbstractWorkflowNode;
import org.restflow.nodes.WorkflowNode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


/**
 * This class is thread safe.  Access to each field is synchronized
 * on a documented field.
 */
@ThreadSafe()
public class BasicTraceRecorder implements TraceRecorder {

	private DumperOptions _yamlDumperOptions;

	private Map<String,Object> _dataStore;
	private WritableTrace _writableTrace;
	private WorkflowContext _workflowContext;
	
	private Map<Inflow,Long> 				_inflowIdMap	  = new Hashtable<Inflow,Long>();
	private Map<Outflow,Long> 				_outflowIdMap	  = new Hashtable<Outflow,Long>();
	private Map<WorkflowNode,Long> 			_nodeIdMap 		  = new Hashtable<WorkflowNode,Long>();
	private Set<Packet>						_unsentPackets	  = new HashSet<Packet>();
	
	private final String EOL = System.getProperty("line.separator");
	
	public Map<Long,Long> _currentStepIdMap = new Hashtable<Long,Long>();
		
	public BasicTraceRecorder(BasicTraceRecorder parentRecorder) {
		_currentStepIdMap.putAll(parentRecorder._currentStepIdMap);
	}

	public BasicTraceRecorder() {
	}

	public synchronized void setApplicationContext(ApplicationContext context) throws BeansException {
		_workflowContext = (WorkflowContext)context;
	}

	public void setDataStore(Map<String,Object> dataStore) {
		_dataStore = dataStore;
	}
	
	public void createTrace() throws Exception {

		MetadataManager metadataManager = _workflowContext.getMetaDataManager();

		if (metadataManager instanceof FileSystemMetadataManager) {
			String metadataDirectory = ((FileSystemMetadataManager)metadataManager).getMetadataDirectory();
			_writableTrace = new WritableTrace(_workflowContext, metadataDirectory);
		} else {
			_writableTrace = new WritableTrace(_workflowContext);
		}
	}

	public WritableTrace getWritableTrace() {
		synchronized(_writableTrace) {
			return _writableTrace;
		}
	}

	@Override
	public Trace getReadOnlyTrace() throws SQLException {
		return _writableTrace.getReadOnlyTrace();
	}
	
	public void recordWorkflowGraph(Workflow workflow) throws Exception {
		
		if (_writableTrace == null) {
			createTrace();
		}
		
		synchronized(_writableTrace) {
			_writableTrace.storeWorkflowGraph(workflow, null);
		}
	}
	
	public void close() throws SQLException {

		if (_writableTrace != null) {
			_writableTrace.close();
		}
	}
	
	public void recordPacketReceived(Inflow inflow, Packet packet) throws Exception {
		
		WorkflowNode node = inflow.getNode();
		Long nodeID	= _getNodeID(node);
		String label = inflow.getLabel();
	
		if (packet != AbstractWorkflowNode.EndOfStreamPacket) {

			synchronized(_writableTrace) {
		
				Long portID	= _inflowIdMap.get(inflow);
				if (portID == null) {
					portID = _writableTrace.identifyInflow(nodeID, label);
					_inflowIdMap.put(inflow, portID);
				}
				
				long packetCount = inflow.getEventCount();
				_writableTrace.insertPortEvent(portID, packet.getID(), null, "r", packetCount, _getCurrentTimestamp());
				_writableTrace.updatePortPacketCount(portID, packetCount);
			
			}
		}
	}

	public void recordPacketCreated(Packet packet, Long stepID) throws Exception {
		
		synchronized(_writableTrace) {

			Long packetID = _writableTrace.insertPacket(null);	
			packet.setID(packetID);
			_unsentPackets.add(packet);
		
			if (packet instanceof MultiResourcePacket) {
				for (PublishedResource resource : ((MultiResourcePacket)packet).getResources()) {
					_recordResource(packetID, resource, resource.referencesData());
				}
			} else {
				PublishedResource resource = ((SingleResourcePacket)packet).getResource();
					_recordResource(packetID, resource, resource.referencesData());
			}

			String[] keys = packet.getMetadataKeys();
			Object[] values = packet.getMetadataValues();
			for (int i = 0; i < keys.length; i++) {
				try {
					long dataID = _writableTrace.insertData(values[i], false, null);
					_writableTrace.insertPacketMetadata(packetID, keys[i], dataID);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void _recordResource(long packetID, PublishedResource resource, boolean isReference) throws SQLException {

		Object value = resource.getData();
		String stringValue = null;
		if (value != null) {
			stringValue = value.toString();
		}
		
		try {
			long dataID = _writableTrace.insertData(stringValue, isReference, null);
			long resourceID = _writableTrace.insertResource(resource.getUri().toString(), dataID);
			_writableTrace.insertPacketResource(packetID, resourceID);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void recordPacketSent(Outflow outflow, Packet packet, Long stepID) throws Exception {
		
		WorkflowNode node = outflow.getNode();
		Long nodeID	= _getNodeID(node);
		String label = outflow.getLabel();

		if (packet != AbstractWorkflowNode.EndOfStreamPacket) {
			
			synchronized(_writableTrace) {
	
				Long portID	= _outflowIdMap.get(outflow);
				if (portID == null) {
					portID = _writableTrace.identifyOutflow(nodeID, label);
					_outflowIdMap.put(outflow, portID);
				}
				
				if (stepID == null) {
					stepID	= _currentStepIdMap.get(nodeID);
				}
		
				long packetCount = outflow.getEventCount();
				long portEventID = _writableTrace.insertPortEvent(portID, packet.getID(), 
						stepID, "w", packetCount, _getCurrentTimestamp());
				_writableTrace.updatePortPacketCount(portID, packetCount);
				
				if (_unsentPackets.contains(packet)) {
					_writableTrace.updatePacketOriginEvent(packet.getID(), portEventID);
					_unsentPackets.remove(packet);
				}
			}
		}
		
		if (packet instanceof MultiResourcePacket) {
			for (PublishedResource resource : ((MultiResourcePacket)packet).getResources()) {
				_recordResourceWriteEvent(node, label, resource, packet.getProtocol());
			}
		} else {
			PublishedResource resource = ((SingleResourcePacket)packet).getResource();
			_recordResourceWriteEvent(node, label, resource, packet.getProtocol());
		}
	}
	
	private Timestamp _getCurrentTimestamp() {
		Date date = new Date();	
		return new Timestamp(date.getTime());
	}
	
	private void _recordResourceWriteEvent(WorkflowNode node, String label, PublishedResource resource, Protocol protocol) {

		if (resource != AbstractWorkflowNode.EndOfStreamResource) {
			
			if (!node.isHidden()) {
				
				if (_dataStore != null) {
					String key = resource.getUri().getPath();
					Object value = resource.getData();
					_dataStore.put(key, value);
				}
				
				_appendToProductsIndex(resource);
			}
		}
	}
	
	private synchronized void _appendToProductsIndex(PublishedResource resource) {
		
		Uri uri = resource.getUri();
		String path = uri.getPath();
		String scheme = uri.getScheme();
			
		String payload = null;		
				
		if (scheme != null && scheme.equals("file")) {
			
			payload = "!file " + path;
			_workflowContext.getMetaDataManager().writeToProductsFile(path + ": " + payload + EOL);	
			
		} else {
			
			if (_yamlDumperOptions == null) {
				_yamlDumperOptions = new DumperOptions();
				_yamlDumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				_yamlDumperOptions.setWidth(160);
			}
			
			Object data = resource.getData();
			
			if (data != null) {
				Map<String,String> map = new HashMap<String,String>();
				map.put(path, data.toString());
				Yaml yaml = new Yaml(_yamlDumperOptions);
				_workflowContext.getMetaDataManager().writeToProductsFile(yaml.dump(map));
			}
		}
	}
	
	private long _getNodeID(WorkflowNode node) throws Exception {

		synchronized(_writableTrace) {
			
			Long nodeID	= _nodeIdMap.get(node);
			
			if (nodeID == null) {
				nodeID = _writableTrace.identifyNode(node.getQualifiedWorkflowNodeName());
				_nodeIdMap.put(node, nodeID);
			}

			return nodeID;	
		}
	}
	
	@Override
	public void recordWorkflowRunStarted() throws Exception {

		synchronized(_writableTrace) {

			// get the ID of the top workflow node
			Long topNodeID = _writableTrace.identifyTopNode();
			
			Long lastStepCount = _writableTrace.getStepCountForNode(topNodeID);
			Long newStepCount = lastStepCount + 1;
			
			// record the step of the top node
			_writableTrace.updateNodeStepCount(topNodeID, newStepCount);
			Long stepID = _writableTrace.insertStep(topNodeID, null, newStepCount, 0L, _getCurrentTimestamp(), null);
			_currentStepIdMap.put(topNodeID, stepID);

		}
	}
	
	public void recordWorkflowRunCompleted() throws Exception {
		
		synchronized(_writableTrace) {

			// get the ID of the top workflow node
			Long topNodeID = _writableTrace.identifyTopNode();

			long stepID = _currentStepIdMap.get(topNodeID);
			_writableTrace.updateStepEnd(stepID, _getCurrentTimestamp());
		}
	}
		
	public long recordStepStarted(WorkflowNode node) throws Exception {
		
		synchronized(_writableTrace) {
			
			Long nodeID	= _getNodeID(node);
			
			Long parentStepID = null;
			Long parentNodeID = _writableTrace.getNodeParentID(nodeID);			
			if (parentNodeID != null) {
				parentStepID = _currentStepIdMap.get(parentNodeID);
			}

			// get a lock on the trace database
			_writableTrace.startTransaction();
			
			// update the step count of the node
			Long lastStepCount = _writableTrace.getStepCountForNode(nodeID);
			Long newStepCount = lastStepCount + 1;
			_writableTrace.updateNodeStepCount(nodeID, newStepCount);

			// record the step in the trace
			Long stepID = _writableTrace.insertStep(nodeID, parentStepID, newStepCount, 0L, _getCurrentTimestamp(), null);
			
			// get the data read events on this node not yet associated with a step
			ResultSet resultSet = _writableTrace.getUnassociatedReadEventsForNode(nodeID);
			
			// associate each of these read events with the new step
			while (resultSet.next()) {
				Long portEventID = resultSet.getLong("PortEventID");
				_writableTrace.updatePortEventStepID(portEventID, stepID);
			}
			
			// unlock the trace database
			_writableTrace.endTransaction();

			// associate the new step record with the node
			_currentStepIdMap.put(nodeID, stepID);
			
			// return the unique ID of this step
			return stepID;
		}
	}
	
	
	public void recordStepCompleted(WorkflowNode node) throws Exception {
		
		synchronized(_writableTrace) {
			long nodeID = _getNodeID(node);
			long stepID = _currentStepIdMap.get(nodeID);
			_writableTrace.updateStepEnd(stepID, _getCurrentTimestamp());
		}
	}
	
	public void recordState(WorkflowNode node, String label, Packet packet) {}

	public void setWritableTrace(WritableTrace writableTrace) {
		_writableTrace = writableTrace;
	}

	@Override
	public void recordWorkflowInputEvent(String inputName, Object value) throws Exception {
		Long dataID = _writableTrace.insertData(value, false, null);
		Long resourceID = _writableTrace.insertResource(null, dataID);
		Long packetID = _writableTrace.insertPacket(null);
		_writableTrace.insertPacketResource(packetID, resourceID);
		Long nodeID = _writableTrace.identifyTopNode();
		Long stepID = _currentStepIdMap.get(nodeID);
		Long stepCount = _writableTrace.getStepCountForNode(nodeID);
		Long portID = _writableTrace.identifyInflow(nodeID, inputName);
		Long originEventID = _writableTrace.insertPortEvent(portID, packetID, stepID, "r", stepCount, _getCurrentTimestamp());
		_writableTrace.updatePortPacketCount(portID, stepCount);
		_writableTrace.updatePacketOriginEvent(packetID, originEventID);
	}

	@Override
	public void recordWorkflowOutputEvent(String inputName, Object value) throws Exception {
		Long dataID = _writableTrace.insertData(value, false, null);
		Long resourceID = _writableTrace.insertResource(null, dataID);
		Long packetID = _writableTrace.insertPacket(null);
		_writableTrace.insertPacketResource(packetID, resourceID);
		Long nodeID = _writableTrace.identifyTopNode();
		Long stepID = _currentStepIdMap.get(nodeID);
		Long stepCount = _writableTrace.getStepCountForNode(nodeID);
		Long portID = _writableTrace.identifyOutflow(nodeID, inputName);
		Long originEventID = _writableTrace.insertPortEvent(portID, packetID, stepID, "w", stepCount, _getCurrentTimestamp());
		_writableTrace.updatePortPacketCount(portID, stepCount);
		_writableTrace.updatePacketOriginEvent(packetID, originEventID);
	}
}