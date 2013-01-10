package org.restflow.data;

import org.restflow.metadata.MetadataManager;
import org.restflow.nodes.WorkflowNode;
import org.restflow.util.PortableIO;

import net.jcip.annotations.ThreadSafe;


@ThreadSafe()
public class LogProtocol extends DataProtocol {

	/**
	 * This class is thread safe.  Its mutable fields are marked volatile and no
	 * logic is performed jointly on more than one field.
	 */
	
	private volatile String logName;
	private volatile boolean teeToStandardOut = true;
	
	public void setLogName(String name) {
		logName = name;
	}
	
	public String getLogName() {
		return logName;
	}
	
	public void setTeeToStandardOut(boolean teeToStandardOut) {
		this.teeToStandardOut = teeToStandardOut;
	}
	
	public boolean getTeeToStandardOut() {
		return teeToStandardOut;
	}
	
	@Override
	public void validateInflowUriTemplate(UriTemplate uriTemplate,
			WorkflowNode node) throws Exception {
		throw new Exception(uriTemplate.getScheme() + " scheme may not be used on an inflow.");
	}
	
	@Override
	public boolean supportsSuffixes() {
		return true;
	}
	
	public MetadataManager getMetadataManager() {
		return _context.getMetaDataManager();
	}
	
	public static class Logger {

		LogProtocol _protocol;
		private StringBuilder _buffer;
		
		public Logger(LogProtocol protocol) {
			super();
			_protocol = protocol;
			_buffer = new StringBuilder();
		}
		
		public void add(String messageBody) {
			
			String message = PortableIO.getLogTimeStamp() + messageBody + PortableIO.EOL;
			
			if (_protocol.getTeeToStandardOut()) {
				System.out.print(message);
			}

			_protocol.getMetadataManager().writeToLog(_protocol.getLogName(), message.toString());

			_buffer.append(messageBody)
					.append('\n')
//					.append(PortableIO.EOL)
			;
		}
		
		public String getMessages() {
			return _buffer.toString();
		}
		
		public void clear() {
			_buffer = new StringBuilder();
		}
	}

}