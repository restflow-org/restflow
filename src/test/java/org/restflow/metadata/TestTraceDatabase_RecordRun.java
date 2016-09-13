package org.restflow.metadata;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.InflowProperty;
import org.restflow.metadata.Trace;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;


public class TestTraceDatabase_RecordRun extends RestFlowTestCase {

	private WorkflowContext 		_context;
	private ConsumableObjectStore	_store;
	
	public void setUp() throws Exception {
		super.setUp();
		
		_store = new ConsumableObjectStore();
		_context = new WorkflowContextBuilder() 
			.store(_store)
			.build();	
	}
	
	public void testRunWorkflow_TopLevelNodes_AnonymousActors() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

		.name("OneShotInflowWorkflow")
		.context(_context)
		
		.node(new JavaNodeBuilder()
			.name("CreateSequenceData")
			.sequence("c", new Integer[] {3, 8, 2})
			.bean(new Object() {
				public int c, v;
				public void step() { v = c; }
			})
			.outflow("v", "/{STEP}/multiplicand"))
		
		.node(new JavaNodeBuilder()
			.name("MultiplySequenceBySingleton")
			.constant("a", 5)
			.inflow("/{}/multiplicand", "b")
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
		
		Trace trace = _context.getTrace();

		workflow.configure();
		workflow.initialize();
		workflow.run();
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       OneShotInflowWorkflow                           " 		+ EOL +
				"2       CreateSequenceData_actor                        " 		+ EOL +
				"3       MultiplySequenceBySingleton_actor               " 		+ EOL +
				"4       RenderProducts_actor                            " 		+ EOL, 
			trace.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       1         OneShotInflowWorkflow                              OneShotInflowWorkflow           " 	+ EOL +
				"2      1            2       3         OneShotInflowWorkflow.CreateSequenceData           CreateSequenceData              " 	+ EOL +
				"3      1            3       3         OneShotInflowWorkflow.MultiplySequenceBySingleton  MultiplySequenceBySingleton     " 	+ EOL +
				"4      1            4       3         OneShotInflowWorkflow.RenderProducts               RenderProducts                  " 	+ EOL, 
			trace.dumpNodeTable());
		
