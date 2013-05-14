/**
    This performs coarse-grained unit tests for the RestFlow CLI  
 * 
 */
package org.restflow.test.system;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.restflow.RestFlow;
import org.restflow.metadata.FileSystemMetadataManager;
import org.restflow.metadata.RunMetadata;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.StdoutRecorder;
import org.restflow.util.TestUtilities;


public class TestRestFlowExecutesActors extends RestFlowTestCase {
	
	public void testRestFlowExecuteActor() throws Exception {

		String testRunsDir = TestUtilities.getTestRunsDirectoryPath();
	    // Create temp file.
	    final File temp = File.createTempFile("tmp", ".yaml");
	    temp.deleteOnExit();
	    
	    //specify a run name so that we can get the metadata back out after the run.
	    String runName = temp.getName().substring(0, temp.getName().indexOf("."));

	    // Write to temp file
	    FileUtils.writeStringToFile(temp, "{a: 5,b: 5}");

				
		RestFlow.main(new String[]{"-f","classpath:common/java/actors.yaml",
					"-w","Adder","-run", runName,
					"-base", testRunsDir, "-infile",temp.getPath() } );		
		
		RunMetadata runMetadata = FileSystemMetadataManager.restoreMetadata(testRunsDir + "/" + runName );

		assertEquals( 10, runMetadata.getOutputValues().get("sum") );
	}
	
	
	
	public void testRestFlowExecuteRestFlowActor() throws Exception {

		String testRunsDir = TestUtilities.getTestRunsDirectoryPath();
	    // Create temp file.
	    final File temp = File.createTempFile("tmp", ".yaml");
	    temp.deleteOnExit();
	    
	    //specify a run name so that we can get the metadata back out after the run.
	    String runName = temp.getName().substring(0, temp.getName().indexOf("."));

	    // Write to temp file
	    FileUtils.writeStringToFile(temp, "{a: 5,b: 5}");

				
		RestFlow.main(new String[]{"-f","classpath:common/restflowActors/actors.yaml",
					"-w","Adder","-run", runName,
					"-base", testRunsDir, "-infile",temp.getPath() } );		
		
		RunMetadata runMetadata = FileSystemMetadataManager.restoreMetadata(testRunsDir + "/" + runName );

		Map<String,Object> outputValues = runMetadata.getOutputValues(); 
		assertEquals( 10, outputValues.get("sum") );
	}

	public void testRestFlowExecuteStatefulRestFlowActor() throws Exception {

		String testRunsDir = TestUtilities.getTestRunsDirectoryPath();
	    // Create temp file.
	    final File temp = File.createTempFile("tmp", ".yaml");
	    temp.deleteOnExit();
	    
	    //specify a run name so that we can get the metadata back out after the run.
	    String runName = temp.getName().substring(0, temp.getName().indexOf("."));

	    // Write to temp file
	    FileUtils.writeStringToFile(temp, "{input: 'count this input'}");

				
		RestFlow.main(new String[]{"-f","classpath:common/java/actors.yaml",
					"-w","Counter","-run", runName + "_1",
					"-base", testRunsDir, "-infile",temp.getPath() } );		
		
		RunMetadata runMetadata = FileSystemMetadataManager.restoreMetadata(testRunsDir + "/" + runName + "_1" );

		assertNotNull( "should have state values in metadata",runMetadata.getActorState().getStateValues());

		assertEquals( 1, runMetadata.getOutputValues().get("count") );
		
		RestFlow.main(new String[]{"-f","classpath:common/java/actors.yaml",
				"-w","Counter","-run", runName + "_2",
				"-base", testRunsDir, "-prevrun", runName +"_1" , "-infile",temp.getPath() } );		

		runMetadata = FileSystemMetadataManager.restoreMetadata(testRunsDir + "/" + runName + "_2" );

		assertNotNull( "should have state values in metadata",runMetadata.getActorState().getStateValues());

		assertEquals( 2, runMetadata.getOutputValues().get("count") );
		
	}

	public void testRestFlowExecuteGreetingActor() throws Exception {

		final String testRunsDir = TestUtilities.getTestRunsDirectoryPath();
		
		StdoutRecorder stdoutRecorder = new StdoutRecorder(false);
		stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {
				RestFlow.main(new String[]{"-f","classpath:actors/hello_actor.yaml",
						"-base", testRunsDir, "-w","HelloWorld" } );		
			}
		});
		assertEquals("Hello World!", stdoutRecorder.getStdoutRecording() );
	}	
	
}