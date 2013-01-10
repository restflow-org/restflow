package org.restflow.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.SubworkflowBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.ContextProtocol;
import org.restflow.data.InflowProperty;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.directors.Director;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


@SuppressWarnings("unused")
public class TestReceiveOnceInflows extends RestFlowTestCase {
	
	public void test_TopLevelReceiveOnceInflow() throws Exception {

		for (Director director : new Director[] {
				new DataDrivenDirector(), 
				new PublishSubscribeDirector(),
				new MTDataDrivenDirector()    	}) {
		
			ConsumableObjectStore store = new ConsumableObjectStore();
			WorkflowContext context = new WorkflowContextBuilder()
				.store(store)
				.build();

			final Workflow workflow = new WorkflowBuilder()
	
				.name("OneShotInflow")
				.context(context)
				.director(director)
				
				.node(new JavaNodeBuilder()
					.name("CreateSingletonData")
					.constant("c", 5)
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplier"))
	
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
					
				.build();
			
			workflow.configure();
	
			for (int i = 0; i < 5; i++) {
			
				workflow.initialize();

				// run the workflow while capturing stdout and stderr 
				StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
					public void execute() throws Exception {workflow.run();}});
		
				// confirm expected stdout
				assertEquals(
						"15" 	+ EOL +
						"40"	+ EOL +
						"10"	+ EOL, 
					recorder.getStdoutRecording());
		
				assertEquals(
					"Warning:  Run 1 of workflow 'OneShotInflow' wrapped up with unused data packets:" 		+ EOL +
					"1 packet in inflow 'a' on node 'MultiplySequenceBySingleton' with URI '/multiplier'"	+ EOL,
					recorder.getStderrRecording());
				
				// confirm expected published data
				assertEquals(5, 	store.take("/multiplier"));
				assertEquals(3, 	store.take("/multiplicand/1"));
				assertEquals(8, 	store.take("/multiplicand/2"));
				assertEquals(2, 	store.take("/multiplicand/3"));
				assertEquals(15, 	store.take("/product/1"));
				assertEquals(40, 	store.take("/product/2"));
				assertEquals(10, 	store.take("/product/3"));
	
				assertTrue(store.isEmpty());
	
				workflow.reset();
			}
		}
	}
	
	public void test_TwoTopLevelReceiveOnceInflows() throws Exception {
		
		for (Director director : new Director[] {
				new DataDrivenDirector(), 
				new PublishSubscribeDirector(),
				new MTDataDrivenDirector()    	}) {

			ConsumableObjectStore store = new ConsumableObjectStore();
			WorkflowContext context = new WorkflowContextBuilder()
				.store(store)
				.build();
	
			final Workflow workflow = new WorkflowBuilder()
				.name("TwoTopLevelReceiveOnceInflows")
				.context(context)
				.director(director)
			
				.node(new JavaNodeBuilder()
					.name("MultiplierSource")
					.constant("c", 5)
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplier"))
	
				.node(new JavaNodeBuilder()
					.name("MultiplicandSource")
					.constant("c", 3)
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplicand"))
				
				.node(new JavaNodeBuilder()
					.name("OffsetSource")
					.sequence("c", new Integer[] {1, 2, 3})
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/offset"))
					
				.node(new JavaNodeBuilder()
					.name("MultiplyAndOffset")
					.inflow("/multiplier", "a", InflowProperty.ReceiveOnce)
					.inflow("/multiplicand", "b", InflowProperty.ReceiveOnce)
					.inflow("/offset", "c")
					.bean(new Object() {
						public int a, b, c, d;
						public void step() { d = a * b + c; }
					})
					.outflow("d", "/result"))
	
				.node(new JavaNodeBuilder()
					.name("PrintResult")
					.inflow("/result", "v")
					.bean(new Object() {
						public int v;
						public void step() { System.out.println(v); }
					}))
					
				.build();
	
			workflow.configure();
	
			for (int i = 0; i < 5; i++) {
				
				workflow.initialize();

				// run the workflow while capturing stdout and stderr 
				StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
					public void execute() throws Exception {workflow.run();}});
		
				// confirm expected stdout
				assertEquals(
						"16" 	+ EOL + 
						"17" 	+ EOL +
						"18" 	+ EOL,
					recorder.getStdoutRecording());
		
				assertEquals(
					"Warning:  Run 1 of workflow 'TwoTopLevelReceiveOnceInflows' wrapped up with unused data packets:" 	+ EOL +
					"1 packet in inflow 'a' on node 'MultiplyAndOffset' with URI '/multiplier'" 						+ EOL +
					"1 packet in inflow 'b' on node 'MultiplyAndOffset' with URI '/multiplicand'"						+ EOL,
					recorder.getStderrRecording());
				
				// confirm expected published data
				assertEquals(5, 	store.take("/multiplier"));
				assertEquals(3, 	store.take("/multiplicand"));
				assertEquals(1, 	store.take("/offset/1"));
				assertEquals(2, 	store.take("/offset/2"));
				assertEquals(3, 	store.take("/offset/3"));
				assertEquals(16, 	store.take("/result/1"));
				assertEquals(17, 	store.take("/result/2"));
				assertEquals(18,	store.take("/result/3"));
				
				assertTrue(store.isEmpty());
	
				workflow.reset();
			}
		}
	}
	
	public void test_TwoTopLevelReceiveOnceInflows_OneFromContext() throws Exception {

		for (Director director : new Director[] {
				new DataDrivenDirector(), 
				new PublishSubscribeDirector(),
				new MTDataDrivenDirector()    	}) {

			ConsumableObjectStore store = new ConsumableObjectStore();
			
			WorkflowContext context = new WorkflowContextBuilder()
				.scheme("context", new ContextProtocol())
				.property("multiplicand", 3)
				.store(store)
				.build();
		
			final Workflow workflow = new WorkflowBuilder()
				.name("TwoTopLevelReceiveOnceInflows")
				.context(context)
				.director(director)
			
				.node(new JavaNodeBuilder()
					.constant("c", 5)
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplier"))
				
				.node(new JavaNodeBuilder()
					.sequence("c", new Integer[] {1, 2, 3})
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/offset"))
					
				.node(new JavaNodeBuilder()
					.name("MultiplyAndOffset")
					.inflow("/multiplier", "a", InflowProperty.ReceiveOnce)
					.inflow("context:/property/multiplicand", "b", InflowProperty.ReceiveOnce)
					.inflow("/offset", "c")
					.bean(new Object() {
						public int a, b, c, d;
						public void step() { d = a * b + c; }
					})
					.outflow("d", "/result"))
	
				.node(new JavaNodeBuilder()
					.inflow("/result", "v")
					.bean(new Object() {
						public int v;
						public void step() { System.out.println(v); }
					}))
					
				.build();
	
			workflow.configure();
	
			for (int i = 0; i < 5; i++) {
				
				workflow.initialize();

				// run the workflow while capturing stdout and stderr 
				StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
					public void execute() throws Exception {workflow.run();}});
		
				// confirm expected stdout
				assertEquals(
						"16" 	+ EOL + 
						"17" 	+ EOL +
						"18" 	+ EOL,
					recorder.getStdoutRecording());
		
				assertEquals(
					"Warning:  Run 1 of workflow 'TwoTopLevelReceiveOnceInflows' wrapped up with unused data packets:" 	+ EOL +
					"1 packet in inflow 'a' on node 'MultiplyAndOffset' with URI '/multiplier'" 						+ EOL +
					"1 packet in inflow 'b' on node 'MultiplyAndOffset' with URI '/property/multiplicand'"				+ EOL,
					recorder.getStderrRecording());
				
				// confirm expected published data
				assertEquals(5, 	store.take("/multiplier"));
				assertEquals(3, 	store.take("/property/multiplicand"));
				assertEquals(1, 	store.take("/offset/1"));
				assertEquals(2, 	store.take("/offset/2"));
				assertEquals(3, 	store.take("/offset/3"));
				assertEquals(16, 	store.take("/result/1"));
				assertEquals(17, 	store.take("/result/2"));
				assertEquals(18,	store.take("/result/3"));
				
				assertTrue(store.isEmpty());
	
				workflow.reset();
			}
		}
	}
	
	public void test_NestedReceiveOnceInflow() throws Exception {

		for (Director director : new Director[] {
				new DataDrivenDirector(), 
				new PublishSubscribeDirector(),
				new MTDataDrivenDirector()    	}) {

			ConsumableObjectStore store = new ConsumableObjectStore();
			WorkflowContext context = new WorkflowContextBuilder()
				.store(store)
				.build();
			
			final Workflow workflow = new WorkflowBuilder()
	
				.name("TopWF")
				.context(context)
	
				.node(new JavaNodeBuilder()
					.name("CreateMultipliers")
					.sequence("c", new Integer[] {5, 10, 15})
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplier"))
	
				.node(new WorkflowNodeBuilder()
				
					.name("SubWF")
					.prefix("/sub{RUN}/")
					.inflow("/multiplier", "/multiplier")
					.director(director)
	
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
				
				.build();
	
			workflow.configure();
			
			for (int i = 0; i < 5; i++) {
				
				workflow.initialize();

				// run the workflow while capturing stdout and stderr 
				StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
					public void execute() throws Exception {workflow.run();}});
		
				// confirm expected stdout
				assertEquals(
						"15" 	+ EOL +
						"40"	+ EOL +
						"10"	+ EOL +
						"30" 	+ EOL +
						"80"	+ EOL +
						"20"	+ EOL +
						"45" 	+ EOL +
						"120"	+ EOL +
						"30"	+ EOL, 
					recorder.getStdoutRecording());
							
				assertEquals(5,   store.take("/multiplier/1"));
				assertEquals(10,  store.take("/multiplier/2"));
				assertEquals(15,  store.take("/multiplier/3"));
				assertEquals(5,   store.take("/sub1/multiplier"));
				assertEquals(10,  store.take("/sub2/multiplier"));
				assertEquals(15,  store.take("/sub3/multiplier"));
				assertEquals(3,   store.take("/sub1/multiplicand/1"));
				assertEquals(8,   store.take("/sub1/multiplicand/2"));
				assertEquals(2,   store.take("/sub1/multiplicand/3"));
				assertEquals(3,   store.take("/sub2/multiplicand/1"));
				assertEquals(8,   store.take("/sub2/multiplicand/2"));
				assertEquals(2,   store.take("/sub2/multiplicand/3"));
				assertEquals(3,   store.take("/sub3/multiplicand/1"));
				assertEquals(8,   store.take("/sub3/multiplicand/2"));
				assertEquals(2,   store.take("/sub3/multiplicand/3"));
				assertEquals(15,  store.take("/sub1/product/1"));
				assertEquals(40,  store.take("/sub1/product/2"));
				assertEquals(10,  store.take("/sub1/product/3"));
				assertEquals(30,  store.take("/sub2/product/1"));
				assertEquals(80,  store.take("/sub2/product/2"));
				assertEquals(20,  store.take("/sub2/product/3"));
				assertEquals(45,  store.take("/sub3/product/1"));
				assertEquals(120, store.take("/sub3/product/2"));
				assertEquals(30,  store.take("/sub3/product/3"));
	
				// confirm expected published data
				assertTrue(store.isEmpty());
	
				workflow.reset();
			}
		}
	}
	
	public void test_NestedReceiveOnceInflows_OneFromContext() throws Exception {

		for (Director director : new Director[] {
				new DataDrivenDirector(), 
				new PublishSubscribeDirector(),
				new MTDataDrivenDirector()    	}) {

			ConsumableObjectStore store = new ConsumableObjectStore();
			
			WorkflowContext context = new WorkflowContextBuilder()
				.scheme("context", new ContextProtocol())
				.property("offset", 5)
				.store(store)
				.build();
			
			final Workflow workflow = new WorkflowBuilder()
	
				.name("TopWF")
				.context(context)
				
				.node(new JavaNodeBuilder()
					.name("CreateMultipliers")
					.sequence("c", new Integer[] {5, 10, 15})
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplier"))
					
				.node(new WorkflowNodeBuilder()
				
					.name("SubWF")
					.prefix("/sub{RUN}/")
					.director(director)
					.inflow("/multiplier", "/multiplier")
		
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
						.inflow("context:/property/offset", "c", InflowProperty.ReceiveOnce)
						.bean(new Object() {
							public int a, b, c, d;
							public void step() { d = a * b + c; }
						})
						.outflow("d", "/product"))
		
					.node(new JavaNodeBuilder()
						.name("RenderProducts")
						.inflow("/product", "v")
						.bean(new Object() {
							public int v;
							public void step() { System.out.println(v); }
						}))
				)
				
				.build();
	
			workflow.configure();
			
			for (int i = 0; i < 5; i++) {
				
				workflow.initialize();

				// run the workflow while capturing stdout and stderr 
				StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
					public void execute() throws Exception {workflow.run();}});
		
				// confirm expected stdout
				assertEquals(
						"20" 	+ EOL +
						"45"	+ EOL +
						"15"	+ EOL +
						"35" 	+ EOL +
						"85"	+ EOL +
						"25"	+ EOL +
						"50" 	+ EOL +
						"125"	+ EOL +
						"35"	+ EOL, 
					recorder.getStdoutRecording());
							
				assertEquals(5,   store.take("/multiplier/1"));
				assertEquals(10,  store.take("/multiplier/2"));
				assertEquals(15,  store.take("/multiplier/3"));
				assertEquals(5,   store.take("/sub1/multiplier"));
				assertEquals(10,  store.take("/sub2/multiplier"));
				assertEquals(15,  store.take("/sub3/multiplier"));
				assertEquals(5,  store.take("/sub1/property/offset"));
				assertEquals(5,  store.take("/sub2/property/offset"));
				assertEquals(5,  store.take("/sub3/property/offset"));
				assertEquals(3,   store.take("/sub1/multiplicand/1"));
				assertEquals(8,   store.take("/sub1/multiplicand/2"));
				assertEquals(2,   store.take("/sub1/multiplicand/3"));
				assertEquals(3,   store.take("/sub2/multiplicand/1"));
				assertEquals(8,   store.take("/sub2/multiplicand/2"));
				assertEquals(2,   store.take("/sub2/multiplicand/3"));
				assertEquals(3,   store.take("/sub3/multiplicand/1"));
				assertEquals(8,   store.take("/sub3/multiplicand/2"));
				assertEquals(2,   store.take("/sub3/multiplicand/3"));
				assertEquals(20,  store.take("/sub1/product/1"));
				assertEquals(45,  store.take("/sub1/product/2"));
				assertEquals(15,  store.take("/sub1/product/3"));
				assertEquals(35,  store.take("/sub2/product/1"));
				assertEquals(85,  store.take("/sub2/product/2"));
				assertEquals(25,  store.take("/sub2/product/3"));
				assertEquals(50,  store.take("/sub3/product/1"));
				assertEquals(125, store.take("/sub3/product/2"));
				assertEquals(35,  store.take("/sub3/product/3"));
	
				// confirm expected published data
				assertEquals(0, store.size());
	
				workflow.reset();
			}
		}
	}	
	
	public void test_DoublyNestedReceiveOnceInflow() throws Exception {

		for (Director director : new Director[] {
				new DataDrivenDirector(), 
				new PublishSubscribeDirector(),
				new MTDataDrivenDirector()    	}) {

			ConsumableObjectStore store = new ConsumableObjectStore();
			WorkflowContext context = new WorkflowContextBuilder()
				.store(store)
				.build();
			
			final Workflow workflow = new WorkflowBuilder()
			
				.name("TopWF")
				.context(context)
				
				.node(new JavaNodeBuilder()
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
						.director(director)
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
			
			workflow.configure();
			
			for (int i = 0; i < 5; i++) {
				
				workflow.initialize();

				// run the workflow while capturing stdout and stderr 
				StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
					public void execute() throws Exception {workflow.run();}});
		
				// confirm expected stdout
				assertEquals(
						"30" 	+ EOL +
						"80"	+ EOL +
						"20"	+ EOL +
						"60" 	+ EOL +
						"160"	+ EOL +
						"40"	+ EOL +
						"90" 	+ EOL +
						"240"	+ EOL +
						"60"	+ EOL, 
					recorder.getStdoutRecording());
				
				assertEquals(5,   store.take("/topmultiplier/1"));
				assertEquals(10,  store.take("/topmultiplier/2"));
				assertEquals(15,  store.take("/topmultiplier/3"));
				assertEquals(5,   store.take("/Sub1/subinmultiplier"));
				assertEquals(10,  store.take("/Sub2/subinmultiplier"));
				assertEquals(15,  store.take("/Sub3/subinmultiplier"));
				assertEquals(10,  store.take("/Sub1/doubledmultiplier"));
				assertEquals(20,  store.take("/Sub2/doubledmultiplier"));
				assertEquals(30,  store.take("/Sub3/doubledmultiplier"));			
				assertEquals(10,  store.take("/Sub1/TopWF.SubWF.SubSubWF/multiplier"));
				assertEquals(20,  store.take("/Sub2/TopWF.SubWF.SubSubWF/multiplier"));
				assertEquals(30,  store.take("/Sub3/TopWF.SubWF.SubSubWF/multiplier"));
				assertEquals(3,   store.take("/Sub1/TopWF.SubWF.SubSubWF/multiplicand/1"));
				assertEquals(8,   store.take("/Sub1/TopWF.SubWF.SubSubWF/multiplicand/2"));
				assertEquals(2,   store.take("/Sub1/TopWF.SubWF.SubSubWF/multiplicand/3"));
				assertEquals(3,   store.take("/Sub2/TopWF.SubWF.SubSubWF/multiplicand/1"));
				assertEquals(8,   store.take("/Sub2/TopWF.SubWF.SubSubWF/multiplicand/2"));
				assertEquals(2,   store.take("/Sub2/TopWF.SubWF.SubSubWF/multiplicand/3"));
				assertEquals(3,   store.take("/Sub3/TopWF.SubWF.SubSubWF/multiplicand/1"));
				assertEquals(8,   store.take("/Sub3/TopWF.SubWF.SubSubWF/multiplicand/2"));
				assertEquals(2,   store.take("/Sub3/TopWF.SubWF.SubSubWF/multiplicand/3"));			
				assertEquals(30,  store.take("/Sub1/TopWF.SubWF.SubSubWF/product/1"));
				assertEquals(80,  store.take("/Sub1/TopWF.SubWF.SubSubWF/product/2"));
				assertEquals(20,  store.take("/Sub1/TopWF.SubWF.SubSubWF/product/3"));
				assertEquals(60,  store.take("/Sub2/TopWF.SubWF.SubSubWF/product/1"));
				assertEquals(160, store.take("/Sub2/TopWF.SubWF.SubSubWF/product/2"));
				assertEquals(40,  store.take("/Sub2/TopWF.SubWF.SubSubWF/product/3"));
				assertEquals(90,  store.take("/Sub3/TopWF.SubWF.SubSubWF/product/1"));
				assertEquals(240, store.take("/Sub3/TopWF.SubWF.SubSubWF/product/2"));
				assertEquals(60,  store.take("/Sub3/TopWF.SubWF.SubSubWF/product/3"));
				
				assertTrue(store.isEmpty());
	
				workflow.reset();
			}
		}
	}
	
	
	public void test_DoublyNestedReceiveOnceInflows_WithContextProtocol() throws Exception {

		for (Director director : new Director[] {
				new DataDrivenDirector(), 
				new PublishSubscribeDirector(),
				new MTDataDrivenDirector()    	}) {

			ConsumableObjectStore store = new ConsumableObjectStore();

			WorkflowContext context = new WorkflowContextBuilder()
				.scheme("context", new ContextProtocol())
				.property("multiplierFactor", 2)
				.property("offset", 5)
				.store(store)
				.build();
				
			final Workflow workflow = new WorkflowBuilder()
			
				.name("TopWF")
				.context(context)
				
				.node(new JavaNodeBuilder()
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
						.inflow("context:/property/multiplierFactor", "factor")
						.stepsOnce()
						.bean(new Object() {
							public int c, factor, v;
							public void step() { v = factor * c; }
						})
						.outflow("v", "/doubledmultiplier"))
		
					.node(new WorkflowNodeBuilder()
						.name("SubSubWF")
						.stepsOnce()
						.director(director)
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
							.inflow("context:/property/offset", "c", InflowProperty.ReceiveOnce)
							.bean(new Object() {
								public int a, b, c, d;
								public void step() { d = a * b + c; }
							})
							.outflow("d", "/product"))
			
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
			
			workflow.configure();
			
			for (int i = 0; i < 5; i++) {
				
				workflow.initialize();

				// run the workflow while capturing stdout and stderr 
				StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
					public void execute() throws Exception {workflow.run();}});
		
				// confirm expected stdout
				assertEquals(
						"35" 	+ EOL +
						"85"	+ EOL +
						"25"	+ EOL +
						"65" 	+ EOL +
						"165"	+ EOL +
						"45"	+ EOL +
						"95" 	+ EOL +
						"245"	+ EOL +
						"65"	+ EOL, 
					recorder.getStdoutRecording());
					
				assertEquals(5,   store.take("/topmultiplier/1"));
				assertEquals(10,  store.take("/topmultiplier/2"));
				assertEquals(15,  store.take("/topmultiplier/3"));
				assertEquals(5,   store.take("/Sub1/subinmultiplier"));
				assertEquals(10,  store.take("/Sub2/subinmultiplier"));
				assertEquals(15,  store.take("/Sub3/subinmultiplier"));
				assertEquals(2,   store.take("/Sub1/property/multiplierFactor"));
				assertEquals(2,   store.take("/Sub2/property/multiplierFactor"));
				assertEquals(2,   store.take("/Sub3/property/multiplierFactor"));			
				assertEquals(10,  store.take("/Sub1/doubledmultiplier"));
				assertEquals(20,  store.take("/Sub2/doubledmultiplier"));
				assertEquals(30,  store.take("/Sub3/doubledmultiplier"));
				assertEquals(10,  store.take("/Sub1/TopWF.SubWF.SubSubWF/multiplier"));
				assertEquals(20,  store.take("/Sub2/TopWF.SubWF.SubSubWF/multiplier"));
				assertEquals(30,  store.take("/Sub3/TopWF.SubWF.SubSubWF/multiplier"));
				assertEquals(5,  store.take("/Sub1/TopWF.SubWF.SubSubWF/property/offset"));
				assertEquals(5,  store.take("/Sub2/TopWF.SubWF.SubSubWF/property/offset"));
				assertEquals(5,  store.take("/Sub3/TopWF.SubWF.SubSubWF/property/offset"));
				assertEquals(3,   store.take("/Sub1/TopWF.SubWF.SubSubWF/multiplicand/1"));
				assertEquals(8,   store.take("/Sub1/TopWF.SubWF.SubSubWF/multiplicand/2"));
				assertEquals(2,   store.take("/Sub1/TopWF.SubWF.SubSubWF/multiplicand/3"));
				assertEquals(3,   store.take("/Sub2/TopWF.SubWF.SubSubWF/multiplicand/1"));
				assertEquals(8,   store.take("/Sub2/TopWF.SubWF.SubSubWF/multiplicand/2"));
				assertEquals(2,   store.take("/Sub2/TopWF.SubWF.SubSubWF/multiplicand/3"));
				assertEquals(3,   store.take("/Sub3/TopWF.SubWF.SubSubWF/multiplicand/1"));
				assertEquals(8,   store.take("/Sub3/TopWF.SubWF.SubSubWF/multiplicand/2"));
				assertEquals(2,   store.take("/Sub3/TopWF.SubWF.SubSubWF/multiplicand/3"));			
				assertEquals(35,  store.take("/Sub1/TopWF.SubWF.SubSubWF/product/1"));
				assertEquals(85,  store.take("/Sub1/TopWF.SubWF.SubSubWF/product/2"));
				assertEquals(25,  store.take("/Sub1/TopWF.SubWF.SubSubWF/product/3"));
				assertEquals(65,  store.take("/Sub2/TopWF.SubWF.SubSubWF/product/1"));
				assertEquals(165, store.take("/Sub2/TopWF.SubWF.SubSubWF/product/2"));
				assertEquals(45,  store.take("/Sub2/TopWF.SubWF.SubSubWF/product/3"));
				assertEquals(95,  store.take("/Sub3/TopWF.SubWF.SubSubWF/product/1"));
				assertEquals(245, store.take("/Sub3/TopWF.SubWF.SubSubWF/product/2"));
				assertEquals(65,  store.take("/Sub3/TopWF.SubWF.SubSubWF/product/3"));
				
				assertEquals(0, store.size());
	
				workflow.reset();
			}
		}
	}
	
	public void test_NestedWorkflowNodeReceivesOnce() throws Exception {

		for (Director director : new Director[] {
				new DataDrivenDirector(), 
				new PublishSubscribeDirector(),
				new MTDataDrivenDirector()    	}) {

			ConsumableObjectStore store = new ConsumableObjectStore();
			WorkflowContext context = new WorkflowContextBuilder()
				.store(store)
				.build();
			
			final Workflow workflow = new WorkflowBuilder()
			
				.name("TopWF")
				.context(context)
				.director(director)
				
				.node(new JavaNodeBuilder()
					.sequence("c", new Integer[] {5, 10, 15})
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplier"))
				
				.node(new JavaNodeBuilder()
					.constant("c", 3)
					.bean(new Object() {
						public int c, v;
						public void step() { v = c; }
					})
					.outflow("v", "/multiplicand"))
					
				.node(new WorkflowNodeBuilder()
					.name("SubWF")
					.prefix("/Sub{RUN}")
					.inflow("/multiplier", "/multiplier")
					.inflow("/multiplicand", "/multiplicand", InflowProperty.ReceiveOnce)
					.outflow("/product", "/product")
					
					.node(new JavaNodeBuilder()
						.inflow("/multiplier", "a")
						.inflow("/multiplicand", "b")
						.stepsOnce()
						.bean(new Object() {
							public int a, b, c;
							public void step() { c = a * b; }
						})
						.outflow("c", "/product"))	
				)
				
				.node(new JavaNodeBuilder()
					.inflow("/product", "v")
					.bean(new Object() {
						public int v;
						public void step() { System.out.println(v); }
					}))
				
				.build();
	
			workflow.configure();
			
			for (int i = 0; i < 5; i++) {
				
				workflow.initialize();
				
				// run the workflow while capturing stdout and stderr 
				StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
					public void execute() throws Exception {workflow.run();}});
		
				// confirm expected stdout
				assertEquals(
						"15" 	+ EOL +
						"30"	+ EOL +
						"45"	+ EOL, 
					recorder.getStdoutRecording());
							
				assertEquals(3,		store.take("/multiplicand"));
				assertEquals(5,   	store.take("/multiplier/1"));
				assertEquals(10,  	store.take("/multiplier/2"));
				assertEquals(15,  	store.take("/multiplier/3"));
				assertEquals(5,   	store.take("/Sub1/multiplier"));
				assertEquals(10,  	store.take("/Sub2/multiplier"));
				assertEquals(15,  	store.take("/Sub3/multiplier"));
				assertEquals(3,   	store.take("/Sub1/multiplicand"));
				assertEquals(3,   	store.take("/Sub2/multiplicand"));
				assertEquals(3,   	store.take("/Sub3/multiplicand"));
				
				assertEquals(15,  	store.take("/Sub1/product"));
				assertEquals(30,  	store.take("/Sub2/product"));
				assertEquals(45,  	store.take("/Sub3/product"));
				assertEquals(15,   	store.take("/product/1"));
				assertEquals(30,   	store.take("/product/2"));
				assertEquals(45,   	store.take("/product/3"));
				
				// confirm expected published data
				assertTrue(store.isEmpty());
	
				workflow.reset();
			}
		}
	}
}
