package org.restflow.beans;

import java.util.Map;

public class ContextRenderer {
	
	public String runPath;
	public String basePath;
	public Map<String,String >importMap;
	
	public void step() {
        System.out.println(runPath);
        System.out.println(basePath);
        System.out.println(importMap);
	}
}