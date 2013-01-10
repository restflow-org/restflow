package org.restflow.data;

import net.jcip.annotations.ThreadSafe;

/* This class is thread safe.  Its superclass is thread safe, and its single field
 * stores an immutable value and is marked volatile.
 */
@ThreadSafe()
public class OutputSignatureElement extends AbstractSignatureElement {
	
	private volatile boolean _defaultOutputEnable = true;
	
	public OutputSignatureElement(String label) {
		super(label);
	}
	
	public void setDefaultOutputEnable(boolean defaultOutputEnable) {
		_defaultOutputEnable = defaultOutputEnable;
	}
	
	public boolean getDefaultOutputEnable() { 
		return _defaultOutputEnable; 
	}
}
