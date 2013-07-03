package org.restflow;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.restflow.test.RestFlowCommandTestCase;

public class TestCommandLine_InputOption_Literals extends RestFlowCommandTestCase {

	private String testResourcePath = "/src/test/resources/org/restflow/test/TestCommandLineIO/";

	public void test_InputRepeater_Integer() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=3" 
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof Integer);
		assertEquals(3, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof Integer);
		assertEquals(3, testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_Double() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=3.14159"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof Double);
		assertTrue(Math.abs(3.14159 - (Double)testRun.getInputValue("inputValue")) < 0.0001);
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof Double);
		assertTrue(Math.abs(3.14159 - (Double)testRun.getOutputValue("outputValue")) < 0.0001);
	}

	
	public void test_InputRepeater_Boolean_true() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=true"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof Boolean);
		assertEquals(true, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof Boolean);
		assertEquals(true, testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_Boolean_TRUE() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=TRUE"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof Boolean);
		assertEquals(true, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof Boolean);
		assertEquals(true, testRun.getOutputValue("outputValue"));
	}

	
	public void test_InputRepeater_Boolean_True() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=True"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof Boolean);
		assertEquals(true, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof Boolean);
		assertEquals(true, testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_Boolean_false() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=false"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof Boolean);
		assertEquals(false, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof Boolean);
		assertEquals(false, testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_Boolean_FALSE() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=FALSE"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof Boolean);
		assertEquals(false, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof Boolean);
		assertEquals(false, testRun.getOutputValue("outputValue"));
	}
	
	
	public void test_InputRepeater_Boolean_False() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=False"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof Boolean);
		assertEquals(false, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof Boolean);
		assertEquals(false, testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_String_Unquoted_NoSpaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=astringvalue"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof String);
		assertEquals("astringvalue", testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof String);
		assertEquals("astringvalue", testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_String_Unquoted_Spaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=a string value"
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof String);
		assertEquals("a string value", testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof String);
		assertEquals("a string value", testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_String_DoubleQuotes_NoSpaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
	runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=\"stringvalue\""
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof String);
		assertEquals("stringvalue", testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof String);
		assertEquals("stringvalue", testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_String_DoubleQuotes_Spaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=\"a string value\"" 
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof String);
		assertEquals("a string value", testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof String);
		assertEquals("a string value", testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_String_SingleQuotes_NoSpaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=\'astringvalue\'" 
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof String);
		assertEquals("astringvalue", testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof String);
		assertEquals("astringvalue", testRun.getOutputValue("outputValue"));
	}
	
	public void test_InputRepeater_String_SingleQuotes_Spaces() throws Exception {
	
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=\'a string value\'" 
			}
		);
		
		assertTrue(testRun.getInputValue("inputValue") instanceof String);
		assertEquals("a string value", testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValue("outputValue") instanceof String);
		assertEquals("a string value", testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_NULL() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=NULL" 
			}
		);
		
		assertTrue(testRun.getInputValues().containsKey("inputValue"));
		assertEquals(null, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValues().containsKey("outputValue"));
		assertEquals(null, testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_Null() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=Null" 
			}
		);
		
		assertTrue(testRun.getInputValues().containsKey("inputValue"));
		assertEquals(null, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValues().containsKey("outputValue"));
		assertEquals(null, testRun.getOutputValue("outputValue"));
	}

	public void test_InputRepeater_null() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=null" 
			}
		);
		
		assertTrue(testRun.getInputValues().containsKey("inputValue"));
		assertEquals(null, testRun.getInputValue("inputValue"));
		
		assertTrue(testRun.getOutputValues().containsKey("outputValue"));
		assertEquals(null, testRun.getOutputValue("outputValue"));
	}

	@SuppressWarnings("unchecked")
	public void test_InputRepeater_List_Integers() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=[1,2,3]" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof List);
		List<Integer> inputValue = (List<Integer>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals((Integer)1, (Integer)inputValue.get(0));
		assertEquals((Integer)2, (Integer)inputValue.get(1));
		assertEquals((Integer)3, (Integer)inputValue.get(2));

		assertTrue(testRun.getOutputValue("outputValue") instanceof List);
		List<Integer> outputValue = (List<Integer>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals((Integer)1, (Integer)outputValue.get(0));
		assertEquals((Integer)2, (Integer)outputValue.get(1));
		assertEquals((Integer)3, (Integer)outputValue.get(2));
	}

	

	@SuppressWarnings("unchecked")
	public void test_InputRepeater_List_Integers_NullElement() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=[1,null,3]" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof List);
		List<Integer> inputValue = (List<Integer>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals((Integer)1, (Integer)inputValue.get(0));
		assertEquals(null, (Integer)inputValue.get(1));
		assertEquals((Integer)3, (Integer)inputValue.get(2));

		assertTrue(testRun.getOutputValue("outputValue") instanceof List);
		List<Integer> outputValue = (List<Integer>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals((Integer)1, (Integer)outputValue.get(0));
		assertEquals(null, (Integer)outputValue.get(1));
		assertEquals((Integer)3, (Integer)outputValue.get(2));
	}

	@SuppressWarnings("unchecked")
	public void test_InputRepeater_List_Strings_Unquoted_NoSpaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=[one,two,three]" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof List);
		List<String> inputValue = (List<String>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals("one", inputValue.get(0));
		assertEquals("two", inputValue.get(1));
		assertEquals("three", inputValue.get(2));

		assertTrue(testRun.getOutputValue("outputValue") instanceof List);
		List<String> outputValue = (List<String>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals("one", outputValue.get(0));
		assertEquals("two", outputValue.get(1));
		assertEquals("three", outputValue.get(2));
	}

	@SuppressWarnings("unchecked")
	public void test_InputRepeater_List_Strings_Unquoted_NullElement() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=[one,null,three]" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof List);
		List<String> inputValue = (List<String>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals("one", inputValue.get(0));
		assertEquals(null, inputValue.get(1));
		assertEquals("three", inputValue.get(2));

		assertTrue(testRun.getOutputValue("outputValue") instanceof List);
		List<String> outputValue = (List<String>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals("one", outputValue.get(0));
		assertEquals(null, outputValue.get(1));
		assertEquals("three", outputValue.get(2));
	}

	
	@SuppressWarnings("unchecked")
	public void test_InputRepeater_List_Strings_Unquoted_Spaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=[number one,number two,number three]" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof List);
		List<String> inputValue = (List<String>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals("number one", inputValue.get(0));
		assertEquals("number two", inputValue.get(1));
		assertEquals("number three", inputValue.get(2));

		assertTrue(testRun.getOutputValue("outputValue") instanceof List);
		List<String> outputValue = (List<String>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals("number one", outputValue.get(0));
		assertEquals("number two", outputValue.get(1));
		assertEquals("number three", outputValue.get(2));
	}

	
	@SuppressWarnings("unchecked")
	public void test_InputRepeater_List_Strings_DoubleQuotes_Spaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=[\"number one\",\"number two\",\"number three\"]" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof List);
		List<String> inputValue = (List<String>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals("number one", inputValue.get(0));
		assertEquals("number two", inputValue.get(1));
		assertEquals("number three", inputValue.get(2));

		assertTrue(testRun.getOutputValue("outputValue") instanceof List);
		List<String> outputValue = (List<String>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals("number one", outputValue.get(0));
		assertEquals("number two", outputValue.get(1));
		assertEquals("number three", outputValue.get(2));
	}

	@SuppressWarnings("unchecked")
	public void test_InputRepeater_List_Strings_SingleQuotes_Spaces() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue=['number one','number two','number three']" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof List);
		List<String> inputValue = (List<String>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals("number one", inputValue.get(0));
		assertEquals("number two", inputValue.get(1));
		assertEquals("number three", inputValue.get(2));

		assertTrue(testRun.getOutputValue("outputValue") instanceof List);
		List<String> outputValue = (List<String>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals("number one", outputValue.get(0));
		assertEquals("number two", outputValue.get(1));
		assertEquals("number three", outputValue.get(2));
	}

	@SuppressWarnings("unchecked")
	public void test_InputRepeater_Map_StringToInteger() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue={'a':1, 'b':2, 'c':3}" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof Map);
		Map<String,Integer> inputValue = (Map<String,Integer>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals((Integer)1, (Integer)inputValue.get("a"));
		assertEquals((Integer)2, (Integer)inputValue.get("b"));
		assertEquals((Integer)3, (Integer)inputValue.get("c"));

		assertTrue(testRun.getOutputValue("outputValue") instanceof Map);
		Map<String,Integer> outputValue = (Map<String,Integer>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals((Integer)1, (Integer)outputValue.get("a"));
		assertEquals((Integer)2, (Integer)outputValue.get("b"));
		assertEquals((Integer)3, (Integer)outputValue.get("c"));
	}

	@SuppressWarnings("unchecked")
	public void test_InputRepeater_Map_StringToString() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue={'a':one, 'b':'number two', 'c':\"and three\"}" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof Map);
		Map<String,String> inputValue = (Map<String,String>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertEquals("one", inputValue.get("a"));
		assertEquals("number two", inputValue.get("b"));
		assertEquals("and three", inputValue.get("c"));

		assertTrue(testRun.getOutputValue("outputValue") instanceof Map);
		Map<String,String> outputValue = (Map<String,String>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertEquals("one", outputValue.get("a"));
		assertEquals("number two", outputValue.get("b"));
		assertEquals("and three", outputValue.get("c"));
	}

	@SuppressWarnings("unchecked")
	public void test_InputRepeater_Map_StringToBoolean() throws Exception {
		
		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run", 
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/InputRepeater.yaml",
				"-i", "inputValue={'a':true, 'b':false, 'c':null}" 
			}
		);
				
		assertTrue(testRun.getInputValue("inputValue") instanceof Map);
		Map<String,Boolean> inputValue = (Map<String,Boolean>)testRun.getInputValue("inputValue");
		assertEquals(3, inputValue.size());
		assertTrue(inputValue.get("a"));
		assertFalse(inputValue.get("b"));
		assertNull(inputValue.get("c"));

		assertTrue(testRun.getOutputValue("outputValue") instanceof Map);
		Map<String,Boolean> outputValue = (Map<String,Boolean>)testRun.getOutputValue("outputValue");
		assertEquals(3, outputValue.size());
		assertTrue(outputValue.get("a"));
		assertFalse(outputValue.get("b"));
		assertNull(inputValue.get("c"));
	}
	
	public void test_IntegerAdder_SaveOutputToFile() throws Exception {

		initializeTestEnvironment(testResourcePath);
		
		runRestFlowWithArguments( new String[] {
				"-base", testWorkingDirectory.getPath(),
				"-run", "run",
				"-f", "classpath:/org/restflow/test/TestCommandLineIO/IntegerAdder.yaml",
				"-i", "a=3",
				"-i", "b=17",
				"-o", "c:" + testWorkingDirectory + "/sum.txt"
			}
		);
		
		assertEquals(3, testRun.getInputValues().get("a"));
		assertEquals(17, testRun.getInputValues().get("b"));
		assertEquals(20, testRun.getOutputValues().get("c"));
		
		assertEquals("20", FileUtils.readFileToString(new File(testWorkingDirectory + "/sum.txt")));
	}
	
	//TODO: STRING WITH EQUALS
	//TODO: STRING WITH COLON

}