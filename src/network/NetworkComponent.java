package network;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Nicholas Contreras
 */

abstract class NetworkComponent<T extends ManagedSocket> {

	private final HashMap<String, String> outgoingData;
	private final HashMap<String, String> incomingData;

	private final HashMap<String, HashMap<String, NetworkCallback>> callbacks;
	private final Timer callbackTimer;

	private T managedSocket;

	private int sendFrequency;

	NetworkComponent(T defaultManagedSocket, int sendFrequency) {
		outgoingData = new HashMap<String, String>();
		incomingData = new HashMap<String, String>();

		callbacks = new HashMap<String, HashMap<String, NetworkCallback>>();
		callbackTimer = new Timer(true);

		managedSocket = defaultManagedSocket;

		this.sendFrequency = sendFrequency;

		Thread outgoingThread = new Thread(() -> writeOutgoingData(), "Outgoing-NetworkComponent-Thread");
		outgoingThread.setDaemon(true);
		outgoingThread.start();

		Thread incomingThread = new Thread(() -> readIncomingData(), "Incoming-NetworkComponent-Thread");
		incomingThread.setDaemon(true);
		incomingThread.start();
	}

	public void setSendFrequency(int sendFrequency) {
		this.sendFrequency = sendFrequency;
	}

	T getManagedSocket() {
		return managedSocket;
	}

	public int getSendFrequency() {
		return sendFrequency;
	}

	public void writeString(String name, String value) {
		writeData(name, value);
	}

	public void writeInt(String name, int value) {
		writeData(name, value + "");
	}

	public void writeDouble(String name, double value) {
		writeData(name, value + "");
	}

	private void writeData(String name, String value) {

		if (!isSendable(name)) {
			throw new IllegalArgumentException("The name " + name + " is not sendable");
		}
		if (!isSendable(value)) {
			throw new IllegalArgumentException("The value " + value + " is not sendable");
		}

		synchronized (outgoingData) {
			outgoingData.put(name, value);
		}
	}

	public String readString(String name) {
		return readData(name);
	}

	public int readInt(String name) {
		if (!hasValue(name)) {
			return 0;
		}

		try {
			return Integer.parseInt(readData(name));
		} catch (NumberFormatException | NullPointerException e) {
			throw new NumberFormatException("Cannot determine the integer value of data: " + incomingData.get(name));
		}
	}

	public double readDouble(String name) {
		if (!hasValue(name)) {
			return 0;
		}

		try {
			return Double.parseDouble(readData(name));
		} catch (NumberFormatException | NullPointerException e) {
			throw new NumberFormatException("Cannot determine the double value of data: " + incomingData.get(name));
		}
	}

	private String readData(String name) {
		synchronized (incomingData) {
			return incomingData.get(name);
		}
	}

	public void addValueMonitor(String valueName, String callbackName, NetworkCallback callback) {
		synchronized (callbacks) {
			if (!callbacks.containsKey(valueName)) {
				callbacks.put(valueName, new HashMap<String, NetworkCallback>());
			}

			callbacks.get(valueName).put(callbackName, callback);
		}
	}

	public void removeValueMonitor(String valueName, String callbackName) {
		synchronized (callbacks) {
			if (!callbacks.containsKey(valueName)) {
				callbacks.put(valueName, new HashMap<String, NetworkCallback>());
			}

			callbacks.get(valueName).remove(callbackName);
		}
	}

	public boolean hasValue(String name) {
		return incomingData.containsKey(name);
	}

	public int getPing() {
		return managedSocket.getPing();
	}

	private boolean isSendable(String string) {
		if (string.contains(",") || string.contains(";") || string.contains("__")) {
			return false;
		}

		if (string.isEmpty()) {
			return false;
		}
		return true;
	}

	private void writeOutgoingData() {
		while (true) {
			long startTime = System.currentTimeMillis();

			synchronized (outgoingData) {
				for (String curName : outgoingData.keySet()) {
					managedSocket.addOutgoingValue(curName + "," + outgoingData.get(curName));
				}
			}

			long endTime = System.currentTimeMillis();
			long sleepTime = (1000 / sendFrequency) - (endTime - startTime);
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void readIncomingData() {
		while (true) {
			if (managedSocket.hasNextIncomingValue()) {
				String incoming = managedSocket.getNextIncomingValue();
				String[] splitString = incoming.split(",");
				String name = splitString[0];
				String value = splitString[1];

				synchronized (incomingData) {
					if (!value.equals(incomingData.get(name))) {
						incomingData.put(name, value);
						runCallbacksFor(name);
					}
				}
			}
			Thread.yield();
		}
	}

	private void runCallbacksFor(String name) {
		synchronized (callbacks) {
			if (callbacks.containsKey(name)) {
				for (String curCallbackName : callbacks.get(name).keySet()) {
					callbackTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							callbacks.get(name).get(curCallbackName).valueChanged();
						}
					}, 0);
				}
			}
		}
	}
}
