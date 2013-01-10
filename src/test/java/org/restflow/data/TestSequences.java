package org.restflow.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.restflow.data.Sequences;
import org.restflow.test.RestFlowTestCase;


public class TestSequences extends RestFlowTestCase {

	private Sequences _s;
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		_s = new Sequences();
		
		Map<String, List<Object>> sequence = new HashMap();
		List<Object> s1 = new Vector<Object>();
		List<Object> s2 = new Vector<Object>();
		List<Object> s3 = new Vector<Object>();		
		
		s1.add("test s1.1");
		s1.add("test s1.2");
		s1.add("test s1.3");
		
		s2.add("test s2.1");
		s2.add("test s2.2");
		s2.add("test s2.3");
		
		s3.add("test s3.1");
		s3.add("test s3.2");
		s3.add("test s3.3");
		
		
		sequence.put("s1", s1 );
		sequence.put("s2", s2 );
		sequence.put("s3", s3 );		
		
		_s.setSequence(sequence);
		
	}
	
	public void testSequence() {

		Map<String,Object> bundle = _s.assembleNextSequenceBundle();
		assertEquals( bundle.get("s1"), "test s1.1");
		assertEquals( bundle.get("s2"), "test s2.1");
		assertEquals( bundle.get("s3"), "test s3.1");		

		bundle = _s.assembleNextSequenceBundle();
		assertEquals( bundle.get("s1"), "test s1.2");
		assertEquals( bundle.get("s2"), "test s2.2");
		assertEquals( bundle.get("s3"), "test s3.2");		
		
		bundle = _s.assembleNextSequenceBundle();		
		assertEquals( bundle.get("s1"), "test s1.3");
		assertEquals( bundle.get("s2"), "test s2.3");
		assertEquals( bundle.get("s3"), "test s3.3");				
		
		bundle = _s.assembleNextSequenceBundle();		
		assertEquals( bundle.get("s1"), null);
		assertEquals( bundle.get("s2"), null);
		assertEquals( bundle.get("s3"), null);				
	}
	

	public void testRepeatSequence() throws Exception {
		_s.setRepeatValues(true);
		_s.configure();
		
		Map<String,Object> bundle = _s.assembleNextSequenceBundle();
		assertEquals( bundle.get("s1"), "test s1.1");
		assertEquals( bundle.get("s2"), "test s2.1");
		assertEquals( bundle.get("s3"), "test s3.1");		

		bundle = _s.assembleNextSequenceBundle();
		assertEquals( bundle.get("s1"), "test s1.2");
		assertEquals( bundle.get("s2"), "test s2.2");
		assertEquals( bundle.get("s3"), "test s3.2");		
		
		bundle = _s.assembleNextSequenceBundle();		
		assertEquals( bundle.get("s1"), "test s1.3");
		assertEquals( bundle.get("s2"), "test s2.3");
		assertEquals( bundle.get("s3"), "test s3.3");				
		
		bundle = _s.assembleNextSequenceBundle();		
		assertEquals( bundle.get("s1"), "test s1.1");
		assertEquals( bundle.get("s2"), "test s2.1");
		assertEquals( bundle.get("s3"), "test s3.1");		
	}	
	
	public void testRepeatNotEqualLength() {
		Map<String, List<Object>> sequence = new HashMap();
		List<Object> s1 = new Vector<Object>();
		List<Object> s2 = new Vector<Object>();		
		s1.add("test s1.1");
		s1.add("test s1.2");
		s1.add("test s1.3");
		
		s2.add("test s2.1");
		s2.add("test s2.2");
		
		sequence.put("s1", s1 );
		sequence.put("s2", s2 );

		Sequences s = new Sequences();
		s.setRepeatValues(true);
		
		
		try {
			s.configure();
			fail("should not be able to repeat a sequence on varying length sequences");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	
}
