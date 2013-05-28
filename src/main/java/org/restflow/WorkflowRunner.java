package org.restflow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.restflow.actors.Actor;
import org.restflow.actors.Workflow;
import org.restflow.data.ProtocolRegistry;
import org.restflow.directors.Director;
import org.restflow.exceptions.ActorException;
import org.restflow.metadata.ActorState;
import org.restflow.metadata.FileSystemMetadataManager;
import org.restflow.metadata.MetadataManager;
import org.restflow.metadata.RunMetadata;
import org.restflow.metadata.Trace;
import org.restflow.metadata.TraceRecorder;
import org.restflow.reporter.Reporter;
import org.restflow.reporter.TraceReporter;
import org.restflow.util.ImmutableMap;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;

import org.restflow.yaml.spring.YamlBeanDefinitionReader;

public class WorkflowRunner {

	private Actor 				_actor;
	private boolean 			_cutTerminalConnectionsAfterPreamble;
	private boolean 			_validateOnly;
	private Map<String, String>	_importSchemeResourceMap;
	private Map<String, Object>	_inputValues;
	private MetadataManager 	_metadataManager;
	private String 				_prevRunName;
	private String 				_runName;
	private String 				_runsDirectory;
	private Map<String,Object>	_stateValues = new ImmutableMap<String,Object>();
	private boolean 			_suppressWorkflowStdout;
	private Director 			_topDirectorOverride;
	private boolean 			_traceReport;
	private WorkflowContext 	_context;
	private String 				_workflowDefinitionPath;
	private String 				_workflowDefinitionString;
	private InputStream 		_workflowDefinitionStream;
	private String 				_workflowName;
	private boolean 			_closeTraceRecorderAfterRun;
	
	public static final String YAML_EXTENSION = ".yaml";
	
	public static class Builder {
		private boolean validateOnly;
		private String workflowDefinitionPath;
		private String workflowName;
		private String runName;
		private String prevRunName;		
		private Map<String, Object> inputBindings;
		private String runsDirectory;
		private TraceRecorder traceRecorder;
		private InputStream workflowDefinitionStream;
		private boolean supressWorkflowStdout = false;
		private Director topDirectorOverride = null;
		private Map<String, String> importSchemeResourceMap;
		private boolean cutTerminalConnectionsAfterPreamble = false;
		private boolean traceReport = false;		
		private boolean closeTraceRecorderAfterRun = true;
		private Reporter finalReporter;
		private String workflowDefinitionString;

		public Builder() {
			super();
		}

		/**
		 * Sets the location of the workflow definition file. The path can be
		 * prepended with a uri scheme, such as 'classpath', 'file', 'url', or a
		 * user defined scheme using the importSchemeResourceMap() setting on
		 * this class.
		 * 
		 * @param val
		 * @return
		 */
		public Builder workflowDefinitionPath(String val) {
			workflowDefinitionPath = val;
			return this;
		}

		/**
		 * InputStream containing the workflow definition file. This allows a
		 * workflow definition to be piped into RestFlow from the command line.
		 * 
		 * @param val
		 * @return
		 */
		public Builder workflowDefinitionStream(InputStream val) {
			workflowDefinitionStream = val;
			return this;
		}

		/**
		 * 
		 * Specifies the name of the top level workflow to run. This is required
		 * if the workflow definition has multiple beans of the 'Workflow' type.
		 * 
		 * @param val
		 * @return
		 */
		public Builder workflowName(String val) {
			workflowName = val;
			return this;
		}

		/**
		 * Binds values to the named inputs of the Workflow.
		 * 
		 * @param val
		 * @return
		 */
		public Builder inputBindings(Map<String, Object> val) {
			inputBindings = val;
			return this;
		}

		/**
		 * 
		 * File system path to the root directory for RestFlow to place results
		 * and scratch files. On each invocation of RestFlow a unique
		 * timestamped directory will be created within this directory in order
		 * to keep the results of multiple invocations (i.e. runs of the
		 * program) separate.
		 * 
		 * @param val
		 * @return
		 */
		public Builder runsDirectory(String val) {
			runsDirectory = val;
			return this;
		}
		
		public Builder suppressWorkflowStdout(boolean val) {
			supressWorkflowStdout = val;
			return this;
		}

		/**
		 * Allows the Workflow's specified director to replaced with the
		 * specified director. This is provided for testing purposes.
		 * 
		 * @param val
		 * @return
		 */
		public Builder topDirectorOverride(Director val) {
			topDirectorOverride = val;
			return this;
		}

