package org.restflow.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.jcip.annotations.Immutable;

import org.apache.commons.io.FileUtils;
import org.restflow.actors.Actor;
import org.restflow.nodes.WorkflowNode;


/**
 * This class is thread safe.  It contains no fields, and is thus stateless.  
 * And its superclass is thread safe.
 */
@Immutable()
public class DirectProtocol extends AbstractProtocol {

	@Override
	public void validateInflowUriTemplate(UriTemplate uriTemplate,
			WorkflowNode node) throws Exception {
	}
	
	public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues, Long stepID) 
		throws Exception {

		File publishedResource;
		
		try {
			publishedResource = (File)data;
		} catch (ClassCastException e) {
			throw new Exception("Only directly created files may be published using the direct protocol: " + data, e);
		}
		
		String uriName = uri.getName();
		String uriPath = uri.getPath();
		
		String uriParent;
		if (uriTemplate.getVariableCount() == 0) {
			uriParent = uriPath.substring(0, uriPath.length() - uriName.length());
		} else {
			uriParent = uriPath + "/";		
		}

		String actualUri = "";
		if (publishedResource.isFile()) {
			actualUri = uriParent + publishedResource.getName();
		} else {
			actualUri = uriParent;
		}
		
		uri = new Uri(actualUri);
		
		PublishedResource resource = null;

		Packet packet;
		
		// if the resource is a single file, copy it to the published location
		if (publishedResource.isFile()) {

			resource = new PublishedResource(data, uri, uriTemplate.getReducedPath(), true);
			packet = new SingleResourcePacket(resource, this, uriTemplate.getVariableNames(), variableValues);
		
		// if it is a directory then recursively copy the whole directory tree
		} else if (publishedResource.isDirectory()) {
			
			Collection<PublishedResource> resources = new ArrayList<PublishedResource>();
			_addDirectoryOfResources(resources, uri, uriTemplate.getReducedPath(), publishedResource);
			packet = new MultiResourcePacket(resources, this, uriTemplate.getVariableNames(), variableValues);
			
		} else {
			throw new Exception("Only normal files and directories may be published.");
		}

		_recordPacketCreated(packet, stepID);
		
		return packet;
	}
	
	private void _addDirectoryOfResources(Collection<PublishedResource> resources, Uri baseUri, String baseBinding, File directory) {
		
		PublishedResource resource = new PublishedResource(directory, baseUri, baseBinding, true);
		
		resources.add(resource);

		for (File item : directory.listFiles()) {
			String name = item.getName();
			Uri uri = new Uri(baseUri + "/" + name);
			String binding = baseBinding + "/" + name;
			if (item.isDirectory()) {
				_addDirectoryOfResources(resources, uri, binding, item);
			} else {
				resource = new PublishedResource(item, uri, binding, true);
				resources.add(resource);
			}
		}
	}
	
	@Override
	public Object loadResourcePayload(PublishedResource resource, Actor actor, String label) throws Exception {

		Object packetPayload = null;
		
		// get the uri of the resource
		Uri uri = resource.getUri();
		
		// access the file at this path relative to the working directory
		File inFile = (File)resource.getData();
		
		String actorInputType = actor.getInputType(label);
		String localPath = actor.getInputLocalPath(label);
		
		if (localPath.isEmpty()) {
			localPath = uri.getName();
		}
		
		if (inFile.isFile()) {
			// if the actor expects a file handle as input, copy the file to the scratch directory
			// and give the actor a handle to the copy
			if (actorInputType.isEmpty() || actorInputType.equals("File")) {
				
				packetPayload = inFile;
			
			// otherwise read the file and provide its contents to the actor as a String
			} else {
				
				String fileContents = FileUtils.readFileToString(inFile);
				packetPayload = fileContents;
			}
		} else if (inFile.isDirectory()) {
		
			packetPayload = inFile;
			
		} else {
			throw new Exception("Only normal files and directories may be subscribed to.");
		}

		return packetPayload;
	}

	public String getResourceSummaryLine(PublishedResource resource) {
		return resource.getUri().toString();
	}
	
	@Override
	public boolean isExternallyResolvable() {
		return true;
	}

	@Override
	public ProtocolReader getNewProtocolReader() throws Exception {
		throw new Exception("Direct protocol does not support resolution of external resources");
	}
	
	@Override
	public boolean supportsSuffixes() {
		return false;
	}

}
