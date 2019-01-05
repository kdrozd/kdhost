package in.drozd.kdhost;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class KDHostSqlException extends RuntimeException {

	public KDHostSqlException(SQLException e) {
		super(e);
	}

}
