package in.drozd.kdhost;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import in.drozd.kdhost.exceptions.KDHostException;
import in.drozd.kdhost.exceptions.KDHostSqlException;
import in.drozd.kdhost.exceptions.KDHostUnableToConnectException;
import in.drozd.kdhost.exceptions.KDHostUnsupportedOperation;
import in.drozd.kdhost.utils.KDFileUtils;

public class KDHost implements AutoCloseable {
	protected final Logger log;

	// For now they look similar but this one will support much more options later
	private static final String	SANCHEZ_URL		= String.format("protocol=jdbc:sanchez/database=%s:SCA$IBS", System.getProperty("KDHOST_HOST", "127.0.0.1:19200"));
	private static final String	FISGLOBAL_URL	= String.format("protocol=jdbc:fisglobal/database=%s:SCA$IBS", System.getProperty("KDHOST_HOST", "127.0.0.1:19200"));

	private static final String EMPTY = "";

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
		this.overwriteFiles = force;
	}

	public void getElement(KDHostElement e) {
		log.info(() -> String.format("Getting element: %s", e));
		if (!this.overwriteFiles && e.getFilePath().toFile().exists()) {
			log.log(Level.WARNING, "{0} element exists localy, will not be overwriten", e);
			return;
		}
		getFromHost(e).ifPresent(conent -> saveToFile(e.getFilePath(), conent));

	}

	private Optional<String> getFromHost(KDHostElement e) {
		return this.initObj(e).flatMap(this::retObj);
	}

	private Optional<String> retObj(String token) {
		StringBuilder sb = new StringBuilder();

		Optional<String> response;
		do {
			response = mrpc121(KDMRPC121Requests.RETOBJ, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, token, EMPTY);

			response.map(resp -> resp.substring(1)).ifPresent(sb::append);

		} while (response.orElseGet(() -> EMPTY).startsWith("1"));

		return Optional.of(sb.toString());
	}

	private Optional<String> initObj(KDHostElement objid) {

		final Optional<String> mrpcResponse = mrpc121(KDMRPC121Requests.INITOBJ, EMPTY, EMPTY, EMPTY, objid.getElementType().typeDescription(), objid.getFileName(), EMPTY, EMPTY);

		if (mrpcResponse.isPresent()) {
			final String response = mrpcResponse.get();
			// 0_CRLF_MSG - error on host
			if (response.startsWith("0")) {
				/*
				 * Usually - geting literal column
				 */
				log.log(Level.WARNING, () -> response.substring(2).trim());
				return Optional.empty();
			}

			if (!response.startsWith("1")) {
				return Optional.empty();
			}
			// It was fine. 1 _ CRLF _ token CRLF file name
			log.log(Level.FINER, () -> String.format("RETURN %s", Arrays.deepToString(response.split("\r\n"))));
			return mrpcResponse.map(resp -> resp.split("\r\n")[1]);
		}

		// Nothing to return
		return Optional.empty();

	}

	private Optional<String> mrpc121(KDMRPC121Requests request, String code, String cmpTok, String lockFile, String objType, String objid, String token, String user) {
		log.entering("KDhost", "mrpc121", new String[] { request.name(), code, cmpTok, lockFile, objType, objid, token, user });

		try (CallableStatement cstatmt1 = conn.prepareCall("{call mrpc(121,?,?,?,?,?,?,?,?,?)}");) {
			cstatmt1.setString(1, request.name()); // REQUEST
			cstatmt1.setString(2, code); // CODE
			cstatmt1.setString(3, cmpTok); // CMPTOK
			cstatmt1.setString(4, lockFile); // LOCKFILE
			cstatmt1.setString(5, objType); // OBJTYPE
			cstatmt1.setString(6, objid); // OBJID
			cstatmt1.setString(7, token); // TOKEN
			cstatmt1.setString(8, user); // USER

			cstatmt1.registerOutParameter(9, Types.VARCHAR, "CODE");

			try (ResultSet rs1 = cstatmt1.executeQuery()) {
				while (rs1.next()) {
					return Optional.ofNullable(rs1.getString("CODE"));
				}
				throw new KDHostUnsupportedOperation("MRPC121 Exception");

			}

		} catch (SQLException e1) {
			throw new KDHostSqlException(e1);
		}
	}

	private void saveToFile(Path procFile, String fileContent) {
		KDFileUtils.createDirectoryIfNotExists(procFile.getParent());
		try (FileWriter writer = new FileWriter(procFile.toFile());) {
			writer.write(fileContent);
			log.finer("Content saved as: " + procFile.toFile().getAbsoluteFile());
		} catch (IOException e1) {
			throw new KDHostException("Cant write file on local drive " + procFile + " " + e1.getMessage());
		}
	}

	private String initCode(byte[] fileContent) {

		// number of chunks has performance impact
		final byte[][] chunked = chunk(fileContent, 270);

		String token = "";
		for (int i = 0; i < chunked.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (byte b : chunked[i]) {
				sb.append(String.valueOf(b));
				sb.append("|");
			}
			token = mrpc121(KDMRPC121Requests.INITCODE, sb.toString(), token, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY)
					.orElseThrow(() -> new KDHostException(String.format("Empty token returned for %s", KDMRPC121Requests.INITCODE)));
		}

		return token;
	}

	private String initCode(Path fileToSend) {

		try {
			byte[] fileContent = Files.readAllBytes(fileToSend);
			return initCode(fileContent);
		} catch (IOException e) {
			throw new KDHostException("Can't read input file");
		}

	}

	protected byte[][] chunk(byte[] input, int chunkSize) {
		final int inputLength = input.length;
		final int numberOfChunks = (int) Math.ceil((double) inputLength / chunkSize);

		byte[][] result = new byte[numberOfChunks][];
		for (int i = 0; i < numberOfChunks; i++) {
			result[i] = Arrays.copyOfRange(input, i * chunkSize, Math.min((i + 1) * chunkSize, inputLength));
		}
		return result;
	}

	private String saveObj(String type, String token) {
		return mrpc121(KDMRPC121Requests.SAVEOBJ, EMPTY, EMPTY, type, EMPTY, EMPTY, token, System.getProperty("user.name", "unkown user"))
				.orElseThrow(() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.SAVEOBJ)));

	}

	private String checkObj(String fileName, String token) {
		return mrpc121(KDMRPC121Requests.CHECKOBJ, EMPTY, EMPTY, fileName, EMPTY, EMPTY, token, EMPTY)
				.orElseThrow(() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.CHECKOBJ)));
	}

	/*
	 * Returns list of PSL build in classes
	 */
	public String[] getPslCls() {
		return mrpc121(KDMRPC121Requests.GETPSLCLS, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY).orElseGet(() -> EMPTY).split(",");
	}

	/*
	 * Returns list of MUMPS functions supported by PSL, currently only $select
	 */
	public String[] getPslFw() {
		return mrpc121(KDMRPC121Requests.GETPSLFW, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY).orElseGet(() -> EMPTY).split(",");
	}

	/*
	 * Returns list of PSL keywords and compiler directoves
	 */
	public String[] getPslKw() {
		return mrpc121(KDMRPC121Requests.GETPSLKW, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY).orElseGet(() -> EMPTY).split(",");
	}

	/*
	 * Returns JSON like list of supported features
	 */
	public String[] getFwkFtrs() {
		return mrpc121(KDMRPC121Requests.GETFWKFTRS, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY).orElseGet(() -> EMPTY).split(",");
	}

	private String preCompileCheck(String elementName) {
		return mrpc121(KDMRPC121Requests.PRECMP, EMPTY, EMPTY, elementName, EMPTY, EMPTY, EMPTY, EMPTY)
				.orElseThrow(() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.PRECMP)));

	}

	private String cmpLink(String cmpTok) {
		return mrpc121(KDMRPC121Requests.CMPLINK, EMPTY, cmpTok, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY)
				.orElseThrow(() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.CMPLINK)));

	}

	/*
	 * Will send command to host to drop/remove (if posible) element form host
	 * 
	 * Local copy stays. This is not supported in all host versions (before 76?)
	 */
	public String drop(KDHostElement element) {
		log.info(() -> String.format("Droping: %s", element));

		String result = mrpc121(KDMRPC121Requests.DROPOBJ, "", "", element.getFileName(), EMPTY, EMPTY, EMPTY, System.getProperty("user.name", "unkown user"))
				.orElseThrow(() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.DROPOBJ)));

		if (!"1".equals(result)) {
			throw new KDHostException(result.substring(2));
		}
		return "Drop sucessful";
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
