package org.restflow.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.TeeOutputStream;

public class StdoutRecorder {
	public static final String STDOUT_FILE = "stdout.txt";
	public static final String STDERR_FILE = "stderr.txt";
	
	private PrintStream stdout;
	private PrintStream stderr;
	private boolean _suppressTerminalOutput;
	private StreamStorage _recording;

	private PrintStream teeStdout;
	private PrintStream teeStderr;
	
	public interface StreamStorage {
		PrintStream getStdout();
		PrintStream getStderr();
		String getStdoutRecording() throws Exception;
		String getStderrRecording() throws Exception;
		void done();
	}
	
	static public class InMemoryRecording implements StreamStorage {
		ByteArrayOutputStream _capturedStdoutBuffer;
		ByteArrayOutputStream _capturedStderrBuffer;
		PrintStream _stdout;
		PrintStream _stderr;
		
		public InMemoryRecording() {
			_capturedStdoutBuffer = new ByteArrayOutputStream();
			_capturedStderrBuffer = new ByteArrayOutputStream();
			_stdout = new PrintStream(_capturedStdoutBuffer);
			_stderr = new PrintStream (_capturedStderrBuffer);
		}
		
		@Override
		public PrintStream getStderr() {
			return _stderr;
		}
		
		@Override
		public PrintStream getStdout() {
			return _stdout;
		}

		@Override
		public String getStderrRecording() throws Exception {
			return _capturedStderrBuffer.toString();
		}
		
		@Override
		public String getStdoutRecording() {
			return _capturedStdoutBuffer.toString();
		}
		
		@Override
		public void done() {};
	}
	
	static public class FileSystemRecording implements StreamStorage {
		FileOutputStream _capturedStdoutBuffer;
		FileOutputStream _capturedStderrBuffer;
		PrintStream _stdout;
		PrintStream _stderr;
		String _stdoutFilePath;
		String _stderrFilePath;
		
		public FileSystemRecording(String path) throws FileNotFoundException {
			_stdoutFilePath = path + File.separator + STDOUT_FILE;
			_stderrFilePath = path + File.separator + STDERR_FILE;
			_capturedStdoutBuffer = new FileOutputStream(_stdoutFilePath);
			_capturedStderrBuffer = new FileOutputStream(_stderrFilePath);
			_stdout = new PrintStream(_capturedStdoutBuffer);
			_stderr = new PrintStream (_capturedStderrBuffer);
		}
		
		@Override
		public PrintStream getStderr() {
			return _stderr;
		}
		
		@Override
		public PrintStream getStdout() {
			return _stdout;
		}
		
		@Override
		public String getStdoutRecording() throws Exception {
			FileInputStream fileInputStream = new FileInputStream(_stdoutFilePath);
			try {
				String val = IOUtils.toString(fileInputStream,"UTF-8");
				return val;
			} finally {
				IOUtils.closeQuietly(fileInputStream);
			}
		}

		@Override
		public String getStderrRecording() throws Exception {
			FileInputStream fileInputStream = new FileInputStream(_stderrFilePath);
			try {
				String val = IOUtils.toString(fileInputStream,"UTF-8");
				return val;
			} finally {
				IOUtils.closeQuietly(fileInputStream);
			}
		}
		
		@Override
		public void done() {
			IOUtils.closeQuietly(_capturedStdoutBuffer);
			IOUtils.closeQuietly(_capturedStderrBuffer);			
		};
	}
	
	/**
	 * @param suppressTerminalOutput  If 'true' record the stdout silently; if 'false' continue streaming to stdout while recording.
	 */
	public StdoutRecorder(boolean suppressTerminalOutput) {
		super();
		_suppressTerminalOutput = suppressTerminalOutput;		
		_recording = new InMemoryRecording();
	}

	/**
	 * @param suppressTerminalOutput  If 'true' record the stdout silently; if 'false' continue streaming to stdout while recording.
	 */
	public StdoutRecorder(boolean suppressTerminalOutput, StreamStorage storage) {
		super();
		_suppressTerminalOutput = suppressTerminalOutput;		
		_recording = storage;
	}
	
	public StdoutRecorder(WrappedCode wrappedCode) throws Exception {
		super();
		_suppressTerminalOutput = true;
		_recording = new InMemoryRecording();
		recordExecution(wrappedCode);
	}
	
	/**
	 * @param wrappedCode  Execute and record the stdout,stderr of the code in the execute method of the WrappedCode implementation.
	 * @throws Exception
	 */
	public void recordExecution (WrappedCode wrappedCode) throws Exception {
		try {
			redirect();
			wrappedCode.execute();
		} catch (Exception e) {
			e.printStackTrace(stderr);
		} finally {
			restore();
			_recording.done();
		}
	}
	
	private void redirect() {

		stdout = System.out;
		stderr = System.err;
		
		teeStdout = null;
		teeStderr = null;
		
		CloseShieldOutputStream stdoutProxy = new CloseShieldOutputStream(stdout);
		CloseShieldOutputStream stderrProxy = new CloseShieldOutputStream(stderr);
		
		OutputStream teedStdoutStream;
		OutputStream teedStderrStream;			
		if (_suppressTerminalOutput) {
			teedStdoutStream = _recording.getStdout();
			teedStderrStream = _recording.getStderr();					
		} else {
			teedStdoutStream = new TeeOutputStream(stdoutProxy, _recording.getStdout() );				
			teedStderrStream = new TeeOutputStream(stderrProxy, _recording.getStderr() );								
		}
		teeStdout = new PrintStream( teedStdoutStream );
		teeStderr = new PrintStream( teedStderrStream );			
		
		System.setOut( teeStdout ) ;
		System.setErr( teeStderr ) ;
		
	}
	
	private void restore() {
		System.setOut( stdout ) ;	
		System.setErr( stderr ) ;	
		
		teeStdout.flush();
		teeStdout.close();
		
		teeStderr.flush();
		teeStderr.close();
	}
	
	/**
	 * @author scottm
	 * Implement the 'execute' method to have code executed and stdout of the code recorded.
	 * 
	 */
	public interface WrappedCode {
		public void execute() throws Exception;
	}
	
	public String getStdoutRecording() throws Exception {
		return _recording.getStdoutRecording();
	}
	
	public String getStderrRecording() throws Exception {
		return _recording.getStderrRecording();
	}
	
	
}
