package org.restflow.test;

import java.io.File;
import java.util.Map;

import org.restflow.WorkflowRunner;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;
import org.restflow.util.TestUtilities;


abstract public class StandaloneActorTestCase extends WorkflowTestCase {
	
	public StandaloneActorTestCase(String parentDirectoryName) {
		super(parentDirectoryName);
	}
		
	public void setUp() throws Exception {
		super.setUp();
	}
	
	protected void loadAndRunActor(String testDirectory, String actorFilePath, String actorName, Map<String,Object> inputBindings) throws Exception {
		
		_testDirectory = testDirectory;
		
		String testRunsDirectory;
		if (_useWorkingDirectory) {
			 testRunsDirectory= TestUtilities.getTestRunsDirectoryPath();
		} else {
			testRunsDirectory = null;
		}

		String workspaceDirectory = PortableIO.getCurrentDirectoryPath() + _resourceDirectory + _parentDirectory + "/" + testDirectory +"/";
		_importSchemeToResourceMap.put("workspace", "file:" + workspaceDirectory);

		
		System.out.println( "-----------------------------------------------------------");
		System.out.println( "Actor: " + actorName);
		System.out.println(	"-----------------------------------------------------------");
		
		_runner = new WorkflowRunner.Builder()
				.workflowName(actorName)
				.importSchemeResourceMap(_importSchemeToResourceMap)
				.workflowDefinitionPath(actorFilePath)
				.runsDirectory( testRunsDirectory )
				.inputBindings(inputBindings)
				.suppressWorkflowStdout(false)
				.closeTraceRecorderAfterRun(false)
				.build();
		
		_stdoutRecorder = new StdoutRecorder(false);
		_stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {
				_runWorkflow();
			}
		});
	}		
	
	protected void _runWorkflow() throws Exception {

		//System.out.println(_runner.generateDot());
		_runner.run();
		
		if (_runner.getRunDirectory() != null) {
			_runDirectory = new File(_runner.getRunDirectory());
		}
	}

}
