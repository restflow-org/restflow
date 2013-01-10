package org.restflow.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.io.FileUtils;
import org.restflow.WorkflowContext;
import org.restflow.actors.Actor;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.PortableIO;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This class is thread safe.  Its single mutable field is volatile and refers to a 
 * thread safe class.
 */
@ThreadSafe()
public class FileProtocol extends AbstractProtocol implements ApplicationContextAware {

	@Override
	public void validateOutflowUriTemplate(WorkflowNode node, String label,
			UriTemplate uriTemplate) throws Exception {
		
		if (_context.getRunDirectoryPath() == null) {
			throw new Exception("Attempt to use the File protocol on an outflow without defining a run directory:" + PortableIO.EOL +
					"node: " + node + PortableIO.EOL +
					"label: " + label + PortableIO.EOL +
					"uri: " + uriTemplate + PortableIO.EOL);
		}
		
		if (uriTemplate.getVariableCount() == 0 && ! node.stepsOnce()) {
			throw new Exception("No variables in outflow template with file scheme: " + uriTemplate + " on " + node);
		}
		
		
	}

	@Override
	public void validateInflowUriTemplate(UriTemplate uriTemplate,
			WorkflowNode node) throws Exception {
	}
	
	public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues, Long stepID) 
		throws Exception {

		Collection<PublishedResource> resources = new ArrayList<PublishedResource>();
		
		String runDirectoryPath = _context.getRunDirectoryPath();

		if (runDirectoryPath == null) {
			throw new Exception("Attempt to send a packet using the File protocol with no run directory.");
		}
		
		File publishedResource = new File(runDirectoryPath + "/" + uri.getPath());
		
		PublishedResource resource = null;

		Packet packet;

		// if the resource is a handle to a filesystem resource 
		// make a copy of the resource in the published area
		if (data instanceof File) {

			File localResource = (File)data;
			
			// if the resource is a single file, copy it to the published location
			if (localResource.isFile()) {

				FileUtils.copyFile(localResource, publishedResource);
				resource = new PublishedResource(data, uri, uriTemplate.getReducedPath(), true);
   			    packet = new SingleResourcePacket(resource, this, uriTemplate.getVariableNames(), variableValues);
			
			// if it is a directory then recursively copy the whole directory tree
			} else if (localResource.isDirectory()) {
				
				if (! (localResource.getAbsolutePath()).equals(publishedResource.getAbsolutePath())) {
					FileUtils.copyDirectory(localResource, publishedResource);
				}
				
				_addDirectoryOfResources(resources, uri, uriTemplate.getReducedPath(), localResource);
				packet = new MultiResourcePacket(resources, this, uriTemplate.getVariableNames(), variableValues);
				
			} else {
				throw new Exception("Only normal files and directories may be published to " + uri.getPath());
			}
		// if the resource is not a File object then publish its string value to a file
		} else {
			if (data != null) {
				FileUtils.writeStringToFile(publishedResource, String.valueOf(data));
			}
			resource = new PublishedResource(data, uri, uriTemplate.getReducedPath(), true);
			resources.add(resource);
			packet = new MultiResourcePacket(resources, this, uriTemplate.getVariableNames(), variableValues);
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
		File inFile = new File(_context.getRunDirectoryPath() + "/" + uri.getPath());
		
		if (!inFile.exists()) {
			return null;
		}
		
		String actorInputType = actor.getInputType(label);
		String localPath = actor.getInputLocalPath(label);
		
		if (localPath.isEmpty()) {
			localPath = uri.getName();
		}
		if (inFile.isFile()) {
			// if the actor expects a file handle as input, copy the file to the scratch directory
			// and give the actor a handle to the copy
			if (actorInputType.isEmpty() || actorInputType.equals("File")) {
				
				// duplicate the file
				File duplicateFile = new File(actor.getNextStepDirectory() + "/" + localPath);				
				FileUtils.copyFile(inFile, duplicateFile);
				
				packetPayload = duplicateFile;
			
			// otherwise read the file and provide its contents to the actor as a String
			} else {
				
				String fileContents = FileUtils.readFileToString(inFile);
				packetPayload = fileContents;
			}
		} else if (inFile.isDirectory()) {

			// duplicate the directory to the current step directory
			File duplicateDirectory = new File(actor.getNextStepDirectory() + "/" + localPath);				
			FileUtils.copyDirectory(inFile, duplicateDirectory);
			
			packetPayload = duplicateDirectory;
			
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
	public ProtocolReader getNewProtocolReader() {
		return new FileProtocolReader();
	}

	@Override
	public boolean supportsSuffixes() {
		return false;
	}

	
	/**
	 * This class is thread safe.  Its single mutable field is a primitive (32-bit) type
	 * and is marked volatile to insure visability of state changes across threads. And
	 * access to the external file resource is synchronized to ensure that the resource
	 * is read only once.
	 */
	public static class FileProtocolReader implements ProtocolReader {
		
		private volatile boolean _hasReadResource = false;
		
		public synchronized void initialize() {
			_hasReadResource = false;
		}

		public boolean stepsOnce() {
			return true;
		}
		
		@Override
		public synchronized Object getExternalResource(String uriPath) throws Exception {

			if (_hasReadResource) {
				return null;
			}
			
			File file = new File(uriPath);
			
			String fileContents = FileUtils.readFileToString(file);
						
			_hasReadResource = true;
			
			return fileContents;
		}
		
	}
}
