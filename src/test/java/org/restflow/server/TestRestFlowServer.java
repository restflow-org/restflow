package org.restflow.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.restflow.test.system.TestRestFlow;
import org.restflow.util.PortableIO;
import org.springframework.util.Assert;



public class TestRestFlowServer extends TestRestFlow {

	static public class Server {
		int port = 0;
		int magic = 0;
		public int getPort() { return port; }
		public int getMagic() { return magic; }
		Process p;
		
		public Server(String[] cmd) throws IOException {
			System.out.println("Executing: " + Arrays.toString(cmd));
			p = Runtime.getRuntime().exec(cmd);
			init(p);
		}
	
		public Server(String cmd) throws IOException {
			System.out.println("Executing: " + cmd);
			p = Runtime.getRuntime().exec(cmd);
			init(p);
		}
		void init(Process p) throws IOException {
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			Assert.notNull(in, "Errors during executing the process");
			
			String line;
			do {
				line = in.readLine();
				Assert.notNull(line, "Cannot read output from process");
				if (line.matches("export RESTFLOW_SERVER_PORT=.*")) {
					String s = line.replaceFirst("export RESTFLOW_SERVER_PORT=", "");
					port = Integer.parseInt(s);
				}
				if (line.matches("export RESTFLOW_SECRET=.*")) {
					String s = line.replaceFirst("export RESTFLOW_SECRET=", "");
					magic = Integer.parseInt(s);
				}
			} while (! line.equals("DONE"));
			
			assertTrue(port != 0);
			assertTrue(magic != 0);
			
		}
		public void stop() throws IOException, InterruptedException {
			verifyRunRegexp(RestFlowInvocationCommand + " -c " + port + " --server-secret " + magic +
					" --server-stop", 
					"",
					"",
					"");
			PortableIO.StreamSink stdOut = new PortableIO.StreamSink(p.getInputStream());
			stdOut.run();
			
			PortableIO.StreamSink stdErr = new PortableIO.StreamSink(p.getErrorStream());
			stdErr.run();
			
			System.out.println("SERVER-OUT:" + EOL + stdOut.toString());
			System.out.println("SERVER-Err:" + EOL + stdOut.toString());
			
			p.getOutputStream().close();
		}
	}

		
	public void testServerShellsAndWatchDog() throws IOException, InterruptedException {
		verifyRunRegexp(RestFlowInvocationCommand + " -s --server-idle-timeout 1",
				  "",
				  ".*export RESTFLOW_SERVER_PORT=.*DONE.*Server stopped due to watch-dog timeout.*",
				  "");

		verifyRunRegexp(RestFlowInvocationCommand + " -s --server-idle-timeout 1 --server-shell TCSH",
				  "",
				  ".*setenv RESTFLOW_SERVER_PORT .*DONE.*Server stopped due to watch-dog timeout.*",
				  "");

		verifyRunRegexp(RestFlowInvocationCommand + " -s --server-idle-timeout 1 --server-shell WIN_CMD",
				  "",
				  ".*SET RESTFLOW_SERVER_PORT=.*DONE.*Server stopped due to watch-dog timeout.*",
				  "");
	}
	
	public void testStartStopServer() throws IOException, InterruptedException {
		// Test if start-stop gets the same port, ie if the first start releases the port correctly
		int port;
		{ Server s = new Server(RestFlowInvocationCommand + " -s --server-secret");
		  port = s.getPort();
		  System.out.println(s.getPort());
		  s.stop();
		}
		{
		  Server s = new Server(RestFlowInvocationCommand + " -s --server-secret");
		  assertEquals(port, s.getPort());
		  System.out.println(s.getPort());
		  s.stop();
		}
	}

	public void testServerExecution() throws IOException, InterruptedException {
		Server s = new Server(RestFlowInvocationCommand + " -s --server-secret");
		
		String saveExec = RestFlowInvocationCommand;
		RestFlowInvocationCommand += " -c " +  s.getPort() + " --server-secret " + s.getMagic();
		
		
		{
			testHelloWorld();
			testHelloWorld();
			testWrongInput();
			
			testClassPath();
//			try {
//				testClassPath();
//				fail("Since this is a server, the class should be loaded already!");
//			} catch (junit.framework.ComparisonFailure e) {
//				// OK. Class is loaded already :)
//			}
			
			testHelloWorldInputFromStdin();
			testHelp_OutputDetails();
			testInput();
			testTrace();
			testWrongOption();
		}
		
		RestFlowInvocationCommand = saveExec;
		s.stop();
	}
	
	public void testServerRegression() throws IOException, InterruptedException {
		Server s = new Server(RestFlowInvocationCommand + " -s --server-secret");
		
		String saveExec = RestFlowInvocationCommand;
		RestFlowInvocationCommand += " -c " +  s.getPort() + " --server-secret " + s.getMagic();
		
		testClassPath();
		for(int i = 0; i < 10; ++i) {
			testHelloWorld();
			testHelloWorld();
			testWrongInput();
			
//			try {
//				testClassPath();
//				fail("Since this is a server, the class should be loaded already!");
//			} catch (junit.framework.ComparisonFailure e) {
//				// OK. Class is loaded already :)
//			}
			
			testHelloWorldInputFromStdin();
			testHelp_OutputDetails();
			testInput();
			testTrace();
			testWrongOption();
		}
		
		RestFlowInvocationCommand = saveExec;
		s.stop();
	}

	public void testServerRestartExecution() throws IOException, InterruptedException {
		String[] cmd_exec = RestFlowInvocationCommand.replaceAll("([^\\\\]) ", "$1__SPLIT_tV5AUkJ36DnlDdbN5I94_HERE__").split("__SPLIT_tV5AUkJ36DnlDdbN5I94_HERE__");
		ArrayList<String> serverCommandLine = new ArrayList<String>();
		serverCommandLine.addAll(java.util.Arrays.asList(cmd_exec));
		serverCommandLine.addAll(java.util.Arrays.asList(
				"-s", "--server-secret", "--server-restart-name"));
		serverCommandLine.add(RestFlowInvocationCommand);

		String[] asAStringArray = new String[0];
		Server s = new Server(serverCommandLine.toArray(asAStringArray));

		String saveExec = RestFlowInvocationCommand;
		RestFlowInvocationCommand += " -c " +  s.getPort() + " --server-secret " + s.getMagic();
		
		for(int i = 0; i < 4; ++i) {
			testHelloWorld();
			testHelloWorld();
			testWrongInput();
			
			testClassPath();
//			try {
//				testClassPath();
//				fail("Since this is a server, the class should be loaded already!");
//			} catch (junit.framework.ComparisonFailure e) {
//				// OK. Class is loaded already :)
//			}
			
			verifyRunRegexp(RestFlowInvocationCommand + " --server-restart",
					"",
					".*Starting new Restflow server.*",
					"");
			Thread.sleep(300);
		}
		
		RestFlowInvocationCommand = saveExec;
		s.stop();
	}
}
