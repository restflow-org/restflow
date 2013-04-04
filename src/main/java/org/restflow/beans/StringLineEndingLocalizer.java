package org.restflow.beans;

import java.io.IOException;

import org.restflow.util.PortableIO;

public class StringLineEndingLocalizer {

	private String _original;

	public StringLineEndingLocalizer() {
	}

	public void setOriginal(String s1) {
		_original = s1;
	}

	public String getFinal() throws IOException {
		return PortableIO.localizeLineEndingsInString(_original);
	}
}