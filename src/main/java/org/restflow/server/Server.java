package org.restflow.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.restflow.RestFlow;


public class Server {

	static class Watchdog {
		// synchronizing access to a boolean is probably not necessary... 
		static class TimeOut {
			final static Object lock = new Object();
			static boolean timed_out;

			static void set() {
				synchronized (lock) {
					timed_out = true;
				}
			}

			static void clear() {
					synchronized (lock) {
						timed_out = false;
					}
			}

			static boolean get() {
				synchronized (lock) {
					return timed_out;
				}
			}
		} // of TimeOut
		
		static long millis;
		static Thread t = null;

		static class Waiter implements Runnable {
			public void run() {
				try {
					Thread.sleep(millis);
					TimeOut.set();
				} catch (InterruptedException e) {}
			}
		}
			
		static void start(long time_out_millis) {
			stop();

			millis = time_out_millis;
			t = new Thread(new Waiter());
			t.start();
		}

		static void stop() {
			if (t != null) {
				t.interrupt();
				try {
					t.join();
				} catch (InterruptedException e) {}
			}
			TimeOut.clear();
			t = null;
		}

		static boolean hasTimedOut() {
			return TimeOut.get();
		}
	}

	static PrintStream stdout = null;
	static PrintStream stderr = null;
	static ServerSocket serverSocket = null;

	static public void start(int port, int loop, long idle_time_out_millis)
			throws Exception {
		Socket clientSocket = null;
		if (port == 0) {
			serverSocket = bindNextFreeSocket();
			ShellVariableOutput.variableOut.addInfo(ServerOptions.env_port, "" + serverSocket.getLocalPort());
			ShellVariableOutput.variableOut.outPut(System.out);
			System.out.println("DONE");
		} else {
			try {
				serverSocket = new ServerSocket(port);
			} catch (java.io.IOException ex) {
				System.err.println("Couldn't Open Socket");
				throw ex;
			}
		}

		stdout = System.out;
		stderr = System.err;

		// loop forever if loop is set to 0 from the beginning
		boolean infinite = loop == 0 ? true : false;
		try {
			while (infinite || (loop-- != 0)) {
				DataInputStream clientctl_in = null;
				DataOutputStream clientctl_out = null;
				String[] new_args = null;
				// TODO do we want to do something with the
				// current-working-dir? Maybe, pass it as command-line parameter...
				// String cwd = "";
				Socket err_socket = null;
				Socket standard_socket = null;
				Socket standard_in_socket = null;

				try {
					Watchdog.start(idle_time_out_millis);
					if (RestFlow.DEBUG) System.out.println("Started Watchdog");
					// poll watch-dog every second
					serverSocket.setSoTimeout(1 * 1000);
					clientSocket = null;
					while (clientSocket == null) {
						try {
							clientSocket = serverSocket.accept();
							if (RestFlow.DEBUG) System.out.println("accepted connection");
						} catch (java.net.SocketTimeoutException e) {
							if (RestFlow.DEBUG) System.out.println("accept timeout");
							if (Watchdog.hasTimedOut()) {
								if (RestFlow.DEBUG) System.out.println("watchdog timeout");
								throw new StopServerException("Server stopped due to watch-dog timeout");
							}
							if (RestFlow.DEBUG) System.out.println("watchdog not timed out");
						}
					}
					Watchdog.stop(); // while executing a workflow, no dog should be watching
					if (RestFlow.DEBUG) System.out.println("Stopped Watchdog");
					
					clientctl_in = new DataInputStream(
							clientSocket.getInputStream());
					clientctl_out = new DataOutputStream(
							clientSocket.getOutputStream());

					if (clientctl_in.readInt() != ServerOptions.MAGIC_NUMBER) {
						throw new Exception(
								"Did not receive correct magic number or server secret. Exiting.");
					}

					// cwd = 
					clientctl_in.readUTF();

					int arg_length = clientctl_in.readInt();
					
					new_args = new String[arg_length];
					for (int i = 0; i < arg_length; ++i)
						new_args[i] = clientctl_in.readUTF();

					if (RestFlow.DEBUG) {
						System.out.println("I received " + arg_length + " :)");
						System.out.print("Arguments: ");
						for (String arg : new_args)
							System.out.print(arg + " ");
						System.out.println();
					}

					ServerSocket standard_s = bindNextFreeSocket();
					clientctl_out.writeInt(standard_s.getLocalPort());

					standard_socket = standard_s.accept();
					if (RestFlow.DEBUG) System.out.println("Connected standard channel");

					System.setOut(new PrintStream(standard_socket
							.getOutputStream()));
					if (RestFlow.DEBUG) System.out.println("Server: New Std-Output in effect");
					if (RestFlow.DEBUG) System.err.println("(on stderr): Server: New Std-Output in effect");

					ServerSocket standard_in_s = bindNextFreeSocket();
					clientctl_out.writeInt(standard_in_s.getLocalPort());

					standard_in_socket = standard_in_s.accept();
					if (RestFlow.DEBUG) System.out.println("Connected standard_in channel");

					System.setIn(standard_in_socket.getInputStream());

					ServerSocket err_s = bindNextFreeSocket();
					if (RestFlow.DEBUG) System.err.println("(on stderr): got Next free socket");
					clientctl_out.writeInt(err_s.getLocalPort());
					clientctl_out.flush();

					if (RestFlow.DEBUG) System.err.println("(on stderr): accept on Error port");
					err_socket = err_s.accept();
					if (RestFlow.DEBUG) System.err.println("(on stderr): Server: About to redirect STDERR as well");
					
					System.setErr(new PrintStream(err_socket
							.getOutputStream()));

					// int exit_val = main(cwd, new_args);
					// TODO: Add correct cwd handling: give as additional
					// command-line parameter
					int exit_val = 0;
					try {
						RestFlow.main(new_args);
					} catch (StopServerException e) {
						clientctl_out.writeInt(exit_val);
						clientctl_out.flush();
						clientctl_out.close();
						
						err_socket.getOutputStream().flush();
						standard_socket.getOutputStream().flush();
						err_socket.close(); err_socket = null;
						standard_socket.close(); standard_socket = null;

						throw e; // aborts the while loop
					} catch (Exception e) {
						System.err.print("Exception in thread \"main\" ");
						// System.out.println(e.getLocalizedMessage());
						e.printStackTrace();
					}

					clientctl_out.writeInt(exit_val);
					clientctl_out.flush();
					clientctl_out.close();

					err_socket.getOutputStream().flush();
					standard_socket.getOutputStream().flush();
					
					err_socket.close(); err_socket = null;
					standard_socket.close(); standard_socket = null;
					
					clientctl_in.close(); clientctl_in = null;

				} catch (java.io.IOException ex) {
					System.err.println("Errors during communication");
					ex.printStackTrace();
				} finally { // for StopServerException
					if (RestFlow.DEBUG) System.out.println("Cleaning up.");
					
					if (err_socket != null)
						try {
							System.err.flush();
							System.setErr(stdout);
							err_socket.close();
							err_socket = null;
						} catch (java.io.IOException ex) {
							System.err.println("Errors during communication");
							ex.printStackTrace();
						}
					if (standard_socket != null)
						try {
							System.out.flush();
							System.setOut(stdout);
							standard_socket.close();
						} catch (java.io.IOException ex) {
							System.err.println("Errors during communication");
							ex.printStackTrace();
						}
					if (clientctl_out != null) {
						try {
							clientctl_out.close();
						} catch (java.io.IOException ex) {
							System.err.println("Errors during communication");
							ex.printStackTrace();
						}
					}
				}
			} // END of WHILE LOOP
			RESTART(ServerOptions.server_cmd, ServerOptions.server_args);
		} catch (StopServerException e) {
			System.out.println(e.getMessage());
		}
		try {
			serverSocket.close();
			serverSocket = null;
		} catch (java.io.IOException ex) {
			System.err.println("Errors during communication");
			ex.printStackTrace();
		}
	}

