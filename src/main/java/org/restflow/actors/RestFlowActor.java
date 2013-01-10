package org.restflow.actors;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.restflow.RestFlow;
import org.restflow.WorkflowRunner;
import org.restflow.metadata.ActorState;
import org.restflow.metadata.FileSystemMetadataManager;
import org.restflow.metadata.RunMetadata;
import org.restflow.util.Contract;
import org.restflow.util.StdoutRecorder;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;


public class RestFlowActor extends AbstractActor implements Cloneable {

	public static final String VARNAME_WORKFLOW_FILE = "restflow-file";
	public static final String VARNAME_WORKFLOW_NAME = "restflow-workflow";		
	public static final String VARNAME_WORKFLOW_BASE = "restflow-baseDir";		
	public static final String VARNAME_WORKFLOW_IMPORTMAP = "restflow-importMapOverride";
	public static final String VARNAME_WORKFLOW_ENABLE_TRACE = "restflow-enableTrace";
	public static final String VARNAME_WORKFLOW_DAEMON = "restflow-daemon";
	public static final String VARNAME_WORKFLOW_WORKSPACE = "restflow-workspace";
	public static final String VARNAME_WORKFLOW_RUNNAME = "restflow-runName";
	public static final String VARNAME_WORKFLOW_REPORTNAME ="restflow-reportName";
	public static final String VARNAME_WORKFLOW_GENERATE_DOT = "restflow-generateDot";	
		
	public synchronized RestFlowActor clone() throws CloneNotSupportedException {
		RestFlowActor theClone = (RestFlowActor) super.clone();
		return theClone;
	}	
	
	public void afterPropertiesSet() throws Exception {
		Contract.requires(_state == ActorFSM.CONSTRUCTED);
		super.afterPropertiesSet();
		_state = ActorFSM.PROPERTIES_SET;
	}
	
	public synchronized void configure() throws Exception {		
		Contract.requires(_state == ActorFSM.PROPERTIES_SET);
		_state = ActorFSM.CONFIGURED;
	}
	
	@Override
	public void initialize() throws Exception {
		Contract.requires(_state == ActorFSM.CONFIGURED || _state == ActorFSM.WRAPPED_UP);
		super.initialize();
		_state = ActorFSM.INITIALIZED;
	}

	@Override
	public synchronized void step() throws Exception {
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		super.step();

		getNextStepDirectory();
		
		Map<String,Object> inputValues = new HashMap<String,Object>();
		inputValues.putAll(_stateVariables);
		inputValues.putAll(_inputValues);
		_inputValues.putAll(_stateVariables);
		
		
		List<String> restflowArgs = new Vector<String>();
		restflowArgs.add("java");
		bindClasspathToArgs(restflowArgs);
		restflowArgs.add("org.restflow.RestFlow");		
		
		bindWorkflowFile(restflowArgs);
		bindWorkflowNameToArgs( resolveWorkflowName(), restflowArgs );
		
		//StdoutRecorder recorder = new StdoutRecorder(true);
		RunMetadata metadata;
		if (resolveBaseDirectory() != null) {
			File runDir = new File(resolveBaseDirectory() + "/1" );
			
			

			bindRunNameToArgs( runDir , restflowArgs );									
			bindBaseDirToArgs(resolveBaseDirectory(), restflowArgs);	
			bindResourceMapToArgs(restflowArgs);
			bindFlagsToArgs(restflowArgs);
			File genInputFile = writeInputsFile(inputValues);
			bindInputsToArgs(restflowArgs, genInputFile.getPath() );

			
			//String[] args = new String[restflowArgs.size()];  
			//restflowArgs.toArray( args );  
			//metadata = RestFlow.loadAndRunWorkflow( args );

			//RestFlowMainWrapper _wrappedRunner = new RestFlowMainWrapper(restflowArgs);
			//recorder.recordExecution(_wrappedRunner);
			//metadata = _wrappedRunner.getMetadata();			
		} else {
			WorkflowRunner.Builder wrb = new WorkflowRunner.Builder();
			wrb.inputBindings(inputValues).workflowDefinitionPath(resolveWorkflowFile()).workflowName(resolveWorkflowName());
			WorkflowRunner runner = wrb.build();
			runner.run();

			//StdoutRecorder.WrappedCode _wrappedRunner = new WrappedRunner(runner);
			//recorder.recordExecution(_wrappedRunner);

			metadata = runner.getRunMetaData();
		}

		ProcessBuilder pb = new ProcessBuilder();
		pb.command(restflowArgs);
		Process p = pb.start();
		p.waitFor();
		
		System.out.print(p.exitValue());
		metadata = FileSystemMetadataManager.restoreMetadata(resolveBaseDirectory() + "/1");
		
		if (metadata == null) {
			Map<String,Object> outputs = new HashMap();
			//TODO cannot use the stdout recorder here because it is not thread safe.
//			_outputValues.put("restflow-stdout", recorder.getStdoutRecording());
//			_outputValues.put("restflow-stderr", recorder.getStderrRecording());			
			return;
		}
		Map<String,Object> outputs = metadata.getOutputValues();
		_outputValues.putAll(metadata.getOutputValues());
		
		
		// update stored values for state variables
		for (String label : _stateVariables.keySet()) {
			_stateVariables.put(label, _outputValues.get(label));
		}		

		// update stored values for state variables
		ActorState state = metadata.getActorState();
		
		Map<String, Boolean> inputEnableMap = state.getInputEnableMap();
		if (inputEnableMap != null) {

			for (String label : inputEnableMap.keySet()) {

				Boolean inputEnable = inputEnableMap.get(label);
				if (inputEnable == true) {
					_actorStatus.enableInput(label);
				} else {
					_actorStatus.disableInput(label);
				}
			}
		}
		
		Map<String, Boolean> outputEnableMap = state.getOutputEnableMap();
		for (String label : outputEnableMap.keySet()) {
			
			if ( outputEnableMap.get(label) == true ) {
				_actorStatus.enableOutput(label);
			} else {
				_actorStatus.disableOutput(label);				
			}
		}		

		_state = ActorFSM.STEPPED;
	}
	
