package org.restflow.data;

import org.restflow.data.DataProtocol;
import org.restflow.data.FileProtocol;
import org.restflow.data.PublishedResource;
import org.restflow.data.SingleResourcePacket;
import org.restflow.data.Uri;
import org.restflow.test.RestFlowTestCase;


public class TestSingleResourcePacket extends RestFlowTestCase {

	private PublishedResource stringResource;	
	private PublishedResource integerResource;
	private PublishedResource nullResource;

	private SingleResourcePacket stringResourcePacket;
	private SingleResourcePacket integerResourcePacket;
	private SingleResourcePacket nullDataResourcePacket;
	private SingleResourcePacket doubleDataPacket;
	private SingleResourcePacket booleanDataPacket;
	private SingleResourcePacket nullDataPacket;

	public void setUp() throws Exception {
		
		super.setUp();

		stringResource = new PublishedResource(
				new String("stringvalue"), 
				new Uri("/string/uri"), 
				"/string/key"
		);
		
		integerResource = new PublishedResource(
				new Integer(42), 
				new Uri("/integer/uri"), 
				"/integer/key"
		);
		
		nullResource = new PublishedResource(
				null, 
				new Uri("/nulldata/uri"), 
				"/nulldata/key"
		);
		
		stringResourcePacket = new SingleResourcePacket(
				stringResource, 
				new FileProtocol(), 
				new String[] {"key1", "key2"}, 
				new String[] {"value1", "value2"}
			);
		
		integerResourcePacket = new SingleResourcePacket(
				integerResource,
				new DataProtocol(),
				new String[] {}, 
				new String[] {}
			);
		
		nullDataResourcePacket = new SingleResourcePacket(
				nullResource, 
				new FileProtocol(),
				new String[] {"key1"}, 
				new String[] {"value1"}
			);

		doubleDataPacket = new SingleResourcePacket(
				new Double(42.3)
			);

		booleanDataPacket = new SingleResourcePacket(
				new Boolean("true")
			);

		nullDataPacket = new SingleResourcePacket(
				null
		);
	}
	
	public void testGetResource_NoKey_StringData() throws Exception {
		assertTrue(stringResourcePacket.getResource().getData() instanceof String);
		assertEquals("stringvalue", stringResourcePacket.getResource().getData());		
	}

	public void testGetResource_NoKey_IntegerData() throws Exception {
		assertTrue(integerResourcePacket.getResource().getData() instanceof Integer);
		assertEquals(42, integerResourcePacket.getResource().getData());
	}

	public void testGetResource_NoKey_NullDataResource() throws Exception {
		assertNull(nullDataResourcePacket.getResource().getData());
	}

	public void testGetResource_NoKey_NullData() throws Exception {
		assertNull(nullDataPacket.getResource().getData());
	}

	public void testGetResource_NoKey_DoubleData() throws Exception {
		assertTrue(doubleDataPacket.getResource().getData() instanceof Double);
		assertEquals(42.3, doubleDataPacket.getResource().getData());
	}

	public void testGetResource_NoKey_BooleanData() throws Exception {
		assertTrue(booleanDataPacket.getResource().getData() instanceof Boolean);
		assertEquals(true, booleanDataPacket.getResource().getData());
	}

	public void testGetResource_NullKey_StringData() throws Exception {		
		assertTrue(stringResourcePacket.getResource(null).getData() instanceof String);
		assertEquals("stringvalue", stringResourcePacket.getResource(null).getData());		
		assertSame(stringResourcePacket.getResource(), stringResourcePacket.getResource(null));
	}	

	public void testGetResource_NullKey_IntegerData() throws Exception {		
		assertTrue(integerResourcePacket.getResource(null).getData() instanceof Integer);
		assertEquals(42, integerResourcePacket.getResource(null).getData());
		assertSame(integerResourcePacket.getResource(), integerResourcePacket.getResource(null));
	}