	static public void RESTART(String cmd, String[] args) throws StopServerException {
		if (serverSocket != null) {
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {}
		}
		// split on spaces if cmd contains spaces without \ in front.
		String[] cmd_line_array = cmd.replaceAll("([^\\\\]) ", "$1__SPLIT_tV5AUkJ36DnlDdbN5I94_HERE__").split("__SPLIT_tV5AUkJ36DnlDdbN5I94_HERE__");
		
		ArrayList<String> exec_array = new ArrayList<String>();
					
		// String[] exec_array = new String[args.length + cmd_line_array.length];
		// int j = 0;
		for (int i = 0; i < cmd_line_array.length; ++i) {
			exec_array.add(cmd_line_array[i]);
		}
		for (int i = 0; i < args.length; ++i) {
			exec_array.add(args[i]);
			if (args[i].contentEquals("--server-secret")) {
				if ((i+1 >= args.length) || (! args[i+1].matches("[0-9]+"))) {
					System.out.println("Using current server-secret");
					exec_array.add((new Integer(ServerOptions.MAGIC_NUMBER)).toString());
				}
			}
		}

		try {
			System.out.print("Starting new Restflow server via:");
			for (String arg : exec_array) {
				System.out.print(" " + arg.replace(" ", "\\ "));
			}
			System.out.println();
			Runtime.getRuntime().exec(exec_array.toArray(new String[0]));
		} catch (IOException e1) {
			System.out.println("Exception: " + e1.getMessage());
			e1.printStackTrace();
		}
		throw new StopServerException("Server Restarted.");
	}
	
	static ServerSocket bindNextFreeSocket() {
		int port = ServerOptions.DISPOSABLE_SOCKETS;

		while (true) {
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				return serverSocket;
			} catch (java.io.IOException ex) {
			}
			port++;
		}
	}
}