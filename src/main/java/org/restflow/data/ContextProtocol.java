package org.restflow.data;

import org.restflow.WorkflowContext;
import org.restflow.nodes.WorkflowNode;
import org.springframework.context.ApplicationContextAware;


//TODO analyze for thread safety
public class ContextProtocol extends AbstractProtocol implements ApplicationContextAware {

	@Override
	public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate,
			Object[] variableValues, Long stepID) throws Exception {
		throw new Exception("Not allowed to publish to the application context.");
	}

	@Override
	public boolean isExternallyResolvable() {
		return true;
	}

	@Override
	public ContextProtocolReader getNewProtocolReader() {
		return new ContextProtocolReader(_context);
	}	

	@Override
	public void validateInflowUriTemplate(UriTemplate uriTemplate,
			WorkflowNode node) throws Exception {

		String uriPath = uriTemplate.getReducedPath();
		
		if (uriPath.equals("/")) return;	
		if (uriPath.equals("/import-map")) return;
		if (uriPath.equals("/run")) return;		
		if (uriPath.equals("/base")) return;	
		if (uriPath.equals("/metadata")) return;

		if (uriPath.startsWith("/property/")) {
			_getPropertyValueForUri(_context, uriPath);
			return;
		}
		
		throw new Exception("unknown 'context' protocol path: " + uriPath);
	}

	private static Object _getPropertyValueForUri(WorkflowContext context, String uriPath) throws Exception {
		String key = uriPath.substring(10); 
		Object value = context.getProperty(key); 
		if (value == null) {
			throw new Exception("Undefined context property '" + key + "' in inflow path '" + uriPath + "'");
		} else {
			return value;
		}
	}
	
	@Override
	public String getResourceSummaryLine(PublishedResource resource) {
		return resource.getUri().toString();
	}
	
	@Override
	public boolean supportsSuffixes() {
		return false;
	}
	
	
	public static class ContextProtocolReader implements ProtocolReader {
		
		private final WorkflowContext _applicationContext;
		
		
		private volatile boolean _hasReadResource = false;
		
		public synchronized void initialize() {
			_hasReadResource = false;
		}
		
		public boolean stepsOnce() {
			return true;
		}

		public ContextProtocolReader(WorkflowContext applicationContext) {
			_applicationContext = applicationContext;
		}
		
		@Override
		public synchronized Object getExternalResource(String uriPath) throws Exception {

			if (_hasReadResource) {
				return null;
			}
			
			_hasReadResource = true;
			
			if (uriPath.startsWith("/property/")) {
				return _getPropertyValueForUri(_applicationContext, uriPath);
			}
			
			if (uriPath.startsWith("/import-map")) {
				return _applicationContext.getImportMappings();	
			}
			
			if (uriPath.startsWith("/run")) {
				return _applicationContext.getRunDirectoryPath();
			}
			
			if (uriPath.startsWith("/base")) {
				return _applicationContext.getBaseDirectoryPath();
			}

			if (uriPath.startsWith("/metadata")) {
				return _applicationContext.getMetaDataManager();
			}
			
			if (uriPath.startsWith("/")) {
				return _applicationContext;	
			}			
						
			throw new Exception("unknown 'context' protocol path: " + uriPath);
		}
	}
	
}
