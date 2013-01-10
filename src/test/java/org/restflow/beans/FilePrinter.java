package org.restflow.beans;

import java.io.File;
import java.io.IOException;

import org.restflow.util.PortableIO;


public class FilePrinter {
	
	private File _inFile;
	
	public void setInFile(File inFile) {
		_inFile = inFile;
	}

	public void step() throws IOException {
		String fileContents = PortableIO.readTextFile(_inFile);
		System.out.print(fileContents);
	}
}