		assertEquals(
				"rf_node(['OneShotInflowWorkflow'])."										+ EOL +
				"rf_node(['OneShotInflowWorkflow','CreateSequenceData'])."					+ EOL +
				"rf_node(['OneShotInflowWorkflow','MultiplySequenceBySingleton'])."			+ EOL +
				"rf_node(['OneShotInflowWorkflow','RenderProducts'])."						+ EOL,
			trace.getWorkflowNodesProlog());
		
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      2      1              o             3           v               /{STEP}/multiplicand                              " 	+ EOL +
				"2      3      2              i             3           b               /{}/multiplicand                                  " 	+ EOL +
				"3      3      3              o             3           c               /product                                          " 	+ EOL +
				"4      4      4              i             3           v               /product                                          " 	+ EOL, 
			trace.dumpPortTable());

		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          2       o                        v            " 	+ EOL +
				"2          3       i                        b            " 	+ EOL +
				"3          3       o                        c            " 	+ EOL +
				"4          4       i                        v            " 	+ EOL, 
			trace.dumpActorVariableTable());

		assertEquals(
				"NodeVariableID NodeID ActorVariableID " 	+ EOL +
				"-------------- ------ --------------- " 	+ EOL +
				"1              2      1               " 	+ EOL +
				"2              3      2               " 	+ EOL +
				"3              3      3               " 	+ EOL +
				"4              4      4               " 	+ EOL, 
			trace.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 	+ EOL +
				"-------- --------- " 	+ EOL +
				"2        1         " 	+ EOL +
				"4        3         " 	+ EOL,
			trace.dumpChannelTable());
		
		assertEquals(
				"PacketID OriginEventID " 		+ EOL +
				"-------- ------------- " 		+ EOL +
				"1        1             " 		+ EOL +
				"2        3             " 		+ EOL +
				"3        5             " 		+ EOL +
				"4        7             " 		+ EOL +
				"5        9             " 		+ EOL +
				"6        11            " 		+ EOL,
			trace.dumpPacketTable());
		
		assertMatchesRegexp(
				"StepID NodeID ParentStepID StepNumber UpdateCount StartTime              EndTime                " 		+ EOL +  
				"------ ------ ------------ ---------- ----------- ---------------------- ---------------------- " 		+ EOL +
				"1      1                   1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"2      2      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"3      3      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"4      4      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"5      2      1            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"6      3      1            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"7      4      1            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"8      2      1            3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"9      3      1            3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"10     4      1            3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpStepTable());

		assertMatchesRegexp(
				"PortEventID PortID PacketID StepID EventClass EventNumber EventTime              " 		+ EOL +
				"----------- ------ -------- ------ ---------- ----------- ---------------------- " 		+ EOL +
				"1           1      1        2      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"2           2      1        3      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"3           3      2        3      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"4           4      2        4      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"5           1      3        5      w          2           ....-..-.. ..:..:..... " 		+ EOL +
				"6           2      3        6      r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"7           3      4        6      w          2           ....-..-.. ..:..:..... " 		+ EOL +
				"8           4      4        7      r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"9           1      5        8      w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"10          2      5        9      r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"11          3      6        9      w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"12          4      6        10     r          3           ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpPortEventTable());

		assertEquals(
				"DataID IsReference DataTypeID Value                                                  " 		+ EOL +
				"------ ----------- ---------- ------------------------------------------------------ " 		+ EOL +
				"1      FALSE                  3                                                      " 		+ EOL +
				"2      FALSE                  1                                                      " 		+ EOL +
				"3      FALSE                  15                                                     " 		+ EOL +
				"4      FALSE                  8                                                      " 		+ EOL +
				"5      FALSE                  2                                                      " 		+ EOL +
				"6      FALSE                  40                                                     " 		+ EOL +
				"7      FALSE                  2                                                      " 		+ EOL +
				"8      FALSE                  3                                                      " 		+ EOL +
				"9      FALSE                  10                                                     " 		+ EOL,
			trace.dumpDataTable());

		assertEquals(
				"ResourceID DataID Uri                                                 " 		+ EOL +
				"---------- ------ --------------------------------------------------- " 		+ EOL +
				"1          1      /1/multiplicand                                     " 		+ EOL +
				"2          3      /product/1                                          " 		+ EOL +
				"3          4      /2/multiplicand                                     " 		+ EOL +
				"4          6      /product/2                                          " 		+ EOL +
				"5          7      /3/multiplicand                                     " 		+ EOL +
				"6          9      /product/3                                          " 		+ EOL,
			trace.dumpResourceTable());
		
		assertEquals(
				"PacketID ResourceID " 		+ EOL +
				"-------- ---------- " 		+ EOL +
				"1        1          " 		+ EOL +
				"2        2          " 		+ EOL +
				"3        3          " 		+ EOL +
				"4        4          " 		+ EOL +
				"5        5          " 		+ EOL +
				"6        6          " 		+ EOL,
			trace.dumpPacketResourceTable());

		assertEquals(
				"MetadataID PacketID Key           DataID " 		+ EOL +
				"---------- -------- ------------- ------ " 		+ EOL +
				"1          1        STEP          2      " 		+ EOL +
				"2          3        STEP          5      " 		+ EOL +
				"3          5        STEP          8      " 		+ EOL,
			trace.dumpPacketMetadataTable());
		
		assertEquals(
				"IsReference DataTypeID Uri                                                 Value                                               " 	+ EOL +
				"----------- ---------- --------------------------------------------------- --------------------------------------------------- " 	+ EOL +
				"FALSE                  /1/multiplicand                                     3                                                   " 	+ EOL +
				"FALSE                  /2/multiplicand                                     8                                                   " 	+ EOL +
				"FALSE                  /3/multiplicand                                     2                                                   " 	+ EOL +
				"FALSE                  /product/1                                          15                                                  " 	+ EOL +
				"FALSE                  /product/2                                          40                                                  " 	+ EOL +
				"FALSE                  /product/3                                          10                                                  " 	+ EOL,
			trace.dumpPublishedResourceView());		
	}
	
	public void testRunWorkflow_TopLevelNodes_WithWorkflowInputs() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

			.name("MultiplicationWorkflow")
			.context(_context)
		
			.inflow("a", "/multiplier")
			.inflow("b", "/multiplicand")
			
			.node(new JavaNodeBuilder()
				.name("multiplier")
				.inflow("/multiplier", "x")
				.inflow("/multiplicand", "y")
				.bean(new CloneableBean() {
					public int x, y, z;
					public void step() {z = x * y;}
					})
				.outflow("z", "/product")
				.stepsOnce())
				
			.node(new JavaNodeBuilder()
				.name("printer")
				.inflow("/product", "value")
				.bean(new Object() {
					public Object value;
					public void step() {System.out.println(value);}
				}))
				
			.outflow("/product", "c")
			
			.build();
		
		workflow.configure();
			
		Trace trace = _context.getTrace();

		workflow.initialize();
		workflow.set("a", 3);
		workflow.set("b", 5);
		workflow.run();
		assertEquals(15, workflow.get("c"));
		workflow.wrapup();
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       MultiplicationWorkflow                          " 		+ EOL +
				"2       InPortal                                        " 		+ EOL +
				"3       OutPortal                                       " 		+ EOL +
				"4       multiplier_actor                                " 		+ EOL +
				"5       printer_actor                                   " 		+ EOL, 
			trace.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       1         MultiplicationWorkflow                             MultiplicationWorkflow          " 	+ EOL +
				"2      1            2       1         MultiplicationWorkflow.inportal                    inportal                        " 	+ EOL +
				"3      1            3       1         MultiplicationWorkflow.outportal                   outportal                       " 	+ EOL +
				"4      1            4       1         MultiplicationWorkflow.multiplier                  multiplier                      " 	+ EOL +
				"5      1            5       1         MultiplicationWorkflow.printer                     printer                         " 	+ EOL, 
			trace.dumpNodeTable());
		
		assertEquals(
				"rf_node(['MultiplicationWorkflow'])."								+ EOL +
				"rf_node(['MultiplicationWorkflow','inportal'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','multiplier'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','outportal'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','printer'])."					+ EOL,
			trace.getWorkflowNodesProlog());
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      1      1              i             1           a                                                                 " 	+ EOL +
				"2      1      2              i             1           b                                                                 " 	+ EOL +
				"3      1      3              o             1           c                                                                 " 	+ EOL +
				"4      2      4              o             1           b               /multiplicand                                     " 	+ EOL +
				"5      2      5              o             1           a               /multiplier                                       " 	+ EOL +
				"6      3      6              i             1           c               /product                                          " 	+ EOL +
				"7      4      7              i             1           x               /multiplier                                       " 	+ EOL +
				"8      4      8              i             1           y               /multiplicand                                     " 	+ EOL +
				"9      4      9              o             1           z               /product                                          "		+ EOL +
				"10     5      10             i             1           value           /product                                          " 	+ EOL, 
			trace.dumpPortTable());
		
		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          1       i                        a            " 	+ EOL +
				"2          1       i                        b            " 	+ EOL +
				"3          1       o                        c            " 	+ EOL +
				"4          2       o                        b            " 	+ EOL +
				"5          2       o                        a            " 	+ EOL +
				"6          3       i                        c            " 	+ EOL +
				"7          4       i                        x            " 	+ EOL +
				"8          4       i                        y            " 	+ EOL +
				"9          4       o                        z            " 	+ EOL +
				"10         5       i                        value        " 	+ EOL,
			trace.dumpActorVariableTable());

		assertEquals(
				"NodeVariableID NodeID ActorVariableID " 	+ EOL +
				"-------------- ------ --------------- " 	+ EOL +
				"1              1      1               " 	+ EOL +
				"2              1      2               " 	+ EOL +
				"3              1      3               " 	+ EOL +
				"4              2      4               " 	+ EOL +
				"5              2      5               " 	+ EOL +
				"6              3      6               " 	+ EOL +
				"7              4      7               " 	+ EOL +
				"8              4      8               " 	+ EOL +
				"9              4      9               " 	+ EOL +
				"10             5      10              " 	+ EOL,
			trace.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 	+ EOL +
				"-------- --------- " 	+ EOL +
				"6        9         " 	+ EOL +
				"7        5         " 	+ EOL +
				"8        4         " 	+ EOL +
				"10       9         " 	+ EOL,
			trace.dumpChannelTable());
		
		assertEquals(
				"PacketID OriginEventID " 		+ EOL +
				"-------- ------------- " 		+ EOL +
				"1        1             " 		+ EOL +
				"2        2             " 		+ EOL +
				"3        3             " 		+ EOL +
				"4        4             " 		+ EOL +
				"5        7             " 		+ EOL +
				"6        10            " 		+ EOL,
			trace.dumpPacketTable());
		
		assertMatchesRegexp(
				"StepID NodeID ParentStepID StepNumber UpdateCount StartTime              EndTime                " 		+ EOL +  
				"------ ------ ------------ ---------- ----------- ---------------------- ---------------------- " 		+ EOL +
				"1      1                   1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"2      2      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"3      4      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"4      3      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"5      5      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpStepTable());

		assertMatchesRegexp(
				"PortEventID PortID PacketID StepID EventClass EventNumber EventTime              " 		+ EOL +
				"----------- ------ -------- ------ ---------- ----------- ---------------------- " 		+ EOL +
				"1           2      1        1      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"2           1      2        1      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"3           5      3        2      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"4           4      4        2      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"5           8      4        3      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"6           7      3        3      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"7           9      5        3      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"8           6      5        4      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"9           10     5        5      r          1           ....-..-.. ..:..:..... "			+ EOL +
				"10          3      6        1      w          1           ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpPortEventTable());
		
		assertEquals(
				"DataID IsReference DataTypeID Value                                                  " 		+ EOL +
				"------ ----------- ---------- ------------------------------------------------------ " 		+ EOL +
				"1      FALSE                  5                                                      " 		+ EOL +
				"2      FALSE                  3                                                      " 		+ EOL +
				"3      FALSE                  3                                                      " 		+ EOL +
				"4      FALSE                  5                                                      " 		+ EOL +
				"5      FALSE                  15                                                     " 		+ EOL +
				"6      FALSE                  15                                                     " 		+ EOL,
			trace.dumpDataTable());
		
		assertEquals(
				"ResourceID DataID Uri                                                 " 		+ EOL +
				"---------- ------ --------------------------------------------------- " 		+ EOL +
				"1          1                                                          " 		+ EOL +
				"2          2                                                          " 		+ EOL +
				"3          3      /multiplier                                         " 		+ EOL +
				"4          4      /multiplicand                                       " 		+ EOL +
				"5          5      /product                                            " 		+ EOL +
				"6          6                                                          " 		+ EOL,
			trace.dumpResourceTable());
		
		assertEquals(
				"PacketID ResourceID " 		+ EOL +
				"-------- ---------- " 		+ EOL +
				"1        1          " 		+ EOL +
				"2        2          " 		+ EOL +
				"3        3          " 		+ EOL +
				"4        4          " 		+ EOL +
				"5        5          " 		+ EOL +
				"6        6          " 		+ EOL,
			trace.dumpPacketResourceTable());
		
		assertEquals(
				"MetadataID PacketID Key           DataID " 		+ EOL +
				"---------- -------- ------------- ------ " 		+ EOL,
			trace.dumpPacketMetadataTable());
		
		assertEquals(
				"IsReference DataTypeID Uri                                                 Value                                               " 	+ EOL +
				"----------- ---------- --------------------------------------------------- --------------------------------------------------- " 	+ EOL +
				"FALSE                                                                      15                                                  "	+ EOL +
				"FALSE                  /multiplicand                                       5                                                   " 	+ EOL +
				"FALSE                  /multiplier                                         3                                                   " 	+ EOL +
				"FALSE                  /product                                            15                                                  " 	+ EOL,
			trace.dumpPublishedResourceView());

		assertEquals(
				"NodeName                                           StepNumber ParentNodeID ActorName               PortName IsReference DataTypeID Uri                                                 Value                                               " 	+ EOL +
				"-------------------------------------------------- ---------- ------------ ----------------------- -------- ----------- ---------- --------------------------------------------------- --------------------------------------------------- " 	+ EOL +
				"MultiplicationWorkflow                             1                       MultiplicationWorkflow  c        FALSE                                                                      15                                                  " 	+ EOL +
				"MultiplicationWorkflow.inportal                    1          1            InPortal                b        FALSE                  /multiplicand                                       5                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    1          1            InPortal                a        FALSE                  /multiplier                                         3                                                   " 	+ EOL +
				"MultiplicationWorkflow.multiplier                  1          1            multiplier_actor        z        FALSE                  /product                                            15                                                  " 	+ EOL,
			trace.dumpNodePublishedResourceView());

		assertEquals(
				"StepNumber PortName           Value                                               " 	+ EOL +
				"---------- ------------------ --------------------------------------------------- " 	+ EOL +
				"1          a                  3                                                   " 	+ EOL +
				"1          b                  5                                                   " 	+ EOL,
			trace.dumpWorkflowInputs());
		
		assertEquals(
				"StepNumber PortName           Value                                               " 	+ EOL +
				"---------- ------------------ --------------------------------------------------- " 	+ EOL +
				"1          c                  15                                                  " 	+ EOL,
			trace.dumpWorkflowOutputs());
	}
	
	public void testRunWorkflow_TopLevelNodes_WithWorkflowInputs_MultipleRuns_MultipleInitializations() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

			.name("MultiplicationWorkflow")
			.context(_context)
		
			.inflow("a", "/multiplier")
			.inflow("b", "/multiplicand")
			
			.node(new JavaNodeBuilder()
				.name("multiplier")
				.inflow("/multiplier", "x")
				.inflow("/multiplicand", "y")
				.bean(new CloneableBean() {
					public int x, y, z;
					public void step() {z = x * y;}
					})
				.outflow("z", "/product")
				.stepsOnce())
			
			.node(new JavaNodeBuilder()
				.name("printer")
				.inflow("/product", "value")
				.bean(new Object() {
					public Object value;
					public void step() {System.out.println(value);}
				}))
				
			.outflow("/product", "c")
			
			.build();
		
		workflow.configure();
		
		
		Trace trace = _context.getTrace();

		for (int i = 1; i <= 3; i++) {
			workflow.initialize();
			workflow.set("a", i);
			workflow.set("b", i * 3);
			workflow.run();
			assertEquals(3 * i * i, workflow.get("c"));
			workflow.wrapup();
		}
		
		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       MultiplicationWorkflow                          " 		+ EOL +
				"2       InPortal                                        " 		+ EOL +
				"3       OutPortal                                       " 		+ EOL +
				"4       multiplier_actor                                " 		+ EOL +
				"5       printer_actor                                   " 		+ EOL, 
			trace.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       3         MultiplicationWorkflow                             MultiplicationWorkflow          " 	+ EOL +
				"2      1            2       3         MultiplicationWorkflow.inportal                    inportal                        " 	+ EOL +
				"3      1            3       3         MultiplicationWorkflow.outportal                   outportal                       " 	+ EOL +
				"4      1            4       3         MultiplicationWorkflow.multiplier                  multiplier                      " 	+ EOL +
				"5      1            5       3         MultiplicationWorkflow.printer                     printer                         " 	+ EOL, 
			trace.dumpNodeTable());

		assertEquals(
				"rf_node(['MultiplicationWorkflow'])."								+ EOL +
				"rf_node(['MultiplicationWorkflow','inportal'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','multiplier'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','outportal'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','printer'])."					+ EOL,
			trace.getWorkflowNodesProlog());
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      1      1              i             3           a                                                                 " 	+ EOL +
				"2      1      2              i             3           b                                                                 " 	+ EOL +
				"3      1      3              o             3           c                                                                 " 	+ EOL +
				"4      2      4              o             5           b               /multiplicand                                     " 	+ EOL +
				"5      2      5              o             5           a               /multiplier                                       " 	+ EOL +
				"6      3      6              i             5           c               /product                                          " 	+ EOL +
				"7      4      7              i             5           x               /multiplier                                       " 	+ EOL +
				"8      4      8              i             5           y               /multiplicand                                     " 	+ EOL +
				"9      4      9              o             5           z               /product                                          " 	+ EOL +
				"10     5      10             i             5           value           /product                                          " 	+ EOL, 
			trace.dumpPortTable());
		
		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          1       i                        a            " 	+ EOL +
				"2          1       i                        b            " 	+ EOL +
				"3          1       o                        c            " 	+ EOL +
				"4          2       o                        b            " 	+ EOL +
				"5          2       o                        a            " 	+ EOL +
				"6          3       i                        c            " 	+ EOL +
				"7          4       i                        x            " 	+ EOL +
				"8          4       i                        y            " 	+ EOL +
				"9          4       o                        z            " 	+ EOL +
				"10         5       i                        value        " 	+ EOL,
			trace.dumpActorVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 	+ EOL +
				"-------- --------- " 	+ EOL +
				"6        9         " 	+ EOL +
				"7        5         " 	+ EOL +
				"8        4         " 	+ EOL +
				"10       9         " 	+ EOL,
			trace.dumpChannelTable());
		
		assertEquals(
				"PacketID OriginEventID " 		+ EOL +
				"-------- ------------- " 		+ EOL +
				"1        1             " 		+ EOL +
				"2        2             " 		+ EOL +
				"3        3             " 		+ EOL +
				"4        4             " 		+ EOL +
				"5        7             " 		+ EOL +
				"6        10            " 		+ EOL +
				"7        11            " 		+ EOL +
				"8        12            " 		+ EOL +
				"9        13            " 		+ EOL +
				"10       14            " 		+ EOL +
				"11       17            " 		+ EOL +
				"12       20            " 		+ EOL +
				"13       21            " 		+ EOL +
				"14       22            " 		+ EOL +
				"15       23            " 		+ EOL +
				"16       24            " 		+ EOL +
				"17       27            " 		+ EOL +
				"18       30            " 		+ EOL,

			trace.dumpPacketTable());
		
		assertMatchesRegexp(
				"StepID NodeID ParentStepID StepNumber UpdateCount StartTime              EndTime                " 		+ EOL +  
				"------ ------ ------------ ---------- ----------- ---------------------- ---------------------- " 		+ EOL +
				"1      1                   1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"2      2      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"3      4      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"4      3      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"5      5      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"6      1                   2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"7      2      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"8      4      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"9      3      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"10     5      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"11     1                   3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"12     2      11           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"13     4      11           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"14     3      11           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"15     5      11           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpStepTable());
		
		assertMatchesRegexp(
				"PortEventID PortID PacketID StepID EventClass EventNumber EventTime              " 		+ EOL +
				"----------- ------ -------- ------ ---------- ----------- ---------------------- " 		+ EOL +
				"1           2      1        1      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"2           1      2        1      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"3           5      3        2      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"4           4      4        2      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"5           8      4        3      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"6           7      3        3      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"7           9      5        3      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"8           6      5        4      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"9           10     5        5      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"10          3      6        1      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"11          2      7        6      r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"12          1      8        6      r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"13          5      9        7      w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"14          4      10       7      w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"15          8      10       8      r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"16          7      9        8      r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"17          9      11       8      w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"18          6      11       9      r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"19          10     11       10     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"20          3      12       6      w          2           ....-..-.. ..:..:..... " 		+ EOL +
				"21          2      13       11     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"22          1      14       11     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"23          5      15       12     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"24          4      16       12     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"25          8      16       13     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"26          7      15       13     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"27          9      17       13     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"28          6      17       14     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"29          10     17       15     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"30          3      18       11     w          3           ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpPortEventTable());
		
		assertEquals(
				"DataID IsReference DataTypeID Value                                                  " 		+ EOL +
				"------ ----------- ---------- ------------------------------------------------------ " 		+ EOL +
				"1      FALSE                  3                                                      " 		+ EOL +
				"2      FALSE                  1                                                      " 		+ EOL +
				"3      FALSE                  1                                                      " 		+ EOL +
				"4      FALSE                  3                                                      " 		+ EOL +
				"5      FALSE                  3                                                      " 		+ EOL +
				"6      FALSE                  3                                                      " 		+ EOL +
				"7      FALSE                  6                                                      " 		+ EOL +
				"8      FALSE                  2                                                      " 		+ EOL +
				"9      FALSE                  2                                                      " 		+ EOL +
				"10     FALSE                  6                                                      " 		+ EOL +
				"11     FALSE                  12                                                     " 		+ EOL +
				"12     FALSE                  12                                                     " 		+ EOL +
				"13     FALSE                  9                                                      " 		+ EOL +
				"14     FALSE                  3                                                      " 		+ EOL +
				"15     FALSE                  3                                                      " 		+ EOL +
				"16     FALSE                  9                                                      " 		+ EOL +
				"17     FALSE                  27                                                     " 		+ EOL +
				"18     FALSE                  27                                                     " 		+ EOL,
			trace.dumpDataTable());
		
		assertEquals(
				"ResourceID DataID Uri                                                 " 		+ EOL +
				"---------- ------ --------------------------------------------------- " 		+ EOL +
				"1          1                                                          " 		+ EOL +
				"2          2                                                          " 		+ EOL +
				"3          3      /multiplier                                         " 		+ EOL +
				"4          4      /multiplicand                                       " 		+ EOL +
				"5          5      /product                                            " 		+ EOL +
				"6          6                                                          " 		+ EOL +
				"7          7                                                          " 		+ EOL +
				"8          8                                                          " 		+ EOL +
				"9          9      /multiplier                                         " 		+ EOL +
				"10         10     /multiplicand                                       " 		+ EOL +
				"11         11     /product                                            " 		+ EOL +
				"12         12                                                         " 		+ EOL +
				"13         13                                                         " 		+ EOL +
				"14         14                                                         " 		+ EOL +
				"15         15     /multiplier                                         " 		+ EOL +
				"16         16     /multiplicand                                       " 		+ EOL +
				"17         17     /product                                            " 		+ EOL +
				"18         18                                                         " 		+ EOL,
			trace.dumpResourceTable());
		
		assertEquals(
				"PacketID ResourceID " 		+ EOL +
				"-------- ---------- " 		+ EOL +
				"1        1          " 		+ EOL +
				"2        2          " 		+ EOL +
				"3        3          " 		+ EOL +
				"4        4          " 		+ EOL +
				"5        5          " 		+ EOL +
				"6        6          " 		+ EOL +
				"7        7          " 		+ EOL +
				"8        8          " 		+ EOL +
				"9        9          " 		+ EOL +
				"10       10         " 		+ EOL +
				"11       11         " 		+ EOL +
				"12       12         " 		+ EOL +
				"13       13         " 		+ EOL +
				"14       14         " 		+ EOL +
				"15       15         " 		+ EOL +
				"16       16         " 		+ EOL +
				"17       17         " 		+ EOL +
				"18       18         " 		+ EOL,
			trace.dumpPacketResourceTable());
		
		assertEquals(
				"MetadataID PacketID Key           DataID " 		+ EOL +
				"---------- -------- ------------- ------ " 		+ EOL,
			trace.dumpPacketMetadataTable());
		
		assertEquals(
				"IsReference DataTypeID Uri                                                 Value                                               " 	+ EOL +
				"----------- ---------- --------------------------------------------------- --------------------------------------------------- " 	+ EOL +
				"FALSE                                                                      3                                                   "	+ EOL +
				"FALSE                                                                      12                                                  "	+ EOL +
				"FALSE                                                                      27                                                  "	+ EOL +
				"FALSE                  /multiplicand                                       3                                                   " 	+ EOL +
				"FALSE                  /multiplicand                                       6                                                   " 	+ EOL +
				"FALSE                  /multiplicand                                       9                                                   " 	+ EOL +
				"FALSE                  /multiplier                                         1                                                   " 	+ EOL +
				"FALSE                  /multiplier                                         2                                                   " 	+ EOL +
				"FALSE                  /multiplier                                         3                                                   " 	+ EOL +
				"FALSE                  /product                                            3                                                   " 	+ EOL +
				"FALSE                  /product                                            12                                                  " 	+ EOL +
				"FALSE                  /product                                            27                                                  " 	+ EOL,
			trace.dumpPublishedResourceView());

		assertEquals(
				"NodeName                                           StepNumber ParentNodeID ActorName               PortName IsReference DataTypeID Uri                                                 Value                                               " 	+ EOL +
				"-------------------------------------------------- ---------- ------------ ----------------------- -------- ----------- ---------- --------------------------------------------------- --------------------------------------------------- " 	+ EOL +
				"MultiplicationWorkflow                             1                       MultiplicationWorkflow  c        FALSE                                                                      3                                                   " 	+ EOL +
				"MultiplicationWorkflow                             2                       MultiplicationWorkflow  c        FALSE                                                                      12                                                  " 	+ EOL +
				"MultiplicationWorkflow                             3                       MultiplicationWorkflow  c        FALSE                                                                      27                                                  " 	+ EOL +
				"MultiplicationWorkflow.inportal                    1          1            InPortal                b        FALSE                  /multiplicand                                       3                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    2          1            InPortal                b        FALSE                  /multiplicand                                       6                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    3          1            InPortal                b        FALSE                  /multiplicand                                       9                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    1          1            InPortal                a        FALSE                  /multiplier                                         1                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    2          1            InPortal                a        FALSE                  /multiplier                                         2                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    3          1            InPortal                a        FALSE                  /multiplier                                         3                                                   " 	+ EOL +
				"MultiplicationWorkflow.multiplier                  1          1            multiplier_actor        z        FALSE                  /product                                            3                                                   " 	+ EOL +
				"MultiplicationWorkflow.multiplier                  2          1            multiplier_actor        z        FALSE                  /product                                            12                                                  " 	+ EOL +
				"MultiplicationWorkflow.multiplier                  3          1            multiplier_actor        z        FALSE                  /product                                            27                                                  " 	+ EOL,
			trace.dumpNodePublishedResourceView());

		assertEquals(
				"StepNumber PortName           Value                                               " 	+ EOL +
				"---------- ------------------ --------------------------------------------------- " 	+ EOL +
				"1          a                  1                                                   " 	+ EOL +
				"1          b                  3                                                   " 	+ EOL +
				"2          a                  2                                                   " 	+ EOL +
				"2          b                  6                                                   " 	+ EOL +
				"3          a                  3                                                   " 	+ EOL +
				"3          b                  9                                                   " 	+ EOL,
			trace.dumpWorkflowInputs());
		
		assertEquals(
				"StepNumber PortName           Value                                               " 	+ EOL +
				"---------- ------------------ --------------------------------------------------- " 	+ EOL +
				"1          c                  3                                                   " 	+ EOL +
				"2          c                  12                                                  " 	+ EOL +
				"3          c                  27                                                  " 	+ EOL,
			trace.dumpWorkflowOutputs());
	}
	
	public void testRunWorkflow_TopLevelNodes_WithWorkflowInputs_MultipleRuns_SingleInitialization() throws Exception {
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

			.name("MultiplicationWorkflow")
			.context(_context)
			.prefix("/Run{RUN}")
			.inflow("a", "/multiplier")
			.inflow("b", "/multiplicand")
			
			.node(new JavaNodeBuilder()
				.name("multiplier")
				.inflow("/multiplier", "x")
				.inflow("/multiplicand", "y")
				.bean(new CloneableBean() {
					public int x, y, z;
					public void step() {z = x * y;}
					})
				.outflow("z", "/product")
				.stepsOnce())
			
			.node(new JavaNodeBuilder()
				.name("printer")
				.inflow("/product", "value")
				.bean(new Object() {
					public Object value;
					public void step() {System.out.println(value);}
				}))
				
			.outflow("/product", "c")
			
			.build();
		
		workflow.configure();

		// initialize just once
		workflow.initialize();
		
		Trace trace = _context.getTrace();

		for (int i = 1; i <= 3; i++) {
			workflow.set("a", i);
			workflow.set("b", i * 3);
			workflow.run();
			assertEquals(3 * i * i, workflow.get("c"));
		}

		// wrapup once
		workflow.wrapup();

		assertEquals(
				"ActorId ActorName                                       " 		+ EOL +
				"------- ----------------------------------------------- " 		+ EOL +
				"1       MultiplicationWorkflow                          " 		+ EOL +
				"2       InPortal                                        " 		+ EOL +
				"3       OutPortal                                       " 		+ EOL +
				"4       multiplier_actor                                " 		+ EOL +
				"5       printer_actor                                   " 		+ EOL, 
			trace.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       3         MultiplicationWorkflow                             MultiplicationWorkflow          " 	+ EOL +
				"2      1            2       3         MultiplicationWorkflow.inportal                    inportal                        " 	+ EOL +
				"3      1            3       3         MultiplicationWorkflow.outportal                   outportal                       " 	+ EOL +
				"4      1            4       3         MultiplicationWorkflow.multiplier                  multiplier                      " 	+ EOL +
				"5      1            5       3         MultiplicationWorkflow.printer                     printer                         " 	+ EOL, 
			trace.dumpNodeTable());
		
		assertEquals(
				"rf_node(['MultiplicationWorkflow'])."								+ EOL +
				"rf_node(['MultiplicationWorkflow','inportal'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','multiplier'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','outportal'])."					+ EOL +
				"rf_node(['MultiplicationWorkflow','printer'])."					+ EOL,
			trace.getWorkflowNodesProlog());

		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      1      1              i             3           a                                                                 " 	+ EOL +
				"2      1      2              i             3           b                                                                 " 	+ EOL +
				"3      1      3              o             3           c                                                                 " 	+ EOL +
				"4      2      4              o             5           b               /multiplicand                                     " 	+ EOL +
				"5      2      5              o             5           a               /multiplier                                       " 	+ EOL +
				"6      3      6              i             5           c               /product                                          " 	+ EOL +
				"7      4      7              i             5           x               /multiplier                                       " 	+ EOL +
				"8      4      8              i             5           y               /multiplicand                                     " 	+ EOL +
				"9      4      9              o             5           z               /product                                          " 	+ EOL +
				"10     5      10             i             5           value           /product                                          " 	+ EOL, 
			trace.dumpPortTable());
		
		assertEquals(
				"VariableID ActorID VariableClass DataTypeID VariableName " 	+ EOL +
				"---------- ------- ------------- ---------- ------------ " 	+ EOL +
				"1          1       i                        a            " 	+ EOL +
				"2          1       i                        b            " 	+ EOL +
				"3          1       o                        c            " 	+ EOL +
				"4          2       o                        b            " 	+ EOL +
				"5          2       o                        a            " 	+ EOL +
				"6          3       i                        c            " 	+ EOL +
				"7          4       i                        x            " 	+ EOL +
				"8          4       i                        y            " 	+ EOL +
				"9          4       o                        z            " 	+ EOL +
				"10         5       i                        value        " 	+ EOL,
			trace.dumpActorVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 	+ EOL +
				"-------- --------- " 	+ EOL +
				"6        9         " 	+ EOL +
				"7        5         " 	+ EOL +
				"8        4         " 	+ EOL +
				"10       9         " 	+ EOL,
			trace.dumpChannelTable());
		
		assertEquals(
				"PacketID OriginEventID " 		+ EOL +
				"-------- ------------- " 		+ EOL +
				"1        1             " 		+ EOL +
				"2        2             " 		+ EOL +
				"3        3             " 		+ EOL +
				"4        4             " 		+ EOL +
				"5        7             " 		+ EOL +
				"6        10            " 		+ EOL +
				"7        11            " 		+ EOL +
				"8        12            " 		+ EOL +
				"9        13            " 		+ EOL +
				"10       14            " 		+ EOL +
				"11       17            " 		+ EOL +
				"12       20            " 		+ EOL +
				"13       21            " 		+ EOL +
				"14       22            " 		+ EOL +
				"15       23            " 		+ EOL +
				"16       24            " 		+ EOL +
				"17       27            " 		+ EOL +
				"18       30            " 		+ EOL,

			trace.dumpPacketTable());
		
		assertMatchesRegexp(
				"StepID NodeID ParentStepID StepNumber UpdateCount StartTime              EndTime                " 		+ EOL +  
				"------ ------ ------------ ---------- ----------- ---------------------- ---------------------- " 		+ EOL +
				"1      1                   1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"2      2      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"3      4      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"4      3      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"5      5      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"6      1                   2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"7      2      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"8      4      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"9      3      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"10     5      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"11     1                   3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"12     2      11           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"13     4      11           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"14     3      11           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"15     5      11           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpStepTable());
		
		assertMatchesRegexp(
				"PortEventID PortID PacketID StepID EventClass EventNumber EventTime              " 		+ EOL +
				"----------- ------ -------- ------ ---------- ----------- ---------------------- " 		+ EOL +
				"1           2      1        1      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"2           1      2        1      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"3           5      3        2      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"4           4      4        2      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"5           8      4        3      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"6           7      3        3      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"7           9      5        3      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"8           6      5        4      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"9           10     5        5      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"10          3      6        1      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"11          2      7        6      r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"12          1      8        6      r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"13          5      9        7      w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"14          4      10       7      w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"15          8      10       8      r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"16          7      9        8      r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"17          9      11       8      w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"18          6      11       9      r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"19          10     11       10     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"20          3      12       6      w          2           ....-..-.. ..:..:..... " 		+ EOL +
				"21          2      13       11     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"22          1      14       11     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"23          5      15       12     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"24          4      16       12     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"25          8      16       13     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"26          7      15       13     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"27          9      17       13     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"28          6      17       14     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"29          10     17       15     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"30          3      18       11     w          3           ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpPortEventTable());
		
		assertEquals(
				"DataID IsReference DataTypeID Value                                                  " 		+ EOL +
				"------ ----------- ---------- ------------------------------------------------------ " 		+ EOL +
				"1      FALSE                  3                                                      " 		+ EOL +
				"2      FALSE                  1                                                      " 		+ EOL +
				"3      FALSE                  1                                                      " 		+ EOL +
				"4      FALSE                  3                                                      " 		+ EOL +
				"5      FALSE                  3                                                      " 		+ EOL +
				"6      FALSE                  3                                                      " 		+ EOL +
				"7      FALSE                  6                                                      " 		+ EOL +
				"8      FALSE                  2                                                      " 		+ EOL +
				"9      FALSE                  2                                                      " 		+ EOL +
				"10     FALSE                  6                                                      " 		+ EOL +
				"11     FALSE                  12                                                     " 		+ EOL +
				"12     FALSE                  12                                                     " 		+ EOL +
				"13     FALSE                  9                                                      " 		+ EOL +
				"14     FALSE                  3                                                      " 		+ EOL +
				"15     FALSE                  3                                                      " 		+ EOL +
				"16     FALSE                  9                                                      " 		+ EOL +
				"17     FALSE                  27                                                     " 		+ EOL +
				"18     FALSE                  27                                                     " 		+ EOL,
			trace.dumpDataTable());
		
		assertEquals(
				"ResourceID DataID Uri                                                 " 		+ EOL +
				"---------- ------ --------------------------------------------------- " 		+ EOL +
				"1          1                                                          " 		+ EOL +
				"2          2                                                          " 		+ EOL +
				"3          3      /Run1/multiplier                                    " 		+ EOL +
				"4          4      /Run1/multiplicand                                  " 		+ EOL +
				"5          5      /Run1/product                                       " 		+ EOL +
				"6          6                                                          " 		+ EOL +
				"7          7                                                          " 		+ EOL +
				"8          8                                                          " 		+ EOL +
				"9          9      /Run2/multiplier                                    " 		+ EOL +
				"10         10     /Run2/multiplicand                                  " 		+ EOL +
				"11         11     /Run2/product                                       " 		+ EOL +
				"12         12                                                         " 		+ EOL +
				"13         13                                                         " 		+ EOL +
				"14         14                                                         " 		+ EOL +
				"15         15     /Run3/multiplier                                    " 		+ EOL +
				"16         16     /Run3/multiplicand                                  " 		+ EOL +
				"17         17     /Run3/product                                       " 		+ EOL +
				"18         18                                                         " 		+ EOL,
			trace.dumpResourceTable());
		
		assertEquals(
				"PacketID ResourceID " 		+ EOL +
				"-------- ---------- " 		+ EOL +
				"1        1          " 		+ EOL +
				"2        2          " 		+ EOL +
				"3        3          " 		+ EOL +
				"4        4          " 		+ EOL +
				"5        5          " 		+ EOL +
				"6        6          " 		+ EOL +
				"7        7          " 		+ EOL +
				"8        8          " 		+ EOL +
				"9        9          " 		+ EOL +
				"10       10         " 		+ EOL +
				"11       11         " 		+ EOL +
				"12       12         " 		+ EOL +
				"13       13         " 		+ EOL +
				"14       14         " 		+ EOL +
				"15       15         " 		+ EOL +
				"16       16         " 		+ EOL +
				"17       17         " 		+ EOL +
				"18       18         " 		+ EOL,
			trace.dumpPacketResourceTable());
		
		assertEquals(
				"MetadataID PacketID Key           DataID " 		+ EOL +
				"---------- -------- ------------- ------ " 		+ EOL,
			trace.dumpPacketMetadataTable());
		
		assertEquals(
				"IsReference DataTypeID Uri                                                 Value                                               " 	+ EOL +
				"----------- ---------- --------------------------------------------------- --------------------------------------------------- " 	+ EOL +
				"FALSE                                                                      3                                                   " 	+ EOL +
				"FALSE                                                                      12                                                  " 	+ EOL +
				"FALSE                                                                      27                                                  " 	+ EOL +
				"FALSE                  /Run1/multiplicand                                  3                                                   " 	+ EOL +
				"FALSE                  /Run1/multiplier                                    1                                                   " 	+ EOL +
				"FALSE                  /Run1/product                                       3                                                   " 	+ EOL +
				"FALSE                  /Run2/multiplicand                                  6                                                   " 	+ EOL +
				"FALSE                  /Run2/multiplier                                    2                                                   " 	+ EOL +
				"FALSE                  /Run2/product                                       12                                                  " 	+ EOL +
				"FALSE                  /Run3/multiplicand                                  9                                                   " 	+ EOL +
				"FALSE                  /Run3/multiplier                                    3                                                   " 	+ EOL +
				"FALSE                  /Run3/product                                       27                                                  " 	+ EOL,
			trace.dumpPublishedResourceView());

		assertEquals(
				"NodeName                                           StepNumber ParentNodeID ActorName               PortName IsReference DataTypeID Uri                                                 Value                                               " 	+ EOL +
				"-------------------------------------------------- ---------- ------------ ----------------------- -------- ----------- ---------- --------------------------------------------------- --------------------------------------------------- " 	+ EOL +
				"MultiplicationWorkflow                             1                       MultiplicationWorkflow  c        FALSE                                                                      3                                                   " 	+ EOL +
				"MultiplicationWorkflow                             2                       MultiplicationWorkflow  c        FALSE                                                                      12                                                  " 	+ EOL +
				"MultiplicationWorkflow                             3                       MultiplicationWorkflow  c        FALSE                                                                      27                                                  " 	+ EOL +
				"MultiplicationWorkflow.inportal                    1          1            InPortal                b        FALSE                  /Run1/multiplicand                                  3                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    1          1            InPortal                a        FALSE                  /Run1/multiplier                                    1                                                   " 	+ EOL +
				"MultiplicationWorkflow.multiplier                  1          1            multiplier_actor        z        FALSE                  /Run1/product                                       3                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    2          1            InPortal                b        FALSE                  /Run2/multiplicand                                  6                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    2          1            InPortal                a        FALSE                  /Run2/multiplier                                    2                                                   " 	+ EOL +
				"MultiplicationWorkflow.multiplier                  2          1            multiplier_actor        z        FALSE                  /Run2/product                                       12                                                  " 	+ EOL +
				"MultiplicationWorkflow.inportal                    3          1            InPortal                b        FALSE                  /Run3/multiplicand                                  9                                                   " 	+ EOL +
				"MultiplicationWorkflow.inportal                    3          1            InPortal                a        FALSE                  /Run3/multiplier                                    3                                                   " 	+ EOL +
				"MultiplicationWorkflow.multiplier                  3          1            multiplier_actor        z        FALSE                  /Run3/product                                       27                                                  " 	+ EOL,
			trace.dumpNodePublishedResourceView());

		assertEquals(
				"StepNumber PortName           Value                                               " 	+ EOL +
				"---------- ------------------ --------------------------------------------------- " 	+ EOL +
				"1          a                  1                                                   " 	+ EOL +
				"1          b                  3                                                   " 	+ EOL +
				"2          a                  2                                                   " 	+ EOL +
				"2          b                  6                                                   " 	+ EOL +
				"3          a                  3                                                   " 	+ EOL +
				"3          b                  9                                                   " 	+ EOL,
			trace.dumpWorkflowInputs());
		
		assertEquals(
				"StepNumber PortName           Value                                               " 	+ EOL +
				"---------- ------------------ --------------------------------------------------- " 	+ EOL +
				"1          c                  3                                                   " 	+ EOL +
				"2          c                  12                                                  " 	+ EOL +
				"3          c                  27                                                  " 	+ EOL,
			trace.dumpWorkflowOutputs());
	}
	
	
	public void testRunWorkflow_DoubleNested() throws Exception {
		
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
				.bean(new Object() {
					public int c, v;
					public void step() { v = 2 * c; }
				})
				.outflow("v", "/doubledmultiplier"))

			.node(new WorkflowNodeBuilder()
				.name("SubSubWF")
				.prefix("subsub{STEP}")
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

		Trace trace = _context.getTrace();
		
		workflow.configure();
		workflow.initialize();
		workflow.run();
		
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
			trace.dumpActorTable());

		assertEquals(
				"NodeId ParentNodeID ActorID StepCount NodeName                                           LocalNodeName                   " 	+ EOL +
				"------ ------------ ------- --------- -------------------------------------------------- ------------------------------- " 	+ EOL +
				"1                   1       1         TopWF                                              TopWF                           " 	+ EOL +
				"2      1            2       3         TopWF.TopLevelSequencer                            TopLevelSequencer               " 	+ EOL +
				"3      1            3       3         TopWF.SubWF                                        SubWF                           " 	+ EOL +
				"4      3            4       3         TopWF.SubWF.inportal                               inportal                        " 	+ EOL +
				"5      3            5       3         TopWF.SubWF.DoubleMultipliers                      DoubleMultipliers               " 	+ EOL +
				"6      3            6       3         TopWF.SubWF.SubSubWF                               SubSubWF                        " 	+ EOL +
				"7      6            7       3         TopWF.SubWF.SubSubWF.inportal                      inportal                        " 	+ EOL +
				"8      6            8       9         TopWF.SubWF.SubSubWF.CreateSequenceData            CreateSequenceData              " 	+ EOL +
				"9      6            9       9         TopWF.SubWF.SubSubWF.MultiplySequenceBySingleton   MultiplySequenceBySingleton     " 	+ EOL +
				"10     6            10      9         TopWF.SubWF.SubSubWF.RenderProducts                RenderProducts                  " 	+ EOL, 
			trace.dumpNodeTable());
		
		assertEquals(
				"rf_node(['TopWF'])."													+ EOL +
				"rf_node(['TopWF','SubWF'])."											+ EOL +
				"rf_node(['TopWF','SubWF','DoubleMultipliers'])."						+ EOL +
				"rf_node(['TopWF','SubWF','SubSubWF'])."								+ EOL +
				"rf_node(['TopWF','SubWF','SubSubWF','CreateSequenceData'])."			+ EOL +
				"rf_node(['TopWF','SubWF','SubSubWF','MultiplySequenceBySingleton'])."	+ EOL +
				"rf_node(['TopWF','SubWF','SubSubWF','RenderProducts'])."				+ EOL +
				"rf_node(['TopWF','SubWF','SubSubWF','inportal'])."						+ EOL +
				"rf_node(['TopWF','SubWF','inportal'])."								+ EOL +
				"rf_node(['TopWF','TopLevelSequencer'])."								+ EOL,
			trace.getWorkflowNodesProlog());
		
		assertEquals(
				"TopWF: 1"												+ EOL +
				"TopWF.SubWF: 3"										+ EOL +
				"TopWF.SubWF.DoubleMultipliers: 3"						+ EOL +
				"TopWF.SubWF.SubSubWF: 3"								+ EOL +
				"TopWF.SubWF.SubSubWF.CreateSequenceData: 9"			+ EOL +
				"TopWF.SubWF.SubSubWF.MultiplySequenceBySingleton: 9"	+ EOL +
				"TopWF.SubWF.SubSubWF.RenderProducts: 9"				+ EOL +
				"TopWF.SubWF.SubSubWF.inportal: 3"						+ EOL +
				"TopWF.SubWF.inportal: 3"								+ EOL +
				"TopWF.TopLevelSequencer: 3"							+ EOL,
			trace.getNodeStepCountsYaml());
		
		assertEquals(
				"PortId NodeID NodeVariableID PortDirection PacketCount PortName        UriTemplate                                       " 	+ EOL +
				"------ ------ -------------- ------------- ----------- --------------- ------------------------------------------------- " 	+ EOL +
				"1      2      1              o             3           v               /topmultiplier                                    " 	+ EOL +
				"2      3      2              i             3           Input0          /topmultiplier                                    " 	+ EOL +
				"3      4      3              o             5           Input0          /subinmultiplier                                  " 	+ EOL +
				"4      5      4              i             5           c               /subinmultiplier                                  " 	+ EOL +
				"5      5      5              o             5           v               /doubledmultiplier                                " 	+ EOL +
				"6      6      6              i             5           Input0          /doubledmultiplier                                " 	+ EOL +
				"7      7      7              o             5           Input0          /multiplier                                       " 	+ EOL +
				"8      8      8              o             11          v               /multiplicand                                     " 	+ EOL +
				"9      9      9              i             5           a               /multiplier                                       " 	+ EOL +
				"10     9      10             i             11          b               /multiplicand                                     " 	+ EOL +
				"11     9      11             o             11          c               /product                                          " 	+ EOL +
				"12     10     12             i             11          v               /product                                          " 	+ EOL, 
			trace.dumpPortTable());

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
				"9          9       i                        a            " 	+ EOL +
				"10         9       i                        b            " 	+ EOL +
				"11         9       o                        c            " 	+ EOL +
				"12         10      i                        v            " 	+ EOL, 
			trace.dumpActorVariableTable());

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
			trace.dumpNodeVariableTable());
		
		assertEquals(
				"InPortID OutPortID " 		+ EOL +
				"-------- --------- " 		+ EOL +
				"2        1         " 		+ EOL +
				"4        3         " 		+ EOL +
				"6        5         " 		+ EOL +
				"9        7         " 		+ EOL +
				"10       8         " 		+ EOL +
				"12       11        " 		+ EOL,
			trace.dumpChannelTable());

		assertEquals(
				"PacketID OriginEventID " 		+ EOL +
				"-------- ------------- " 		+ EOL +
				"1        1             " 		+ EOL +
				"2        3             " 		+ EOL +
				"3        5             " 		+ EOL +
				"4        7             " 		+ EOL +
				"5        9             " 		+ EOL +
				"6        11            " 		+ EOL +
				"7        13            " 		+ EOL +
				"8        15            " 		+ EOL +
				"9        17            " 		+ EOL +
				"10       19            " 		+ EOL +
				"11       21            " 		+ EOL +
				"12       23            " 		+ EOL +
				"13       25            " 		+ EOL +
				"14       27            " 		+ EOL +
				"15       29            " 		+ EOL +
				"16       31            " 		+ EOL +
				"17       33            " 		+ EOL +
				"18       35            " 		+ EOL +
				"19       37            " 		+ EOL +
				"20       39            " 		+ EOL +
				"21       41            " 		+ EOL +
				"22       43            " 		+ EOL +
				"23       45            " 		+ EOL +
				"24       47            " 		+ EOL +
				"25       49            " 		+ EOL +
				"26       51            " 		+ EOL +
				"27       53            " 		+ EOL +
				"28       55            " 		+ EOL +
				"29       57            " 		+ EOL +
				"30       59            " 		+ EOL,
			trace.dumpPacketTable());
		
		assertMatchesRegexp(
				"StepID NodeID ParentStepID StepNumber UpdateCount StartTime              EndTime                " 		+ EOL +  
				"------ ------ ------------ ---------- ----------- ---------------------- ---------------------- " 		+ EOL +
				"1      1                   1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"2      2      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"3      3      1            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"4      4      3            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"5      5      3            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"6      6      3            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"7      7      6            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"8      8      6            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"9      9      6            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"10     10     6            1          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"11     8      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"12     9      6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"13     10     6            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"14     8      6            3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"15     9      6            3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"16     10     6            3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"17     2      1            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"18     3      1            2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"19     4      18           2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"20     5      18           2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"21     6      18           2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"22     7      21           2          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"23     8      21           4          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"24     9      21           4          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"25     10     21           4          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"26     8      21           5          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"27     9      21           5          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"28     10     21           5          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"29     8      21           6          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"30     9      21           6          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"31     10     21           6          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"32     2      1            3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"33     3      1            3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"34     4      33           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"35     5      33           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"36     6      33           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"37     7      36           3          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"38     8      36           7          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"39     9      36           7          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"40     10     36           7          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"41     8      36           8          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"42     9      36           8          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"43     10     36           8          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"44     8      36           9          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"45     9      36           9          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL +
				"46     10     36           9          0           ....-..-.. ..:..:..... ....-..-.. ..:..:..... " 		+ EOL,
			trace.dumpStepTable());

		
		
		
		assertMatchesRegexp(
				"PortEventID PortID PacketID StepID EventClass EventNumber EventTime              " 		+ EOL +
				"----------- ------ -------- ------ ---------- ----------- ---------------------- " 		+ EOL +
				"1           1      1        2      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"2           2      1        3      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"3           3      2        4      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"4           4      2        5      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"5           5      3        5      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"6           6      3        6      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"7           7      4        7      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"8           9      4        9      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"9           8      5        8      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"10          10     5        9      r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"11          11     6        9      w          1           ....-..-.. ..:..:..... " 		+ EOL +
				"12          12     6        10     r          1           ....-..-.. ..:..:..... " 		+ EOL +
				"13          8      7        11     w          2           ....-..-.. ..:..:..... " 		+ EOL +
				"14          10     7        12     r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"15          11     8        12     w          2           ....-..-.. ..:..:..... " 		+ EOL +
				"16          12     8        13     r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"17          8      9        14     w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"18          10     9        15     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"19          11     10       15     w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"20          12     10       16     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"21          1      11       17     w          2           ....-..-.. ..:..:..... " 		+ EOL +
				"22          2      11       18     r          2           ....-..-.. ..:..:..... " 		+ EOL +
				"23          3      12       19     w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"24          4      12       20     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"25          5      13       20     w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"26          6      13       21     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"27          7      14       22     w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"28          9      14       24     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"29          8      15       23     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"30          10     15       24     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"31          11     16       24     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"32          12     16       25     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"33          8      17       26     w          6           ....-..-.. ..:..:..... " 		+ EOL +
				"34          10     17       27     r          6           ....-..-.. ..:..:..... " 		+ EOL +
				"35          11     18       27     w          6           ....-..-.. ..:..:..... " 		+ EOL +
				"36          12     18       28     r          6           ....-..-.. ..:..:..... " 		+ EOL +
				"37          8      19       29     w          7           ....-..-.. ..:..:..... " 		+ EOL +
				"38          10     19       30     r          7           ....-..-.. ..:..:..... " 		+ EOL +
				"39          11     20       30     w          7           ....-..-.. ..:..:..... " 		+ EOL +
				"40          12     20       31     r          7           ....-..-.. ..:..:..... " 		+ EOL +
				"41          1      21       32     w          3           ....-..-.. ..:..:..... " 		+ EOL +
				"42          2      21       33     r          3           ....-..-.. ..:..:..... " 		+ EOL +
				"43          3      22       34     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"44          4      22       35     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"45          5      23       35     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"46          6      23       36     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"47          7      24       37     w          5           ....-..-.. ..:..:..... " 		+ EOL +
				"48          9      24       39     r          5           ....-..-.. ..:..:..... " 		+ EOL +
				"49          8      25       38     w          9           ....-..-.. ..:..:..... " 		+ EOL +
				"50          10     25       39     r          9           ....-..-.. ..:..:..... " 		+ EOL +
				"51          11     26       39     w          9           ....-..-.. ..:..:..... " 		+ EOL +
				"52          12     26       40     r          9           ....-..-.. ..:..:..... " 		+ EOL +
				"53          8      27       41     w          10          ....-..-.. ..:..:..... " 		+ EOL +
				"54          10     27       42     r          10          ....-..-.. ..:..:..... " 		+ EOL +
				"55          11     28       42     w          10          ....-..-.. ..:..:..... " 		+ EOL +
				"56          12     28       43     r          10          ....-..-.. ..:..:..... " 		+ EOL +
				"57          8      29       44     w          11          ....-..-.. ..:..:..... " 		+ EOL +
				"58          10     29       45     r          11          ....-..-.. ..:..:..... " 		+ EOL +
				"59          11     30       45     w          11          ....-..-.. ..:..:..... " 		+ EOL +
				"60          12     30       46     r          11          ....-..-.. ..:..:..... " 		+ EOL,
		trace.dumpPortEventTable());

		assertEquals(
				"DataID IsReference DataTypeID Value                                                  " 		+ EOL +
				"------ ----------- ---------- ------------------------------------------------------ " 		+ EOL +
				"1      FALSE                  5                                                      " 		+ EOL +
				"2      FALSE                  5                                                      " 		+ EOL +
				"3      FALSE                  10                                                     " 		+ EOL +
				"4      FALSE                  10                                                     " 		+ EOL +
				"5      FALSE                  3                                                      " 		+ EOL +
				"6      FALSE                  30                                                     " 		+ EOL +
				"7      FALSE                  8                                                      " 		+ EOL +
				"8      FALSE                  80                                                     " 		+ EOL +
				"9      FALSE                  2                                                      " 		+ EOL +
				"10     FALSE                  20                                                     " 		+ EOL +
				"11     FALSE                  10                                                     " 		+ EOL +
				"12     FALSE                  10                                                     " 		+ EOL +
				"13     FALSE                  20                                                     " 		+ EOL +
				"14     FALSE                  20                                                     " 		+ EOL +
				"15     FALSE                  3                                                      " 		+ EOL +
				"16     FALSE                  60                                                     " 		+ EOL +
				"17     FALSE                  8                                                      " 		+ EOL +
				"18     FALSE                  160                                                    " 		+ EOL +
				"19     FALSE                  2                                                      " 		+ EOL +
				"20     FALSE                  40                                                     " 		+ EOL +
				"21     FALSE                  15                                                     " 		+ EOL +
				"22     FALSE                  15                                                     " 		+ EOL +
				"23     FALSE                  30                                                     " 		+ EOL +
				"24     FALSE                  30                                                     " 		+ EOL +
				"25     FALSE                  3                                                      " 		+ EOL +
				"26     FALSE                  90                                                     " 		+ EOL +
				"27     FALSE                  8                                                      " 		+ EOL +
				"28     FALSE                  240                                                    " 		+ EOL +
				"29     FALSE                  2                                                      " 		+ EOL +
				"30     FALSE                  60                                                     " 		+ EOL,
			trace.dumpDataTable());

		assertEquals(
				"ResourceID DataID Uri                                                 " 		+ EOL +
				"---------- ------ --------------------------------------------------- " 		+ EOL +
				"1          1      /topmultiplier/1                                    " 		+ EOL +
				"2          2      /Sub1/subinmultiplier                               " 		+ EOL +
				"3          3      /Sub1/doubledmultiplier/1                           " 		+ EOL +
				"4          4      /Sub1subsub1/multiplier                             " 		+ EOL +
				"5          5      /Sub1subsub1/multiplicand/1                         " 		+ EOL +
				"6          6      /Sub1subsub1/product/1                              " 		+ EOL +
				"7          7      /Sub1subsub1/multiplicand/2                         " 		+ EOL +
				"8          8      /Sub1subsub1/product/2                              " 		+ EOL +
				"9          9      /Sub1subsub1/multiplicand/3                         " 		+ EOL +
				"10         10     /Sub1subsub1/product/3                              " 		+ EOL +
				"11         11     /topmultiplier/2                                    " 		+ EOL +
				"12         12     /Sub2/subinmultiplier                               " 		+ EOL +
				"13         13     /Sub2/doubledmultiplier/1                           " 		+ EOL +
				"14         14     /Sub2subsub1/multiplier                             " 		+ EOL +
				"15         15     /Sub2subsub1/multiplicand/1                         " 		+ EOL +
				"16         16     /Sub2subsub1/product/1                              " 		+ EOL +
				"17         17     /Sub2subsub1/multiplicand/2                         " 		+ EOL +
				"18         18     /Sub2subsub1/product/2                              " 		+ EOL +
				"19         19     /Sub2subsub1/multiplicand/3                         " 		+ EOL +
				"20         20     /Sub2subsub1/product/3                              " 		+ EOL +
				"21         21     /topmultiplier/3                                    " 		+ EOL +
				"22         22     /Sub3/subinmultiplier                               " 		+ EOL +
				"23         23     /Sub3/doubledmultiplier/1                           " 		+ EOL +
				"24         24     /Sub3subsub1/multiplier                             " 		+ EOL +
				"25         25     /Sub3subsub1/multiplicand/1                         " 		+ EOL +
				"26         26     /Sub3subsub1/product/1                              " 		+ EOL +
				"27         27     /Sub3subsub1/multiplicand/2                         " 		+ EOL +
				"28         28     /Sub3subsub1/product/2                              " 		+ EOL +
				"29         29     /Sub3subsub1/multiplicand/3                         " 		+ EOL +
				"30         30     /Sub3subsub1/product/3                              " 		+ EOL,
			trace.dumpResourceTable());
		
		assertEquals(
				"PacketID ResourceID " 		+ EOL +
				"-------- ---------- " 		+ EOL +
				"1        1          " 		+ EOL +
				"2        2          " 		+ EOL +
				"3        3          " 		+ EOL +
				"4        4          " 		+ EOL +
				"5        5          " 		+ EOL +
				"6        6          " 		+ EOL +
				"7        7          " 		+ EOL +
				"8        8          " 		+ EOL +
				"9        9          " 		+ EOL +
				"10       10         " 		+ EOL +
				"11       11         " 		+ EOL +
				"12       12         " 		+ EOL +
				"13       13         " 		+ EOL +
				"14       14         " 		+ EOL +
				"15       15         " 		+ EOL +
				"16       16         " 		+ EOL +
				"17       17         " 		+ EOL +
				"18       18         " 		+ EOL +
				"19       19         " 		+ EOL +
				"20       20         " 		+ EOL +
				"21       21         " 		+ EOL +
				"22       22         " 		+ EOL +
				"23       23         " 		+ EOL +
				"24       24         " 		+ EOL +
				"25       25         " 		+ EOL +
				"26       26         " 		+ EOL +
				"27       27         " 		+ EOL +
				"28       28         " 		+ EOL +
				"29       29         " 		+ EOL +
				"30       30         " 		+ EOL,
			trace.dumpPacketResourceTable());

		assertEquals(
				"IsReference DataTypeID Uri                                                 Value                                               " 	+ EOL +
				"----------- ---------- --------------------------------------------------- --------------------------------------------------- " 	+ EOL +
				"FALSE                  /Sub1/doubledmultiplier/1                           10                                                  " 	+ EOL +
				"FALSE                  /Sub1/subinmultiplier                               5                                                   " 	+ EOL +
				"FALSE                  /Sub1subsub1/multiplicand/1                         3                                                   " 	+ EOL +
				"FALSE                  /Sub1subsub1/multiplicand/2                         8                                                   " 	+ EOL +
				"FALSE                  /Sub1subsub1/multiplicand/3                         2                                                   " 	+ EOL +
				"FALSE                  /Sub1subsub1/multiplier                             10                                                  " 	+ EOL +
				"FALSE                  /Sub1subsub1/product/1                              30                                                  " 	+ EOL +
				"FALSE                  /Sub1subsub1/product/2                              80                                                  " 	+ EOL +
				"FALSE                  /Sub1subsub1/product/3                              20                                                  " 	+ EOL +
				"FALSE                  /Sub2/doubledmultiplier/1                           20                                                  " 	+ EOL +
				"FALSE                  /Sub2/subinmultiplier                               10                                                  " 	+ EOL +
				"FALSE                  /Sub2subsub1/multiplicand/1                         3                                                   " 	+ EOL +
				"FALSE                  /Sub2subsub1/multiplicand/2                         8                                                   " 	+ EOL +
				"FALSE                  /Sub2subsub1/multiplicand/3                         2                                                   " 	+ EOL +
				"FALSE                  /Sub2subsub1/multiplier                             20                                                  " 	+ EOL +
				"FALSE                  /Sub2subsub1/product/1                              60                                                  " 	+ EOL +
				"FALSE                  /Sub2subsub1/product/2                              160                                                 " 	+ EOL +
				"FALSE                  /Sub2subsub1/product/3                              40                                                  " 	+ EOL +
				"FALSE                  /Sub3/doubledmultiplier/1                           30                                                  " 	+ EOL +
				"FALSE                  /Sub3/subinmultiplier                               15                                                  " 	+ EOL +
				"FALSE                  /Sub3subsub1/multiplicand/1                         3                                                   " 	+ EOL +
				"FALSE                  /Sub3subsub1/multiplicand/2                         8                                                   " 	+ EOL +
				"FALSE                  /Sub3subsub1/multiplicand/3                         2                                                   " 	+ EOL +
				"FALSE                  /Sub3subsub1/multiplier                             30                                                  " 	+ EOL +
				"FALSE                  /Sub3subsub1/product/1                              90                                                  " 	+ EOL +
				"FALSE                  /Sub3subsub1/product/2                              240                                                 " 	+ EOL +
				"FALSE                  /Sub3subsub1/product/3                              60                                                  " 	+ EOL +
				"FALSE                  /topmultiplier/1                                    5                                                   " 	+ EOL +
				"FALSE                  /topmultiplier/2                                    10                                                  " 	+ EOL +
				"FALSE                  /topmultiplier/3                                    15                                                  " 	+ EOL,
			trace.dumpPublishedResourceView());
	}
}
