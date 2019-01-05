package in.drozd.kdhost;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

import in.drozd.kdhost.exceptions.KDHostSqlException;
import in.drozd.kdhost.utils.KDFileUtils;
import in.drozd.kdhost.utils.KDStringUtils;

public class KDHostElement {

	private String			name;
	private String			fileName;
	private String			elementPackage	= null;
	private KDElementTypes	elementType;
	private Path			filePath;

	public KDHostElement(String name, KDElementTypes type, String fileName) {
		this.name = name;
		this.elementType = type;
		this.fileName = fileName;
		this.filePath = KDFileUtils.getDefaultPath(elementType, fileName);
	}

	public KDHostElement(String fileName) {
		this.fileName = fileName;
		this.elementType = KDElementTypes.typeForFileName(fileName);
		this.name = KDFileUtils.getElementName(fileName);
		this.filePath = KDFileUtils.getDefaultPath(elementType, fileName);

	}

	public KDHostElement(Path el) {
		this.filePath = el;
		this.fileName = el.getFileName().toString();
		this.elementType = KDElementTypes.typeForFileName(fileName);
		this.name = KDFileUtils.getElementName(fileName);
	}

	public KDHostElement(ResultSet rs, KDElementTypes type) {
		this.elementType = type;
		this.fileName = getFileName(rs, type);
		this.name = KDFileUtils.getElementName(fileName);
		this.filePath = KDFileUtils.getDefaultPath(elementType, fileName);

	}

	private String getFileName(ResultSet rs, KDElementTypes type) {
		final int columnsCount = (int) KDStringUtils.countChar(type.getQueryColumns(), ',') + 1;
		final String[] parts = new String[columnsCount];

		for (int i = 0; i < columnsCount; i++) {
			try {
				parts[i] = rs.getString(i + 1);
			} catch (SQLException e) {
				throw new KDHostSqlException(e);
			}
		}
		if (type == KDElementTypes.FKEY) {
			parts[1] = parts[1].replace(',', '~');
			parts[1] = parts[1].replace('%', '_');

		}
		if (parts[columnsCount - 1].endsWith("." + type.fileExtension()))
			return String.join("-", parts);
		return String.join("-", parts) + "." + type.fileExtension();
	}

	public KDElementTypes getElementType() {
		return elementType;
	}

	public String getElementName() {
		return name;
	}

	public Path getFilePath() {
		return filePath;
	}

	public String getElementPackage() {
		return this.elementPackage;
	}

	public String getFileName() {
		return this.fileName;
	}

	@Override
	public String toString() {
		return fileName;
	}

}