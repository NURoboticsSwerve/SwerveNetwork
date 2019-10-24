package network;

/**
 * A NetworkClient is a convenience class for the client side of a network
 * communication between two devices communicating primitive data types. A
 * NetworkClient manages all aspects of establishing and maintaining the
 * connection to the remote device. Additionally, a NetworkClient asynchronously
 * reads and writes data to the socket. Data to be written is given to the
 * NetworkClient, and cached for a predetermined time before being sent in-bulk
 * with all the data given to it. Data read from the underlying socket is saved
 * and can be read from the NetworkClient, with the NetworkClient changing the
 * value provided when it receives a new value from the remote
 * {@link NetworkServer}. If the connection to the NetworkServer is lost, the
 * last known set of values continues to be provided by the NetworkClient to the
 * user while it automatically attempts to reconnect. Upon reconnection, the
 * provided values are updated, and any backlog of outgoing transmissions is
 * summarized and sent to update the NetworkServer.
 * 
 * <br>
 * <br>
 * 
 * <b>Note that this class operates on a singleton pattern, and should not be directly
 * instantiated. Use {@link #getInstance()} to access the instance of
 * NetworkClient.</b>
 * 
 * @see NetworkServer
 * 
 * @author Nicholas Contreras
 */
public class NetworkClient extends NetworkComponent<ManagedClientSocket> {

	private static final String DEFAULT_ADDRESS = "localhost";
	private static final int DEFAULT_PORT = 12345;

	private static final NetworkClient INSTANCE = new NetworkClient();

	private String targetAddress;
	private int targetPort;

	/**
	 * Gets the NetworkClient for this device.
	 * 
	 * @return The NetworkClient.
	 */
	public static NetworkClient getInstance() {
		return INSTANCE;
	}

	/**
	 * Sets the target address and port for this NetworkClient to connect to.
	 * The address must be the internet address of the device hosting the
	 * {@link NetworkServer}, and the port must match the port number set on the
	 * NetworkServer. Calls to this method will reset the connection, and this
	 * NetworkClient will automatically being connecting to the address
	 * specified.
	 * 
	 * @param address
	 *            String form of the internet address of the remote device.
	 * @param port
	 *            The port configured on the NetworkServer to accept
	 *            connections.
	 */
	public void setAddress(String address, int port) {
		this.getManagedSocket().setAddress(address, port);
		this.targetAddress = address;
		this.targetPort = port;
	}

	/**
	 * Gets the internet address that this NetworkClient is set to connect to.
	 * 
	 * @return The configured internet address.
	 */
	public String getTargetAddress() {
		return targetAddress;
	}

	/**
	 * Gets the port that this NetworkClient is set to connect to.
	 * 
	 * @return The configured port number.
	 */
	public int getTargetPort() {
		return targetPort;
	}

	private NetworkClient() {
		super(new ManagedClientSocket(DEFAULT_ADDRESS, DEFAULT_PORT), 1);
	}

}
