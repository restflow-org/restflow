package org.restflow.reporter;

import java.util.List;
import java.util.Map;

import org.restflow.test.WorkflowTestCase;
import org.restflow.util.PortableIO;
import org.yaml.snakeyaml.Yaml;


public class TestReports extends WorkflowTestCase {

//	static String RestFlowInvocationCommand = "java -classpath bin:target/restflow-dependencies.jar org.restflow.RestFlow";
	static String RestFlowInvocationCommand = "java -jar target/RestFlow-0.3.4.jar";

	public TestReports() {
		super("src/test/resources/reports/");
	}
	
	public void test_report() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_finalReporter=null;
		_loadAndRunWorkflow("groovyReport", "helloWorld", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("helloWorld_trace.txt"), _runner.getTraceReport());		
//		assertEquals(_getExpectedStdout("helloWorld_stdout.txt"), _stdoutRecorder.getStdoutRecording());
		
		loadAndRunReport(_runDirectory, "plots");

		Yaml yaml = new Yaml();
		Map<String, Object> report = (Map<String,Object>)yaml.load(_stdoutRecorder.getStdoutRecording());
		
		List<String> errors = (List<String>)report.get("errors");
		assertEquals("Should have one errors",1,errors.size());	
		String error1 = errors.get(0);
		assertTrue("should report missing report definition", error1.contains("Report 'plots' not defined in"));

		loadAndRunReport(_runDirectory, "shortestGreeting");
		assertEquals(PortableIO.EOL + "Shortest greeting: Hello World!!" + PortableIO.EOL, _stdoutRecorder.getStdoutRecording());

		loadAndRunReport(_runDirectory, "longestGreeting");
		assertEquals(PortableIO.EOL + "Longest greeting: Good night, and good luck!!" + PortableIO.EOL, _stdoutRecorder.getStdoutRecording());

