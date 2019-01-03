package in.drozd.kdhost.cliutils;

import java.io.IOException;
import java.util.Properties;

import picocli.CommandLine.IVersionProvider;

public class KDHostVersionInformation implements IVersionProvider {

	// Fields from property files
	private static final String VERSION = "version";
	private static final String AUTHOR = "author";

	// Properties file name
	private static final String PROJECT_PROPERTIES = "project.properties";

	@Override
	public String[] getVersion() throws IOException {
		final Properties properties = new Properties();

		properties.load(this.getClass().getClassLoader().getResourceAsStream(PROJECT_PROPERTIES));

		return new String[] { String.format("version: %s by: %s", properties.getProperty(VERSION, "0.0.1"),
				properties.getProperty(AUTHOR, "Krzysztof Drozd.")) };
	}

}
