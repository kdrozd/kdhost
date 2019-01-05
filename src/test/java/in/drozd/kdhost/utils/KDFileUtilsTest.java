package in.drozd.kdhost.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import in.drozd.kdhost.KDElementTypes;

class KDFileUtilsTest {

	@Test
	void testGetElementNamePathDEPTBL() {
		assertEquals("DEP", KDFileUtils.getElementName(Path.of("dataqwik", "table", "dep", "DEP.TBL")));
	}

	@Test
	void testGetElementNamePathDEPTBL2() {
		assertEquals("DEP", KDFileUtils.getElementName(Path.of("DEP.TBL")));
	}

	@Test
	void testGetElementNameStringDEPTBL() {
		assertEquals("DEP", KDFileUtils.getElementName(Path.of("dataqwik", "table", "dep", "DEP.TBL").toString()));
	}

	@Test
	void testGetElementNameStringDEPTBL2() {
		assertEquals("DEP", KDFileUtils.getElementName("DEP.TBL"));
	}

	@Test
	void testGetDefaultPath() {
		assertEquals(Path.of("dataqwik", "table", "dep", "DEP.TBL"), KDFileUtils.getDefaultPath(KDElementTypes.TABLE, "DEP.TBL"));
	}

	@Test
	void testGetExtensionString() {
		assertEquals("TBL", KDFileUtils.getExtension(Path.of("dataqwik", "table", "dep", "DEP.TBL").toString()).get());
	}

	@Test
	void testGetExtensionFile() {
		assertEquals("TBL", KDFileUtils.getExtension(Path.of("dataqwik", "table", "dep", "DEP.TBL").toFile()).get());
	}

	@Test
	void testGetExtensionPath() {
		assertEquals("TBL", KDFileUtils.getExtension(Path.of("dataqwik", "table", "dep", "DEP.TBL")).get());
	}

	@Test
	void testGetExtensionStrings() {
		assertEquals("TBL", KDFileUtils.getExtension("DEP.TBL").get());
	}

	@Test
	void testGetExtensionFiles() {
		assertEquals("TBL", KDFileUtils.getExtension(Path.of("DEP.TBL").toFile()).get());
	}

	@Test
	void testGetExtensionPaths() {
		assertEquals("TBL", KDFileUtils.getExtension(Path.of("DEP.TBL")).get());
	}
}
