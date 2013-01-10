package org.restflow.beans;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.restflow.actors.ActorStatus;
import org.restflow.util.PortableIO;


public class FileAppender {

	private File _inFile;
	private File _outFile;
	private ActorStatus _status;
	private String _textToAppend;
	
	public void setStatus(ActorStatus actorStatus) {
		_status = actorStatus;
	}
	
	public void setInFile(File inFile) {
		_inFile = inFile;
	}

	public void setTextToAppend(String text) {
		_textToAppend = text;
	}
	
	public File getOutFile() {
		return _outFile;
	}

	public void step() throws IOException {		
		String inFileText = PortableIO.readTextFile(_inFile);
		_outFile = new File(_status.getStepDirectory(), "outFile.txt");
		FileUtils.writeStringToFile(_outFile,  inFileText + _textToAppend);
	}
	
}