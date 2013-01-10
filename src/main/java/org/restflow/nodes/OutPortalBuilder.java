package org.restflow.nodes;

import java.util.HashMap;
import java.util.Map;

public class OutPortalBuilder {
	
	private String 						_name = "";
	private Map<String, Object> 		_inflows = new HashMap<String, Object>();
	
	public OutPortalBuilder name(String name) {
		_name = name;
		return this;
	}

	public OutPortalBuilder inflow(String label, String expression) {
		_inflows.put(label, expression);
		return this;
	}

	public OutPortal build() throws Exception {
		
		OutPortal portal = new OutPortal();
		
		portal.setName(_name);
		portal.setInflows(_inflows);
		 
		return portal;
	}
}
