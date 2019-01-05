package in.drozd.kdhost.exceptions;

import java.io.IOException;

@SuppressWarnings("serial")
public class KDHostIOException extends RuntimeException {

	public KDHostIOException(IOException e) {
		super(e);
	}

}
