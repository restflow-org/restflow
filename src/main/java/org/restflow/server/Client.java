package org.restflow.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.restflow.RestFlow;

public class Client {

	final static boolean flush_per_byte = false;

	static class Pipe implements Runnable {
		InputStream in;
		OutputStream out;
		String name;

		Pipe(InputStream in, OutputStream out, String name) {
			this.in = in;
			this.out = out;
			this.name = name;
		}

		public void run() {
			int read;
			try {
				while (((read = in.read()) != -1)) {
					out.write(read);
					if (flush_per_byte)
						out.flush();
				}
				out.flush();
			} catch (java.io.IOException ex) {
					System.err.println("Errors during communication on stream " + name);
					ex.printStackTrace();
			}
			try {
				out.close();
			} catch (java.io.IOException ex) {
				System.err.println("Errors while closing " + name);
			}
		}
	}

	public static void start(String server_name, int server_port,
			String[] args) throws Exception {
		Socket server = null;
		Socket err_socket = null;
		Socket standard_socket = null;
		Socket standard_in_socket = null;

		if (server_port == 0) {
			String envPort = System.getenv(ServerOptions.env_port);
			if (envPort != null) {
				server_port = Integer.parseInt(envPort);
			} else {
				throw new Exception("Please specify the server port to "
						+ "connect to either via the environment variable "
						+ ServerOptions.env_port + " or as parameter");
			}
		}
		// connect to socket
		try {
			server = new Socket(server_name, server_port);
		} catch (java.io.IOException ex) {
			System.err.println("Could not connect to server socket");
			throw ex;
		}

		int exit_val = 255;
		try {
			DataInputStream serverctl_in = new DataInputStream(
					server.getInputStream());
			DataOutputStream serverctl_out = new DataOutputStream(
					server.getOutputStream());

			serverctl_out.writeInt(ServerOptions.MAGIC_NUMBER);

			String cwd = System.getProperty("user.dir");
			serverctl_out.writeUTF(cwd);

			serverctl_out.writeInt(args.length);
			for (String arg : args)
				serverctl_out.writeUTF(arg);

			serverctl_out.flush();

			// read port for standard-communication
			int port = serverctl_in.readInt();
			// connect to remote port
			standard_socket = new Socket("localhost", port);
			if (RestFlow.DEBUG) System.out.println("Connected standard channel");

			port = serverctl_in.readInt();
			// connect to remote port
			standard_in_socket = new Socket("localhost", port);
			if (RestFlow.DEBUG) System.out.println("Connected standard_in channel");

			int err_port = serverctl_in.readInt();
			if (RestFlow.DEBUG) System.out.println("Error port to connect to is " + err_port);
			// connect to remote port
			err_socket = new Socket("localhost", err_port);
			if (RestFlow.DEBUG) System.out.println("Connected error channel");

			// pass input to Server
			Thread in2serv = new Thread(new Pipe(System.in,
					standard_in_socket.getOutputStream(), "Clien:SysIn --> Server:standard_in_socket"));
			in2serv.start();

			// get stderr from Server
			Thread serv2err = new Thread(new Pipe(
					err_socket.getInputStream(), System.err, "Server:err_socket --> Client:StdErr"));
			serv2err.start();

			// get std_output from server
			Thread serv2std = new Thread(new Pipe(
					standard_socket.getInputStream(), System.out, "Server:standard_socket --> Client:StdOut"));
			serv2std.start();

			// Wait for being done
			exit_val = serverctl_in.readInt();

			// here we would like to destroy the client's input stream.
			// I do not know how to do so, and I thus leave it there.

			serverctl_out.close();
			serverctl_out = null;
			
			standard_in_socket.close();
			standard_in_socket = null;

			try {
				serv2err.join();
			} catch (java.lang.InterruptedException e) {
				System.out.println("Interrupted during wait for serv2err");
			}
			try {
				serv2std.join();
			} catch (java.lang.InterruptedException e) {
				System.out.println("Interrupted during wait for serv2std");
			}

			if (RestFlow.DEBUG) System.out.println("DONE");
		} catch (java.io.IOException ex) {
			System.err.println("Errors during communication");
			ex.printStackTrace();
		} finally {
			if (server != null)
				try {
					server.close();
				} catch (java.io.IOException ex) {}
			if (err_socket != null)
				try {
					err_socket.close();
				} catch (java.io.IOException ex) {}
			if (standard_socket != null)
				try {
					standard_socket.close();
				} catch (java.io.IOException ex) {}
			if (standard_in_socket != null)
				try {
					standard_in_socket.close();
				} catch (java.io.IOException ex) {}
		}
		// exiting the client
		
		// TODO: register on-exit: send interrupt; close server
		System.exit(exit_val);
	}
}