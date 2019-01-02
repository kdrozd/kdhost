package in.drozd.kdhost;

import java.nio.file.Path;
import java.util.List;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "kdhost", // Program name
		mixinStandardHelpOptions = true, // Add build in help and version options
		description = "Remote command line interface for Host", // Description for usage information
		versionProvider = in.drozd.kdhost.KDHostVersionInformation.class // Version information provider
)
public class KDHostMain implements Runnable {
	// TODO: Add logging
	public static void main(String[] args) {
		CommandLine cmd = new CommandLine(new KDHostMain());
		cmd.parseWithHandlers(new CommandLine.RunFirst().andExit(0), CommandLine.defaultExceptionHandler().andExit(1), args);
	}

	@Override
	public void run() {

	}

	@Command(description = "Send element(s) to host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void send(@Option(names = { "-c" }, description = "For Tables send table definition with columns. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean completeTable,
			@Parameters(paramLabel = "PATH") Path[] paths) throws Exception {

		try (KDHost host = new KDHost()) {
			for (Path el : paths) {
				host.sendElement(new KDHostElement(el), completeTable);
			}

		}
	}

	@Command(description = "Compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void compile(@Parameters(index = "0..*", description = "Files to compile", arity = "0..*", paramLabel = "ELEMENT") String[] elements) throws Exception {
		try (KDHost host = new KDHost()) {

			for (String el : elements) {

				host.compileElement(new KDHostElement(el));
			}

		}
	}

	@Command(description = "Drop elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void drop(@Parameters(description = "elements to drop", paramLabel = "ELEMENT") String[] elements) throws Exception {
		try (KDHost host = new KDHost()) {

			for (String el : elements) {
				host.dropElement(new KDHostElement(el));
			}

		}
	}

	@Command(name = "extract", description = "Extract environment", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void extractEnv(@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force) {

	}

	@Command(description = "Get all elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void getall(@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force,
			@Option(names = "-r", description = "Download filer/record elements. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean record,
			@Parameters(paramLabel = "ELEMENT", index = "0..*", arity = "1..*", description = "Element(s) to get from host") String[] elements) {

	}

	@Command(description = "Get elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void get(@Option(names = "-f", description = "Override file if it exist. Default value: ${DEFAULT-VALUE}", defaultValue = "false") boolean force,
			@Parameters(paramLabel = "ELEMENT", index = "0..*", arity = "1..*", description = "Element(s) to get from host") String[] elements) throws Exception {

		try (KDHost host = new KDHost()) {
			host.connectToHost();
			if (force) {
				host.setForceOverRide(force);
			}
			for (String el : elements) {
				host.getElement(new KDHostElement(el));
			}
		}

	}

	@Command(description = "Execute psl code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void psl() {

	}

	@Command(description = "Execute mrpc code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void mrpc(@Option(names = { "-mv", "--mrpc-version" }, description = "MRPC version to use, default: ${DEFAULT-VALUE}", hidden = false) String mrpcVersion,
			@Parameters(index = "0", arity = "1", description = "MRPC ID", defaultValue = "1") String mrpcid,
			@Parameters(index = "1..*", arity = "0..*", description = "MRPC parameters") String[] parameters) {

	}

	@Command(description = "List elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void list(@Option(names = "-a", description = "List all (supported) elements from host.", defaultValue = "false", hidden = true) boolean all,
			@Option(names = "-l", description = "List listable element types", defaultValue = "false") boolean listListableTypes,
			@Option(names = "-s", description = "List supported element types", defaultValue = "false") boolean listAllTypes,
			@Option(names = "-n", description = "Show element names instead of file names", defaultValue = "false") boolean asNames,
			@Option(names = "-t", description = "List subelement of specific table.", paramLabel = "TABLE-NAME", defaultValue = "") String table,
			@Parameters(index = "0..*", arity = "0..*", description = "Element types to list from host", paramLabel = "ELEMENTS") List<KDElementTypes> elementTypes) {

		// Offline actions
		if (listListableTypes) {
			return;
		}
		if (listAllTypes) {
			return;
		}

		// This actions will use host connection

	}

	@Command(description = "Refresh elements from host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void refresh(@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements) {

	}

	@Command(description = "Execute sql code on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void sql(@Option(names = "-s", description = "Character used to separate columns, default: ${DEFAULT-VALUE}", defaultValue = "|") String separator,
			@Parameters(index = "0..*", description = "SQL query to execute") String[] sqlQry) {

	}

	@Command(description = "Test compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void test(@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements) {

	}

	@Command(description = "Test, Send, Compile elements on host", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void tsc(@Parameters(index = "0..*", description = "Elements to refresh", arity = "1..*", paramLabel = "ELEMENT") Path[] elements) {

	}

	@Command(description = "Watch for changes and execute tsc", mixinStandardHelpOptions = true, versionProvider = in.drozd.kdhost.KDHostVersionInformation.class)
	void watch(@Parameters(index = "0..*", description = "Directory to watch", paramLabel = "PATH") Path folder) {

	}

}
