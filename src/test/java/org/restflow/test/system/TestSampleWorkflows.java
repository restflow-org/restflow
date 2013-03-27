package org.restflow.test.system;

import org.restflow.test.WorkflowTestCase;

public class TestSampleWorkflows extends WorkflowTestCase {
	
	public TestSampleWorkflows() {
		super("samples");
		_resourceDirectory = "/src/main/resources/";
	}
	
//	public void testHelloWorldInputFromStdin() throws IOException, InterruptedException {
//		String RestFlowInvocationCommand = 	"java -classpath bin" +
//			System.getProperty("path.separator") + 
//			"lib/runtime/*" +
//			System.getProperty("path.separator") + 
//			"src/main/resources org.restflow.RestFlow";
//		String s = PortableIO.readTextFile("src/test/resources/samples/hello/hello1" + WorkflowRunner.YAML_EXTENSION);
//		verifyRunExact(RestFlowInvocationCommand + " -base RESTFLOW_TESTRUNS_DIR",
//				  s, 
//				  "Reading Workflow Description from std in:" + EOL +
//				  "Hello World!" + EOL,
//				  "");
//	}
	
	public void test_Hello1() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("hello", "hello1", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("hello1_trace.txt"), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("hello1_stdout.txt"), _runner.getStdoutRecording());
	}
	


	public void test_Hello2() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("hello", "hello2", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("hello2_trace.txt"), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout("hello2_stdout.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_Hello3() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("hello", "hello3", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("hello3_trace.txt"), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout("hello3_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_Hello3_merge_uri() throws Exception {
		configureForGroovyActor();
		//PublishSubscribeDirector d = new PublishSubscribeDirector();
		//d.setPublisher(new BufferedPublisher());
		//DemandDrivenDirector d = new DemandDrivenDirector();
		//DataDrivenDirector d = new DataDrivenDirector();
		//MTDataDrivenDirector d = new MTDataDrivenDirector();
		
		_loadAndRunWorkflow("hello", "hello3_merge_uri", null, "HelloWorld");
		assertEquals(_getExpectedStdout("hello3_stdout.txt"), _runner.getStdoutRecording());		
		assertEquals(_getExpectedResultFile("hello3_merge_uri_trace.txt"), _runner.getTraceReport());			
		
	}	
	
	public void test_Hello4() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("hello", "hello4", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("hello4_trace.txt"), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout("hello4_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_Hello5() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("hello", "hello5", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("hello5_trace.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("hello5_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_Hello6() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("hello", "hello6", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("hello6_trace.txt"),_runner.getTraceReport());
		assertEquals(_getExpectedStdout("hello6_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_Hello7() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("hello", "hello7", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("hello7_trace.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("hello7_stdout.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_Incrementer1() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("nested", "incrementer1", _dataDrivenDirector(), "Incrementer");
		assertEquals(_getExpectedResultFile("incrementer1_trace.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("incrementer1_stdout.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_Incrementer2() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("nested", "incrementer2", _dataDrivenDirector(), "ssrl.adders.Incrementer");
		assertEquals(_getExpectedResultFile("incrementer2_trace.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("incrementer2_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_NestedWorkflow1_Incrementer() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("nested", "nestedworkflow1", _dataDrivenDirector(), "Incrementer");
		assertEquals(_getExpectedResultFile("nestedworkflow1_incrementer_trace.txt"),_runner.getTraceReport());;	
		assertEquals(_getExpectedStdout("nestedworkflow1_incrementer_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_NestedWorkflow1() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("nested", "nestedworkflow1", _dataDrivenDirector(), "NestedWorkflow");
		assertEquals(_getExpectedResultFile("nestedworkflow1_trace.txt"), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("nestedworkflow1_stdout.txt"), _runner.getStdoutRecording());		
	}	
	
	public void test_NestedWorkflow2() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("nested", "nestedworkflow2", _dataDrivenDirector(), "NestedWorkflow");
		assertEquals(_getExpectedResultFile("nestedworkflow2_trace.txt"), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout("nestedworkflow2_stdout.txt"), _runner.getStdoutRecording());		
	}	
	
	public void test_NestedWorkflow3() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("nested", "nestedworkflow3", _dataDrivenDirector(), "NestedWorkflow");
		assertEquals(_getExpectedResultFile("nestedworkflow3_trace.txt"), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout("nestedworkflow3_stdout.txt"), _runner.getStdoutRecording());		
	}		
	
	public void test_TimedWorkflow1() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow1", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedResultFile("timedworkflow1_trace.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("timedworkflow1_stdout.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_TimedWorkflow2() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow2", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedResultFile("timedworkflow2_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("timedworkflow2_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_TimedWorkflow3() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow3", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedResultFile("timedworkflow3_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("timedworkflow2_stdout.txt"), _runner.getStdoutRecording());
	}
	
	public void test_TimedWorkflow4() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow4", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedResultFile("timedworkflow4_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("timedworkflow4_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_TimedWorkflow5() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow5", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedResultFile("timedworkflow5_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("timedworkflow5_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_CountToTen() throws Exception {
		_loadAndRunWorkflow("behaviors", "counttoten", _dataDrivenDirector(), "Count");
		assertEquals(_getExpectedResultFile("counttoten_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("counttoten_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_Filter() throws Exception {
		_loadAndRunWorkflow("behaviors", "filter", _dataDrivenDirector(), "Filter");
		assertEquals(_getExpectedResultFile("filter_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("filter_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_Hamming() throws Exception {
		_loadAndRunWorkflow("behaviors", "hamming", _dataDrivenDirector(), "Hamming");
		assertEquals(_getExpectedResultFile("hamming_trace.txt"), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("hamming_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_Loop() throws Exception {
		_loadAndRunWorkflow("behaviors", "loop", _dataDrivenDirector(), "Loop");
		assertEquals(_getExpectedResultFile("loop_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("loop_stdout.txt"), _runner.getStdoutRecording());
	}
	
	public void test_TextScanner() throws Exception {
		_loadAndRunWorkflow("textScanner", "textScanner2", _dataDrivenDirector(), "TextScanner");
		assertEquals(_getExpectedResultFile("textScanner2_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("textScanner2_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_TextHtmlScanner() throws Exception {
		_loadAndRunWorkflow("textScanner", "textHtmlScanner", _dataDrivenDirector(), "TextScanner");
		assertEquals(_getExpectedResultFile("textHtmlScanner_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("textHtmlScanner_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_TextLabelitScanner() throws Exception {
		_loadAndRunWorkflow("textScanner", "textLabelitScanner", _dataDrivenDirector(), "TextScanner");
		assertEquals(_getExpectedResultFile("textLabelitScanner_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("textLabelitScanner_stdout.txt"), _runner.getStdoutRecording());		
	}

//	public void test_ParseGetImageHeader() throws Exception {
//		_loadAndRunWorkflow("textScanner", "getImageHeader", _dataDrivenDirector(), "workflow");
//		assertEquals(_getExpectedTrace("getImageHeader_trace.txt"), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout("getImageHeader_stdout.txt"), _runner.getStdoutRecording());		
//	}
	
	public void test_files() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("files", "helloWorld", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedResultFile("helloWorld_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("helloWorld_stdout.txt"), _runner.getStdoutRecording());		
	}	
	



}
