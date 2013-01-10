package org.restflow.metadata;

import java.io.File;
import java.sql.Connection;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.metadata.WritableTrace;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.PortableDB;


public class TestTraceDatabase_Inserts extends RestFlowTestCase {

	private WorkflowContext _context;
	
	public void setUp() throws Exception {
		super.setUp();
		_context = new WorkflowContextBuilder().build();
	}
	
	public void test_ConnectAndDisconnectDatabase() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();
		assertTrue(connection.isValid(0));
		assertFalse(connection.isClosed());
		
		// close the connection and discard the database
		connection.close();
		assertFalse(connection.isValid(0));
		assertTrue(connection.isClosed());
	}
	
	public void test_CreateTraceDatabaseTables_Volatile() throws Exception {
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();
		
		String[] tableNames = new String[] {
			"Actor", "Node", "ActorVariable", "DependencyRule", "Node",
			"NodeVariable", "Data", "Port", "Channel", "Step", "Update",
			"PortEvent", "Packet", "PacketMetadata", "Resource", "PacketResource"
		};
		
		// make sure none of the trace tables exist in this database yet
		for (String tableName: tableNames) {
			assertFalse(PortableDB.databaseHasTable(connection, tableName));
		}

		// create all of the tables in the trace schema
		int statementCount = WritableTrace.createTraceDBTables(_context, connection);
		assertEquals(21, statementCount);
		
		// make sure all of the trace tables now exist in the database
		for (String tableName: tableNames) {
			assertTrue(PortableDB.databaseHasTable(connection, tableName));
		}
		
		// check the nonexistence of an unexpected table
		assertFalse(PortableDB.databaseHasTable(connection, "NotATableName"));
		
		// close the connection and discard the database
		connection.close();
	}

	public void test_CreateTraceDatabaseTables_Persistent() throws Exception {
		
		File testDirectory = getRunDirectoryForTest("CreateTraceDatabaseTablesPersistent");
		String traceDBFilePath = testDirectory + "/trace";
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createNewPersistentDatabase(traceDBFilePath);
		
		String[] tableNames = new String[] {
			"Actor", "Node", "ActorVariable", "DependencyRule", "Node",
			"NodeVariable", "Data", "Port", "Channel", "Step", "Update",
			"PortEvent", "Packet", "PacketMetadata", "Resource", "PacketResource"
		};
		
		// make sure none of the trace tables exist in this database yet
		for (String tableName: tableNames) {
			assertFalse(PortableDB.databaseHasTable(connection, tableName));
		}

		// create all of the tables in the trace schema
		int statementCount = WritableTrace.createTraceDBTables(_context, connection);
		assertEquals(21, statementCount);
		
		// make sure all of the trace tables now exist in the database
		for (String tableName: tableNames) {
			assertTrue(PortableDB.databaseHasTable(connection, tableName));
		}
		
		// check the nonexistence of an unexpected table
		assertFalse(PortableDB.databaseHasTable(connection, "NotATableName"));
		
		// close the connection
		connection.close();
	}


	public void test_AccessPreviouslyCreatedTraceDatabase() throws Exception {
		
		File testDirectory = getRunDirectoryForTest("AccessPreviouslyCreatedTraceDatabase");
		String traceDBFilePath = testDirectory + File.separator;
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createNewPersistentDatabase(traceDBFilePath);
		
		String[] tableNames = new String[] {
			"Actor", "Node", "ActorVariable", "DependencyRule", "Node",
			"NodeVariable", "Data", "Port", "Channel", "Step", "Update",
			"PortEvent", "Packet", "PacketMetadata", "Resource", "PacketResource"
		};
		
		// make sure none of the trace tables exist in this database yet
		for (String tableName: tableNames) {
			assertFalse(PortableDB.databaseHasTable(connection, tableName));
		}

		// create all of the tables in the trace schema
		int statementCount = WritableTrace.createTraceDBTables(_context, connection);
		assertEquals(21, statementCount);
				
		// close the connection
		connection.close();	
			
		// reopen a connection to the persistent database
		Connection newConnection = WritableTrace.connectToExistingPersistentDatabase(traceDBFilePath);
		
		// make sure all of the trace tables exist in the database
		for (String tableName: tableNames) {
			assertTrue(PortableDB.databaseHasTable(newConnection, tableName));
		}
		
		// check the nonexistence of an unexpected table
		assertFalse(PortableDB.databaseHasTable(newConnection, "NotATableName"));
		
		// close the connection
		newConnection.close();	
}

	
	
	
	public void test_InsertNode_TwoPeerNodes() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert one top-level node
		long nodeID = manager.insertNode("NodeOne", "NodeOne",null, null, 0L, false, false);
		assertEquals(1, nodeID);
		assertEquals(1, manager.getRowCountForTable("Node"));
		
		// insert a second top-level node
		nodeID = manager.insertNode("NodeTwo", "NodeTwo", null, null, 0L, false, false);
		assertEquals(2, nodeID);
		assertEquals(2, manager.getRowCountForTable("Node"));

		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertNode_TwoNestedNodes() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert one top-level node
		long nodeOneID = manager.insertNode("NodeOne", "NodeOne", null, null, 0L, false, true);
		assertEquals(1, nodeOneID);
		assertEquals(1, manager.getRowCountForTable("Node"));
		
		// insert a node nested in the first node
		long nodeTwoID = manager.insertNode("NodeTwo", "NodeTwo", nodeOneID, null, 0L, false, false);
		assertEquals(2, nodeTwoID);
		assertEquals(2, manager.getRowCountForTable("Node"));

		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertPort() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
		
		// insert a top-level node
		long nodeID = manager.insertNode("NodeOne", "NodeOne", null, null, 0L, false, false);

		// add a inflow and an outflow to the node
		long inflowID = manager.insertInflow("Inflow", nodeID, null, "/in");
		long outflowID = manager.insertOutflow("Outflow", nodeID, null, "/out");

		assertEquals(1, nodeID);
		assertEquals(1, inflowID);
		assertEquals(2, outflowID);
		assertEquals(1, manager.getRowCountForTable("Node"));
		assertEquals(2, manager.getRowCountForTable("Port"));

		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertChannel() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a datbase access manager
		WritableTrace manager = new WritableTrace(connection);
		
		// insert a top-level node with an outflow
		long nodeOneID = manager.insertNode("NodeOne", "NodeOne", null, null, 0L, false, false);
		long outflowID = manager.insertOutflow("Outflow", nodeOneID, null, "/out");

		// insert a top-level node with an inflow
		long nodeTwoID = manager.insertNode("NodeTwo", "NodeTwo", null, null, 0L, false, false);
		long inflowID = manager.insertInflow("Inflow", nodeTwoID, null, "/in");
		
		// insert a channel between the outflow and the inflow
		manager.insertChannel(outflowID, inflowID);

		assertEquals(1, nodeOneID);
		assertEquals(2, nodeTwoID);
		assertEquals(1, outflowID);
		assertEquals(2, inflowID);

		assertEquals(2, manager.getRowCountForTable("Node"));
		assertEquals(2, manager.getRowCountForTable("Port"));
		assertEquals(1, manager.getRowCountForTable("Channel"));
		
		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertStep() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
		
		// insert a top-level node
		long nodeID = manager.insertNode("NodeOne", "NodeOne", null, null, 0L, false, false);

		// insert a step of the node
		long firstStepID = manager.insertStep(nodeID, null, 1L, 0L, null, null);
		assertEquals(1, nodeID);
		assertEquals(1, firstStepID);
		assertEquals(1, manager.getRowCountForTable("Node"));
		assertEquals(1, manager.getRowCountForTable("Step"));

		// insert a second step of the node
		long secondStepID = manager.insertStep(nodeID, null, 2L, 0L, null, null);
		assertEquals(2, secondStepID);
		assertEquals(2, manager.getRowCountForTable("Step"));
		
		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertStep_NestedSteps() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);

		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert one top-level node
		long nodeOneID = manager.insertNode("NodeOne", "NodeOne", null, null, 0L, false, true);
		assertEquals(1, nodeOneID);
		assertEquals(1, manager.getRowCountForTable("Node"));
		
		// insert a node nested in the first node
		long nodeTwoID = manager.insertNode("NodeTwo", "NodeTwo", nodeOneID, null, 0L, false, false);
		assertEquals(2, nodeTwoID);
		assertEquals(2, manager.getRowCountForTable("Node"));
		
		// insert a step of the top-level node
		long nodeOneStepID = manager.insertStep(nodeOneID, null, 1L, 0L, null, null);
		assertEquals(1, nodeOneID);
		assertEquals(2, nodeTwoID);
		assertEquals(1, nodeOneStepID);
		assertEquals(2, manager.getRowCountForTable("Node"));
		assertEquals(1, manager.getRowCountForTable("Step"));

		// insert a step of the nested node
		long nodeTwoStepID = manager.insertStep(nodeTwoID, nodeOneStepID, 2L, 0L, null, null);
		assertEquals(2, nodeTwoStepID);
		assertEquals(2, manager.getRowCountForTable("Step"));
		
		// close the connection and discard the database
		connection.close();
	}
	
	public void test_InsertPacket() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
		
		// insert a packet
		long packetID = manager.insertPacket(null);
		assertEquals(1, packetID);
		assertEquals(1, manager.getRowCountForTable("Packet"));

		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertPortEvent() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
		
		// insert two top-level nodes with a channel between them
		long nodeOneID = manager.insertNode("NodeOne", "NodeOne", null, null, 0L, false, false);
		long nodeTwoID = manager.insertNode("NodeTwo", "NodeTwo", null, null, 0L, false, false);
		long outflowID = manager.insertOutflow("Outflow", nodeOneID, null, "/out");
		long inflowID = manager.insertInflow("Inflow", nodeTwoID, null, "/in");
		manager.insertChannel(outflowID, inflowID);
		assertEquals(1, nodeOneID);
		assertEquals(2, nodeTwoID);
		assertEquals(1, outflowID);
		assertEquals(2, inflowID);
		assertEquals(2, manager.getRowCountForTable("Node"));
		assertEquals(2, manager.getRowCountForTable("Port"));
		assertEquals(1, manager.getRowCountForTable("Channel"));

		// insert a step of the first node
		long nodeOneStepID = manager.insertStep(nodeOneID, null, 1L, 0L, null, null);		
		assertEquals(1, nodeOneStepID);
		assertEquals(1, manager.getRowCountForTable("Step"));

		// insert a packet
		long packetID = manager.insertPacket(null);
		assertEquals(1, packetID);
		assertEquals(1, manager.getRowCountForTable("Packet"));

		// insert a packet write event
		long writeEventID = manager.insertPortEvent(outflowID, packetID, nodeOneStepID, "w", 1, null);
		assertEquals(1, writeEventID);
		assertEquals(1, manager.getRowCountForTable("PortEvent"));
		
		// update the packet to assign its origin event
		manager.updatePacketOriginEvent(packetID, writeEventID);
		
		// insert a step of the second node
		long nodeTwoStepID = manager.insertStep(nodeTwoID, null, 1L, 0L, null, null);		
		assertEquals(2, nodeTwoStepID);
		assertEquals(2, manager.getRowCountForTable("Step"));
		
		// insert a packet read event
		long readEventID = manager.insertPortEvent(inflowID, packetID, nodeTwoStepID, "r", 1, null);
		assertEquals(2, readEventID);
		assertEquals(2, manager.getRowCountForTable("PortEvent"));

		// close the connection and discard the database
		connection.close();
	}
	
	public void test_InsertResource_NullDatumID() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert a resource
		long firstResourceID = manager.insertResource("/d/1", null);
		assertEquals(1, firstResourceID);
		assertEquals(1, manager.getRowCountForTable("Resource"));
		
		// insert a second resource
		long secondResourceID = manager.insertResource("/d/2", null);
		assertEquals(2, secondResourceID);
		assertEquals(2, manager.getRowCountForTable("Resource"));

		// close the connection and discard the database
		connection.close();
	}
	
	public void test_InsertDatum() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert first datum
		long firstDatumID = manager.insertData(17, false, null);
		assertEquals(1, firstDatumID);
		assertEquals(1, manager.getRowCountForTable("Data"));
		
		// insert second datum
		long secondDatumID = manager.insertData(42, false, null);
		assertEquals(2, secondDatumID);
		assertEquals(2, manager.getRowCountForTable("Data"));

		// close the connection and discard the database
		connection.close();
	}
	
	public void test_InsertResource_WithData() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert first datum
		long firstDatumID = manager.insertData(17, false, null);
		long secondDatumID = manager.insertData(42, false, null);
		assertEquals(1, firstDatumID);
		assertEquals(2, secondDatumID);
		assertEquals(2, manager.getRowCountForTable("Data"));

		// insert resource for first datum
		long firstResourceID = manager.insertResource("/d/1", firstDatumID);
		assertEquals(1, firstResourceID);
		assertEquals(1, manager.getRowCountForTable("Resource"));
		
		// insert resource for second datum
		long secondResourceID = manager.insertResource("/d/2", secondDatumID);
		assertEquals(2, secondResourceID);
		assertEquals(2, manager.getRowCountForTable("Resource"));

		// close the connection and discard the database
		connection.close();
	}
	
	
	public void test_InsertPacketResource() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert first datum
		long firstDatumID = manager.insertData(17, false, null);
		long secondDatumID = manager.insertData(42, false, null);
		assertEquals(1, firstDatumID);
		assertEquals(2, secondDatumID);
		assertEquals(2, manager.getRowCountForTable("Data"));

		// insert resource for first datum
		long firstResourceID = manager.insertResource("/d/1", firstDatumID);
		assertEquals(1, firstResourceID);
		assertEquals(1, manager.getRowCountForTable("Resource"));
		
		// insert resource for second datum
		long secondResourceID = manager.insertResource("/d/2", secondDatumID);
		assertEquals(2, secondResourceID);
		assertEquals(2, manager.getRowCountForTable("Resource"));

		// insert a packet
		long packetID = manager.insertPacket(null);
		assertEquals(1, packetID);
		assertEquals(1, manager.getRowCountForTable("Packet"));

		// attach the two resources to the packet
		manager.insertPacketResource(packetID, firstResourceID);
		manager.insertPacketResource(packetID, secondResourceID);
		assertEquals(2, manager.getRowCountForTable("PacketResource"));

		// close the connection and discard the database
		connection.close();
	}
	
	public void test_InsertPacketMetadata() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert first datum
		long firstDatumID = manager.insertData(17, false, null);
		long secondDatumID = manager.insertData(42, false, null);
		assertEquals(1, firstDatumID);
		assertEquals(2, secondDatumID);
		assertEquals(2, manager.getRowCountForTable("Data"));

		// insert a packet
		long packetID = manager.insertPacket(null);
		assertEquals(1, packetID);
		assertEquals(1, manager.getRowCountForTable("Packet"));

		// attach the two resources to the packet
		long firstPacketMetadataID = manager.insertPacketMetadata(packetID, "keyOne", firstDatumID);
		long secondPacketMetadataID = manager.insertPacketMetadata(packetID, "keyTwo", secondDatumID);
		assertEquals(1, firstPacketMetadataID);
		assertEquals(2, secondPacketMetadataID);
		assertEquals(2, manager.getRowCountForTable("PacketMetadata"));

		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertActor() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert one actor
		long actorID = manager.insertActor("ActorOne");
		assertEquals(1, actorID);
		assertEquals(1, manager.getRowCountForTable("Actor"));
		
		// close the connection and discard the database
		connection.close();
	}
	
	public void test_InsertNode_TwoNodes_OneActor() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert one actor
		long actorID = manager.insertActor("ActorOne");
		assertEquals(1, actorID);
		assertEquals(1, manager.getRowCountForTable("Actor"));

		// insert one top-level node using the actor
		long nodeID = manager.insertNode("NodeOne", "NodeOne", null, actorID, 0L, false, false);
		assertEquals(1, nodeID);
		assertEquals(1, manager.getRowCountForTable("Node"));
		
		// insert a second top-level node
		nodeID = manager.insertNode("NodeTwo", "NodeTwo", null, actorID, 0L, false, false);
		assertEquals(2, nodeID);
		assertEquals(2, manager.getRowCountForTable("Node"));

		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertActorVariable() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert one actor
		long actorID = manager.insertActor("ActorOne");
		assertEquals(1, actorID);
		assertEquals(1, manager.getRowCountForTable("Actor"));

		// insert actor variables
		long actorVariableOneID = manager.insertActorVariable(actorID, "v1", "i", null);
		long actorVariableTwoID = manager.insertActorVariable(actorID, "v2", "o", null);
		long actorVariableThreeID = manager.insertActorVariable(actorID, "v3", "s", null);
		
		assertEquals(1, actorVariableOneID);
		assertEquals(2, actorVariableTwoID);
		assertEquals(3, actorVariableThreeID);
		assertEquals(3, manager.getRowCountForTable("ActorVariable"));

		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertNodeVariable() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert one actor
		long actorID = manager.insertActor("ActorOne");
		long actorVariableOneID = manager.insertActorVariable(actorID, "v1", "i", null);
		long actorVariableTwoID = manager.insertActorVariable(actorID, "v2", "o", null);
		long actorVariableThreeID = manager.insertActorVariable(actorID, "v3", "s", null);
		long nodeID = manager.insertNode("NodeOne", "NodeOne", null, null, 0L, false, false);
		assertEquals(1, actorID);
		assertEquals(1, manager.getRowCountForTable("Actor"));
		assertEquals(1, actorVariableOneID);
		assertEquals(2, actorVariableTwoID);
		assertEquals(3, actorVariableThreeID);
		assertEquals(3, manager.getRowCountForTable("ActorVariable"));
		assertEquals(1, nodeID);
		assertEquals(1, manager.getRowCountForTable("Node"));
		
		// insert node variables for each actor variable
		long nodeVariableOneID = manager.insertNodeVariable(nodeID, actorVariableOneID);
		long nodeVariableTwoID = manager.insertNodeVariable(nodeID, actorVariableTwoID);
		long nodeVariableThreeID = manager.insertNodeVariable(nodeID, actorVariableThreeID);
		assertEquals(1, nodeVariableOneID);
		assertEquals(2, nodeVariableTwoID);
		assertEquals(3, nodeVariableThreeID);

		// add a inflow and an outflow to the node and attach to the node variables
		long inflowID = manager.insertInflow("Inflow", nodeID, nodeVariableOneID, "/in");
		long outflowID = manager.insertOutflow("Outflow", nodeID, nodeVariableTwoID, "/out");
		assertEquals(1, inflowID);
		assertEquals(2, outflowID);
		assertEquals(2, manager.getRowCountForTable("Port"));

		// close the connection and discard the database
		connection.close();
	}
	
	public void test_InsertUpdate() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
		
		// insert an actor node with three variables
		long actorID = manager.insertActor("ActorOne");
		long actorVariableOneID = manager.insertActorVariable(actorID, "v1", "i", null);
		long actorVariableTwoID = manager.insertActorVariable(actorID, "v2", "o", null);
		long actorVariableThreeID = manager.insertActorVariable(actorID, "v3", "s", null);
		long nodeID = manager.insertNode("NodeOne", "NodeOne", null, null, 0L, false, false);
		long nodeVariableOneID = manager.insertNodeVariable(nodeID, actorVariableOneID);
		long nodeVariableTwoID = manager.insertNodeVariable(nodeID, actorVariableTwoID);
		long nodeVariableThreeID = manager.insertNodeVariable(nodeID, actorVariableThreeID);
		long inflowID = manager.insertInflow("Inflow", nodeID, nodeVariableOneID, "/in");
		long outflowID = manager.insertOutflow("Outflow", nodeID, nodeVariableTwoID, "/out");
		long stepID = manager.insertStep(nodeID, null, 2L, 0L, null, null);
		long firstDatumID = manager.insertData(17, false, null);
		long secondDatumID = manager.insertData(42, false, null);
		long thirdDatumID = manager.insertData(92, false, null);
		assertEquals(1, actorID);
		assertEquals(1, manager.getRowCountForTable("Actor"));
		assertEquals(1, actorVariableOneID);
		assertEquals(2, actorVariableTwoID);
		assertEquals(3, actorVariableThreeID);
		assertEquals(3, manager.getRowCountForTable("ActorVariable"));
		assertEquals(1, nodeID);
		assertEquals(1, manager.getRowCountForTable("Node"));
		assertEquals(1, nodeVariableOneID);
		assertEquals(2, nodeVariableTwoID);
		assertEquals(3, nodeVariableThreeID);
		assertEquals(1, inflowID);
		assertEquals(2, outflowID);
		assertEquals(2, manager.getRowCountForTable("Port"));
		assertEquals(1, stepID);
		assertEquals(1, manager.getRowCountForTable("Step"));
		assertEquals(1, firstDatumID);
		assertEquals(2, secondDatumID);
		assertEquals(3, thirdDatumID);
		assertEquals(3, manager.getRowCountForTable("Data"));
		
		// insert three variable updates
		long updateOneID = manager.insertUpdate(nodeVariableOneID, firstDatumID, stepID, 1);
		long updateTwoID = manager.insertUpdate(nodeVariableTwoID, secondDatumID, stepID, 1);
		long updateThreeID = manager.insertUpdate(nodeVariableThreeID, thirdDatumID, stepID, 1);
		assertEquals(1, updateOneID);
		assertEquals(2, updateTwoID);
		assertEquals(3, updateThreeID);
		assertEquals(3, manager.getRowCountForTable("Update"));
		
		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertDataType() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert two data types
		long dataTypeOneID = manager.insertDataType("String");
		long dataTypeTwoID = manager.insertDataType("Resolution");
		assertEquals(1, dataTypeOneID);
		assertEquals(2, dataTypeTwoID);
		assertEquals(2, manager.getRowCountForTable("DataType"));

		// insert three data referring to these types
		long firstDatumID = manager.insertData(17, false, dataTypeOneID);
		long secondDatumID = manager.insertData(42, false, dataTypeTwoID);
		long thirdDatumID = manager.insertData(92, false, dataTypeTwoID);
		assertEquals(1, firstDatumID);
		assertEquals(2, secondDatumID);
		assertEquals(3, thirdDatumID);
		assertEquals(3, manager.getRowCountForTable("Data"));

		// close the connection and discard the database
		connection.close();
	}

	public void test_InsertDependencyRule() throws Exception {	
		
		// get a connection to a new private in-memory database instance
		Connection connection = WritableTrace.createPrivateVolatileDatabase();

		// create all of the tables in the trace schema
		WritableTrace.createTraceDBTables(_context, connection);
		
		// get a database access manager
		WritableTrace manager = new WritableTrace(connection);
	
		// insert one actor with three variables
		long actorID = manager.insertActor("ActorOne");
		long actorVariableOneID = manager.insertActorVariable(actorID, "v1", "i", null);
		long actorVariableTwoID = manager.insertActorVariable(actorID, "v2", "o", null);
		long actorVariableThreeID = manager.insertActorVariable(actorID, "v3", "s", null);
		assertEquals(1, actorID);
		assertEquals(1, manager.getRowCountForTable("Actor"));
		assertEquals(1, actorVariableOneID);
		assertEquals(2, actorVariableTwoID);
		assertEquals(3, actorVariableThreeID);
		assertEquals(3, manager.getRowCountForTable("ActorVariable"));
		
		// insert two dependency rules
		long depRuleOneID = manager.insertDependencyRule(actorID, actorVariableThreeID, actorVariableOneID, "dep");
		long depRuleTwoID = manager.insertDependencyRule(actorID, actorVariableTwoID, actorVariableThreeID, "der");
		assertEquals(1, depRuleOneID);
		assertEquals(2, depRuleTwoID);
		assertEquals(2, manager.getRowCountForTable("DependencyRule"));

		// close the connection and discard the database
		connection.close();
	}	
}
