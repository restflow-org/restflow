package org.restflow.metadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.restflow.WorkflowContext;
import org.restflow.actors.Actor;
import org.restflow.actors.Workflow;
import org.restflow.data.Inflow;
import org.restflow.data.Outflow;
import org.restflow.nodes.AbstractWorkflowNode;
import org.restflow.nodes.ActorWorkflowNode;
import org.restflow.nodes.BufferNode;
import org.restflow.nodes.InPortal;
import org.restflow.nodes.OutPortal;
import org.restflow.nodes.SourceNode;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.PortableDB;
import org.springframework.context.ApplicationContext;


public class WritableTrace extends Trace {
	
	private PreparedStatement _identifyInflowStatement			= null;
	private PreparedStatement _identifyNodeParentStatement		= null;
	private PreparedStatement _identifyOutflowStatement			= null;
	
	private PreparedStatement _insertChannelStatement 			= null;
	private PreparedStatement _insertActorStatement 			= null;
	private PreparedStatement _insertActorVariableStatement 	= null;
	private PreparedStatement _insertDataTypeStatement 			= null;
	private PreparedStatement _insertDataStatement 				= null;
	private PreparedStatement _insertDependencyRuleStatement 	= null;
	private PreparedStatement _insertNodeStatement 				= null;
	private PreparedStatement _insertNodeVariableStatement 		= null;
	private PreparedStatement _insertPacketMetadataStatement	= null;	
	private PreparedStatement _insertPacketResourceStatement	= null;	
	private PreparedStatement _insertPacketStatement 			= null;
	private PreparedStatement _insertPortEventStatement			= null;
	private PreparedStatement _insertPortStatement 				= null;
	private PreparedStatement _insertResourceStatement			= null;
	private PreparedStatement _insertUpdateStatement 			= null;	
	private PreparedStatement _insertStepStatement 				= null;	
	private PreparedStatement _updatePacketStatement 			= null;
	private PreparedStatement _updatePortPacketCountStatement	= null;
	private PreparedStatement _updateStepEndStatement			= null;
	private PreparedStatement _updateNodeStepCountStatement 	= null;
	private PreparedStatement _getNodeStepCountStatement		= null;
	private PreparedStatement _selectNullStepReadEventsStatement= null;
	private PreparedStatement _updatePortEventStepIDStatement 	= null;
	private PreparedStatement _identifyActorVariableStatement 	= null;
		
	private Long _inportalActorID;
	private Long _outportalActorID;
	private Long _bufferActorID;
	private Long _sourceActorID;
	private Trace _readOnlyTrace;
		
