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
import java.util.Collections;
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
	private static final String CRLF = "\r\n";

	protected final Logger log;

	// For now they look similar but this one will support much more options later
	private static final String	SANCHEZ_URL		= String.format("protocol=jdbc:sanchez/database=%s:SCA$IBS",
			System.getProperty("KDHOST_HOST", "127.0.0.1:49200"));
	private static final String	FISGLOBAL_URL	= String.format("protocol=jdbc:fisglobal/database=%s:SCA$IBS",
			System.getProperty("KDHOST_HOST", "127.0.0.1:49200"));

	private static final String EMPTY = "";

	private static final String SUCCESS = "Success";

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

			conn = DriverManager.getConnection(FISGLOBAL_URL, System.getProperty("KDHOST_USER", "1"),
					System.getProperty("KDHOST_PASS", "xxx"));
		} catch (Exception e) {
			try {
				log.fine("Using fallback driver");
				Class.forName("sanchez.jdbc.driver.ScDriver").getDeclaredConstructor().newInstance();
				log.log(Level.CONFIG, "Connection string: {0}", SANCHEZ_URL);

				conn = DriverManager.getConnection(SANCHEZ_URL, System.getProperty("KDHOST_USER", "1"),
						System.getProperty("KDHOST_PASS", "xxx"));

			} catch (Exception e1) {
				logError(() -> e1.getMessage());
				throw new KDHostUnableToConnectException(e1.getMessage());
			}
		}

		try {
			log.log(Level.CONFIG, "Driver version: {0} {1}",
					new String[] { conn.getMetaData().getDriverName(), conn.getMetaData().getDriverVersion() });
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
		Optional<String> returnValue = null;
		try {
			returnValue = this.initObj(e).flatMap(this::retObj);

		} catch (KDHostSqlException exc) {
			log.severe("Host Communitation problem: " + exc.getMessage());
			return Optional.empty();
		}
		return returnValue;
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

		final Optional<String> mrpcResponse = mrpc121(KDMRPC121Requests.INITOBJ, EMPTY, EMPTY, EMPTY,
				objid.getElementType().typeDescription(), objid.getElementName(), EMPTY, EMPTY);

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
			log.log(Level.FINER, () -> String.format("RETURN %s", Arrays.deepToString(response.split(KDHost.CRLF))));
			return mrpcResponse.map(resp -> resp.split(KDHost.CRLF)[1]);
		}

		// Nothing to return
		return Optional.empty();

	}

	private Optional<String> mrpc121(KDMRPC121Requests request, String code, String cmpTok, String lockFile,
			String objType, String objid, String token, String user) {
		log.entering("KDhost", "mrpc121",
				new String[] { request.name(), code, cmpTok, lockFile, objType, objid, token, user });

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

	// TODO: Refactor initCode to return Optional of String
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
					.orElseThrow(() -> new KDHostException(
							String.format("Empty token returned for %s", KDMRPC121Requests.INITCODE)));
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
		return mrpc121(KDMRPC121Requests.SAVEOBJ, EMPTY, EMPTY, type, EMPTY, EMPTY, token,
				System.getProperty("user.name", "unkown user"))
						.orElseThrow(() -> new KDHostException(
								String.format("Empty response returned for %s", KDMRPC121Requests.SAVEOBJ)));

	}

	private String checkObj(String fileName, String token) {
		return mrpc121(KDMRPC121Requests.CHECKOBJ, EMPTY, EMPTY, fileName, EMPTY, EMPTY, token, EMPTY).orElseThrow(
				() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.CHECKOBJ)));
	}

	/*
	 * Returns list of PSL build in classes
	 */
	public String[] getPslCls() {
		return mrpc121(KDMRPC121Requests.GETPSLCLS, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY)
				.orElseGet(() -> EMPTY).split(",");
	}

	/*
	 * Returns list of MUMPS functions supported by PSL, currently only $select
	 */
	public String[] getPslFw() {
		return mrpc121(KDMRPC121Requests.GETPSLFW, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY)
				.orElseGet(() -> EMPTY).split(",");
	}

	/*
	 * Returns list of PSL keywords and compiler directoves
	 */
	public String[] getPslKw() {
		return mrpc121(KDMRPC121Requests.GETPSLKW, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY)
				.orElseGet(() -> EMPTY).split(",");
	}

	/*
	 * Returns JSON like list of supported features
	 */
	public String[] getFwkFtrs() {
		return mrpc121(KDMRPC121Requests.GETFWKFTRS, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY)
				.orElseGet(() -> EMPTY).split(",");
	}

	private String preCompileCheck(String elementName) {
		return mrpc121(KDMRPC121Requests.PRECMP, EMPTY, EMPTY, elementName, EMPTY, EMPTY, EMPTY, EMPTY).orElseThrow(
				() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.PRECMP)));

	}

	private String cmpLink(String cmpTok) {
		return mrpc121(KDMRPC121Requests.CMPLINK, EMPTY, cmpTok, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY).orElseThrow(
				() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.CMPLINK)));

	}

	/*
	 * Will send command to host to drop/remove (if posible) element form host
	 * 
	 * Local copy stays. This is not supported in all host versions (before 76?)
	 */
	public String drop(KDHostElement element) {
		log.info(() -> String.format("Droping: %s", element));

		String result = mrpc121(KDMRPC121Requests.DROPOBJ, "", "", element.getFileName(), EMPTY, EMPTY, EMPTY,
				System.getProperty("user.name", "unkown user"))
						.orElseThrow(() -> new KDHostException(
								String.format("Empty response returned for %s", KDMRPC121Requests.DROPOBJ)));

		if (!"1".equals(result)) {
			throw new KDHostException(result.substring(2));
		}
		return "Drop sucessful";
	}

	public void sendElement(KDHostElement el, boolean completeTable) {

		// send to host in loop
		String token = initCode(el.getFilePath());

		final String info = checkObj(el.getFileName(), token);
		if (!info.startsWith("1")) {
			log.severe(info);
			throw new KDHostException("Unable to save file on host: ");

		} else {
			log.log(Level.INFO, () -> String.format("%s", info.substring(3)));
		}
		// change name of temporary file to original and load
		String retVal = saveObj(el.getFileName(), token);
		if (!retVal.startsWith("1"))
			throw new KDHostException("Unable to save file on host");
	}

	public void compileElement(KDHostElement el) {
		if (el.getElementType().canCompile()) {
			log.info("Starting compilation of: " + el.getElementName());
			String cmpResult;
			if (!KDElementTypes.BATCH.equals(el.getElementType())) {
				final String cmpTok = preCompileCheck(el.getFileName());
				if (cmpTok.startsWith("0"))
					throw new KDHostException(cmpTok.substring(2));
				else
					log.log(Level.FINEST, "Compilation token/information: {0}", cmpTok);
				cmpResult = this.cmpLink(cmpTok);
				log.log(Level.INFO, "Compilation result: {0}", cmpResult.substring(2));
			} else {
				// Custom support for batches
				mrpc081("DBTBL33", el.getElementName()).ifPresent(
						result -> log.exiting("KDHost", "mrpc081", result.isEmpty() ? KDHost.SUCCESS : result));

			}
		} else {
			log.warning("Element can't be compiled");
		}
	}

	private Optional<String> mrpc081(String table, String element) {

		try (CallableStatement cstatmt1 = conn.prepareCall("{call mrpc(81,?,?,?)}")) {
			cstatmt1.setString(1, table); // REQUEST
			cstatmt1.setString(2, element); // CODE

			cstatmt1.registerOutParameter(3, Types.VARCHAR, "CODE");
			try (ResultSet rs1 = cstatmt1.executeQuery()) {
				if (!rs1.next()) {
					return Optional.of(KDHost.SUCCESS);
				} else {
					throw new KDHostSqlException(String.format("Unable to compile %s", rs1.getString("CODE")));
				}
			}
		} catch (SQLException e) {
			throw new KDHostSqlException(e);
		}
	}

	public String dropElement(KDHostElement element) {
		log.info(() -> String.format("Droping: %s", element));

		String result = mrpc121(KDMRPC121Requests.DROPOBJ, EMPTY, EMPTY, element.getFileName(), EMPTY, EMPTY, EMPTY,
				System.getProperty("user.name", "unkown user"))
						.orElseThrow(() -> new KDHostException(
								String.format("Empty response returned for %s", KDMRPC121Requests.DROPOBJ)));

		if (!"1".equals(result)) {
			return result.substring(2);
		}
		return "Drop sucessful";
	}

	public String testElement(KDHostElement el) {
		logInfo(() -> "Test compile ");

		if (el.getElementType().canCompile()) {
			log.info("Test compile of: " + el.getFileName());
			final String cmpTok = initCode(el.getFilePath());
			final String testCompileResult = this.execComp(el.getFileName(), cmpTok);
			if (!testCompileResult.contains("%PSL-I-LIST: 0 errors, 0 warnings, 0 informational messages")) {
				throw new KDHostException(testCompileResult);
			}
			log.info(testCompileResult);
			return testCompileResult;
		} else {
			throw new KDHostException("This element type is not supporting test compile");
		}
	}

	private String execComp(String fileName, String cmpTok) {
		return mrpc121(KDMRPC121Requests.EXECCOMP, EMPTY, cmpTok, fileName, EMPTY, EMPTY, EMPTY, EMPTY).orElseThrow(
				() -> new KDHostException(String.format("Empty response returned for %s", KDMRPC121Requests.EXECCOMP)));

	}

	public void callMrpc(String mrpcid, String mrpcVersion, String[] mrpcParameters) {
		/*
		 * Make generic call to any MRPC and display results/error.
		 * 
		 * There is still some work missing to make this code clean... in next version
		 * :)
		 */
		log.entering(KDHost.class.getName(), "callMrpc",
				new Object[] { mrpcid, mrpcVersion, Arrays.deepToString(mrpcParameters) });

		String response = "";
		String errors = "";
		int numberOfParameters = mrpcParameters.length + 1; // +1 is for response parameter
		final String mrpcCallString = "{call mrpc(" + mrpcid
				+ String.join("", Collections.nCopies(numberOfParameters, ",?")) + ")}";

		try (CallableStatement cs = conn.prepareCall(mrpcCallString);) {

			for (int i = 1; i < numberOfParameters; i++) {
				cs.setString(i, mrpcParameters[i - 1]);
			}
			cs.registerOutParameter(numberOfParameters, Types.VARCHAR, "KDRPCXRESPONSE");
			try (ResultSet rs1 = cs.executeQuery()) {
				while (rs1.next()) {
					response = rs1.getString("KDRPCXRESPONSE");
				}
			}
		} catch (SQLException e) {
			response = "";
			log.severe(e.getMessage());
			errors = e.getMessage();
		} finally {
			// FIXME: Move this out, and return touple with response in client class

			// This is output not logs
			System.out.println("RESPONSE: " + response);
			if (!errors.isEmpty()) {
				// This is output not logs
				System.err.println("ERROR   " + errors);
			}
		}
		log.exiting(KDHost.class.getName(), "callMrpc");

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

		final String qry = (table != null && !table.isBlank() && !elementType.getTableNameField().isBlank())
				? elementType.getQuery(table)
				: elementType.getQuery();
		if (!qry.isBlank()) {
			try (Statement st = conn.createStatement()) {
				try (ResultSet rs = st.executeQuery(qry)) {
					while (rs.next()) {
						KDHostElement he = new KDHostElement(rs, elementType);
						if (!he.isLiteralColumnName()) {
							elements.add(he);
						} else {
							log.fine("Skipping literal column: " + he.getFileName());
						}
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
