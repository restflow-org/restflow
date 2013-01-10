package org.restflow.data;

import org.restflow.nodes.WorkflowNode;

import net.jcip.annotations.Immutable;

/**
 * This class is thread safe because it has no fields and thus no state; and its superclass is
 * similarly thread safe.
 */
@Immutable
public class StdoutProtocol extends DataProtocol {

	public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues, Long stepID) 
			throws Exception {

		System.out.println(data);
		
		return super.createPacket(data, uri, uriTemplate, variableValues, stepID);
	}
	
	@Override
	public void validateInflowUriTemplate(UriTemplate uriTemplate,
			WorkflowNode node) throws Exception {
		throw new Exception("Stdout protocol is not allowed on inflows.");
	}

	@Override
	public ProtocolReader getNewProtocolReader() throws Exception {
		throw new Exception("Stdout protocol does not support resolution of external resources");
	}
}
