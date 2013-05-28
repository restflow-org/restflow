package org.restflow.features;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.restflow.WorkflowContext;
import org.restflow.WorkflowContextBuilder;
import org.restflow.actors.CloneableBean;
import org.restflow.actors.JavaActor;
import org.restflow.actors.JavaActorBuilder;
import org.restflow.actors.SubworkflowBuilder;
import org.restflow.actors.Workflow;
import org.restflow.actors.WorkflowBuilder;
import org.restflow.data.ConsumableObjectStore;
import org.restflow.data.ControlProtocol;
import org.restflow.data.DataProtocol;
import org.restflow.data.FileProtocol;
import org.restflow.data.MultiResourcePacket;
import org.restflow.data.Packet;
import org.restflow.data.Protocol;
import org.restflow.data.PublishedResource;
import org.restflow.data.SingleResourcePacket;
import org.restflow.data.StderrProtocol;
import org.restflow.data.StdoutProtocol;
import org.restflow.data.Uri;
import org.restflow.data.UriTemplate;
import org.restflow.exceptions.NullInputException;
import org.restflow.exceptions.NullOutputException;
import org.restflow.exceptions.WorkflowRuntimeException;
import org.restflow.metadata.NoopTraceRecorder;
import org.restflow.nodes.ActorNodeBuilder;
import org.restflow.nodes.InPortalBuilder;
import org.restflow.nodes.JavaNodeBuilder;
import org.restflow.nodes.WorkflowNodeBuilder;
import org.restflow.test.RestFlowTestCase;
import org.restflow.util.PortableIO;
import org.restflow.util.StdoutRecorder;
import org.restflow.util.TestUtilities;


@SuppressWarnings("unused")
public class TestNullDataSupport extends RestFlowTestCase {

	private WorkflowContext			_context;
	private WorkflowContext			_noopContext;
	private ConsumableObjectStore 	_store;
	private String 					_testRunsDirectoryPath;
	
	public void setUp() throws Exception {
		super.setUp();
		_store = new ConsumableObjectStore();
		_testRunsDirectoryPath = TestUtilities.getTestRunsDirectoryPath();
		_context = new WorkflowContextBuilder().build();
		_noopContext = new WorkflowContextBuilder()
			.recorder(new NoopTraceRecorder())
			.build();
	}
	
	public void testJavaActor_AllowedNullInputsAndOutputs() throws Exception {
	
		 Map<String,Object> nullable = new HashMap<String,Object>();
		 nullable.put("nullable", true);
		
		JavaActor actor = new JavaActorBuilder()
			.name("greatest")
			.context(_context)
			.input("a", nullable)
			.input("b", nullable)
			.bean(new Object() {
				public Integer a, b, c;
				public void step() {
					if 		(a == null && b == null) c = null;
					else if (a != null && b == null) c = a;
					else if (b != null && a == null) c = b;
					else if (a > b) 				 c = a;
					else  							 c = b;
				}	
			})
			.output("c", nullable)
			.build();

		actor.elaborate();
		actor.configure();
		actor.initialize();

		actor.set("a", 2);
		actor.set("b", 1);
		actor.step();
		assertEquals(2, actor.get("c"));

		actor.set("a", 3);
		actor.set("b", 4);
		actor.step();
		assertEquals(4, actor.get("c"));

		actor.set("a", null);
		actor.set("b", 5);
		actor.step();
		assertEquals(5, actor.get("c"));

		actor.set("a", 6);
		actor.set("b", null);
		actor.step();
		assertEquals(6, actor.get("c"));
	
		actor.set("a", null);
		actor.set("b", null);
		actor.step();
		assertNull(actor.get("c"));
	}
	
	
	
