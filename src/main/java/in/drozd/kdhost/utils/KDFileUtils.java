package in.drozd.kdhost.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import in.drozd.kdhost.KDElementTypes;

public class KDFileUtils {

	public static String getElementName(Path file) {
		return KDFileUtils.getElementName(file.getFileName().toString());

	}

	public static String getElementName(String element) {
		return element.substring(element.lastIndexOf(File.separatorChar) + 1, element.lastIndexOf('.'));
	}

	public static Path getDefaultPath(KDElementTypes elementType, String fileName) {
		return Path.of(elementType.getDefaultDirectory(fileName).toString(), fileName);
	}

	public static Optional<String> getExtension(String fileName) {
		char ch;
		int len;
		if (fileName == null || (len = fileName.length()) == 0 || (ch = fileName.charAt(len - 1)) == '/' || ch == '\\' || // in the case of a directory
				ch == '.') // in the case of . or ..
			return Optional.empty();
		int dotInd = fileName.lastIndexOf('.'), sepInd = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		if (dotInd <= sepInd)
			return Optional.empty();
		else
			return Optional.ofNullable(fileName.substring(dotInd + 1));
	}

	public static Optional<String> getExtension(File fileName) {
		if (fileName.isDirectory())
			return Optional.empty();
		return getExtension(fileName.toString());
	}

	public static Optional<String> getExtension(Path fileName) {

		return getExtension(fileName.toFile());
	}
}