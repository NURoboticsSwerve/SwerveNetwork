package network;

/**
 * @author Nicholas Contreras
 */

public class NetworkServer extends NetworkComponent<ManagedServerSocket> {

	private static final int DEFAULT_PORT = 12345;

	private static final NetworkServer INSTANCE = new NetworkServer();

	private int targetPort;

	public static NetworkServer getInstance() {
		return INSTANCE;
	}

	public void setPort(int port) {
		getManagedSocket().setPort(port);
		this.targetPort = port;
	}

	public int getTargetPort() {
		return targetPort;
	}

	private NetworkServer() {
		super(new ManagedServerSocket(DEFAULT_PORT), 50);
	}
}
