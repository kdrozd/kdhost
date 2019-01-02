package in.drozd.kdhost;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("offline")
class KDHostMainListOfflineTest {

	@Test
	@Tag("offline")
	void testListSupported() {
		KDHostMain.main(new String[] { "list", "-s" });
	}

	@Test
	@Tag("offline")
	void testListListable() {
	}
}
