/**
    This performs coarse-grained unit tests for the RestFlow CLI  
 * 
 */
package org.restflow.test.system;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.restflow.RestFlow;
import org.restflow.WorkflowRunner;
import org.restflow.beans.TextScanner;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;


public class TestWorkflowToDot extends RestFlowTestCase {
	// this field will be modified in the Client-Server Tests. 
	//static String RestFlowInvocationCommand = "./eclipse-RestFlow";
	
	// uncomment the following line to test RestFlow against the RestFlow jar in target directory
	//   (must first build RestFlow-0.3.4.jar using 'ant' command in RestFlow directory)
	//static String RestFlowInvocationCommand = "java -jar target/RestFlow-0.3.4.jar";
	
	// uncomment the following line to test RestFlow against classes last compiled by Eclipse along with RestFlow's dependences
	//   (must first build restflow-dependencies.jar using 'ant dep-jar' command in RestFlow directory)
//	static String RestFlowInvocationCommand = "java -classpath bin" +
//											  System.getProperty("path.separator") + 
//											  "target/restflow-dependencies.jar org.restflow.RestFlow";
	
	static String RestFlowInvocationCommand = "java -classpath bin" +
	  System.getProperty("path.separator") + 
	  "lib/runtime/* org.restflow.RestFlow";
	
	public void testHammingToDot() throws Exception {

		StdoutRecorder _stdoutRecorder = new StdoutRecorder(false);
		_stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {
				RestFlow.main(new String[]{
						"-i","restflowFile=classpath:workflows/HammingSequence/HammingSequence.yaml",
						"-i","workflowName=HammingSequence",
						"-f","classpath:tools/dot.yaml",
						"-base", "RESTFLOW_TESTRUNS_DIR" } );		
			}
		});
		
		String actualOutput = _stdoutRecorder.getStdoutRecording();
		String expectedOutput = PortableIO.readTextFileOnFilesystem("src/test/resources/ssrl/workflow/RestFlow/hammingDot.txt");
		
		assertStringsEqualWhenLineEndingsNormalized(expectedOutput , actualOutput);
	}

	public void testToDot_WorkflowUsingFileProtocol() throws Exception {

		StdoutRecorder _stdoutRecorder = new StdoutRecorder(false);
		_stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {
				RestFlow.main(new String[]{
						"-i","restflowFile=classpath:samples/files/helloWorld.yaml",
						"-i","workflowName=HelloWorld",
						"-f","classpath:tools/dot.yaml",
						"-base", "RESTFLOW_TESTRUNS_DIR" } );		
			}
		});
		
		String actualOutput = _stdoutRecorder.getStdoutRecording();
		String expectedOutput = PortableIO.readTextFileOnFilesystem("src/test/resources/ssrl/workflow/RestFlow/helloWorldDot.txt");
		
		assertStringsEqualWhenLineEndingsNormalized(expectedOutput , actualOutput);
		
	}	
	
	public void testTimeHamming() throws Exception {

		StdoutRecorder _stdoutRecorder = new StdoutRecorder(false);
		_stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {
				RestFlow.main(new String[]{
						"-i","restflowFile=classpath:workflows/HammingSequence/HammingSequence.yaml",
						"-i","workflowName=HammingSequence",
						"-f","classpath:tools/timer.yaml",
						"-base", "RESTFLOW_TESTRUNS_DIR" } );		
			}
		});
		
		String actualOutput = _stdoutRecorder.getStdoutRecording();

		TextScanner s = new TextScanner();
		s.addDefaultTags();
		s.setAbsorbWhiteSpaceSymbol("~");
		List<String> params = new Vector<String>();
		params.add("{workflowOutput:TEXT_BLOCK}*** Hamming Sequence ***{sequence:TEXT_BLOCK}Execution time ~ {time:INT} ~ ms");
		s.setTemplate(params);
		s.compile();
		Map<String,Object> result = s.search( actualOutput );
		
		String expectedOutput = PortableIO.readTextFileOnFilesystem("src/test/resources/ssrl/workflow/RestFlow/hammingStdout.txt");
		assertStringsEqualWhenLineEndingsNormalized(expectedOutput , (String)result.get("workflowOutput"));

		Object execTime = result.get("time");
		assertNotNull("no time found", execTime);
		assertTrue("time must be position", Integer.parseInt((String)execTime) > 0);
		
	}

/*
	public void testVisualizeToolsToDot() throws Exception {

		StdoutRecorder _stdoutRecorder = new StdoutRecorder(false);
		_stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {
				RestFlow.main(new String[]{
						"-i","restflowFile=classpath:tools/visualize.yaml",
						"-i","workflowName=Visualize.WorkflowGraph",
						"-f","classpath:tools/visualize.yaml",
						"-base", "RESTFLOW_TESTRUNS_DIR" } );		
			}
		});
		
		String actualOutput = _stdoutRecorder.getStdoutRecording();
		String expectedOutput = PortableIO.readTextFile("src/test/resources/ssrl/workflow/RestFlow/hammingDot.txt");
		
		assertStringsEqualWhenLineEndingsNormalized(expectedOutput , actualOutput);
		
	}	
	*/
	
}