		/**
		 * In the 'import' section of the workflow definition file, the imported
		 * resources can be declared with a scheme (such as 'file' or
		 * 'classpath'). The importSchemResourceMap setting allows additional
		 * schemes to be declared and mapped to a different resource. An example
		 * of this feature is used in the tests to declare an 'actors' scheme
		 * which can be changed for each test to load a different actors
		 * implementation, such as groovy or java.
		 * 
		 * @param val
		 * @return
		 */
		public Builder importSchemeResourceMap(Map<String, String> val) {
			importSchemeResourceMap = val;
			return this;
		}

		
		/**
		 * If RestFlow is run in background from the command line, the preamble
		 * can be output and then the terminal can be cut. This is designed for
		 * the impersonation daemon to be able to run RestFlow (with ampersand),
		 * get some job data from RestFlow over stdout, and then have the
		 * terminal cut so that the blocking call ends for the impersonation
		 * daemon.
		 * 
		 * Another way to use this is with the ssh host -x
		 * "java -jar RestFlow -f /etc/etc -base testruns &" The preamble will
		 * be returned over stdout and then the terminal will be cut.
		 * 
		 * @throws IOException
		 */
		public Builder cutTerminalConnectionsAfterPreamble(boolean val) {
			cutTerminalConnectionsAfterPreamble = val;
			return this;
		}

		/**
		 * Indicate that the engine should output a trace report after the workflow
		 * run is complete.
		 * 
		 * @param val
		 * @return
		 */
		public Builder traceReport(boolean val) {
			traceReport = val;
			return this;
		}		
		
		public Builder runName(String val) {
			runName = val;
			return this;
		}
		
		public Builder prevRunName(String val) {
			prevRunName = val;
			return this;
		}

		public Builder traceRecorder(TraceRecorder recorder) {
			traceRecorder = recorder;
			return this;
		}

		public Builder validateOnly(boolean val) {
			validateOnly = val;
			return this;
		}

		public Builder closeTraceRecorderAfterRun(boolean val) {
			closeTraceRecorderAfterRun = val;
			return this;
		}
		
		public Builder workflowDefinitionString(String val) {
			workflowDefinitionString = val;
			return this;
		}

		public WorkflowRunner build() throws Exception {
			return new WorkflowRunner(this);
		}

	}
	

	public WorkflowRunner(Builder builder) throws Exception {
		
		// copy properties from builder to the new instance
		_workflowDefinitionPath = builder.workflowDefinitionPath;
		_workflowName = builder.workflowName;
		_inputValues = builder.inputBindings;
		_runsDirectory = builder.runsDirectory;
		_workflowDefinitionString = builder.workflowDefinitionString;
		_workflowDefinitionStream = builder.workflowDefinitionStream;
		_suppressWorkflowStdout = builder.supressWorkflowStdout;
		_topDirectorOverride = builder.topDirectorOverride;
		_importSchemeResourceMap = builder.importSchemeResourceMap;
		_cutTerminalConnectionsAfterPreamble = builder.cutTerminalConnectionsAfterPreamble;
		_runName = builder.runName;
		_prevRunName = builder.prevRunName;		
		_traceReport = builder.traceReport;
		_validateOnly = builder.validateOnly;
		_closeTraceRecorderAfterRun = builder.closeTraceRecorderAfterRun;
		
		// create the application context for the workflow run
		_context = new WorkflowContextBuilder()
			.importMappings(_importSchemeResourceMap)
			.recorder(builder.traceRecorder)
			.workflowDefinitionString(_workflowDefinitionString)
			.workflowDefinitionPath(_workflowDefinitionPath)
			.build();
		
		// get workflow definition and configuration details from external Spring bean definitions
		_loadBeansFromContext();
		
		// load state from the previous run directory if available
		if (_prevRunName != null) {
			if (_runsDirectory != null) {
				_stateValues = loadLastRunState(_runsDirectory, _prevRunName);
			} else {
				throw new Exception("cannot load state from previous run without a runs directory");	
			}
		}
		
		// create a new directory for the current run if a runs directory was provided
		if (_runsDirectory != null) {
			File runDirectory = _createRunDirectory();
			_metadataManager = new FileSystemMetadataManager();
			_metadataManager.setRunDirectory(runDirectory.getAbsolutePath());
			_context.setMetadataManager(_metadataManager);
			_context.setRunDirectory(runDirectory);
		}

		// get the metadata manager from the context
		_metadataManager = _context.getMetaDataManager();
	}

