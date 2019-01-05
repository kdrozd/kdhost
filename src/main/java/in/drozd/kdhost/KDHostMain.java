package in.drozd.kdhost;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

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
	private static final String	DEFAULT_LOG_LEVEL_STR	= "WARNING";
	private static final Level	DEFAULT_LOG_LEVEL_LEVEL	= Level.WARNING;

	protected static final Logger log = Logger.getLogger(KDHostMain.class.getName());

	@Option(names = { "--verbosity", "-v" }, description = "Verbosity level, default: ${DEFAULT-VALUE}")
	String logLevel = DEFAULT_LOG_LEVEL_STR;

	public static void main(String[] args) {
		CommandLine cmd = new CommandLine(new KDHostMain());
		cmd.parseWithHandlers(new CommandLine.RunAll().andExit(0), CommandLine.defaultExceptionHandler().andExit(1), args);
	}

	@Override
	public void run() {
		setupLogger(this.logLevel);
	}

	@Command(description = "Send element(s) to host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void send(@Option(names = { "-c" }, description = "For Tables send table definition with columns. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean completeTable,
			@Parameters(paramLabel = "PATH") Path[] paths) throws Exception {

		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (Path el : paths) {
				host.sendElement(new KDHostElement(el), completeTable);
			}

		}
	}

	@Command(description = "Compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void compile(@Parameters(index = "0..*", description = "Files to compile", arity = "0..*", paramLabel = "ELEMENT") String[] elements) throws Exception {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (String el : elements) {
				host.compileElement(new KDHostElement(el));
			}

		}
	}

	@Command(description = "Drop elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void drop(@Parameters(description = "elements to drop", paramLabel = "ELEMENT") String[] elements) throws Exception {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (String el : elements) {
				host.dropElement(new KDHostElement(el));
			}

		}
	}

	@Command(name = "extract", description = "Extract environment", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void extractEnv(@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force) throws Exception {
		this.getall(true, true, new String[] {});
	}

	@Command(description = "Get all elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void getall(@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force,
			@Option(names = "-r", description = "Download filer/record elements. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean record,
			@Parameters(paramLabel = "ELEMENT", index = "0..*", arity = "1..*", description = "Element(s) to get from host") String[] elements) {

	}

	@Command(description = "Get elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void get(@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force,
			@Parameters(paramLabel = "ELEMENT", index = "0..*", arity = "1..*", description = "Element(s) to get from host") String[] elements) throws Exception {
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

	@Command(description = "Execute psl code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void psl() {

	}

	@Command(description = "Execute mrpc code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void mrpc(@Option(names = { "-mv", "--mrpc-version" }, description = "MRPC version to use, default: ${DEFAULT-VALUE}", hidden = false) String mrpcVersion,
			@Parameters(index = "0", arity = "1", description = "MRPC ID", defaultValue = "1") String mrpcid,
			@Parameters(index = "1..*", arity = "0..*", description = "MRPC parameters") String[] parameters) {

	}

	@Command(description = "List elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void list(@Option(names = "-a", description = "List all (supported) elements from host.", defaultValue = "false", hidden = true) boolean all,
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
				elementTypes.stream().flatMap(elType -> host.streamElementsOfType(elType, table)).forEach(el -> printElements(el, asNames));
			} else {

				if (all) {
					KDElementTypes.stream().filter(et -> et.isListable()).flatMap(elType -> host.streamElementsOfType(elType)).forEach(el -> printElements(el, asNames));
				} else if (!table.isBlank()) {
					Stream.of(KDElementTypes.TABLE, KDElementTypes.COLUMN).flatMap(elType -> host.streamElementsOfType(elType, table)).forEach(el -> printElements(el, asNames));

				}
			}
		}

		// This actions will use host connection
		exitingCommand(() -> "List command");

	}

	@Command(description = "Refresh elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void refresh(@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements) {

	}

	@Command(description = "Execute sql code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void sql(@Option(names = "-s", description = "Character used to separate columns, default: ${DEFAULT-VALUE}", defaultValue = "|") String separator,
			@Parameters(index = "0..*", description = "SQL query to execute") String[] sqlQry) {

	}

	@Command(description = "Test compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void test(@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements) throws Exception {
		try (KDHost host = new KDHost(log)) {
			host.connectToHost();
			for (Path el : elements) {
				host.testElement(new KDHostElement(el));
			}

		}
	}

	@Command(description = "Test, Send, Compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.cliutils.KDHostVersionInformation.class)
	void tsc(@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements) throws Exception {
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
	void watch(@Parameters(index = "0..*", description = "Directory to watch", paramLabel = "PATH") Path folder) {

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

		if (Stream.of("INFO", "ALL", "FINE", "SEVERE", "CONFIG", "FINER", "FINEST").noneMatch(el -> this.logLevel.equals(el))) {
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
