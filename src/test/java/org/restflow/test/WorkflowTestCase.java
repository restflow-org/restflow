package org.restflow.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restflow.RestFlow;
import org.restflow.WorkflowRunner;
import org.restflow.actors.Workflow;
import org.restflow.directors.DataDrivenDirector;
import org.restflow.directors.DemandDrivenDirector;
import org.restflow.directors.Director;
import org.restflow.directors.MTDataDrivenDirector;
import org.restflow.directors.PublishSubscribeDirector;
import org.restflow.reporter.JunitFinalReporter;
import org.restflow.reporter.MultiRunReporter;
import org.restflow.reporter.Reporter;
import org.restflow.reporter.ReporterUtilities;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;
import org.restflow.util.TestUtilities;


abstract public class WorkflowTestCase extends RestFlowTestCase {

	protected String _parentDirectory = "";
	private String _directory;
	private boolean _useWorkingDirectory;
	public File _runDirectory;
	protected Map<String,String> _importSchemeToResourceMap;
	protected Workflow _workflow;
	protected WorkflowRunner _runner;
	protected StdoutRecorder _stdoutRecorder;
	protected Reporter _finalReporter;
	protected boolean _teeLogToStandardOutput;
	protected String _resourceDirectory = "/src/test/resources/";
	
	public WorkflowTestCase(String parentDirectoryName) {
		_parentDirectory = parentDirectoryName;
	}
	
	protected void _useWorkingDirectory() {
		_useWorkingDirectory = true;
	}
	
	public void setUp() throws Exception {
		super.setUp();
		System.gc();
/*		while (systemMemoryLow()) {
			System.out.println("System memory low.  Sleeping");
			Thread.sleep(1000);
		}*/
		_useWorkingDirectory = false;
		_importSchemeToResourceMap = new HashMap<String, String>();
		_finalReporter = new JunitFinalReporter();
		_teeLogToStandardOutput = true;
	}
	
	@SuppressWarnings("unused")
	private boolean systemMemoryLow() {
		
		long freeMemory = Runtime.getRuntime().freeMemory();
		long maxMemory = Runtime.getRuntime().maxMemory();
		float percentFree = (float)Math.abs(freeMemory - maxMemory) / (float)maxMemory * 100;
		
		System.out.println("Runtime memory percent remaining: " + percentFree);
		if (percentFree <50) {
			return true;
		}
			
		return false;
	}

	protected void assertFileResourcesMatchExactly(String resourcePath) throws Exception {
		
		File expectedRoot = new File(PortableIO.getCurrentDirectoryPath() + _resourceDirectory  + _parentDirectory + "/" + _directory + "/expected/");
		File actualRoot = new File(_runDirectory.getAbsolutePath());
		
		assertFilesystemResourceValid(expectedRoot, actualRoot, resourcePath);
	}

	protected void assertFileMatchesTemplate(String resourcePath) throws Exception {
		
		File expectedRoot = new File(PortableIO.getCurrentDirectoryPath() + "/" + _resourceDirectory+ _parentDirectory + "/" + _directory + "/expected/");
		File actualRoot = new File(_runDirectory.getAbsolutePath());
		
		assertFilesystemResourceMatchesTemplate(expectedRoot, actualRoot, resourcePath);
	}
	
	protected DataDrivenDirector _dataDrivenDirector() {
		DataDrivenDirector director = new DataDrivenDirector();
		director.afterPropertiesSet();
		return director;
	}

	protected MTDataDrivenDirector _MTDataDrivenDirector() {
		MTDataDrivenDirector director = new MTDataDrivenDirector();
		director.afterPropertiesSet();
		return director;
	}

	protected DemandDrivenDirector _demandDrivenDirector() {
		DemandDrivenDirector director = new DemandDrivenDirector();
		director.afterPropertiesSet();
		return director;
	}
		
	protected PublishSubscribeDirector _publishSubscribeDirector() {
		PublishSubscribeDirector director = new PublishSubscribeDirector();
		director.afterPropertiesSet();
		return director;
	}

	protected PublishSubscribeDirector _jobDependencyDirector() {
		PublishSubscribeDirector director = new PublishSubscribeDirector();
		director.setNodesStepOnce(true);
		director.afterPropertiesSet();		
		return director;
	}

	
	protected void configureForGroovyActor() {
		_importSchemeToResourceMap.put("actors", "classpath:common/groovy/");
		_importSchemeToResourceMap.put("testActors", "classpath:testActors/groovy/");		
	}

	protected void configureForBeanActor() {
		_importSchemeToResourceMap.put("actors", "classpath:common/java/");
		_importSchemeToResourceMap.put("testActors", "classpath:testActors/java/");				
	}	
		
	protected void configureForRestFlowActor() {
		_importSchemeToResourceMap.put("actors", "classpath:common/restflowActors/");
		_importSchemeToResourceMap.put("testActors", "classpath:testActors/restflowActors/");								
	}	

	protected void _loadAndRunWorkflow(String name, Director director) throws Exception {
		_loadAndRunWorkflow(name, name, director, name);
	}

