package network;

import java.io.Serializable;

/**
 * A functional interface used to provide a procedure to run when a value saved
 * by a {@link NetworkComponent} is changed.
 * 
 * @see NetworkComponent#addValueMonitor(String, String, NetworkCallback)
 * 
 * @author Nicholas Contreras
 */
public interface NetworkCallback extends Serializable {
	/**
	 * Callback method invoked by the NetworkComponent it is registered to when
	 * the monitored value has been changed by the remote device.
	 * 
	 * @see NetworkComponent#addValueMonitor(String, String, NetworkCallback)
	 */
	void valueChanged();
}
