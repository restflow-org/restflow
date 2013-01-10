package org.restflow.beans;

import java.io.IOException;


public class TextPrinter {

	private String _inText;
	
	public void setInText(String inText) {
		_inText = inText;
	}

	public void step() throws IOException {		
		System.out.println(_inText);
	}
}