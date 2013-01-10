package org.restflow.data;

import java.util.Hashtable;
import java.util.Map;

import net.jcip.annotations.ThreadSafe;

/*
 *  This class is thread safe because its superclass is thread safe
 *  and it adds no state to it.
*/
@ThreadSafe()
@SuppressWarnings("serial")
public class ProtocolRegistry extends Hashtable<String,Protocol> {
	
	public ProtocolRegistry() {
		setDefault(new DataProtocol());
	}
	
	public void setDefault(Protocol defaultProtocol) {
		put(Uri.NO_SCHEME, defaultProtocol);
	}
	
	public void setProtocols(Map<String, Protocol> protocols) throws Exception {
		putAll(protocols);
	}

	public Protocol getProtocolForScheme(String scheme) throws Exception {	
		return get(scheme);
	}
}