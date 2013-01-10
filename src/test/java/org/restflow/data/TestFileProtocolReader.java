package org.restflow.data;

import org.restflow.data.FileProtocol;
import org.restflow.data.Protocol;
import org.restflow.data.ProtocolReader;
import org.restflow.test.RestFlowTestCase;


public class TestFileProtocolReader extends RestFlowTestCase {

	private Protocol _fileProtocol;
	
	public void setUp() throws Exception {
				super.setUp();
		
		_fileProtocol = new FileProtocol();
	}
	
	public void testReadExternalFile() throws Exception {
		
		ProtocolReader reader = _fileProtocol.getNewProtocolReader();
		String fileContents = (String)reader.getExternalResource("src/test/resources/unit/TestSourceNode/test.txt");
		
		assertEquals("hello", fileContents);
	}

	public void testReadExternalFileTwiceWithoutReset() throws Exception {
		
		ProtocolReader reader = _fileProtocol.getNewProtocolReader();
		String fileContents = (String)reader.getExternalResource("src/test/resources/unit/TestSourceNode/test.txt");
		assertNotNull(fileContents);
		assertEquals("hello", fileContents);

		fileContents = (String)reader.getExternalResource("src/test/resources/unit/TestSourceNode/test.txt");
		assertNull(fileContents);
	}

	public void testReadExternalFileTwiceWithReset() throws Exception {
		
		ProtocolReader reader = _fileProtocol.getNewProtocolReader();
		String fileContents = (String)reader.getExternalResource("src/test/resources/unit/TestSourceNode/test.txt");
		assertNotNull(fileContents);
		assertEquals("hello", fileContents);

		reader.initialize();
		
		fileContents = (String)reader.getExternalResource("src/test/resources/unit/TestSourceNode/test.txt");
		assertNotNull(fileContents);
		assertEquals("hello", fileContents);
	}
}
