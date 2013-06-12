/**
    This performs coarse-grained unit tests for the RestFlow CLI  
 * 
 */
package org.restflow;

import java.io.IOException;

import org.restflow.RestFlow;
import org.restflow.WorkflowRunner;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;


public class TestRestFlow extends RestFlowTestCase {
	
	public static String RestFlowInvocationCommand = "java -classpath target/classes" +
											  System.getProperty("path.separator") + "target/test-classes" +
											  System.getProperty("path.separator") + 
											  "target/dependency/* org.restflow.RestFlow";
	
	public void testHelloWorld() throws IOException, InterruptedException {

		String workflow = "classpath:/org/restflow/test/TestRestFlow/HelloWorld" + WorkflowRunner.YAML_EXTENSION;
		
		verifyRunExact(RestFlowInvocationCommand + " -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "", 
				  "Hello World!" + EOL,
				  "");
	}
	
	public void testHelloWorld_ValidateOnly() throws IOException, InterruptedException {

		String workflow = "classpath:/org/restflow/test/TestRestFlow/HelloWorld" + WorkflowRunner.YAML_EXTENSION;
		
		verifyRunExact(RestFlowInvocationCommand + " -v -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "", 
				  "",
				  "");
	}

	public void test_ValidateWorkflow_MissingDirector() throws IOException, InterruptedException {
		
		String workflow = "classpath:/org/restflow/test/TestRestFlow/TestValidateWorkflowMissingDirector" + WorkflowRunner.YAML_EXTENSION;

		verifyRunRegexp(RestFlowInvocationCommand + " -v -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "", 
				  "",
				  "java.lang.Exception: No director specified for workflow.*");
	}

//	public void test_ValidateWorkflow_FileProtocolWithoutUriVariables() throws IOException, InterruptedException {
//		System.out.println(RestFlowInvocationCommand);
//		String workflow = "src/test/resources/ssrl/workflow/RestFlow/workflow_fileProtocolWithoutUriVariables"+WorkflowRunner.YAML_EXTENSION;
//		verifyRunExact(RestFlowInvocationCommand + " -v -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
//				  "", 
//				  "",
//				  "java.lang.Exception: No variables in outflow template with file scheme: file:/messages/greeting on HelloWorld.CreateGreeting.*");
//	}
	
