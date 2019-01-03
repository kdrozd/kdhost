package in.drozd.kdhost;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("offline")
class KDHostMainListOfflineTest {

	private KDHostMain kdhost = new KDHostMain();

	@Test
	@Tag("offline")
	void testListSupported() {
		kdhost.list(false, true, false, false, null, null);
	}

	@Test
	@Tag("offline")
	void testListListable() {
	}
}
