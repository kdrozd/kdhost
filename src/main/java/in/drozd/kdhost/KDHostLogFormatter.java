package in.drozd.kdhost;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class KDHostLogFormatter extends Formatter {

	// Create a DateFormat to format the logger timestamp.
	private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(1000);
		// Date and time
		builder.append("[").append(df.format(new Date(record.getMillis()))).append("] ");

		builder.append("[").append(String.format("%-7s", record.getLevel())).append("] ");
		builder.append("[").append(String.format("%-20s", record.getSourceMethodName())).append("] ");
		builder.append(formatMessage(record));
		builder.append("\n");
		return builder.toString();
	}

}
