package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A NetworkComponent is a convenience class for network communication between
 * two devices communicating primitive data types. A NetworkComponent manages
 * all aspects of establishing and maintaining the connection to the remote
 * device. Additionally, a NetworkComponent asynchronously reads and writes data
 * to the socket. Data to be written is given to the NetworkCompoenent, and
 * cached for a predetermined time before being sent in-bulk with all the data
 * given to it. Data read from the underlying socket is saved and can be read
 * from the NetworkComponent, with the NetworkComponent changing the value
 * provided when it receives a new value from the remote device. If the
 * connection to the remote device is lost, the last known set of values
 * continues to be provided by the NetworkComponent to the user while it
 * automatically attempts to reconnect. Upon reconnection, the provided values
 * are updated, and any backlog of outgoing transmissions is summarized and sent
 * to update the remote device.
 * 
 * @author Nicholas Contreras
 */

abstract class NetworkComponent<T extends ManagedSocket> {

	private final HashMap<String, String> outgoingData;
	private final HashMap<String, String> incomingData;

	private final HashMap<String, HashMap<String, NetworkCallback>> callbacks;
	private final Timer timer;

	private T managedSocket;

	private int sendFrequency;

	private ArrayList<Integer> pingHistory;

	NetworkComponent(T defaultManagedSocket, int sendFrequency) {
		outgoingData = new HashMap<String, String>();
		incomingData = new HashMap<String, String>();

		callbacks = new HashMap<String, HashMap<String, NetworkCallback>>();
		timer = new Timer(true);

		managedSocket = defaultManagedSocket;

		this.sendFrequency = sendFrequency;

		pingHistory = new ArrayList<Integer>();
		pingHistory.add(0);

		Thread outgoingThread = new Thread(() -> writeOutgoingData(), "Outgoing-NetworkComponent-Thread");
		outgoingThread.setDaemon(true);
		outgoingThread.start();

		Thread incomingThread = new Thread(() -> readIncomingData(), "Incoming-NetworkComponent-Thread");
		incomingThread.setDaemon(true);
		incomingThread.start();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (managedSocket.isConnected()) {
					managedSocket.write("__ping__," + System.currentTimeMillis());
				}
			}
		}, 0, 1000);
	}

	/**
	 * Sets the frequency that this NetworkComponent should update it's partner
	 * with the most up-to-date values. A lower frequency will reduce load on
	 * both the device and the network, but may result in data being less
	 * precise. More formally, the maximum amount of time between this component
	 * being told a value has changed and it sending that change on the network
	 * is (1 / sendFrequency) seconds.
	 * 
	 * @param sendFrequency
	 *            The number of times to transmit the most up-to-date values to
	 *            the network, in transmissions per second.
	 */
	public void setSendFrequency(int sendFrequency) {
		this.sendFrequency = sendFrequency;
	}

	T getManagedSocket() {
		return managedSocket;
	}

	/**
	 * Returns the frequency at which this NetworkComponent updates the remote
	 * device.
	 * 
	 * @see #setSendFrequency(int)
	 * 
	 * @return The frequency, in transmissions per second.
	 */
	public int getSendFrequency() {
		return sendFrequency;
	}

	/**
	 * Writes a string value with the given name to this NetworkComponent. The
	 * value is sent under the name given to the remote device during the next
	 * transmission.
	 * 
	 * @param name
	 *            A unique name to access the value on the remote device.
	 * @param value
	 *            The value to be sent.
	 */
	public void writeString(String name, String value) {
		writeData(name, value);
	}

	/**
	 * Writes a integer value with the given name to this NetworkComponent. The
	 * value is sent under the name given to the remote device during the next
	 * transmission.
	 * 
	 * @param name
	 *            A unique name to access the value on the remote device.
	 * @param value
	 *            The value to be sent.
	 */
	public void writeInt(String name, int value) {
		writeData(name, value + "");
	}

	/**
	 * Writes a double value with the given name to this NetworkComponent. The
	 * value is sent under the name given to the remote device during the next
	 * transmission.
	 * 
	 * @param name
	 *            A unique name to access the value on the remote device.
	 * @param value
	 *            The value to be sent.
	 */
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

	/**
	 * Reads the string form of the value for the given name received from the
	 * remote device.
	 * 
	 * @param name
	 *            The name of the value to be read.
	 * 
	 * @return The string value for the given name, or null if the value does
	 *         not exist.
	 */
	public String readString(String name) {
		return readData(name);
	}

	/**
	 * Reads the integer form of the value for the given name received from the
	 * remote device.
	 * 
	 * @param name
	 *            The name of the value to be read.
	 * 
	 * @return The integer value for the given name, or null if the value does
	 *         not exist.
	 * 
	 * @throws NumberFormatException
	 *             If the value for the given name exists but cannot be
	 *             represented as an integer.
	 */
	public int readInt(String name) {
		if (!hasValue(name)) {
			return 0;
		}

		try {
			return Integer.parseInt(readData(name));
		} catch (NumberFormatException | NullPointerException e) {
			throw new NumberFormatException("Cannot determine the integer value of data: " + readData(name));
		}
	}

	/**
	 * Reads the double form of the value for the given name received from the
	 * remote device.
	 * 
	 * @param name
	 *            The name of the value to be read.
	 * 
	 * @return The double value for the given name, or null if the value does
	 *         not exist.
	 * 
	 * @throws NumberFormatException
	 *             If the value for the given name exists but cannot be
	 *             represented as an double.
	 */
	public double readDouble(String name) {
		if (!hasValue(name)) {
			return 0;
		}

		try {
			return Double.parseDouble(readData(name));
		} catch (NumberFormatException | NullPointerException e) {
			throw new NumberFormatException("Cannot determine the double value of data: " + readData(name));
		}
	}

	private String readData(String name) {
		synchronized (incomingData) {
			return incomingData.get(name);
		}
	}

	/**
	 * Adds a value monitor to this NetworkComponent. A value monitor is a
	 * functional interface which allows the user to specify a procedure to be
	 * run when this NetworkComponent receives a change in the value for the
	 * name given.
	 * 
	 * @param valueName
	 *            The name of the value to track and report changes for.
	 * @param callbackName
	 *            A unique name for the value monitor, used to remove value
	 *            monitors.
	 * @param callback
	 *            An implementation of a functional interface specifying the
	 *            procedure to run for this callback.
	 */
	public void addValueMonitor(String valueName, String callbackName, NetworkCallback callback) {
		synchronized (callbacks) {
			if (!callbacks.containsKey(valueName)) {
				callbacks.put(valueName, new HashMap<String, NetworkCallback>());
			}

			callbacks.get(valueName).put(callbackName, callback);
		}
	}

	/**
	 * Removes the value monitor of the given name for the given value name. If
	 * a value monitor on the given value with the given name does not exist,
	 * this method does nothing.
	 * 
	 * @param valueName
	 *            The name of the value being monitored.
	 * @param callbackName
	 *            The name of the value monitor itself that will be removed.
	 * 
	 * @see #addValueMonitor(String, String, NetworkCallback)
	 */
	public void removeValueMonitor(String valueName, String callbackName) {
		synchronized (callbacks) {
			if (!callbacks.containsKey(valueName)) {
				callbacks.put(valueName, new HashMap<String, NetworkCallback>());
			}

			callbacks.get(valueName).remove(callbackName);
		}
	}

	/**
	 * Determines if this NetworkClient has received and saved a value for the
	 * name given
	 * 
	 * @param name
	 *            The name of the value to check for.
	 * @return <tt>true</tt> if the name has a value saved, <tt>false</tt> if it
	 *         has not received such a value.
	 */
	public boolean hasValue(String name) {
		return incomingData.containsKey(name);
	}

	private boolean isSendable(String string) {
		if (string == null) {
			return false;
		}
		if (string.contains("`") || string.contains(",") || string.contains(";") || string.contains("__")) {
			return false;
		}
		return true;
	}

	private void writeOutgoingData() {
		while (true) {
			long startTime = System.currentTimeMillis();

			synchronized (outgoingData) {
				for (String curName : outgoingData.keySet()) {
					managedSocket.write("`" + curName + "`,`" + outgoingData.get(curName) + "`");
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
			String incoming = managedSocket.read();
			String[] splitString = incoming.split(",");
			String name = splitString[0];
			String value = splitString[1];

			if (name.equals("__ping__")) {
				respondToPingRequest(value);
			} else if (name.equals("__pong__")) {
				updatePingHistory(value);
			} else {
				name = name.substring(name.indexOf("`") + 1, name.lastIndexOf("`"));
				value = value.substring(value.indexOf("`") + 1, value.lastIndexOf("`"));

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

	private void respondToPingRequest(String valueRecieved) {
		managedSocket.write("__pong__," + valueRecieved);
	}

	private void updatePingHistory(String valueRecieved) {
		int pingTime = (int) (System.currentTimeMillis() - Long.parseLong(valueRecieved));
		this.pingHistory.add(pingTime);

		if (this.pingHistory.size() > 10) {
			this.pingHistory.remove(0);
		}
	}

	/**
	 * Returns a boolean indicating whether the underlying socket is currently
	 * successfully connected to the remote socket.
	 * 
	 * @return <tt> true </tt> if this NetworkComponent is connected,
	 *         <tt> false </tt> otherwise.
	 */
	public boolean isConnected() {
		return this.managedSocket.isConnected();
	}

	/**
	 * Gets the network ping time for the connection between this
	 * NetworkComponent and the remote. Note that this ping time measures the
	 * speed of the underlying connection to the remote device, and does not
	 * account for the fact that a NetworkComponent may cache values for some
	 * time before transmitting them. More formally, this time is the lower
	 * bound on the delay between this NetworkComponent receiving an updated
	 * value, and the remote NetworkComponent receiving that value.
	 * 
	 * @return The ping time (in milliseconds).
	 */
	public int getPingTime() {
		int pingTime = 0;
		for (int i = 0; i < this.pingHistory.size(); i++) {
			pingTime += this.pingHistory.get(i);
		}
		return pingTime / this.pingHistory.size();
	}

	private void runCallbacksFor(String name) {
		synchronized (callbacks) {
			if (callbacks.containsKey(name)) {
				for (String curCallbackName : callbacks.get(name).keySet()) {
					timer.schedule(new TimerTask() {
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
