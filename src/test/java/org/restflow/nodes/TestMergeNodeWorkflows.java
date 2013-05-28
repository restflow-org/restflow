package org.restflow.nodes;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.ActorStatus;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.directors.Director;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.MergeNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestMergeNodeWorkflows extends RestFlowTestCase {
	
	private WorkflowContext			_context;
	private ConsumableObjectStore 	_store;
	
	private ActorNodeBuilder 		_triggerNodeBuilder;
	private ActorNodeBuilder		_sequenceOneNodeBuilder;
	private ActorNodeBuilder		_sequenceTwoNodeBuilder;
	private ActorNodeBuilder		_sequenceThreeNodeBuilder;
	private MergeNodeBuilder		_mergeNodeBuilder;
	private ActorNodeBuilder		_printerNodeBuilder;
	
	public void setUp() throws Exception {
		super.setUp();
	}
	
	@SuppressWarnings("unused")
	private void _createMergeIntegerStreamBuilders() throws Exception {
		
		_store = new ConsumableObjectStore();	 

		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();
			
		_triggerNodeBuilder = new JavaNodeBuilder()
			.name("trigger")
			.context(_context)
			.sequence("constant", new Object [] {"A", "B", "C"})
			.bean(new Object() {
				public String value, constant;
				public void step() {value = constant;}
			})
			.outflow("value", "/trigger");	

		
		_sequenceOneNodeBuilder = new JavaNodeBuilder()
			.name("GenerateSequence1")
			.sequence("c", new Object [] {
					2, 
					4, 
					6, 
					8})
			.bean(new Object() {
				public int value, c;
				public void step() { value = c; }
			})
			.outflow("value", "/sequenceOne");

		_sequenceTwoNodeBuilder = new JavaNodeBuilder()
			.name("GenerateSequence2")
			.sequence("c", new Object [] {
					1, 
					3, 
					5})
			.bean(new Object() {
				public int value, c;
				public void step() { value = c; }
			})
			.outflow("value", "/sequenceTwo");
		
		_sequenceThreeNodeBuilder = new JavaNodeBuilder()
			.name("GenerateSequence3")
			.sequence("c", new Object [] {
					13, 
					75, 
					60, 
					15, 
					20})
			.bean(new Object() {
				public int value, c;
				public void step() { value = c; }
			})
			.outflow("value", "/sequenceThree");

		_mergeNodeBuilder = new MergeNodeBuilder()
			.name("MergeNode")
			.inflow("/sequenceOne")
			.inflow("/sequenceTwo")
			.inflow("/sequenceThree")
			.outflow("/merged");

		_printerNodeBuilder = new JavaNodeBuilder()
			.name("Printer")
			.inflow("/merged", "value")
			.bean(new Object() {
				public Object value;
				public void step() { System.out.println(value); }
			});
	}

	public void testMergeIntegerStreams_PublishSubscribeDirector() throws Exception {
		
		_createMergeIntegerStreamBuilders();
		
		final Workflow workflow = new WorkflowBuilder()
			.name("MergeIntegerStreams")
			.context(_context)
			.director(new PublishSubscribeDirector())
			.node(_sequenceOneNodeBuilder)
			.node(_sequenceTwoNodeBuilder)
			.node(_sequenceThreeNodeBuilder)
			.node(_mergeNodeBuilder)
			.node(_printerNodeBuilder)
			.build();
		
		workflow.configure();
		workflow.initialize();

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
			}
		});

		assertEquals(
				"2" 	+ EOL +
				"1"		+ EOL +
				"13" 	+ EOL +
				"4" 	+ EOL +
				"3" 	+ EOL +
				"75" 	+ EOL +
				"6" 	+ EOL +
				"5" 	+ EOL +
				"60" 	+ EOL +
				"8" 	+ EOL +
				"15" 	+ EOL +
				"20"	+ EOL, 
				recorder.getStdoutRecording());
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(2, 	_store.take("/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sequenceThree/5"));
		assertEquals(2, 	_store.take("/merged/1"));
		assertEquals(1, 	_store.take("/merged/2"));
		assertEquals(13, 	_store.take("/merged/3"));
		assertEquals(4, 	_store.take("/merged/4"));
		assertEquals(3, 	_store.take("/merged/5"));
		assertEquals(75, 	_store.take("/merged/6"));
		assertEquals(6, 	_store.take("/merged/7"));
		assertEquals(5, 	_store.take("/merged/8"));
		assertEquals(60, 	_store.take("/merged/9"));
		assertEquals(8, 	_store.take("/merged/10"));
		assertEquals(15, 	_store.take("/merged/11"));
		assertEquals(20, 	_store.take("/merged/12"));

		assertEquals(0, _store.size());
	}
	
	public void testMergeIntegerStreams_DataDrivenDirector() throws Exception {
		
		_createMergeIntegerStreamBuilders();
		
		final Workflow workflow = new WorkflowBuilder()
			.name("MergeIntegerStreams")
			.context(_context)
			.director(new DataDrivenDirector())
			.node(_sequenceOneNodeBuilder)
			.node(_sequenceTwoNodeBuilder)
			.node(_sequenceThreeNodeBuilder)
			.node(_mergeNodeBuilder)
			.node(_printerNodeBuilder)
			.build();
		
		workflow.configure();
		workflow.initialize();

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
			}
		});

		assertEquals(
				"2" 	+ EOL +
				"4" 	+ EOL +
				"6" 	+ EOL +
				"8" 	+ EOL +
				"13" 	+ EOL +
				"75" 	+ EOL +
				"60" 	+ EOL +
				"15" 	+ EOL +
				"20"	+ EOL + 
				"1"		+ EOL +
				"3" 	+ EOL +
				"5" 	+ EOL,
				recorder.getStdoutRecording());
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(2, 	_store.take("/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sequenceThree/5"));
		assertEquals(2, 	_store.take("/merged/1"));
		assertEquals(4, 	_store.take("/merged/2"));
		assertEquals(6, 	_store.take("/merged/3"));
		assertEquals(8, 	_store.take("/merged/4"));
		assertEquals(13, 	_store.take("/merged/5"));
		assertEquals(75, 	_store.take("/merged/6"));
		assertEquals(60, 	_store.take("/merged/7"));
		assertEquals(15, 	_store.take("/merged/8"));
		assertEquals(20, 	_store.take("/merged/9"));
		assertEquals(1, 	_store.take("/merged/10"));
		assertEquals(3, 	_store.take("/merged/11"));
		assertEquals(5, 	_store.take("/merged/12"));

		assertEquals(0, _store.size());
	}

	public void testMergeIntegerStreams_MTDataDrivenDirector() throws Exception {

		_createMergeIntegerStreamBuilders();
		
		final Workflow workflow = new WorkflowBuilder()
			.name("MergeIntegerStreams")
			.context(_context)
			.director(new MTDataDrivenDirector())
			.node(_sequenceOneNodeBuilder)
			.node(_sequenceTwoNodeBuilder)
			.node(_sequenceThreeNodeBuilder)
			.node(_mergeNodeBuilder)
			.node(_printerNodeBuilder)
			.build();
		
		workflow.configure();
		workflow.initialize();

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
			}
		});

		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(24, _store.size());

		assertEquals(2, 	_store.take("/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sequenceThree/5"));
		
		assertEquals(12, _store.size());

		_store.removeValue(2);
		_store.removeValue(4);
		_store.removeValue(6);
		_store.removeValue(8);
		_store.removeValue(1);
		_store.removeValue(3);
		_store.removeValue(5);
		_store.removeValue(13);
		_store.removeValue(75);
		_store.removeValue(60);
		_store.removeValue(15);
		_store.removeValue(20);

		assertEquals(0, _store.size());
	}
	
	public void testMergeIntegerStreams_DoublyNested_PublishSubscribeDirector() throws Exception {

		_createMergeIntegerStreamBuilders();

		final Workflow workflow = new WorkflowBuilder()
			.name("MergeIntegerStreams")
			.context(_context)
			.director(new PublishSubscribeDirector())
			.node(new WorkflowNodeBuilder()
				.director(new PublishSubscribeDirector())
				.prefix("/sub{STEP}")
				.node(_triggerNodeBuilder)
				.node(new WorkflowNodeBuilder()
				.prefix("/subsub{STEP}")
					.director(new PublishSubscribeDirector())
					.inflow("/trigger", "/discard")
					.node(_sequenceOneNodeBuilder)
					.node(_sequenceTwoNodeBuilder)
					.node(_sequenceThreeNodeBuilder)
					.node(_mergeNodeBuilder)
					.node(_printerNodeBuilder)
					)
			)
			.build();
		
		workflow.configure();
		workflow.initialize();

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
			}
		});

		assertEquals(
				"2" 	+ EOL +
				"1"		+ EOL +
				"13" 	+ EOL +
				"4" 	+ EOL +
				"3" 	+ EOL +
				"75" 	+ EOL +
				"6" 	+ EOL +
				"5" 	+ EOL +
				"60" 	+ EOL +
				"8" 	+ EOL +
				"15" 	+ EOL +
				"20"	+ EOL +
				"2" 	+ EOL +
				"1"		+ EOL +
				"13" 	+ EOL +
				"4" 	+ EOL +
				"3" 	+ EOL +
				"75" 	+ EOL +
				"6" 	+ EOL +
				"5" 	+ EOL +
				"60" 	+ EOL +
				"8" 	+ EOL +
				"15" 	+ EOL +
				"20"	+ EOL +
				"2" 	+ EOL +
				"1"		+ EOL +
				"13" 	+ EOL +
				"4" 	+ EOL +
				"3" 	+ EOL +
				"75" 	+ EOL +
				"6" 	+ EOL +
				"5" 	+ EOL +
				"60" 	+ EOL +
				"8" 	+ EOL +
				"15" 	+ EOL +
				"20"	+ EOL, 
				recorder.getStdoutRecording());
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(2, 	_store.take("/sub1/subsub1/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub1/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub1/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub1/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub1/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub1/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub1/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub1/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub1/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub1/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub1/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub1/sequenceThree/5"));
		assertEquals(2, 	_store.take("/sub1/subsub1/merged/1"));
		assertEquals(1, 	_store.take("/sub1/subsub1/merged/2"));
		assertEquals(13, 	_store.take("/sub1/subsub1/merged/3"));
		assertEquals(4, 	_store.take("/sub1/subsub1/merged/4"));
		assertEquals(3, 	_store.take("/sub1/subsub1/merged/5"));
		assertEquals(75, 	_store.take("/sub1/subsub1/merged/6"));
		assertEquals(6, 	_store.take("/sub1/subsub1/merged/7"));
		assertEquals(5, 	_store.take("/sub1/subsub1/merged/8"));
		assertEquals(60, 	_store.take("/sub1/subsub1/merged/9"));
		assertEquals(8, 	_store.take("/sub1/subsub1/merged/10"));
		assertEquals(15, 	_store.take("/sub1/subsub1/merged/11"));
		assertEquals(20, 	_store.take("/sub1/subsub1/merged/12"));
		
		assertEquals(2, 	_store.take("/sub1/subsub2/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub2/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub2/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub2/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub2/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub2/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub2/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub2/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub2/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub2/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub2/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub2/sequenceThree/5"));
		assertEquals(2, 	_store.take("/sub1/subsub2/merged/1"));
		assertEquals(1, 	_store.take("/sub1/subsub2/merged/2"));
		assertEquals(13, 	_store.take("/sub1/subsub2/merged/3"));
		assertEquals(4, 	_store.take("/sub1/subsub2/merged/4"));
		assertEquals(3, 	_store.take("/sub1/subsub2/merged/5"));
		assertEquals(75, 	_store.take("/sub1/subsub2/merged/6"));
		assertEquals(6, 	_store.take("/sub1/subsub2/merged/7"));
		assertEquals(5, 	_store.take("/sub1/subsub2/merged/8"));
		assertEquals(60, 	_store.take("/sub1/subsub2/merged/9"));
		assertEquals(8, 	_store.take("/sub1/subsub2/merged/10"));
		assertEquals(15, 	_store.take("/sub1/subsub2/merged/11"));
		assertEquals(20, 	_store.take("/sub1/subsub2/merged/12"));
		
		assertEquals(2, 	_store.take("/sub1/subsub3/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub3/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub3/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub3/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub3/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub3/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub3/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub3/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub3/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub3/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub3/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub3/sequenceThree/5"));
		assertEquals(2, 	_store.take("/sub1/subsub3/merged/1"));
		assertEquals(1, 	_store.take("/sub1/subsub3/merged/2"));
		assertEquals(13, 	_store.take("/sub1/subsub3/merged/3"));
		assertEquals(4, 	_store.take("/sub1/subsub3/merged/4"));
		assertEquals(3, 	_store.take("/sub1/subsub3/merged/5"));
		assertEquals(75, 	_store.take("/sub1/subsub3/merged/6"));
		assertEquals(6, 	_store.take("/sub1/subsub3/merged/7"));
		assertEquals(5, 	_store.take("/sub1/subsub3/merged/8"));
		assertEquals(60, 	_store.take("/sub1/subsub3/merged/9"));
		assertEquals(8, 	_store.take("/sub1/subsub3/merged/10"));
		assertEquals(15, 	_store.take("/sub1/subsub3/merged/11"));
		assertEquals(20, 	_store.take("/sub1/subsub3/merged/12"));

		assertEquals("A", 	_store.take("/sub1/trigger/1"));
		assertEquals("B", 	_store.take("/sub1/trigger/2"));
		assertEquals("C", 	_store.take("/sub1/trigger/3"));

		assertEquals("A", 	_store.take("/sub1/subsub1/discard"));
		assertEquals("B", 	_store.take("/sub1/subsub2/discard"));
		assertEquals("C", 	_store.take("/sub1/subsub3/discard"));
		
		assertEquals(0, _store.size());
	}
	
	
	public void testMergeIntegerStreams_DoublyNested_DataDrivenDirector() throws Exception {

		_createMergeIntegerStreamBuilders();

		final Workflow workflow = new WorkflowBuilder()
			.name("MergeIntegerStreams")
			.context(_context)
			.director(new DataDrivenDirector())
			.node(new WorkflowNodeBuilder()
				.name("sub")
				.director(new DataDrivenDirector())
				.prefix("/sub{STEP}")
				.node(_triggerNodeBuilder)
				.node(new WorkflowNodeBuilder()
				.prefix("/subsub{STEP}")
					.name("subsub")
					.director(new DataDrivenDirector())
					.inflow("/trigger", "/discard")
					.node(_sequenceOneNodeBuilder)
					.node(_sequenceTwoNodeBuilder)
					.node(_sequenceThreeNodeBuilder)
					.node(_mergeNodeBuilder)
					.node(_printerNodeBuilder)
					)
			)
			.build();
		
		workflow.configure();
		workflow.initialize();

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
			}
		});

		assertEquals(
				"2" 	+ EOL +
				"4" 	+ EOL +
				"6" 	+ EOL +
				"8" 	+ EOL +
				"13" 	+ EOL +
				"75" 	+ EOL +
				"60" 	+ EOL +
				"15" 	+ EOL +
				"20"	+ EOL + 
				"1"		+ EOL +
				"3" 	+ EOL +
				"5" 	+ EOL +
				"2" 	+ EOL +
				"4" 	+ EOL +
				"6" 	+ EOL +
				"8" 	+ EOL +
				"13" 	+ EOL +
				"75" 	+ EOL +
				"60" 	+ EOL +
				"15" 	+ EOL +
				"20"	+ EOL + 
				"1"		+ EOL +
				"3" 	+ EOL +
				"5" 	+ EOL +
				"2" 	+ EOL +
				"4" 	+ EOL +
				"6" 	+ EOL +
				"8" 	+ EOL +
				"13" 	+ EOL +
				"75" 	+ EOL +
				"60" 	+ EOL +
				"15" 	+ EOL +
				"20"	+ EOL + 
				"1"		+ EOL +
				"3" 	+ EOL +
				"5" 	+ EOL,
				recorder.getStdoutRecording());
		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(2, 	_store.take("/sub1/subsub1/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub1/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub1/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub1/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub1/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub1/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub1/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub1/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub1/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub1/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub1/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub1/sequenceThree/5"));
		assertEquals(2, 	_store.take("/sub1/subsub1/merged/1"));
		assertEquals(4, 	_store.take("/sub1/subsub1/merged/2"));
		assertEquals(6, 	_store.take("/sub1/subsub1/merged/3"));
		assertEquals(8, 	_store.take("/sub1/subsub1/merged/4"));
		assertEquals(13, 	_store.take("/sub1/subsub1/merged/5"));
		assertEquals(75, 	_store.take("/sub1/subsub1/merged/6"));
		assertEquals(60, 	_store.take("/sub1/subsub1/merged/7"));
		assertEquals(15, 	_store.take("/sub1/subsub1/merged/8"));
		assertEquals(20, 	_store.take("/sub1/subsub1/merged/9"));
		assertEquals(1, 	_store.take("/sub1/subsub1/merged/10"));
		assertEquals(3, 	_store.take("/sub1/subsub1/merged/11"));
		assertEquals(5, 	_store.take("/sub1/subsub1/merged/12"));
		
		assertEquals(2, 	_store.take("/sub1/subsub2/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub2/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub2/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub2/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub2/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub2/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub2/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub2/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub2/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub2/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub2/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub2/sequenceThree/5"));
		assertEquals(2, 	_store.take("/sub1/subsub2/merged/1"));
		assertEquals(4, 	_store.take("/sub1/subsub2/merged/2"));
		assertEquals(6, 	_store.take("/sub1/subsub2/merged/3"));
		assertEquals(8, 	_store.take("/sub1/subsub2/merged/4"));
		assertEquals(13, 	_store.take("/sub1/subsub2/merged/5"));
		assertEquals(75, 	_store.take("/sub1/subsub2/merged/6"));
		assertEquals(60, 	_store.take("/sub1/subsub2/merged/7"));
		assertEquals(15, 	_store.take("/sub1/subsub2/merged/8"));
		assertEquals(20, 	_store.take("/sub1/subsub2/merged/9"));
		assertEquals(1, 	_store.take("/sub1/subsub2/merged/10"));
		assertEquals(3, 	_store.take("/sub1/subsub2/merged/11"));
		assertEquals(5, 	_store.take("/sub1/subsub2/merged/12"));
		
		assertEquals(2, 	_store.take("/sub1/subsub3/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub3/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub3/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub3/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub3/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub3/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub3/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub3/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub3/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub3/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub3/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub3/sequenceThree/5"));
		assertEquals(2, 	_store.take("/sub1/subsub3/merged/1"));
		assertEquals(4, 	_store.take("/sub1/subsub3/merged/2"));
		assertEquals(6, 	_store.take("/sub1/subsub3/merged/3"));
		assertEquals(8, 	_store.take("/sub1/subsub3/merged/4"));
		assertEquals(13, 	_store.take("/sub1/subsub3/merged/5"));
		assertEquals(75, 	_store.take("/sub1/subsub3/merged/6"));
		assertEquals(60, 	_store.take("/sub1/subsub3/merged/7"));
		assertEquals(15, 	_store.take("/sub1/subsub3/merged/8"));
		assertEquals(20, 	_store.take("/sub1/subsub3/merged/9"));
		assertEquals(1, 	_store.take("/sub1/subsub3/merged/10"));
		assertEquals(3, 	_store.take("/sub1/subsub3/merged/11"));
		assertEquals(5, 	_store.take("/sub1/subsub3/merged/12"));
		
		assertEquals("A", 	_store.take("/sub1/trigger/1"));
		assertEquals("B", 	_store.take("/sub1/trigger/2"));
		assertEquals("C", 	_store.take("/sub1/trigger/3"));

		assertEquals("A", 	_store.take("/sub1/subsub1/discard"));
		assertEquals("B", 	_store.take("/sub1/subsub2/discard"));
		assertEquals("C", 	_store.take("/sub1/subsub3/discard"));
		
		assertEquals(0, _store.size());
	}
	
	public void testMergeIntegerStreams_DoublyNested_MTDataDrivenDirector() throws Exception {

		_createMergeIntegerStreamBuilders();

		final Workflow workflow = new WorkflowBuilder()
			.name("MergeIntegerStreams")
			.context(_context)
			.director(new MTDataDrivenDirector())
			.node(new WorkflowNodeBuilder()
				.director(new MTDataDrivenDirector())
				.prefix("/sub{STEP}")
				.node(_triggerNodeBuilder)
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.director(new MTDataDrivenDirector())
					.inflow("/trigger", "/discard")
					.node(_sequenceOneNodeBuilder)
					.node(_sequenceTwoNodeBuilder)
					.node(_sequenceThreeNodeBuilder)
					.node(_mergeNodeBuilder)
					.node(_printerNodeBuilder)
					)
			)
			.build();
		
		workflow.configure();
		workflow.initialize();

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				workflow.run();
			}
		});

		assertEquals("", recorder.getStderrRecording());
		
		assertEquals(2, 	_store.take("/sub1/subsub1/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub1/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub1/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub1/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub1/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub1/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub1/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub1/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub1/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub1/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub1/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub1/sequenceThree/5"));
		assertEquals(2, 	_store.take("/sub1/subsub2/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub2/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub2/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub2/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub2/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub2/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub2/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub2/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub2/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub2/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub2/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub2/sequenceThree/5"));
		assertEquals(2, 	_store.take("/sub1/subsub3/sequenceOne/1"));
		assertEquals(4, 	_store.take("/sub1/subsub3/sequenceOne/2"));
		assertEquals(6, 	_store.take("/sub1/subsub3/sequenceOne/3"));
		assertEquals(8, 	_store.take("/sub1/subsub3/sequenceOne/4"));
		assertEquals(1, 	_store.take("/sub1/subsub3/sequenceTwo/1"));
		assertEquals(3, 	_store.take("/sub1/subsub3/sequenceTwo/2"));
		assertEquals(5, 	_store.take("/sub1/subsub3/sequenceTwo/3"));
		assertEquals(13, 	_store.take("/sub1/subsub3/sequenceThree/1"));
		assertEquals(75, 	_store.take("/sub1/subsub3/sequenceThree/2"));
		assertEquals(60, 	_store.take("/sub1/subsub3/sequenceThree/3"));
		assertEquals(15, 	_store.take("/sub1/subsub3/sequenceThree/4"));
		assertEquals(20, 	_store.take("/sub1/subsub3/sequenceThree/5"));
		
		_store.removeValue(2);
		_store.removeValue(4);
		_store.removeValue(6);
		_store.removeValue(8);
		_store.removeValue(1);
		_store.removeValue(3);
		_store.removeValue(5);
		_store.removeValue(13);
		_store.removeValue(75);
		_store.removeValue(60);
		_store.removeValue(15);
		_store.removeValue(20);		
		_store.removeValue(2);
		_store.removeValue(4);
		_store.removeValue(6);
		_store.removeValue(8);
		_store.removeValue(1);
		_store.removeValue(3);
		_store.removeValue(5);
		_store.removeValue(13);
		_store.removeValue(75);
		_store.removeValue(60);
		_store.removeValue(15);
		_store.removeValue(20);		
		_store.removeValue(2);
		_store.removeValue(4);
		_store.removeValue(6);
		_store.removeValue(8);
		_store.removeValue(1);
		_store.removeValue(3);
		_store.removeValue(5);
		_store.removeValue(13);
		_store.removeValue(75);
		_store.removeValue(60);
		_store.removeValue(15);
		_store.removeValue(20);
		
		assertEquals("A", 	_store.take("/sub1/trigger/1"));
		assertEquals("B", 	_store.take("/sub1/trigger/2"));
		assertEquals("C", 	_store.take("/sub1/trigger/3"));

		assertEquals("A", 	_store.take("/sub1/subsub1/discard"));
		assertEquals("B", 	_store.take("/sub1/subsub2/discard"));
		assertEquals("C", 	_store.take("/sub1/subsub3/discard"));
		
		assertEquals(0, _store.size());
	}
	
	public void testConditionalRouting_ThreeDirectors() throws Exception {
		
		Director _mtDataDrivenDirector = new MTDataDrivenDirector();
		
		for (Director nestedDirector :  new Director[] { new PublishSubscribeDirector() ,
														 new DataDrivenDirector(), 
														 _mtDataDrivenDirector }) {
			
			_store = new ConsumableObjectStore();	 

			_context = new WorkflowContextBuilder()
				.store(_store)
				.build();
				
			nestedDirector.setNodesStepOnce(true);
			
			@SuppressWarnings("unused")
			final Workflow workflow = new WorkflowBuilder()
			
				.name("ConditionalRouting")
				.context(_context)
				.director(new PublishSubscribeDirector())
				
				.node(new JavaNodeBuilder()
					.name("CreateIntegerSequence")
					.sequence("c", new Object [] {
							1, 
							3, 
							13, 
							5,
							60,
							9,
							15,
							2})
					.bean(new Object() {
						public int value, c;
						public void step() { value = c; }
					})
					.outflow("value", "/integers"))
				
				.node(new WorkflowNodeBuilder()
					.name("saturate")
					.director(nestedDirector)
					.prefix("/Saturate{STEP}")
					.inflow("/integers", "/original" )
					
					.node(new JavaNodeBuilder()
						.name("RouteIntegers")
						.inflow("/original", "input")
						.bean(new Object() {
							public int input, under, over;
							private ActorStatus _status;
							public void setStatus(ActorStatus actorStatus) {_status = actorStatus;}
							public void step() {
								if (input < 10) {
									under = input;
								    _status.disableOutput("over");
								} else {
									over = input;
								    _status.disableOutput("under");
								}
							}
						})
						.outflow("under", "/allowedInteger")
						.outflow("over", "/oversizedInteger"))

					.node(new JavaNodeBuilder()
						.name("SaturateInteger")
						.inflow("/oversizedInteger", "input")
						.bean(new Object() {
							public int input, output;
							public void step() { output = 10; }
						})
						.outflow("output", "/saturatedInteger"))
					
					.node(new MergeNodeBuilder()
						.name("MergeNode")
						.inflow("/allowedInteger")
						.inflow("/saturatedInteger")
						.outflow("/merged"))
						
					.outflow("/merged", "/saturated")
				)
				
				.node( new JavaNodeBuilder()
					.name("Printer")
					.inflow("/saturated", "value")
					.bean(new Object() {
						public int value;
						public void step() { System.out.println(value); }
					}))
				
				.build();
			
			workflow.configure();
			workflow.initialize();
	
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {
					workflow.run();
				}
			});
			
			assertEquals(
					"1"		+ EOL +
					"3"		+ EOL +
					"10"	+ EOL +
					"5"		+ EOL +
					"10"	+ EOL +
					"9"		+ EOL +
					"10"	+ EOL +
					"2"		+ EOL, 
					recorder.getStdoutRecording());
			
			assertEquals("", recorder.getStderrRecording());
			
			assertEquals(1, 	_store.take("/integers/1"));
			assertEquals(3, 	_store.take("/integers/2"));
			assertEquals(13, 	_store.take("/integers/3"));
			assertEquals(5, 	_store.take("/integers/4"));
			assertEquals(60, 	_store.take("/integers/5"));
			assertEquals(9, 	_store.take("/integers/6"));
			assertEquals(15, 	_store.take("/integers/7"));
			assertEquals(2, 	_store.take("/integers/8"));
			
			assertEquals(1, 	_store.take("/Saturate1/original"));
			assertEquals(3, 	_store.take("/Saturate2/original"));
			assertEquals(13, 	_store.take("/Saturate3/original"));
			assertEquals(5, 	_store.take("/Saturate4/original"));
			assertEquals(60, 	_store.take("/Saturate5/original"));
			assertEquals(9, 	_store.take("/Saturate6/original"));
			assertEquals(15, 	_store.take("/Saturate7/original"));
			assertEquals(2, 	_store.take("/Saturate8/original"));
			
			assertEquals(1, 	_store.take("/Saturate1/allowedInteger"));
			assertEquals(3, 	_store.take("/Saturate2/allowedInteger"));
			assertEquals(5, 	_store.take("/Saturate4/allowedInteger"));
			assertEquals(9, 	_store.take("/Saturate6/allowedInteger"));
			assertEquals(2, 	_store.take("/Saturate8/allowedInteger"));
			
			assertEquals(13, 	_store.take("/Saturate3/oversizedInteger"));
			assertEquals(60, 	_store.take("/Saturate5/oversizedInteger"));
			assertEquals(15, 	_store.take("/Saturate7/oversizedInteger"));
			
			assertEquals(10, 	_store.take("/Saturate3/saturatedInteger"));
			assertEquals(10, 	_store.take("/Saturate5/saturatedInteger"));
			assertEquals(10, 	_store.take("/Saturate7/saturatedInteger"));
					
			assertEquals(1, 	_store.take("/Saturate1/merged"));
			assertEquals(3, 	_store.take("/Saturate2/merged"));
			assertEquals(10, 	_store.take("/Saturate3/merged"));
			assertEquals(5, 	_store.take("/Saturate4/merged"));
			assertEquals(10, 	_store.take("/Saturate5/merged"));
			assertEquals(9, 	_store.take("/Saturate6/merged"));
			assertEquals(10, 	_store.take("/Saturate7/merged"));
			assertEquals(2, 	_store.take("/Saturate8/merged"));
			
			assertEquals(1, 	_store.take("/saturated/1"));
			assertEquals(3, 	_store.take("/saturated/2"));
			assertEquals(10, 	_store.take("/saturated/3"));
			assertEquals(5, 	_store.take("/saturated/4"));
			assertEquals(10, 	_store.take("/saturated/5"));
			assertEquals(9, 	_store.take("/saturated/6"));
			assertEquals(10, 	_store.take("/saturated/7"));
			assertEquals(2, 	_store.take("/saturated/8"));		
			
			assertEquals(0, _store.size());
		}
	}
}
