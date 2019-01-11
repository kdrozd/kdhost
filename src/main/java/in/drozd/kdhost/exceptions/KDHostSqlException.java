package in.drozd.kdhost.exceptions;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class KDHostSqlException extends RuntimeException {

	public KDHostSqlException(SQLException e) {
		super(e);
	}

	public KDHostSqlException(String e) {
		super(e);

	}

}