	public synchronized void wrapup() throws Exception {		
		Contract.requires(_state == ActorFSM.INITIALIZED || _state == ActorFSM.STEPPED);
		super.wrapup();
		_state = ActorFSM.WRAPPED_UP;
	}
	
	public synchronized void dispose() throws Exception {
		Contract.requires(_state == ActorFSM.WRAPPED_UP);
		super.dispose();
		_state = ActorFSM.DISPOSED;
	}	
	protected void bindClasspathToArgs ( List<String> restflowArgs ) {
		String cp = System.getProperty("java.class.path");
		restflowArgs.add("-cp");
		restflowArgs.add(cp);
	}
	
	protected void bindWorkflowFile ( List<String> restflowArgs ) {
		String workflowFile = resolveWorkflowFile();
		
		restflowArgs.add("-f"); restflowArgs.add(workflowFile);		
	}
	
	protected void bindFlagsToArgs ( List<String> restflowArgs ) {
		Boolean enableTrace = resolveEnableTrace();
		Boolean daemon = resolveDaemon();
		Boolean dot = resolveGenerateDot();
		
		if (enableTrace != null && enableTrace ) {
			restflowArgs.add("-t");
		}
		
		if (daemon !=null && daemon ) {
			restflowArgs.add("-daemon");
		}
		
		if (dot !=null && dot ) {
			restflowArgs.add("-to-dot");
		}		
		
	}


	static protected void bindWorkflowNameToArgs ( String workflowName, List<String> restflowArgs ) {
		
		restflowArgs.add("-w"); restflowArgs.add(workflowName);		
	}
	
	static protected void bindRunNameToArgs ( File dir, List<String> restflowArgs ) {
		restflowArgs.add("-run"); restflowArgs.add( dir.getName() );		
	}

	static protected void bindBaseDirToArgs ( String baseDir, List<String> restflowArgs ) {
		if (baseDir == null ) return;
		
		restflowArgs.add("-base"); restflowArgs.add(baseDir);		
	}

	
	private synchronized String resolveWorkflowFile() {
		String workflowFile;
		workflowFile = (String)_inputValues.get(VARNAME_WORKFLOW_FILE);	
		if (workflowFile == null) {
			workflowFile = (String)_constants.get(VARNAME_WORKFLOW_FILE);
		}
		return workflowFile;
	}
	
	
	protected synchronized void bindResourceMapToArgs( List<String> restflowArgs  ) {
		List<String> resourceList = new Vector<String>();
		Map<String, String> parentResourceMap = _workflowContext.getImportMappings();
		
		Map<String, String> thisResourceMap = new HashMap<String,String>();
		thisResourceMap.putAll(parentResourceMap);
		
		Map<String,String> importMapOverrides = (Map<String,String>)_constants.get(VARNAME_WORKFLOW_IMPORTMAP);
		if ( importMapOverrides == null) {
			importMapOverrides = (Map<String,String>)_inputValues.get(VARNAME_WORKFLOW_IMPORTMAP);
		}
		
		if (importMapOverrides != null)
			thisResourceMap.putAll( importMapOverrides );
		
		for (String resourceName : thisResourceMap.keySet()) {
			if (resourceName == "workspace") continue;
			String resourcePath = thisResourceMap.get(resourceName);
			resourceList.add("-import-map");
			resourceList.add( resourceName + "="+ resourcePath);			
		}

		restflowArgs.addAll(resourceList);
	}

	
	/**
	 * Generates an input file in yaml and passes it through the args
	 * of the command line to RestFlow.  Writes the input file to the
	 * actor's step directory.
	 * @return
	 * @throws IOException
	 */
	static protected synchronized void bindInputsToArgs (List<String> restflowArgs, String inputFilePath) throws IOException {
		restflowArgs.add("-infile");
		restflowArgs.add( inputFilePath );		
	}

