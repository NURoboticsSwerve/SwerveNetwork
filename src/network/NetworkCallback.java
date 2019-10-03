package network;

import java.io.Serializable;

/**
 * @author Nicholas Contreras
 */

public interface NetworkCallback extends Serializable {
	void valueChanged();
}
