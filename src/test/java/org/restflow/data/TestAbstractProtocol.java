package org.restflow.data;

import org.restflow.data.AbstractProtocol;
import org.restflow.data.Packet;
import org.restflow.data.Protocol;
import org.restflow.data.PublishedResource;
import org.restflow.data.Uri;
import org.restflow.data.UriTemplate;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.PortableIO;


public class TestAbstractProtocol extends RestFlowTestCase {

	private Protocol protocol;
	private PublishedResource resource;
	private String key;
	private Uri uri;
	private static final String EOL = PortableIO.EOL;
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		protocol = new AbstractProtocol() {
			public Packet createPacket(Object data, Uri uri, UriTemplate uriTemplate, Object[] variableValues) 
					throws Exception { return null;}
			public boolean supportsSuffixes() { return false; }
		};
	}
	
	public void testExternallyProtocol() {
		assertFalse(protocol.isExternallyResolvable());
	}
	
	public void testGetSummaryLine_SingleLine_DefaultResourceProperties() {
		
		resource = new PublishedResource("foo");
		assertEquals("/: foo", protocol.getResourceSummaryLine(resource));
		
		resource = new PublishedResource(123);
		assertEquals("/: 123", protocol.getResourceSummaryLine(resource));

		resource = new PublishedResource(123.456);
		assertEquals("/: 123.456", protocol.getResourceSummaryLine(resource));

		resource = new PublishedResource(false);
		assertEquals("/: false", protocol.getResourceSummaryLine(resource));
	}
	
	public void testGetSummaryLine_MultiLine_DefaultResourceProperties() {
		
		resource = new PublishedResource(
				"foo" + EOL +
				"bar"
		);
		assertEquals("/: foo bar", protocol.getResourceSummaryLine(resource));
		
		resource = new PublishedResource(
				"foo" + EOL +
				"bar" + EOL
		);
		assertEquals("/: foo bar", protocol.getResourceSummaryLine(resource));

		resource = new PublishedResource(
				"a" + EOL +
				"b c" + EOL +
				"d e f"
		);
		assertEquals("/: a b c d e f", protocol.getResourceSummaryLine(resource));

		resource = new PublishedResource(
				"a" + EOL +
				"b c" + EOL+
				"d e f" + EOL
		);
		assertEquals("/: a b c d e f", protocol.getResourceSummaryLine(resource));
		
		resource = new PublishedResource(
				"a" + EOL +
				"b c" + EOL + EOL +
				"d e f" + EOL + EOL + EOL +
				"g h i j"
		);
		assertEquals("/: a b c d e f g h i j", protocol.getResourceSummaryLine(resource));

		resource = new PublishedResource(
				"a" + EOL +
				"b c" + EOL + EOL +
				"d e f" + EOL + EOL + EOL +
				"g h i j" + EOL + EOL + EOL + EOL
		);
		assertEquals("/: a b c d e f g h i j", protocol.getResourceSummaryLine(resource));
	}

	public void testGetSummaryLine_NullPayload_DefaultPacketProperties() {
		
		resource  = new PublishedResource(null);
		assertEquals("/: null", protocol.getResourceSummaryLine(resource));
	}

	public void testGetSummaryLine_NullPayload_ExplicitPacketProperties() {

		key = "/";
		uri = new Uri(key);
		resource = new PublishedResource(null, uri, key);
		assertEquals("/: null", protocol.getResourceSummaryLine(resource));

		key = "data:/";
		uri = new Uri(key);
		resource = new PublishedResource(null, uri, key);
		assertEquals("data:/: null", protocol.getResourceSummaryLine(resource));

		key = "/foo/bar/";
		uri = new Uri(key);
		resource = new PublishedResource(null, uri, key);
		assertEquals("/foo/bar/: null", protocol.getResourceSummaryLine(resource));

		key = "file:/foo/bar";
		uri = new Uri(key);
		resource = new PublishedResource(null, uri, key);
		assertEquals("file:/foo/bar: null", protocol.getResourceSummaryLine(resource));
	}

	public void testGetSummaryLine_SingleLine_ExplicitPacketProperties() {
		
		key = "/";
		uri = new Uri(key);
		resource = new PublishedResource("foo", uri, key);
		assertEquals("/: foo", protocol.getResourceSummaryLine(resource));
		
		key = "data:/";
		uri = new Uri(key);
		resource = new PublishedResource(123, uri, key);;
		assertEquals("data:/: 123", protocol.getResourceSummaryLine(resource));

		key = "/foo/bar/";
		uri = new Uri(key);
		resource = new PublishedResource(123.456, uri, key);;
		assertEquals("/foo/bar/: 123.456", protocol.getResourceSummaryLine(resource));

		uri = new Uri("file:/foo/bar");
		resource = new PublishedResource(false, uri, key);
		assertEquals("file:/foo/bar: false", protocol.getResourceSummaryLine(resource));
	}

	public void testGetSummaryLine_MultiLine_ExplicitPacketProperties() {
		
		key = "/";
		uri = new Uri(key);
		resource = new PublishedResource(
				"foo" + EOL +
				"bar", 
				uri, key 
		);
		assertEquals("/: foo bar", protocol.getResourceSummaryLine(resource));

		key = "/";
		uri = new Uri(key);
		resource = new PublishedResource(
				"foo" + EOL +
				"bar" + EOL, 
				uri, key
		);
		assertEquals("/: foo bar",  protocol.getResourceSummaryLine(resource));
		
		key = "data:/";
		uri = new Uri(key);
		resource = new PublishedResource(
				"a" + EOL +
				"b c" + EOL +
				"d e f", 
				uri, key
		);
		assertEquals("data:/: a b c d e f",  protocol.getResourceSummaryLine(resource));

		key = "data:/";
		uri = new Uri(key);
		resource = new PublishedResource(
				"a" + EOL +
				"b c" + EOL +
				"d e f" + EOL, 
				uri, key
		);
		assertEquals("data:/: a b c d e f", protocol.getResourceSummaryLine(resource));
		
		key = "/foo/bar/";
		uri = new Uri(key);
		resource = new PublishedResource(
				"a" + EOL +
				"b c" + EOL + EOL +
				"d e f" + EOL + EOL + EOL +
				"g h i j", 
				uri, key
		);
		assertEquals("/foo/bar/: a b c d e f g h i j", protocol.getResourceSummaryLine(resource));

		key = "/foo/bar/";
		uri = new Uri(key);
		resource = new PublishedResource(
				"a" + EOL +
				"b c" + EOL +
				"d e f" + EOL +
				"g h i j" + EOL + EOL + EOL + EOL, 
				uri, key
		);
		assertEquals("/foo/bar/: a b c d e f g h i j", protocol.getResourceSummaryLine(resource));
	}

