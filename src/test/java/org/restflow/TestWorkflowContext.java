package org.restflow;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.util.PortableIO;
import org.springframework.core.io.Resource;


import junit.framework.TestCase;

public class TestWorkflowContext extends TestCase {

	public void testImportMappings_FindFileResource() throws Exception {
		
		Map<String,String> importMappings = new HashMap<String, String>();
		importMappings.put("workspace", "file:" + PortableIO.getCurrentDirectoryPath() + "src/main/resources/");
		
		WorkflowContext context = new WorkflowContextBuilder()
			.importMappings(importMappings)
			.build();
		
		Resource r = context.getResource("workspace:/org/restflow/types.yaml");
		File f = r.getFile();
		assertTrue("Resource not found:" + f.getAbsolutePath(), f.exists());
	}

	public void testGetProperty_systemProperty() throws Exception {		
		System.setProperty("keyofsystemproperty", "valueofsystemproperty");
		WorkflowContext context = new WorkflowContextBuilder().build();
		assertEquals("valueofsystemproperty", context.getProperty("keyofsystemproperty"));
		assertEquals("valueofsystemproperty", System.getProperty("keyofsystemproperty"));
	}
	
	public void testGetProperty_contextProperty() throws Exception {	
		WorkflowContext context = new WorkflowContextBuilder()
			.property("keyofcontextproperty", "valueofcontextproperty")
			.build();
		assertEquals("valueofcontextproperty", context.getProperty("keyofcontextproperty"));
		assertNull(System.getProperty("keyofcontextproperty"));
	}

	public void testGetProperty_contextPropertyHidesSystemProperty() throws Exception {	
		System.setProperty("keyofproperty", "valueofsystemproperty");
		WorkflowContext context = new WorkflowContextBuilder()
			.property("keyofproperty", "valueofcontextproperty")
			.build();
		assertEquals("valueofcontextproperty", context.getProperty("keyofproperty"));
		assertEquals("valueofsystemproperty", System.getProperty("keyofproperty"));
	}
	
	public void testGetProperty_environmentVariable() throws Exception {
		String key = System.getenv().keySet().iterator().next();
		WorkflowContext context = new WorkflowContextBuilder().build();
		String valueFromEnv = System.getenv(key);
		String valueFromContext = (String)context.getProperty(key);
		assertEquals(valueFromEnv, valueFromContext);
	}

	public void testGetProperty_systemPropertyHidesEnvironmentVariable() throws Exception {
		String key = System.getenv().keySet().iterator().next();
		String valueFromEnv = System.getenv(key);
		System.setProperty(key, "valueofsystemproperty");
		WorkflowContext context = new WorkflowContextBuilder().build();
		String valueFromContext = (String)context.getProperty(key);
		assertEquals("valueofsystemproperty", valueFromContext);
		assertEquals(valueFromEnv, System.getenv(key));
		System.clearProperty(key);
		assertEquals(valueFromEnv, (String)context.getProperty(key));
	}

	public void testGetProperty_contextPropertyHidesEnvironmentVariable() throws Exception {
		String key = System.getenv().keySet().iterator().next();
		String valueFromEnv = System.getenv(key);
		WorkflowContext context = new WorkflowContextBuilder()
			.property(key, "valueofcontextproperty")
			.build();
		String valueFromContext = (String)context.getProperty(key);
		assertEquals("valueofcontextproperty", valueFromContext);
		assertEquals(valueFromEnv, System.getenv(key));
	}
	
}
