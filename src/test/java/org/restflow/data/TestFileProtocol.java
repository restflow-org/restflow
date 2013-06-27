package org.restflow.data;

import java.io.File;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.FileProtocol;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;
import org.restflow.util.TestUtilities;


public class TestFileProtocol extends RestFlowTestCase {

	private ConsumableObjectStore 	_store;
	private String 					_testRunsDirectoryPath;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_testRunsDirectoryPath = TestUtilities.getTestRunsDirectoryPath();
	}
	
	public void testWriteFile_SingleNodeWorkflow_OneFileInOneDirectory() throws Exception {
		
		// create a run directory for this test
		File runDirectory = PortableIO.createUniqueTimeStampedDirectory(
				_testRunsDirectoryPath, "testWriteFile");
		
		// create a run context specifying the run directory
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("file", new FileProtocol())
			.runDirectory(runDirectory)
			.build();
		
		/// build the workflow
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.bean(new Object() {
					public String message;
					public void step() {message="Hello";}
				})
				.outflow("message", "file:/greeting.txt"))
			.build();
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		@SuppressWarnings("unused")
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm type and value of published data item
		assertTrue(_store.get("/greeting.txt") instanceof String);
		assertEquals("Hello", _store.take("/greeting.txt"));
		
		// make sure nothing else was published
		assertEquals(0, _store.size());
		
		// check that the run directory was created
		assertTrue(runDirectory.exists());
		assertTrue(runDirectory.isDirectory());
		
		// get the file listing for the run directory
		File[] runFiles = runDirectory.listFiles();

		// make sure run directory contains exactly one file
		assertEquals(1, runFiles.length);
		
		// check the name, type, and contents of the single published file
		File greetingFile = runFiles[0];
		assertTrue(greetingFile.isFile());
		assertEquals("greeting.txt", greetingFile.getName());
		assertEquals("Hello" + PortableIO.EOL, PortableIO.readTextFile(greetingFile));
	}
	
	public void testWriteFile_SingleNodeWorkflow_MultipleFilesInOneDirectory() throws Exception {
		
		// create a run directory for this test
		File runDirectory = PortableIO.createUniqueTimeStampedDirectory(
				_testRunsDirectoryPath, "testWriteFile");
		
		// create a run context specifying the run directory
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("file", new FileProtocol())
			.runDirectory(runDirectory)
			.build();
		
		/// build the workflow
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 
			.context(context)
			.node(new JavaNodeBuilder()
				.bean(new Object() {
					public String message;
					public int messageLength;
					public void step() {
						message = "Hello";
						messageLength = message.length();
					}
				})
				.outflow("message", "file:/greeting.txt")
				.outflow("messageLength", "file:/length.txt"))
			.build();
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		@SuppressWarnings("unused")
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm type and value of published data items
		assertTrue(_store.get("/greeting.txt") instanceof String);
		assertEquals("Hello", _store.take("/greeting.txt"));

		assertTrue(_store.get("/length.txt") instanceof Integer);
		assertEquals(5, _store.take("/length.txt"));
		
		// make sure nothing else was published
		assertEquals(0, _store.size());
		
		// check that the run directory was created
		assertTrue(runDirectory.exists());
		assertTrue(runDirectory.isDirectory());
		
		// get the file listing for the run directory
		File[] runFiles = runDirectory.listFiles();

		// make sure run directory contains exactly two files
		assertEquals(2, runFiles.length);
		
		// check the names, types, and contents of the published files
		File greetingFile = new File(runDirectory + "/greeting.txt");
		assertTrue(greetingFile.isFile());
		assertEquals("Hello" + PortableIO.EOL, PortableIO.readTextFile(greetingFile));

		File lengthFile = new File(runDirectory + "/length.txt");
		assertTrue(lengthFile.isFile());
		assertEquals("5" + PortableIO.EOL, PortableIO.readTextFile(lengthFile));
	}
	
	public void testWriteFile_TwoNodeWorkflow_FilesPassedBetweenNodes() throws Exception {
		
		// create a run directory for this test
		File runDirectory = PortableIO.createUniqueTimeStampedDirectory(
				_testRunsDirectoryPath, "testWriteFile");
		
		// create a run context specifying the run directory
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("file", new FileProtocol())
			.runDirectory(runDirectory)
			.build();
		
		/// build the workflow
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder() 

			.context(context)
			
			.node(new JavaNodeBuilder()
				.bean(new Object() {
					public String greetingOne, greetingTwo;
					public void step() {
						greetingOne = "Hello";
						greetingTwo = "Hey";
					}
				})
				.outflow("greetingOne", "file:/greetingOne.txt")
				.outflow("greetingTwo", "file:/greetingTwo.txt"))
				
			.node(new JavaNodeBuilder()
				.bean(new Object() {
					public File messageOne, messageTwo;
				})
				.inflow("file:/greetingOne.txt", "messageOne")
				.inflow("file:/greetingTwo.txt", "messageTwo"))
				
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		@SuppressWarnings("unused")
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		// confirm type and value of published data items
		assertTrue(_store.get("/greetingOne.txt") instanceof String);
		assertEquals("Hello", _store.take("/greetingOne.txt"));

		assertTrue(_store.get("/greetingTwo.txt") instanceof String);
		assertEquals("Hey", _store.take("/greetingTwo.txt"));
		
		// make sure nothing else was published
		assertEquals(0, _store.size());
		
		// check that the run directory was created
		assertTrue(runDirectory.exists());
		assertTrue(runDirectory.isDirectory());
		
		// get the file listing for the run directory
		@SuppressWarnings("unused")
		File[] runFiles = runDirectory.listFiles();

		// check the names, types, and contents of the published files
		File greetingFile = new File(runDirectory + "/greetingOne.txt");
		assertTrue(greetingFile.isFile());
		assertEquals("Hello" + PortableIO.EOL, PortableIO.readTextFile(greetingFile));

		File lengthFile = new File(runDirectory + "/greetingTwo.txt");
		assertTrue(lengthFile.isFile());
		assertEquals("Hey" + PortableIO.EOL, PortableIO.readTextFile(lengthFile));
	}
	
	public void test_SubWorkflowMultiRunsNoResets() throws Exception {
		
		// create a run directory for this test
		File multirunDirectory = PortableIO.createUniqueTimeStampedDirectory(
				_testRunsDirectoryPath, "testWriteFile");
		
		// create a run context specifying the run directory
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("file", new FileProtocol())
			.runDirectory(multirunDirectory)
			.build();
		
		@SuppressWarnings("unused")
		final Workflow workflow = new WorkflowBuilder()

			.name("top")
			.context(context)
			.prefix("/run{RUN}")
			
			.inflow("u", "/inputNumber")
		
			.node(new JavaNodeBuilder() 
				.inflow("/inputNumber", "m")
				.stepsOnce()
				.bean(new Object() {
					public int m, n;
					public void step() { n = m + 1;}
				})
				.outflow("n", "/incrementedInputNumber"))
			
			.node(new WorkflowNodeBuilder()
			
				.stepsOnce()
			
				.name("multiplier")
				.inflow("/inputNumber", "/multiplier")
				.inflow("/incrementedInputNumber", "/multiplicand")
					
				.node(new JavaNodeBuilder()
					.inflow("/multiplier", "x")
					.inflow("/multiplicand", "y")
					.stepsOnce()
					.bean(new CloneableBean() {
						public int x, y, z;
						public void step() {z = x * y; System.out.println(x + "*" + y);}
						})
					.outflow("z", "/product"))
				
				.node(new JavaNodeBuilder()
					.inflow("/product", "value")
					.bean(new Object() {
						public int value;
						public void step() {System.out.println(value);}
					}))
					
				.outflow("/product", "file:/outputNumber")
				)
				
			.outflow("/outputNumber", "v")
			
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		StringBuffer multirunStdout = new StringBuffer();
		
		for (int u = 1; u <= 3; u++) {
			workflow.set("u", u);
			StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
				public void execute() throws Exception {workflow.run();}});
			multirunStdout.append(recorder.getStdoutRecording());
			assertEquals(u*(u+1), workflow.get("v"));
		}

		// confirm expected stdout showing three values printed
		assertEquals(
			"1*2" 	+ EOL +
			"2" 	+ EOL +
			"2*3" 	+ EOL +
			"6" 	+ EOL +
			"3*4" 	+ EOL +
			"12" 	+ EOL,
			multirunStdout.toString());
		
		assertEquals(1, _store.take("/run1/inputNumber"));
		assertEquals(2, _store.take("/run2/inputNumber"));
		assertEquals(3, _store.take("/run3/inputNumber"));

		assertEquals(2, _store.take("/run1/incrementedInputNumber"));
		assertEquals(3, _store.take("/run2/incrementedInputNumber"));
		assertEquals(4, _store.take("/run3/incrementedInputNumber"));
		
		assertEquals(1, _store.take("/run1/top.multiplier/multiplier"));
		assertEquals(2, _store.take("/run2/top.multiplier/multiplier"));
		assertEquals(3, _store.take("/run3/top.multiplier/multiplier"));

		assertEquals(2, _store.take("/run1/top.multiplier/multiplicand"));
		assertEquals(3, _store.take("/run2/top.multiplier/multiplicand"));
		assertEquals(4, _store.take("/run3/top.multiplier/multiplicand"));

		assertEquals( 2, _store.take("/run1/top.multiplier/product"));
		assertEquals( 6, _store.take("/run2/top.multiplier/product"));
		assertEquals(12, _store.take("/run3/top.multiplier/product"));
		
		assertEquals( 2, _store.take("/run1/outputNumber"));
		assertEquals( 6, _store.take("/run2/outputNumber"));
		assertEquals(12, _store.take("/run3/outputNumber"));
		
		assertEquals(0, _store.size());

		// check that the run directory was created
		assertTrue(multirunDirectory.exists());
		assertTrue(multirunDirectory.isDirectory());
		
		// get the file listing for the run directory and make sure it contains three subrun directories
		File[] runDirectories = multirunDirectory.listFiles();
		assertEquals(3, runDirectories.length);
				
		// check the names, types, and contents of three sub-run directories
		File runDir1 = new File(multirunDirectory + "/run1");
		assertTrue(runDir1.isDirectory());
		assertEquals(1, runDir1.listFiles().length);		
		File outputFile1 = new File(runDir1 + "/outputNumber");
		assertTrue(outputFile1.isFile());
		assertEquals("2" + PortableIO.EOL, PortableIO.readTextFile(outputFile1));

		File runDir2 = new File(multirunDirectory + "/run2");
		assertTrue(runDir2.isDirectory());
		assertEquals(1, runDir2.listFiles().length);		
		File outputFile2 = new File(runDir2 + "/outputNumber");
		assertTrue(outputFile2.isFile());
		assertEquals("6" + PortableIO.EOL, PortableIO.readTextFile(outputFile2));

		File runDir3 = new File(multirunDirectory + "/run3");
		assertTrue(runDir3.isDirectory());
		assertEquals(1, runDir3.listFiles().length);		
		File outputFile3 = new File(runDir3 + "/outputNumber");
		assertTrue(outputFile3.isFile());
		assertEquals("12" + PortableIO.EOL, PortableIO.readTextFile(outputFile3));
	}
}
