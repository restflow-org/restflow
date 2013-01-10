package org.restflow.data;

import java.io.File;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.restflow.WorkflowContext;
import org.restflow.actors.Actor;
import org.restflow.nodes.WorkflowNode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;


/**
 * This class is thread safe.  Its single mutable field is volatile and refers to a 
 * thread safe class.
 */
@ThreadSafe()
public class WorkspaceProtocol extends AbstractProtocol implements ApplicationContextAware {
	
	@Override
	public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues, Long stepID) 
			throws Exception {
		throw new Exception("not allowed to publish to workspace directory");
	}

	@Override
	public boolean isExternallyResolvable() {
		return true;
	}

	@Override
	public ProtocolReader getNewProtocolReader() throws Exception {
		return new WorkspaceProtocolReader();
	}

	@Override
	public Object loadResourcePayload(PublishedResource resource, Actor actor, String label) throws Exception {
		
		Object packetPayload = null;
		
		// get the expanded uri for incoming file item
		Uri uri = resource.getUri();
		
		// get the path portion of the URI
		String relativePath = uri.getPath();
		Resource r = _context.getResource("workspace:" + relativePath);

		// access the file at this path relative to the working directory
		File inFile = new File(r.getFilename());
		
		String actorInputType = actor.getInputType(label);
		String localPath = actor.getInputLocalPath(label);
		
		if (localPath.isEmpty()) {
			// TODO Should this be set to uri.getName() as in the File protocol?
			localPath = uri.getPath();
		}
		
		// if the actor expects a file handle as input, copy the file to the scratch directory
		// and give the actor a handle to the copy
		if (actorInputType == null || actorInputType.equals("File")) {
			
			// duplicate the file
			File duplicateFile = new File(actor.getNextStepDirectory() + "/" + localPath);				
			FileUtils.copyFile(inFile, duplicateFile);
			
			packetPayload = duplicateFile;
		
		// otherwise read the file and provide its contents to the actor as a String
		} else {
			
			String fileContents = FileUtils.readFileToString(inFile);
			packetPayload = fileContents;
		}
		
		return packetPayload;

	}
	
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		_context = (WorkflowContext)context;
	}

	@Override
	public void validateOutflowUriTemplate(WorkflowNode node, String label,
			UriTemplate uriTemplate) throws Exception {
		throw new Exception("not allowed to publish to workspace directory");
	}

	@Override
	public boolean supportsSuffixes() {
		return false;
	}

	public class WorkspaceProtocolReader implements ProtocolReader {
		
		private boolean _hasReadResource = false;
		
		public boolean stepsOnce() {
			return true;
		}
		
		@Override
		public Object getExternalResource(String uriPath) throws Exception {

			if (_hasReadResource) {
				return null;
			}
			
			String relativePath = uriPath;
			Resource r = _context.getResource("workspace:" + relativePath );
			
			_hasReadResource = true;
			
			String fileContents = IOUtils.toString(r.getInputStream(),"UTF-8");
									
			return fileContents;
			
/*			String fileContents = FileUtils.readFileToString(file);
						
			_hasReadResource = true;
			
			return fileContents;*/
		}

		@Override
		public void initialize() {
			_hasReadResource = false;
		}
	}
}
