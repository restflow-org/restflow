package org.restflow.reporter;

import java.util.HashMap;
import java.util.Map;

import org.restflow.metadata.Trace;
import org.restflow.metadata.WorkflowModel;


public abstract class GraphvizReporter extends BaseReporter {

	protected final static String EOL = System.getProperty("line.separator");
	
	public static class WorkflowGraphReporter extends GraphvizReporter {
	
		public String getReport() throws Exception {
			
			Trace trace = (Trace)_reportModel.get("trace");
			
			Dot dot = new Dot();
			dot.begin();
			
			Long topNodeID = trace.identifyTopNode();
			
			for (WorkflowModel.Node n : trace.getNodes(topNodeID)) {
				if (n.hasChildren) {
					dot.node(n.localName, "ellipse", 2);
				} else {
					dot.node(n.localName, "ellipse");
				}
			}
			
			for (WorkflowModel.Outflow o : trace.getOutflows(topNodeID)) { 
				dot.node(o.uriTemplate, "box");
				dot.edge(o.nodeLocalName, o.uriTemplate, o.portName);
			}
	
			for (WorkflowModel.Channel c : trace.getChannels(topNodeID)) {
				dot.edge(c.sendingPortUriTemplate, c.receivingNodeLocalName, c.receivingPortName);
			}
			
			dot.end();
			
			return dot.toString();
		}
	}
	
	public static class Dot {
		
		private StringBuilder _buffer = new StringBuilder();
		private int nodeCount = 0;
		private Map<String,String> nodeNameToIdMap = new HashMap<String,String>();
	
		public  void begin() {
			_buffer	.append(	"digraph Workflow {" + EOL	);
		}
		
		public  void node(String name, String shape) {
			node(name, shape, 1);
		}

		public  void node(String name, String shape, int peripheries) {
			
			String id = "node" + ++nodeCount;
			nodeNameToIdMap.put(name, id);
			
			_buffer	.append(	id			)
					.append(	" [label="		)
					.append(	dq(name)			)
					.append(	",shape="		)
					.append(	shape			)
					.append(	",peripheries="	)
					.append(	peripheries		)
					.append(	"];" + EOL		);
		}

		
		public void edge(String fromNode, String toNode, String edgeLabel) {
			
			String fromId 	= nodeNameToIdMap.get(fromNode);
			String toId 	= nodeNameToIdMap.get(toNode);
			
			_buffer .append(	fromId			)
					.append(	" -> "			)
					.append(	toId			)
					.append(	" [label="		)
					.append(	dq(edgeLabel)	)
					.append(	"];" + EOL		);
		}
		
		public  void end() {
			_buffer	.append(	"}" + EOL		);
		}

		private String dq(String text) {
			return "\"" + text + "\"";
		}
		
		public String toString() {
			return _buffer.toString();
		}
	}
}

