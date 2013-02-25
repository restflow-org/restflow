package org.restflow.test.system;

import org.restflow.directors.GroovyDirector;
import org.restflow.test.WorkflowTestCase;


public class TestGroovyDirectorWorkflows extends WorkflowTestCase {

//	static String RestFlowInvocationCommand = "java -classpath bin:target/restflow-dependencies.jar org.restflow.RestFlow";
	static String RestFlowInvocationCommand = "java -jar target/RestFlow-0.3.8.jar";

	public TestGroovyDirectorWorkflows() {
		super("src/main/resources/samples/");
	}
	
	public void test_Hello1() throws Exception {
		configureForGroovyActor();

		GroovyDirector director = new GroovyDirector();
		
		
		String script = "CreateGreeting.step(); RenderGreeting.step(); ";
//		String script = "CreateGreeting.trigger(); director._publishOutputs( CreateGreeting ); RenderGreeting.trigger() ; ";		
		director.setSchedule(script);
		
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("hello", "hello1", director, "HelloWorld");
		assertEquals(_getExpectedResultFile("hello1_trace.txt"), _runner
				.getTraceReport());
		assertEquals(_getExpectedStdout("hello1_stdout.txt"), _runner
				.getStdoutRecording());
	}
	
	public void test_Hello2() throws Exception {
		configureForGroovyActor();

		GroovyDirector director = new GroovyDirector();
		String script = "CreateGreeting.step(); RenderGreeting.step(); ";
		//String script = "CreateGreeting.trigger(); director._publishOutputs( CreateGreeting ); RenderGreeting.trigger() ; ";		
		director.setSchedule(script);
		director.afterPropertiesSet();

		_loadAndRunWorkflow("hello", "hello2", director, "HelloWorld");
		assertEquals(_getExpectedResultFile("hello2_trace.txt"), _runner
				.getTraceReport());
		assertEquals(_getExpectedStdout("hello2_stdout.txt"), _runner
				.getStdoutRecording());
	}

	public void test_Hello3() throws Exception {
		configureForGroovyActor();

		GroovyDirector director = new GroovyDirector();
		
		String script = "CreateGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); RenderGreeting.step(); ";
		
		director.setSchedule(script);
		director.afterPropertiesSet();

		_loadAndRunWorkflow("hello", "hello3", director, "HelloWorld");
		assertEquals(_getExpectedResultFile("hello3_trace.txt"), _runner
				.getTraceReport());
		assertEquals(_getExpectedStdout("hello3_stdout.txt"), _runner
				.getStdoutRecording());
	}	


