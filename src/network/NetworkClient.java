package network;

/**
 * A NetworkClient is a convenience class for the client side of a network communication between
 * two devices communicating primitive data types. A NetworkClient manages
 * all aspects of establishing and maintaining the connection to the remote
 * device. Additionally, a NetworkClient asynchronously reads and writes data
 * to the socket. Data to be written is given to the NetworkClient, and
 * cached for a predetermined time before being sent in-bulk with all the data
 * given to it. Data read from the underlying socket is saved and can be read
 * from the NetworkClient, with the NetworkClient changing the value
 * provided when it receives a new value from the remote {@link NetworkServer}. If the
 * connection to the NetworkServer is lost, the last known set of values
 * continues to be provided by the NetworkClient to the user while it
 * automatically attempts to reconnect. Upon reconnection, the provided values
 * are updated, and any backlog of outgoing transmissions is summarized and sent
 * to update the NetworkServer.
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
	
	public static NetworkClient getInstance() {
		return INSTANCE;
	}

	public void setAddress(String address, int port) {
		this.getManagedSocket().setAddress(address, port);
		this.targetAddress = address;
		this.targetPort = port;
	}
	
	public String getTargetAddress() {
		return targetAddress;
	}
	
	public int getTargetPort() {
		return targetPort;
	}

	private NetworkClient() {
		super(new ManagedClientSocket(DEFAULT_ADDRESS, DEFAULT_PORT), 1);
	}

}
