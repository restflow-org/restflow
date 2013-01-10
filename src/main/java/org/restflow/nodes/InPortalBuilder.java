package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;

public class InPortalBuilder {
	
	private String 						_name = "";
	private Map<String, String> 		_outflows = new HashMap<String, String>();
	
	public InPortalBuilder name(String name) {
		_name = name;
		return this;
	}

	public InPortalBuilder outflow(String label, String outflowExpression) {
		_outflows.put(label, outflowExpression);
		return this;
	}

	public InPortal build() throws Exception {
		
		InPortal portal = new InPortal();
		
		portal.setName(_name);
		portal.setOutflows(_outflows);
		 						
		return portal;
	}
}