	//TODO
/*	public void test_HelloTemplate() throws Exception {
		verifyRunExact(RESTFLOW_EXECUTABLE + " -f src/test/resources/samples/hello/helloTemplate.yaml -base testruns",
				  "", 
				  "Hello World!" + ls,
				  "");
	}*/
	
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
		
//		String actualOutput = _stdoutRecorder.getStdoutRecording();
//		String expectedOutput = PortableIO.readTextFileOnClasspath("ssrl/workflow/RestFlow/hammingDot.txt");
//		
//		assertStringsEqualWhenLineEndingsNormalized(expectedOutput , actualOutput);
		
	}

	
	
	public void testHelloWorldInputFromStdin() throws IOException, InterruptedException {
		
		String s = PortableIO.readTextFileOnClasspath("org/restflow/test/TestRestFlow/HelloWorld" + WorkflowRunner.YAML_EXTENSION);
		
		verifyRunExact(RestFlowInvocationCommand + " -base RESTFLOW_TESTRUNS_DIR",
				  s,
				  "Hello World!" + EOL,
				  "");
	}

	public void testHelp_OutputPattern() throws IOException, InterruptedException {
		
		verifyRunRegexp(RestFlowInvocationCommand + " -h",
				  "", 
				  ".*Option.*Description.*------.*-----------.*show help.*",
				  "");
	}

	public void testHelp_OutputDetails() throws IOException, InterruptedException {
		
		verifyRunExact(RestFlowInvocationCommand + " -h",
			"", 
				"Option                                  Description                            " + EOL +
				"------                                  -----------                            " + EOL +
				"-?, -h                                  show help                              " + EOL +
				"--base <directory>                      base working directory                 " + EOL +
				"--cp <jar|directory>                    add to classpath                       " + EOL +
				"--daemon                                Daemonize after header printed         " + EOL +
				"-f, --workflow-description <file>       (default: -)                           " + EOL +
				"-i, --input <key=value>                 key-valueed inputs                     " + EOL +
				"--import-map <scheme=resource>          key-valueed import scheme to file      " + EOL +
				"                                          resource                             " + EOL +
				"--infile, --input-file <file>           yaml input file                        " + EOL +
				"--outfile, --output-file <file>         yaml input file                        " + EOL +
				"--preamble <directory>                  preamble template path (default: )     " + EOL +
				"--prevrun <run directory>               uses final state from previous run     " + EOL +
				"--report <name>                         run report on existing run directory   " + EOL +
				"                                          (default: )                          " + EOL +
				"--run <name>                            forces run name                        " + EOL +
				"-t, --enable-trace                      enable trace                           " + EOL +
				"--to-dot                                output a Graphviz dot file instead of  " + EOL +
				"                                          running the workflow                 " + EOL +
				"-v, --validate                          validate the workflow without running  " + EOL +
				"                                          it                                   " + EOL +
				"-w, --workflow <name>                   workflow name                          " + EOL +
				"--workspace <directory>                 workspace folder                       " + EOL,
			"");
	}
	
	public void testWrongOption() throws IOException, InterruptedException {
		
		verifyRunRegexp(RestFlowInvocationCommand + " --definitely-not-an-option",
				  "", 
				  "",
				  ".*Error in command-line options.*Option.*Description.*------.*-----------.*show help.*");
	}
	
	public void testTrace() throws IOException, InterruptedException {
		
		String workflow = "classpath:/org/restflow/test/TestRestFlow/HelloWorld" + WorkflowRunner.YAML_EXTENSION;
		
		verifyRunExact(RestFlowInvocationCommand + " -t -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR ",
				  "",
				  
				  "Hello World!" 						+ EOL +
				  "*** Node step counts ***" 			+ EOL +
				  "HelloWorld: 1"						+ EOL +
				  "HelloWorld.CreateGreeting: 1" 		+ EOL +
				  "HelloWorld.RenderGreeting: 1" 		+ EOL +
				  "*** Published resources ***" 		+ EOL +
				  "/messages/greeting: Hello World!" 	+ EOL,
				  
				  "");
	}
	
	public void testClassPath() throws IOException, InterruptedException {
		
		// define path to test workflow
		String workflow = "classpath:/org/restflow/test/TestRestFlow/TestClasspathParameter" + WorkflowRunner.YAML_EXTENSION;
			
		// run unsuccessfully when not including class directory to classpath
		verifyRunRegexp(RestFlowInvocationCommand + " -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
			"", "", ".*Bean class ActorNotOnMainClasspath not found.*");

		// run successfully by explicitly adding excluded class directory to classpath
		verifyRunExact(RestFlowInvocationCommand + " -cp src/test/exclude -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
			"", "42" + EOL, "");
	}
	
	public void testWorkflowInputs() throws IOException, InterruptedException {

		// define path to test workflow
		String workflow = "classpath:/org/restflow/test/TestRestFlow/TestWorkflowInputs" + WorkflowRunner.YAML_EXTENSION;

		// run workflow using defaults for workflow inputs
		verifyRunExact(RestFlowInvocationCommand + " -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "1" 		+ EOL +
				  "2" 		+ EOL,
				  "");
		
		// run workflow providing an explicit value for first workflow input
		verifyRunExact(RestFlowInvocationCommand + "  --input argumentOne=hello -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "hello" 	+ EOL +
				  "2" 		+ EOL,
				  "");
		
		// run workflow providing an explicit value for both workflow inputs
		verifyRunExact(RestFlowInvocationCommand + "  --input argumentOne=hello -i argumentTwo=world -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "hello" 	+ EOL +
				  "world" 	+ EOL,
				  "");
	}
	
	public void testWrongInput() throws IOException, InterruptedException {
		// not including the class path
		verifyRunRegexp(RestFlowInvocationCommand + " -f this_workflow_does_not_exist" + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "",
				  "Exception in thread .main. org.springframework.beans.factory.BeanDefinitionStoreException: URL .file:this_workflow_does_not_exist." +
				  ".*YamlBeanDefinitionReader.loadBeanDefinitions.*");
	}
	
	public void testNamedRun() throws IOException, InterruptedException {
		// not including the class path
		
		String uniqueName = "NamedRun_" + PortableIO.createTimeStampString();
		String workflow = "classpath:/org/restflow/test/TestRestFlow/HelloWorld" + WorkflowRunner.YAML_EXTENSION;
		verifyRunExact(RestFlowInvocationCommand + " -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR -run " + uniqueName,
				  "", 
				  "Hello World!" + EOL,
				  "");
		
		//run a second time with the same name to show exception;
		verifyRunRegexp(RestFlowInvocationCommand + " -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR -run " + uniqueName,
				  "",
				  "",
				  "Exception in thread .main. java.lang.Exception:.* "+ "already exists in .*");
	}
}