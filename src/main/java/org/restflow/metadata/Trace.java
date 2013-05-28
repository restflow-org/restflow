package org.restflow.metadata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.restflow.util.PortableDB;
import org.restflow.util.PortableIO;


public class Trace {

	protected Connection 	_traceDBConnection;
	protected Statement 	_statement;
	protected Map<Long,Long> _nodeParentMap;
	
	protected static String jdbcDriverClassName 				= "org.h2.Driver";
	protected static String privateVolatileDatabaseUrl 			= "jdbc:h2:mem:";
	protected static String persistentDatabaseUrlPrefix			= "jdbc:h2:";
	protected static String traceDBFileNameRoot					= "tracedb";
	protected static String dbExistsSuffix						= ";IFEXISTS=TRUE";
	protected static String dbSerializableIsolationModeSuffix	= ";LOCK_MODE=1";
	protected static String tableCreationSqlFile 				= "classpath:/org/restflow/metadata/tracedb/createtables.sql";

	private PreparedStatement _identifyNodeStatement			= null;
	private PreparedStatement _identifyTopNodeStatement			= null; 

	public Trace(String directory) throws SQLException {
		
		this(DriverManager.getConnection(
				persistentDatabaseUrlPrefix 		+
				directory 							+
				traceDBFileNameRoot					+
				dbExistsSuffix 						+
				dbSerializableIsolationModeSuffix
		));
	}
	
	protected Trace(Connection traceDBConnection) throws SQLException {	
		_nodeParentMap	= new HashMap<Long,Long>();
		_traceDBConnection = traceDBConnection;
		synchronized(_traceDBConnection) {
			_statement = _traceDBConnection.createStatement();
		}
	}

	public Long getNodeParentID(Long nodeID) {
		return _nodeParentMap.get(nodeID);
	}
	
	public int getRowCountForTable(String tableName) throws SQLException {
		synchronized(_traceDBConnection) {
			return PortableDB.getRowCountForTable(_traceDBConnection, tableName);
		}
	}
	
	public String getWorkflowNodesProlog() throws SQLException {

		synchronized(_traceDBConnection) {

			StringBuffer prologFacts = new StringBuffer();
	
			ResultSet resultSet = _statement.executeQuery(
										"SELECT NodeName " 		+
										"FROM Node " 			+
						 			 	"ORDER BY NodeName");
			
			while (resultSet.next()) {
				prologFacts.append(
						"rf_node(['" 												+ 
						resultSet.getString("NodeName").replaceAll("\\.", "','") 	+ 
						"'])." 														+ 
						PortableIO.EOL 
				);
			}
			
			return prologFacts.toString();
		}
	}
	
	public String getWorkflowPortsProlog() throws SQLException {
		
		synchronized(_traceDBConnection) {
			StringBuffer prologFacts = new StringBuffer();
			
			ResultSet resultSet = _statement.executeQuery(
										"SELECT NodeName, PortName, PortDirection " 			+
										"FROM Port JOIN Node ON Port.NodeID = Node.NodeID " 	+
										"ORDER BY NodeName, PortName");
			
			while (resultSet.next()) {
				prologFacts.append(
						"rf_port('" 															+ 
						resultSet.getString("NodeName") 										+ 
						"','" 																	+ 
						resultSet.getString("PortName") 										+ 
						"'," 																	+
						((resultSet.getString("PortDirection").equals("i")) ? "in" : "out") 	+
						")."																	+ 
						PortableIO.EOL
				);
			}
				
			return prologFacts.toString();
		}
	}
	
