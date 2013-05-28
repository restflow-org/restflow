package org.restflow.data;

import java.util.Map;

import org.restflow.RestFlow;
import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.SubworkflowBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.ContextProtocol;
import org.restflow.data.InflowProperty;
import org.restflow.nodes.JavaNodeBuilder;
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
				recorder.getStdoutRecording().contains("strategy=classpath:/strategy/"));
	}
		
	public void testImportMappings_SingleNodeWorkflow() throws Exception {
		
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("context", new ContextProtocol())
			.importMapping("strategy", "classpath:/strategy/")
			.importMapping("myActors", "~/myactors/")
			.build();

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.inflow("context:/import-map", "importMap")
				.bean(new Object() {
					public Map<String,String> importMap;
					public void step() {
						System.out.println(importMap.get("strategy"));
						System.out.println(importMap.get("myActors"));
					}
				}))
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

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.inflow("context:/property/mycontextproperty", "propertyvalue")
				.bean(new Object() {
					public String propertyvalue;
					public void step() {	
						System.out.println(propertyvalue);
					}
				}))
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

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.inflow("context:/property/mysystemproperty", "propertyvalue")
				.bean(new Object() {
					public String propertyvalue;
					public void step() {
						System.out.println(propertyvalue);
					}
				}))
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

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.inflow("context:/property/" + key, "propertyvalue")
				.bean(new Object() {
					public String propertyvalue;
					public void step() {
						System.out.println(propertyvalue);
					}
				}))
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

		@SuppressWarnings("unused")
		SubworkflowBuilder workflowBuilder = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.inflow("context:/property/mycontextproperty", "propertyvalue")
				.bean(new Object() {
					public String propertyvalue;
					public void step() {
						System.out.println(propertyvalue);
					}
				}));
		
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

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.inflow("context:/property/mysystemproperty", "propertyvalue")
				.bean(new Object() {
					public String propertyvalue;
					public void step() {
						System.out.println(propertyvalue);
					}
				}))
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

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.inflow("context:/property/mycontextproperty", "contextpropertyvalue")
				.inflow("context:/property/mysystemproperty", "systempropertyvalue")
				.inflow("context:/property/" + key, "envpropertyvalue")
				.bean(new Object() {
					public String envpropertyvalue;
					public String systempropertyvalue;
					public String contextpropertyvalue;
					public void step() {
						System.out.println(envpropertyvalue);
						System.out.println(systempropertyvalue);
						System.out.println(contextpropertyvalue);
					}
				}))
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

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()
			
			.context(context)
			
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				
				.node(new JavaNodeBuilder()
					.name("trigger")
					.sequence("constant", new Object [] {"A", "B", "C"})
					.bean(new Object() {
						public String constant, value;
						public void step() {
							value = constant;
						}
					})
					.outflow("value", "/trigger"))
					
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.inflow("/trigger", "/discard")
	
					.node(new JavaNodeBuilder()
						.inflow("context:/property/mycontextproperty", "contextpropertyvalue")
						.inflow("context:/property/mysystemproperty", "systempropertyvalue")
						.inflow("context:/property/" + keyFromEnvironment, "envpropertyvalue")
						.bean(new Object() {
							public String envpropertyvalue;
							public String systempropertyvalue;
							public String contextpropertyvalue;
							public void step() {
								System.out.println(envpropertyvalue);
								System.out.println(systempropertyvalue);
								System.out.println(contextpropertyvalue);
							}
						}))
					)
				)
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

		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()
			
			.context(context)
			
			.node(new WorkflowNodeBuilder()
				.prefix("/sub{STEP}")
				
				.node(new JavaNodeBuilder()
					.name("trigger")
					.sequence("constant", new Object [] {"A", "B", "C"})
					.bean(new Object() {
						public String constant, value;
						public void step() {
							value = constant;
						}})
					.outflow("value", "/trigger"))
					
				.node(new WorkflowNodeBuilder()
					.prefix("/subsub{STEP}")
					.inflow("/trigger", "/discard")
	
					.node(new JavaNodeBuilder()
						.sequence("constant", new Object [] {"sampleOne", "sampleTwo", "sampleThree"})
						.bean(new Object() {
							public String constant, value;
							public void step() {
								value = constant;
							}})
						.outflow("value", "/sampleName"))

					.node(new JavaNodeBuilder()
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
						.bean(new Object() {
							public String sample;
							public String envpropertyvalue;
							public String systempropertyvalue;
							public String contextpropertyvalue;
							public void step() {
								System.out.println(sample);
								System.out.println(envpropertyvalue);
								System.out.println(systempropertyvalue);
								System.out.println(contextpropertyvalue);
							}
						}))))
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

