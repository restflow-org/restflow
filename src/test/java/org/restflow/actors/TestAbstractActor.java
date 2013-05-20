package org.restflow.actors;

import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.AbstractActor;
import org.restflow.test.RestFlowTestCase;
import org.yaml.snakeyaml.Yaml;


@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestAbstractActor extends RestFlowTestCase {

	private TestActor actor;
	private Yaml yaml;
	
	private String EOL = System.getProperty("line.separator");
	
	public void setUp() throws Exception {
		super.setUp();
		
		WorkflowContext context = new WorkflowContextBuilder()
			.build();
		
		actor = new TestActor();
		actor.setApplicationContext(context);
		actor.setName("TestActor");
		yaml = new Yaml();
	}
	
	private void _setActorInputsViaYaml(String yamlString) throws Exception {
		actor.setInputs((Map)yaml.load(yamlString));
	}

	public void testSetInputs_ViaApiCall_NameOnly_OneInput() throws Exception {

		assertEquals(0, actor._inputSignature.size());

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("a", null);
		
		actor.setInputs(inputs);

		assertEquals(1, actor._inputSignature.size());
		assertTrue(actor._inputSignature.containsKey("a"));
		assertNull(actor._inputSignature.get("a").getDefaultValue());
	}

	public void testSetInputs_ViaApiCall_NamesOnly_ThreeInputs() throws Exception {

		assertEquals(0, actor._inputSignature.size());

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("a", null);
		inputs.put("b", null);
		inputs.put("c", null);

		actor.setInputs(inputs);

		assertEquals(3, actor._inputSignature.size());
		assertTrue(actor._inputSignature.containsKey("a"));		
		assertTrue(actor._inputSignature.containsKey("b"));		
		assertTrue(actor._inputSignature.containsKey("c"));		
		assertNull(actor._inputSignature.get("a").getDefaultValue());
		assertNull(actor._inputSignature.get("b").getDefaultValue());
		assertNull(actor._inputSignature.get("c").getDefaultValue());
	}

	public void testSetInputs_NameAndDefault_OneInput() throws Exception {

		assertEquals(0, actor._inputSignature.size());

		_setActorInputsViaYaml(
				"a:" 					+ EOL +
				"  default: 4"			+ EOL
		);

		assertEquals(1, actor._inputSignature.size());
		assertTrue(actor._inputSignature.containsKey("a"));
		assertEquals(4, actor._inputSignature.get("a").getDefaultValue());
	}
	
	public void testSetInputs__NamesAndDefaults_ThreeInputs() throws Exception {

		assertEquals(0, actor._inputSignature.size());

		_setActorInputsViaYaml(
				"a:" 					+ EOL +
				"  default: 1"			+ EOL +
				"b:" 					+ EOL +
				"  default: 47.2"		+ EOL +
				"c:" 					+ EOL +
				"  default: hello"		+ EOL
		);

		assertEquals(3, actor._inputSignature.size());
		assertTrue(actor._inputSignature.containsKey("a"));		
		assertTrue(actor._inputSignature.containsKey("b"));		
		assertTrue(actor._inputSignature.containsKey("c"));		
		assertEquals(1, actor._inputSignature.get("a").getDefaultValue());
		assertEquals(47.2, actor._inputSignature.get("b").getDefaultValue());
		assertEquals("hello", actor._inputSignature.get("c").getDefaultValue());
}

	
	public void testSetInputs_ViaYaml_NamesOnly_OneInput() throws Exception {

		assertEquals(0, actor._inputSignature.size());

		_setActorInputsViaYaml(
			"a:" 					+ EOL
		);
		
		assertEquals(1, actor._inputSignature.size());
		assertTrue(actor._inputSignature.containsKey("a"));
	}

	public void testSetInputs_ViaYaml_NamesOnly_ThreeInputs() throws Exception {

		assertEquals(0, actor._inputSignature.size());

		_setActorInputsViaYaml(
				"a:" 					+ EOL +
				"b:" 					+ EOL +
				"c:" 					+ EOL
			);

		assertEquals(3, actor._inputSignature.size());
		assertTrue(actor._inputSignature.containsKey("a"));		
		assertTrue(actor._inputSignature.containsKey("b"));		
		assertTrue(actor._inputSignature.containsKey("c"));		
	}
	
	public void testSetInputs_nameAndType() throws Exception {

		_setActorInputsViaYaml(
			"a:" 					+ EOL +
			"  type: List"			+ EOL
		);
		
		actor.afterPropertiesSet();
		actor.elaborate();
		actor.configure();
		actor.initialize();
		
		assertTrue(actor._inputSignature.get("a").isList());
	}
	
	class TestActor extends AbstractActor {
		
		public void afterPropertiesSet() throws Exception {
			super.afterPropertiesSet(); 
			_state = ActorFSM.PROPERTIES_SET;
		}

		public void elaborate() throws Exception {
			super.elaborate(); 
			_state = ActorFSM.ELABORATED;
		}

		public void configure() throws Exception {
			super.configure(); 
			_state = ActorFSM.CONFIGURED;
		}
		
		public void initialize() throws Exception {
			super.initialize();
			_state = ActorFSM.CONFIGURED;
		}

		public void step() throws Exception {
			super.step();
			_state = ActorFSM.STEPPED;
		}

		public void wrapup() throws Exception {
			super.wrapup();
			_state = ActorFSM.WRAPPED_UP;
		}

		public void dispose() throws Exception {
			super.dispose();
			_state = ActorFSM.DISPOSED;
		}
	}
}
