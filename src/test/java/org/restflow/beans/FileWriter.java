package org.restflow.beans;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.restflow.actors.ActorStatus;

public class FileWriter {

	private File _outFile;
	private ActorStatus _status;
	private String _inText;
	
	public void setStatus(ActorStatus actorStatus) {
		_status = actorStatus;
	}
	
	public void setInText(Object text) {
		_inText = text.toString();
	}

	public void step() throws IOException {
		_outFile = new File(_status.getStepDirectory(), "file.txt");
		FileUtils.writeStringToFile(_outFile, _inText);
	}
	
	public File getOutFile() {
		return _outFile;
	}
}