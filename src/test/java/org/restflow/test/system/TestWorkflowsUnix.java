package org.restflow.test.system;

import java.util.Map;

import org.restflow.directors.DemandDrivenDirector;
import org.restflow.test.WorkflowTestCase;
import org.yaml.snakeyaml.Yaml;


public class TestWorkflowsUnix extends WorkflowTestCase {

	public TestWorkflowsUnix() {
		super("src/test/resources/workflows/");
	}

	public void test_PublishDirectory_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("PublishDirectory", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertStringsEqualWhenLineEndingsNormalized(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("files");
		assertFileResourcesMatchExactly("directories");
		assertFileResourcesMatchExactly("scratch");
	}
	
	public void test_PublishDirectoryFromSubworkflow_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("PublishDirectoryFromSubworkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertStringsEqualWhenLineEndingsNormalized(_getExpectedStdout(), _runner.getStdoutRecording());
		assertFileResourcesMatchExactly("files");
		assertFileResourcesMatchExactly("sub");
		assertFileResourcesMatchExactly("top");
		assertFileResourcesMatchExactly("scratch");
	}
	
	public void test_SubscribeWithLocalPaths_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SubscribeWithLocalPaths", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertStringsEqualWhenLineEndingsNormalized(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("files");
		assertFileResourcesMatchExactly("directories");
		assertFileResourcesMatchExactly("scratch");
	}

	public void test_SubscribeWithLocalPaths_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SubscribeWithLocalPaths", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertStringsEqualWhenLineEndingsNormalized(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("files");
		assertFileResourcesMatchExactly("directories");
		assertFileResourcesMatchExactly("scratch");
	}

	public void test_SubscribeWithLocalPaths_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SubscribeWithLocalPaths", _MTDataDrivenDirector());
		assertStringsEqualWhenLineEndingsNormalized(_getExpectedTrace(), _runner.getTraceReport());
		assertFileResourcesMatchExactly("files");
		assertFileResourcesMatchExactly("directories");
		assertFileResourcesMatchExactly("scratch");
	}	
	public void test_BashFiles_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("BashFiles", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
		assertFileResourcesMatchExactly("scratch");
	}

	public void test_HelloWorld_BashActor_DataDrivenDirector() throws Exception {
		configureForBashActor();
		_loadAndRunWorkflow("HelloWorld", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_HelloWorld_BashActor_MTDataDrivenDirector() throws Exception {
		configureForBashActor();
		_loadAndRunWorkflow("HelloWorld", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_HelloWorld_BashActor_PublishSubscribeDirector() throws Exception {
		configureForBashActor();
		_loadAndRunWorkflow("HelloWorld", _publishSubscribeDirector());
	//	assertEquals(_getExpectedTrace(), _runner.getTraceAsString());
		String expected = _getExpectedStdout();
		String actual = _runner.getStdoutRecording();
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_HelloWorld_BashActor_demandDrivenDirector() throws Exception {
		configureForBashActor();
		_loadAndRunWorkflow("HelloWorld", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	

	public void test_BranchingWorkflow_BashActor_DataDrivenDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("BranchingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}		
	
	public void test_BranchingWorkflow_BashActor_MTDataDrivenDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("BranchingWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
	}	

	public void test_BranchingWorkflow_BashActor_PublishSubscribeDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("BranchingWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
	}

	public void test_BranchingWorkflow_BashActor_DemandDrivenDirector() throws Exception {
		configureForBashActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("BranchingWorkflow", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("stdout_demand.txt"), _runner.getStdoutRecording());		
	}	

	public void test_MergingWorkflow_BashActor_DataDrivenDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("MergingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_MergingWorkflow_BashActor_MTDataDrivenDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("MergingWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_MergingWorkflow_BashActor_PublishSubscribeDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("MergingWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_MergingWorkflow_BashActor_DemandDrivenDirector() throws Exception {
		configureForBashActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("MergingWorkflow", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}			

	public void test_CountToThree_BashActor_DataDrivenDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("CountToThree", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_CountToThree_BashActor_MTDataDrivenDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("CountToThree", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
	
	public void test_CountToThree_BashActor_PublishSubscribeDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("CountToThree", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_CountToThree_BashActor_DemandDrivenDirector() throws Exception {
		configureForBashActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("CountToThree", director);
		assertEquals(_getExpectedResultFile("trace_demand.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_IntegerFilter_BashActor_DataDrivenDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("IntegerFilter", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_IntegerFilter_BashActor_MTDataDrivenDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("IntegerFilter", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_IntegerFilter_BashActor_PublishSubscribeDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("IntegerFilter", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_AdderLoop_BashActor_DataDrivenDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("AdderLoop", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_AdderLoop_BashActor_MTDataDrivenDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("AdderLoop", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_AdderLoop_BashActor_PublishSubscribeDirector() throws Exception {
		configureForBashActor();

		_loadAndRunWorkflow("AdderLoop", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_IntegerStreamMergeDuplicates_BashActor_DataDrivenDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
		System.out.println(_runner.getStderrRecording());
	}	
	
	public void test_IntegerStreamMergeDuplicates_BashActor_MTDataDrivenDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		System.out.println(_runner.getStderrRecording());
	}
	
	public void test_IntegerStreamMergeDuplicates_BashActor_PublishSubscribeDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());
		System.out.println(_runner.getStderrRecording());
	}		
	
	public void test_HammingSequence_BashActor_DataDrivenDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("HammingSequence", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_HammingSequence_BashActor_MTDataDrivenDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("HammingSequence", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}	

	public void test_HammingSequence_BashActor_PublishSubscribeDirector() throws Exception {
		configureForBashActor();
		
		_loadAndRunWorkflow("HammingSequence", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}		
	
	public void test_liveReport() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		//setOutputTemplatePath(null);
		
		loadAndRunReport("liveReport", "status");
		
		Yaml yaml = new Yaml();
		Map<String, Object> report = (Map<String,Object>)yaml.load(_stdoutRecorder.getStdoutRecording());
		//System.out.println(report);
		
		Map<String,Object> meta = (Map<String,Object>)report.get("meta");
		assertEquals("Should have 5 meta entries", 5, meta.size());	
		
		assertTrue("Should have host", meta.containsKey("host"));
		assertTrue("Should have pid", meta.containsKey("pid"));
		assertTrue("Should have running", meta.containsKey("running"));
		
		Map<String,Object> inputs = (Map<String,Object>)report.get("inputs");
		
		assertNotNull("should include inputs in model",inputs);
		assertEquals("Should have 5 inputs entries", inputs.size(),5);	
		
		assertEquals("Input file is wrong", "1.0", inputs.get("InputResolution"));

	}
	
	public void test_WorkspaceFiles() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		
		_loadAndRunWorkflow("WorkspaceFiles", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertStringsEqualWhenLineEndingsNormalized(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}
	
	public void test_WorkspaceFilesBash() throws Exception {
		configureForBashActor();
		_useWorkingDirectory();
		
		_loadAndRunWorkflow("WorkspaceFilesBash", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}

	/*	public void test_Environment_Back_Populate() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("Environment", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}
	
	public void test_Execute() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("Execute", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}*/
	
	
}
