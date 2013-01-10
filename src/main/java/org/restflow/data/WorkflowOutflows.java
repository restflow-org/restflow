package org.restflow.data;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.restflow.actors.Workflow;
import org.restflow.util.PortableIO;


public class WorkflowOutflows {
	
	private Map<String,List<Outflow>> _expressionToOutflowsMap;
	private Workflow _workflow;
	
	
	public WorkflowOutflows(Workflow workflow) {
		
		super();
		
		synchronized(this) {
			_workflow = workflow;
			_expressionToOutflowsMap = new Hashtable<String, List<Outflow>>();
		}
	}
	
	
	public void addOutflow(String expression, Outflow outflow) {
	
		List<Outflow> outflows = _expressionToOutflowsMap.get(expression);
		
		if (outflows == null) {
			outflows = new Vector<Outflow>();
			_expressionToOutflowsMap.put(expression, outflows);
		}
		
		outflows.add(outflow);
	}

	public List<Outflow> get(String expression ){
		return _expressionToOutflowsMap.get(expression);
	}

	
	public void assertNoUriExtensionOfAnyOther() throws Exception {
		// make sure that no outflow binding expression is an extension of any other
		for (String outflowExpressionOne : _expressionToOutflowsMap.keySet()) {

			// make sure that no outflow binding expression is an extension of any other
			for (String outflowExpressionTwo : _expressionToOutflowsMap.keySet()) {
				
				if (outflowExpressionOne != outflowExpressionTwo && 
					outflowExpressionOne.startsWith(outflowExpressionTwo)) {

					int lengthTwo = outflowExpressionTwo.length();
					char c = outflowExpressionOne.charAt(lengthTwo);
					if (c == '/') {						
						throw new Exception(
								"  An outflow cannot write inside a collection written by another outflow: " +  PortableIO.EOL +
								"  Expression 1 : " + outflowExpressionOne +  PortableIO.EOL +
								"  Expression 2 : " + outflowExpressionTwo +  PortableIO.EOL +
								"  Workflow     : " + _workflow
						);
					}
				}
			}
		}
	}
	
	public synchronized List<Outflow> findOutflowsStartingWithExpression(String expression ) {
		
		List<Outflow> outflows = null;
		
		for (Map.Entry<String,List<Outflow>> entry : _expressionToOutflowsMap.entrySet()) {
			
			String outflowExpression = entry.getKey();
			
			if (expression.startsWith(outflowExpression)) {
				outflows = entry.getValue();
				break;
			}
		}
		
		return outflows;
	}
}