	public Actor run() throws Exception {
				
		_storePreRunMetadata();
		
		// render preamble report for the top-level workflow before standard streams are cut
		if (_actor instanceof Workflow) {
			
			// view the actor as a workflow
			Workflow workflow = (Workflow)_actor;

			// show the premble report immediately
			workflow.printPreambleReport();
			
			// now disable preamble and final reports on top-level workflow 
			// so that they aren't displayed by Workflow.run
			workflow.setShowPreambleReport(false);
			workflow.setShowFinalReport(false);
		}

		if (_cutTerminalConnectionsAfterPreamble) {
			_cutTerminalConnections();
		}
		
		
		final TraceRecorder traceRecorder = _context.getTraceRecorder();

		_metadataManager.buildStdoutRecorder(_suppressWorkflowStdout).recordExecution(new StdoutRecorder.WrappedCode() {
			
			@Override
			public void execute() throws Exception {
				
				try {

					// elaborate the workflow (or standalone actor)
					_actor.elaborate();
					
					// store workflow graph in trace DB if provided and not running a standalone actor
					if (_actor instanceof Workflow) {
						traceRecorder.recordWorkflowGraph((Workflow)_actor);
					}
					
					// configure and run the actor or workflow if not simply validating it
					if (! _validateOnly) {
						_actor.configure();
						_actor.initialize();
						_actor.loadInputValues(_inputValues);
						_actor.loadStateValues(_stateValues);
						_actor.step();
						_actor.wrapup();
						_actor.dispose();
						_writeOutputFile();
					}
					
				} catch (ActorException e) {
					System.err.println(e.getMessage());
					e.getCause().printStackTrace(System.err);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				} finally {
					// show the final report we just ran a workflow
					if ( _actor instanceof Workflow) {
						((Workflow)_actor).printFinalReport();
					}					
				}
			}
		});

		//TODO this block is needed so unit tests can compare the workflow output without the output template clutter.
		//TODO investigate modifying unit tests so this code block can be removed.
		if (!_cutTerminalConnectionsAfterPreamble && _traceReport ) {
			_renderTraceReport();
		}
		
		if (_closeTraceRecorderAfterRun) {
			traceRecorder.close();
		}
		
		return _actor;
	}

	public String generateDot() throws Exception {

//		_metadataManager.buildStdoutRecorder(_suppressWorkflowStdout).recordExecution(new StdoutRecorder.WrappedCode() {
//			@Override
//			public void execute() throws Exception {
//				try {
//					GroovyTemplateReporter reporter = new GroovyTemplateReporter();
//					reporter.setTemplate( IOUtils.toString(
//							_context.getResource(
//									"classpath:ssrl/restflow/templates/dotWorkflow.txt").getInputStream(), "UTF-8") );
//					
//					reporter.addToModel("workflow", _actor);
//					reporter.renderReport();
//				} catch (ActorException e) {
//					System.err.println(e.getMessage());
//					e.getCause().printStackTrace(System.err);
//				} catch (Exception e) {
//					e.printStackTrace(System.err);
//				}
//			}
//		});
//		
//		_writeOutputFile();
		
		return "";
	}	
	
	public void renderReport(Reporter reporter) throws Exception {
		if (reporter == null) return;
		
		reporter.decorateModel( _metadataManager.getRunMetadata(_context.getTraceRecorder()) );
		reporter.addToModel("workflow", _actor);
		
		reporter.renderReport();	
	}

	public void printWorkflowInputs() {

		System.out.println("Input               Description");
		System.out.println("-----               -----------");

		String spaces = "                                         ";
		for (String name : _actor.getInputNames()) {
			String description = _actor.getInputDescription(name);
			Object defaultValue = _actor.getDefaultInputValue(name);
			System.out.print(name + spaces.substring(0, 20 - name.length()));
			if (description.length() > 0) {
				System.out.print(description + " ");
			}
			if (defaultValue != null) {
				System.out.print("(default: " + defaultValue + ")");
			}
			System.out.println();
		}
	}

	public String getProductsAsString() throws Exception {
		String workingDirectoryName = _metadataManager.getRunDirectory();
		RunMetadata metadata = FileSystemMetadataManager.restoreMetadata(workingDirectoryName);
		return metadata.getProductsYaml();
	}

	public MetadataManager getMetadataManager() {
		return _metadataManager;
	}
	
	public RunMetadata getRunMetaData() throws Exception {
		return _metadataManager.getRunMetadata(_context.getTraceRecorder());
	}

	public String getStderrRecording() throws Exception {
		return _metadataManager.getStderrRecording();
	}

	public String getStdoutRecording() throws Exception {
		return _metadataManager.getStdoutRecording();
	}
	
	public Trace getTrace() throws Exception {
		TraceRecorder recorder = _context.getTraceRecorder();
		RunMetadata metadata = _metadataManager.getRunMetadata(recorder);
		return metadata.getTrace();		
	}
	
	public String getTraceReport() throws Exception {
		return TraceReporter.getReport(getTrace());
	}

	public String getRunDirectory() {
		return _metadataManager.getRunDirectory();
	}

