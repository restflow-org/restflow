package org.restflow.data;

import org.restflow.WorkflowContext;
import org.restflow.actors.Actor;
import org.restflow.nodes.WorkflowNode;
import org.springframework.context.ApplicationContextAware;


public interface Protocol extends ApplicationContextAware {

//	Packet createPacket(Object value, Map<String, Object> metadata,
//			Outflow outflow) throws Exception;

	Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues, Long stepID) 
		throws Exception;

	boolean isExternallyResolvable();

	String getResourceSummaryLine(PublishedResource publishedResource);

	ProtocolReader getNewProtocolReader() throws Exception;

	Object loadResourcePayload(PublishedResource resource, Actor actor,
			String label) throws Exception;

	void validateInflowUriTemplate(UriTemplate uriTemplate, WorkflowNode node)
		throws Exception;

	boolean supportsSuffixes();

	void validateOutflowUriTemplate(WorkflowNode _node, String _label,
			UriTemplate _uriTemplate) throws Exception;
}
