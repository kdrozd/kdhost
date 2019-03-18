package in.drozd.kdhost;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import in.drozd.kdhost.utils.KDFileUtils;

/**
 * @author Krzysztof Drozd
 *
 */
public enum KDElementTypes {
	PROCEDURE {
		@Override
		public String fileExtension() {
			return "PROC";
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "procedure");
		}

		@Override
		public Path getDefaultDirectory(String elementName) {
			return this.getDefaultDirectory();
		}

		@Override
		public Path getDefaultDirectory(KDHostElement element) {
			return this.getDefaultDirectory();
		}

		@Override
		protected String getQueryColumns() {
			return "PROCID";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL25";
		}

		@Override
		public boolean canCompile() {
			return true;
		}
	},
	TABLE {
		@Override
		public String fileExtension() {
			return "TBL";
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "table");
		}

		@Override
		public Path getDefaultDirectory(String elementName) {
			String[] names = elementName.split("-");
			if (names != null && names[0] != null && names.length > 1) {
				return Path.of("dataqwik", "table", names[0].toLowerCase());

			}
			return Path.of("dataqwik", "table", KDFileUtils.getElementName(elementName).toLowerCase());
		}

		@Override
		public Path getDefaultDirectory(KDHostElement element) {
			String[] names = element.getElementName().split("-");
			if (names != null && names[0] != null && names.length > 1) {
				return Path.of("dataqwik", "table", names[0].toLowerCase());
			}
			return Path.of("dataqwik", "table", element.getElementName().toLowerCase());
		}

		@Override
		protected String getQueryColumns() {
			return "FID";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL1";
		}

		@Override
		public boolean canCompile() {
			return true;
		}
	},
	COLUMN {
		@Override
		public String fileExtension() {
			return "COL";
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "table");
		}

		@Override
		public Path getDefaultDirectory(String elementName) {
			return TABLE.getDefaultDirectory(elementName);
		}

		@Override
		public Path getDefaultDirectory(KDHostElement element) {
			return TABLE.getDefaultDirectory(element);
		}

		@Override
		protected String getTableNameField() {
			return "FID";
		}

		@Override
		protected String getQueryColumns() {
			return "FID,DI";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL1D";
		}

		@Override
		public boolean canCompile() {
			return true;
		}
	},
	PROPERTY {
		@Override
		public String fileExtension() {
			return "properties";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("property");
		}

	},
	COMPLETETABLE {
		@Override
		public String fileExtension() {
			return "table";
		}

		@Override
		public Path getDefaultDirectory() {
			return TABLE.getDefaultDirectory();
		}

		@Override
		public Path getDefaultDirectory(String elementName) {
			return TABLE.getDefaultDirectory(elementName);
		}

		@Override
		public Path getDefaultDirectory(KDHostElement element) {
			return TABLE.getDefaultDirectory(element);
		}

		@Override
		protected String getTableNameField() {
			return TABLE.getTableNameField();
		}

		@Override
		protected String getQueryColumns() {
			return TABLE.getQueryColumns();
		}

		@Override
		protected String getTableForQuery() {
			return TABLE.getTableForQuery();
		}

		@Override
		public boolean canCompile() {
			return true;
		}
	},
	PSLXTRA {
		@Override
		public String fileExtension() {
			return "psllxtra";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "pslxtra");
		}

	},
	LOOKUPDOC {
		@Override
		public String fileExtension() {
			return "LUD";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "lookup_doc");
		}

	},
	GLOBAL {
		@Override
		public String fileExtension() {
			return "G";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("data");
		}

	},
	AGGREGATE {
		@Override
		public String fileExtension() {
			return "AGR";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "aggregate");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getQueryColumns() {
			return "AGID";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL22";
		}

	},
	DATA {
		@Override
		public String fileExtension() {
			return "DAT";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("data");
		}

	},
	REPORT {
		@Override
		public String fileExtension() {
			return "RPT";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "report");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getQueryColumns() {
			return "RID";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL5D";
		}

		@Override
		public boolean canCompile() {
			return true;
		}

	},
	PREPOSTLIB {
		@Override
		public String fileExtension() {
			return "PPL";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "pre_post_lib");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getQueryColumns() {
			return "PID";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL13";
		}

	},
	ROUTINE {
		@Override
		public String fileExtension() {
			return "m";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("routine");
		}

	},
	QUERY {
		@Override
		public String fileExtension() {
			return "QRY";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "query");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getQueryColumns() {
			return "QID";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL4";
		}

	},
	SCREEN {
		@Override
		public String fileExtension() {
			return "SCR";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "screen");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL2";
		}

		@Override
		protected String getQueryColumns() {
			return "SID";
		}

		@Override
		public boolean canCompile() {
			return true;
		}

	},
	FKEY {
		@Override
		public String fileExtension() {
			return "FKY";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "foreign_key");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getTableNameField() {
			return "FID";
		}

		@Override
		protected String getQueryColumns() {
			return "FID,FKEYS";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL1F";
		}
	},
	PSLX {
		@Override
		public String fileExtension() {
			return "pslx";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("psl", "custom");
		}

		@Override
		public Path getDefaultDirectory(String elementName) {
			return Path.of("psl", elementName);
		}

		@Override
		public Path getDefaultDirectory(KDHostElement element) {
			return Path.of("psl", element.getElementPackage());

		}
	},
	PSQL {
		@Override
		public String fileExtension() {
			return "psql";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("scripts");
		}

	},
	BATCH {
		@Override
		public String fileExtension() {
			return "BATCH";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "batch");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL33";
		}

		@Override
		protected String getQueryColumns() {
			return "BCHID";
		}

		@Override
		public boolean canCompile() {
			return true;
		}
	},
	PSL {
		@Override
		public String fileExtension() {
			return "psl";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("psl", "custom");
		}

		// FIXME: Add package support
		@Override
		public boolean canCompile() {
			return true;
		}
	},
	TRIGGER {
		@Override
		public String fileExtension() {
			return "TRIG";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "trigger");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getTableNameField() {
			return "TABLE";
		}

		@Override
		protected String getQueryColumns() {
			return "TABLE,TRGID";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL7";
		}

		@Override
		public boolean canCompile() {
			return true;
		}
	},
	INDEX {
		@Override
		public String fileExtension() {
			return "IDX";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "index");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getTableNameField() {
			return "FID";
		}

		@Override
		protected String getQueryColumns() {
			return "FID,INDEXNM";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL8";
		}

		@Override
		public boolean canCompile() {
			return true;
		}
	},
	JOURNAL {
		@Override
		public String fileExtension() {
			return "JFD";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of("dataqwik", "journal");
		}

		@Override
		public boolean isListable() {
			return true;
		}

		@Override
		protected String getTableNameField() {
			return "PRITABLE";
		}

		@Override
		protected String getQueryColumns() {
			return "PRITABLE,JRNID";
		}

		@Override
		protected String getTableForQuery() {
			return "DBTBL9";
		}

		@Override
		public boolean canCompile() {
			return true;
		}
	},
	SFILES {
		@Override
		public String fileExtension() {
			return "";
		}

		@Override
		public Path getDefaultDirectory() {
			return Path.of(".");
		}

		@Override
		public Path getDefaultDirectory(String elementName) {
			if (elementName.contains("testfiles"))
				return Path.of("unittest");
			if (elementName.contains("expected"))
				return Path.of("unittest");
			if (elementName.contains("uxscrpt"))
				return Path.of("system_files");
			if (elementName.contains(".ini"))
				return Path.of("system_files", "ini");
			if (elementName.contains("ini/")) {
				return Path.of("system_files");
			}
			return this.getDefaultDirectory();
		}

		@Override
		public Path getDefaultDirectory(KDHostElement element) {
			return this.getDefaultDirectory(element.getFilePath().toString());
		}

	};

	public static Stream<KDElementTypes> stream() {
		return Arrays.stream(KDElementTypes.values());
	}

	public boolean isListable() {
		return false;
	}

	public boolean canCompile() {
		return false;
	}

	/**
	 * Get file extension used by this element type.
	 * 
	 * @return String with file extension.
	 */
	public abstract String fileExtension();

	public abstract Path getDefaultDirectory();

	public Path getDefaultDirectory(String elementName) {
		return this.getDefaultDirectory();
	}

	public Path getDefaultDirectory(KDHostElement element) {
		return this.getDefaultDirectory();
	}

	public static KDElementTypes typeForName(String name) {
		switch (name.toLowerCase()) {
		case "table":
		case "tables":
			return KDElementTypes.TABLE;
		case "procedure":
		case "procedures":
			return KDElementTypes.PROCEDURE;
		case "column":
		case "columns":
			return KDElementTypes.COLUMN;
		case "batch":
		case "batches":
			return KDElementTypes.BATCH;
		case "psql":
			return KDElementTypes.PSQL;
		case "pslx":
			return KDElementTypes.PSLX;
		case "psl":
		case "psls":
			return KDElementTypes.PSL;
		case "fkey":
		case "fkeys":
			return KDElementTypes.FKEY;
		case "screen":
		case "screens":
			return KDElementTypes.SCREEN;
		case "query":
		case "querys":
			return KDElementTypes.QUERY;
		case "trigger":
		case "triggers":
			return KDElementTypes.TRIGGER;
		case "index":
		case "indekses":
			return KDElementTypes.INDEX;
		case "journal":
		case "journals":
			return KDElementTypes.JOURNAL;
		case "sfiles":
		case "sfile":
			return KDElementTypes.SFILES;
		case "routine":
		case "routines":
			return KDElementTypes.COLUMN;
		case "prepostlib":
			return KDElementTypes.PREPOSTLIB;
		case "reports":
		case "report":
			return KDElementTypes.COLUMN;
		case "data":
			return KDElementTypes.DATA;
		case "aggregate":
			return KDElementTypes.AGGREGATE;
		case "lookupdoc":
			return KDElementTypes.LOOKUPDOC;
		case "globals":
		case "global":
			return KDElementTypes.GLOBAL;
		case "pslxtra":
			return KDElementTypes.PSLXTRA;
		case "completetable":
			return KDElementTypes.COMPLETETABLE;
		case "properties":
		case "property":
			return KDElementTypes.PROPERTY;
		}
		return KDElementTypes.SFILES;

	}

	public String getQuery() {
		if (!this.isListable())
			return null;

		return String.format("SELECT %s FROM %s ", this.getQueryColumns(), this.getTableForQuery());
	}

	public String getQuery(String tableName) {
		if (!this.isListable())
			return null;

		return String.format("SELECT %s FROM %s WHERE %s='%s' ", this.getQueryColumns(), this.getTableForQuery(),
				this.getTableNameField(), tableName);
	}

	protected String getTableNameField() {
		return "";
	}

	protected String getQueryColumns() {
		return "";
	}

	protected String getTableForQuery() {
		return "";
	}

	public static KDElementTypes typeForExtension(String extension) {
		final String ext = extension.toLowerCase();

		// No extension is bad case
		if (ext.isEmpty()) {
			return null;
		}
		switch (ext) {
		case "proc":
			return PROCEDURE;
		case "psl":
			return PSL;
		case "tbl":
			return TABLE;
		case "col":
			return COLUMN;
		case "batch":
			return BATCH;
		case "dat":
			return DATA;
		case "fky":
			return FKEY;
		case "g":
			return GLOBAL;
		case "idx":
			return INDEX;
		case "jfd":
			return JOURNAL;
		case "properties":
			return PROPERTY;
		case "ppl":
			return PREPOSTLIB;
		case "pslx":
			return PSLX;
		case "psql":
			return PSQL;
		case "qry":
			return QUERY;
		case "rpt":
			return REPORT;
		case "m":
			return ROUTINE;
		case "scr":
			return SCREEN;
		case "trig":
			return TRIGGER;
		case "table":
			return COMPLETETABLE;

		default: {
			return SFILES;
		}
		}

	}

	public String typeDescription() {

		switch (this) {
		case PROPERTY:
			return "properties file";
		case PROCEDURE:
			return "Procedure";
		case TABLE:
			return "Table";
		case COMPLETETABLE:
			return "Complete Table";
		case COLUMN:
			return "Column";
		case INDEX:
			return "Index";
		case JOURNAL:
			return "Journal";
		case TRIGGER:
			return "Trigger";
		case BATCH:
			return "Batch";
		case PSL:
			return "psl File";
		case PSLX:
			return "pslx File";
		case PSQL:
			return "PSQLScript";
		case PREPOSTLIB:
			return "Pre Post Lib";
		case FKEY:
			return "Foreign Key";
		case ROUTINE:
			return "M routine";
		case SCREEN:
			return "Screen";
		case REPORT:
			return "Report";
		case QUERY:
			return "Query";
		case AGGREGATE:
			return "Aggregate";
		case GLOBAL:
			return "Global";
		case DATA:
			return "Data";
		case LOOKUPDOC:
			return "lookupdocs";
		case PSLXTRA:
			return "pslxtra File";
		default:
			return "Unknown file";

		}
	}

	@Override
	public String toString() {
		return this.typeDescription();

	}

	public static KDElementTypes typeForFileName(String fileName) {
		return KDElementTypes.typeForExtension(KDFileUtils.getExtension(fileName).orElse(""));
	}
}