	protected void _loadAndRunWorkflow(String name, String workflow, Director director) throws Exception {
		_loadAndRunWorkflow(name, name, director, workflow);
	}
	
	
	protected void _loadAndRunWorkflow(String directory, final String name, 
			Director director, String  bean) throws Exception {
		
		_directory = directory;
		
		String testRunsDirectory;
		if (_useWorkingDirectory) {
			 testRunsDirectory= TestUtilities.getTestRunsDirectoryPath();
		} else {
			testRunsDirectory = null;
		}

		
		
		String workflowFilePath = _parentDirectory + "/" + _directory + "/" + name + WorkflowRunner.YAML_EXTENSION;
		String workspaceDirectory = PortableIO.getCurrentDirectoryPath() + _resourceDirectory + _parentDirectory + "/" + directory +"/";
		_importSchemeToResourceMap.put("workspace", "file:" + workspaceDirectory);

		
		System.out.println( "-----------------------------------------------------------");
		System.out.println( "Workflow: " + bean);
		System.out.println(	"-----------------------------------------------------------");
		
		_runner = new WorkflowRunner.Builder()
				.workflowName(bean)
				.importSchemeResourceMap(_importSchemeToResourceMap)
				.workflowDefinitionPath(workflowFilePath)
				.runsDirectory( testRunsDirectory )
				.suppressWorkflowStdout(false)
				.topDirectorOverride(director)
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

	
	public void test_RestFlowMain(final String[] args) throws Exception {

		//String base = PortableIO.getCurrentDirectoryPath() + _parentDirectory + "/multirun/";

		_stdoutRecorder = new StdoutRecorder(false);
		_stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {

			@Override
			public void execute() throws Exception {
				RestFlow.main( args );
			}
		});
			
	}			
	
	protected void loadAndRunReport(String runDirectory,  String reportName) throws Exception {
		
		_directory = runDirectory;
			
		final String workflowFilePath =  PortableIO.getCurrentDirectoryPath() + _resourceDirectory + _parentDirectory + "/" + _directory;

		loadAndRunReport(new File(workflowFilePath), reportName);
	}

	protected void loadAndRunReport(File runDirectory,  String reportName) throws Exception {
					
		final String workflowFilePath =  runDirectory.getAbsolutePath();
		final Reporter reporter = ReporterUtilities.loadReporterFromRunDirectory( workflowFilePath, reportName);

		_stdoutRecorder = new StdoutRecorder(false);
		_stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {

				reporter.renderReport();
			}
		});
	}
	
	protected void multiRunReport(String runDirectory,  String reportName) throws Exception {
		
		_directory = runDirectory;
			
		final String workflowFilePath =  PortableIO.getCurrentDirectoryPath() + "/" + _parentDirectory + "/" + _directory;

		multiRunReport(new File(workflowFilePath), reportName);
	}
	
	protected void multiRunReport(File runDirectory,  String reportName) throws Exception {
		
		final String workflowFilePath =  runDirectory.getAbsolutePath();
		final MultiRunReporter wf = new MultiRunReporter( workflowFilePath,reportName);
		
		
		_stdoutRecorder = new StdoutRecorder(false);
		_stdoutRecorder.recordExecution(new StdoutRecorder.WrappedCode() {
			@Override
			public void execute() throws Exception {
				wf.renderReport();
			}
		});
	}
	
//	ProtocolRegistry r = _workflow.getProtocolRegistry();
//	LogProtocol protocol =  (LogProtocol)(r.getProtocolForScheme("file"));
//	protocol.setTeeToStandardOut(true);


	protected void _runWorkflow() throws Exception {

		//System.out.println(_runner.generateDot());
		_workflow = (Workflow)_runner.run();
		if (_runner.getRunDirectory() != null) {
			_runDirectory = new File(_runner.getRunDirectory());
		}
	}
	
	protected String _getExpectedTrace() throws IOException {
		return _getExpectedResultFile("trace.txt");
	}

	protected String _getExpectedResultFile(String filename) throws IOException {
		return PortableIO.readTextFileOnClasspath(_parentDirectory + "/" + _directory + "/" + filename);
	}

	protected String _getExpectedStdout() throws IOException {
		return _getExpectedStdout("stdout.txt");
	}

	protected String _getExpectedStdout(String filename) throws IOException {
		return PortableIO.readTextFileOnClasspath(_parentDirectory + "/" + _directory + "/" + filename);
	}	

	protected String _getExpectedStderr() throws IOException {
		return _getExpectedStderr("stderr.txt");
	}

	protected String _getExpectedStderr(String filename) throws IOException {
		return PortableIO.readTextFileOnClasspath(_parentDirectory + "/" + _directory + "/" + filename);
	}
	
	protected String _getExpectedProducts() throws IOException {
		return _getExpectedProducts("products.yaml");
	}

	protected String _getExpectedProducts(String filename) throws IOException {
		return PortableIO.readTextFileOnClasspath(_parentDirectory + "/" + _directory + "/" + filename);
	}

	
}
