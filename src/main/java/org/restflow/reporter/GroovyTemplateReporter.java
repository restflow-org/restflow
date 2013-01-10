package org.restflow.reporter;

import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

public class GroovyTemplateReporter extends BaseReporter {

	String _reportTemplateString;
	

	public GroovyTemplateReporter() {
		super();
	}
	
	public GroovyTemplateReporter(String template) {
		super();
		_reportTemplateString = template;
	}
	
	public void setTemplate(String template) {
		_reportTemplateString = template;
	}

	public String getTemplate() {
		return _reportTemplateString;
	}	

	@Override public String getReport( ) throws Exception {
		
		try {
			
			// create a Groovy template engine and create a template from ther report template string
			SimpleTemplateEngine engine = new SimpleTemplateEngine();
			Template groovyTextTemplate = engine.createTemplate(_reportTemplateString);			
			
			// expand the report template with values from the report model
			Writable templateWritable = groovyTextTemplate.make(_reportModel);
			String reportOutput = templateWritable.toString();
			
			// replace newlines inserted into the report by Groovy with system-specific line separators
		    reportOutput = reportOutput.replace(System.getProperty("line.separator"), "\n");
		    reportOutput = reportOutput.replace("\n", System.getProperty("line.separator"));

		    // return the report output
		    return reportOutput;
   	      	
		} catch (Exception e) {
			_reportGenerationErrors.add(e.getMessage());
			throw e;
		} 		
	}
}