//	public void testLoadPacketPayload_NonNullPayload() throws Exception {
//		
//		resource = new PublishedResource("foo");
//		assertEquals("foo", protocol.loadResourcePayload(resource, null, ""));
//		
//		resource = new PublishedResource(123);
//		assertEquals(123, protocol.loadResourcePayload(resource, null, ""));
//	
//		resource = new PublishedResource(123.456);
//		assertEquals(123.456, protocol.loadResourcePayload(resource, null, ""));
//	
//		resource = new PublishedResource(false);
//		assertEquals(false, protocol.loadResourcePayload(resource, null, ""));
//	
//	}
//	
//	public void testLoadPacketPayload_NullPayload() throws Exception {
//	
//		resource = new PublishedResource(null);
//		assertNull(protocol.loadResourcePayload(resource, null, ""));
//	}	
}





//public void testStorePacketPayload_NonEmptyString() throws Exception {
//Packet packet = new Packet("foo");
//assertTrue(protocol.storePacketPayload(packet));
//}
//
//public void testStorePacketPayload_EmptyString() throws Exception {
//Packet packet = new Packet("");
//assertTrue(protocol.storePacketPayload(packet));
//}
//
//public void testStorePacketPayload_NulLPacket() throws Exception {
//
//String exceptionMessage = null;
//try {
//	protocol.storePacketPayload(null);
//} catch(IllegalArgumentException e) {
//	exceptionMessage = e.getMessage();
//}
//assertEquals("Null argument to storePacketPayload not allowed.", exceptionMessage);
//}
//