	public String getWorkflowChannelsProlog() throws SQLException {

		synchronized(_traceDBConnection) {
			StringBuffer prologFacts = new StringBuffer();
	
			ResultSet resultSet = _statement.executeQuery(
					"SELECT SourceNode.NodeName AS SourceNodeName, SourcePort.PortName AS SourcePortName, "		+
					"       SinkNode.NodeName AS SinkNodeName, SinkPort.PortName AS SinkPortName "  			+
					"FROM Node AS SourceNode " 																	+
					"    JOIN Port AS SourcePort ON SourceNode.NodeID = SourcePort.NodeID " 					+
					"    JOIN Channel ON SourcePort.PortID = Channel.OutPortID " 								+
					"    JOIN Port As SinkPort ON Channel.InPortID = SinkPort.PortID "							+
					"    JOIN Node As SinkNode ON SinkPort.NodeID = SinkNode.NodeID "							+
					"ORDER BY SourceNode.NodeName, SourcePort.PortName, SinkNode.NodeName, SinkPort.PortName");
			
			int channelCount = 0;
			while (resultSet.next()) {
				
				String edgeName = "e" + ++channelCount;
				
				prologFacts.append(
						"rf_link('" 								+ 
						resultSet.getString("SourceNodeName") 		+ 
						"','" 										+ 
						resultSet.getString("SourcePortName")		+
						"'," 										+
						edgeName 									+
						")."										+ 
						PortableIO.EOL
				);
				
				prologFacts.append(
						"rf_link('" 								+ 
						resultSet.getString("SinkNodeName")			+ 
						"','" 										+ 
						resultSet.getString("SinkPortName") 		+ 
						"'," 										+
						edgeName 									+
						")."										+ 
						PortableIO.EOL
				);
			}
	
			return prologFacts.toString();
		}
	}
		

	public String getWorkflowGraphProlog() throws SQLException {

		synchronized(_traceDBConnection) {

			return  getWorkflowNodesProlog() + PortableIO.EOL +
					getWorkflowPortsProlog() + PortableIO.EOL +
					getWorkflowChannelsProlog();
		}
	}
	
	public String getDataEventsProlog() throws SQLException {

		synchronized(_traceDBConnection) {
			StringBuffer prologFacts = new StringBuffer();
	
			ResultSet resultSet = _statement.executeQuery(
					"SELECT NodeName, StepNumber, PortName, EventClass, Uri "				+
					"FROM Node " 															+
					"    JOIN Port ON Node.NodeID = Port.NodeID "							+
					"    JOIN PortEvent ON Port.PortID = PortEvent.PortID "					+
					"    JOIN Packet ON PortEvent.PacketID = Packet.PacketID " 				+
					"    JOIN PacketResource ON Packet.PacketID = PacketResource.PacketID " +
					"    JOIN Resource ON PacketResource.ResourceID = Resource.ResourceID "	+
					"    JOIN Step ON PortEvent.StepID = Step.StepID "						+
					"ORDER BY NodeName, StepNumber, PortName, EventClass");
		
			while (resultSet.next()) {
				
				prologFacts.append(
						"rf_event(" 						+
						resultSet.getString("EventClass")	+
						",'"								+ 
						resultSet.getString("NodeName") 	+ 
						"','" 								+ 
						resultSet.getString("StepNumber")	+ 
						"','" 								+
						resultSet.getString("PortName")		+
						"','" 								+
						resultSet.getString("Uri")			+
						"')."								+ 
						PortableIO.EOL
				);
			}
			
			return prologFacts.toString();
		}
	}
	