		loadAndRunReport(_runDirectory, "status");
		report = (Map<String,Object>)yaml.load(_stdoutRecorder.getStdoutRecording());
		Map<String,Object> meta = (Map<String,Object>)report.get("meta");
		assertNotNull("should have meta data",meta);
		assertEquals("run directory should be the same",meta.get("runName"),_runDirectory.getName());
	}		

	public void test_specialGreeting() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_finalReporter=null;
		_loadAndRunWorkflow("specialGreeting", "helloWorld", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("helloWorld_trace.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("helloWorld_stdout.txt"), _stdoutRecorder.getStdoutRecording());
		
		loadAndRunReport(_runDirectory, "plots");

		Yaml yaml = new Yaml();
		Map<String, Object> report = (Map<String,Object>)yaml.load(_stdoutRecorder.getStdoutRecording());
		
		List<String> errors = (List<String>)report.get("errors");
		assertEquals("Should have one errors",1,errors.size());	
		String error1 = errors.get(0);
		assertTrue("should report missing report definition", error1.contains("Report 'plots' not defined in "));

		loadAndRunReport(_runDirectory, "shortestGreeting");
		assertEquals(EOL + "Shortest greeting: Hello World!!" + EOL,_stdoutRecorder.getStdoutRecording());			

		loadAndRunReport(_runDirectory, "longestGreeting");
		assertEquals(EOL + "Longest greeting: Good night, and good luck!!" + EOL,_stdoutRecorder.getStdoutRecording());			

		loadAndRunReport(_runDirectory, "status");
		report = (Map<String,Object>)yaml.load(_stdoutRecorder.getStdoutRecording());
		Map<String,Object> meta = (Map<String,Object>)report.get("meta");
		assertNotNull("should have meta data",meta);
		assertEquals("run directory should be the same",meta.get("runName"),_runDirectory.getName());
	}		
		
	@SuppressWarnings("unchecked")
	public void test_yamlModelReport() throws Exception {
		
		configureForGroovyActor();
		_useWorkingDirectory();
		_finalReporter = null;
		
		_loadAndRunWorkflow("yamlModelReport", "helloWorld", _dataDrivenDirector(), "HelloWorld");
		
		assertEquals(_getExpectedResultFile("helloWorld_trace.txt"), _runner.getTraceReport());		
		//assertEquals(_getExpectedStdout("helloWorld_stdout.txt"), _stdoutRecorder.getStdoutRecording());
		
		loadAndRunReport(_runDirectory, "summary");

		Yaml yaml = new Yaml();
		Map<String, Object> report = (Map<String,Object>)yaml.load(_stdoutRecorder.getStdoutRecording());
		assertEquals(4, report.size());
		
		List<String> myList = (List<String>)report.get("myList");
		assertEquals(3, myList.size());
		assertEquals("Hello World!!", myList.get(0));
		assertEquals("Good Afternoon, Cosmos!!", myList.get(1));
		assertEquals("Good night, and good luck!!", myList.get(2));

		Map<String, Object> myMap = (Map<String,Object>)report.get("myMap");
		assertEquals(3, myMap.size());
		assertEquals("Good Afternoon, Cosmos!!", myMap.get("g2"));
		assertEquals("Good night, and good luck!!", myMap.get("g3"));
		
		Map<String, Object> innerMap = (Map<String, Object>) myMap.get("innerMap");
		assertEquals(2, innerMap.size());
		assertEquals("entered EmphasizedGreeting" + PortableIO.EOL +
				     "entered EmphasizedGreeting" + PortableIO.EOL +
				     "entered EmphasizedGreeting" + PortableIO.EOL, innerMap.get("stdout"));
	}			
	
	
	public void test_plotReport() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();

		
		loadAndRunReport("plotReport", "plots");
		assertEquals(_getExpectedStdout("plotReport_stdout.txt"), _stdoutRecorder.getStdoutRecording());		
	}			
	
	public void test_reportMalformedRun() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
	
		loadAndRunReport("reportMalformedRun", "plots");
			
		Yaml yaml = new Yaml();
		Map<String, Object> report = (Map<String,Object>)yaml.load(_stdoutRecorder.getStdoutRecording());
		
		List<String> errors = (List<String>)report.get("errors");
		assertEquals("Should have eight errors", 8 ,errors.size());	
		
		String error1 = errors.get(0);
		assertTrue("should report missing file", error1.contains("report-defs.yaml (No such file or directory)") ||
				error1.contains("report-defs.yaml (The system cannot find the path specified)"));
		
		String error2 = errors.get(1);
		assertTrue("should report missing file", 
				error2.contains("control.yaml (No such file or directory)") 				||
				error2.contains("control.yaml (The system cannot find the path specified)") ||
				error2.contains("control.yaml' does not exist"));
		
		Map<String,Object> meta = (Map<String,Object>)report.get("meta");
		assertNotNull ("should have meta data", meta);
		assertEquals("Should have two fields",2, meta.size());	

		String dir = (String)meta.get("runName");
		assertEquals("should report missing file", dir, "reportMalformedRun" );
		
		Long lastModified = (Long)meta.get("lastModified");
		assertNotNull("should have last modification data", lastModified );
	}		
	
	public void test_finalReport() throws Exception {
		configureForGroovyActor();
		
		//setOutputTemplatePath(null);
		 //_outputHeaderTemplate = null;
		_finalReporter=null; //let the workflow set the outputTemplate
		_loadAndRunWorkflow("finalReport", "helloTemplate", _dataDrivenDirector(), "HelloWorld");
		//assertEquals(_getExpectedTrace("helloTemplate_trace.txt"), _runner.getTraceAsString());
		// TODO Standardize CR/LF handling for output templates that span multiple lines
		// so that _getExpectedStdout() will match the stdout recording on all platforms.
		assertEquals("Hello World!", _stdoutRecorder.getStdoutRecording());
	}	
	
	public void test_multiRunReport() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();

		multiRunReport("multirun", "status");
		
		Yaml yaml = new Yaml();
		Iterable<Object> reports = yaml.loadAll(_stdoutRecorder.getStdoutRecording());
		
		for (Object obj : reports ) {
			Map map = (Map)obj;
			assertTrue("should contain meta data",map.containsKey("meta"));		
		}
	}		
	
	public void test_RestFlowMainMultiRunReport() throws Exception {

		String base = PortableIO.getCurrentDirectoryPath() + _parentDirectory + "/multirun/";
		test_RestFlowMain(new String[]{"-report","status","-base", base });
		
		Yaml yaml = new Yaml();
		Iterable<Object> reports = yaml.loadAll(_stdoutRecorder.getStdoutRecording());
		
		for (Object obj : reports ) {
			Map map = (Map)obj;
			assertTrue("should contain meta data",map.containsKey("meta"));		
		}
	}

	public void test_RestFlowMainSingleRunReport() throws Exception {

		String base = PortableIO.getCurrentDirectoryPath() + _parentDirectory + "/multirun/";
		test_RestFlowMain(new String[]{"-report","status","-base", base, "-run", "run1" });
		
		Yaml yaml = new Yaml();
		Map<String,Object> report = (Map<String,Object>)yaml.load(_stdoutRecorder.getStdoutRecording());
		
		assertTrue("should have meta data", report.containsKey("meta"));
		assertFalse("report should not have any errors", report.containsKey("errors"));
		assertTrue("should have inputs", report.containsKey("inputs"));		
		
	}

	
	
	
	//TODO make this test pass by reading the plot files as yaml files
/*	public void test_plotReportYaml() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();

		
		loadAndRunReport("yamlPlotReport", "plots");
		assertEquals(_getExpectedStdout("plotReport_stdout.txt"), _stdoutRecorder.getStdoutRecording());		
	}			
	*/
	
}
