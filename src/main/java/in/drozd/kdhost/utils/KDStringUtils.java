package in.drozd.kdhost.utils;

public class KDStringUtils {

	private KDStringUtils() {

	}

	public static String capitalizeFirstLetter(String original) {
		if (original == null || original.length() == 0) {
			return original;
		}
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}

	public static long countChar(String str, char c) {
		if (str == null || c == 0 || str.isEmpty())
			return 0;
		return str.chars().filter(ch -> ch == c).count();
	}

}
