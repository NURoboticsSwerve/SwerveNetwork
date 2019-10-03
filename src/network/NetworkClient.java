package network;

/**
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
		getManagedSocket().setAddress(address, port);
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
