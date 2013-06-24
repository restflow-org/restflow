package org.restflow.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.restflow.actors.Actor;
import org.restflow.reporter.Reporter;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;
import org.yaml.snakeyaml.Yaml;


public class FileSystemMetadataManager extends BaseMetadataManager {

	private static final String YAML_EXTENSION = ".yaml";	
	public static final String METADATA_DIR = File.separator + "_metadata" + File.separator;	
	private static final String CONTROL_FILE = METADATA_DIR + "control" + YAML_EXTENSION;
	private static final String PRODUCTS_FILE = METADATA_DIR + "products"  + YAML_EXTENSION ;	
	private static final String INPUTS_FILE = METADATA_DIR + "inputs" + YAML_EXTENSION;			
	private static final String OUTPUTS_FILE = METADATA_DIR + "outputs" + YAML_EXTENSION;	
	public static final String REPORTS_DEFINITION_FILE = METADATA_DIR + "report-defs" + YAML_EXTENSION;				
	public static final String ACTOR_STATE_FILE = METADATA_DIR + "endstate" + YAML_EXTENSION;

	private Map<String, PrintStream> _logStreams = new HashMap<String,PrintStream>();
	private PrintStream _productsFileStream;
	
	@Override	
	public String getRunDirectory() {
		return _runMetadata.getRunDirectory();
	}

	public String getMetadataDirectory() {
		return getRunDirectory() + METADATA_DIR;
	}
	
	@Override	
	public void setRunDirectory(String dir) throws Exception {
		_runMetadata.setRunDirectory(dir);
		FileUtils.forceMkdir(new File(dir + METADATA_DIR) );
	}

	@Override
	public void storeWorkflowInputs(Map<String,Object> inputBindings) throws Exception {
		
		Yaml yaml = new Yaml();
		yaml.setBeanAccess(org.yaml.snakeyaml.introspector.BeanAccess.FIELD);
		FileUtils.writeStringToFile(new File(_runMetadata.getRunDirectory() ,
				INPUTS_FILE), yaml.dump(inputBindings), "UTF-8");
	}	
	
	@Override
	public void storeProcessInfo() throws Exception {
		super.storeProcessInfo();
		Yaml yaml = new Yaml();
		FileUtils.writeStringToFile(new File(_runMetadata.getRunDirectory() ,
				CONTROL_FILE), yaml.dump( _runMetadata.getProcessProperties()), "UTF-8");
	}
	
	public String getProductsAsString() throws IOException {
		return PortableIO.readTextFileOnFilesystem(
				_runMetadata.getRunDirectory() + File.separator + PRODUCTS_FILE);
	}

	@Override
	public void storeReportDefinitions(Map <String,Reporter> reports) throws IOException {
		
		super.storeReportDefinitions(reports);

		if (reports.size() > 0) {
			Yaml yaml = new Yaml();
			FileUtils.writeStringToFile( new File(_runMetadata.getRunDirectory() + REPORTS_DEFINITION_FILE), yaml.dump(reports), "UTF-8");
		}
	}	
	

	@Override
	public void storeWorkflowOutputs(Actor actor, Map<String, Object> outputBindings) throws Exception {
		super.storeWorkflowOutputs(actor, outputBindings);
		
		Yaml yaml = new Yaml();
		yaml.setBeanAccess(org.yaml.snakeyaml.introspector.BeanAccess.FIELD);		
		FileUtils.writeStringToFile(new File(_runMetadata.getRunDirectory() ,
				OUTPUTS_FILE), yaml.dump(_runMetadata.getOutputValues() ));
		
		FileUtils.writeStringToFile(new File(_runMetadata.getRunDirectory() ,
				ACTOR_STATE_FILE), yaml.dump( _runMetadata.getActorState() ));
		
	}	
	
	@Override
	public StdoutRecorder buildStdoutRecorder(boolean suppressTerminalOutput) throws FileNotFoundException {
		if (_stdoutRecorder != null) return _stdoutRecorder;
		
		StdoutRecorder.FileSystemRecording recording = new StdoutRecorder.FileSystemRecording(
				_runMetadata.getRunDirectory() + File.separator + METADATA_DIR);
		
		_stdoutRecorder = new StdoutRecorder( suppressTerminalOutput, recording);
		
		return _stdoutRecorder;	
	}