	private File writeInputsFile(Map<String, Object> inputValues)
			throws IOException {
		Yaml yaml = new Yaml(new MyRepresenter());
		String inputValuesYamlStr = yaml.dump( inputValues);
		File genInputFile = new File(_stepDirectory + "/" + "inputs.yaml");
		FileUtils.writeStringToFile(genInputFile,inputValuesYamlStr);
		return genInputFile;
	}
	
	
	

	private synchronized Boolean resolveGenerateDot() {
		Boolean dot;
		dot = (Boolean)_inputValues.get(VARNAME_WORKFLOW_GENERATE_DOT);	
		if (dot == null) {
			dot = (Boolean)_constants.get(VARNAME_WORKFLOW_GENERATE_DOT);
		}
		return dot;
	}

	private synchronized Boolean resolveDaemon() {
		Boolean daemon;
		daemon = (Boolean)_inputValues.get(VARNAME_WORKFLOW_DAEMON);	
		if (daemon == null) {
			daemon = (Boolean)_constants.get(VARNAME_WORKFLOW_DAEMON);
		}
		return daemon;
	}

	private synchronized Boolean resolveEnableTrace() {
		Boolean enableTrace;
		enableTrace = (Boolean)_inputValues.get(VARNAME_WORKFLOW_ENABLE_TRACE);	
		if (enableTrace == null) {
			enableTrace = (Boolean)_constants.get(VARNAME_WORKFLOW_ENABLE_TRACE);
		}
		return enableTrace;
	}

	
	private synchronized String resolveWorkflowName() {
		String workflowName;
		workflowName = (String)_inputValues.get(VARNAME_WORKFLOW_NAME);	
		if (workflowName == null) {
			workflowName = (String)_constants.get(VARNAME_WORKFLOW_NAME);
		}
		return workflowName;
	}

	private synchronized String resolveBaseDirectory() {
		String baseDir = (String)_inputValues.get(VARNAME_WORKFLOW_BASE);

		if (baseDir == null) {
			baseDir = (String)_constants.get(VARNAME_WORKFLOW_BASE);
		}
		
		if (baseDir == null) {
			if (_stepDirectory == null) {
				baseDir = null;
			} else {
				baseDir = _stepDirectory.getAbsolutePath();
			}
		} else if ( baseDir.equals("PARENT") ) {
			baseDir = _workflowContext.getBaseDirectoryPath();
		}
		return baseDir;
	}
	
		
    static public class MyRepresenter extends Representer {
        public MyRepresenter() {
            this.representers.put(File.class, new FileRepresenter());
        }

        public class FileRepresenter implements Represent {
            public Node representData(Object data) {
                File file = (File) data;
                Node scalar = representScalar(new Tag("!!java.io.File"), file.getAbsolutePath());
                return scalar;
            }
        }
    }


	static public class WrappedRunner implements StdoutRecorder.WrappedCode {

		private final WorkflowRunner _runner;
		public WrappedRunner(WorkflowRunner runner) {
			super();
			_runner = runner;
		}

		public void execute() throws Exception {
			_runner.run();
		}
	}
	
	static public class RestFlowMainWrapper implements StdoutRecorder.WrappedCode {

		private final List<String> restflowArgs;
		private RunMetadata metadata;
		
		public RestFlowMainWrapper(List<String> restflowArgs) {
			super();
			this.restflowArgs = restflowArgs;
		}

		public void execute() throws Exception {
			String[] args = new String[restflowArgs.size()];  
			restflowArgs.toArray( args );  
			metadata = RestFlow.loadAndRunWorkflow( args );
		}

		public RunMetadata getMetadata() {
			return metadata;
		}
		
		
		
	}    
	
	
}