	/**
	 * If RestFlow is run in background from the command line, the header can be
	 * output and then the terminal can be cut. This is designed for the
	 * impersonation daemon to be able to run RestFlow (with ampersand), get
	 * some job data from RestFlow over stdout, and then have the terminal cut
	 * so that the blocking call on ends for the impersonation daemon.
	 * 
	 * Another way to use this is with the ssh -x
	 * "java -jar RestFlow -f /etc/etc -base testruns &" The header will be
	 * returned over stdout and then the terminal will be cut.
	 * 
	 * @throws IOException
	 */
	private void _cutTerminalConnections() throws IOException {
		System.out.close();
		System.err.close();
		System.in.close();
	}

	private void _loadBeansFromContext() throws Exception {
		
		// create a yaml bean reader
		YamlBeanDefinitionReader rdr = new YamlBeanDefinitionReader(_context);

		// register bean definitions from the provided definition path or stream
		if (_workflowDefinitionPath != null) {
			rdr.registerBeanDefinitions(_workflowDefinitionPath);
		} else if (_workflowDefinitionStream != null) {
			rdr.loadBeanDefinitions(_workflowDefinitionStream, "-");
		} else if (_workflowDefinitionString != null) {
			InputStream stream = new YamlStream(_workflowDefinitionString);
			rdr.loadBeanDefinitions(stream, "-");
		} else {
			throw new Exception("Must set either workflowDefinitionPath or workflowDefinitionStream");
		}

		// read the bean definitions
		_context.refresh();
		
		// load the protocol registry bean
		ProtocolRegistry protocolRegistry = 
			(ProtocolRegistry)_context.getBean("DefaultProtocolRegistry");
		if (protocolRegistry != null) {
			_context.setProtocolRegistry(protocolRegistry);
		}
		
		// load all Actor beans
		String actorBeanNames[] = _context.getBeanNamesForType(Class.forName("org.restflow.actors.Actor"));
		if (actorBeanNames.length == 0) {
			throw new Exception("Must have at least one Actor defined in workflow script.");
		}

		//If the workflow name isn't specified then a single Workflow bean in the context will be used
		if (_workflowName == null) {

			String workflowBeanNames[] = 
				_context.getBeanNamesForType(Class.forName("org.restflow.actors.Workflow"));			
			
			if (workflowBeanNames.length == 1) {
				_workflowName = workflowBeanNames[0];
			} else {
				throw new Exception("Must specify one of the following workflows or actors: "
										+ Arrays.asList(actorBeanNames));
			}
		}

		_actor = (Actor) _context.getBean(_workflowName);

		// Optionally override the director for the top-level worflow
		if (_topDirectorOverride != null) {
			if (! (_actor instanceof Workflow) ) {
				throw new Exception ("can not set a Director on an Actor while instantiating " + _workflowName);
			}
			Workflow workflow = (Workflow)_actor;
			_topDirectorOverride.setApplicationContext(_context);
			workflow.setDirector(_topDirectorOverride);
		}
	}

	private  Map<String,Object> loadLastRunState(String runsDirectoryName, String previousRunName) throws Exception {
		RunMetadata prevMetadata = FileSystemMetadataManager.restoreMetadata(runsDirectoryName + "/" + previousRunName);
		ActorState previousState = prevMetadata.getActorState();
		Map<String,Object> stateValues = previousState.getStateValues();
		return stateValues;
	}
		
	private File _createRunDirectory() throws Exception {
		
		File runDir = null;
		if (_runName != null) {
			runDir = new File(_runsDirectory + "/" + _runName);
			if (runDir.exists()) throw new Exception(_runName + " already exists in " + _runsDirectory);
		} else {
			runDir = PortableIO.createUniqueTimeStampedDirectory(_runsDirectory, _workflowName);
		}
		
		return runDir;
	}
	
	/* 
	 * Call after a run to generate a trace report.  The metadata should still be in the MetadataManager memory.
	 * 
	 */
	private void _renderTraceReport () throws Exception {
		TraceReporter traceReporter = new TraceReporter();
		traceReporter.decorateModel(_metadataManager.getRunMetadata(_context.getTraceRecorder()));		
		
		traceReporter.renderReport();		
	}

	private void _storePreRunMetadata() throws Exception {
		
		_metadataManager.storeWorkflowInputs(_inputValues);
	
		_metadataManager.storeProcessInfo();
		
		if (_actor instanceof Workflow) {
			_metadataManager.storeReportDefinitions( ((Workflow)_actor).getReports());
		}
	}

	private void _writeOutputFile() throws Exception {
		_metadataManager.storeWorkflowOutputs(_actor);		
	}

	public void dispose() throws SQLException {
		_context.getTraceRecorder().close();
		
	}
	
	@SuppressWarnings("deprecation")
	public static class YamlStream extends StringBufferInputStream {
		
		public YamlStream(String yaml) {
			super(yaml);
		}
	}	

}
