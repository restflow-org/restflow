
package org.restflow.test.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.restflow.beans.TextScanner;
import org.restflow.directors.DemandDrivenDirector;
import org.restflow.reporter.JunitFinalReporter;
import org.restflow.test.WorkflowTestCase;


public class TestWorkflows extends WorkflowTestCase {

	public TestWorkflows() {
		super("src/test/resources/workflows/");
		
	}

	public void setUp() throws Exception {
		super.setUp();
		org.restflow.RestFlow.enableLog4J();	
	}	
	
	public void test_OrderedConcurrentMultipliers_MTDataDrivenDirector() throws Exception {

		_useWorkingDirectory();
		configureForBeanActor();
		_loadAndRunWorkflow("ConcurrentMultipliers", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_UnorderedConcurrentMultipliers_MTDataDrivenDirector() throws Exception {

		_useWorkingDirectory();
		configureForBeanActor();
		_loadAndRunWorkflow("UnorderedConcurrentMultipliers", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
	}

	public void test_UnorderedConcurrentMultiplyAdd_MTDataDrivenDirector() throws Exception {

		_useWorkingDirectory();
		configureForBeanActor();
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
		configureForBeanActor();
		_loadAndRunWorkflow("OneShotInflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		String expected = _getExpectedProducts();
		String actual = _runner.getProductsAsString();
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_NestedOneShotInflow() throws Exception {
		_useWorkingDirectory();
		configureForBeanActor();
		_loadAndRunWorkflow("NestedOneShotInflow", _publishSubscribeDirector());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
	}

	public void test_ControlSignals() throws Exception {
		_useWorkingDirectory();
		configureForBeanActor();
		_loadAndRunWorkflow("ControlSignals", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_TwoControlSignals() throws Exception {
		_useWorkingDirectory();
		configureForBeanActor();
		_loadAndRunWorkflow("TwoControlSignals", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}
	
	public void test_FilesFromStrings() throws Exception {
		_useWorkingDirectory();
		configureForBeanActor();
		_loadAndRunWorkflow("FilesFromStrings", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
//		assertFileResourcesMatchExactly("scratch");
//		assertFileResourcesMatchExactly("files");
	}

	public void test_Files_PublishSubscribeDirector() throws Exception {
		_useWorkingDirectory();
		configureForBeanActor();
		_loadAndRunWorkflow("Files", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
		assertFileResourcesMatchExactly("scratch");
		assertFileResourcesMatchExactly("files");
	}

	public void test_CaughtActorExceptions_PublishSubscribeDirector() throws Exception {	
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("CaughtActorExceptions", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedStderr(), _runner.getStderrRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}
	
	public void test_CaughtActorExceptions_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("CaughtActorExceptions", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
		assertEquals(_getExpectedStderr(), _runner.getStderrRecording());
		assertEquals(_getExpectedProducts("products_data.yaml"), _runner.getProductsAsString());
	}
	
	public void test_UncaughtActorExceptionNested_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		
		try {
			_loadAndRunWorkflow("UncaughtActorExceptionNested", _dataDrivenDirector());
		} catch (Exception e) {
			System.err.println(e);
		}
		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());

		assertTrue(_runner.getStderrRecording().startsWith(
				"Actor UncaughtActorExceptionNested.RepeatIntegers.RepeaterWorkflow threw exception: " +
				"org.restflow.exceptions.ActorException: " + 
				"Actor UncaughtActorExceptionNested.RepeatIntegers.ThrowExceptionOnThirdStep.(inner bean) threw exception: " +
				"java.lang.Exception: Value of 30 not allowed!"));
		
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_UncaughtActorException_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("UncaughtActorException", _dataDrivenDirector());
		assertEquals(_getExpectedResultFile("trace_data.txt"), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
		assertTrue(_runner.getStderrRecording().startsWith(
				"Actor UncaughtActorException.ThrowExceptionOnThirdStep.(inner bean) threw exception: " + 
				"java.lang.Exception: More than three steps!"));
		assertEquals(_getExpectedProducts("products_data.yaml"), _runner.getProductsAsString());
	}
	
	public void test_UncaughtActorException_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("UncaughtActorException", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertTrue(_runner.getStderrRecording().startsWith(
				"Actor UncaughtActorException.ThrowExceptionOnThirdStep.(inner bean) threw exception: " + 
				"java.lang.Exception: More than three steps!"));
		assertEquals(_getExpectedProducts("products_publish.yaml"), _runner.getProductsAsString());
	}

	public void test_UncaughtActorException_DemandDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(6);
		_loadAndRunWorkflow("UncaughtActorException", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertTrue(_runner.getStderrRecording().startsWith(
				"Actor UncaughtActorException.ThrowExceptionOnThirdStep.(inner bean) threw exception: " + 
				"java.lang.Exception: More than three steps!"));
		assertEquals(_getExpectedProducts("products_demand.yaml"), _runner.getProductsAsString());
	}

	public void test_UncaughtActorException_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("UncaughtActorException", _MTDataDrivenDirector());
		assertTrue(_runner.getStderrRecording().startsWith(
				"Actor UncaughtActorException.ThrowExceptionOnThirdStep.(inner bean) threw exception: " + 
				"java.lang.Exception: More than three steps!"));
	}
	
	public void test_EndFlowOnActorException_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("EndFlowOnActorException", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedStderr("stderr_data.txt"), _runner.getStderrRecording());
	}
	
	public void test_EndFlowOnActorException_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("EndFlowOnActorException", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedStderr("stderr_publish.txt"), _runner.getStderrRecording());
	}
	
	public void test_EndFlowOnActorException_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("EndFlowOnActorException", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedStderr("stderr_mtdata.txt"), _runner.getStderrRecording());
	}

	public void test_EndFlowOnActorException_DemandDrivenDirector() throws Exception {
		configureForBeanActor();
		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("EndFlowOnActorException", director);
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedResultFile("trace_demand.txt"), _runner.getTraceReport());	
		assertEquals(_getExpectedStderr("stderr_demand.txt"), _runner.getStderrRecording());
	}
	
	
//	public void test_TestNestedInflowBindings() throws Exception {
//		configureForGroovyActor();
//		_loadAndRunWorkflow("NestedInflowBinding", _publishSubscribeDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;
//		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
//	}
//	
	public void test_DirectFilesGroovy() throws Exception {
		
		configureForGroovyActor();
		_useWorkingDirectory();
		
		_loadAndRunWorkflow("DirectFilesGroovy", _publishSubscribeDirector());
		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertFileResourcesMatchExactly("messages1");
		assertFileResourcesMatchExactly("messages2");
		assertFileResourcesMatchExactly("messages3");
		assertFileResourcesMatchExactly("products");
	}
	
	
	public void test_DirectDirectoriesGroovy() throws Exception {
		
		configureForGroovyActor();
		_useWorkingDirectory();
		
		_loadAndRunWorkflow("DirectDirectoriesGroovy", _publishSubscribeDirector());
		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertFileResourcesMatchExactly("data_set_1");
		assertFileResourcesMatchExactly("data_set_2");
		assertFileResourcesMatchExactly("data_set_3");
		assertFileResourcesMatchExactly("data_set_4");
		assertFileResourcesMatchExactly("scratch");
	}
	
	public void test_NonDeterministicMerge_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("NonDeterministicMerge", _publishSubscribeDirector());
		assertEquals(_getExpectedResultFile("trace_publish.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
	}

	public void test_NonDeterministicMerge_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("NonDeterministicMerge", _dataDrivenDirector());
		assertEquals(_getExpectedResultFile("trace_data.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_NonDeterministicMerge_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("NonDeterministicMerge", _MTDataDrivenDirector());
	}

	public void test_ConditionalRouting_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("ConditionalRouting", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_ConditionalRouting_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("ConditionalRouting", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	
	public void test_ExternalFile_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("ExternalFile", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_ExternalFile_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("ExternalFile", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_ExternalFile_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("ExternalFile", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_ExternalFile_DemandDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("ExternalFile", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
	public void test_TwoExternalFiles_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("TwoExternalFiles", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_StepsOnce_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}
	
	public void test_StepsOnce_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}

	public void test_StepsOnce_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}

	public void test_StepsOnce_DemandDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("StepsOnce", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
	}

	public void test_StepsOnce_JobDependencyDirector() throws Exception {
		configureForBeanActor();
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

	public void test_MissingFiles_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("MissingFiles", _dataDrivenDirector());
	}

	public void test_FileHandles_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("FileHandles", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;	
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
		assertFileResourcesMatchExactly("scratch");
	}

	public void test_ScratchFiles_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("ScratchFiles", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
		assertFileResourcesMatchExactly("scratch");
	}

	public void test_ScratchFiles_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("ScratchFiles", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
		assertFileResourcesMatchExactly("scratch");
	}

	public void test_ScratchFiles_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("ScratchFiles", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
		assertFileResourcesMatchExactly("scratch");
	}
	
	//TODO make this work
/*	public void test_ScratchFiles_TclActor_DataDrivenDirector() throws Exception {
		configureForTclActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("ScratchFiles", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertFileResourcesMatchExactly("messages");
		assertFileResourcesMatchExactly("scratch");
	}		*/
	
	public void test_BasicFiles_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("BasicFiles", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertFileResourcesMatchExactly("messages");
	}	
	
	public void test_BasicFiles_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("BasicFiles", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertFileResourcesMatchExactly("messages");
	}	
	
	public void test_BasicFiles_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("BasicFiles", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertFileResourcesMatchExactly("messages");
	}	

	public void test_BasicFiles_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("BasicFiles", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertFileResourcesMatchExactly("messages");
	}
	
	public void test_SimulateDataCollection_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_teeLogToStandardOutput = true;
		_loadAndRunWorkflow("SimulateDataCollection", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertStringMatchesTemplate(_getExpectedStdout("stdout_datadriven.txt"), _runner.getStdoutRecording());
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("data");
		assertFileMatchesTemplate("_metadata/log.txt");
	}

	public void test_SimulateDataCollection_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollection", _MTDataDrivenDirector());
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("data");
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertFileMatchesTemplate("_metadata/log.txt");
	}

	public void test_SimulateDataCollection_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollection", _publishSubscribeDirector());
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("data");
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertFileMatchesTemplate("_metadata/log.txt");
		
		TextScanner s = new TextScanner();
		s.addDefaultTags();

		s.setAbsorbWhiteSpaceSymbol("~");
		
		s.setTokenMatcherPrefix("<%");
		s.setTokenMatcherSuffix("%>");
		
		
		List<String> params = new Vector<String>();
		params.add("<%trace[]:TRACE_LINE%>");
		params.add("<%log[]:LOG_LINE%>");
		s.getTags().put("DATE","<%month:STRING%> <%day:INT%>");
		s.getTags().put("TIME","<%hours:INT%>:<%minutes:INT%>:<%seconds:INT%>");
		s.getTags().put("LOG_STAMP","<%:DATE%> <%time:TIME%>");
		s.getTags().put("TRACE_LINE","Sample DRT<%num:INT%>    Image <%imageNum:INT%>    Average intensity: <%avgIntensity:FLOAT%>");
		s.getTags().put("LOG_LINE","<%stamp:LOG_STAMP%> <%text:TEXT_BLOCK%>\n");
		
		s.setTemplate(params);
		s.compile();
		
		Map<String,Object> result = s.search(_runner.getStdoutRecording());
		List logs = (List)result.get("log");
		assertEquals ("6 log lines",6,logs.size());

		List trace = (List)result.get("trace");
		assertEquals ("26 trace lines",26,trace.size());
		
		Map firstLine = (Map)trace.get(0);
		assertEquals(firstLine.get("imageNum"),"001");
	}

	public void test_SimulateDataCollectionNested_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollectionNested", _publishSubscribeDirector());
		assertStringMatchesTemplate(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertFileMatchesTemplate("_metadata/log.txt");
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("scratch");
	}

	public void test_SimulateDataCollectionNestedFeedback_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollectionNestedFeedback", _publishSubscribeDirector());
		assertStringMatchesTemplate(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertFileMatchesTemplate("_metadata/log.txt");
		assertFileResourcesMatchExactly("_metadata/products.yaml");
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("scratch");
	}

	public void test_SimulateDataCollectionNestedFeedback_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollectionNestedFeedback", _MTDataDrivenDirector());
		assertStringMatchesTemplate(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertFileMatchesTemplate("_metadata/log.txt");
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("scratch");
	}
		
	public void test_SimulateDataCollectionNestedFeedback_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("SimulateDataCollectionNestedFeedback", _dataDrivenDirector());
		assertStringMatchesTemplate(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertFileMatchesTemplate("_metadata/log.txt");
		assertFileResourcesMatchExactly("sample");
		assertFileResourcesMatchExactly("scratch");
	}

	
	public void test_Maps_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("Maps", _publishSubscribeDirector());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_SampleScreening_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_teeLogToStandardOutput = true;
		//_finalReporter = null;
		_loadAndRunWorkflow("SampleScreening", _publishSubscribeDirector());
		//assertEquals(_getExpectedTrace(), _runner.getTraceAsString());
		assertStringMatchesTemplate(_getExpectedStdout("stdout_datadriven.txt"), _runner.getStdoutRecording());
		//assertFileMatchesTemplate("_metadata/log.txt");
	}
	
	public void test_HelloWorld_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_HelloWorld_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}
	
	public void test_HelloWorld_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_HelloWorld_BeanActor_DemandDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_HelloWorld_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}
	
	public void test_HelloWorld_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}
	public void test_HelloWorld_Groovyctor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_HelloWorld_GroovyActor_DemandDrivenDirector() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("HelloWorld", _demandDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
		assertEquals(_getExpectedProducts(), _runner.getProductsAsString());
	}

	public void test_Lists_JavaActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("Lists", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}

	public void test_Lists_JavaActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("Lists", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}

	public void test_Lists_JavaActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("Lists", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
	}

	public void test_Lists_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("Lists", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}

	public void test_Lists_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("Lists", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}

	public void test_Lists_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("Lists", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
	}	
	
//	public void test_SerialAdder_BeanActor_DataDrivenDirector() throws Exception {
//		configureForBeanActor();
//		_loadAndRunWorkflow("AddSerially", _dataDrivenDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
//	}
//	
//	public void test_SerialAdder_BeanActor_MTDataDrivenDirector() throws Exception {
//		configureForBeanActor();
//		_loadAndRunWorkflow("AddSerially", _MTDataDrivenDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
//	}
//
//	public void test_SerialAdderWorkflow_BeanActor_PublishSubscribeDirector() throws Exception {
//		configureForBeanActor();
//		_loadAndRunWorkflow("AddSerially", _publishSubscribeDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
//	}
	
//	public void test_SerialAdder_GroovyActor_DataDrivenDirector() throws Exception {
//		configureForGroovyActor();
//		_loadAndRunWorkflow("AddSerially", _dataDrivenDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
//	}
//	
//	public void test_SerialAdder_GroovyActor_MTDataDrivenDirector() throws Exception {
//		configureForGroovyActor();
//
//		_loadAndRunWorkflow("AddSerially", _MTDataDrivenDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
//	}
//
//	public void test_SerialAdder_GroovyActor_PublishSubscribeDirector() throws Exception {
//		configureForGroovyActor();
//
//		_loadAndRunWorkflow("AddSerially", _publishSubscribeDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
//	}

	public void test_AveragerWorkflow_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("AveragerWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
		
	public void test_AveragerWorkflow_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("AveragerWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
	public void test_AveragerWorkflow_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("AveragerWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}

	public void test_AveragerWorkflow_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("AveragerWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
	public void test_AveragerWorkflow_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("AveragerWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
	public void test_AveragerWorkflow_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("AveragerWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}
	
//	public void test_AveragerWorkflow_BashActor_DataDrivenDirector() throws Exception {
//		configureForBashActor();
//
//		_loadAndRunWorkflow("AveragerWorkflow", _dataDrivenDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
//	}
	
	public void test_BranchingWorkflow_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("BranchingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}	

	public void test_BranchingWorkflow_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("BranchingWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}	
	
	public void test_BranchingWorkflow_BeanActor_DemandDrivenDirector() throws Exception {
		configureForBeanActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("BranchingWorkflow", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_demand.txt"), _runner.getStdoutRecording());		
	}	

	public void test_BranchingWorkflow_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("BranchingWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
	}	
	
	public void test_BranchingWorkflow_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("BranchingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());		
	}	

	public void test_BranchingWorkflow_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("BranchingWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}	
	
	public void test_BranchingWorkflow_GroovyActor_DemandDrivenDirector() throws Exception {
		configureForGroovyActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("BranchingWorkflow", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_demand.txt"), _runner.getStdoutRecording());		
	}	

	public void test_BranchingWorkflow_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("BranchingWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());		
	}	


	public void test_MergingWorkflow_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("MergingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_MergingWorkflow_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("MergingWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_MergingWorkflow_BeanActor_DemandDrivenDirector() throws Exception {
		configureForBeanActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("MergingWorkflow", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	

	public void test_MergingWorkflow_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("MergingWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_MergingWorkflow_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("MergingWorkflow", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_MergingWorkflow_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("MergingWorkflow", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	
	public void test_MergingWorkflow_GroovyActor_DemandDrivenDirector() throws Exception {
		configureForGroovyActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("MergingWorkflow", director);
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	

	public void test_MergingWorkflow_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("MergingWorkflow", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());		
	}	
	

	public void test_CountToThree_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("CountToThree", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_CountToThree_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("CountToThree", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
		
	public void test_CountToThree_BeanActor_DemandDrivenDirector() throws Exception {
		configureForBeanActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("CountToThree", director);
		assertEquals(_getExpectedResultFile("trace_demand.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_CountToThree_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
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
	
	public void test_CountToThree_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("CountToThree", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_CountToThree_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("CountToThree", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
		
	public void test_CountToThree_GroovyActor_DemandDrivenDirector() throws Exception {
		configureForGroovyActor();

		DemandDrivenDirector director = _demandDrivenDirector();
		director.setFiringCount(3);
		_loadAndRunWorkflow("CountToThree", director);
		assertEquals(_getExpectedResultFile("trace_demand.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_CountToThree_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();

		
		_loadAndRunWorkflow("CountToThree", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}


	
	public void test_IntegerFilter_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("IntegerFilter", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_IntegerFilter_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("IntegerFilter", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
	
	public void test_IntegerFilter_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("IntegerFilter", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_IntegerFilter_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("IntegerFilter", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	

	public void test_IntegerFilter_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("IntegerFilter", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	
	
	public void test_IntegerFilter_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();

		_loadAndRunWorkflow("IntegerFilter", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}	


	public void test_AdderLoop_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("AdderLoop", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_AdderLoop_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("AdderLoop", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}		

	public void test_AdderLoop_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		_loadAndRunWorkflow("AdderLoop", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_AdderLoop_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("AdderLoop", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_AdderLoop_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("AdderLoop", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_AdderLoop_GroovyActor_PublishSubscribeDrivenDirector() throws Exception {
		configureForGroovyActor();
		_loadAndRunWorkflow("AdderLoop", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	

	
	public void test_IntegerStreamMerge_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("IntegerStreamMerge", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
	}
	
	public void test_IntegerStreamMerge_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("IntegerStreamMerge", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}		

	public void test_IntegerStreamMerge_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("IntegerStreamMerge", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());
	}
	
	public void test_IntegerStreamMerge_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("IntegerStreamMerge", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
	}
	
	public void test_IntegerStreamMerge_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("IntegerStreamMerge", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;		
	}		

	public void test_IntegerStreamMerge_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("IntegerStreamMerge", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());
	}	

	public void test_IntegerStreamMergeDuplicates_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _dataDrivenDirector());		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
	}

	public void test_IntegerStreamMergeDuplicates_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _MTDataDrivenDirector());		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
	}

	public void test_IntegerStreamMergeDuplicates_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _publishSubscribeDirector());		
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());
	}

	public void test_IntegerStreamMergeDuplicates_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_data.txt"), _runner.getStdoutRecording());
		System.out.println(_runner.getStderrRecording());
	}
	
	public void test_IntegerStreamMergeDuplicates_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		System.out.println(_runner.getStderrRecording());
	}	
	
	public void test_IntegerStreamMergeDuplicates_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("IntegerStreamMergeDuplicates", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout_publish.txt"), _runner.getStdoutRecording());
		System.out.println(_runner.getStderrRecording());
	}	


	public void test_HammingSequence_BeanActor_DataDrivenDirector() throws Exception {
		configureForBeanActor();
		
		_loadAndRunWorkflow("HammingSequence", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout("stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_HammingSequence_BeanActor_MTDataDrivenDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("HammingSequence", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;			
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
		
	public void test_HammingSequence_BeanActor_PublishSubscribeDirector() throws Exception {
		configureForBeanActor();

		_loadAndRunWorkflow("HammingSequence", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
	public void test_HammingSequence_GroovyActor_DataDrivenDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("HammingSequence", _dataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}

	public void test_HammingSequence_GroovyActor_MTDataDrivenDirector() throws Exception {
		configureForGroovyActor();
		
		_loadAndRunWorkflow("HammingSequence", _MTDataDrivenDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
		
	public void test_HammingSequence_GroovyActor_PublishSubscribeDirector() throws Exception {
		configureForGroovyActor();
		
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

//	public void test_ScriptRunner() throws Exception {
//		_loadAndRunWorkflow("script_runner", _publishSubscribeDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;		
//	}
	
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

	public void test_Groovy_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("GroovyWorkflow", _publishSubscribeDirector());
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

	public void test_NestedWorkflowEnableInputs_PublishSubscribeDirector() throws Exception {
		_loadAndRunWorkflow("NestedWorkflowEnableInputs", _publishSubscribeDirector());
		assertEquals(_getExpectedTrace(), _runner.getTraceReport());;
		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
	}
	
//	public void test_AbstractSubworkflowsWithOutportals_PublishSubscribeDirector() throws Exception {
//		_loadAndRunWorkflow("AbstractSubworkflowsWithOutportals", "WA", _publishSubscribeDirector());
//		assertEquals(_getExpectedTrace(), _runner.getTraceAsString());;
//		assertEquals(_getExpectedStdout(), _runner.getStdoutRecording());
//	}
}
