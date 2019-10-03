package network;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Nicholas Contreras
 */

class ManagedClientSocket extends ManagedSocket {

	private String address;
	private int port;

	ManagedClientSocket(String address, int port) {
		setAddress(address, port);
	}

	void setAddress(String address, int port) {
		this.address = address;
		this.port = port;
		forceDisconnect();
	}

	String getAddress() {
		return address;
	}

	int getPort() {
		return port;
	}

	void attemptToConnect() {
		try {
			setSocket(new Socket(address, port));
		} catch (IOException e) {
			System.err.println(
					"Unable to connect to the server at address: '" + address + "' Port: " + port + System.lineSeparator()
							+ "Check that the address and port are correct and that the server is running");
		}
	}
}