	public void testGetResource_NullKey_NullDataResource() throws Exception {
		assertNull(nullDataResourcePacket.getResource(null).getData());
	}
	
	public void testGetResource_NullKey_NullData() throws Exception {
		assertNull(nullDataPacket.getResource(null).getData());
	}

	public void testGetResource_NullKey_DoubleData() throws Exception {		
		assertTrue(doubleDataPacket.getResource(null).getData() instanceof Double);
		assertEquals(42.3, doubleDataPacket.getResource(null).getData());
		assertSame(doubleDataPacket.getResource(), doubleDataPacket.getResource(null));
	}

	public void testGetResource_NullKey_BooleanData() throws Exception {		
		assertTrue(booleanDataPacket.getResource(null).getData() instanceof Boolean);
		assertEquals(true, booleanDataPacket.getResource(null).getData());
		assertSame(booleanDataPacket.getResource(), booleanDataPacket.getResource(null));
	}

	public void testGetResource_WithWrongKey_StringData() throws Exception {
		assertTrue(stringResourcePacket.getResource("/wrong/key").getData() instanceof String);
		assertEquals("stringvalue", stringResourcePacket.getResource("/wrong/key").getData());		
		assertSame(stringResourcePacket.getResource(), stringResourcePacket.getResource("/wrong/key"));
	}
	
	public void testGetResource_WithWrongKey_IntegerData() throws Exception {
		assertTrue(integerResourcePacket.getResource("/wrong/key").getData() instanceof Integer);
		assertEquals(42, integerResourcePacket.getResource("/wrong/key").getData());
		assertSame(integerResourcePacket.getResource(), integerResourcePacket.getResource("/wrong/key"));
	}	

	public void testGetResource_WithWrongKey_NullDataResource() throws Exception {
		assertNull(nullDataResourcePacket.getResource("/wrong/key").getData());
	}
	
	public void testGetResource_WithWrongKey_NullData() throws Exception {
		assertNull(nullDataPacket.getResource("/wrong/key").getData());
	}

	public void testGetResource_WithWrongKey_DoubleData() throws Exception {
		assertTrue(doubleDataPacket.getResource("/wrong/key").getData() instanceof Double);
		assertEquals(42.3, doubleDataPacket.getResource("/wrong/key").getData());
		assertSame(doubleDataPacket.getResource(), doubleDataPacket.getResource("/wrong/key"));
	}	

	public void testGetResource_WithWrongKey_BooleanData() throws Exception {
		assertTrue(booleanDataPacket.getResource("/wrong/key").getData() instanceof Boolean);
		assertEquals(42.3, doubleDataPacket.getResource("/wrong/key").getData());
		assertSame(booleanDataPacket.getResource(), booleanDataPacket.getResource("/wrong/key"));
	}	

	public void testGetProtocol_NoKey_StringData() throws Exception {
		assertTrue(stringResourcePacket.getProtocol() instanceof FileProtocol);
	}

	public void testGetProtocol_NoKey_IntegerData() throws Exception {
		assertTrue(integerResourcePacket.getProtocol() instanceof DataProtocol);
	}

	public void testGetProtocol_NoKey_NullDataResource() throws Exception {
		assertTrue(nullDataResourcePacket.getProtocol() instanceof FileProtocol);
	}
	
	public void testGetProtocol_NoKey_NullData() throws Exception {
		assertTrue(nullDataPacket.getProtocol() instanceof DataProtocol);
	}

	public void testGetProtocol_NoKey_DoubleData() throws Exception {
		assertTrue(doubleDataPacket.getProtocol() instanceof DataProtocol);
	}

	public void testGetProtocol_NoKey_BooleanData() throws Exception {
		assertTrue(booleanDataPacket.getProtocol() instanceof DataProtocol);
	}

	public void testGetResourceUriExpression_StringData() throws Exception {	
		assertEquals("/string/uri", stringResourcePacket.getResource().getUri().getExpression());
	}
	
	public void testGetResourceUriExpression_IntegerData() throws Exception {	
		assertEquals("/integer/uri", integerResourcePacket.getResource().getUri().getExpression());
	}

