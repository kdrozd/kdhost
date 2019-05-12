package in.drozd.kdhost;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.neva.commons.gitignore.GitIgnore;

import in.drozd.kdhost.cliutils.KDElementTypeConverter;
import in.drozd.kdhost.exceptions.KDHostException;
import in.drozd.kdhost.utils.KDHostLogFormatter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "kdhost", // Program name
		mixinStandardHelpOptions = true, // Add build in help and version options
		description = "Remote command line interface for Host", // Description for usage information
		versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class // Version information provider
)
public class KDHostMain implements Runnable {
	// Using JUL to avoid external dependencies
	private static final String DEFAULT_LOG_LEVEL_STR = "WARNING";
	private static final Level DEFAULT_LOG_LEVEL_LEVEL = Level.WARNING;

	protected static final Logger log = Logger.getLogger(KDHostMain.class.getName());

	// For WATCH command
	private WatchService watcher = null;
	private GitIgnore gitIgnore = null;

	private Map<WatchKey, Path> keys = null;

	@Option(names = { "--verbosity", "-v" }, description = "Verbosity level, default: ${DEFAULT-VALUE}")
	String logLevel = DEFAULT_LOG_LEVEL_STR;

	public static void main(String[] args) {
		CommandLine cmd = new CommandLine(new KDHostMain());
		cmd.parseWithHandlers(new CommandLine.RunAll().andExit(0), CommandLine.defaultExceptionHandler().andExit(1),
				args);
	}

	@Override
	public void run() {
		setupLogger(this.logLevel);
	}

