package org.restflow.features;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.directors.DemandDrivenDirector;
import org.restflow.directors.Director;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.GroovyNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestStepsOnceSupport extends RestFlowTestCase {

	private WorkflowContext 		_context;
	private Director 				_publishSubscribeDirector;
	private Director 				_dataDrivenDirector;
	private Director 				_mtDataDrivenDirector;
	private DemandDrivenDirector	_demandDrivenDirector;
	private ConsumableObjectStore 	_store;
	private ActorNodeBuilder		_triggerNodeBuilder;
	private ActorNodeBuilder		_valueCopierNodeBuilder;
	private ActorNodeBuilder 		_valuePrinterNodeBuilder;
	private ActorNodeBuilder		_valueProducerNodeBuilder;
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		_publishSubscribeDirector 	= new PublishSubscribeDirector();
		_dataDrivenDirector 		= new DataDrivenDirector();
		_mtDataDrivenDirector		= new MTDataDrivenDirector();
		_demandDrivenDirector		= new DemandDrivenDirector();
		
	}
	
	private void _setUp() throws Exception {

		_store = new ConsumableObjectStore();
		
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();

		_demandDrivenDirector.setFiringCount(3);
		
		_triggerNodeBuilder = new GroovyNodeBuilder()
			.name("trigger")
			.context(_context)
			.sequence("constant", new Object [] {"A", "B", "C"})
			.step("value=constant")
			.outflow("value", "/trigger");

		_valueProducerNodeBuilder = new GroovyNodeBuilder()
			.name("valueProducer")
			.context(_context)
			.sequence("constant", new Object [] {2, 4, 6})
			.step("value=constant")
			.outflow("value", "/value");

		_valueCopierNodeBuilder = new GroovyNodeBuilder()
			.name("valueCopier")
			.context(_context)
			.inflow("/value", "original")
			.step("copy=original")
			.outflow("copy", "/copy");

		_valuePrinterNodeBuilder = new GroovyNodeBuilder()
			.name("valuePrinter")
			.context(_context)
			.inflow("/copy", "value")
			.step("println value");
	}
	
	public void test_DataSequencePrinter() throws Exception {
		
		// perform test for each of four directors
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector,
												 _demandDrivenDirector}) {
		
			_setUp();
			
			// build and prepare the workflow to run
			final Workflow workflow = new WorkflowBuilder()
				.name("DataSequencePrinter")
				.context(_context)
				.director(director)
				.node(_valueProducerNodeBuilder)
				.node(_valueCopierNodeBuilder)
				.node(_valuePrinterNodeBuilder)
				.build();
			workflow.configure();
			workflow.initialize();

			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout showing three values printed
			assertEquals(
				"2" 	+ EOL +
				"4" 	+ EOL +
				"6" 	+ EOL,
				recorder.getStdoutRecording());
			
			// confirm that no error messages were generated
			assertEquals("", recorder.getStderrRecording());
			
			// confirm expected published values and URIs
			assertEquals(2, _store.take("/value/1"));
			assertEquals(4, _store.take("/value/2"));
			assertEquals(6, _store.take("/value/3"));
			assertEquals(2, _store.take("/copy/1"));
			assertEquals(4, _store.take("/copy/2"));
			assertEquals(6, _store.take("/copy/3"));
			assertEquals(0, _store.size());	
		}
	}
	
	public void test_DataSequencePrinter_FirstNodeStepsOnce() throws Exception {
		
		// perform test for each of four directors
		for (Director director : new Director[] {_publishSubscribeDirector, 
											 	 _dataDrivenDirector, 
												 _mtDataDrivenDirector,
												 _demandDrivenDirector}) {
											
			_setUp();

			// configure the first node to step just once
			_valueProducerNodeBuilder.stepsOnce();
	
			// build and prepare the workflow to run
			final Workflow workflow = new WorkflowBuilder()
				.context(_context)
				.director(director)
				.node(_valueProducerNodeBuilder)
				.node(_valueCopierNodeBuilder)
				.node(_valuePrinterNodeBuilder)
				.build();
			workflow.configure();
			workflow.initialize();
	
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout show just one value printed
			assertEquals(
				"2" + EOL,
				recorder.getStdoutRecording());
			
			// confirm that no error messages were generated
			assertEquals("", recorder.getStderrRecording());
			
			// confirm expected published values and URIs
			assertEquals(2, _store.take("/value"));
			assertEquals(2, _store.take("/copy/1"));
			assertEquals(0, _store.size());
		}
	}

	public void test_DataSourceWithInputSequence_SecondNodeStepsOnce() throws Exception {
		
		// perform test for each of four directors
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector,
												 _demandDrivenDirector}) {

			_setUp();

			// configure the second node to step just once
			_valueCopierNodeBuilder.stepsOnce();
	
			// build and prepare the workflow to run
			final Workflow workflow = new WorkflowBuilder()
				.name("SecondNodeStepsOnce")
				.context(_context)
				.director(director)
				.node(_valueProducerNodeBuilder)
				.node(_valueCopierNodeBuilder)
				.node(_valuePrinterNodeBuilder)
				.build();
			workflow.configure();
			workflow.initialize();
	
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout showing just one value printed
			assertEquals(
				"2" + EOL,
				recorder.getStdoutRecording());
			
			if (director == _publishSubscribeDirector) {
			
				assertEquals(
					"Warning:  Run 1 of workflow <SecondNodeStepsOnce> wrapped up with unused data packets:" 	+ EOL +
					"1 packet in inflow 'original' on node [valueCopier] with URI '/value/2'" 					+ EOL +
					"1 packet in queue 'original' on node [valueCopier] with URI '/value/3'"					+ EOL, 
					recorder.getStderrRecording());
			
			} else if (director == _dataDrivenDirector) {
				
				assertEquals(
					"Warning:  Run 1 of workflow <SecondNodeStepsOnce> wrapped up with unused data packets:" 			+ EOL +
					"1 packet in outflow 'output' on node [BufferNode-for-valueCopier-original] with URI '/value/3'"	+ EOL + 
					"1 packet in inflow 'original' on node [valueCopier] with URI '/value/2'" 							+ EOL,
					recorder.getStderrRecording());
				
			} else if (director == _mtDataDrivenDirector) {
				
				assertEquals(
					"Warning:  Run 1 of workflow <SecondNodeStepsOnce> wrapped up with unused data packets:" 	+ EOL +
					"2 packets in queue 'original' on node [valueCopier] with URIs '/value/2', '/value/3'" 		+ EOL, 
					recorder.getStderrRecording());
			
			}  else if (director == _demandDrivenDirector) {
				
				assertEquals(
					"Warning:  Run 1 of workflow <SecondNodeStepsOnce> wrapped up with unused data packets:" 	+ EOL +
					"1 packet in inflow 'original' on node [valueCopier] with URI '/value/3'" 					+ EOL, 
					recorder.getStderrRecording());
			}
			
			// confirm expected published values and URIs
			assertEquals(2, _store.take("/value/1"));
			assertEquals(4, _store.take("/value/2"));
			assertEquals(6, _store.take("/value/3"));
			assertEquals(2, _store.take("/copy"));
			assertEquals(0, _store.size());	
		}
	}
	
	public void test_DataSourceWithInputSequence_ThirdNodeStepsOnce() throws Exception {
			
		// perform test for each of four directors
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector,
												 _demandDrivenDirector}) {
			
			_setUp();

			// configure the third node to step just once
			_valuePrinterNodeBuilder.stepsOnce();
	
			// build and prepare the workflow to run
			final Workflow workflow = new WorkflowBuilder()
				.name("ThirdNodeStepsOnce")
				.context(_context)
				.director(director)
				.node(_valueProducerNodeBuilder)
				.node(_valueCopierNodeBuilder)
				.node(_valuePrinterNodeBuilder)
				.build();
			workflow.configure();
			workflow.initialize();
	
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout showing just one value printed
			assertEquals(
				"2" + EOL,
				recorder.getStdoutRecording());
			
			if (director == _publishSubscribeDirector) {
				
				assertEquals(
					"Warning:  Run 1 of workflow <ThirdNodeStepsOnce> wrapped up with unused data packets:"		+ EOL +
					"1 packet in inflow 'value' on node [valuePrinter] with URI '/copy/2'"						+ EOL +
					"1 packet in queue 'value' on node [valuePrinter] with URI '/copy/3'"						+ EOL,
					recorder.getStderrRecording());
			
			} else if (director == _dataDrivenDirector) {
				
				assertEquals(
					"Warning:  Run 1 of workflow <ThirdNodeStepsOnce> wrapped up with unused data packets:"				+ EOL +
					"1 packet in outflow 'output' on node [BufferNode-for-valuePrinter-value] with URI '/copy/3'"		+ EOL +
					"1 packet in inflow 'value' on node [valuePrinter] with URI '/copy/2'"								+ EOL,
					recorder.getStderrRecording());
				
			} else if (director == _mtDataDrivenDirector) {
				
				assertEquals(
					"Warning:  Run 1 of workflow <ThirdNodeStepsOnce> wrapped up with unused data packets:"		+ EOL +
					"2 packets in queue 'value' on node [valuePrinter] with URIs '/copy/2', '/copy/3'"			+ EOL,
					recorder.getStderrRecording());
			
			} else {
								
				assertEquals(
						"Warning:  Run 1 of workflow <ThirdNodeStepsOnce> wrapped up with unused data packets:"		+ EOL +
						"1 packet in inflow 'value' on node [valuePrinter] with URI '/copy/3'"						+ EOL,
						recorder.getStderrRecording());
			}
			
			// confirm expected published values and URIs
			assertEquals(2, _store.take("/value/1"));
			assertEquals(4, _store.take("/value/2"));
			assertEquals(6, _store.take("/value/3"));
			assertEquals(2, _store.take("/copy/1"));
			assertEquals(4, _store.take("/copy/2"));
			assertEquals(6, _store.take("/copy/3"));
			assertEquals(0, _store.size());
		}
	}

	public void test_DataSequencePrinter_FirstAndSecondNodesStepOnce() throws Exception {
		
		// perform test for each of four directors
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector,
												 _demandDrivenDirector}) {

			_setUp();

			// configure the first and second node to step just once
			_valueProducerNodeBuilder.stepsOnce();
			_valueCopierNodeBuilder.stepsOnce();
			
			// build and prepare the workflow to run
			final Workflow workflow = new WorkflowBuilder()
				.name("FirstAndSecondNodesStepOnce")
				.context(_context)
				.director(director)
				.node(_valueProducerNodeBuilder)
				.node(_valueCopierNodeBuilder)
				.node(_valuePrinterNodeBuilder)
				.build();
			workflow.configure();
			workflow.initialize();
	
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout show just one value printed
			assertEquals(
				"2" + EOL,
				recorder.getStdoutRecording());
			
			assertEquals("", recorder.getStderrRecording());
			
			// confirm expected published values and URIs
			assertEquals(2, _store.take("/value"));
			assertEquals(2, _store.take("/copy"));
			assertEquals(0, _store.size());
		}
	}
	
	public void test_DataSourceWithInputSequence_DirectorStepsOnce() throws Exception {
		
		// perform test for each of four directors
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector,
												 _demandDrivenDirector}) {
			
			_setUp();

			// configure the director to step each node at most once
			director.setNodesStepOnce(true);
			
			// build and prepare the workflow to run
			final Workflow workflow = new WorkflowBuilder()
				.name("DirectorStepsOnce")
				.context(_context)
				.director(director)
				.node(_valueProducerNodeBuilder)
				.node(_valueCopierNodeBuilder)
				.node(_valuePrinterNodeBuilder)
				.build();
			workflow.configure();
			workflow.initialize();
	
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout
			assertEquals(
				"2" 	+ EOL,
				recorder.getStdoutRecording());
						
			assertEquals("", recorder.getStderrRecording());
			
			// confirm expected published values and URIs
			assertEquals(2, _store.take("/value"));
			assertEquals(2, _store.take("/copy"));
			assertEquals(0, _store.size());
		}
	}

	public void test_DoublyNestedDataSequencePrinter() throws Exception {

		// perform test for each of three directors compatible with the workflow
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector}) {

			_setUp();

			final Workflow workflow = new WorkflowBuilder()
				.context(_context)
				.node(new WorkflowNodeBuilder()
					.prefix("/sub{STEP}")
					.node(_triggerNodeBuilder)
					.node(new WorkflowNodeBuilder()
						.prefix("/subsub{STEP}")
						.director(director)
						.inflow("/trigger", "/discard")
						.node(_valueProducerNodeBuilder)
						.node(_valueCopierNodeBuilder)
						.node(_valuePrinterNodeBuilder)
					)
				)
				.build();
			
			workflow.configure();
			workflow.initialize();
			
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout
			assertEquals(
				"2" 	+ EOL +
				"4" 	+ EOL +
				"6" 	+ EOL +
				"2" 	+ EOL +
				"4" 	+ EOL +
				"6" 	+ EOL +
				"2" 	+ EOL +
				"4" 	+ EOL +
				"6" 	+ EOL,
				recorder.getStdoutRecording());
			
			// confirm that no error messages were generated
			assertEquals("", recorder.getStderrRecording());
			
			assertEquals("A", _store.take("/sub1/trigger/1"));
			assertEquals("B", _store.take("/sub1/trigger/2"));
			assertEquals("C", _store.take("/sub1/trigger/3"));

			assertEquals("A", _store.take("/sub1/subsub1/discard"));
			assertEquals("B", _store.take("/sub1/subsub2/discard"));
			assertEquals("C", _store.take("/sub1/subsub3/discard"));

			
			assertEquals(2, _store.take("/sub1/subsub1/value/1"));
			assertEquals(4, _store.take("/sub1/subsub1/value/2"));
			assertEquals(6, _store.take("/sub1/subsub1/value/3"));
			assertEquals(2, _store.take("/sub1/subsub1/copy/1"));
			assertEquals(4, _store.take("/sub1/subsub1/copy/2"));
			assertEquals(6, _store.take("/sub1/subsub1/copy/3"));
	
			assertEquals(2, _store.take("/sub1/subsub2/value/1"));
			assertEquals(4, _store.take("/sub1/subsub2/value/2"));
			assertEquals(6, _store.take("/sub1/subsub2/value/3"));
			assertEquals(2, _store.take("/sub1/subsub2/copy/1"));
			assertEquals(4, _store.take("/sub1/subsub2/copy/2"));
			assertEquals(6, _store.take("/sub1/subsub2/copy/3"));
	
			assertEquals(2, _store.take("/sub1/subsub3/value/1"));
			assertEquals(4, _store.take("/sub1/subsub3/value/2"));
			assertEquals(6, _store.take("/sub1/subsub3/value/3"));
			assertEquals(2, _store.take("/sub1/subsub3/copy/1"));
			assertEquals(4, _store.take("/sub1/subsub3/copy/2"));
			assertEquals(6, _store.take("/sub1/subsub3/copy/3"));		
			
			assertEquals(0, _store.size());
		}
	}
	
	
	public void test_DoublyNestedDataSequencePrinter_FirstNodeStepsOnce() throws Exception {

		// perform test for each of three directors compatible with the workflow
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector}) {
			_setUp();

			_valueProducerNodeBuilder.stepsOnce();
			
			final Workflow workflow = new WorkflowBuilder()
				.context(_context)
				.node(new WorkflowNodeBuilder()
					.prefix("/sub{STEP}")
					.node(_triggerNodeBuilder)
					.node(new WorkflowNodeBuilder()
						.prefix("/subsub{STEP}")
						.director(director)
						.inflow("/trigger", "/discard")
						.node(_valueProducerNodeBuilder)
						.node(_valueCopierNodeBuilder)
						.node(_valuePrinterNodeBuilder)
					)
				)
				.build();
			
			workflow.configure();
			workflow.initialize();
			
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout
			assertEquals(
				"2" 	+ EOL +
				"2" 	+ EOL +
				"2" 	+ EOL,
				recorder.getStdoutRecording());
			
			// confirm that no error messages were generated
			assertEquals("", recorder.getStderrRecording());
			
			assertEquals("A", _store.take("/sub1/trigger/1"));
			assertEquals("B", _store.take("/sub1/trigger/2"));
			assertEquals("C", _store.take("/sub1/trigger/3"));

			assertEquals("A", _store.take("/sub1/subsub1/discard"));
			assertEquals("B", _store.take("/sub1/subsub2/discard"));
			assertEquals("C", _store.take("/sub1/subsub3/discard"));

			
			assertEquals(2, _store.take("/sub1/subsub1/value"));
			assertEquals(2, _store.take("/sub1/subsub1/copy/1"));
	
			assertEquals(2, _store.take("/sub1/subsub2/value"));
			assertEquals(2, _store.take("/sub1/subsub2/copy/1"));
	
			assertEquals(2, _store.take("/sub1/subsub3/value"));
			assertEquals(2, _store.take("/sub1/subsub3/copy/1"));
			
			assertEquals(0, _store.size());
		}
	}
	
	public void test_DoublyNestedDataSequencePrinter_SecondNodeStepsOnce() throws Exception {

		// perform test for each of three directors compatible with the workflow
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector
												 }) {
			_setUp();

			_valueCopierNodeBuilder.stepsOnce();
			
			final Workflow workflow = new WorkflowBuilder()
				.name("SecondNodeStepsOnce")
				.context(_context)
				.name("top")
				.node(new WorkflowNodeBuilder()
					.name("sub")
					.prefix("/sub{STEP}")
					.node(_triggerNodeBuilder)
					.node(new WorkflowNodeBuilder()
						.name("subsub")
						.prefix("/subsub{STEP}")
						.director(director)
						.inflow("/trigger", "/discard")
						.node(_valueProducerNodeBuilder)
						.node(_valueCopierNodeBuilder)
						.node(_valuePrinterNodeBuilder)
					)
				)
				.build();
			
			workflow.configure();
			workflow.initialize();
			
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout
			assertEquals(
					"2" 	+ EOL +
					"2" 	+ EOL +
					"2" 	+ EOL,
					recorder.getStdoutRecording());
			
			if (director == _publishSubscribeDirector) {
				
				assertEquals(
						
					"Warning:  Run 1 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:" 	+ EOL +
					"1 packet in inflow 'original' on node [valueCopier] with URI '/sub1/subsub1/value/2'" 			+ EOL +
					"1 packet in queue 'original' on node [valueCopier] with URI '/sub1/subsub1/value/3'" 			+ EOL +
					
					"Warning:  Run 2 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:" 	+ EOL +
					"1 packet in inflow 'original' on node [valueCopier] with URI '/sub1/subsub2/value/2'" 			+ EOL +
					"1 packet in queue 'original' on node [valueCopier] with URI '/sub1/subsub2/value/3'" 			+ EOL +
					
					"Warning:  Run 3 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"	 	+ EOL +
					"1 packet in inflow 'original' on node [valueCopier] with URI '/sub1/subsub3/value/2'" 			+ EOL +
					"1 packet in queue 'original' on node [valueCopier] with URI '/sub1/subsub3/value/3'"			+ EOL, 
					
					recorder.getStderrRecording());
			
			} else if (director == _dataDrivenDirector) {
				
				assertEquals(
						
					"Warning:  Run 1 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:" 					+ EOL +
					"1 packet in outflow 'output' on node [BufferNode-for-valueCopier-original] with URI '/sub1/subsub1/value/3'" 	+ EOL +
					"1 packet in inflow 'original' on node [valueCopier] with URI '/sub1/subsub1/value/2'" 							+ EOL +
					
					"Warning:  Run 2 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:" 					+ EOL +
					"1 packet in outflow 'output' on node [BufferNode-for-valueCopier-original] with URI '/sub1/subsub2/value/3'" 	+ EOL +
					"1 packet in inflow 'original' on node [valueCopier] with URI '/sub1/subsub2/value/2'"							+ EOL +
					
					"Warning:  Run 3 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:" 					+ EOL +
					"1 packet in outflow 'output' on node [BufferNode-for-valueCopier-original] with URI '/sub1/subsub3/value/3'" 	+ EOL +
					"1 packet in inflow 'original' on node [valueCopier] with URI '/sub1/subsub3/value/2'" 							+ EOL,
					
					recorder.getStderrRecording());
				
			} else if (director == _mtDataDrivenDirector) {
				
				assertEquals(
						
					"Warning:  Run 1 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"	+ EOL +
					"2 packets in queue 'original' on node [valueCopier] with URIs "							+ 
						"'/sub1/subsub1/value/2', '/sub1/subsub1/value/3'"										+ EOL +
					
					"Warning:  Run 2 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"	+ EOL +
					"2 packets in queue 'original' on node [valueCopier] with URIs " 							+ 
						"'/sub1/subsub2/value/2', '/sub1/subsub2/value/3'"										+ EOL +
					
					"Warning:  Run 3 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"	+ EOL +
					"2 packets in queue 'original' on node [valueCopier] with URIs " 							+ 
						"'/sub1/subsub3/value/2', '/sub1/subsub3/value/3'"										+ EOL,
						
					recorder.getStderrRecording());
			
			}
			
			assertEquals("A", _store.take("/sub1/trigger/1"));
			assertEquals("B", _store.take("/sub1/trigger/2"));
			assertEquals("C", _store.take("/sub1/trigger/3"));

			assertEquals("A", _store.take("/sub1/subsub1/discard"));
			assertEquals("B", _store.take("/sub1/subsub2/discard"));
			assertEquals("C", _store.take("/sub1/subsub3/discard"));

			assertEquals(2, _store.take("/sub1/subsub1/value/1"));
			assertEquals(4, _store.take("/sub1/subsub1/value/2"));
			assertEquals(6, _store.take("/sub1/subsub1/value/3"));
			assertEquals(2, _store.take("/sub1/subsub1/copy"));
	
			assertEquals(2, _store.take("/sub1/subsub2/value/1"));
			assertEquals(4, _store.take("/sub1/subsub2/value/2"));
			assertEquals(6, _store.take("/sub1/subsub2/value/3"));
			assertEquals(2, _store.take("/sub1/subsub2/copy"));
	
			assertEquals(2, _store.take("/sub1/subsub3/value/1"));
			assertEquals(4, _store.take("/sub1/subsub3/value/2"));
			assertEquals(6, _store.take("/sub1/subsub3/value/3"));
			assertEquals(2, _store.take("/sub1/subsub3/copy"));
			
			assertEquals(0, _store.size());
		}
	}
	
	public void test_DoublyNestedDataSequencePrinter_ThirdNodeStepsOnce() throws Exception {

		// perform test for each of three directors compatible with the workflow
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector }) {
			
			_setUp();

			_valuePrinterNodeBuilder.stepsOnce();
			
			final Workflow workflow = new WorkflowBuilder()
				.name("top")
				.context(_context)
				.node(new WorkflowNodeBuilder()
					.name("sub")
					.prefix("/sub{STEP}")
					.node(_triggerNodeBuilder)
					.node(new WorkflowNodeBuilder()
						.name("subsub")
						.prefix("/subsub{STEP}")
						.director(director)
						.inflow("/trigger", "/discard")
						.node(_valueProducerNodeBuilder)
						.node(_valueCopierNodeBuilder)
						.node(_valuePrinterNodeBuilder)
					)
				)
				.build();
			
			workflow.configure();
			workflow.initialize();
			
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout
			if (director == _publishSubscribeDirector || director == _mtDataDrivenDirector) {

				assertEquals(
						"2" 	+ EOL +
						"2" 	+ EOL +
						"2" 	+ EOL,
						recorder.getStdoutRecording());
			} else {
				
				assertEquals(
						"2" 	+ EOL +
						"2" 	+ EOL +
						"2" 	+ EOL,
						recorder.getStdoutRecording());
			}
			
			if (director == _publishSubscribeDirector) {
				
				assertEquals(
						
					"Warning:  Run 1 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"		+ EOL +
					"1 packet in inflow 'value' on node [valuePrinter] with URI '/sub1/subsub1/copy/2'"				+ EOL +
					"1 packet in queue 'value' on node [valuePrinter] with URI '/sub1/subsub1/copy/3'"				+ EOL +
							
					"Warning:  Run 2 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"		+ EOL +
					"1 packet in inflow 'value' on node [valuePrinter] with URI '/sub1/subsub2/copy/2'"				+ EOL +
					"1 packet in queue 'value' on node [valuePrinter] with URI '/sub1/subsub2/copy/3'"				+ EOL +
					
					"Warning:  Run 3 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"		+ EOL +
					"1 packet in inflow 'value' on node [valuePrinter] with URI '/sub1/subsub3/copy/2'"				+ EOL +
					"1 packet in queue 'value' on node [valuePrinter] with URI '/sub1/subsub3/copy/3'"				+ EOL,
					
					 recorder.getStderrRecording());
	
			} else if (director == _dataDrivenDirector) {
				
				assertEquals(
					
					"Warning:  Run 1 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"					+ EOL +
					"1 packet in outflow 'output' on node [BufferNode-for-valuePrinter-value] with URI '/sub1/subsub1/copy/3'"	+ EOL +
					"1 packet in inflow 'value' on node [valuePrinter] with URI '/sub1/subsub1/copy/2'"							+ EOL +
					
					"Warning:  Run 2 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"					+ EOL +
					"1 packet in outflow 'output' on node [BufferNode-for-valuePrinter-value] with URI '/sub1/subsub2/copy/3'"	+ EOL +
					"1 packet in inflow 'value' on node [valuePrinter] with URI '/sub1/subsub2/copy/2'"							+ EOL +
					
					"Warning:  Run 3 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"					+ EOL +
					"1 packet in outflow 'output' on node [BufferNode-for-valuePrinter-value] with URI '/sub1/subsub3/copy/3'"	+ EOL +
					"1 packet in inflow 'value' on node [valuePrinter] with URI '/sub1/subsub3/copy/2'"							+ EOL,
					
					recorder.getStderrRecording());
				
			} else {
				
				assertEquals(
						
					"Warning:  Run 1 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"	+ EOL +
					"2 packets in queue 'value' on node [valuePrinter] with URIs " 								+
						"'/sub1/subsub1/copy/2', '/sub1/subsub1/copy/3'"										+ EOL +	
					
					"Warning:  Run 2 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"	+ EOL +
					"2 packets in queue 'value' on node [valuePrinter] with URIs " 								+
						"'/sub1/subsub2/copy/2', '/sub1/subsub2/copy/3'"										+ EOL +
					
					"Warning:  Run 3 of workflow <top>[sub][subsub]<subsub> wrapped up with unused data packets:"	+ EOL +
					"2 packets in queue 'value' on node [valuePrinter] with URIs " 								+
						"'/sub1/subsub3/copy/2', '/sub1/subsub3/copy/3'"				 						+ EOL,
					
					recorder.getStderrRecording());
			}
			
			assertEquals("A", _store.take("/sub1/trigger/1"));
			assertEquals("B", _store.take("/sub1/trigger/2"));
			assertEquals("C", _store.take("/sub1/trigger/3"));

			assertEquals("A", _store.take("/sub1/subsub1/discard"));
			assertEquals("B", _store.take("/sub1/subsub2/discard"));
			assertEquals("C", _store.take("/sub1/subsub3/discard"));

			
			assertEquals(2, _store.take("/sub1/subsub1/value/1"));
			assertEquals(4, _store.take("/sub1/subsub1/value/2"));
			assertEquals(6, _store.take("/sub1/subsub1/value/3"));
			assertEquals(2, _store.take("/sub1/subsub1/copy/1"));
			assertEquals(4, _store.take("/sub1/subsub1/copy/2"));
			assertEquals(6, _store.take("/sub1/subsub1/copy/3"));
	
			assertEquals(2, _store.take("/sub1/subsub2/value/1"));
			assertEquals(4, _store.take("/sub1/subsub2/value/2"));
			assertEquals(6, _store.take("/sub1/subsub2/value/3"));
			assertEquals(2, _store.take("/sub1/subsub2/copy/1"));
			assertEquals(4, _store.take("/sub1/subsub2/copy/2"));
			assertEquals(6, _store.take("/sub1/subsub2/copy/3"));
	
			assertEquals(2, _store.take("/sub1/subsub3/value/1"));
			assertEquals(4, _store.take("/sub1/subsub3/value/2"));
			assertEquals(6, _store.take("/sub1/subsub3/value/3"));
			assertEquals(2, _store.take("/sub1/subsub3/copy/1"));
			assertEquals(4, _store.take("/sub1/subsub3/copy/2"));
			assertEquals(6, _store.take("/sub1/subsub3/copy/3"));		
			
			assertEquals(0, _store.size());
		}
	}
	
	public void test_DoublyNestedDataSequencePrinter_FirstAndSecondNodesStepOnce() throws Exception {

		// perform test for each of three directors compatible with the workflow
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector  }) {
			
			_setUp();

			_valueProducerNodeBuilder.stepsOnce();
			_valueCopierNodeBuilder.stepsOnce();
			
			final Workflow workflow = new WorkflowBuilder()
				.context(_context)
				.name("top")
				.node(new WorkflowNodeBuilder()
					.name("sub")
					.prefix("/sub{STEP}")
					.node(_triggerNodeBuilder)
					.node(new WorkflowNodeBuilder()
						.name("subsub")
						.prefix("/subsub{STEP}")
						.director(director)
						.inflow("/trigger", "/discard")
						.node(_valueProducerNodeBuilder)
						.node(_valueCopierNodeBuilder)
						.node(_valuePrinterNodeBuilder)
					)
				)
				.build();
			
			workflow.configure();
			workflow.initialize();
			
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout
			// confirm expected stdout
			assertEquals(
					"2" 	+ EOL +
					"2" 	+ EOL +
					"2" 	+ EOL,
					recorder.getStdoutRecording());

			assertEquals("", recorder.getStderrRecording());
			
			assertEquals("A", _store.take("/sub1/trigger/1"));
			assertEquals("B", _store.take("/sub1/trigger/2"));
			assertEquals("C", _store.take("/sub1/trigger/3"));

			assertEquals("A", _store.take("/sub1/subsub1/discard"));
			assertEquals("B", _store.take("/sub1/subsub2/discard"));
			assertEquals("C", _store.take("/sub1/subsub3/discard"));

			assertEquals(2, _store.take("/sub1/subsub1/value"));
			assertEquals(2, _store.take("/sub1/subsub1/copy"));
	
			assertEquals(2, _store.take("/sub1/subsub2/value"));
			assertEquals(2, _store.take("/sub1/subsub2/copy"));
	
			assertEquals(2, _store.take("/sub1/subsub3/value"));
			assertEquals(2, _store.take("/sub1/subsub3/copy"));
			
			assertEquals(0, _store.size());
		}
	}	
	
	public void test_DoublyNestedDataSequencePrinter_DirectorStepOnce() throws Exception {

		// perform test for each of three directors compatible with the workflow
		for (Director director : new Director[] {_publishSubscribeDirector, 
												 _dataDrivenDirector, 
												 _mtDataDrivenDirector
												 }) {
			_setUp();

			director.setNodesStepOnce(true);
			
			final Workflow workflow = new WorkflowBuilder()
				.name("top")
				.context(_context)
				.node(new WorkflowNodeBuilder()
					.name("sub")
					.prefix("/sub{STEP}")
					.node(_triggerNodeBuilder)
					.node(new WorkflowNodeBuilder()
						.name("subsub")
						.prefix("/subsub{STEP}")
						.director(director)
						.inflow("/trigger", "/discard")
						.node(_valueProducerNodeBuilder)
						.node(_valueCopierNodeBuilder)
						.node(_valuePrinterNodeBuilder)
					)
				)
				.build();
			
			workflow.configure();
			workflow.initialize();
			
			// run the workflow while capturing stdout and stderr 
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
				
			// confirm expected stdout
			// confirm expected stdout
			assertEquals(
					"2" 	+ EOL +
					"2" 	+ EOL +
					"2" 	+ EOL,
					recorder.getStdoutRecording());
					
			assertEquals("", recorder.getStderrRecording());

			assertEquals("A", _store.take("/sub1/trigger/1"));
			assertEquals("B", _store.take("/sub1/trigger/2"));
			assertEquals("C", _store.take("/sub1/trigger/3"));

			assertEquals("A", _store.take("/sub1/subsub1/discard"));
			assertEquals("B", _store.take("/sub1/subsub2/discard"));
			assertEquals("C", _store.take("/sub1/subsub3/discard"));

			assertEquals(2, _store.take("/sub1/subsub1/value"));
			assertEquals(2, _store.take("/sub1/subsub1/copy"));
	
			assertEquals(2, _store.take("/sub1/subsub2/value"));
			assertEquals(2, _store.take("/sub1/subsub2/copy"));
	
			assertEquals(2, _store.take("/sub1/subsub3/value"));
			assertEquals(2, _store.take("/sub1/subsub3/copy"));
			
			assertEquals(0, _store.size());
		}
	}
}

