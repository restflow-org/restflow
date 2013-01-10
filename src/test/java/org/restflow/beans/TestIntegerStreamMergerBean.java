package org.restflow.beans;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.InputProperty;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.directors.Director;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestIntegerStreamMergerBean extends RestFlowTestCase {
	
	private WorkflowContext			_context;
	private ConsumableObjectStore 	_store;
	private Director 				_publishSubscribeDirector;
	private Director 				_dataDrivenDirector;
	private Director 				_mtDataDrivenDirector;
	
	public void setUp() throws Exception {
		super.setUp();
		
		_publishSubscribeDirector 	= new PublishSubscribeDirector();
		_dataDrivenDirector 		= new DataDrivenDirector();
		_mtDataDrivenDirector 		= new MTDataDrivenDirector();
	}
	
	private void _setUp() throws Exception {
		
		_store = new ConsumableObjectStore();	 
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();

	}
	
	public void testMergeIntegerStreams_ThreeDirectors() throws Exception {
		
		for (Director director :  new Director[] { 	_publishSubscribeDirector,
													_dataDrivenDirector, 
													_mtDataDrivenDirector }) {
			
			_setUp();
			 
			final Workflow workflow = new WorkflowBuilder()
			
				.name("ConditionalRouting")
				.context(_context)
				.director(director)
				
				.node(new JavaNodeBuilder()
					.name("CreateFirstIntegerSequence")
					.sequence("value", new Object [] {1,3,5,6,9,15})
					.bean(new org.restflow.beans.ConstantSource())
					.outflow("value", "/stream/one"))
									
				.node(new ActorNodeBuilder()
					.name("CreateSecondIntegerSequence")
					.constant("initial", 2)
					.constant("step", 2)
					.constant("max", 10)
					.actor(new JavaActorBuilder()
						.bean(new org.restflow.beans.Ramp())
						.state("value")
						.state("first")
					)
					.outflow("value", "/stream/two")
					.endFlowOnNoOutput())

				.node(new ActorNodeBuilder()
					.name("MergeTwoIntegerSequences")
					.inflow("/stream/one", "a")
					.inflow("/stream/two", "b")
					.actor(new JavaActorBuilder()			
						.input("a", InputProperty.Optional |
									InputProperty.Nullable |
									InputProperty.DefaultReadinessFalse)
						.input("b", InputProperty.Optional |
									InputProperty.Nullable |
									InputProperty.DefaultReadinessFalse)
						.state("a")
						.state("b")
						.bean(new org.restflow.beans.IntegerStreamMerger())
						.stepMethod("selectLowerValue")
					)
					.outflow("c", "/stream/merged"))
					
				.node( new ActorNodeBuilder()
					.name("RenderMergedIntegerSequence")
					.constant("outputImmediately", true)
					.inflow("/stream/merged", "message")
					.actor(new JavaActorBuilder()
						.bean(new org.restflow.beans.PrintStreamMessageWriter())
						.stepMethod("renderMessage")))
						
				.build();
			
			workflow.configure();
			workflow.initialize();
	
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {
					workflow.run();
				}
			});
			
			workflow.wrapup();
			workflow.dispose();
			
			assertEquals(
					"1"		+ EOL +
					"2"		+ EOL +
					"3"		+ EOL +
					"4"		+ EOL +
					"5"		+ EOL +
					"6"		+ EOL +
					"6"		+ EOL +
					"8"		+ EOL +
					"9"		+ EOL +
					"10"	+ EOL +
					"15"	+ EOL, 
					recorder.getStdoutRecording());
			
			assertEquals("", recorder.getStderrRecording());
			
			assertEquals(1, 	_store.take("/stream/one/1"));
			assertEquals(3, 	_store.take("/stream/one/2"));
			assertEquals(5, 	_store.take("/stream/one/3"));
			assertEquals(6, 	_store.take("/stream/one/4"));
			assertEquals(9, 	_store.take("/stream/one/5"));
			assertEquals(15, 	_store.take("/stream/one/6"));
		
			assertEquals(2, 	_store.take("/stream/two/1"));
			assertEquals(4, 	_store.take("/stream/two/2"));
			assertEquals(6, 	_store.take("/stream/two/3"));
			assertEquals(8, 	_store.take("/stream/two/4"));
			assertEquals(10, 	_store.take("/stream/two/5"));

			assertEquals(1, 	_store.take("/stream/merged/1"));
			assertEquals(2, 	_store.take("/stream/merged/2"));
			assertEquals(3, 	_store.take("/stream/merged/3"));
			assertEquals(4, 	_store.take("/stream/merged/4"));
			assertEquals(5, 	_store.take("/stream/merged/5"));
			assertEquals(6, 	_store.take("/stream/merged/6"));
			assertEquals(6, 	_store.take("/stream/merged/7"));
			assertEquals(8, 	_store.take("/stream/merged/8"));
			assertEquals(9, 	_store.take("/stream/merged/9"));
			assertEquals(10, 	_store.take("/stream/merged/10"));
			assertEquals(15, 	_store.take("/stream/merged/11"));
			
			assertEquals(0, _store.size());
		}
	}

	public void testMergeIntegerStreams_ThreeDirectors_Minimal() throws Exception {
		
		for (Director director :  new Director[] { 	_publishSubscribeDirector,
													_dataDrivenDirector, 
													_mtDataDrivenDirector }) {
			_setUp();
			 
			final Workflow workflow = new WorkflowBuilder()
			
				.name("ConditionalRouting")
				.context(_context)
				.director(director)
				
				.node(new JavaNodeBuilder()
					.name("CreateFirstIntegerSequence")
					.sequence("value", new Object [] {1,3})
					.bean(new org.restflow.beans.ConstantSource())
					.outflow("value", "/stream/one"))
									
				.node(new ActorNodeBuilder()
					.name("CreateSecondIntegerSequence")
					.constant("initial", 2)
					.constant("step", 2)
					.constant("max", 2)
					.actor(new JavaActorBuilder()
						.bean(new org.restflow.beans.Ramp())
						.state("value")
						.state("first")
					)
					.outflow("value", "/stream/two")
					.endFlowOnNoOutput())

				.node(new ActorNodeBuilder()
					.name("MergeTwoIntegerSequences")
					.inflow("/stream/one", "a")
					.inflow("/stream/two", "b")
					.actor(new JavaActorBuilder()			
						.input("a", InputProperty.Optional |
									InputProperty.Nullable |
									InputProperty.DefaultReadinessFalse)
						.input("b", InputProperty.Optional |
									InputProperty.Nullable |
									InputProperty.DefaultReadinessFalse)
						.state("a")
						.state("b")
						.bean(new org.restflow.beans.IntegerStreamMerger())
						.stepMethod("selectLowerValue")
					)
					.outflow("c", "/stream/merged"))
					
				.node( new ActorNodeBuilder()
					.name("RenderMergedIntegerSequence")
					.constant("outputImmediately", true)
					.inflow("/stream/merged", "message")
					.actor(new JavaActorBuilder()
						.bean(new org.restflow.beans.PrintStreamMessageWriter())
						.stepMethod("renderMessage")))
						
				.build();
			
			workflow.configure();
			workflow.initialize();
	
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {
					workflow.run();
				}
			});
			
			workflow.wrapup();
			workflow.dispose();
			
			assertEquals(
					"1"		+ EOL +
					"2"		+ EOL +
					"3"		+ EOL, 
					recorder.getStdoutRecording());
		
			assertEquals("", recorder.getStderrRecording());
			
			assertEquals(1, 	_store.take("/stream/one/1"));
			assertEquals(3, 	_store.take("/stream/one/2"));
			assertEquals(2, 	_store.take("/stream/two/1"));
			assertEquals(1, 	_store.take("/stream/merged/1"));
			assertEquals(2, 	_store.take("/stream/merged/2"));
			assertEquals(3, 	_store.take("/stream/merged/3"));
			
			assertEquals(0, _store.size());
		}
	}
}
