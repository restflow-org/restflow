package org.restflow.metadata;

import java.sql.Connection;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.JavaActor;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.FileProtocol;
import org.restflow.data.InflowProperty;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.metadata.WritableTrace;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.SourceNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;


public class TestTraceDatabase_LoadWorkflow extends RestFlowTestCase {

	private WorkflowContext 		_context;
	private ConsumableObjectStore	_store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();
	}
	
	public void testLoadWorkflow_TopLevelNodes_AnonymousActors() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

		.name("OneShotInflowWorkflow")
		.context(_context)
		
		.node(new JavaNodeBuilder()
			.name("CreateSingletonData")
			.constant("constant", 5)
			.bean(new Object() {
				public int constant, value;
				public void step() { value = constant; }
			})
			.outflow("value", "/multiplier"))

		.node(new JavaNodeBuilder()
			.name("CreateSequenceData")
			.sequence("c", new Integer[] {3, 8, 2})
			.bean(new Object() {
				public int c, value;
				public void step() { value = c; }
			})
			.outflow("v", "/multiplicand"))
		
		.node(new JavaNodeBuilder()
			.name("MultiplySequenceBySingleton")
			.inflow("/multiplier", "a", InflowProperty.ReceiveOnce)
			.inflow("/multiplicand", "b")
			.bean(new Object() {
				public int a, b, c;
				public void step() { c = a * b; }
			})
			.outflow("c", "/product"))

		.node(new JavaNodeBuilder()
			.name("RenderProducts")
			.inflow("/product", "v")
			.bean(new Object() {
				public int v;
				public void step() { System.out.println(v); }
			}))
			
		.build();

		// create the database and get a manager for it
		Connection connection = WritableTrace.createPrivateVolatileDatabase();
		WritableTrace.createTraceDBTables(_context, connection);
		WritableTrace manager = new WritableTrace(connection);
		
		// load the workflow into the database
		manager.storeWorkflowGraph(workflow, null);
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       OneShotInflowWorkflow                           " 		+ EOL +
				"2       CreateSingletonData_actor                       " 		+ EOL +
				"3       CreateSequenceData_actor                        " 		+ EOL +
				"4       MultiplySequenceBySingleton_actor               " 		+ EOL +
				"5       RenderProducts_actor                            " 		+ EOL, 
			manager.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       0         OneShotInflowWorkflow                              OneShotInflowWorkflow           " 	+ EOL +
				"2      1            2       0         OneShotInflowWorkflow.CreateSingletonData          CreateSingletonData             " 	+ EOL +
				"3      1            3       0         OneShotInflowWorkflow.CreateSequenceData           CreateSequenceData              " 	+ EOL +
				"4      1            4       0         OneShotInflowWorkflow.MultiplySequenceBySingleton  MultiplySequenceBySingleton     " 	+ EOL +
				"5      1            5       0         OneShotInflowWorkflow.RenderProducts               RenderProducts                  " 	+ EOL, 
			manager.dumpNodeTable());
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      2      1              o             0           value           /multiplier                                       " 	+ EOL +
				"2      3      2              o             0           v               /multiplicand                                     " 	+ EOL +
				"3      4      3              i             0           b               /multiplicand                                     " 	+ EOL +
				"4      4      4              i             0           a               /multiplier                                       " 	+ EOL +
				"5      4      5              o             0           c               /product                                          " 	+ EOL +
				"6      5      6              i             0           v               /product                                          " 	+ EOL, 
			manager.dumpPortTable());

		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          2       o                        value        " 	+ EOL +
				"2          3       o                        v            " 	+ EOL +
				"3          4       i                        b            " 	+ EOL +
				"4          4       i                        a            " 	+ EOL +
				"5          4       o                        c            " 	+ EOL +
				"6          5       i                        v            " 	+ EOL, 
			manager.dumpActorVariableTable());

		assertEquals(
				"NodeVariableID NodeID ActorVariableID " 	+ EOL +
				"-------------- ------ --------------- " 	+ EOL +
				"1              2      1               " 	+ EOL +
				"2              3      2               " 	+ EOL +
				"3              4      3               " 	+ EOL +
				"4              4      4               " 	+ EOL +
				"5              4      5               " 	+ EOL +
				"6              5      6               " 	+ EOL, 
			manager.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 	+ EOL +
				"-------- --------- " 	+ EOL +
				"3        2         " 	+ EOL +
				"4        1         " 	+ EOL +
				"6        5         " 	+ EOL,
			manager.dumpChannelTable());
	}

	public void testLoadWorkflow_TopLevelNodes_SourceNode() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

		.name("OneShotInflowWorkflow")
		.context(_context)
		
		.node(new SourceNodeBuilder()
			.name("GetGreeting")
			.protocol(new FileProtocol())
			.resource("src/test/resources/unit/TestSourceNode/test.txt")
			.outflow("greeting", "/greeting")
		)

		.node(new JavaNodeBuilder()
			.name("CreatePunctuationSequence")
			.sequence("s", new String[] {"!", "?", "."})
			.bean(new Object() {
				public int s, punct;
				public void step() { punct = s; }
			})
			.outflow("punct", "/punct"))
		
		.node(new JavaNodeBuilder()
			.name("AppendGreetingWithPunctuation")
			.inflow("/greeting", "stringOne", InflowProperty.ReceiveOnce)
			.inflow("/punct", "stringTwo")
			.bean(new Object() {
				public String stringOne, stringTwo, concatenatedString;
				public void step() { concatenatedString = stringOne + stringTwo; }
			})
			.outflow("concatenatedString", "/greeting"))

		.node(new JavaNodeBuilder()
			.name("PrintAppendedGreeting")
			.inflow("/greeting", "g")
			.bean(new Object() {
				public String g;
				public void step() { System.out.println(g); }
			}))
			
		.build();

		// create the database and get a manager for it
		Connection connection = WritableTrace.createPrivateVolatileDatabase();
		WritableTrace.createTraceDBTables(_context, connection);
		WritableTrace manager = new WritableTrace(connection);
		
		// load the workflow into the database
		manager.storeWorkflowGraph(workflow, null);
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       OneShotInflowWorkflow                           " 		+ EOL +
				"2       CreatePunctuationSequence_actor                 " 		+ EOL +
				"3       AppendGreetingWithPunctuation_actor             " 		+ EOL +
				"4       PrintAppendedGreeting_actor                     " 		+ EOL +
				"5       Source                                          " 		+ EOL, 
			manager.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       0         OneShotInflowWorkflow                              OneShotInflowWorkflow           " 	+ EOL +
				"2      1            2       0         OneShotInflowWorkflow.CreatePunctuationSequence    CreatePunctuationSequence       " 	+ EOL +
				"3      1            3       0         OneShotInflowWorkflow.AppendGreetingWithPunctuatio AppendGreetingWithPunctuation   " 	+ EOL +
				"4      1            4       0         OneShotInflowWorkflow.PrintAppendedGreeting        PrintAppendedGreeting           " 	+ EOL +
				"5      1            5       0         OneShotInflowWorkflow.GetGreeting                  GetGreeting                     " 	+ EOL, 
			manager.dumpNodeTable());
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      2      1              o             0           punct           /punct                                            "		+ EOL +
				"2      3      2              i             0           stringOne       /greeting                                         "		+ EOL +
				"3      3      3              i             0           stringTwo       /punct                                            "		+ EOL +
				"4      3      4              o             0           concatenatedStr /greeting                                         "		+ EOL +
				"5      4      5              i             0           g               /greeting                                         "		+ EOL +
				"6      5      6              o             0           greeting        /greeting                                         " 	+ EOL, 
			manager.dumpPortTable());

		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          2       o                        punct        " 	+ EOL +
				"2          3       i                        stringOne    " 	+ EOL +
				"3          3       i                        stringTwo    " 	+ EOL +
				"4          3       o                        concatenated " 	+ EOL +
				"5          4       i                        g            " 	+ EOL +
				"6          5       o                        greeting     " 	+ EOL, 
			manager.dumpActorVariableTable());

		assertEquals(
				"NodeVariableID NodeID ActorVariableID " 	+ EOL +
				"-------------- ------ --------------- " 	+ EOL +
				"1              2      1               " 	+ EOL +
				"2              3      2               " 	+ EOL +
				"3              3      3               " 	+ EOL +
				"4              3      4               " 	+ EOL +
				"5              4      5               " 	+ EOL +
				"6              5      6               " 	+ EOL, 
			manager.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 	+ EOL +
				"-------- --------- " 	+ EOL +
				"2        4         " 	+ EOL +
				"2        6         " 	+ EOL +
				"3        1         " 	+ EOL +
				"5        4         " 	+ EOL +
				"5        6         " 	+ EOL,
			manager.dumpChannelTable());
	}

	public void test_SourceNode_MinimalWorkflow_ExplicitSourceNode() throws Exception {

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()
	
			.context(_context)
			.prefix("/run{RUN}/")

			.node(new SourceNodeBuilder()
				.protocol(new FileProtocol())
				.resource("src/test/resources/unit/TestSourceNode/test.txt")
				.outflow("a", "/a")
			)
				
			.node(new ActorNodeBuilder()
				.inflow("/a", "value")
				.actor(new JavaActorBuilder()
					.bean(new Object() {
						public String value;
						public void step() { System.out.println(value); }
					}))
			)

		.build();
		

		// create the database and get a manager for it
		Connection connection = WritableTrace.createPrivateVolatileDatabase();
		WritableTrace.createTraceDBTables(_context, connection);
		WritableTrace manager = new WritableTrace(connection);
		
		// load the workflow into the database
		manager.storeWorkflowGraph(workflow, null);
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1                                                       " 		+ EOL +
				"2       anonymous_node_1_actor                          " 		+ EOL +
				"3       Source                                          " 		+ EOL, 
			manager.dumpActorTable());
		
		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       0                                                                                            " 	+ EOL +
				"2      1            2       0         .anonymous_node_1                                  anonymous_node_1                " 	+ EOL +
				"3      1            3       0         .anonymous_node_2                                  anonymous_node_2                " 	+ EOL, 
			manager.dumpNodeTable());
		
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      2      1              i             0           value           /a                                                " 	+ EOL +
				"2      3      2              o             0           a               /a                                                " 	+ EOL, 
			manager.dumpPortTable());

		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          2       i                        value        " 	+ EOL +
				"2          3       o                        a            " 	+ EOL, 
			manager.dumpActorVariableTable());

		assertEquals(
				"NodeVariableID NodeID ActorVariableID " 	+ EOL +
				"-------------- ------ --------------- " 	+ EOL +
				"1              2      1               " 	+ EOL +
				"2              3      2               " 	+ EOL, 
			manager.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 	+ EOL +
				"-------- --------- " 	+ EOL +
				"1        2         " 	+ EOL,
			manager.dumpChannelTable());
	}
	
	public void testLoadWorkflow_TopLevelNodes_DataDrivenDirector() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

		.name("MultiplierWorkflow")
		.context(_context)
		.director(new DataDrivenDirector())
		
		.node(new JavaNodeBuilder()
			.name("CreateSingletonData")
			.constant("constant", 5)
			.bean(new Object() {
				public int constant, value;
				public void step() { value = constant; }
			})
			.outflow("value", "/multiplier"))

		.node(new JavaNodeBuilder()
			.name("CreateSequenceData")
			.sequence("c", new Integer[] {3, 8, 2})
			.bean(new Object() {
				public int c, value;
				public void step() { value = c; }
			})
			.outflow("v", "/multiplicand"))
		
		.node(new JavaNodeBuilder()
			.name("MultiplyBySingleton")
			.inflow("/multiplier", "a", InflowProperty.ReceiveOnce)
			.inflow("/multiplicand", "b")
			.bean(new Object() {
				public int a, b, c;
				public void step() { c = a * b; }
			})
			.outflow("c", "/product"))

		.node(new JavaNodeBuilder()
			.name("RenderProducts")
			.inflow("/product", "v")
			.bean(new Object() {
				public int v;
				public void step() { System.out.println(v); }
			}))
			
		.build();

		// create the database and get a manager for it
		Connection connection = WritableTrace.createPrivateVolatileDatabase();
		WritableTrace.createTraceDBTables(_context, connection);
		WritableTrace manager = new WritableTrace(connection);
		
		// load the workflow into the database
		manager.storeWorkflowGraph(workflow, null);
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       MultiplierWorkflow                              " 		+ EOL +
				"2       CreateSingletonData_actor                       " 		+ EOL +
				"3       CreateSequenceData_actor                        " 		+ EOL +
				"4       MultiplyBySingleton_actor                       " 		+ EOL +
				"5       RenderProducts_actor                            " 		+ EOL +
				"6       Buffer                                          " 		+ EOL, 
			manager.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                                       LocalNodeName                             " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------------------- ----------------------------------------- " 	+ EOL +
				"1                   1       0         MultiplierWorkflow                                             MultiplierWorkflow                        " 	+ EOL +
				"2      1            2       0         MultiplierWorkflow.CreateSingletonData                         CreateSingletonData                       " 	+ EOL +
				"3      1            3       0         MultiplierWorkflow.CreateSequenceData                          CreateSequenceData                        " 	+ EOL +
				"4      1            4       0         MultiplierWorkflow.MultiplyBySingleton                         MultiplyBySingleton                       " 	+ EOL +
				"5      1            5       0         MultiplierWorkflow.RenderProducts                              RenderProducts                            " 	+ EOL +
				"6      1            6       0         MultiplierWorkflow.BufferNode-for-MultiplyBySingleton-a        BufferNode-for-MultiplyBySingleton-a      " 	+ EOL +
				"7      1            6       0         MultiplierWorkflow.BufferNode-for-MultiplyBySingleton-b        BufferNode-for-MultiplyBySingleton-b      " 	+ EOL +
				"8      1            6       0         MultiplierWorkflow.BufferNode-for-RenderProducts-v             BufferNode-for-RenderProducts-v           " 	+ EOL,
			manager.dumpNodeTableWide());
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      2      1              o             0           value           /multiplier                                       " 	+ EOL +
				"2      3      2              o             0           v               /multiplicand                                     " 	+ EOL +
				"3      4      3              i             0           b               BufferNode-for-MultiplyBySingleton-b/multiplicand " 	+ EOL +
				"4      4      4              i             0           a               BufferNode-for-MultiplyBySingleton-a/multiplier   " 	+ EOL +
				"5      4      5              o             0           c               /product                                          " 	+ EOL +
				"6      5      6              i             0           v               BufferNode-for-RenderProducts-v/product           " 	+ EOL +
				"7      6      7              i             0           input           /multiplier                                       " 	+ EOL +
				"8      6      8              o             0           output          BufferNode-for-MultiplyBySingleton-a/multiplier   " 	+ EOL +
				"9      7      9              i             0           input           /multiplicand                                     " 	+ EOL +
				"10     7      10             o             0           output          BufferNode-for-MultiplyBySingleton-b/multiplicand " 	+ EOL +
				"11     8      11             i             0           input           /product                                          " 	+ EOL +
				"12     8      12             o             0           output          BufferNode-for-RenderProducts-v/product           " 	+ EOL,
			manager.dumpPortTable());

		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          2       o                        value        " 	+ EOL +
				"2          3       o                        v            " 	+ EOL +
				"3          4       i                        b            " 	+ EOL +
				"4          4       i                        a            " 	+ EOL +
				"5          4       o                        c            " 	+ EOL +
				"6          5       i                        v            " 	+ EOL +
				"7          6       i                        input        " 	+ EOL +
				"8          6       o                        output       " 	+ EOL,
			manager.dumpActorVariableTable());

		assertEquals(
				"NodeVariableID NodeID ActorVariableID " 	+ EOL +
				"-------------- ------ --------------- " 	+ EOL +
				"1              2      1               " 	+ EOL +
				"2              3      2               " 	+ EOL +
				"3              4      3               " 	+ EOL +
				"4              4      4               " 	+ EOL +
				"5              4      5               " 	+ EOL +
				"6              5      6               " 	+ EOL +
				"7              6      7               " 	+ EOL +
				"8              6      8               " 	+ EOL +
				"9              7      7               " 	+ EOL +
				"10             7      8               " 	+ EOL +
				"11             8      7               " 	+ EOL +
				"12             8      8               " 	+ EOL,
			manager.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 		+ EOL +
				"-------- --------- " 		+ EOL +
				"3        10        " 		+ EOL +
				"4        8         " 		+ EOL +
				"6        12        " 		+ EOL +
				"7        1         " 		+ EOL +
				"9        2         " 		+ EOL +
				"11       5         " 		+ EOL,
			manager.dumpChannelTable());
	}
		
	public static class ConstantActor {
		public int constant, value;
		public void step() { value = constant; }
	}
		
	public void testLoadWorkflow_TopLevelNodes_NamedActors() throws Exception {
			
		@SuppressWarnings("unused")
		JavaActor singletonDataCreationActor = new JavaActorBuilder()
			.context(_context)
			.name("SingletonDataCreator")
			.bean(new Object() {
				public int constant, value;
				public void step() { value = constant; }
			})
			.build();

		JavaActorBuilder constantActorBuilder = new JavaActorBuilder()
			.name("SequenceDataCreator")
			.bean(new ConstantActor());
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

		.name("OneShotInflow")
		.context(_context)
		
		.node(new JavaNodeBuilder()
			.context(_context)
			.name("CreateSingletonData")
			.actor(singletonDataCreationActor)
			.constant("constant", 5)
			.outflow("value", "/multiplier")
			.build())

		.node(new JavaNodeBuilder()
			.name("CreateSequenceData")
			.sequence("c", new Integer[] {3, 8, 2})
			.actor(constantActorBuilder)
			.outflow("v", "/multiplicand"))
		
		.node(new JavaNodeBuilder()
			.name("MultiplySequenceBySingleton")
			.inflow("/multiplier", "a", InflowProperty.ReceiveOnce)
			.inflow("/multiplicand", "b")
			.actor( new JavaActorBuilder()
				.name("MultiplyActor")
				.bean(new Object() {
					public int a, b, c;
					public void step() { c = a * b; }
				}))
			.outflow("c", "/product"))

		.node(new JavaNodeBuilder()
			.name("RenderProducts")
			.inflow("/product", "v")
			.bean(new Object() {
				public int v;
				public void step() { System.out.println(v); }
			}))
			
		.build();

		// create the database and get a manager for it
		Connection connection = WritableTrace.createPrivateVolatileDatabase();
		WritableTrace.createTraceDBTables(_context, connection);
		WritableTrace manager = new WritableTrace(connection);
		
		// load the workflow into the database
		manager.storeWorkflowGraph(workflow, null);
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       OneShotInflow                                   " 		+ EOL +
				"2       SingletonDataCreator                            " 		+ EOL +
				"3       SequenceDataCreator                             " 		+ EOL +
				"4       MultiplyActor                                   " 		+ EOL +
				"5       RenderProducts_actor                            " 		+ EOL, 
			manager.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       0         OneShotInflow                                      OneShotInflow                   " 	+ EOL +
				"2      1            2       0         OneShotInflow.CreateSingletonData                  CreateSingletonData             " 	+ EOL +
				"3      1            3       0         OneShotInflow.CreateSequenceData                   CreateSequenceData              " 	+ EOL +
				"4      1            4       0         OneShotInflow.MultiplySequenceBySingleton          MultiplySequenceBySingleton     " 	+ EOL +
				"5      1            5       0         OneShotInflow.RenderProducts                       RenderProducts                  " 	+ EOL, 
			manager.dumpNodeTable());
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      2      1              o             0           value           /multiplier                                       " 	+ EOL +
				"2      3      2              o             0           v               /multiplicand                                     " 	+ EOL +
				"3      4      3              i             0           b               /multiplicand                                     " 	+ EOL +
				"4      4      4              i             0           a               /multiplier                                       " 	+ EOL +
				"5      4      5              o             0           c               /product                                          " 	+ EOL +
				"6      5      6              i             0           v               /product                                          " 	+ EOL, 
			manager.dumpPortTable());

		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          2       o                        value        " 	+ EOL +
				"2          3       o                        v            " 	+ EOL +
				"3          4       i                        b            " 	+ EOL +
				"4          4       i                        a            " 	+ EOL +
				"5          4       o                        c            " 	+ EOL +
				"6          5       i                        v            " 	+ EOL, 
			manager.dumpActorVariableTable());

		assertEquals(
				"NodeVariableID NodeID ActorVariableID " 	+ EOL +
				"-------------- ------ --------------- " 	+ EOL +
				"1              2      1               " 	+ EOL +
				"2              3      2               " 	+ EOL +
				"3              4      3               " 	+ EOL +
				"4              4      4               " 	+ EOL +
				"5              4      5               " 	+ EOL +
				"6              5      6               " 	+ EOL, 
			manager.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 	+ EOL +
				"-------- --------- " 	+ EOL +
				"3        2         " 	+ EOL +
				"4        1         " 	+ EOL +
				"6        5         " 	+ EOL,
			manager.dumpChannelTable());
	}

	
	public void testLoadWorkflow_DoubleNested() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()
		
		.name("TopWF")
		.context(_context)
		
		.node(new JavaNodeBuilder()
			.name("TopLevelSequencer")
			.sequence("c", new Integer[] {5, 10, 15})
			.bean(new Object() {
				public int c, v;
				public void step() { v = c; }
			})
			.outflow("v", "/topmultiplier"))
			
		.node(new WorkflowNodeBuilder()
			.name("SubWF")
			.prefix("/Sub{RUN}/")
			.inflow("/topmultiplier", "/subinmultiplier")
			
			.node(new JavaNodeBuilder()
				.name("DoubleMultipliers")
				.inflow("/subinmultiplier", "c")
				.stepsOnce()
				.bean(new Object() {
					public int c, v;
					public void step() { v = 2 * c; }
				})
				.outflow("v", "/doubledmultiplier"))

			.node(new WorkflowNodeBuilder()
				.name("SubSubWF")
				.stepsOnce()
				.inflow("/doubledmultiplier", "/multiplier")
	
				.node(new JavaNodeBuilder()
					.name("CreateSequenceData")
					.sequence("c", new Integer[] {3, 8, 2})
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplicand"))
				
				.node(new JavaNodeBuilder()
					.name("MultiplySequenceBySingleton")
					.inflow("/multiplier", "a", InflowProperty.ReceiveOnce)
					.inflow("/multiplicand", "b")
					.bean(new Object() {
						public int a, b, c;
						public void step() { c = a * b; }
					})
					.outflow("c", "/product"))
	
				.node(new JavaNodeBuilder()
					.name("RenderProducts")
					.inflow("/product", "v")
					.bean(new Object() {
						public int v;
						public void step() { System.out.println(v); }
					}))
			)
		)
		
		.build();

		// create the database and get a manager for it
		Connection connection = WritableTrace.createPrivateVolatileDatabase();
		WritableTrace.createTraceDBTables(_context, connection);
		WritableTrace manager = new WritableTrace(connection);
		
		// load the workflow into the database
		manager.storeWorkflowGraph(workflow, null);
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       TopWF                                           " 		+ EOL +
				"2       TopLevelSequencer_actor                         " 		+ EOL +
				"3       SubWF                                           " 		+ EOL +
				"4       InPortal                                        " 		+ EOL +
				"5       DoubleMultipliers_actor                         " 		+ EOL +
				"6       SubSubWF                                        " 		+ EOL +
				"7       InPortal                                        " 		+ EOL +
				"8       CreateSequenceData_actor                        " 		+ EOL +
				"9       MultiplySequenceBySingleton_actor               " 		+ EOL +
				"10      RenderProducts_actor                            " 		+ EOL, 
			manager.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       0         TopWF                                              TopWF                           " 	+ EOL +
				"2      1            2       0         TopWF.TopLevelSequencer                            TopLevelSequencer               " 	+ EOL +
				"3      1            3       0         TopWF.SubWF                                        SubWF                           " 	+ EOL +
				"4      3            4       0         TopWF.SubWF.inportal                               inportal                        " 	+ EOL +
				"5      3            5       0         TopWF.SubWF.DoubleMultipliers                      DoubleMultipliers               " 	+ EOL +
				"6      3            6       0         TopWF.SubWF.SubSubWF                               SubSubWF                        " 	+ EOL +
				"7      6            7       0         TopWF.SubWF.SubSubWF.inportal                      inportal                        " 	+ EOL +
				"8      6            8       0         TopWF.SubWF.SubSubWF.CreateSequenceData            CreateSequenceData              " 	+ EOL +
				"9      6            9       0         TopWF.SubWF.SubSubWF.MultiplySequenceBySingleton   MultiplySequenceBySingleton     " 	+ EOL +
				"10     6            10      0         TopWF.SubWF.SubSubWF.RenderProducts                RenderProducts                  " 	+ EOL, 
			manager.dumpNodeTable());
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      2      1              o             0           v               /topmultiplier                                    " 	+ EOL +
				"2      3      2              i             0           Input0          /topmultiplier                                    " 	+ EOL +
				"3      4      3              o             0           Input0          /subinmultiplier                                  " 	+ EOL +
				"4      5      4              i             0           c               /subinmultiplier                                  " 	+ EOL +
				"5      5      5              o             0           v               /doubledmultiplier                                " 	+ EOL +
				"6      6      6              i             0           Input0          /doubledmultiplier                                " 	+ EOL +
				"7      7      7              o             0           Input0          /multiplier                                       " 	+ EOL +
				"8      8      8              o             0           v               /multiplicand                                     " 	+ EOL +
				"9      9      9              i             0           b               /multiplicand                                     " 	+ EOL +
				"10     9      10             i             0           a               /multiplier                                       " 	+ EOL +
				"11     9      11             o             0           c               /product                                          " 	+ EOL +
				"12     10     12             i             0           v               /product                                          " 	+ EOL, 
			manager.dumpPortTable());

		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          2       o                        v            " 	+ EOL +
				"2          3       i                        Input0       " 	+ EOL +
				"3          4       o                        Input0       " 	+ EOL +
				"4          5       i                        c            " 	+ EOL +
				"5          5       o                        v            " 	+ EOL +
				"6          6       i                        Input0       " 	+ EOL +
				"7          7       o                        Input0       " 	+ EOL +
				"8          8       o                        v            " 	+ EOL +
				"9          9       i                        b            " 	+ EOL +
				"10         9       i                        a            " 	+ EOL +
				"11         9       o                        c            " 	+ EOL +
				"12         10      i                        v            " 	+ EOL, 
			manager.dumpActorVariableTable());

		assertEquals(
				"NodeVariableID NodeID ActorVariableID " 		+ EOL +
				"-------------- ------ --------------- " 		+ EOL +
				"1              2      1               " 		+ EOL +
				"2              3      2               " 		+ EOL +
				"3              4      3               " 		+ EOL +
				"4              5      4               " 		+ EOL +
				"5              5      5               " 		+ EOL +
				"6              6      6               " 		+ EOL +
				"7              7      7               " 		+ EOL +
				"8              8      8               " 		+ EOL +
				"9              9      9               " 		+ EOL +
				"10             9      10              " 		+ EOL +
				"11             9      11              " 		+ EOL +
				"12             10     12              " 		+ EOL, 
			manager.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 		+ EOL +
				"-------- --------- " 		+ EOL +
				"2        1         " 		+ EOL +
				"4        3         " 		+ EOL +
				"6        5         " 		+ EOL +
				"9        8         " 		+ EOL +
				"10       7         " 		+ EOL +
				"12       11        " 		+ EOL,
			manager.dumpChannelTable());
	}
}
