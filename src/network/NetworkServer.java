package network;

/**
 * A NetworkServer is a convenience class for the server side of a network
 * communication between two devices communicating primitive data types. A
 * NetworkServer manages all aspects of establishing and maintaining the
 * connection to the remote device. Additionally, a NetworkServer asynchronously
 * reads and writes data to the socket. Data to be written is given to the
 * NetworkServer, and cached for a predetermined time before being sent in-bulk
 * with all the data given to it. Data read from the underlying socket is saved
 * and can be read from the NetworkServer, with the NetworkServer changing the
 * value provided when it receives a new value from the remote
 * {@link NetworkClient}. If the connection to the NetworkClient is lost, the
 * last known set of values continues to be provided by the NetworkServer to the
 * user while it automatically attempts to reconnect. Upon reconnection, the
 * provided values are updated, and any backlog of outgoing transmissions is
 * summarized and sent to update the NetworkClient. <br>
 * <br>
 * 
 * <b>Note that this class operates on a singleton pattern, and should not be
 * directly instantiated. Use {@link #getInstance()} to access the instance of
 * NetworkServer.</b>
 * 
 * @see NetworkClient
 * 
 * @author Nicholas Contreras
 */

public class NetworkServer extends NetworkComponent<ManagedServerSocket> {

	private static final int DEFAULT_PORT = 12345;

	private static final NetworkServer INSTANCE = new NetworkServer();

	private int targetPort;

	/**
	 * Gets the NetworkServer for this device.
	 * 
	 * @return The NetworkServer.
	 */
	public static NetworkServer getInstance() {
		return INSTANCE;
	}

	/**
	 * Sets the port to listen on for the incoming NetworkClient connection.
	 * 
	 * @param port
	 *            An integer port number.
	 */
	public void setPort(int port) {
		getManagedSocket().setPort(port);
		this.targetPort = port;
	}

	/**
	 * Gets the port configured to listen for incoming connections from a
	 * NetworkClient.
	 * 
	 * @return The integer port number.
	 */
	public int getTargetPort() {
		return targetPort;
	}

	private NetworkServer() {
		super(new ManagedServerSocket(DEFAULT_PORT), 50);
	}
}
