package org.restflow.data;

import net.jcip.annotations.ThreadSafe;

/**
 * This class is thread safe. The types of all field values are immutable,
 * each field is marked volatile, no logic is performed jointly
 * on more than one field, and the superclass is thread safe.
 */
@ThreadSafe()
public class InputSignatureElement extends AbstractSignatureElement {

	private volatile Object _defaultValue = null;
	private volatile boolean _defaultInputEnable = true;
	private volatile String _localPath = "";

	public InputSignatureElement(String label) {
		super(label);
	}

	public void setDefaultValue(Object value) {
		_defaultValue = value;
	}

	public Object getDefaultValue() { 
		return _defaultValue; 
	}

	public void setDefaultInputEnable(boolean defaultInputEnable) {
		_defaultInputEnable = defaultInputEnable;
	}

	public boolean getDefaultInputEnable() { 
		return _defaultInputEnable; 
	}

	public void setLocalPath(String localPath) {
		_localPath = localPath;
	}

	public String getLocalPath() {
		return _localPath;
	}
}
