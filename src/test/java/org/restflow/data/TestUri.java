package org.restflow.data;

import org.restflow.data.Uri;
import org.restflow.data.UriTemplate;
import org.restflow.test.RestFlowTestCase;


public class TestUri extends RestFlowTestCase {

	private Uri u;
	
	public void testEquals_SameObject() {
		
		u = new Uri("");
		assertTrue(u.equals(u));

		u = new Uri("/");
		assertTrue(u.equals(u));

		u = new Uri("a");
		assertTrue(u.equals(u));

		u = new Uri("/a");
		assertTrue(u.equals(u));

		u = new Uri("/a/b");
		assertTrue(u.equals(u));

		u = new Uri("foo:/");
		assertTrue(u.equals(u));

		u = new Uri("foo:/a/");
		assertTrue(u.equals(u));

		u = new Uri("foo:/a/b");
		assertTrue(u.equals(u));
	}
	
	public void testEquals_WrongType() {
		
		u = new Uri("");
		assertFalse(u.equals(""));

		u = new Uri("/");
		assertFalse(u.equals("/"));

		u = new Uri("a");
		assertFalse(u.equals(new UriTemplate("a")));

		u = new Uri("/a");
		assertFalse(u.equals(new UriTemplate("/a")));
	}	
	
	public void testEquals_Null() {
		
		u = new Uri("");
		assertFalse(u.equals(null));

		u = new Uri("/");
		assertFalse(u.equals(null));

		u = new Uri("a");
		assertFalse(u.equals(null));

		u = new Uri("/a");
		assertFalse(u.equals(null));
	}

	public void testEquals_SameExpression() {
		
		u = new Uri("");
		assertTrue(u.equals(new Uri("")));

		u = new Uri("/");
		assertTrue(u.equals(new Uri("/")));

		u = new Uri("a");
		assertTrue(u.equals(new Uri("a")));

		u = new Uri("/a");
		assertTrue(u.equals(new Uri("/a")));

		u = new Uri("/a/b");
		assertTrue(u.equals(new Uri("/a/b")));

		u = new Uri("foo:/");
		assertTrue(u.equals(new Uri("foo:/")));

		u = new Uri("foo:/a/");
		assertTrue(u.equals(new Uri("foo:/a/")));

		u = new Uri("foo:/a/b");
		assertTrue(u.equals(new Uri("foo:/a/b")));
	}

	public void testCompareTo_SameObject() {
		
		u = new Uri("");
		assertEquals(0, u.compareTo(u));

		u = new Uri("/");
		assertEquals(0, u.compareTo(u));

		u = new Uri("a");
		assertEquals(0, u.compareTo(u));

		u = new Uri("/a");
		assertEquals(0, u.compareTo(u));

		u = new Uri("/a/b");
		assertEquals(0, u.compareTo(u));

		u = new Uri("foo:/");
		assertEquals(0, u.compareTo(u));

		u = new Uri("foo:/a/");
		assertEquals(0, u.compareTo(u));

		u = new Uri("foo:/a/b");
		assertEquals(0, u.compareTo(u));
	}

	public void testCompareTo_Null() {
		
		Exception e;

		u = new Uri("");
		e = null;
		try { u.compareTo(null); } catch (NullPointerException ex) { e = ex; }
		assertNotNull(e);

		u = new Uri("/");
		e = null;
		try { u.compareTo(null); } catch (NullPointerException ex) { e = ex; }
		assertNotNull(e);
		
		u = new Uri("a");
		e = null;
		try { u.compareTo(null); } catch (NullPointerException ex) { e = ex; }
		assertNotNull(e);

		u = new Uri("file:/a");
		e = null;
		try { u.compareTo(null); } catch (NullPointerException ex) { e = ex; }
		assertNotNull(e);
	}
	
	public void testCompareTo_SameExpression() {
		
		u = new Uri("");
		assertEquals(0, u.compareTo(new Uri("")));

		u = new Uri("/");
		assertEquals(0, u.compareTo(new Uri("/")));

		u = new Uri("a");
		assertEquals(0, u.compareTo(new Uri("a")));

		u = new Uri("/a");
		assertEquals(0, u.compareTo(new Uri("/a")));

		u = new Uri("/a/b");
		assertEquals(0, u.compareTo(new Uri("/a/b")));

		u = new Uri("foo:/");
		assertEquals(0, u.compareTo(new Uri("foo:/")));

		u = new Uri("foo:/a/");
		assertEquals(0, u.compareTo(new Uri("foo:/a/")));

		u = new Uri("foo:/a/b");
		assertEquals(0, u.compareTo(new Uri("foo:/a/b")));
	}

	public void testCompareTo_DifferentExpressions() {
		
		u = new Uri("a");
		assertTrue(u.compareTo(new Uri("b")) < 0);

		u = new Uri("file:/a");
		assertTrue(u.compareTo(new Uri("file:/b")) < 0);

		u = new Uri("file:/b");
		assertTrue(u.compareTo(new Uri("foo:/a")) < 0);

		u = new Uri("/a");
		assertTrue(u.compareTo(new Uri("/a/")) < 0);

		u = new Uri("/");
		assertTrue(u.compareTo(new Uri("/a")) < 0);
	}
}
	