	// TODO Modify query to use Update, NodeVariable, and ActorVariable tables to 
	// infer name of each variable name receiving metadata, rather than using
	// the keys of the metadata items assigned by the metadata creator, when reporting
	// metadata read events.
	public String getMetadataEventsProlog() throws SQLException {
		
		synchronized(_traceDBConnection) {

			StringBuffer prologFacts = new StringBuffer();

			ResultSet resultSet = _statement.executeQuery(
					"SELECT NodeName, StepNumber, PortName, EventClass, Key, Value "		+
					"FROM Node " 															+
					"    JOIN Port ON Node.NodeID = Port.NodeID "							+
					"    JOIN PortEvent ON Port.PortID = PortEvent.PortID "					+
					"    JOIN Packet ON PortEvent.PacketID = Packet.PacketID " 				+
					"    JOIN PacketResource ON Packet.PacketID = PacketResource.PacketID " +
					"    JOIN Resource ON PacketResource.ResourceID = Resource.ResourceID "	+
					"    JOIN PacketMetadata ON Packet.PacketID = PacketMetadata.PacketID " +
					"	 JOIN Data ON PacketMetadata.DataID = Data.DataID"					+
					"    JOIN Step ON PortEvent.StepID = Step.StepID "						+
					"ORDER BY NodeName, StepNumber, PortName, EventClass");
		
			while (resultSet.next()) {
				
				prologFacts.append(
						"rf_event(" 														+
						resultSet.getString("EventClass")									+
						",'"																+ 
						resultSet.getString("NodeName") 									+ 
						"','" 																+ 
						resultSet.getString("StepNumber")									+ 
						"','" 																+
						resultSet.getString("PortName") + "@" + resultSet.getString("Key")	+
						"','" 																+
						resultSet.getString("Value")										+
						"')."																+ 
						PortableIO.EOL
				);
			}
			
			return prologFacts.toString();
		}
	}

	public String getStepEventsProlog() throws SQLException {
		
		synchronized(_traceDBConnection) {

			StringBuffer prologFacts = new StringBuffer();
	
			ResultSet resultSet = _statement.executeQuery(
					"SELECT NodeName, StepNumber "					+
					"FROM Node " 									+
					"    JOIN Step ON Node.NodeID = Step.NodeID "	+
					"ORDER BY StepID");
		
			while (resultSet.next()) {
				
				prologFacts.append(
						"rf_step("								+ 
						resultSet.getString("NodeName") 		+ 
						"','" 									+ 
						resultSet.getString("StepNumber")		+
						"')."									+ 
						PortableIO.EOL);
			}
			
			return prologFacts.toString();
		}
	}

	public String getPortEventsProlog() throws SQLException {
		synchronized(_traceDBConnection) {
			return  getDataEventsProlog() + getMetadataEventsProlog();
		}
	}
	
	public String getNodeStepCountsYaml() throws SQLException {
		
		synchronized(_traceDBConnection) {
			StringBuffer records = new StringBuffer();
			
			ResultSet resultSet = _statement.executeQuery(
					"SELECT NodeName, StepCount "		+
					"FROM Node "						+
					"WHERE IsHidden = FALSE "			+
					"ORDER BY NodeName");
			
			while (resultSet.next()) {
	
				records.append(	
						resultSet.getString("NodeName") 	+ 
						": " 								+ 
						resultSet.getString("StepCount") 	+ PortableIO.EOL
				);
			}
			
			return records.toString();
		}
	}

	public String getResourcesYaml() throws SQLException {
		
		synchronized(_traceDBConnection) {

			StringBuffer records = new StringBuffer();

			ResultSet resultSet = _statement.executeQuery(
					"SELECT Uri, Value, IsReference "												+
					"FROM Data  "																	+
					"	JOIN Resource ON Resource.DataID = Data.DataID  "							+
					"	JOIN PacketResource ON PacketResource.ResourceID = Resource.ResourceID  "	+
					"	JOIN Packet ON Packet.PacketID = PacketResource.PacketID  "					+
					"	JOIN PortEvent ON PortEvent.PortEventID = Packet.OriginEventID  "			+
					"	JOIN Step ON Step.StepID = PortEvent.StepID "								+
					"	JOIN Port ON Port.PortID = PortEvent.PortID  "								+
					"	JOIN Node ON Node.NodeID = Port.NodeID  "									+
					"	JOIN Actor ON Actor.ActorID = Node.ActorID  "								+
					"	WHERE Node.ParentNodeID IS NOT NULL "											+
					"ORDER BY Uri "								
			);
			
			while (resultSet.next()) {
				records.append(resultSet.getString("Uri"));
				if (! resultSet.getBoolean("IsReference")) {
					records.append(": " + PortableIO.replaceLineBreaksWithSpaces(resultSet.getString("Value")));
				}
				records.append(PortableIO.EOL);
			}
			
			return records.toString();
		}
	}
	
