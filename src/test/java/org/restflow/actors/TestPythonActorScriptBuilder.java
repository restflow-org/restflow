package org.restflow.actors;

import org.restflow.actors.ActorScriptBuilder;
import org.restflow.actors.PythonActor;
import org.restflow.test.RestFlowTestCase;


public class TestPythonActorScriptBuilder extends RestFlowTestCase {

	public void testAppendCode() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		assertEquals(
			"", 
			builder.toString()
			);

		builder.appendCode("Some code");
		
		assertEquals(
			"Some code" 			+ EOL, 
			builder.toString()
		);
		
		builder.appendCode("Some more code");
		
		assertEquals(
			"Some code" 			+ EOL +
			"Some more code" 		+ EOL,
			builder.toString()
		);
	}
	
	
	public void testAppendSeparator() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendCode("Some code");
		builder.appendSeparator();
		builder.appendCode("Some more code");
		
		assertEquals(
			"Some code" 			+ EOL +
			"######################################################################################" + EOL +
			"Some more code" 		+ EOL,
			builder.toString()
		);
	}
	
	public void testAppendBlankLine() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendCode("Some code");
		builder.appendBlankLine();
		builder.appendCode("Some more code");
		
		assertEquals(
			"Some code" 			+ EOL +
			""						+ EOL +
			"Some more code" 		+ EOL,
			builder.toString()
		);
	}

	public void testAppendComment() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendCode("Some code");
		builder.appendComment("A comment");
		builder.appendCode("Some more code");
		
		assertEquals(
			"Some code" 			+ EOL +
			"# A comment"			+ EOL +
			"Some more code" 		+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_NullType_NullValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_no_type", null, null, false, false);
		assertEquals(
			"var_with_no_type=None" 			+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_NullType_StringValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_no_type", "A string", null, false, false);
		assertEquals(
			"var_with_no_type='A string'" 			+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_NullType_IntegerValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_no_type", 42, null, false, false);
		assertEquals(
			"var_with_no_type='42'" 				+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_NullType_BooleanValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_no_type", true, null, false, false);
		assertEquals(
			"var_with_no_type='true'" 				+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_StringType_NullValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_string_type", null, "String", false, false);
		assertEquals(
			"var_with_string_type=None" 			+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_StringType_StringValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_string_type", "A string", "String", false, false);
		assertEquals(
			"var_with_string_type='A string'" 		+ EOL,
			builder.toString()
		);
	}
	
	public void testAppendLiteralAssignment_StringType_IntegerValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_string_type", 42, "String", false, false);
		assertEquals(
			"var_with_string_type='42'" 			+ EOL,
			builder.toString()
		);
	}
	
	public void testAppendLiteralAssignment_StringType_BooleanValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_string_type", true, "String", false, false);
		assertEquals(
			"var_with_string_type='true'" 			+ EOL,
			builder.toString()
		);
	}
	
	public void testAppendLiteralAssignment_IntegerType_NullValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_integer_type", null, "Integer", false, false);
		assertEquals(
			"var_with_integer_type=None" 			+ EOL,
			builder.toString()
		);
	}
	
	public void testAppendLiteralAssignment_IntegerType_StringValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		Exception exception = null;
		try {
			builder.appendLiteralAssignment("var_with_integer_type", "A string", "Integer", false, false);
		} catch (Exception e) {
			exception = e;
		}
		
		assertNotNull(exception);
		assertEquals(
			"Error assigning value to python Integer variable 'var_with_integer_type': A string", 
			exception.getMessage()
		);
	}
	
	public void testAppendLiteralAssignment_IntegerType_IntegerValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_integer_type", 42, "Integer", false, false);
		assertEquals(
			"var_with_integer_type=42" 				+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_IntegerType_BooleanValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		Exception exception = null;
		try {
			builder.appendLiteralAssignment("var_with_integer_type", true, "Integer", false, false);
		} catch (Exception e) {
			exception = e;
		}
		
		assertNotNull(exception);
		assertEquals(
			"Error assigning value to python Integer variable 'var_with_integer_type': true", 
			exception.getMessage()
		);
	}
	
	public void testAppendLiteralAssignment_BooleanType_NullValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_boolean_type", null, "Boolean", false, false);
		assertEquals(
			"var_with_boolean_type=None" 			+ EOL,
			builder.toString()
		);
	}
	
	public void testAppendLiteralAssignment_BooleanType_StringValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		Exception exception = null;
		try {
			builder.appendLiteralAssignment("var_with_boolean_type", "A string", "Boolean", false, false);
		} catch (Exception e) {
			exception = e;
		}
		
		assertNotNull(exception);
		assertEquals(
			"Error assigning value to python Boolean variable 'var_with_boolean_type': A string", 
			exception.getMessage()
		);
	}

	public void testAppendLiteralAssignment_BooleanType_NonZeroIntegerValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_boolean_type", 42, "Boolean", false, false);
		assertEquals(
			"var_with_boolean_type=1" 				+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_BooleanType_ZeroIntegerValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_boolean_type", 0, "Boolean", false, false);
		assertEquals(
			"var_with_boolean_type=0" 				+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_BooleanType_TrueBooleanValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_boolean_type", true, "Boolean", false, false);
		assertEquals(
			"var_with_boolean_type=1" 				+ EOL,
			builder.toString()
		);
	}

	public void testAppendLiteralAssignment_BooleanType_FalseBooleanValue() throws Exception {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendLiteralAssignment("var_with_boolean_type", false, "Boolean", false, false);
		assertEquals(
			"var_with_boolean_type=0" 				+ EOL,
			builder.toString()
		);
	}

	public void testAppendChangeDirectory() {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendChangeDirectory("test/temp");
		assertEquals(
			"os.chdir('test/temp')" 				+ EOL,
			builder.toString()
		);
	}

	public void testAppendPrintStringStatement() {

		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendPrintStringStatement("string to print");
		
		assertEquals(
			"print 'string to print'" 				+ EOL,
			builder.toString()
		);
	}
	
	public void testAppendVariableYamlPrintStatement_StringType() {
		
		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendVariableYamlPrintStatement("var_with_string_type", "String");
		
		assertEquals(
				"print 'var_with_string_type: ', (\"\\\"%s\\\"\" % var_with_string_type, '~')[var_with_string_type==None]" +EOL, 	
				builder.toString()
		);		
	}

	public void testAppendVariableYamlPrintStatement_IntegerType() {
		
		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendVariableYamlPrintStatement("var_with_integer_type", "Integer");
		
		assertEquals(
			"print 'var_with_integer_type: ', (var_with_integer_type, 'null')[var_with_integer_type==None]"		+ EOL,
			builder.toString()
		);		
	}

	public void testAppendVariableYamlPrintStatement_BooleanType() {
		
		ActorScriptBuilder builder = new PythonActor.ScriptBuilder();
		
		builder.appendVariableYamlPrintStatement("var_with_boolean_type", "Boolean");
		
		assertEquals(
			"print 'var_with_boolean_type: ', (\"false\", \"true\")[var_with_boolean_type==True]"		+ EOL,
			builder.toString()
		);		
	}
}
