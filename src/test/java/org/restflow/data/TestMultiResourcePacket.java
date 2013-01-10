package org.restflow.data;

import java.util.LinkedList;
import java.util.List;

import org.restflow.data.DataProtocol;
import org.restflow.data.FileProtocol;
import org.restflow.data.MultiResourcePacket;
import org.restflow.data.PublishedResource;
import org.restflow.data.Uri;
import org.restflow.test.RestFlowTestCase;


public class TestMultiResourcePacket extends RestFlowTestCase {

	private PublishedResource stringResource;	
	private PublishedResource integerResource;
	private PublishedResource nullResource;

	private MultiResourcePacket oneResourcePacket;
	private MultiResourcePacket threeResourcePacket;

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
		
		List<PublishedResource> listOfOneResoure = new LinkedList<PublishedResource>();
		listOfOneResoure.add(stringResource);	
		oneResourcePacket = new MultiResourcePacket(
			listOfOneResoure, 
			new FileProtocol(), 
			new String[] {"key1", "key2"}, 
			new String[] {"value1", "value2"}
		);

		List<PublishedResource> listOfThreeResources = new LinkedList<PublishedResource>();
		listOfThreeResources.add(stringResource);
		listOfThreeResources.add(integerResource);
		listOfThreeResources.add(nullResource);
		threeResourcePacket = new MultiResourcePacket(
			listOfThreeResources, 
			new DataProtocol(),
			new String[] {}, 
			new String[] {}
		);
	}
	
	public void test_GetResourceCollection_OneResourcePacket() throws Exception {
		assertEquals(1, oneResourcePacket.getResources().size());
		assertTrue(oneResourcePacket.getResources().contains(stringResource));
	}
	
	public void test_GetResourceCollection_ThreeResourcePacket() throws Exception {
		assertEquals(3, threeResourcePacket.getResources().size());
		assertTrue(threeResourcePacket.getResources().contains(stringResource));
		assertTrue(threeResourcePacket.getResources().contains(integerResource));
		assertTrue(threeResourcePacket.getResources().contains(nullResource));
	}

	public void test_GetResourcesByIterator_OneResourcePacket() throws Exception {
		assertSame(stringResource, oneResourcePacket.getResources().iterator().next());
	}
	
	public void test_GetResourceByKey_OneResourcePacket() throws Exception {	
		assertTrue(oneResourcePacket.getResource("/string/key").getData() instanceof String);
		assertEquals("stringvalue", oneResourcePacket.getResource("/string/key").getData());
	}		

	public void test_GetResourcesByKey_ThreeResourcePacket() throws Exception {	
		
		assertTrue(threeResourcePacket.getResource("/string/key").getData() instanceof String);
		assertEquals("stringvalue", threeResourcePacket.getResource("/string/key").getData());

		assertTrue(threeResourcePacket.getResource("/integer/key").getData() instanceof Integer);
		assertEquals(42, threeResourcePacket.getResource("/integer/key").getData());

		assertNull(threeResourcePacket.getResource("/nulldata/key").getData());
	}

	public void test_GetProtocol_OneResourcePacket() throws Exception {	
		assertTrue(oneResourcePacket.getProtocol() instanceof FileProtocol);
	}
		
	public void test_GetProtocol_ThreeResourcePacket() throws Exception {	
		assertTrue(threeResourcePacket.getProtocol() instanceof DataProtocol);
	}

	public void test_GetResourceUri_OneResourcePacket() throws Exception {	
		assertEquals("/string/uri", oneResourcePacket.getResource("/string/key").getUri().getExpression());
	}

	public void test_GetResourceUris_ThreeResourcePacket() throws Exception {	
		assertEquals("/string/uri", threeResourcePacket.getResource("/string/key").getUri().getExpression());
		assertEquals("/integer/uri", threeResourcePacket.getResource("/integer/key").getUri().getExpression());
		assertEquals("/nulldata/uri", threeResourcePacket.getResource("/nulldata/key").getUri().getExpression());
	}
	
	public void test_GetResourceKey_OneResourcePacket() throws Exception {	
		assertEquals("/string/key", oneResourcePacket.getResource("/string/key").getKey());
	}

	public void test_GetResourceKeys_ThreeResourcePacket() throws Exception {	
		assertEquals("/string/key", threeResourcePacket.getResource("/string/key").getKey());
		assertEquals("/integer/key", threeResourcePacket.getResource("/integer/key").getKey());
		assertEquals("/nulldata/key", threeResourcePacket.getResource("/nulldata/key").getKey());
	}

	public void test_GetResource_WrongKey_OneResourcePacket() throws Exception {	
		assertNull(oneResourcePacket.getResource("/non/existent/key"));
	}

	public void test_GetResource_WrongKey_ThreeResourcePacket() throws Exception {	
		assertNull(threeResourcePacket.getResource("/non/existent/path"));
	}
	
	public void test_GetMetadataKeys_OneResourcePacket() throws Exception {	
		assertEquals(2, oneResourcePacket.getMetadataKeys().length);
		assertEquals("key1", oneResourcePacket.getMetadataKeys()[0]);
		assertEquals("key2", oneResourcePacket.getMetadataKeys()[1]);
	}
	
	public void test_GetMetadataKeys_ThreeResourcePacket() throws Exception {	
		assertEquals(0, threeResourcePacket.getMetadataKeys().length);
	}

	public void test_GetMetadataValues_OneResourcePacket() throws Exception {	
		assertEquals(2, oneResourcePacket.getMetadataValues().length);
		assertEquals("value1", oneResourcePacket.getMetadataValues()[0]);
		assertEquals("value2", oneResourcePacket.getMetadataValues()[1]);
	}

	public void test_GetMetadataValues_ThreeResourcePacket() throws Exception {	
		assertEquals(0, threeResourcePacket.getMetadataValues().length);
	}

	
}