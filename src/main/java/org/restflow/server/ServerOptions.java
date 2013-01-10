package org.restflow.server;

import org.restflow.server.ShellVariableOutput.ShellType;

import joptsimple.OptionSet;

public class ServerOptions {

	public final static String env_port = "RESTFLOW_SERVER_PORT";
	public static String[] server_args;
	public static String server_cmd = "RestFlow"; 
	public final static String env_secret = "RESTFLOW_SECRET";
	public static int MAGIC_NUMBER = 424342; // is used to implement server-secret protocol 
	final static int DISPOSABLE_SOCKETS = 12345;

	static boolean running_as_server = false;

	public static boolean handleServerOptions(String[] args, OptionSet options) throws Exception {
		
		try {
			ShellVariableOutput.variableOut.setShell(ShellType.valueOf((String) options.valueOf("server-shell")));
		} catch (java.lang.IllegalArgumentException e) {
			System.out.print("ERROR: Parameter to server-shell should be one of");
			for (ShellType t : ShellType.values()) {
				System.out.print(" " + t.toString());
			}
			System.out.println(".");
			return true;
		}

		if (options.hasArgument("server-restart-name")) {
			server_cmd = (String)options.valueOf("server-restart-name");
		}
		
		if (options.has("s")) {
			if (running_as_server) {
				throw new Exception("Cannot run a server inside a server");
			}
			running_as_server = true;
			server_args = args;
			if (options.has("server-secret")) {
				if (options.hasArgument("server-secret")) {
					MAGIC_NUMBER = (Integer) options
							.valueOf("server-secret");
				} else {
					MAGIC_NUMBER = java.lang.Math
							.abs((new java.util.Random()).nextInt());
				}
				ShellVariableOutput.variableOut.addInfo(env_secret, "" + MAGIC_NUMBER);
			}
			int loop;
			if (options.has("server-loop")) {
				loop = (Integer) options.valueOf("server-loop");
			} else {
				loop = 0;
			}
			Server.start(
					(Integer) options.valueOf("s"),
					loop,
					(new Long((Integer) options
							.valueOf("server-idle-timeout"))) * 1000L);
			return true;
		}

		if (options.has("c") && (!running_as_server)) {
			if (options.has("server-secret")
					|| (System.getenv(env_secret) != null)) {
				if (options.hasArgument("server-secret")) {
					MAGIC_NUMBER = (Integer) options
							.valueOf("server-secret");
				} else {
					String mNumber = System.getenv(env_secret);
					if (mNumber != null) {
						MAGIC_NUMBER = Integer.parseInt(mNumber);
					} else {
						throw new Exception(
								"Please specify the server secret either via the environment variable "
										+ env_secret + " or as parameter");
					}
				}
			}
			Client.start((String) options.valueOf("server-name"),
					(Integer) options.valueOf("c"), args);
			return true;
		} // else run locally. This means either without -c, 
		  // or we are a server and run a client request

		if (options.has("server-stop") && running_as_server) {
			throw new StopServerException("Server Stopped");
		}

		if (options.has("server-restart") && running_as_server) {
			Server.RESTART(server_cmd, server_args);
		}
		
		return false;
	}
	
}
