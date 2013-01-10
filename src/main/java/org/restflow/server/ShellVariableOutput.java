package org.restflow.server;

import java.io.PrintStream;
import java.util.HashMap;

public class ShellVariableOutput {

	public enum ShellType { BASH, TCSH, WIN_CMD	}
	
	public static ShellVariableOutput variableOut = new ShellVariableOutput(ShellType.BASH);
	
	public ShellVariableOutput(ShellType t) {
		shell = t;
	}

	public void setShell(ShellType t) {
		shell = t;
	}

	public void addInfo(String key, String value) {
		data.put(key, value);
	}

	public void outPut(PrintStream o) {
		for (String k : data.keySet()) {
			outPutOne(k, data.get(k), o);
		}
	}

	protected void outPutOne(String k, String v, PrintStream o) {
		switch (shell) {
		case BASH:
			o.println("export " + k + "=" + v);
			break;
		case TCSH:
			o.println("setenv " + k + " " + v);
			break;
		case WIN_CMD:
			o.println("SET " + k + "=" + v);
			break;
		};
	}
	ShellType shell;
	HashMap<String, String> data = new HashMap<String, String>();
}