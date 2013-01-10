package org.restflow.beans;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrintStreamMessageWriter {

	private Object _message;
	private ByteArrayOutputStream _bufferedStream;
	private PrintStream _printStream = null;
	private boolean _outputImmediately = true;
	private boolean _outputAtWrapup = false;
	private String _name = null;
	
	public void initialize() {
		
		// initialize buffer only if currently null so that buffer contents
		// maintained across multiple subworkflow invocations
		if (_bufferedStream == null) {
			_bufferedStream = new ByteArrayOutputStream();
			_printStream = new PrintStream(_bufferedStream);
		}
	}
	
	public void setOutputImmediately(boolean outputImmediately) {
		_outputImmediately = outputImmediately;
	}
	
	public void setOutputAtWrapup(boolean outputAtWrapup) {
		_outputAtWrapup = outputAtWrapup;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public void setPrintStream(PrintStream printStream) {
		_printStream = printStream;
	}
	
	public PrintStream getPrintStream() {
		return _printStream;
	}

	public ByteArrayOutputStream getBufferedStream() {
		return _bufferedStream;
	}
	
	public void setBufferedStream(ByteArrayOutputStream s) {
		_bufferedStream = s;
	}
	
	public void renderMessage() {
		
		_printStream.println(_message);

		if (_outputImmediately) {
			System.out.println(_message);
		}
	}

	public void setMessage(Object message) {
		_message = message;
	}
	
	public void dispose() {
		if (_outputAtWrapup && _bufferedStream != null) {
			if (_name != null) {
				System.out.println("*** " + _name + " ***");
			}
			System.out.print(_bufferedStream.toString());
		}
	}
	
	public String toString() {
		return _bufferedStream.toString();	
	}
}