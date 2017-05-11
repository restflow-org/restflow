package org.restflow;

import org.restflow.directors.DemandDrivenDirector;
import org.restflow.test.WorkflowTestCase;

public class TestWorkflows extends WorkflowTestCase {

	public TestWorkflows() {
		super("org/restflow/test/TestWorkflows");
	}

	public void setUp() throws Exception {
		super.setUp();
		org.restflow.RestFlow.enableLog4J();
		_importSchemeToResourceMap.put("actors", "classpath:/org/restflow/java/");
		_importSchemeToResourceMap.put("testActors", "classpath:/org/restflow/java/");
	}

	public void test_OrderedConcurrentMultipliers_MTDataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("ConcurrentMultipliers", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_UnorderedConcurrentMultipliers_MTDataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("UnorderedConcurrentMultipliers", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
	}

	public void test_UnorderedConcurrentMultiplyAdd_MTDataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("UnorderedConcurrentMultiplyAdd", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
	}
	
//	public void test_ConcurrentMultiplier_DataDrivenDirector() throws Exception {
//		_useWorkingDirectory();
//		configureForBeanActor();
//		_loadAndRunWorkflow("ConcurrentMultipliers", _dataDrivenDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
//	}
	
	public void test_OneShotInflow() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("OneShotInflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_NestedOneShotInflow() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("NestedOneShotInflow", _publishSubscribeDirector());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
	}

	public void test_ControlSignals() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("ControlSignals", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_TwoControlSignals() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("TwoControlSignals", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}
	
	public void test_FilesFromStrings() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("FilesFromStrings", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_Files_PublishSubscribeDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("Files", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
		assertFileResourcesMatchExactly(".steps");
		assertFileResourcesMatchExactly("files");
	}


	public void test_NonDeterministicMerge_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("NonDeterministicMerge", _publishSubscribeDirector());
		assertEquals(_getExpectedResultFile("trace_publish.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
	}

	public void test_NonDeterministicMerge_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("NonDeterministicMerge", _dataDrivenDirector());
		assertEquals(_getExpectedResultFile("trace_data.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_NonDeterministicMerge_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("NonDeterministicMerge", _MTDataDrivenDirector());
	}

	public void test_ExternalFile_DataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("ExternalFile", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_ExternalFile_PublishSubscribeDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("ExternalFile", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_ExternalFile_MTDataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("ExternalFile", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_ExternalFile_DemandDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("ExternalFile", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
	public void test_TwoExternalFiles_DataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("TwoExternalFiles", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_StepsOnce_DataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}
	
	public void test_StepsOnce_MTDataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}

	public void test_StepsOnce_PublishSubscribeDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}

	public void test_StepsOnce_DemandDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}

	public void test_StepsOnce_JobDependencyDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _jobDependencyDirector());
		assertEquals(_getExpectedResultFile("trace_job.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}
	
//	public void test_CascadingStepsOnce_JobDependencyDirector() throws Exception {
//		configureForBeanActor();
//		_useWorkingDirectory();
//		_loadAndRunWorkflow("CascadingStepsOnce", _jobDependencyDirector());
//		assertEquals(_getExpectedTrace("trace_job.txt"), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
//		assertFilesystemResourceCorrect("messages");
//	}
	
	public void test_BasicFiles_DataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("BasicFiles", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertFileResourcesMatchExactly("messages");
	}	
	
	public void test_BasicFiles_MTDataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("BasicFiles", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertFileResourcesMatchExactly("messages");
	}	
	
	public void test_BasicFiles_PublishSubscribeDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("BasicFiles", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertFileResourcesMatchExactly("messages");
	}	

	
	public void test_HelloWorld_BeanActor_DataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_HelloWorld_BeanActor_MTDataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}
	
	public void test_HelloWorld_BeanActor_PublishSubscribeDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_HelloWorld_BeanActor_DemandDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_AveragerWorkflow_BeanActor_DataDrivenDirector() throws Exception {
		
		_loadAndRunWorkflow("AveragerWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
		
	public void test_AveragerWorkflow_BeanActor_MTDataDrivenDirector() throws Exception {
		
		_loadAndRunWorkflow("AveragerWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
	public void test_AveragerWorkflow_BeanActor_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("AveragerWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_BranchingWorkflow_BeanActor_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("BranchingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}	

	public void test_BranchingWorkflow_BeanActor_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("BranchingWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}	
	
	public void test_BranchingWorkflow_BeanActor_DemandDrivenDirector() throws Exception {
		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("BranchingWorkflow", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_demand.txt"), _runner.getStdoutRecording());		
	}	

	public void test_BranchingWorkflow_BeanActor_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("BranchingWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
	}	
	
	public void test_MergingWorkflow_BeanActor_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("MergingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_MergingWorkflow_BeanActor_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("MergingWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_MergingWorkflow_BeanActor_DemandDrivenDirector() throws Exception {
		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("MergingWorkflow", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	

	public void test_MergingWorkflow_BeanActor_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("MergingWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_CountToThree_BeanActor_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("CountToThree", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_CountToThree_BeanActor_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("CountToThree", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
		
	public void test_CountToThree_BeanActor_DemandDrivenDirector() throws Exception {
		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("CountToThree", director);
		assertEquals(_getExpectedResultFile("trace_demand.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_CountToThree_BeanActor_PublishSubscribeDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("CountToThree", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

//	public void test_CountToThree_BeanActor_JobDependencyDirector() throws Exception {
//		configureForBeanActor();
//		_useWorkingDirectory();
//
//		_loadAndRunWorkflow("CountToThree", _jobDependencyDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
//	}
	
	public void test_IntegerFilter_BeanActor_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("IntegerFilter", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_IntegerFilter_BeanActor_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("IntegerFilter", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
	
	public void test_IntegerFilter_BeanActor_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("IntegerFilter", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_AdderLoop_BeanActor_DataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("AdderLoop", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_AdderLoop_BeanActor_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("AdderLoop", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}		

	public void test_AdderLoop_BeanActor_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("AdderLoop", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_IntegerStreamMerge_BeanActor_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("IntegerStreamMerge", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
	}
	
	public void test_IntegerStreamMerge_BeanActor_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("IntegerStreamMerge", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}		

	public void test_IntegerStreamMerge_BeanActor_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("IntegerStreamMerge", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());
	}
	
	public void test_IntegerStreamMergeDuplicates_BeanActor_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _dataDrivenDirector());		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
	}

	public void test_IntegerStreamMergeDuplicates_BeanActor_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _MTDataDrivenDirector());		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}

	public void test_IntegerStreamMergeDuplicates_BeanActor_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _publishSubscribeDirector());		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());
	}

	public void test_HammingSequence_BeanActor_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("HammingSequence", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_HammingSequence_BeanActor_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("HammingSequence", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;			
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
		
	public void test_HammingSequence_BeanActor_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("HammingSequence", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_SubWorkflow_DataDrivenDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("Incrementer", "Incrementer", 
				_dataDrivenDirector(), "ssrl.Sub.Incrementer");
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_SubWorkflow_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("Incrementer", "Incrementer", 
				_MTDataDrivenDirector(), "ssrl.Sub.Incrementer");
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_SubWorkflow_DemandDrivenDirector() throws Exception {
		_loadAndRunWorkflow("Incrementer", "Incrementer", 
				_demandDrivenDirector(), "ssrl.Sub.Incrementer");
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_SubWorkflow_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("Incrementer", "Incrementer", 
				_publishSubscribeDirector(), "ssrl.Sub.Incrementer");
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_Squares_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("Squares", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_Squares_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("Squares", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_Squares_DemandDrivenDirector() throws Exception {
		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("Squares", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_Squares_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("Squares", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_NestedWorkflow_DataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("NestedWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
	}

	public void test_NestedWorkflow_MTDataDrivenDirector() throws Exception {
		_loadAndRunWorkflow("NestedWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}

	public void test_NestedWorkflow_DemandDrivenDirector() throws Exception {
		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("NestedWorkflow", director);
		assertEquals(_getExpectedResultFile("trace_demand.txt"), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_demand.txt"), _runner.getStdoutRecording());
	}	

	public void test_NestedWorkflow_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("NestedWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());
	}

	public void test_DoublyNestedWorkflow_PublishSubscribeDirector() throws Exception {
		_useWorkingDirectory();
		_loadAndRunWorkflow("DoublyNestedWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_AbstractSubworkflows_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("AbstractSubworkflows", "WA", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_AbstractSubworkflowsWithPublication_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("AbstractSubworkflowsWithPublication", "WA", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_AbstractSubworkflowsWithInportals_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("AbstractSubworkflowsWithInportals", "WA", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
}
