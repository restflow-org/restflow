package org.restflow.data;

import net.jcip.annotations.ThreadSafe;

/*
 *  This class is thread safe because all of its configuration fields are final. 
*/
@ThreadSafe()
public class PublishedResource {
	
	///////////////////////////////////////////////////////////////////
	////                  private instance fields                  ////

	private final Object data;
	private final Uri uri;
	private final String key;
	private final boolean referencesData;

	///////////////////////////////////////////////////////////////////
	////                  public class constants                   ////

	public static final String defaultKey = "/";
	public static final Uri defaultUri = new Uri(defaultKey);
	public static final Protocol defaultProtocol = AbstractPacket.defaultProtocol;
	
	public PublishedResource(Object data, Uri uri, String key, boolean referencesData) {
		this.data = data;
		this.uri = uri;
		this.key = key;
		this.referencesData = referencesData;
	}

	public PublishedResource(Object data, Uri uri, String key) {
		this(data, uri, key, false);
	}
	
	public PublishedResource(Object data, Uri uri) {
		this(data, uri, defaultKey, false);
	}
	
	public PublishedResource(Object data) {
		this(data, defaultUri, defaultKey, false);
	}

	public boolean referencesData() {
		return referencesData;
	}
	
	public String getKey() {
		return key;
	}

	public Object getData() {
		return data;
	}

	public Uri getUri() {
		return uri;
	}

	public String toString() {
		return defaultProtocol.getResourceSummaryLine(this);
	}
}