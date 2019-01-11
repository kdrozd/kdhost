package in.drozd.kdhost.exceptions;

@SuppressWarnings("serial")
public class KDHostException extends RuntimeException {

	public KDHostException(String e) {
		super(e);
	}

	public KDHostException(Exception e) {
		super(e);
	}

}
