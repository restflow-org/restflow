package org.restflow.reporter;

import java.sql.ResultSet;

import org.restflow.metadata.FileSystemMetadataManager;
import org.restflow.metadata.RunMetadata;
import org.restflow.metadata.Trace;
import org.restflow.reporter.TraceReporter;
import org.restflow.test.WorkflowTestCase;


public class TestTraceQueryMethods_FileSystemMetadata  extends WorkflowTestCase {
	
	public TestTraceQueryMethods_FileSystemMetadata() {
		super("org/restflow/test/TestWorkflows");
	}
	
	public void setUp() throws Exception {
		super.setUp();
		org.restflow.RestFlow.enableLog4J();
		_importSchemeToResourceMap.put("actors", "classpath:/org/restflow/java/");
		_importSchemeToResourceMap.put("testActors", "classpath:/org/restflow/java/");
	}

	public void test_HelloWorld_BeanActor_PublishSubscribeDirector() throws Exception {
		
		_useWorkingDirectory();
		
		_loadAndRunWorkflow("HelloWorld", _publishSubscribeDirector());

		RunMetadata metadata = FileSystemMetadataManager.restoreMetadata(_runner.getRunDirectory());
		Trace trace = metadata.getTrace();

		assertEquals(_getExpectedStdout(), metadata.getStdoutText());
		assertEquals(_getExpectedProducts(), metadata.getProductsYaml());
		assertEquals(_getExpectedTrace(), TraceReporter.getReport(trace));
		
		assertEquals(
				"rf_node(['HelloWorld'])."						+ EOL +
				"rf_node(['HelloWorld','CreateGreeting'])."		+ EOL +
				"rf_node(['HelloWorld','RenderGreeting'])."		+ EOL,
			trace.getWorkflowNodesProlog());

		assertEquals(
				"rf_port('HelloWorld.CreateGreeting','value',out)."			+ EOL +
				"rf_port('HelloWorld.RenderGreeting','message',in)."		+ EOL,
			trace.getWorkflowPortsProlog());

		assertEquals(
				"rf_link('HelloWorld.CreateGreeting','value',e1)."			+ EOL +
				"rf_link('HelloWorld.RenderGreeting','message',e1)."		+ EOL,
			trace.getWorkflowChannelsProlog());

		assertEquals(
				"rf_event(w,'HelloWorld.CreateGreeting','1','value','CreateGreeting.value')."		+ EOL +
				"rf_event(r,'HelloWorld.RenderGreeting','1','message','CreateGreeting.value')."		+ EOL,
			trace.getDataEventsProlog());	
	
		ResultSet rs = trace.query("SELECT Uri, Value FROM Data JOIN Resource ON Data.DataID = Resource.DataID ORDER BY Uri");
		StringBuffer buffer = new StringBuffer();
		while (rs.next()) buffer.append(rs.getString("Uri") + ": " + rs.getString("Value") + EOL);
		assertEquals("CreateGreeting.value: hello" + EOL, buffer.toString());
	}
}