	public ResultSet query(String sql) throws SQLException {
		synchronized(_traceDBConnection) {
			return _statement.executeQuery(sql);
		}
	}
	
	public String dumpActorTable() throws SQLException {
		
		synchronized(_traceDBConnection) {
			return PortableDB.dumpTable( 
					_statement,
					"Actor",
					 new String[] {"ActorId", 
								   "ActorName                                      "}
			);
		}
	}
	
	public String dumpActorVariableTable() throws SQLException {
		
		synchronized(_traceDBConnection) {
		
			return PortableDB.dumpTable( 
					_statement,
					"ActorVariable",
					new String[] {"VariableID", "ActorID", "VariableClass", 
								   "DataTypeID", "VariableName"} 
			);
		}
	}
	
	public String dumpChannelTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
										 "Channel",
										 new String[] {"InPortID", "OutPortID"}, 
										 "ORDER BY InPortID, OutPortID"
			);
		}
	}

	public String dumpDataTable() throws SQLException {
		
		synchronized(_traceDBConnection) {
			return PortableDB.dumpTable( _statement,
					 "Data",
					 new String[] {"DataID", "IsReference", "DataTypeID", 
								   "Value                                                 "}
			);
		}
	}

	public String dumpNodePublishedResourceView() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "NodePublishedResource",
					 new String[] { 
									"NodeName                                          ",
									"StepNumber", 
									"ParentNodeID",
									"ActorName              ",
									"PortName",
									"IsReference",
					 				"DataTypeID", 
									"Uri                                                ",
									"Value                                              "}
			);
		}
	}
	
	public String dumpNodeTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable(
					_statement,
					"Node", 			
					new String[] {"NodeId", "ParentNodeID", "ActorID", "StepCount", 
					"NodeName                                          ",
					"LocalNodeName                  "}
			);
		}
	}
	
	public String dumpNodeTableWide() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable(
					_statement,
					"Node", 			
					new String[] {"NodeId", "ParentNodeID", "ActorID", "StepCount", 
					"NodeName                                                      ",
					"LocalNodeName                            "}
			);
		}
	}

	public String dumpNodeVariableTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( 
					_statement,
					"NodeVariable",
					new String[] {"NodeVariableID", "NodeID", "ActorVariableID"}
				);
		}		
	}
	
	public String dumpPacketMetadataTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "PacketMetadata",
					 new String[] {"MetadataID", "PacketID", "Key          ", "DataID"}
			);
		}
	}

	
	public String dumpPacketResourceTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "PacketResource",
					 new String[] {"PacketID", 
								   "ResourceID" }
			);
		}
	}	
	
	
	public String dumpPacketTable() throws SQLException {
		
		synchronized(_traceDBConnection) {
		
			return PortableDB.dumpTable( _statement,
					 "Packet",
					 new String[] {"PacketID", "OriginEventID"}
			);
		}
	}

	
	public String dumpPortEventTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "PortEvent",
					 new String[] {"PortEventID", "PortID", "PacketID", "StepID", "EventClass", "EventNumber", "EventTime             "}
			);
		}
	}
	
	public String dumpPortTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( 
					_statement,
					"Port",
					 new String[] {"PortId", "NodeID", "NodeVariableID",
								   "PortDirection", "PacketCount", "PortName       ", 
								   "UriTemplate                                      "}
			);
		}
	}

	public String dumpPublishedResourceView() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "PublishedResource",
					 new String[] { "IsReference", "" +
					 				"DataTypeID", 
									"Uri                                                ",
									"Value                                              "}
			);
		}
	}	

	public String dumpWorkflowInputs() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "WorkflowInput",
					 new String[] {"StepNumber",
								   "PortName          ", 
					 			   "Value                                              " }
			);
		}
	}

	public String dumpWorkflowOutputs() throws SQLException {
		
		synchronized(_traceDBConnection) {
			return PortableDB.dumpTable( _statement,
					 "WorkflowOutput",
					 new String[] {"StepNumber",
								   "PortName          ", 
					 			   "Value                                              " }
			);
		}
	}

	
	public String dumpResourceTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "Resource",
					 new String[] {"ResourceID", 
					 			   "DataID", 
								   "Uri                                                " }
			);
		}
	}
	

	public String dumpStepTable_NoTimestamps_Ordered() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "Step",
					 new String[] {"StepID", "NodeID", "ParentStepID", "StepNumber", "UpdateCount"},
					 "ORDER BY NodeID, StepNumber"
			);
		}
	}

	
	public String dumpStepTable_NoTimestamps() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "Step",
					 new String[] {"StepID", "NodeID", "ParentStepID", "StepNumber", "UpdateCount"}
			);
		}
	}

	public String dumpStepTable() throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					 "Step",
					 new String[] {"StepID", "NodeID", "ParentStepID", "StepNumber", "UpdateCount", "StartTime             ", "EndTime               "}
			);
		}
	}
	
	public String dumpTable(String tableName, String[] columns, String qualifier) throws SQLException {
		
		synchronized(_traceDBConnection) {

			return PortableDB.dumpTable( _statement,
					tableName,
					columns,
					qualifier
			);
		}
	}

	public synchronized Long identifyNode(String nodeName) throws Exception {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getIdentifyNodeStatement();
			PortableDB.setStringParameter(statement,	1, nodeName);
			ResultSet results = statement.executeQuery();
			if (results.next() == false) {
				throw new Exception("Node '" + nodeName + "' not found in Trace DB");
			}
			
			return results.getLong(1);
		}
	}
	
	private PreparedStatement _getIdentifyNodeStatement() throws SQLException {
		if (_identifyNodeStatement == null) {
			String sql = "SELECT NodeID " 									+
						 "From Node " 										+
						 "WHERE NodeName = ?";
			_identifyNodeStatement = _traceDBConnection.prepareStatement(sql);
		}
		return _identifyNodeStatement;
	}

	public synchronized Long identifyTopNode() throws Exception {
		
		synchronized(_traceDBConnection) {
			
			PreparedStatement statement = _getIdentifyTopNodeStatement();
			ResultSet results = statement.executeQuery();
			if (results.next() == false) {
				throw new Exception("Top node not found in Trace DB");
			}
			
			return results.getLong(1);
		}
	}
	
	private PreparedStatement _getIdentifyTopNodeStatement() throws SQLException {
		
		if (_identifyTopNodeStatement == null) {
			String sql = "SELECT NodeID " 									+
						 "FROM Node " 										+
						 "WHERE ParentNodeID IS NULL";
			_identifyTopNodeStatement = _traceDBConnection.prepareStatement(sql);
		}
		
		return _identifyTopNodeStatement;
	}


	
	public List<WorkflowModel.Node> getNodes(Long parentNodeID) throws Exception {
		
		String sql;
		PreparedStatement statement;
		
		sql =	"SELECT NodeID, ParentNodeID, NodeName, LocalNodeName, HasChildren " +
				"FROM Node " +
				"WHERE  Node.IsHidden = FALSE ";
		
		if (parentNodeID == null) {
			sql += " AND ParentNodeID IS NULL";
			statement = _traceDBConnection.prepareStatement(sql);
		} else {			
			sql += " AND ParentNodeID = ?";
			statement = _traceDBConnection.prepareStatement(sql);
			PortableDB.setNullableLongParameter(statement,	1, parentNodeID);
		}
		
		ResultSet rs = statement.executeQuery();

		List<WorkflowModel.Node> nodes = new LinkedList<WorkflowModel.Node>();
		while (rs.next()) {
			nodes.add(new WorkflowModel.Node(
					rs.getLong("NodeID"), 
					rs.getLong("ParentNodeID"),
					rs.getString("NodeName"), 
					rs.getString("LocalNodeName"),
					rs.getBoolean("HasChildren")
				)
			);

		}
		
		return nodes;
	}
	
	public List<WorkflowModel.Outflow> getOutflows(Long parentNodeID) throws SQLException {
		
		String sql;
		PreparedStatement statement;
		
		sql =	"SELECT NodeName, LocalNodeName, PortName, UriTemplate " 			+
				"FROM Port JOIN Node ON Port.NodeID = Node.NodeID " 	+
				"WHERE PortDirection = 'o' AND Node.IsHidden = FALSE ";
		
		if (parentNodeID == null) {
			sql += " AND ParentNodeID IS NULL";
			statement = _traceDBConnection.prepareStatement(sql);
		} else {
			sql += " AND ParentNodeID = ?";
			statement = _traceDBConnection.prepareStatement(sql);
			PortableDB.setNullableLongParameter(statement,	1, parentNodeID);
		}
		
		ResultSet rs = statement.executeQuery();

		List<WorkflowModel.Outflow> outflows = new LinkedList<WorkflowModel.Outflow>();
		while(rs.next()) {
			outflows.add(new WorkflowModel.Outflow(
					rs.getString("NodeName"), 
					rs.getString("LocalNodeName"), 
					rs.getString("PortName"),
					rs.getString("UriTemplate")
				)
			);
		}
		
		return outflows;
	}
	
	
	public  List<WorkflowModel.Channel> getChannels(Long parentNodeID) throws SQLException {
		List<WorkflowModel.Channel> channels = getDirectChannels(parentNodeID);
		channels.addAll(getBufferedChannels(parentNodeID));
		return channels;
	}
	
	
	public List<WorkflowModel.Channel> getDirectChannels(Long parentNodeID) throws SQLException {
		
		String sql;
		PreparedStatement statement;
				
		sql =	"SELECT ReceivingNode.NodeName 		AS	ReceivingNodeNodeName, " 		+
				"		ReceivingNode.LocalNodeName AS 	ReceivingNodeLocalNodeName, " 	+
				"		ReceivingPort.PortName		AS 	ReceivingPortPortName, " 		+
				"		ReceivingPort.UriTemplate 	AS	ReceivingPortUriTemplate, " 	+
				"		SendingNode.NodeName		AS  SendingNodeNodeName, "			+
				"		SendingNode.LocalNodeName	AS	SendingNodeLocalNodeName, "		+ 
				"		SendingPort.PortName		AS	SendingPortPortName, "			+
				"		SendingPort.UriTemplate 	AS	SendingPortUriTemplate " 		+
				
				"FROM Node 		AS ReceivingNode 													" +
				"JOIN Port 		AS ReceivingPort 	ON ReceivingPort.NodeID	= ReceivingNode.NodeID 	" +
				"JOIN Channel 						ON Channel.InPortID 	= ReceivingPort.PortID 	" +
				"JOIN Port 		As SendingPort 		ON Channel.OutPortID 	= SendingPort.PortID 	" +
				"JOIN Node 		As SendingNode 		ON SendingPort.NodeID 	= SendingNode.NodeID 	" +
				
				"WHERE 	SendingNode.IsHidden 		= FALSE 	AND 								" +
				"		ReceivingNode.IsHidden = FALSE";
		
		if (parentNodeID == null) {
			sql += " AND ReceivingNode.ParentNodeID IS NULL";
			statement = _traceDBConnection.prepareStatement(sql);
		} else {
			sql += " AND ReceivingNode.ParentNodeID = ?";
			statement = _traceDBConnection.prepareStatement(sql);
			PortableDB.setNullableLongParameter(statement, 1, parentNodeID);
		}
		
		ResultSet rs = statement.executeQuery();

		List<WorkflowModel.Channel> channels = new LinkedList<WorkflowModel.Channel>();
		while(rs.next()) {
			channels.add(new WorkflowModel.Channel(
					rs.getString("SendingNodeNodeName"), 
					rs.getString("SendingNodeLocalNodeName"),
					rs.getString("SendingPortPortName"), 
					rs.getString("SendingPortUriTemplate"),
					rs.getString("ReceivingNodeNodeName"), 
					rs.getString("ReceivingNodeLocalNodeName"),
					rs.getString("ReceivingPortPortName"), 
					rs.getString("ReceivingPortUriTemplate")
				)
			);
		}
		
		return channels;
	}

	public List<WorkflowModel.Channel> getBufferedChannels(Long parentNodeID) throws SQLException {
		
		String sql;
		PreparedStatement statement;
				
		sql =	"SELECT ReceivingNode.NodeName 		AS	ReceivingNodeNodeName, " 		+
				"		ReceivingNode.LocalNodeName AS 	ReceivingNodeLocalNodeName, " 	+
				"		ReceivingPort.PortName		AS 	ReceivingPortPortName, " 		+
				"		ReceivingPort.UriTemplate 	AS	ReceivingPortUriTemplate, " 	+
				"		SendingNode.NodeName		AS  SendingNodeNodeName, "			+
				"		SendingNode.LocalNodeName	AS	SendingNodeLocalNodeName, "		+ 
				"		SendingPort.PortName		AS	SendingPortPortName, "			+
				"		SendingPort.UriTemplate 	AS	SendingPortUriTemplate " 		+
				
				"FROM Node 		AS 	ReceivingNode 															" +
				"JOIN Port 		AS 	ReceivingPort 	ON 	ReceivingPort.NodeID 	= ReceivingNode.NodeID 		" +
				"JOIN Channel 	AS 	BufferChannel 	ON 	BufferChannel.InPortID 	= ReceivingPort.PortID 		" +
				"JOIN Port 		AS 	BufferOutPort 	ON 	BufferOutPort.PortID 	= BufferChannel.OutPortID 	" +
				"JOIN Node 		AS 	BufferNode 		ON 	BufferNode.NodeID 		= BufferOutPort.NodeID 		" +
				"JOIN Port 		AS 	BufferInPort 	ON 	BufferInPort.NodeID 	= BufferNode.NodeID 		" +
				"JOIN Channel 	AS 	SenderChannel 	ON 	SenderChannel.InPortID 	= BufferInPort.PortID 		" +
				"JOIN Port 		AS 	SendingPort 	ON 	SenderChannel.OutPortID = SendingPort.PortID 		" +
				"JOIN Node		AS 	SendingNode 	ON 	SendingPort.NodeID 		= SendingNode.NodeID 		" +
				
				"WHERE 	ReceivingNode.IsHidden		= FALSE		AND											" +
				"		BufferNode.IsHidden 		= TRUE 		AND											" +
				"		SendingNode.IsHidden 		= FALSE 												";
		
		if (parentNodeID == null) {
			sql += " AND ReceivingNode.ParentNodeID IS NULL";
			statement = _traceDBConnection.prepareStatement(sql);
		} else {
			sql += " AND ReceivingNode.ParentNodeID = ?";
			statement = _traceDBConnection.prepareStatement(sql);
			PortableDB.setNullableLongParameter(statement, 1, parentNodeID);
		}
		
		ResultSet rs = statement.executeQuery();

		List<WorkflowModel.Channel> channels = new LinkedList<WorkflowModel.Channel>();
		while(rs.next()) {
			channels.add(new WorkflowModel.Channel(
					rs.getString("SendingNodeNodeName"), 
					rs.getString("SendingNodeLocalNodeName"),
					rs.getString("SendingPortPortName"), 
					rs.getString("SendingPortUriTemplate"),
					rs.getString("ReceivingNodeNodeName"), 
					rs.getString("ReceivingNodeLocalNodeName"),
					rs.getString("ReceivingPortPortName"), 
					rs.getString("ReceivingPortUriTemplate")
				)
			);
		}
		
		return channels;
	}
	
	


}