	public void test_Hello4() throws Exception {
		configureForGroovyActor();
		
		GroovyDirector director = new GroovyDirector();
		
		String script = "CreateGreeting.step(); EmphasizeGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); EmphasizeGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); EmphasizeGreeting.step(); RenderGreeting.step(); ";		
		director.setSchedule(script);
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("hello", "hello4", director, "HelloWorld");
		assertEquals(_getExpectedResultFile("hello4_trace.txt"), _runner.getTraceReport());	
		assertEquals(_getExpectedStdout("hello4_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_Hello5() throws Exception {
		configureForGroovyActor();
		
		GroovyDirector director = new GroovyDirector();

		String script = "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); ";		
		director.setSchedule(script);		
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("hello", "hello5", director, "HelloWorld");
		assertEquals(_getExpectedResultFile("hello5_trace.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("hello5_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_Hello6() throws Exception {
		configureForGroovyActor();
		
		GroovyDirector director = new GroovyDirector();

		String script = "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); ";		
		director.setSchedule(script);	
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("hello", "hello6", director, "HelloWorld");
		assertEquals(_getExpectedResultFile("hello6_trace.txt"),_runner.getTraceReport());
		assertEquals(_getExpectedStdout("hello6_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_Hello7() throws Exception {
		configureForGroovyActor();

		GroovyDirector director = new GroovyDirector();

		String script = "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); "
				+ "CreateGreeting.step(); CreateEmphasis.step(); EmphasizeGreeting.step(); RenderGreeting.step(); ";		
		director.setSchedule(script);			
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("hello", "hello7", director, "HelloWorld");
		assertEquals(_getExpectedResultFile("hello7_trace.txt"), _runner.getTraceReport());		
		assertEquals(_getExpectedStdout("hello7_stdout.txt"), _runner.getStdoutRecording());		
	}
	
	/*
	public void test_Incrementer1() throws Exception {
		configureForGroovyActor();
		
		GroovyDirector director = new GroovyDirector();
		String script = " InputValueAndIncrement.step(); IncrementInputValue.step(); OutputIncrementedValueAndIncrement.step(); RenderInputs.step(); ";		
		director.setSchedule(script);			
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("nested", "incrementer1", director, "Incrementer");
		assertEquals(_getExpectedTrace("incrementer1_trace.txt"), _runner.getTraceAsString());		
		assertEquals(_getExpectedStdout("incrementer1_stdout.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_Incrementer2() throws Exception {
		configureForGroovyActor();
		
		GroovyDirector director = new GroovyDirector();
		String script = " InputValueAndIncrement.step(); IncrementInputValue.step(); OutputIncrementedValueAndIncrement.step(); RenderInputs.step(); ";		
		director.setSchedule(script);			
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("nested", "incrementer2", director, "ssrl.adders.Incrementer");
		assertEquals(_getExpectedTrace("incrementer2_trace.txt"), _runner.getTraceAsString());		
		assertEquals(_getExpectedStdout("incrementer2_stdout.txt"), _runner.getStdoutRecording());		
	}

	public void test_NestedWorkflow1_Incrementer() throws Exception {
		configureForGroovyActor();

		configureForGroovyActor();
		
		GroovyDirector director = new GroovyDirector();
		String script = " InputValueAndIncrement.step(); IncrementInputValue.step(); OutputIncrementedValueAndIncrement.step(); RenderInputs.step(); ";		
		director.setSchedule(script);			
		director.afterPropertiesSet();

		
		_loadAndRunWorkflow("nested", "nestedworkflow1", director, "Incrementer");
		assertEquals(_getExpectedTrace("nestedworkflow1_incrementer_trace.txt"),_runner.getTraceAsString());;	
		assertEquals(_getExpectedStdout("nestedworkflow1_incrementer_stdout.txt"), _runner.getStdoutRecording());		
	}
	*/

	public void test_NestedWorkflow1() throws Exception {
		configureForGroovyActor();
		
	
		GroovyDirector director = new GroovyDirector();
		String pipeline = " GenerateIntegerSequence.step(); IncrementByDefaultIncrement.step(); RenderFirstIncrement.step(); RenderIncrementedIntegers.step(); ";		
		String script = pipeline + pipeline +pipeline +pipeline +pipeline +pipeline;
		director.setSchedule(script);			
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("nested", "nestedworkflow1", director, "NestedWorkflow");
		assertEquals(_getExpectedResultFile("nestedworkflow1_trace.txt"), _runner.getTraceReport());
		assertEquals(_getExpectedStdout("nestedworkflow1_stdout.txt"), _runner.getStdoutRecording());		
	}	

	/*
	public void test_NestedWorkflow2() throws Exception {
		configureForGroovyActor();
		
		GroovyDirector director = new GroovyDirector();
		String pipeline = " GenerateIntegerSequence.step(); IncrementByDefaultIncrement.step(); RenderFirstIncrement.step();  RenderIncrementedIntegers.step();  IncrementByFive.step(); RenderTwiceIncrementedIntegers.step(); ";		
		String script = pipeline + pipeline +pipeline +pipeline +pipeline +pipeline;
		director.setSchedule(script);	
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("nested", "nestedworkflow2", director, "NestedWorkflow");
		assertEquals(_getExpectedTrace("nestedworkflow2_trace.txt"), _runner.getTraceAsString());	
		assertEquals(_getExpectedStdout("nestedworkflow2_stdout.txt"), _runner.getStdoutRecording());		
	}	
	
	public void test_NestedWorkflow3() throws Exception {
		configureForGroovyActor();
		
		
		GroovyDirector director = new GroovyDirector();
		String pipeline = " GenerateIntegerSequence.step(); IncrementByDefaultIncrement.step(); RenderFirstIncrement.step();  RenderIncrementedIntegers.step();  IncrementByFive.step(); RenderTwiceIncrementedIntegers.step(); ";		
		String script = pipeline + pipeline +pipeline +pipeline +pipeline +pipeline;
		director.setSchedule(script);			
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("nested", "nestedworkflow3", director, "NestedWorkflow");
		assertEquals(_getExpectedTrace("nestedworkflow3_trace.txt"), _runner.getTraceAsString());	
		assertEquals(_getExpectedStdout("nestedworkflow3_stdout.txt"), _runner.getStdoutRecording());		
	}
	*/
	
	/*
	public void test_TimedWorkflow1() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow1", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedTrace("timedworkflow1_trace.txt"), _runner.getTraceAsString());		
		assertEquals(_getExpectedStdout("timedworkflow1_stdout.txt"), _runner.getStdoutRecording());		
	}
	
	public void test_TimedWorkflow2() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow2", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedTrace("timedworkflow2_trace.txt"), _runner.getTraceAsString());;		
		assertEquals(_getExpectedStdout("timedworkflow2_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_TimedWorkflow3() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow3", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedTrace("timedworkflow3_trace.txt"), _runner.getTraceAsString());;		
		assertEquals(_getExpectedStdout("timedworkflow2_stdout.txt"), _runner.getStdoutRecording());
	}
	
	public void test_TimedWorkflow4() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow4", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedTrace("timedworkflow4_trace.txt"), _runner.getTraceAsString());;		
		assertEquals(_getExpectedStdout("timedworkflow4_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_TimedWorkflow5() throws Exception {
		_loadAndRunWorkflow("parallel", "timedworkflow5", _dataDrivenDirector(), "TimedWorkflow");
		assertEquals(_getExpectedTrace("timedworkflow5_trace.txt"), _runner.getTraceAsString());;		
		assertEquals(_getExpectedStdout("timedworkflow5_stdout.txt"), _runner.getStdoutRecording());
	}
	*/

	public void test_CountToTen() throws Exception {
		GroovyDirector director = new GroovyDirector();
		String pipeline = " while (true) {CountToTen.step(); PrintNumber.step(); if (CountToTen.out.number.value == 'EndOfStream') break; };  ";		
		String script = pipeline;
		director.setSchedule(script);			
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("behaviors", "counttoten", director, "Count");
		assertEquals(_getExpectedResultFile("counttoten_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("counttoten_stdout.txt"), _runner.getStdoutRecording());
	}

	public void test_Filter() throws Exception {
		GroovyDirector director = new GroovyDirector();
		String script = "while (true) {CountToTen.step(); FilterNumbers.step(); if (FilterNumbers.out.outNumber.ready ) { PrintNumbers.step()};  if (CountToTen.out.number.value == 'EndOfStream') break; };";		
		director.setSchedule(script);				
		director.afterPropertiesSet();
		
		_loadAndRunWorkflow("behaviors", "filter", director, "Filter");
		assertEquals(_getExpectedResultFile("filter_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("filter_stdout.txt"), _runner.getStdoutRecording());
	}

	/*
	public void test_Hamming() throws Exception {
		_loadAndRunWorkflow("behaviors", "hamming", _dataDrivenDirector(), "Hamming");
		assertEquals(_getExpectedTrace("hamming_trace.txt"), _runner.getTraceAsString());;
		assertEquals(_getExpectedStdout("hamming_stdout.txt"), _runner.getStdoutRecording());
	}
	*/

	public void test_Loop() throws Exception {
		_loadAndRunWorkflow("behaviors", "loop", _dataDrivenDirector(), "Loop");
		assertEquals(_getExpectedResultFile("loop_trace.txt"), _runner.getTraceReport());;		
		assertEquals(_getExpectedStdout("loop_stdout.txt"), _runner.getStdoutRecording());
	}


	/*
	public void test_files() throws Exception {
		configureForGroovyActor();
		_useWorkingDirectory();
		_loadAndRunWorkflow("files", "helloWorld", _dataDrivenDirector(), "HelloWorld");
		assertEquals(_getExpectedTrace("helloWorld_trace.txt"), _runner.getTraceAsString());;		
		assertEquals(_getExpectedStdout("helloWorld_stdout.txt"), _runner.getStdoutRecording());		
	}	
	*/



}
