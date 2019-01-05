package in.drozd.kdhost;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import in.drozd.kdhost.exceptions.KDHostSqlException;
import in.drozd.kdhost.exceptions.KDHostUnableToConnectException;

public class KDHost implements AutoCloseable {
	protected final Logger log;

	// For now they look similar but this one will support much more options later
	private static final String	SANCHEZ_URL		= String.format("protocol=jdbc:sanchez/database=%s:SCA$IBS", System.getProperty("KDHOST_HOST", "127.0.0.1:19200"));
	private static final String	FISGLOBAL_URL	= String.format("protocol=jdbc:fisglobal/database=%s:SCA$IBS", System.getProperty("KDHOST_HOST", "127.0.0.1:19200"));

	protected Connection	conn;
	private boolean			overwriteFiles	= false;

	public KDHost(Logger log) {
		this.log = log;
	}

	@Override
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			logError(() -> e.getMessage());
		}
	}

	public void connectToHost() {
		logInfo(() -> "Connecting to host");
		try {
			log.fine("Trying new driver");
			Class.forName("fisglobal.jdbc.driver.ScDriver").getDeclaredConstructor().newInstance();
			log.log(Level.CONFIG, "Connection string: {0}", FISGLOBAL_URL);

			conn = DriverManager.getConnection(FISGLOBAL_URL, System.getProperty("KDHOST_USER", "1"), System.getProperty("KDHOST_PASS", "xxx"));
		} catch (Exception e) {
			try {
				log.fine("Using fallback driver");
				Class.forName("sanchez.jdbc.driver.ScDriver").getDeclaredConstructor().newInstance();
				log.log(Level.CONFIG, "Connection string: {0}", SANCHEZ_URL);

				conn = DriverManager.getConnection(SANCHEZ_URL, System.getProperty("KDHOST_USER", "1"), System.getProperty("KDHOST_PASS", "xxx"));

			} catch (Exception e1) {
				logError(() -> e1.getMessage());
				throw new KDHostUnableToConnectException(e.getMessage());
			}
		}

		try {
			log.log(Level.CONFIG, "Driver version: {0} {1}", new String[] { conn.getMetaData().getDriverName(), conn.getMetaData().getDriverVersion() });
		} catch (SQLException e) {
			throw new KDHostSqlException(e);
		}
	}

	public void setForceOverRide(boolean force) {
		// TODO Auto-generated method stub

	}

	public void getElement(KDHostElement kdHostElement) {
		// TODO Auto-generated method stub

	}

	public void sendElement(KDHostElement kdHostElement, boolean completeTable) {
		// TODO Auto-generated method stub

	}

	public void compileElement(KDHostElement kdHostElement) {
		// TODO Auto-generated method stub

	}

	public void dropElement(KDHostElement kdHostElement) {
		// TODO Auto-generated method stub

	}

	public void testElement(KDHostElement kdHostElement) {
		// TODO Auto-generated method stub

	}

	private void logInfo(Supplier<String> msgSup) {
		log.log(Level.INFO, msgSup);
	}

	private void logError(Supplier<String> msgSup) {
		log.log(Level.SEVERE, msgSup);
	}

	public Stream<KDHostElement> streamElementsOfType(KDElementTypes elType, String table) {
		return listElements(elType, table).stream();
	}

	public Stream<KDHostElement> streamElementsOfType(KDElementTypes elType) {
		return listElements(elType, null).stream();
	}

	public List<KDHostElement> listElements(KDElementTypes elementType, String table) {
		log.log(Level.INFO, "Geting list of elements of type: {0}", elementType);

		List<KDHostElement> elements = new LinkedList<>();

		if (!elementType.isListable())
			return elements;
		// If query is invalid don't return anything = used when some DQ elements are
		// obsoleted
		if (!this.isValidTable(elementType.getTableForQuery()))
			return elements;

		final String qry = (table != null && !table.isBlank() && !elementType.getTableNameField().isBlank()) ? elementType.getQuery(table) : elementType.getQuery();
		if (!qry.isBlank()) {
			try (Statement st = conn.createStatement()) {
				try (ResultSet rs = st.executeQuery(qry)) {
					while (rs.next()) {
						elements.add(new KDHostElement(rs, elementType));
					}
				}
			} catch (SQLException ex) {
				log.log(Level.SEVERE, () -> String.format("SQL Exception %s %s", elementType, ex.getMessage()));
				throw new KDHostSqlException(ex);
			}
		}
		return elements;
	}

	private boolean isValidTable(String tableName) {
		if (tableName == null)
			return false;
		DatabaseMetaData dbm;
		try {
			dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, tableName, null);
			return tables.next();
		} catch (SQLException e) {
			return false;
		}
	}
}
