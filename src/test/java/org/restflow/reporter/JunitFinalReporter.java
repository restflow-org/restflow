package org.restflow.reporter;

import org.restflow.metadata.Trace;
import org.restflow.reporter.BaseReporter;
import org.restflow.util.PortableIO;


public class JunitFinalReporter extends BaseReporter {
	
	static String EOL = PortableIO.EOL;
	
	public String getReport() throws Exception {
		
		StringBuilder buffer = new StringBuilder();
		
		buffer.append("-------------------- Terminal output ----------------------"	+ EOL)
			  .append(_reportModel.get("stdout") 									+ EOL)
			  .append("--------------------- Error messages ----------------------" + EOL)
			  .append(_reportModel.get("stderr") 									+ EOL);

		Trace trace = (Trace)_reportModel.get("trace");	

		buffer.append("------------------------- Trace ---------------------------" + EOL)
		      .append("*** Node firing counts ***" 									+ EOL)
			  .append(trace.getNodeStepCountsYaml()										+ EOL)
			  .append("*** Published resources ***" 								+ EOL)
			  .append(trace.getResourcesYaml()									+ EOL)
			  .append("*** Workflow graph ***" 										+ EOL)
			  .append( trace.getWorkflowGraphProlog() 									+ EOL)
			  .append("*** Workflow run events ***" 								+ EOL)
			  .append(trace.getPortEventsProlog()	 								+ EOL);
		
		return buffer.toString();
	}	
}
