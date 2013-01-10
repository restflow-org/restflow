package org.restflow.nodes;

import org.restflow.WorkflowContext;
import org.restflow.data.FileProtocol;
import org.restflow.data.Protocol;
import org.restflow.data.UriTemplate;


public class SourceNodeBuilder {
	
	private WorkflowContext		_context = null;
	private SourceNode 			_node = null;
	protected String			_name = "";
	private String 				_outflowExpression;
	private String 				_outflowLabel;
	private String 				_path;
	private Protocol 			_protocol;
	
	public SourceNodeBuilder context(WorkflowContext context) {
		_context = context;
		return this;
	}
	
	public SourceNodeBuilder name(String name) {
		_name = name;
		return this;
	}
	
	public SourceNodeBuilder outflow(String label, String outflowExpression) {
		_outflowLabel = label;
		_outflowExpression = outflowExpression;
		return this;
	}
	
	public SourceNodeBuilder protocol(FileProtocol protocol) {
		_protocol = protocol;
		return this;
	}

	public SourceNodeBuilder resource(String path) {
		_path = path;
		return this;
	}

	public SourceNode build() throws Exception {
		
		_node = new SourceNode();
		
		_node.setName(_name);
		_node.setApplicationContext(_context);
		_node.setUriTemplate(new UriTemplate(_path));
		_node.setProtocolReader(_protocol.getNewProtocolReader());
		_node.registerOutflow(_outflowLabel, _outflowExpression, false);

		return _node;
	}
}
