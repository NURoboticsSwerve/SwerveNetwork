package network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Nicholas Contreras
 */

public abstract class ManagedSocket {

	private Socket socket;
	private OutputStreamWriter socketWriter;
	private InputStreamReader socketReader;

	private final ArrayList<String> outputBuffer;
	private final ArrayList<String> inputBuffer;

	private final ArrayList<Integer> pingHistory;

	private String inputAccumulator;

	private boolean connected;

	ManagedSocket() {
		outputBuffer = new ArrayList<String>();
		inputBuffer = new ArrayList<String>();

		pingHistory = new ArrayList<Integer>();
		pingHistory.add(0);

		inputAccumulator = "";

		Thread thread = new Thread(() -> update(), "Managed-Server-Socket-Thread");
		thread.setDaemon(true);
		thread.start();

		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				addOutgoingValue("__ping," + System.currentTimeMillis());
			}
		}, 0, 250);
	}

	private void update() {
		while (true) {
			if (connected) {
				try {
					if (socketReader.ready()) {
						int curChar = socketReader.read();

						if (curChar == -1) {
							connected = false;
						} else if (curChar == ';') {
							processInputAccumulator();
						} else {
							inputAccumulator += (char) curChar;
						}
					}

					synchronized (inputBuffer) {
						if (!inputBuffer.isEmpty()) {
							String messageToSend = inputBuffer.remove(0);
							socketWriter.write(messageToSend + ";");
							socketWriter.flush();
						}
					}

				} catch (IOException e) {
					System.err.println("Error using the socket, the connection has been lost");
					connected = false;
				}
			} else {
				attemptToConnect();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Thread.yield();
		}
	}

	private void processInputAccumulator() {

		if (inputAccumulator.startsWith("__ping,")) {
			String resp = "__pong," + inputAccumulator.substring(inputAccumulator.indexOf(",") + 1);
			addOutgoingValue(resp);
		} else if (inputAccumulator.startsWith("__pong,")) {
			long time = Long.parseLong(inputAccumulator.substring(inputAccumulator.indexOf(",") + 1));
			int diff = (int) (System.currentTimeMillis() - time);
			addValueToPingHistory(diff);
		} else {
			synchronized (outputBuffer) {
				outputBuffer.add(inputAccumulator);
			}
		}
		inputAccumulator = "";
	}

	private void addValueToPingHistory(int ping) {

		pingHistory.add(ping);

		if (pingHistory.size() > 10) {
			pingHistory.remove(0);
		}
	}

	int getPing() {
		int sum = 0;

		for (int i = 0; i < pingHistory.size(); i++) {
			sum += pingHistory.get(i);
		}

		return sum / pingHistory.size();
	}

	void setSocket(Socket socket) throws IOException {

		if (this.socket != null) {
			this.socket.close();
		}

		this.socket = socket;
		this.socketWriter = new OutputStreamWriter(socket.getOutputStream());
		this.socketReader = new InputStreamReader(socket.getInputStream());

		synchronized (outputBuffer) {
			this.outputBuffer.clear();
		}

		synchronized (inputBuffer) {
			this.inputBuffer.clear();
		}
		this.connected = true;
	}

	void addOutgoingValue(String message) {
		synchronized (inputBuffer) {
			inputBuffer.add(message);
		}
	}

	boolean hasNextIncomingValue() {
		synchronized (outputBuffer) {
			return !outputBuffer.isEmpty();
		}
	}

	String getNextIncomingValue() {
		synchronized (outputBuffer) {
			return outputBuffer.remove(0);
		}
	}

	boolean isConnected() {
		return connected;
	}

	void forceDisconnect() {
		this.connected = false;
	}

	abstract void attemptToConnect();
}