	@Command(description = "Send element(s) to host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void send(@Option(names = {
			"-c" }, description = "For Tables send table definition with columns. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean completeTable,
			@Parameters(paramLabel = "PATH") Path[] paths) throws Exception {

		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (Path el : paths) {
				host.sendElement(new KDHostElement(el), completeTable);
			}

		}
	}

	@Command(description = "Compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void compile(
			@Parameters(index = "0..*", description = "Files to compile", arity = "0..*", paramLabel = "ELEMENT") Path[] elements)
			throws Exception {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (Path el : elements) {
				// TODO: compile element should return string with result of the compilation
				// with printCommandResult
				host.compileElement(new KDHostElement(el));
			}

		}
	}

	@Command(description = "Drop elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void drop(@Parameters(description = "elements to drop", paramLabel = "ELEMENT") String[] elements)
			throws Exception {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (String el : elements) {
				this.printCommandResult(host.dropElement(new KDHostElement(el)));
			}

		}
	}

	@Command(name = "extract", description = "Extract environment", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class, hidden = true)
	void extractEnv(
			@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force)
			throws Exception {
		// Step 1 - Download listable elements
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			if (force) {
				host.setForceOverRide(force);
			}
			KDElementTypes.stream().filter(et -> et.isListable()).flatMap(host::streamElementsOfType).parallel()
					.forEach(host::getElement);
		}

	}

	@Command(description = "Get all elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class, hidden = true)
	void getall(
			@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force,
			@Option(names = "-r", description = "Download filer/record elements. Default value: ${DEFAULT-VALUE}", defaultValue = "false", hidden = true) boolean record,
			@Parameters(paramLabel = "ELEMENT", index = "0..*", arity = "1..*", description = "Element(s) to get from host") List<String> elements) {

		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			if (force) {
				host.setForceOverRide(force);
			}
			if (elements != null && !elements.isEmpty()) {
				elements.stream().map(s -> KDElementTypes.typeForName(s)).flatMap(host::streamElementsOfType)
						.forEach(host::getElement);
			}
		}

	}

	@Command(description = "Get elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void get(
			@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force,
			@Parameters(paramLabel = "ELEMENT", index = "0..*", arity = "1..*", description = "Element(s) to get from host") String[] elements)
			throws Exception {
		startingCommand(() -> "Get command");

		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			if (force) {
				host.setForceOverRide(force);
			}
			for (String el : elements) {
				host.getElement(new KDHostElement(el));
			}
		}
		exitingCommand(() -> "Get command");

	}

	@Command(description = "Execute psl code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class, hidden = true)
	void psl() {
		// TODO: Implement psl command and pslscript commands
	}

	@Command(description = "Execute mrpc code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void mrpc(@Option(names = {
			"--mv" }, paramLabel = "VERSION", defaultValue = "1", description = "MRPC version to use, default: ${DEFAULT-VALUE}", hidden = false) String mrpcVersion,
			@Option(names = "-r", description = "Repeat call, default: ${DEFAULT-VALUE}\"", paramLabel = "N", defaultValue = "1") int repeat,
			@Parameters(index = "0", arity = "1", description = "MRPC ID", paramLabel = "MRPC_ID") String mrpcid,
			@Parameters(index = "1..*", arity = "0..*", description = "MRPC parameters", paramLabel = "PARAMETERS") String[] parameters) {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			// TODO: callMRPC should return something that will be printed
			long start = System.currentTimeMillis();
			for (int i = 0; i < repeat; i++) {
				host.callMrpc(mrpcid, mrpcVersion, parameters);
			}

			log.fine("Exec time: " + (System.currentTimeMillis() - start));
		}

	}

	@Command(description = "List elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void list(
			@Option(names = "-a", description = "List all (supported) elements from host.", defaultValue = "false", hidden = true) boolean all,
			@Option(names = "-l", description = "List listable element types", defaultValue = "false") boolean listListableTypes,
			@Option(names = "-s", description = "List supported element types", defaultValue = "false") boolean listAllTypes,
			@Option(names = "-n", description = "Show element names", defaultValue = "false") boolean asNames,
			@Option(names = "-t", description = "List subelement of specific table.", paramLabel = "TABLE-NAME", defaultValue = "") String table,
			@Parameters(index = "0..*", arity = "0..*", description = "Element types to list from host", paramLabel = "ELEMENT-TYPES", converter = KDElementTypeConverter.class) List<KDElementTypes> elementTypes)
			throws Exception {

		startingCommand(() -> "List command");
		// Offline actions
		if (listListableTypes) {
			KDElementTypes.stream().filter(et -> et.isListable()).sorted().forEach(el -> printElementType(el, asNames));
			exitingCommand(() -> "List command");
			return;
		}

		if (listAllTypes) {
			KDElementTypes.stream().sorted().forEach(el -> printElementType(el, asNames));
			exitingCommand(() -> "List command");
			return;
		}
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			if (elementTypes != null && !elementTypes.isEmpty()) {
				elementTypes.stream().flatMap(elType -> host.streamElementsOfType(elType, table))
						.forEach(el -> printElements(el, asNames));
			} else {

				if (all) {
					KDElementTypes.stream().filter(et -> et.isListable())
							.flatMap(elType -> host.streamElementsOfType(elType))
							.forEach(el -> printElements(el, asNames));
				} else if (!table.isBlank()) {
					Stream.of(KDElementTypes.TABLE, KDElementTypes.COLUMN)
							.flatMap(elType -> host.streamElementsOfType(elType, table))
							.forEach(el -> printElements(el, asNames));

				}
			}
		}

		// This actions will use host connection
		exitingCommand(() -> "List command");

	}

	@Command(description = "Refresh elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class, hidden = true)
	void refresh(
			@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements) {
		// TODO: IMplement refresh command
	}

	@Command(description = "Execute sql code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void sql(
			@Option(names = "-s", description = "Character used to separate columns, default: ${DEFAULT-VALUE}", defaultValue = "|", paramLabel = "SEPARATOR") String separator,
			@Parameters(index = "0..*", description = "SQL query to execute", paramLabel = "SQL QUERY") String[] sqlQry) {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			this.executeQuery(host.conn, String.join(" ", sqlQry), separator);

		}
	}

	private void executeQuery(Connection connection, String sqlQueryString, String separator) {
		log.log(Level.INFO, "Executing: {0}", sqlQueryString);
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sqlQueryString)) {

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			for (int i = 1; i <= columnsNumber; i++) {
				System.out.format("%s", rsmd.getColumnName(i));
				if (i != columnsNumber)
					System.out.print(separator);
			}
			System.out.print("\n");
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					System.out.format("%s", rs.getString(i));
					if (i != columnsNumber)
						System.out.print(separator);
				}
				System.out.println("");
			}

		} catch (SQLException e) {
			log.severe("Unable to execute SQL Query: " + e.getMessage());
		}
	}

	@Command(description = "Test compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void test(
			@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements)
			throws Exception {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (Path el : elements) {
				printCommandResult(host.testElement(new KDHostElement(el)));
			}

		}
	}

	@Command(description = "Test, Send, Compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void tsc(
			@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements)
			throws Exception {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (Path file : elements) {
				// Test Compile
				KDHostElement el = new KDHostElement(file);
				try {
					host.testElement(el);
				} catch (KDHostException e) {
					// FIXME: Add logging!!!
					continue;
				}
				// Save file in env if it's fine
				try {
					host.sendElement(el, KDElementTypes.TABLE.equals(el.getElementType()));
				} catch (KDHostException e) {
					continue;
				}
				// Compile and link
				try {
					host.compileElement(el);
				} catch (KDHostException e) {
					continue;
				}
			}

		}
	}

	@Command(description = "Watch for changes and execute tsc", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void watch(@Parameters(index = "0..*", description = "Directory to watch", paramLabel = "PATH") Path folder)
			throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<>();

		log.log(Level.INFO, () -> String.format("Watcher started in: %s", folder.toAbsolutePath().normalize()));
		try (KDHost host = new KDHost(log)) {
			this.walkAndRegisterDirectories(folder);
			host.connectToHost();
			for (;;) {

				// wait for key to be signaled
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException x) {
					log.info("Stoping watching");
					Thread.currentThread().interrupt();

					return;
				}

				Path dir = keys.get(key);
				if (dir == null) {
					log.severe("Not a direcotry / not recognized!!");
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					@SuppressWarnings("rawtypes")
					WatchEvent.Kind kind = event.kind();

					// Context for directory entry event is the file name of entry
					@SuppressWarnings("unchecked")
					Path name = ((WatchEvent<Path>) event).context();
					Path child = dir.resolve(name);

					// if directory is created, and watching recursively, then register it and its
					// sub-directories
					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						try {
							if (child.toFile().isDirectory()) {
								walkAndRegisterDirectories(child);
							} else if (child.toFile().exists()) {
								processNewFile(host, child);
							}
						} catch (IOException x) {
							log.log(Level.SEVERE, () -> String.format("Unable to register watcher for: %s",
									folder.toAbsolutePath().normalize()));
							throw new KDHostException(String.format("Unable to register watcher for: %s",
									folder.toAbsolutePath().normalize()));
						}
					}
					if (kind == StandardWatchEventKinds.ENTRY_MODIFY && child.toFile().exists()) {
						// Support just file changes, ignore directories modifications
						processChangedFile(host, child);
					}
					if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
						// For delete

						if (child.toFile().exists()) {
							// process file removal
							processDeleteFile(host, child);
						}
						if (child.toFile().isDirectory()) {
							// Don't remove files from host - you don't want it.
							// If you have to use drop/remove command manually
							log.log(Level.SEVERE, "Files from {0} are not removed from host", child);

						}
					}
				}

				// reset key and remove from set if directory no longer accessible
				boolean valid = key.reset();
				if (!valid) {
					keys.remove(key);

					// all directories are inaccessible
					if (keys.isEmpty()) {
						break;
					}
				}
			}
		} catch (IOException e) {
			log.log(Level.SEVERE,
					() -> String.format("Unable to register watcher for: %s", folder.toAbsolutePath().normalize()));
			throw new KDHostException(e);
		}
	}

	private void processDeleteFile(KDHost host, Path el) {
		log.info("Deleting file from host: " + el.getFileName());
		if (gitIgnore.isExcluded(el.toAbsolutePath().normalize().toFile())) {
			log.log(Level.CONFIG, "{0} file excluded by gitignore", el);
			return;
		}
		try {
			host.drop(new KDHostElement(el));

		} catch (Exception e) {
			log.severe("Can't delete file: " + e.getMessage());
		}
	}

	private void processChangedFile(KDHost host, Path child) {
		log.log(Level.INFO, "Changed file: {0}", child);
		processNewChangedFile(host, child);
	}

	private void processNewFile(KDHost host, Path child) {
		log.log(Level.INFO, "New file: {0}", child);
		processNewChangedFile(host, child);

	}

	private void processNewChangedFile(KDHost host, Path child) {
		if (gitIgnore.isExcluded(child.toAbsolutePath().normalize().toFile())) {
			log.log(Level.CONFIG, "{0} file excluded by gitignore", child);
			return;
		}
		try {
			log.log(Level.INFO, "{0} can be compiled", child);
			this.tsc(new Path[] { child });

		} catch (Exception e) {
			log.severe("Can't process file: " + child);
		}
	}

	private void registerDirectory(Path dir) throws IOException {
		if (gitIgnore.isExcluded(dir.toAbsolutePath().normalize().toFile())) {
			log.log(Level.CONFIG, "{0} file excluded by gitignore", dir);
			return;
		}
		WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);
		keys.put(key, dir);
		log.log(Level.INFO, "Watching: {0}", dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void walkAndRegisterDirectories(final Path start) throws IOException {
		gitIgnore = new GitIgnore(start.toAbsolutePath().normalize().toFile());

		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				registerDirectory(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private void printElementType(KDElementTypes el, boolean justNames) {
		if (justNames)
			System.out.println(String.format("%s", el.name().toLowerCase()));
		else
			System.out.println(String.format("%-15s: %s", el.name().toLowerCase(), el));

	}

	private void printElements(KDHostElement element, boolean justNames) {
		if (justNames)
			System.out.println(element.getElementName());
		else
			System.out.println(element.getFileName());
	}

	private void printCommandResult(String result) {
		System.out.println(result);
	}

	private void printCommandError(String error) {
		System.err.println(error);
	}

	private void startingCommand(Supplier<String> msgSup) {
		log.log(Level.FINER, msgSup);
	}

	private void exitingCommand(Supplier<String> msgSup) {
		log.log(Level.FINER, msgSup);
	}

	private void setupLogger(String level) {
		var handlerObj = new ConsoleHandler();

		if (this.logLevel == null || "".equals(this.logLevel) || this.logLevel.isEmpty())
			this.logLevel = DEFAULT_LOG_LEVEL_STR;

		this.logLevel = this.logLevel.toUpperCase();

		if (Stream.of("INFO", "ALL", "FINE", "SEVERE", "CONFIG", "FINER", "FINEST")
				.noneMatch(el -> this.logLevel.equals(el))) {
			this.logLevel = DEFAULT_LOG_LEVEL_STR;
		}

		Level logLevelParsed = DEFAULT_LOG_LEVEL_LEVEL;
		try {
			logLevelParsed = Level.parse(this.logLevel);
		} catch (Exception e) {
			logLevelParsed = DEFAULT_LOG_LEVEL_LEVEL;
			log.throwing("KDHostMain", "setupLogger", e);
		}
		handlerObj.setLevel(logLevelParsed);
		handlerObj.setFormatter(new KDHostLogFormatter());

		for (Handler h : log.getHandlers()) {
			log.removeHandler(h);
		}

		log.addHandler(handlerObj);
		log.setLevel(logLevelParsed);
		log.setUseParentHandlers(false);
	}
}
