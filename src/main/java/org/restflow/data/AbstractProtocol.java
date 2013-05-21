package org.restflow.data;

import java.io.File;

import net.jcip.annotations.Immutable;

import org.apache.commons.io.FileUtils;
import org.restflow.WorkflowContext;
import org.restflow.actors.Actor;
import org.restflow.metadata.TraceRecorder;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.PortableIO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This class is thread safe because it has no fields and thus no state.
 */
@Immutable
public abstract class AbstractProtocol implements Protocol, ApplicationContextAware {

	protected WorkflowContext _context;

	public void setApplicationContext(ApplicationContext context) {
		_context = (WorkflowContext)context;
	}
	
	@Override
	public boolean isExternallyResolvable() {
		return false;
	}

	@Override
	public ProtocolReader getNewProtocolReader() throws Exception {
		throw new Exception("Abstract protocol does not support resolution of external resources");
	}

	public String getResourceSummaryLine(PublishedResource resource) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(resource.getUri());
		builder.append(": ");
		
		Object data = resource.getData();

		if (data == null) {
		
			builder.append("null");
		
		} else {
			
			String multiLineString = data.toString();
			String singleLineString = PortableIO.replaceLineBreaksWithSpaces(multiLineString);
			builder.append(singleLineString);
		}
		
		return builder.toString();
	}

	@Override
	public Object loadResourcePayload(PublishedResource resource, Actor actor,
			String label) throws Exception {

		Object data = resource.getData();

		// get the uri of the resource
		Uri uri = resource.getUri();

		String actorInputType = actor.getInputType(label);

		String localPath = actor.getInputLocalPath(label);

		if (localPath.isEmpty()) {
			localPath = uri.getName();
		}
		
		if (actorInputType.contains("File")) {
			
			// get the expanded uri for incoming file item
//			String relativePath = resource.getUri().path;
			File file = new File(actor.getNextStepDirectory() + "/" + localPath);
			FileUtils.writeStringToFile(file, data.toString());
			
			return file;			

		} else {
		
			return data;
		}
	}

	@Override
	public void validateInflowUriTemplate(UriTemplate uriTemplate,
			WorkflowNode node) throws Exception {
	}

	@Override
	public void validateOutflowUriTemplate(WorkflowNode node, String label, UriTemplate uriTemplate) throws Exception {
	}
	
	public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues, Long stepID)
			throws Exception {
		return null;
	}
	
	protected void _recordPacketCreated(Packet packet, Long stepID) throws Exception {
		TraceRecorder recorder = _context.getTraceRecorder();
		recorder.recordPacketCreated(packet, stepID);
	}
}
