package org.restflow.data;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.data.DataProtocol;
import org.restflow.data.Packet;
import org.restflow.data.Protocol;
import org.restflow.data.PublishedResource;
import org.restflow.data.SingleResourcePacket;
import org.restflow.data.Uri;
import org.restflow.data.UriTemplate;
import org.restflow.metadata.NoopTraceRecorder;
import org.restflow.test.RestFlowTestCase;


public class TestDataProtocol extends RestFlowTestCase {

	private Packet packetStringData;
	private Packet packetNullData;

	public void setUp() throws Exception {
		
		super.setUp();
		
		WorkflowContext context = new WorkflowContextBuilder()
			.recorder(new NoopTraceRecorder())
			.build();
		
		Protocol protocol = new DataProtocol();
		protocol.setApplicationContext(context);
		
		packetStringData = protocol.createPacket(
				"bar", 
				new Uri("/string35/foo"), 
				new UriTemplate("/string{num}/foo"), 
				new Object[] {35},
				null
		);

		packetNullData = protocol.createPacket(
				null, 
				new Uri("/null181/foo/two"), 
				new UriTemplate("/null{id}/foo/{version}"), 
				new Object[] {181, "two"},
				null
		);
}
	
	public void testCreatePacket_StringData() throws Exception {
		assertTrue(packetStringData instanceof SingleResourcePacket);
		assertTrue(packetStringData.getProtocol() instanceof DataProtocol);
	}

	public void testCreatePacket_NullData() throws Exception {
		assertTrue(packetNullData instanceof SingleResourcePacket);
		assertTrue(packetNullData.getProtocol() instanceof DataProtocol);
	}
	
	public void testCreatePacket_GetResource_StringData() throws Exception {
		PublishedResource resource = packetStringData.getResource("/string{}/bar");
		assertSame(resource, ((SingleResourcePacket)packetStringData).getResource());
		assertEquals("bar", resource.getData());
		assertEquals("/string35/foo", resource.getUri().getExpression());
		assertEquals("/string{}/foo", resource.getKey());
	}

	public void testCreatePacket_GetResource_NullData() throws Exception {
		PublishedResource resource = packetNullData.getResource("/null{}/foo/{}");
		assertSame(resource, ((SingleResourcePacket)packetNullData).getResource());
		assertEquals(null, resource.getData());
		assertEquals("/null181/foo/two", resource.getUri().getExpression());
		assertEquals("/null{}/foo/{}", resource.getKey());
	}

	public void testCreatePacket_getMetadataKeys_StringData() {
		assertEquals(1, packetStringData.getMetadataKeys().length);
		assertEquals("num", packetStringData.getMetadataKeys()[0]);
	}
	
	public void testCreatePacket_getMetadataKeys_NullData() {
		assertEquals(2, packetNullData.getMetadataKeys().length);
		assertEquals("id", packetNullData.getMetadataKeys()[0]);
		assertEquals("version", packetNullData.getMetadataKeys()[1]);
	}

	public void testCreatePacket_getMetadataValues_StringData() {
		assertEquals(1, packetStringData.getMetadataValues().length);
		assertEquals(35, packetStringData.getMetadataValues()[0]);
	}
	
	public void testCreatePacket_getMetadataValues_NullData() {
		assertEquals(2, packetNullData.getMetadataValues().length);
		assertEquals(181, packetNullData.getMetadataValues()[0]);
		assertEquals("two", packetNullData.getMetadataValues()[1]);
	}

	//	public void testStorePacketPayload_EmptyString() throws Exception {
//		Packet packet = new Packet("");
//		assertTrue(protocol.storePacketPayload(packet));
//	}
//	
//	public void testStorePacketPayload_NulLPacket() throws Exception {
//	
//		String exceptionMessage = null;
//		try {
//			protocol.storePacketPayload(null);
//		} catch(IllegalArgumentException e) {
//			exceptionMessage = e.getMessage();
//		}
//		assertEquals("Null argument to storePacketPayload not allowed.", exceptionMessage);
//	}
}
	
