package in.drozd.kdhost;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import in.drozd.kdhost.cliutils.KDHostVersionInformation;

class KDHostVersionInformationTest {

	@Test
	void testGetVersion() {
		KDHostVersionInformation kdvi = new KDHostVersionInformation();
		try {
			String[] vi = kdvi.getVersion();
			assertNotEquals("version: 0.0.1 by: Krzysztof Drozd.", vi[0]);
		} catch (IOException e) {
			fail(e.getMessage());
		}

	}

}
