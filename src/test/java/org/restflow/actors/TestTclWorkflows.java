package org.restflow.actors;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.TclActorBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.InputProperty;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.TclNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;


public class TestTclWorkflows extends RestFlowTestCase {
	
	private WorkflowContext _context;
	private ConsumableObjectStore _store;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_context = new WorkflowContextBuilder()
			.store(_store)
			.build();
	}
		
	public void test_WorkflowBuilder_HelloWorld_OneNode_Tcl() throws Exception {

		// declare and build the workflow
		final Workflow workflow = new WorkflowBuilder()
		
			.context(_context)
			.node(new TclNodeBuilder()
				.step(
					"		puts {Hello world!}				")
			)
			
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
		
		// confirm expected stdout
		assertEquals("Hello world!" + EOL, recorder.getStdoutRecording());
		
		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		// confirm that no data was published
		assertEquals(0, _store.size());
	}

	
	public void test_HelloWorld() throws Exception {
		
		// declare and build the workflow
		final Workflow workflow = new WorkflowBuilder()
		
			.name("HelloWorld")
			.context(_context)
			
			.node(new TclNodeBuilder()
				.name("CreateGreeting")
				.constant("s", "Hello world!")
				.step(
					"		set outValue [concat $s]			"
				)
				.outflow("outValue", "/greeting")
			)
				
			.node(new TclNodeBuilder()
				.name("RenderGreeting")
				.inflow("/greeting", "g")
				.step(
					"		puts [concat $g]					"
				)
			)
			
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
		
		// confirm expected stdout
		assertEquals("Hello world!" + EOL, recorder.getStdoutRecording());
		
		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		// confirm expected published data
		assertEquals("Hello world!", _store.take("/greeting"));
		assertEquals(0, _store.size());
	}
	
	public void test_BranchingWorkflow() throws Exception {
		
		// declare and build the workflow
		final Workflow workflow = new WorkflowBuilder()
		
			.name("BranchingWorkflow")
			.context(_context)
			
			.node(new TclNodeBuilder()
				.name("CreateGreetings")
				.sequence("s", new String[] {
						"Hey there", 
						"Hello", 
						"Hi"			})
				.step(
					"		set g [concat $s]							"
				)
				.outflow("g", "/messages/greeting")
			)

			.node(new TclNodeBuilder()
				.name("EmphasizeGreeting")
				.inflow("/messages/greeting/", "s1")
				.constant("s2", "!!")
				.step(
					"		set s1s2 [concat ${s1}${s2}]				"
				)
				.outflow("s1s2", "/messages/emphasizedGreeting1/")
			)
			
			.node(new TclNodeBuilder()
				.name("FurtherEmphasizeGreeting")
				.inflow("/messages/emphasizedGreeting1/", "s1")
				.sequence("s2", new String[] {
						" :-)",
				        "!!!"})
				.repeatValues(true)
				.step(
					"		set s1s2 [concat ${s1}${s2}]				"
				)
				.outflow("s1s2", "/messages/emphasizedGreeting2/")
			)
			
			.node(new TclNodeBuilder()
				.name("RenderDoublyEmphasizedGreeting")
				.inflow("/messages/emphasizedGreeting2/", "g")
				.step(
					"		puts [concat $g]							"
				)
			)
			
			.node(new TclNodeBuilder()
				.name("CountGreetings")
				.inflow("/messages/emphasizedGreeting2/", "g")
				.initialize(
					"		set count 0									"
				)
				.step(
					"		set count [expr $count + 1]					"
				)
				.state("count")
				.outflow("count", "/messages/count/")
			)
			
			.node(new TclNodeBuilder()
				.name("RenderGreetingCount")
				.inflow("/messages/count/", "c")
				.step(
					"		puts [concat $c]							"
				)
			)
			
			.build();
	
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout
		assertEquals(
			"1" 			  	+ EOL +
			"Hey there!! :-)" 	+ EOL +
			"2"					+ EOL +
			"Hello!!!!!"		+ EOL +
			"3"					+ EOL +
			"Hi!! :-)" 			+ EOL,
			recorder.getStdoutRecording());
		
		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		// confirm expected published data
		assertEquals("Hey there",		_store.take("/messages/greeting/1"));
		assertEquals("Hello", 			_store.take("/messages/greeting/2"));
		assertEquals("Hi", 				_store.take("/messages/greeting/3"));
		assertEquals("Hey there!!",		_store.take("/messages/emphasizedGreeting1/1"));
		assertEquals("Hello!!", 		_store.take("/messages/emphasizedGreeting1/2"));
		assertEquals("Hi!!", 			_store.take("/messages/emphasizedGreeting1/3"));
		assertEquals("Hey there!! :-)",	_store.take("/messages/emphasizedGreeting2/1"));
		assertEquals("Hello!!!!!", 		_store.take("/messages/emphasizedGreeting2/2"));
		assertEquals("Hi!! :-)", 		_store.take("/messages/emphasizedGreeting2/3"));
		assertEquals("1",				_store.take("/messages/count/1"));
		assertEquals("2", 				_store.take("/messages/count/2"));
		assertEquals("3", 				_store.take("/messages/count/3"));

		assertEquals(0, 				_store.size());
	}

	public void test_MergingWorkflow() throws Exception {
		
		// declare and build the workflow
		final Workflow workflow = new WorkflowBuilder()
		
			.name("MergingWorkflow")
			.context(_context)
			
			.node(new TclNodeBuilder()
				.name("CreateGreetings")
				.sequence("s", new String[] {
						"Hey there my friend",
				        "Hello",
				        "Good night"})
				.step(
					"		set m [concat $s]							"
				)
				.outflow("m", "/messages/greeting")
			)

			.node(new TclNodeBuilder()
				.name("CreateEmphases")
				.sequence("s", new String[] {
						"!",
				        "!!",
				        "!!!"})
				.step(
					"		set m [concat $s]							"
				)
				.outflow("m", "/messages/emphasis")
			)
			
			.node(new TclNodeBuilder()
				.name("EmphasizeGreeting")
				.inflow("/messages/greeting/", "g")
				.inflow("/messages/emphasis/", "e")
				.step(
					"		set ge [concat ${g}${e}]					"
				)
				.outflow("ge", "/messages/emphasizedGreeting/")
			)
			
			.node(new TclNodeBuilder()
				.name("RenderEmphasizedGreeting")
				.inflow("/messages/emphasizedGreeting/", "value")
				.step(
					"		puts [concat $value]						"
				)
			)
			
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout
		assertEquals(
			"Hey there my friend!" 	+ EOL +
			"Hello!!" 				+ EOL +
			"Good night!!!"			+ EOL,
			recorder.getStdoutRecording());
		
		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		// confirm expected published data
		assertEquals("Hey there my friend",		_store.take("/messages/greeting/1"));
		assertEquals("Hello", 					_store.take("/messages/greeting/2"));
		assertEquals("Good night", 				_store.take("/messages/greeting/3"));
		assertEquals("!",						_store.take("/messages/emphasis/1"));
		assertEquals("!!", 						_store.take("/messages/emphasis/2"));
		assertEquals("!!!", 					_store.take("/messages/emphasis/3"));
		assertEquals("Hey there my friend!",	_store.take("/messages/emphasizedGreeting/1"));
		assertEquals("Hello!!", 				_store.take("/messages/emphasizedGreeting/2"));
		assertEquals("Good night!!!", 			_store.take("/messages/emphasizedGreeting/3"));

		assertEquals(0, 						_store.size());
}
	
	public void test_CountToThree() throws Exception {
		
		// declare and build the workflow
		final Workflow workflow = new WorkflowBuilder()
		
			.name("CountToThree")
			.context(_context)
			
			.node(new TclNodeBuilder()
				.name("GenerateIntegerSequence")
				.constant("initial",1)
				.constant("step", 	1)
				.constant("max", 	3)
				.initialize(
					"		set next $initial							"
				)
				.step(
					"		set value $next;							" +
					"		set next [expr $next + $step];				" +
					"		if { $value > $max } { 						" +
					"			disableOutput value;					" +
					"		}											"
				)
				.state("next")
				.outflow("value")
			    .endFlowOnNoOutput()
			)

			.node(new TclNodeBuilder()
				.name("RenderIntegerSequence")
				.inflow("GenerateIntegerSequence.value", "n")
				.step(
					"		puts [concat $n]							"
				)
			)
			
			.build();
		
		workflow.configure();
		workflow.initialize();

		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout
		assertEquals(
			"1" 	+ EOL +
			"2" 	+ EOL +
			"3"		+ EOL,
			recorder.getStdoutRecording());
		
		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		// confirm expected published data
		assertEquals("1",		_store.take("GenerateIntegerSequence.value/1"));
		assertEquals("2", 		_store.take("GenerateIntegerSequence.value/2"));
		assertEquals("3", 		_store.take("GenerateIntegerSequence.value/3"));
		
		assertEquals(0, 		_store.size());
	}

	public void test_IntegerFilter() throws Exception {
		
		// declare and build the workflow
		final Workflow workflow = new WorkflowBuilder()

			.name("IntegerFilter")
			.context(_context)
			
			.node(new TclNodeBuilder()
				.name("GenerateIntegerSequence")
				.constant("initial",1)
				.constant("step", 	1)
				.constant("max", 	10)
				.initialize(
					"		set next $initial								"
				)
				.step(
					"		set value $next;								" + 
					" 		set next [expr $next + $step];					" + 
					" 		if { $value > $max } { 							" + 
					"			disableOutput value;						" + 
					"		}												"
				)	  
				.state("next")
				.outflow("value", "/counter/value")
			    .endFlowOnNoOutput()
			)

			.node(new TclNodeBuilder()
				.name("DiscardIntegersOutsideDesiredRange")
				.constant("min", 3)
				.constant("max", 7)
				.inflow("/counter/value", "input")
				.step(
					"		if { $input >= $min && $input <= $max } {		" + 
					"			set output $input;							" + 
					"		} else {										" + 
					" 			disableOutput output;						" +
					"		}												"
				)
				.outflow("output", "/counter/filtered")
			)
						
			.node(new TclNodeBuilder()
				.name("RenderIntegerSequence")
				.inflow("/counter/filtered", "n")
				.step(
					"		puts [concat $n]								"
				)
			)
			
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout
		assertEquals(
			"3" 	+ EOL +
			"4" 	+ EOL +
			"5" 	+ EOL +
			"6" 	+ EOL +
			"7"		+ EOL,
			recorder.getStdoutRecording());
		
		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		// confirm expected published data
		assertEquals("1",		_store.take("/counter/value/1"));
		assertEquals("2",		_store.take("/counter/value/2"));
		assertEquals("3",		_store.take("/counter/value/3"));
		assertEquals("4",		_store.take("/counter/value/4"));
		assertEquals("5",		_store.take("/counter/value/5"));
		assertEquals("6",		_store.take("/counter/value/6"));
		assertEquals("7",		_store.take("/counter/value/7"));
		assertEquals("8",		_store.take("/counter/value/8"));
		assertEquals("9",		_store.take("/counter/value/9"));
		assertEquals("10",		_store.take("/counter/value/10"));
		
		assertEquals("3",		_store.take("/counter/filtered/1"));
		assertEquals("4",		_store.take("/counter/filtered/2"));
		assertEquals("5",		_store.take("/counter/filtered/3"));
		assertEquals("6",		_store.take("/counter/filtered/4"));
		assertEquals("7",		_store.take("/counter/filtered/5"));
	
		assertEquals(0, 		_store.size());
	}

	
	public void test_AdderLoop() throws Exception {
		
		// declare and build the workflow
		final Workflow workflow = new WorkflowBuilder()
		
			.name("CountToThree")
			.context(_context)
			
			.node(new TclNodeBuilder()
				.name("GenerateIntegerSequence")
				.constant("a", 1)
				.constant("b", 2)
				.inflow("/filtered", "b")
				.step(
					"		set sum [expr $a + $b]							"
				)
				.outflow("sum", "/sum")
			    .endFlowOnNoOutput()
			)
			
			.node(new TclNodeBuilder()
				.name("DiscardIntegersGreaterThanMax")
				.constant("min", 1)
				.constant("max", 7)
				.inflow("/sum", "input")
				.step(
					"		if { $input >= $min && $input <= $max } {		" + 
					"			set output $input;							" + 
					"		} else {										" + 
					" 			disableOutput output;						" +
					"		}												"
				)
				.outflow("output", "/filtered")
			)

			.node(new TclNodeBuilder()
				.name("RenderIntegers")
				.inflow("/filtered", "n")
				.step(
					"		puts [concat $n]							"
				)
			)
			
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout
		assertEquals(
			"3" 	+ EOL +
			"4" 	+ EOL +
			"5" 	+ EOL +
			"6" 	+ EOL +
			"7" 	+ EOL,
			recorder.getStdoutRecording());
		
		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		// confirm expected published data
		assertEquals("3",		_store.take("/sum/1"));
		assertEquals("4",		_store.take("/sum/2"));
		assertEquals("5",		_store.take("/sum/3"));
		assertEquals("6",		_store.take("/sum/4"));
		assertEquals("7",		_store.take("/sum/5"));
		assertEquals("8",		_store.take("/sum/6"));
		assertEquals("3",		_store.take("/filtered/1"));
		assertEquals("4",		_store.take("/filtered/2"));
		assertEquals("5",		_store.take("/filtered/3"));
		assertEquals("6",		_store.take("/filtered/4"));
		assertEquals("7",		_store.take("/filtered/5"));

		assertEquals(0, 		_store.size());
	}
	
	public void test_IntegerStreamMergeDuplicates() throws Exception {
		
		// declare and build the workflow
		final Workflow workflow = new WorkflowBuilder()
		
			.name("IntegerStreamMergeDuplicates")
			.context(_context)
			
			.node(new TclNodeBuilder()
				.name("CreateFirstIntegerSequence")
				.sequence("i", new Integer[] {1,1,1,3,3,3,6,9})
				.step(
					"		set n $i							"
				)
				.outflow("n", "/stream/one")
			)

			.node(new TclNodeBuilder()
				.name("CreateSecondIntegerSequence")
				.sequence("i", new Integer[] {1,1,1,1,3,3,5,6,6,6})
				.step(
					"		set n $i							"
				)
				.outflow("n", "/stream/two")
			)
			
			.node(new ActorNodeBuilder()
				.name("MergeTwoIntegerSequences")
				.inflow("/stream/one", "a")
				.inflow("/stream/two", "b")
				.actor(new TclActorBuilder()
					.input("a", InputProperty.Optional |
								InputProperty.Nullable | 
								InputProperty.DefaultReadinessFalse)
					.input("b", InputProperty.Optional | 
								InputProperty.Nullable | 
								InputProperty.DefaultReadinessFalse)
					.initialize(
						"      	set _last 0;								" +
						"      	set _first true;							" +
						"      	enableInput a;								" +
						"      	enableInput b;								"
					)
					.step(
						"		if { $a == {null} && $b == {null} } {		" +
						"	    	enableInput a;							" +
						"	        enableInput b;							" +
						"	        disableOutput c;						" +
						"	 	} elseif { $a == {null} } {					" +
						"	        set c $b;								" +
						"	        enableInput b;							" +
						"	 	} elseif { $b == {null} } {					" +
						"	        set c $a;								" +
						"	        enableInput a;							" +
						"	 	} elseif { $a < $b } {						" +
						"	        set c $a;								" +
						"	        enableInput a;							" +
						"	 	} elseif { $a > $b } {						" +
						"	        set c $b;								" +
						"	        enableInput b;							" +
						"	 	} elseif { $a == $b } {						" +
						"	        set c $a;								" +
						"	        enableInput a;							" +
						"	        enableInput b;							" +
						"	 	};											" + 
						"	 	if { $c != {null} } {						" + 
						"	 		if { $_first == {true} } {				" +
						"	 			set _last $c;						" +
						"	 			set _first false;					" +
						"	 		} else {								" +
						"	 			if { $c == $_last } {				" +
						"	 				disableOutput c;				" +
						"	 			} else {							" +
						"	 				set _last $c;					" +
						"	 			}									" +
						"	 		}										" +
						"	 	}; 											"
					)
					.state("a")
					.state("b")
					.state("_last")
					.state("_first")
					.output("c")
				)
				.outflow("c", "/stream/merged")
			)

			.node(new TclNodeBuilder()
				.name("RenderMergedIntegerSequence")
				.inflow("/stream/merged", "n")
				.step(
					"		puts [concat $n]							"
				)
			)
			
			.build();

		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});
			
		// confirm expected stdout
		assertEquals(
			"1" 	+ EOL +
			"3" 	+ EOL +
			"5" 	+ EOL +
			"6" 	+ EOL +
			"9" 	+ EOL,
			recorder.getStdoutRecording());
		
		// confirm that no error messages were generated
		assertEquals("", recorder.getStderrRecording());
		
		// confirm expected published data
		assertEquals("1",		_store.get("/stream/merged/1"));
		assertEquals("3",		_store.get("/stream/merged/2"));
		assertEquals("5",		_store.get("/stream/merged/3"));
		assertEquals("6",		_store.get("/stream/merged/4"));
		assertEquals("9",		_store.get("/stream/merged/5"));

		assertEquals(23, 		_store.size());
	}
}
