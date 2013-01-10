package org.restflow.data;

import org.restflow.actors.Actor;
import org.restflow.exceptions.RestFlowException;

import net.jcip.annotations.Immutable;

/**
 * This class is thread safe because it has no fields and thus no state; and its superclass is
 * similarly thread safe.
 */
@Immutable
public class ControlProtocol extends DataProtocol {

	public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues, Long stepID) 
			throws Exception {
		
		return super.createPacket(data, uri, uriTemplate, variableValues, stepID);
	}
	
	@Override
	public ProtocolReader getNewProtocolReader() throws Exception {
		throw new RestFlowException("Control protocol does not support resolution of external resources");
	}
	
	@Override
	public Object loadResourcePayload(PublishedResource resource, Actor actor,
			String label) throws Exception {
		throw new RestFlowException("Payload may not be loaded for packets sent using a control protocol.");
	}
}