	public void testGetResourceUriExpression_NullDataResource() throws Exception {	
		assertEquals("/nulldata/uri", nullDataResourcePacket.getResource().getUri().getExpression());
	}

	public void testGetResourceUriExpression_NullData() throws Exception {	
		assertEquals("/", nullDataPacket.getResource().getUri().getExpression());
	}

	public void testGetResourceUriExpression_DoubleData() throws Exception {	
		assertEquals("/", doubleDataPacket.getResource().getUri().getExpression());
	}

	public void testGetResourceUriExpression_BooleanData() throws Exception {	
		assertEquals("/", booleanDataPacket.getResource().getUri().getExpression());
	}

	public void testGetResourceKey_StringData() throws Exception {	
		assertEquals("/string/key", stringResourcePacket.getResource().getKey());
	}
	
	public void testGetResourceKey_IntegerData() throws Exception {	
		assertEquals("/integer/key", integerResourcePacket.getResource().getKey());
	}

	public void testGetResourceKey_NullDataResource() throws Exception {	
		assertEquals("/nulldata/key", nullDataResourcePacket.getResource().getKey());
	}
	
	public void testGetResourceKey_NullData() throws Exception {	
		assertEquals("/", nullDataPacket.getResource().getKey());
	}

	public void testGetResourceKey_DoubleData() throws Exception {	
		assertEquals("/", doubleDataPacket.getResource().getKey());
	}

	public void testGetResourceKey_BooleanData() throws Exception {	
		assertEquals("/", booleanDataPacket.getResource().getKey());
	}

	public void testGetMetadataKeys_StringData() throws Exception {	
		assertEquals(2, stringResourcePacket.getMetadataKeys().length);
		assertEquals("key1", stringResourcePacket.getMetadataKeys()[0]);
		assertEquals("key2", stringResourcePacket.getMetadataKeys()[1]);
	}
	
	public void testGetMetadataKeys_IntegerData() throws Exception {	
		assertEquals(0, integerResourcePacket.getMetadataKeys().length);
	}

	public void testGetMetadataKeys_NullDataResource() throws Exception {	
		assertEquals(1, nullDataResourcePacket.getMetadataKeys().length);
		assertEquals("key1", nullDataResourcePacket.getMetadataKeys()[0]);
	}

	public void testGetMetadataKeys_NullData() throws Exception {	
		assertEquals(0, nullDataPacket.getMetadataKeys().length);
	}

	public void testGetMetadataKeys_DoubleData() throws Exception {	
		assertEquals(0, doubleDataPacket.getMetadataKeys().length);
	}
	
	public void testGetMetadataKeys_BooleanData() throws Exception {	
		assertEquals(0, booleanDataPacket.getMetadataKeys().length);
	}

	public void testGetMetadataValues_StringData() throws Exception {	
		assertEquals(2, stringResourcePacket.getMetadataValues().length);
		assertEquals("value1", stringResourcePacket.getMetadataValues()[0]);
		assertEquals("value2", stringResourcePacket.getMetadataValues()[1]);
	}
	
	public void testGetMetadataValues_IntegerData() throws Exception {	
		assertEquals(0, integerResourcePacket.getMetadataValues().length);
	}

	public void testGetMetadataValues_NullDataResource() throws Exception {	
		assertEquals(1, nullDataResourcePacket.getMetadataValues().length);
		assertEquals("value1", nullDataResourcePacket.getMetadataValues()[0]);
	}

	public void testGetMetadataValues_NullData() throws Exception {	
		assertEquals(0, nullDataPacket.getMetadataValues().length);
	}

	public void testGetMetadataValues_DoubleData() throws Exception {	
		assertEquals(0, doubleDataPacket.getMetadataValues().length);
	}

	public void testGetMetadataValues_BooleanData() throws Exception {	
		assertEquals(0, booleanDataPacket.getMetadataValues().length);
	}
}