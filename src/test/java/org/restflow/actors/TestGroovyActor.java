package org.restflow.actors;

import java.util.Map;

import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.GroovyActor;
import org.restflow.test.RestFlowTestCase;
import org.springframework.context.ApplicationContext;
import org.yaml.snakeyaml.Yaml;


public class TestGroovyActor extends RestFlowTestCase {

	private GroovyActor actor;
	private Yaml yaml;
	
	private String EOL = System.getProperty("line.separator");
	
	public void setUp() throws Exception {
		super.setUp();
		actor = new GroovyActor();
		actor.setName("TestActor");
		yaml = new Yaml();
		ApplicationContext context = new WorkflowContextBuilder().build();
		actor.setApplicationContext(context);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void _setActorInputsViaYaml(String yamlString) throws Exception {
		actor.setInputs((Map)yaml.load(yamlString));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void _setActorOutputsViaYaml(String yamlString) throws Exception {
		actor.setOutputs((Map)yaml.load(yamlString));
	}


	public void testSetInputs_nameAndDefault() throws Exception {

		_setActorInputsViaYaml(
			"a:" 								+ EOL +
	    	"  default: the default value"		+ EOL
		);

		_setActorOutputsViaYaml(
	    	"out:"								+ EOL
		);

		actor.setStep(
			"out = a;"							+ EOL
		);

		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		actor.step();
		
		assertEquals(actor.getOutputValue("out"),"the default value");
	}
	
//	public void testSetInputs_nameAndAccrue() throws Exception {
//
//	    Map<String, Object> inputs = (Map) yaml.load(
//	    		"a:\n" +
//	    		"  accrue: 2");
//
//		actor.setInputs(inputs);		
//		actor.initialize();
//		actor.setInputValue("a", "FIRST");
//		assertTrue("Should need one more data point",actor.readyForInput("a"));
//		actor.setInputValue("a", "SECOND");
//		assertFalse("Should enough data",actor.readyForInput("a"));		
//		
//		actor.setStep("first = a[0]; second = a[1]");
//		actor.step();
//		
//		assertEquals(actor.getOutputValue("first"),"FIRST");
//		assertEquals(actor.getOutputValue("second"),"SECOND");
//		
//		assertTrue("Should need one more data after fire",actor.readyForInput("a"));
//
//	}


	public void testSetInputs_testSetInputValue() throws Exception {

		_setActorInputsViaYaml(
			"a:" 				 	+ EOL
		);
		
		_setActorOutputsViaYaml(
			"out:"					+ EOL
		);

		actor.setStep(
			"out = a;"				+ EOL
		);

		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		actor.setInputValue("a", 3);
		actor.step();
		
		assertEquals(3, actor.getOutputValue("out") );
	}
	
	public void testStep_MissingOutput() throws Exception {

		_setActorOutputsViaYaml(
			"a:" 					+ EOL + 
			"b:" 					+ EOL +
			"c:"
		);

		actor.setStep(
			"a = 1;" 				+ EOL +
			"c = 3"
		);

		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		String message = null;
		try {
			actor.step();
		} catch (Exception e) {
			message = e.getMessage();
		}

		assertEquals("Actor TestActor did not output a value for b", message);
	}
	
	public void testStep_NullOutput() throws Exception {

		_setActorOutputsViaYaml(
			"a:" 					+ EOL +
			"b:" 					+ EOL +
			"  nullable: true" 		+ EOL +
			"c:"
		);

		actor.setStep(
			"a = 1;" 				+ EOL +
			"b = null;"				+ EOL +
			"c = 3"
		);
				
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		actor.step();
		
		assertEquals(1, actor.getOutputValue("a"));
		assertNull(actor.getOutputValue("b"));
		assertEquals(3, actor.getOutputValue("c"));
	}

	public void testStep_MissingOutputFile() throws Exception {

		_setActorOutputsViaYaml(
			"a:" 									+ EOL +
			"b:" 									+ EOL +
			"  nullable: true" 						+ EOL +
			"c:" 									+ EOL
		);
		
		actor.setStep(
			"a = 1;" 								+ EOL +
			"b = new File(\"nonexistentfile\");" 	+ EOL +
			"c = 3;" 								+ EOL
		);

		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		actor.step();
		
		assertEquals(1, actor.getOutputValue("a"));
		assertNull(actor.getOutputValue("b"));
		assertEquals(3, actor.getOutputValue("c"));
	}
}
