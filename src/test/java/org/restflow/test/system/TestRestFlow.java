/**
    This performs coarse-grained unit tests for the RestFlow CLI  
 * 
 */
package org.restflow.test.system;

import java.io.File;
import java.io.IOException;

import org.restflow.RestFlow;
import org.restflow.WorkflowRunner;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;


public class TestRestFlow extends RestFlowTestCase {
	
	// uncomment the following line to test RestFlow against the RestFlow jar in target directory
	//   (must first build RestFlow-0.3.4.jar using 'ant' command in RestFlow directory)
	//static String RestFlowInvocationCommand = "java -jar target/RestFlow-standalone.jar";
	
	// uncomment the following line to test RestFlow against classes last compiled by Eclipse
	// along with RestFlow's dependencies obtained via the ivy ant target
	// TODO make this static field final and refactor TestRestFlowServer to not require write access to it
	public static String RestFlowInvocationCommand = "java -classpath target/classes" +
											  System.getProperty("path.separator") + 
											  "target/dependency/* org.restflow.RestFlow";
	
	public void testHelloWorld() throws IOException, InterruptedException {
		System.out.println(RestFlowInvocationCommand);
		String workflow = "src/test/resources/ssrl/workflow/RestFlow/hello1"+WorkflowRunner.YAML_EXTENSION;
		verifyRunExact(RestFlowInvocationCommand + " -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "", 
				  "Hello World!" + EOL,
				  "");
	}
	
	public void testHelloWorld_ValidateOnly() throws IOException, InterruptedException {
		System.out.println(RestFlowInvocationCommand);
		String workflow = "src/test/resources/ssrl/workflow/RestFlow/hello1"+WorkflowRunner.YAML_EXTENSION;
		verifyRunExact(RestFlowInvocationCommand + " -v -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "", 
				  "",
				  "");
	}

	public void test_ValidateWorkflow_MissingDirector() throws IOException, InterruptedException {
		System.out.println(RestFlowInvocationCommand);
		String workflow = "src/test/resources/ssrl/workflow/RestFlow/workflow_missingDirector"+WorkflowRunner.YAML_EXTENSION;
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
		
		String actualOutput = _stdoutRecorder.getStdoutRecording();
		String expectedOutput = PortableIO.readTextFile("src/test/resources/ssrl/workflow/RestFlow/hammingDot.txt");
		
		//assertStringsEqualWhenLineEndingsNormalized(expectedOutput , actualOutput);
		
	}

	
	
	public void testHelloWorldInputFromStdin() throws IOException, InterruptedException {
		String s = PortableIO.readTextFile("src/test/resources/ssrl/workflow/RestFlow/hello1" +WorkflowRunner.YAML_EXTENSION);
		verifyRunExact(RestFlowInvocationCommand + " -base RESTFLOW_TESTRUNS_DIR",
				  s, 
				  "Reading Workflow Description from std in:" + EOL +
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
				"-c, --client [Integer: remote port]     start as client (default: 0)           " + EOL +
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
				"-s, --server [Integer: listen port]     start as server (default: 0)           " + EOL +
				"--server-idle-timeout [Integer:         The server will terminate itself if    " + EOL +
				"  seconds]                                not used by a client for this time   " + EOL +
				"                                          span. (default: 3600)                " + EOL +
				"--server-loop <Integer: num>            number of clients to be served before  " + EOL +
				"                                          exit.                                " + EOL +
				"--server-name [hostname]                server to connect to (default:         " + EOL +
				"                                          localhost)                           " + EOL +
				"--server-restart                        restart RestFlow server                " + EOL +
				"--server-restart-name [RestFlow         restart RestFlow server (default:      " + EOL +
				"  executable]                             RestFlow)                            " + EOL +
				"--server-secret [Integer: secret]       Simple challenge-response secret. If   " + EOL +
				"                                          no secret is specified, a random     " + EOL +
				"                                          number is used.                      " + EOL +
				"--server-shell [BASH|TCSH|WIN_CMD]      format for environmental variables     " + EOL +
				"                                          (default: BASH)                      " + EOL +
				"--server-stop                           stop RestFlow server                   " + EOL +
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
		String workflow = "src/test/resources/ssrl/workflow/RestFlow/hello1" + WorkflowRunner.YAML_EXTENSION;
		verifyRunExact(RestFlowInvocationCommand + " -t -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR ",
				  "",
				  
				  "Hello World!" 						+ EOL +
				  "*** Node step counts ***" 			+ EOL +
				  "<HelloWorld>: 1"						+ EOL +
				  "<HelloWorld>[CreateGreeting]: 1" 		+ EOL +
				  "<HelloWorld>[RenderGreeting]: 1" 		+ EOL +
				  "*** Published resources ***" 		+ EOL +
				  "/messages/greeting: Hello World!" 	+ EOL,
				  
				  "");
	}
	
	public void testClassPath() throws IOException, InterruptedException {
		// not including the class path
		String workflow = "src/test/resources/ssrl/workflow/RestFlow/TestCp"+WorkflowRunner.YAML_EXTENSION;
		
		
		verifyRunRegexp(RestFlowInvocationCommand + " -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "",
				   	"Actor <TestCP>\\[Actor\\]<\\(inner bean\\)> threw exception: groovy.lang.MissingPropertyException: " +
				  	"No such property: TestUtil for class: Script1.*" + 
				  	"groovy.lang.MissingPropertyException: No such property: TestUtil for class: Script1.*"
				  	
		);


		// including the class path
		verifyRunExact(RestFlowInvocationCommand + " -cp src/test/exclude -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "42" + EOL,
				  "");
	}
	
	public void testInput() throws IOException, InterruptedException {
		// not including the class path
		String workflow = "src/test/resources/ssrl/workflow/RestFlow/incrementer1" + WorkflowRunner.YAML_EXTENSION;
		verifyRunExact(RestFlowInvocationCommand + " -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "Incrementer received value=0 and increment=1" + EOL,
				  "");
		verifyRunExact(RestFlowInvocationCommand + "  --input value=hello -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "Incrementer received value=hello and increment=1" + EOL,
				  "");
		verifyRunExact(RestFlowInvocationCommand + "  --input value=hello -i increment=world -f " + workflow + " -base RESTFLOW_TESTRUNS_DIR",
				  "",
				  "Incrementer received value=hello and increment=world" + EOL,
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
		String workflow = "src/test/resources/ssrl/workflow/RestFlow/hello1" + WorkflowRunner.YAML_EXTENSION;
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
