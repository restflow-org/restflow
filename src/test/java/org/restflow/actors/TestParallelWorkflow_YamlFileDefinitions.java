package org.restflow.actors;

import org.restflow.test.WorkflowTestCase;

public class TestParallelWorkflow_YamlFileDefinitions extends WorkflowTestCase {

	public TestParallelWorkflow_YamlFileDefinitions() {
		super("workflows");
	}
	
	public void test_SimulateDataCollectionNestedParallel_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollectionNestedParallel", _dataDrivenDirector());
		assertStringMatchesTemplate(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertFileMatchesTemplate("_metadata/log.txt");
		assertFileResourcesMatchExactly("_metadata/products.yaml");
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("scratch");
	}
	
	public void test_SimulateDataCollectionNestedParallel_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollectionNestedParallel", _MTDataDrivenDirector());
		assertStringMatchesTemplate(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertFileMatchesTemplate("_metadata/log.txt");
		assertFileResourcesMatchExactly("_metadata/products.yaml");
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("scratch");
	}
	
	public void test_SimulateDataCollectionNestedParallel_PublishSubscribeDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollectionNestedParallel", _publishSubscribeDirector());
		assertStringMatchesTemplate(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertFileMatchesTemplate("_metadata/log.txt");
		assertFileResourcesMatchExactly("_metadata/products.yaml");
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("scratch");
	}
	
	public void test_SimulateDataCollectionParallelAnalysis_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollectionParallelAnalysis", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
	}
}
