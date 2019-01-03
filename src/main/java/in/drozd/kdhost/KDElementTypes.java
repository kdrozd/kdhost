package in.drozd.kdhost;

import java.util.Arrays;
import java.util.stream.Stream;

public enum KDElementTypes {
	PROCEDURE, TABLE, COLUMN;

	public static Stream<KDElementTypes> stream() {
		return Arrays.stream(KDElementTypes.values());
	}

	public boolean isListable() {
		// TODO Auto-generated method stub
		return false;
	}
}
