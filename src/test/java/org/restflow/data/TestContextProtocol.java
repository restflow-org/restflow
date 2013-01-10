package org.restflow.data;

import org.restflow.RestFlow;
import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.SubworkflowBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.ContextProtocol;
import org.restflow.data.InflowProperty;
import org.restflow.nodes.GroovyNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestContextProtocol extends RestFlowTestCase {

	private ConsumableObjectStore 	_store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
	}
	
	public void testExternalProtocol() {
		ContextProtocol p = new ContextProtocol();
		assertTrue(p.isExternallyResolvable());
	}

	public void testImportMappings_SingleNodeYamlWorkflow() throws Exception {

		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {
				RestFlow.main(new String[]{"-f","classpath:protocols/context/DisplayContext.yaml",
						"-w","DisplayContext",
						"-base", "RESTFLOW_TESTRUNS_DIR",
						"-i","spreadsheetId=19980",
						"-import-map", "strategy=classpath:/strategy/"} );
			}
		});
		
		assertTrue("import map should be in stdout",
				recorder.getStdoutRecording().contains("strategy:classpath:/strategy/"));
	}
		
	public void testImportMappings_SingleNodeWorkflow() throws Exception {
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.importMapping("strategy", "classpath:/strategy/")
			.importMapping("myActors", "~/myactors/")
			.build();

		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new GroovyNodeBuilder()
				.inflow("context:/import-map", "importMap")
				.step(	"println importMap.get('strategy');		" +
						"println importMap.get('myActors');  	"))
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals(
				"classpath:/strategy/" 		+ EOL +
				"~/myactors/"				+ EOL, 
			recorder.getStdoutRecording());
	}
	
	public void testContextPropertyInflow_ContextProperty_SingleNodeWorkflow() throws Exception {
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.property("mycontextproperty", "valueofcontextproperty")
			.build();

		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new GroovyNodeBuilder()
				.inflow("context:/property/mycontextproperty", "propertyvalue")
				.step("println propertyvalue;"))
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals("valueofcontextproperty" + EOL, recorder.getStdoutRecording());
	}

	public void testContextPropertyInflow_SystemProperty_SingleNodeWorkflow() throws Exception {

		System.setProperty("mysystemproperty", "valueofsystemproperty");
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.build();

		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new GroovyNodeBuilder()
				.inflow("context:/property/mysystemproperty", "propertyvalue")
				.step("println propertyvalue;"))
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals("valueofsystemproperty" + EOL, recorder.getStdoutRecording());
	}
	
	public void testContextPropertyInflow_EnvironmentProperty_SingleNodeWorkflow() throws Exception {

		String key = System.getenv().keySet().iterator().next();
		String valueFromEnv = System.getenv(key);
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.build();

		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new GroovyNodeBuilder()
				.inflow("context:/property/" + key, "propertyvalue")
				.step("println propertyvalue;"))
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals(valueFromEnv + EOL, recorder.getStdoutRecording());
	}


	public void testContextPropertyInflow_MissingPropertyAtBuild_SingleNodeWorkflow() throws Exception {
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.build();

		SubworkflowBuilder workflowBuilder = new WorkflowBuilder() 
			.context(context)
			.node(new GroovyNodeBuilder()
				.inflow("context:/property/mycontextproperty", "propertyvalue")
				.step("println propertyvalue;"));
		
		Exception exception = null;
		
		try {
			workflowBuilder.build();
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("Undefined context property 'mycontextproperty' in inflow path '/property/mycontextproperty'", 
				exception.getMessage());
	}


	public void testContextPropertyInflow_MissingPropertyAtRun_SingleNodeWorkflow() throws Exception {
		
		System.setProperty("mysystemproperty", "valueofsystemproperty");

		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.build();

		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new GroovyNodeBuilder()
				.inflow("context:/property/mysystemproperty", "propertyvalue")
				.step("println propertyvalue;"))
				.build();

		workflow.configure();
		workflow.initialize();
		
		Exception exception = null;

		System.getProperties().remove("mysystemproperty");
		
		try {
			workflow.run();
		} catch (Exception e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("Undefined context property 'mysystemproperty' in inflow path '/property/mysystemproperty'", 
				exception.getMessage());
	}
	
	
	public void testContextPropertyInflow_MultiplePropertyTypes_SingleNodeWorkflow() throws Exception {

		System.setProperty("mysystemproperty", "valueofsystemproperty");

		String key = System.getenv().keySet().iterator().next();
		String valueFromEnv = System.getenv(key);
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.property("mycontextproperty", "valueofcontextproperty")
			.build();

		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new GroovyNodeBuilder()
				.inflow("context:/property/mycontextproperty", "contextpropertyvalue")
				.inflow("context:/property/mysystemproperty", "systempropertyvalue")
				.inflow("context:/property/" + key, "envpropertyvalue")
				.step("		println envpropertyvalue;		" +
					  "		println systempropertyvalue;	" +
					  "		println contextpropertyvalue;	" ))
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals( valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL,	recorder.getStdoutRecording());
	}
	

	public void testContextPropertyInflow_MultiplePropertyTypes_DoublyNestedSingleNodeWorkflow() throws Exception {

		System.setProperty("mysystemproperty", "valueofsystemproperty");

		String keyFromEnvironment = System.getenv().keySet().iterator().next();
		String valueFromEnv = System.getenv(keyFromEnvironment);
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.property("mycontextproperty", "valueofcontextproperty")
			.build();

		final Workflow workflow = new WorkflowBuilder()
			
			.context(context)
			
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				
				.node(new GroovyNodeBuilder()
					.name("trigger")
					.sequence("constant", new Object [] {"A", "B", "C"})
					.step("value=constant")
					.outflow("value", "/trigger"))
					
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.inflow("/trigger", "/discard")
	
					.node(new GroovyNodeBuilder()
						.inflow("context:/property/mycontextproperty", "contextpropertyvalue")
						.inflow("context:/property/mysystemproperty", "systempropertyvalue")
						.inflow("context:/property/" + keyFromEnvironment, "envpropertyvalue")
						.step("		println envpropertyvalue;		" +
							  "		println systempropertyvalue;	" +
							  "		println contextpropertyvalue;	" ))))
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals( valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL,	recorder.getStdoutRecording());
		
		assertEquals("A", _store.take("/sub1/trigger/1"));
		assertEquals("B", _store.take("/sub1/trigger/2"));
		assertEquals("C", _store.take("/sub1/trigger/3"));

		assertEquals("A", _store.take("/sub1/subsub1/discard"));
		assertEquals("B", _store.take("/sub1/subsub2/discard"));
		assertEquals("C", _store.take("/sub1/subsub3/discard"));
		
		assertEquals(valueFromEnv, _store.take("/sub1/subsub1/property/" + keyFromEnvironment));
		assertEquals(valueFromEnv, _store.take("/sub1/subsub2/property/" + keyFromEnvironment));
		assertEquals(valueFromEnv, _store.take("/sub1/subsub3/property/" + keyFromEnvironment));

		assertEquals("valueofcontextproperty", _store.take("/sub1/subsub1/property/mycontextproperty"));
		assertEquals("valueofcontextproperty", _store.take("/sub1/subsub2/property/mycontextproperty"));
		assertEquals("valueofcontextproperty", _store.take("/sub1/subsub3/property/mycontextproperty"));
		
		assertEquals("valueofsystemproperty", _store.take("/sub1/subsub1/property/mysystemproperty"));
		assertEquals("valueofsystemproperty", _store.take("/sub1/subsub2/property/mysystemproperty"));
		assertEquals("valueofsystemproperty", _store.take("/sub1/subsub3/property/mysystemproperty"));

		assertEquals(0, _store.size());
	}	
	
	public void testContextPropertyInflow_MultiplePropertyTypes_DoublyNestedTwoNodeWorkflow() throws Exception {

		System.setProperty("mysystemproperty", "valueofsystemproperty");

		String keyFromEnvironment = System.getenv().keySet().iterator().next();
		String valueFromEnv = System.getenv(keyFromEnvironment);
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.property("mycontextproperty", "valueofcontextproperty")
			.build();

		final Workflow workflow = new WorkflowBuilder()
			
			.context(context)
			
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				
				.node(new GroovyNodeBuilder()
					.name("trigger")
					.sequence("constant", new Object [] {"A", "B", "C"})
					.step("value=constant")
					.outflow("value", "/trigger"))
					
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.inflow("/trigger", "/discard")
	
					.node(new GroovyNodeBuilder()
						.sequence("constant", new Object [] {"sampleOne", "sampleTwo", "sampleThree"})
						.step("value=constant")
						.outflow("value", "/sampleName"))

					.node(new GroovyNodeBuilder()
						.inflow("context:/property/mycontextproperty", 
								"contextpropertyvalue", 
								InflowProperty.ReceiveOnce)
						.inflow("context:/property/mysystemproperty", 
								"systempropertyvalue", 
								InflowProperty.ReceiveOnce)
						.inflow("context:/property/" + keyFromEnvironment, 
								"envpropertyvalue", 
								InflowProperty.ReceiveOnce)
						.inflow("/sampleName", 
								"sample")
						.step("		println sample;					" +
							  "		println envpropertyvalue;		" +
							  "		println systempropertyvalue;	" +
							  "		println contextpropertyvalue;	" ))))
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm expected stdout
		assertEquals("sampleOne" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 "sampleTwo" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 "sampleThree" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 "sampleOne" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 "sampleTwo" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 "sampleThree" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 "sampleOne" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 "sampleTwo" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL +
					 "sampleThree" 				+ EOL +
					 valueFromEnv 				+ EOL +
					 "valueofsystemproperty" 	+ EOL +
					 "valueofcontextproperty" 	+ EOL,	recorder.getStdoutRecording());
		
		assertEquals("A", _store.take("/sub1/trigger/1"));
		assertEquals("B", _store.take("/sub1/trigger/2"));
		assertEquals("C", _store.take("/sub1/trigger/3"));

		assertEquals("A", _store.take("/sub1/subsub1/discard"));
		assertEquals("B", _store.take("/sub1/subsub2/discard"));
		assertEquals("C", _store.take("/sub1/subsub3/discard"));
		
		assertEquals(valueFromEnv, _store.take("/sub1/subsub1/property/" + keyFromEnvironment));
		assertEquals(valueFromEnv, _store.take("/sub1/subsub2/property/" + keyFromEnvironment));
		assertEquals(valueFromEnv, _store.take("/sub1/subsub3/property/" + keyFromEnvironment));

		assertEquals("valueofcontextproperty", _store.take("/sub1/subsub1/property/mycontextproperty"));
		assertEquals("valueofcontextproperty", _store.take("/sub1/subsub2/property/mycontextproperty"));
		assertEquals("valueofcontextproperty", _store.take("/sub1/subsub3/property/mycontextproperty"));
		
		assertEquals("valueofsystemproperty", _store.take("/sub1/subsub1/property/mysystemproperty"));
		assertEquals("valueofsystemproperty", _store.take("/sub1/subsub2/property/mysystemproperty"));
		assertEquals("valueofsystemproperty", _store.take("/sub1/subsub3/property/mysystemproperty"));
		
		assertEquals("sampleOne", _store.take("/sub1/subsub1/sampleName/1"));
		assertEquals("sampleOne", _store.take("/sub1/subsub2/sampleName/1"));
		assertEquals("sampleOne", _store.take("/sub1/subsub3/sampleName/1"));
		assertEquals("sampleTwo", _store.take("/sub1/subsub1/sampleName/2"));
		assertEquals("sampleTwo", _store.take("/sub1/subsub2/sampleName/2"));
		assertEquals("sampleTwo", _store.take("/sub1/subsub3/sampleName/2"));
		assertEquals("sampleThree", _store.take("/sub1/subsub1/sampleName/3"));
		assertEquals("sampleThree", _store.take("/sub1/subsub2/sampleName/3"));
		assertEquals("sampleThree", _store.take("/sub1/subsub3/sampleName/3"));

		System.out.println(_store);
		
		assertEquals(0, _store.size());
	}
}

