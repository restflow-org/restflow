package org.restflow.data;

import java.io.File;

import net.jcip.annotations.Immutable;

/**
 * This class is thread safe because it has no fields and thus no state; and its superclass is
 * similarly thread safe.
 */
@Immutable
public class DataProtocol extends AbstractProtocol {

	public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues, Long stepID) 
			throws Exception {
		
		if (data instanceof File) { 
			throw new Exception("A file may not be published using the data protocol.");
		}
		
		PublishedResource resource = new PublishedResource(data, uri, uriTemplate.getReducedPath(), false);

		SingleResourcePacket packet = 
			new SingleResourcePacket(resource, this, uriTemplate.getVariableNames(), variableValues);
		
		_recordPacketCreated(packet, stepID);
		
		return packet;
	}
	
	@Override
	public ProtocolReader getNewProtocolReader() throws Exception {
		throw new Exception("Data protocol does not support resolution of external resources");
	}

	@Override
	public boolean supportsSuffixes() {
		return true;
	}
}
