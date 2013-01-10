package org.restflow.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class InflowToOutflowsMap extends HashMap<Inflow,List<Outflow>> {

	public InflowToOutflowsMap() {
		super();
	}
	
	public InflowToOutflowsMap(InflowToOutflowsMap original) {
		super();
		for (Map.Entry<Inflow, List<Outflow>> entry : original.entrySet()) {
			List<Outflow> outflowList = new ArrayList<Outflow>(entry.getValue());
			this.put(entry.getKey(), outflowList);
		}
	}
}