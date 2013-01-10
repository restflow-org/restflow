package org.restflow.reporter;

import java.sql.SQLException;

import org.restflow.metadata.Trace;


public class TraceReporter extends BaseReporter {
	
	static private final String EOL = System.getProperty("line.separator");		
	
	@Override
	public String getReport() throws Exception {
		Trace trace = (Trace)_reportModel.get("trace");
		return getReport(trace);
	}
	
	static public String getReport(Trace trace) throws SQLException {
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("*** Node step counts ***" + EOL);
		buffer.append(trace.getNodeStepCountsYaml());
		
		buffer.append("*** Published resources ***" + EOL);
		buffer.append(trace.getResourcesYaml());
		
		return buffer.toString();
	}
}