	public synchronized void writeToLog(String logName, String message) {
		
		PrintStream logStream = _logStreams.get(logName);
		
		if (logStream == null) {
			String path = _runMetadata.getRunDirectory() + File.separator + METADATA_DIR + logName;
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(path);
			} catch (FileNotFoundException e) {
			}
			logStream =  new PrintStream(fileOutputStream);
			_logStreams.put(logName, logStream);
		}
		
		if (logStream != null) {
			logStream.append(message);
		}
	}
	
	public synchronized void writeToProductsFile(String message) {		
		_productsFileStream.append(message);
	}
	
	/**
	 * Allows an active instance of the MetaDataManager to return a copy of the current metadata.
	 * 
	 * @return
	 * @throws Exception
	 */
	@Override
	public RunMetadata getRunMetadata(TraceRecorder recorder) throws Exception {
		return restoreMetadata(_runMetadata.getRunDirectory());
	}

	static public RunMetadata restoreMetadata(String runDirectoryPath) throws Exception {
		
		RunMetadata metadata = new RunMetadata();
		metadata.setRunDirectory(runDirectoryPath);
		File runDirectory = new File(runDirectoryPath);
		
		try {
			metadata.setMetadataStorageProperty("runName", runDirectory.getName());
			metadata.setMetadataStorageProperty("lastModified", runDirectory.lastModified());
		} catch (Exception e ) {
			metadata.getRestoreErrors().add( e.getMessage() );			
		}
			
		Yaml rdr = new Yaml();

		File report = new File (runDirectoryPath + REPORTS_DEFINITION_FILE);
		File control = new File (runDirectoryPath + CONTROL_FILE);		
		File productsFile = new File( runDirectoryPath + PRODUCTS_FILE );
		File stdout = new File(runDirectoryPath + File.separator + METADATA_DIR + StdoutRecorder.STDOUT_FILE );
		File stderr = new File(runDirectoryPath + File.separator + METADATA_DIR + StdoutRecorder.STDOUT_FILE );
		File inputFile = new File(runDirectoryPath + INPUTS_FILE );
		File outputFile = new File(runDirectoryPath + OUTPUTS_FILE );
		File stateFile = new File (runDirectoryPath + ACTOR_STATE_FILE );		
		
		
		try {
			metadata.setReporters( (HashMap<String,Reporter>)rdr.load(new FileInputStream(report)) );
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}
		
		try {	
			metadata.setProcessProperties((Map<String,Object>)rdr.load(FileUtils.readFileToString(control)));
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}

		try {
			String productsYamlString = PortableIO.readTextFile(productsFile);
			metadata.setProductsYamlString(productsYamlString);
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}
		
		try {
			metadata.setStdoutText( FileUtils.readFileToString(stdout) );
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}
		try {
			metadata.setStderr(FileUtils.readFileToString(stderr) );
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}
		try {
			String metadataDirectory = runDirectoryPath + METADATA_DIR;
			Trace trace = new Trace(metadataDirectory);
			metadata.setTrace(trace);
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}
		
		Yaml reader = new Yaml();
		try { 
			if (inputFile.exists()) {
				Map<String,Object> inputs = new HashMap<String,Object>();		
				String inputFileContents =FileUtils.readFileToString(inputFile);
				Map<String,Object> inputMap = (Map<String,Object>)reader.load(inputFileContents);
				if ( inputMap != null) {
					inputs.putAll( inputMap );
				}
				metadata.setInputValues(inputs);
			}
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}
		
		try {
			Map<String,Object> outputValues = new HashMap<String,Object>();
			outputValues = (Map<String,Object>) reader.load( FileUtils.readFileToString(outputFile ));
	 		
			metadata.setOutputValues(outputValues);
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}
		
		try {
			ActorState state = new ActorState();
			state = (ActorState) reader.load( FileUtils.readFileToString( stateFile ));
	 		
			metadata.setActorState(state);
		} catch (Exception e) {
			metadata.getRestoreErrors().add( e.getMessage() );
		}

		
		return metadata;
	}

	public void createProductsFileStream() throws Exception {
		
		if (_productsFileStream == null) {
			String path = 	_runMetadata.getRunDirectory() + 
							File.separator + 
							PRODUCTS_FILE;
			
			FileOutputStream fileOutputStream;
			
			try {
				fileOutputStream = new FileOutputStream(path);
			} catch (FileNotFoundException e) {
				throw new Exception("Error creating products file at " + path);
			}
			
			_productsFileStream =  new PrintStream(fileOutputStream);
		}
	}
}
