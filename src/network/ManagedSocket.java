package network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * @author Nicholas Contreras
 */

abstract class ManagedSocket {

	private Socket socket;
	private OutputStreamWriter socketWriter;
	private InputStreamReader socketReader;

	private boolean connecting;
	private boolean connected;

	void write(String data) {
		if (connected) {
			try {
				socketWriter.write(data + ";");
				socketWriter.flush();
			} catch (IOException e) {
				System.err.println("Error writing to the socket, the connection has been lost");
				connected = false;
			}
		} else {
			if (!this.connecting) {
				startConnecting();
			}
		}
	}

	String read() {
		String stringRead = "";
		try {
			while (true) {
				if (this.connected) {
					int charRead = socketReader.read();
					if (charRead == -1) {
						System.err.println("EOF read from socket, the connection has been lost");
						connected = false;
					} else if (charRead == ';') {
						break;
					} else {
						stringRead += (char) charRead;
					}
				} else {
					if (!this.connecting) {
						this.startConnecting();
					} else {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading from socket, the connection has been lost");
			connected = false;
		}
		return stringRead;
	}

	boolean isConnected() {
		return connected;
	}

	void setSocket(Socket socket) throws IOException {

		if (this.socket != null) {
			this.socket.close();
		}

		this.socket = socket;
		this.socketWriter = new OutputStreamWriter(socket.getOutputStream());
		this.socketReader = new InputStreamReader(socket.getInputStream());

		this.connected = true;
	}

	void forceDisconnect() {
		this.connected = false;
	}

	private void startConnecting() {
		this.connecting = true;
		this.attemptToConnect();
		this.connecting = false;
	}

	abstract void attemptToConnect();
}