	public void testJavaActor_DisallowedNullInput() throws Exception {
		
		JavaActor actor = new JavaActorBuilder()
			.name("buffer")
			.context(_context)
			.input("a")
			.bean(new Object() {
				public Integer a, c;
				public void step() {
					c = a;
				}	
			})
			.output("c")
			.build();

		actor.elaborate();
		actor.configure();
		actor.initialize();

		actor.set("a", 2);
		actor.step();
		assertEquals(2, actor.get("c"));

		actor.set("a", 0);
		actor.step();
		assertEquals(0, actor.get("c"));
		
		Exception exception = null;
		try {
			actor.set("a", null);
		} catch(NullInputException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("Null data received on non-nullable input 'a' of actor 'buffer'", exception.getMessage());
	}
	
	
	public void testJavaActor_DisallowedNullOutput() throws Exception {
		
		 Map<String,Object> nullable = new HashMap<String,Object>();
		 nullable.put("nullable", true);
		
		JavaActor actor = new JavaActorBuilder()
			.name("buffer")
			.context(_context)
			.input("a", nullable)
			.bean(new Object() {
				public Integer a, c;
				public void step() {
					c = a;
				}	
			})
			.output("c")
			.build();

		actor.elaborate();
		actor.configure();
		actor.initialize();

		actor.set("a", 2);
		actor.step();
		assertEquals(2, actor.get("c"));

		actor.set("a", 0);
		actor.step();
		assertEquals(0, actor.get("c"));
		
		actor.set("a", null);
		
		Exception exception = null;
		try {
			actor.step();
		} catch(NullOutputException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("Null data produced on non-nullable output 'c' of actor 'buffer'", exception.getMessage());
	}
	
	
	public void testJavaActor_UnsupportedNullInput() throws Exception {
		
		 Map<String,Object> nullable = new HashMap<String,Object>();
		 nullable.put("nullable", true);
		 
		JavaActor actor = new JavaActorBuilder()
			.name("buffer")
			.context(_context)
			.input("a", nullable)
			.bean(new Object() {
				public int a, c;
				public void step() {
					c = a;
				}	
			})
			.output("c")
			.build();

		actor.elaborate();
		actor.configure();
		actor.initialize();

		actor.set("a", 2);
		actor.step();
		assertEquals(2, actor.get("c"));

		actor.set("a", 7);
		actor.step();
		assertEquals(7, actor.get("c"));
		
		actor.set("a", null);
		Exception exception = null;
		try {
			actor.step();
		} catch(WorkflowRuntimeException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("Exception setting property 'a' on actor 'buffer'", exception.getMessage());
	}
	
	public void testJavaActor_UnsupportedNullOutput() throws Exception {
		
		 Map<String,Object> nullable = new HashMap<String,Object>();
		 nullable.put("nullable", true);
		 
		JavaActor actor = new JavaActorBuilder()
			.name("buffer")
			.context(_context)
			.input("a", nullable)
			.bean(new Object() {
				public Integer a;
				public int c;
				public void step() {
					c = a;
				}	
			})
			.output("c", nullable)
			.build();

		actor.elaborate();
		actor.configure();
		actor.initialize();

		actor.set("a", 2);
		actor.step();
		assertEquals(2, actor.get("c"));

		actor.set("a", 7);
		actor.step();
		assertEquals(7, actor.get("c"));
		
		actor.set("a", null);
		Exception exception = null;
		try {
			actor.step();
		} catch(WorkflowRuntimeException e) {
			exception = e;
		}
		assertNotNull(exception);
		assertEquals("Exception in step() method of actor 'buffer'", exception.getMessage());
	}
	
	public void testPublishedResource_ThreeArgConstructorNullData() throws Exception {
		
		Uri uri = new Uri("/s");
		String nonNullStringReference = "A string";
		String nullStringReference = null;
		
		// first with non-null data
		PublishedResource nonNullDataResource = new PublishedResource(nonNullStringReference, uri, "key", false);
		assertEquals("key", nonNullDataResource.getKey());
		assertEquals("/s", nonNullDataResource.getUri().toString());
		assertEquals("A string", nonNullDataResource.getData());
		assertSame(nonNullStringReference, nonNullDataResource.getData());
		assertEquals("/s: A string", nonNullDataResource.toString());

		// now with null data
		PublishedResource nullDataResource = new PublishedResource(nullStringReference, uri, "key", false);
		assertEquals("key", nullDataResource.getKey());
		assertEquals("/s", nullDataResource.getUri().toString());
		assertNull(nullDataResource.getData());
		assertEquals("/s: null", nullDataResource.toString());
	}
	
	
	public void testPublishedResource_SingleArgConstructorNullData() throws Exception {
		
		String nonNullStringReference = "A string";
		String nullStringReference = null;
		
		// first with non-null data
		PublishedResource nonNullDataResource = new PublishedResource(nonNullStringReference);
		assertEquals("/", nonNullDataResource.getKey());
		assertEquals("/", nonNullDataResource.getUri().toString());
		assertEquals("A string", nonNullDataResource.getData());
		assertSame(nonNullStringReference, nonNullDataResource.getData());
		assertEquals("/: A string", nonNullDataResource.toString());

		// now with null data
		PublishedResource nullDataResource = new PublishedResource(nullStringReference);
		assertEquals("/", nullDataResource.getKey());
		assertEquals("/", nullDataResource.getUri().toString());
		assertNull(nullDataResource.getData());
		assertEquals("/: null", nullDataResource.toString());
	}
	
	public void testSingleResourcePacket_FourArgConstructorNullResourceData() throws Exception {
		
		Protocol dataProtocol = new DataProtocol();
		String[] metadataKeys = new String[] {};
		Object[] metadataValues = new Object[] {};
		
		Uri resourceUri = new Uri("/s");
		String nonNullResourceDataReference = "A string";
		String nullResourceDataReference = null;
		
		// first with non-null data
		PublishedResource nonNullDataResource = 
			new PublishedResource(nonNullResourceDataReference, resourceUri, "key", false);
		SingleResourcePacket packetWithNonNullDataResource = 
			new SingleResourcePacket(nonNullDataResource, dataProtocol, metadataKeys, metadataValues);
		assertSame(nonNullDataResource, packetWithNonNullDataResource.getResource());
		assertSame(nonNullDataResource, packetWithNonNullDataResource.getResource("key"));
		assertEquals("key", packetWithNonNullDataResource.getResource().getKey());
		assertEquals("/s", packetWithNonNullDataResource.getResource().getUri().toString());
		assertEquals("A string", packetWithNonNullDataResource.getResource().getData());
		assertSame(nonNullResourceDataReference, packetWithNonNullDataResource.getResource().getData());
		assertEquals("/s: A string", packetWithNonNullDataResource.getResource().toString());
		
		// now with null data
		PublishedResource nullDataResource = 
			new PublishedResource(nullResourceDataReference, resourceUri, "key", false);
		SingleResourcePacket packetWithNullDataResource = 
			new SingleResourcePacket(nullDataResource, dataProtocol, metadataKeys, metadataValues);
		assertNotNull(packetWithNullDataResource.getResource());
		assertSame(nullDataResource, packetWithNullDataResource.getResource());
		assertSame(nullDataResource, packetWithNullDataResource.getResource("key"));
		assertEquals("key", packetWithNullDataResource.getResource().getKey());
		assertEquals("/s", packetWithNullDataResource.getResource().getUri().toString());
		assertNull(packetWithNullDataResource.getResource().getData());
		assertEquals("/s: null", packetWithNullDataResource.getResource().toString());	
	}
	
	public void testSingleResourcePacket_SingleArgConstructorNullResourceData() throws Exception {
		
		String nonNullResourceDataReference = "A string";
		String nullResourceDataReference = null;
		
		// first with non-null data
		SingleResourcePacket packetWithNonNullDataResource = 
			new SingleResourcePacket(nonNullResourceDataReference);
		assertEquals("/", packetWithNonNullDataResource.getResource().getKey());
		assertEquals("/", packetWithNonNullDataResource.getResource().getUri().toString());
		assertEquals("A string", packetWithNonNullDataResource.getResource().getData());
		assertSame(nonNullResourceDataReference, packetWithNonNullDataResource.getResource().getData());
		assertEquals("/: A string", packetWithNonNullDataResource.getResource().toString());
		
		// now with null data
		SingleResourcePacket packetWithNullDataResource = 
			new SingleResourcePacket(nullResourceDataReference);
		assertNotNull(packetWithNullDataResource.getResource());
		assertEquals("/", packetWithNullDataResource.getResource().getKey());
		assertEquals("/", packetWithNullDataResource.getResource().getUri().toString());
		assertNull(packetWithNullDataResource.getResource().getData());
		assertEquals("/: null", packetWithNullDataResource.getResource().toString());	
	}

	public void testMultiResourcePacket_FourArgConstructorNullResourceData() throws Exception {
		
		Protocol dataProtocol = new DataProtocol();
		String[] metadataKeys = new String[] {};
		Object[] metadataValues = new Object[] {};
		
		String dataA = "A";
		Uri uriB = new Uri("/b");

		String dataB = null;
		Uri uriA = new Uri("/a");
		
		PublishedResource resourceA = new PublishedResource(dataA, uriA, "keyA", false);
		PublishedResource resourceB = new PublishedResource(dataB, uriB, "keyB", false);
		
		Collection<PublishedResource> resources = new LinkedList<PublishedResource>();
		resources.add(resourceA);
		resources.add(resourceB);
		
		MultiResourcePacket packet = 
			new MultiResourcePacket(resources, dataProtocol, metadataKeys, metadataValues);
		
		assertSame(resourceA, packet.getResource("keyA"));
		assertSame(resourceB, packet.getResource("keyB"));
		
		assertEquals("/a", packet.getResource("keyA").getUri().toString());
		assertEquals("/b", packet.getResource("keyB").getUri().toString());

		assertEquals("A",  packet.getResource("keyA").getData());
		assertEquals(null, packet.getResource("keyB").getData());

		assertEquals("/a: A",    packet.getResource("keyA").toString());
		assertEquals("/b: null", packet.getResource("keyB").toString());

		assertEquals(2, packet.getResources().size());
		assertTrue(packet.getResources().contains(resourceA));
		assertTrue(packet.getResources().contains(resourceB));
	}
	
	
	public void testDataProtocol_CreatePacketNullData() throws Exception {		
	
		Protocol protocol = new DataProtocol();
		protocol.setApplicationContext(_noopContext);
		
		// first with non-null data
		String dataA = "A string";
		Packet packetA = protocol.createPacket(
				dataA, 
				new Uri("/a"), 
				new UriTemplate("/a"), 
				new Object[]{},
				null);
	
		assertEquals("/a", packetA.getResource("/a").getUri().toString());
		assertEquals("/a", packetA.getResource("/a").getKey().toString());
		assertEquals("/a: A string", packetA.getResource("/a").toString());
		assertEquals("A string", packetA.getResource("/a").getData());
		assertEquals("/a: A string", protocol.getResourceSummaryLine(packetA.getResource("/a")));

		// now with non-null data
		String dataB = null;
		Packet packetB = protocol.createPacket(
				dataB, 
				new Uri("/b"), 
				new UriTemplate("/b"), 
				new Object[]{},
				null);
	
		assertEquals("/b", packetB.getResource("/b").getUri().toString());
		assertEquals("/b", packetB.getResource("/b").getKey().toString());
		assertEquals("/b: null", packetB.getResource("/b").toString());
		assertEquals(null, packetB.getResource("/b").getData());
		assertEquals("/b: null", protocol.getResourceSummaryLine(packetB.getResource("/a")));
	}

	public void testControlProtocol_CreatePacketNullData() throws Exception {		
		
		Protocol protocol = new ControlProtocol();
		protocol.setApplicationContext(_noopContext);
		
		// first with non-null data
		String dataA = "A string";
		Packet packetA = protocol.createPacket(
				dataA, 
				new Uri("/a"), 
				new UriTemplate("/a"), 
				new Object[]{},
				null);
	
		assertEquals("/a", packetA.getResource("/a").getUri().toString());
		assertEquals("/a", packetA.getResource("/a").getKey().toString());
		assertEquals("/a: A string", packetA.getResource("/a").toString());
		assertEquals("A string", packetA.getResource("/a").getData());
		assertEquals("/a: A string", protocol.getResourceSummaryLine(packetA.getResource("/a")));

		// now with non-null data
		String dataB = null;
		Packet packetB = protocol.createPacket(
				dataB, 
				new Uri("/b"), 
				new UriTemplate("/b"), 
				new Object[]{},
				null);
	
		assertEquals("/b", packetB.getResource("/b").getUri().toString());
		assertEquals("/b", packetB.getResource("/b").getKey().toString());
		assertEquals("/b: null", packetB.getResource("/b").toString());
		assertEquals(null, packetB.getResource("/b").getData());
		assertEquals("/b: null", protocol.getResourceSummaryLine(packetB.getResource("/a")));
	}
	
	public void testStdoutProtocol_CreatePacketNullData() throws Exception {		
		
		Protocol protocol = new StdoutProtocol();
		protocol.setApplicationContext(_noopContext);
		
		// first with non-null data
		String dataA = "A string";
		Packet packetA = protocol.createPacket(
				dataA, 
				new Uri("/a"), 
				new UriTemplate("/a"), 
				new Object[]{},
				null);
	
		assertEquals("/a", packetA.getResource("/a").getUri().toString());
		assertEquals("/a", packetA.getResource("/a").getKey().toString());
		assertEquals("/a: A string", packetA.getResource("/a").toString());
		assertEquals("A string", packetA.getResource("/a").getData());
		assertEquals("/a: A string", protocol.getResourceSummaryLine(packetA.getResource("/a")));

		// now with non-null data
		String dataB = null;
		Packet packetB = protocol.createPacket(
				dataB, 
				new Uri("/b"), 
				new UriTemplate("/b"), 
				new Object[]{},
				null);
	
		assertEquals("/b", packetB.getResource("/b").getUri().toString());
		assertEquals("/b", packetB.getResource("/b").getKey().toString());
		assertEquals("/b: null", packetB.getResource("/b").toString());
		assertEquals(null, packetB.getResource("/b").getData());
		assertEquals("/b: null", protocol.getResourceSummaryLine(packetB.getResource("/a")));
	}

	public void testStderrProtocol_CreatePacketNullData() throws Exception {		
		
		Protocol protocol = new StderrProtocol();
		protocol.setApplicationContext(_noopContext);
		
		// first with non-null data
		String dataA = "A string";
		Packet packetA = protocol.createPacket(
				dataA, 
				new Uri("/a"), 
				new UriTemplate("/a"), 
				new Object[]{},
				null);
	
		assertEquals("/a", packetA.getResource("/a").getUri().toString());
		assertEquals("/a", packetA.getResource("/a").getKey().toString());
		assertEquals("/a: A string", packetA.getResource("/a").toString());
		assertEquals("A string", packetA.getResource("/a").getData());
		assertEquals("/a: A string", protocol.getResourceSummaryLine(packetA.getResource("/a")));

		// now with non-null data
		String dataB = null;
		Packet packetB = protocol.createPacket(
				dataB, 
				new Uri("/b"), 
				new UriTemplate("/b"), 
				new Object[]{},
				null);
	
		assertEquals("/b", packetB.getResource("/b").getUri().toString());
		assertEquals("/b", packetB.getResource("/b").getKey().toString());
		assertEquals("/b: null", packetB.getResource("/b").toString());
		assertEquals(null, packetB.getResource("/b").getData());
		assertEquals("/b: null", protocol.getResourceSummaryLine(packetB.getResource("/a")));
	}
	
	public void testNullDataViaFileProtocol() throws Exception {
		
		// create a run directory for this test
		File runDirectory = PortableIO.createUniqueTimeStampedDirectory(
				_testRunsDirectoryPath, "testWriteFile");
		
		// create a run context specifying the run directory
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("file", new FileProtocol())
			.runDirectory(runDirectory)
			.build();
		
		Map<String,Object> nullable = new HashMap<String,Object>();
		nullable.put("nullable", true);
		
		Map<String,Object> fileType = new HashMap<String,Object>();
		fileType.put("type", "String");

		/// build the workflow
		final Workflow workflow = new WorkflowBuilder() 

			.context(context)
			
			.node(new ActorNodeBuilder()
			.actor(new JavaActorBuilder()
				.bean(new Object() {
					public String greetingOne, greetingTwo;
					public void step() {
						greetingOne = "Hello";
						greetingTwo = null;
					}})
					.output("greetingOne")
					.output("greetingTwo", nullable)
				)
				.outflow("greetingOne", "file:/greetingOne.txt")
				.outflow("greetingTwo", "file:/greetingTwo.txt")
			)
				
			.node(new ActorNodeBuilder()
				.actor(new JavaActorBuilder()
					.input("messageOne", fileType)
					.input("messageTwo", nullable)
					.bean(new Object() {
						public String messageOne, messageTwo;
						public void step() {
							System.out.println(messageOne);
							System.out.println(messageTwo);
						}
					}))
				.inflow("file:/greetingOne.txt", "messageOne")
				.inflow("file:/greetingTwo.txt", "messageTwo")
			)
				
			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		assertEquals(
				"Hello"		+ EOL +
				"null"		+ EOL, 
			recorder.getStdoutRecording());
		
		// confirm type and value of published data items
		assertTrue(_store.get("/greetingOne.txt") instanceof String);
		assertEquals("Hello", _store.take("/greetingOne.txt"));

		assertTrue(_store.containsKey("/greetingTwo.txt"));
		assertEquals(null, _store.take("/greetingTwo.txt"));
		
		// make sure nothing else was published
		assertEquals(0, _store.size());
		
		// check that the run directory was created
		assertTrue(runDirectory.exists());
		assertTrue(runDirectory.isDirectory());
		
		// get the file listing for the run directory
		File[] runFiles = runDirectory.listFiles();

		// check the names, types, and contents of the published files
		File greetingFileOne = new File(runDirectory + "/greetingOne.txt");
		assertTrue(greetingFileOne.isFile());
		assertEquals("Hello" + PortableIO.EOL, PortableIO.readTextFile(greetingFileOne));

		File greetingFileTwo = new File(runDirectory + "/greetingTwo.txt");
		assertFalse(greetingFileTwo.exists());
	}
	
	public void testNullDataIntoSubworkflow() throws Exception {
		
		// create a run directory for this test
		File runDirectory = PortableIO.createUniqueTimeStampedDirectory(
				_testRunsDirectoryPath, "testWriteFile");
		
		// create a run context specifying the run directory
		WorkflowContext context = new WorkflowContextBuilder()
			.store(_store)
			.scheme("file", new FileProtocol())
			.runDirectory(runDirectory)
			.build();
		
		Map<String,Object> nullable = new HashMap<String,Object>();
		nullable.put("nullable", true);
		
		Map<String,Object> fileType = new HashMap<String,Object>();
		fileType.put("type", "String");

		// build the workflow
		final Workflow workflow = new WorkflowBuilder() 

			.name("Top")
		
			.context(context)
			
			.node(new ActorNodeBuilder()
				.actor(new JavaActorBuilder()
					.bean(new Object() {
						public String greetingOne, greetingTwo;
						public void step() {
							greetingOne = "Hello";
							greetingTwo = null;
						}})
					.output("greetingOne")
					.output("greetingTwo", nullable)
				)
				.outflow("greetingOne", "file:/greetingOne.txt")
				.outflow("greetingTwo", "file:/greetingTwo.txt")
			)
			
			.node(new WorkflowNodeBuilder()
			
				.name("Sub")
				.prefix("/sub{STEP}")
		
				.inflow("file:/greetingOne.txt", "messageOne", "file:/messageOne.txt")
				.inflow("file:/greetingTwo.txt", "messageTwo", "file:/messageTwo.txt", nullable)
						
				.node(new ActorNodeBuilder()
					.inflow("file:/messageOne.txt", "messageOne")
					.inflow("file:/messageTwo.txt", "messageTwo")
					.actor(new JavaActorBuilder()
						.input("messageOne", fileType)
						.input("messageTwo", nullable)
						.bean(new Object() {
							public String messageOne, messageTwo;
							public void step() {
								System.out.println(messageOne);
								System.out.println(messageTwo);
							}
						})
					)
				)				
			)

			.build();
		
		workflow.configure();
		workflow.initialize();
		
		// run the workflow while capturing stdout and stderr 
		StdoutRecorder recorder = new StdoutRecorder(new StdoutRecorder.WrappedCode() {
			public void execute() throws Exception {workflow.run();}});

		assertEquals(
				"Hello"		+ EOL +
				"null"		+ EOL, 
			recorder.getStdoutRecording());
		
		// confirm type and value of published data items
		assertEquals("Hello", _store.take("/greetingOne.txt"));
		
		File messageOneFile = (File)_store.take("/sub1/messageOne.txt");
		assertEquals("Hello" + EOL, PortableIO.readTextFile(messageOneFile));
		
		assertTrue(_store.containsKey("/greetingTwo.txt"));
		assertEquals(null, _store.take("/greetingTwo.txt"));
		
		assertTrue(_store.containsKey("/sub1/messageTwo.txt"));
		assertEquals(null, _store.take("/sub1/messageTwo.txt"));

		// make sure nothing else was published
		assertEquals(0, _store.size());
		
		// check that the run directory was created
		assertTrue(runDirectory.exists());
		assertTrue(runDirectory.isDirectory());
		
		// get the file listing for the run directory
		File[] runFiles = runDirectory.listFiles();

		// check the names, types, and contents of the published files
		File greetingFileOne = new File(runDirectory + "/greetingOne.txt");
		assertTrue(greetingFileOne.isFile());
		assertEquals("Hello" + PortableIO.EOL, PortableIO.readTextFile(greetingFileOne));

		File greetingFileTwo = new File(runDirectory + "/greetingTwo.txt");
		assertFalse(greetingFileTwo.exists());
	}
}
