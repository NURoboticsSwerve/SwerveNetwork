package network;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Nicholas Contreras
 */

class ManagedServerSocket extends ManagedSocket {

	private ServerSocket serverSocket;

	private int port;

	ManagedServerSocket(int port) {
		setPort(port);
	}

	void setPort(int port) {
		this.port = port;
		forceDisconnect();
	}

	void attemptToConnect() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(5000);
			setSocket(serverSocket.accept());
		} catch (IOException e) {
			System.err.println("Unable to connect to the client on port: " + port + System.lineSeparator()
					+ "Check that the port is correct and that the client is running");
		}
	}
}