	static {
		try {
			Class.forName(jdbcDriverClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public WritableTrace(WorkflowContext context) throws Exception {
		this(createPrivateVolatileDatabase());
		createTraceDBTables(context, _traceDBConnection);
	}

	public WritableTrace(WorkflowContext context, String metadataDirectory) throws Exception {
		this(createNewPersistentDatabase(metadataDirectory));
		createTraceDBTables(context, _traceDBConnection);
	}
	
	WritableTrace(Connection connection) throws SQLException {
		super(connection);
	}
	
	static Connection createPrivateVolatileDatabase() throws SQLException {
		
		return DriverManager.getConnection(
				privateVolatileDatabaseUrl 			+
				dbSerializableIsolationModeSuffix
		);
	}
	
	static Connection createNewPersistentDatabase(String metadataDirectory) throws SQLException {
		
		return DriverManager.getConnection(
				persistentDatabaseUrlPrefix 		+ 
				metadataDirectory 					+ 
				traceDBFileNameRoot 				+
				dbSerializableIsolationModeSuffix
		);
	}
	
	static Connection connectToExistingPersistentDatabase(String metadataDirectory) throws SQLException {
		
		return DriverManager.getConnection(
				persistentDatabaseUrlPrefix 		+
				metadataDirectory 					+
				traceDBFileNameRoot					+
				dbExistsSuffix 						+
				dbSerializableIsolationModeSuffix
		);
	}
	
	static int createTraceDBTables(ApplicationContext context, Connection connection) throws Exception {

		// read the table creation sql script from a resource in the context
		String sqlScript = IOUtils.toString(
				context.getResource(tableCreationSqlFile).getInputStream(), 
				"UTF-8");
		
		// execute the sql script
		int statementCount = PortableDB.executeSqlScript(connection, sqlScript);
		
		// report the number of sql statements executed
		return statementCount;
	}
	
	public void close() throws SQLException {
		synchronized(_traceDBConnection) {
			_statement.close(); 
			_traceDBConnection.close();
		}
	}

	protected void finalize() throws Throwable {
	     
	     try {
	         close();
	     } catch(Exception e) {}
	  
	     finally {
	         super.finalize();
	     }
	 }
	
	public void startTransaction() throws SQLException {
		synchronized(_traceDBConnection) {
			_traceDBConnection.setAutoCommit(false);
		}
	}
	
	public void endTransaction() throws SQLException {
		synchronized(_traceDBConnection) {
			_traceDBConnection.commit();
			_traceDBConnection.setAutoCommit(true);
		}
	}
	
	public synchronized long insertNode(String nodeName, String localNodeName, Long parentNodeID, Long actorID, long stepCount, boolean isHidden, boolean hasChildren) throws SQLException {
		
		synchronized(_traceDBConnection) {

			long nodeID;
	
			PreparedStatement statement = _getInsertNodeStatement();
	
			statement.setString(							1, nodeName);
			statement.setString(							2, localNodeName);
			PortableDB.setNullableLongParameter(statement, 	3, parentNodeID);
			PortableDB.setNullableLongParameter(statement, 	4, actorID);
			PortableDB.setLongParameter(statement, 			5, stepCount);
			PortableDB.setBooleanParameter(statement, 		6, isHidden);
			PortableDB.setBooleanParameter(statement, 		7, hasChildren);
			
			statement.executeUpdate();
			nodeID = PortableDB.getGeneratedID(statement);
			
			return nodeID;
		}
	}
	
	
	public ResultSet selectNodeRow_WithName(String nodeName) throws SQLException {
		
		synchronized(_traceDBConnection) {
		
			PreparedStatement statement;
			
			String sql = "SELECT NodeId, NodeName, ParentNodeID, ActorID, StepCount " 	+
						 "FROM Node " 											+
						 "WHERE NodeName = ? ";
	
			statement = _traceDBConnection.prepareStatement(sql);
			statement.setString(	1, nodeName);
			 
			ResultSet rs = statement.executeQuery();
			 
			return rs;
		}
	}
	

	
	private PreparedStatement _getInsertNodeStatement() throws SQLException {
			
		if (_insertNodeStatement == null) {
			String sql = "INSERT INTO NODE " 										+
						 "(NodeName, LocalNodeName, ParentNodeID, ActorID, StepCount, IsHidden, HasChildren) " 	+ 
						 "VALUES (?, ?, ?, ?, ?, ?, ?)";
			_insertNodeStatement = _traceDBConnection.prepareStatement(sql);
		}
		
		return _insertNodeStatement;
	}
	

	
	public synchronized Long getStepCountForNode(Long nodeID) throws Exception {

		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getGetNodeStepCountStatement();
			PortableDB.setLongParameter(statement,	1, nodeID);
			ResultSet results = statement.executeQuery();
			results.next();
			
			return results.getLong(1);
		}
	}

	private PreparedStatement _getGetNodeStepCountStatement() throws SQLException {
		if (_getNodeStepCountStatement == null) {
			String sql = "SELECT StepCount " 		+
						 "FROM Node "				+
						 "WHERE NodeID = ?";
			_getNodeStepCountStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _getNodeStepCountStatement;
	}
	
	public void updateNodeStepCount(long nodeID, long stepCount) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getUpdateNodeStepCountStatement();
	
			PortableDB.setLongParameter(statement, 	1, stepCount);
			PortableDB.setLongParameter(statement, 	2, nodeID);
			statement.executeUpdate();
		}
	}
	
	private PreparedStatement _getUpdateNodeStepCountStatement() throws SQLException {
		if (_updateNodeStepCountStatement == null) {
			String sql = "UPDATE Node " 		+
						 "SET StepCount = ?"	+
						 "WHERE NodeID = ?";
			_updateNodeStepCountStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _updateNodeStepCountStatement;
	}


	public long insertInflow(String portName, Long nodeID, Long nodeVariableID, 
			String uriTemplate) throws SQLException {

		synchronized(_traceDBConnection) {	
			return _insertPort(portName, nodeID, nodeVariableID, "i", uriTemplate);
		}
	}

	public long insertOutflow(String portName, Long nodeID, Long nodeVariableID, 
			String uriTemplate) throws SQLException {

		synchronized(_traceDBConnection) {
			return _insertPort(portName, nodeID, nodeVariableID, "o", uriTemplate);
		}
	}

	
	private synchronized long _insertPort(String portName, Long nodeID, Long nodeVariableID, 
			String direction, String uriTemplate) throws SQLException {

		long portID;

		PreparedStatement statement = _getInsertPortStatement();

		statement.setString(							1, portName);
		PortableDB.setLongParameter(statement, 			2, nodeID);
		PortableDB.setNullableLongParameter(statement, 	3, nodeVariableID);
		statement.setString(							4, direction);
		statement.setString(							5, uriTemplate);
		
		statement.executeUpdate();
		portID = PortableDB.getGeneratedID(statement);

		
		return portID;
	}

	private PreparedStatement _getInsertPortStatement() throws SQLException {
		if (_insertPortStatement == null) {
			String sql = "INSERT INTO Port " 												+
						 "(PortName, NodeID, NodeVariableID, PortDirection, UriTemplate) " 	+
						 "VALUES (?, ?, ?, ?, ?)";
			_insertPortStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertPortStatement;
	}	
	
	public synchronized void insertChannel(long outflowID, long inflowID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getInsertChannelStatement();
	
			PortableDB.setLongParameter(statement, 	1, outflowID);
			PortableDB.setLongParameter(statement, 	2, inflowID);
			statement.executeUpdate();
		}
	}

	private PreparedStatement _getInsertChannelStatement() throws SQLException {
		if (_insertChannelStatement == null) {
			String sql = "INSERT INTO Channel " 	+
						 "(OutPortID, InPortID) " 	+
						 "VALUES (?, ?)";
			_insertChannelStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertChannelStatement;
	}	
	

	
	public synchronized long insertStep(long nodeID, Long parentStepID, Long stepNumber, long updateCount, 
			Timestamp startTime, Timestamp endTime) throws SQLException {
		
		synchronized(_traceDBConnection) {

			long stepID;
	
			PreparedStatement statement = _getInsertStepStatement();
	
			PortableDB.setLongParameter(statement, 			1, nodeID);
			PortableDB.setNullableLongParameter(statement, 	2, parentStepID);
			PortableDB.setNullableLongParameter(statement, 	3, stepNumber);
			PortableDB.setLongParameter(statement, 			4, updateCount);
			PortableDB.setNullableTimeStamp(statement, 		5, startTime);
			PortableDB.setNullableTimeStamp(statement, 		6, endTime);
			statement.executeUpdate();
	
			stepID = PortableDB.getGeneratedID(statement);
			
			return stepID;
		}
	}
	
	private PreparedStatement _getInsertStepStatement() throws SQLException {
		if (_insertStepStatement == null) {
			String sql = "INSERT INTO Step " 	+
						 "(NodeID, ParentStepID, StepNumber, UpdateCount, StartTime, EndTime) " 	+
						 "VALUES (?, ?, ?, ?, ?, ?)";
			_insertStepStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertStepStatement;
	}
	
//	public void updateStepStart(long stepID, Long parentStepID,
//			long StepNumber, Timestamp timestamp) throws SQLException {
//		
//		synchronized(_traceDBConnection) {
//			PreparedStatement statement = _getUpdateStepStartStatement();
//	
//			PortableDB.setNullableLongParameter(statement, 	1, parentStepID);
//			PortableDB.setTimeStamp(statement, 		2, timestamp);
//			PortableDB.setLongParameter(statement, 	3, StepNumber);
//			PortableDB.setLongParameter(statement, 	4, stepID);
//			statement.executeUpdate();
//		}
//	}
//	
//	
//	
//	private PreparedStatement _getUpdateStepStartStatement() throws SQLException {
//		if (_updateStepStartStatement == null) {
//			String sql = "UPDATE Step " 			+
//						 "SET ParentStepID = ?,"	+
//						 "    StartTime    = ?," 	+
//						 "    StepNumber   = ? "	+
//						 "WHERE StepID = ?";
//			_updateStepStartStatement = _traceDBConnection.prepareStatement(sql);
//		}
//		return _updateStepStartStatement;
//	}

	public void updateStepEnd(long stepID, Timestamp timestamp) throws SQLException {
		
		synchronized(_traceDBConnection) {

			PreparedStatement statement = _getUpdateStepEndStatement();
	
			PortableDB.setTimeStamp(statement, 		1, timestamp);
			PortableDB.setLongParameter(statement, 	2, stepID);
			statement.executeUpdate();
		}
	}

	
	private PreparedStatement _getUpdateStepEndStatement() throws SQLException {
		if (_updateStepEndStatement == null) {
			String sql = "UPDATE Step " 			+
						 "SET EndTime = ? " 	+
						 "WHERE StepID = ?";
			_updateStepEndStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _updateStepEndStatement;
	}	

	public synchronized long insertPacket(Long originEventID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			long packetID;
	
			PreparedStatement statement = _getInsertPacketStatement();
	
			PortableDB.setNullableLongParameter(statement, 	1, originEventID);
			statement.executeUpdate();
	
			packetID = PortableDB.getGeneratedID(statement);
			
			return packetID;
		}
	}
	
	private PreparedStatement _getInsertPacketStatement() throws SQLException {
		if (_insertPacketStatement == null) {
			String sql = "INSERT INTO Packet " 	+
						 "(OriginEventID) " 	+
						 "VALUES (?)";
			_insertPacketStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertPacketStatement;
	}
	
	public synchronized void updatePacketOriginEvent(long packetID, long originEventID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getUpdatePacketStatement();
	
			PortableDB.setLongParameter(statement, 			1, originEventID);
			PortableDB.setNullableLongParameter(statement, 	2, packetID);
			statement.executeUpdate();
		}
	}
	
	private PreparedStatement _getUpdatePacketStatement() throws SQLException {
		if (_updatePacketStatement == null) {
			String sql = "UPDATE Packet " 			+
						 "SET OriginEventID = ? " 	+
						 "WHERE PacketID = ?";
			_updatePacketStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _updatePacketStatement;
	}
	
	public synchronized long insertPortEvent(long portID, long packetID, Long stepID, String eventClass, 
			long eventNumber, Timestamp eventTime) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getInsertPortEventStatement();
	
			PortableDB.setLongParameter			(	statement, 	1,	portID		);
			PortableDB.setLongParameter			(	statement,	2, 	packetID	);
			PortableDB.setNullableLongParameter	(	statement,	3, 	stepID		);
			PortableDB.setStringParameter		(	statement, 	4, 	eventClass	);
			PortableDB.setLongParameter			(	statement, 	5, 	eventNumber	);
			PortableDB.setNullableTimeStamp		(	statement, 	6, 	eventTime	);

			statement.executeUpdate();
	
			long portEventID = PortableDB.getGeneratedID(statement);
			
			return portEventID;
		}
	}
	
	private PreparedStatement _getInsertPortEventStatement() throws SQLException {
		if (_insertPortEventStatement == null) {
			String sql = "INSERT INTO PortEvent " 	+
						 "(PortID, PacketID, StepID, EventClass, EventNumber, EventTime) " 	+
						 "VALUES (?, ?, ?, ?, ?, ?)";
			_insertPortEventStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertPortEventStatement;
	}
	
	public void updatePortPacketCount(long portID, long packetCount) throws SQLException {
		
		synchronized(_traceDBConnection) {

			PreparedStatement statement = _getUpdatePortPacketCountStatement();

			PortableDB.setLongParameter(statement, 	1, packetCount);
			PortableDB.setLongParameter(statement, 	2, portID);
			statement.executeUpdate();
		}
	}
	
	private PreparedStatement _getUpdatePortPacketCountStatement() throws SQLException {
		if (_updatePortPacketCountStatement == null) {
			String sql = "UPDATE Port " 			+
						 "SET PacketCount = ? " 	+
						 "WHERE PortID = ?";
			_updatePortPacketCountStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _updatePortPacketCountStatement;
	}

	
	public synchronized long insertResource(String uri, Long dataID) throws SQLException {
		
		synchronized(_traceDBConnection) {

			long resourceID;

			PreparedStatement statement = _getInsertResourceStatement();
	
			PortableDB.setStringParameter(statement, 		1, uri);
			PortableDB.setNullableLongParameter(statement,	2, dataID);
			statement.executeUpdate();
	
			resourceID = PortableDB.getGeneratedID(statement);
			
			return resourceID;
		}
	}

	private PreparedStatement _getInsertResourceStatement() throws SQLException {
		if (_insertResourceStatement == null) {
			String sql = "INSERT INTO Resource " 	+
						 "(URI, DataID) " 	+
						 "VALUES (?, ?)";
			_insertResourceStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertResourceStatement;
	}
	

	public synchronized long insertData(Object value, boolean isReference, Long dataTypeID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			long dataID;
	
			PreparedStatement statement = _getInsertDataStatement();
	
			PortableDB.setObjectParameter(statement, 		1, value);
			PortableDB.setBooleanParameter(statement, 		2, isReference);
			PortableDB.setNullableLongParameter(statement,	3, dataTypeID);
			statement.executeUpdate();
	
			dataID = PortableDB.getGeneratedID(statement);
			
			return dataID;
		}
	}
	
	private PreparedStatement _getInsertDataStatement() throws SQLException {
		if (_insertDataStatement == null) {
			String sql = "INSERT INTO Data " 					+
						 "(Value, IsReference, DataTypeID) " 	+
						 "VALUES (?, ?, ?)";
			_insertDataStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertDataStatement;
	}

	public synchronized void insertPacketResource(long packetID, long resourceID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getInsertPacketResourceStatement();
	
			PortableDB.setLongParameter(statement,		1, packetID);
			PortableDB.setLongParameter(statement,		2, resourceID);
			statement.executeUpdate();
		}
	}
	
	
	private PreparedStatement _getInsertPacketResourceStatement() throws SQLException {
		if (_insertPacketResourceStatement == null) {
			String sql = "INSERT INTO PacketResource " 					+
						 "(PacketID, ResourceID) " 	+
						 "VALUES (?, ?)";
			_insertPacketResourceStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertPacketResourceStatement;
	}
	
	public synchronized long insertPacketMetadata(long packetID, String key, long dataID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			long metadataID;
	
			PreparedStatement statement = _getInsertPacketMetadataStatement();
	
			PortableDB.setLongParameter(statement, 		1, packetID);
			PortableDB.setStringParameter(statement, 	2, key);
			PortableDB.setLongParameter(statement,		3, dataID);
			statement.executeUpdate();
	
			metadataID = PortableDB.getGeneratedID(statement);
			
			return metadataID;
		}
	}
	
	private PreparedStatement _getInsertPacketMetadataStatement() throws SQLException {
		if (_insertPacketMetadataStatement == null) {
			String sql = "INSERT INTO PacketMetadata " 					+
						 "(PacketID, Key, DataID) " 	+
						 "VALUES (?, ?, ?)";
			_insertPacketMetadataStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertPacketMetadataStatement;
	}


	public synchronized long insertActor(String actorName) throws SQLException {
		
		synchronized(_traceDBConnection) {	

			long actorID;
	
			PreparedStatement statement = _getInsertActorStatement();
	
			PortableDB.setStringParameter(statement, 	1, actorName);
			statement.executeUpdate();
	
			actorID = PortableDB.getGeneratedID(statement);
			
			return actorID;
		}
	}
	
	private PreparedStatement _getInsertActorStatement() throws SQLException {
		if (_insertActorStatement == null) {
			String sql = "INSERT INTO Actor " 					+
						 "(ActorName) " 	+
						 "VALUES (?)";
			_insertActorStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertActorStatement;
	}
	
	public synchronized long insertActorVariable(long actorID, String variableName,
			String variableClass, Long dataTypeID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			long actorVariableID;
	
			PreparedStatement statement = _getInsertActorVariableStatement();
	
			PortableDB.setLongParameter(statement, 			1, actorID);
			PortableDB.setStringParameter(statement, 		2, variableName);
			PortableDB.setStringParameter(statement, 		3, variableClass);
			PortableDB.setNullableLongParameter(statement, 	4, dataTypeID);
			statement.executeUpdate();
	
			actorVariableID = PortableDB.getGeneratedID(statement);
			
			return actorVariableID;
		}
	}

	private PreparedStatement _getInsertActorVariableStatement() throws SQLException {
		if (_insertActorVariableStatement == null) {
			String sql = "INSERT INTO ActorVariable " 							+
						 "(ActorID, VariableName, VariableClass, DataTypeID) " 	+
						 "VALUES (?, ?, ?, ?)";
			_insertActorVariableStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertActorVariableStatement;
	}

	public Long identifyActorVariable(Long actorID, String label, String direction) throws Exception {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getIdentifyActorVariableStatement();
			PortableDB.setLongParameter(statement,		1, actorID);
			PortableDB.setStringParameter(statement,	2, label);
			PortableDB.setStringParameter(statement,	3, direction);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return results.getLong(1);
			} else {
				return null;
			}
		}		
	}

	private PreparedStatement _getIdentifyActorVariableStatement() throws SQLException {
		if (_identifyActorVariableStatement == null) {
			String sql = "SELECT VariableID  " 							+
						 "FROM ActorVariable " 							+
						 "WHERE ActorID = ? AND  VariableName = ?  AND VariableClass = ?";
			_identifyActorVariableStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _identifyActorVariableStatement;
	}

	
	public synchronized long insertNodeVariable(long nodeID, Long actorVariableID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			long nodeVariableID;
	
			PreparedStatement statement = _getInsertNodeVariableStatement();
	
			PortableDB.setLongParameter(statement, 		1, nodeID);
			PortableDB.setLongParameter(statement, 		2, actorVariableID);
			statement.executeUpdate();
	
			nodeVariableID = PortableDB.getGeneratedID(statement);
			
			return nodeVariableID;
		}
	}

	private PreparedStatement _getInsertNodeVariableStatement() throws SQLException {
		if (_insertNodeVariableStatement == null) {
			String sql = "INSERT INTO NodeVariable " 							+
						 "(NodeID, ActorVariableID) " 	+
						 "VALUES (?, ?)";
			_insertNodeVariableStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertNodeVariableStatement;
	}

	
	public synchronized long insertUpdate(long nodeVariableID, long dataID, long stepID, long updateNumber) throws SQLException {

		synchronized(_traceDBConnection) {

			long updateID;
	
			PreparedStatement statement = _getInsertUpdateStatement();
	
			PortableDB.setLongParameter(statement, 		1, nodeVariableID);
			PortableDB.setLongParameter(statement, 		2, dataID);
			PortableDB.setLongParameter(statement, 		3, stepID);
			PortableDB.setLongParameter(statement, 		4, updateNumber);
			statement.executeUpdate();
	
			updateID = PortableDB.getGeneratedID(statement);
			
			return updateID;
		}
	}

	private PreparedStatement _getInsertUpdateStatement() throws SQLException {
		if (_insertUpdateStatement == null) {
			String sql = "INSERT INTO Update " 									+
						 "(NodeVariableID, DataID, StepID, UpdateNumber) " 	+
						 "VALUES (?, ?, ?, ?)";
			_insertUpdateStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertUpdateStatement;
	}

	public synchronized long insertDataType(String typeName) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			long dataTypeID;
	
			PreparedStatement statement = _getInsertDataTypeStatement();
	
			PortableDB.setStringParameter(statement,	1, typeName);
			statement.executeUpdate();
	
			dataTypeID = PortableDB.getGeneratedID(statement);
			
			return dataTypeID;
		}
	}
	
	private PreparedStatement _getInsertDataTypeStatement() throws SQLException {
		if (_insertDataTypeStatement == null) {
			String sql = "INSERT INTO DataType "	+
						 "(TypeName) " 				+
						 "VALUES (?)";
			_insertDataTypeStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertDataTypeStatement;
	}
	
	public synchronized long insertDependencyRule(long actorID, long sourceVarID, 
			long targetVarID, String dependencyClass) throws SQLException {

		synchronized(_traceDBConnection) {
			
			long dependencyRuleID;
		
			PreparedStatement statement = _getInsertDependencyRuleStatement();
		
			PortableDB.setLongParameter(statement, 			1, actorID);
			PortableDB.setLongParameter(statement, 			2, sourceVarID);
			PortableDB.setLongParameter(statement, 			3, targetVarID);
			PortableDB.setStringParameter(statement, 		4, dependencyClass);
			statement.executeUpdate();
		
			dependencyRuleID = PortableDB.getGeneratedID(statement);
			
			return dependencyRuleID;
		}
	}

	private PreparedStatement _getInsertDependencyRuleStatement() throws SQLException {
		if (_insertDependencyRuleStatement == null) {
			String sql = "INSERT INTO DependencyRule " 							+
						 "(ActorID, SourceVarID, TargetVarID, DependencyClass) " 	+
						 "VALUES (?, ?, ?, ?)";
			_insertDependencyRuleStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _insertDependencyRuleStatement;
	}
	
	public void storeWorkflowGraph(Workflow workflow, Long parentNodeID) throws Exception {
		
		synchronized(_traceDBConnection) {
			
			// store information about the top-level workflow and an associated virtual node
			if (parentNodeID ==  null) {
				parentNodeID = storeTopNode(workflow);
			}

			for (WorkflowNode node: workflow.getNodes()) {
				storeWorkflowNode(node, parentNodeID);
			}
			
			for (Map.Entry<Inflow,List<Outflow>> entry : workflow.getInflowToOutflowsMap().entrySet()) {
				Inflow inflow = entry.getKey();
				Long inflowNodeID = identifyNode(inflow.getNode().getQualifiedName());
				Long inflowID = identifyInflow(inflowNodeID, inflow.getLabel());
				for (Outflow outflow : entry.getValue()) {
					Long outflowNodeID = identifyNode(outflow.getNode().getQualifiedName());
					Long outflowID = identifyOutflow(outflowNodeID, outflow.getLabel());
					insertChannel(outflowID, inflowID);
				}
			}
		}
	}

	private Long storeTopNode(Workflow workflow) throws SQLException {

		String workflowName = workflow.getName();
		if (workflowName == null) {
			workflowName = "Workflow";
		}
		Long topWorkflowID = insertActor(workflowName);
		long topNodeID = insertNode(workflowName, workflowName, null, topWorkflowID, 0L, false, true);
		_nodeParentMap.put(topNodeID, null);
		
		for (String inputName : workflow.getInputNames()) {
			long actorVariableID = insertActorVariable(topWorkflowID, inputName, "i", null);
			long nodeVariableID = insertNodeVariable(topNodeID, actorVariableID);
			insertInflow(inputName, topNodeID, nodeVariableID, null);
		}

		for (String outputName : workflow.getOutputNames()) {
			long actorVariableID = insertActorVariable(topWorkflowID, outputName, "o", null);
			long nodeVariableID = insertNodeVariable(topNodeID, actorVariableID);
			insertOutflow(outputName, topNodeID, nodeVariableID, null);
		}
		
		return topNodeID;
	}
	
	public void storeWorkflowNode(WorkflowNode node, Long parentNodeID) throws Exception {
		
		synchronized(_traceDBConnection) {

			Object genericActor = new Object();
		
			Object actor = null;
			Long actorID = null;
			
			if (node instanceof ActorWorkflowNode) {
			
				actor = ((ActorWorkflowNode)node).getActor();
				actorID = insertActor(((Actor)actor).getName());
	
			} else if (node instanceof InPortal){
	
//				if (_inportalActorID == null) {
					_inportalActorID = insertActor("InPortal");
//				}
				actor = genericActor;
				actorID = _inportalActorID;
	
			} else if (node instanceof OutPortal){
	
	//			if (_outportalActorID == null) {
					_outportalActorID = insertActor("OutPortal");
	//			}
				actor = genericActor;
				actorID = _outportalActorID;
				
			} else if (node instanceof BufferNode){
				
				if (_bufferActorID == null) {
					_bufferActorID = insertActor("Buffer");
				}
				actor = genericActor;
				actorID = _bufferActorID;
				
			} else if (node instanceof SourceNode){
				
				if (_sourceActorID == null) {
					_sourceActorID = insertActor("Source");
				}
				actor = genericActor;
				actorID = _sourceActorID;
				
			} else {
				actor = genericActor;
				actorID = insertActor(null);
			}
			
			boolean hasChildren = false;
			if (node instanceof ActorWorkflowNode) {
				Actor a = ((ActorWorkflowNode)node).getActor();
				hasChildren = (a instanceof Workflow);
			}
			
			long nodeID = insertNode(node.getQualifiedName(), node.getName(), parentNodeID, actorID, 0L, node.isHidden(), hasChildren);
			_nodeParentMap.put(nodeID, parentNodeID);
			
			for (Inflow inflow : node.getNodeInflows()) {
				
				String inflowLabel = inflow.getLabel();
				
				Long actorVariableID = identifyActorVariable(actorID, inflowLabel, "i");
				if (actorVariableID == null) {
					actorVariableID = insertActorVariable(actorID, inflowLabel, "i", null);
				}
				
				long nodeVariableID = insertNodeVariable(nodeID, actorVariableID);
				insertInflow(inflowLabel, nodeID, nodeVariableID, inflow.getUriTemplate().toString());
			}
	
			for (Outflow outflow: node.getOutflows().values()) {
				String outflowLabel = outflow.getLabel();

				Long actorVariableID = identifyActorVariable(actorID, outflowLabel, "o");
				if (actorVariableID == null) {
					actorVariableID = insertActorVariable(actorID, outflowLabel, "o", null);
				}

				long nodeVariableID = insertNodeVariable(nodeID, actorVariableID);
				insertOutflow(outflowLabel, nodeID, nodeVariableID, outflow.getUriTemplate().toString());
			}
			
			if (actor instanceof Workflow) {
				storeWorkflowGraph((Workflow)actor, nodeID);
			}
		}
	}

	public synchronized Long identifyNodeParent(long nodeID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getIdentifyNodeParentStatement();
			PortableDB.setLongParameter(statement,	1, nodeID);
			ResultSet results = statement.executeQuery();
			boolean hasResult = results.next();
			if (hasResult) {
				return results.getLong(1);
			} else {
				return null;
			}
		}
	}

	private PreparedStatement _getIdentifyNodeParentStatement() throws SQLException {
		if (_identifyNodeParentStatement == null) {
			String sql = "SELECT Parent.NodeID " 									+
						 "From Node as Parent LEFT OUTER JOIN Node as Child " 					+
						 "ON Parent.NodeID = Child.ParentNodeID WHERE Child.NodeID = ?";
			_identifyNodeParentStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _identifyNodeParentStatement;
	}

	public synchronized Long identifyInflow(long nodeID, String inflowLabel) throws SQLException {

		synchronized(_traceDBConnection) {

			PreparedStatement statement = _getIdentifyInflowStatement();
			PortableDB.setStringParameter(statement,	1, inflowLabel);
			PortableDB.setLongParameter(statement, 		2, nodeID);
			ResultSet results = statement.executeQuery();
			results.next();
		
			return results.getLong(1);
		}
	}

	public Long identifyOutflow(long nodeID, String outflowLabel) throws SQLException {

		synchronized(_traceDBConnection) {			

			PreparedStatement statement = _getIdentifyOutflowStatement();
			PortableDB.setStringParameter(statement,	1, outflowLabel);
			PortableDB.setLongParameter(statement, 		2, nodeID);
			ResultSet results = statement.executeQuery();
			results.next();

			return results.getLong(1);
		}
	}
	
	private PreparedStatement _getIdentifyInflowStatement() throws SQLException {
		if (_identifyInflowStatement == null) {
			String sql = "SELECT PortID " 									+
						 "From Port " 										+
						 "WHERE PortName = ? AND NodeID = ? AND PortDirection = 'i'";
			_identifyInflowStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _identifyInflowStatement;
	}
	
	private PreparedStatement _getIdentifyOutflowStatement() throws SQLException {
		if (_identifyOutflowStatement == null) {
			String sql = "SELECT PortID " 									+
						 "From Port " 										+
						 "WHERE PortName = ? AND NodeID = ? AND PortDirection = 'o'";
			_identifyOutflowStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _identifyOutflowStatement;
	}

	public Trace getReadOnlyTrace() throws SQLException {
		synchronized(_traceDBConnection) {
			if (_readOnlyTrace == null) {
				_readOnlyTrace = new Trace(_traceDBConnection);
			}
			return _readOnlyTrace;
		}
	}

	public ResultSet getUnassociatedReadEventsForNode(Long nodeID) throws SQLException {

		synchronized(_traceDBConnection) {			
			PreparedStatement statement = _getSelectNullStepReadEventsStatement();
			PortableDB.setLongParameter(statement, 1, nodeID);
			ResultSet results = statement.executeQuery();
			return results;
		}
	}

	private PreparedStatement _getSelectNullStepReadEventsStatement() throws SQLException {
		if (_selectNullStepReadEventsStatement == null) {
			String sql = "SELECT PortEventID " 											+
						 "From PortEvent JOIN PORT ON PortEvent.PortID = Port.PortID " 	+
						 "WHERE NodeID = ? AND PortDirection = 'i' AND StepID IS NULL";
			_selectNullStepReadEventsStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _selectNullStepReadEventsStatement;
	}

	
	public void updatePortEventStepID(Long portEventID, Long stepID) throws SQLException {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getUpdatePortEventStepIDStatement();
	
			PortableDB.setLongParameter(statement, 	1, stepID);
			PortableDB.setLongParameter(statement, 	2, portEventID);
			statement.executeUpdate();
		}
	}
	
	private PreparedStatement _getUpdatePortEventStepIDStatement() throws SQLException {
		if (_updatePortEventStepIDStatement == null) {
			String sql = "UPDATE PortEvent " 		+
						 "SET StepID = ? "	+
						 "WHERE PortEventID = ?";
			_updatePortEventStepIDStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _updatePortEventStepIDStatement;
	}

}
