package in.drozd.kdhost;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class KDHostElementTest {

	@Test
	void testKDHostElementStringKDElementTypesStringPROC() {
		KDHostElement el = new KDHostElement("MRPC121", KDElementTypes.PROCEDURE, "MRPC121.PROC");
		assertAll("Constructor STS", () -> assertEquals("MRPC121", el.getElementName(), "Name"), //
				() -> assertEquals("MRPC121.PROC", el.getFileName(), "File Name"), //
				() -> assertEquals(null, el.getElementPackage(), "Package"), //
				() -> assertEquals(KDElementTypes.PROCEDURE, el.getElementType(), "Type"), //
				() -> assertEquals(Path.of("dataqwik", "procedure", "MRPC121.PROC"), el.getFilePath(), "Path"));
	}

	@Test
	void testKDHostElementStringPROC() {
		KDHostElement el = new KDHostElement("MRPC121.PROC");
		assertAll("Constructor S", () -> assertEquals("MRPC121", el.getElementName(), "Name"), //
				() -> assertEquals("MRPC121.PROC", el.getFileName(), "File Name"), //
				() -> assertEquals(null, el.getElementPackage(), "Package"), //
				() -> assertEquals(KDElementTypes.PROCEDURE, el.getElementType(), "Type"), //
				() -> assertEquals(Path.of("dataqwik", "procedure", "MRPC121.PROC"), el.getFilePath(), "Path"));
	}

	@Test
	void testKDHostElementPathPROC() {
		KDHostElement el = new KDHostElement(Path.of("dataqwik", "procedure", "MRPC121.PROC"));
		assertAll("Constructor P", () -> assertEquals("MRPC121", el.getElementName(), "Name"), //
				() -> assertEquals("MRPC121.PROC", el.getFileName(), "File Name"), //
				() -> assertEquals(null, el.getElementPackage(), "Package"), //
				() -> assertEquals(KDElementTypes.PROCEDURE, el.getElementType(), "Type"), //
				() -> assertEquals(Path.of("dataqwik", "procedure", "MRPC121.PROC"), el.getFilePath(), "Path"));
	}

	@Test
	void testKDHostElementStringKDElementTypesStringTBL() {
		KDHostElement el = new KDHostElement("DEP", KDElementTypes.TABLE, "DEP.TBL");
		assertAll("Constructor STS", () -> assertEquals("DEP", el.getElementName(), "Name"), //
				() -> assertEquals("DEP.TBL", el.getFileName(), "File Name"), //
				() -> assertEquals(null, el.getElementPackage(), "Package"), //
				() -> assertEquals(KDElementTypes.TABLE, el.getElementType(), "Type"), //
				() -> assertEquals(Path.of("dataqwik", "table", "dep", "DEP.TBL"), el.getFilePath(), "Path"));
	}

	@Test
	void testKDHostElementStringTBL() {
		KDHostElement el = new KDHostElement("DEP.TBL");
		assertAll("Constructor S", () -> assertEquals("DEP", el.getElementName(), "Name"), //
				() -> assertEquals("DEP.TBL", el.getFileName(), "File Name"), //
				() -> assertEquals(null, el.getElementPackage(), "Package"), //
				() -> assertEquals(KDElementTypes.TABLE, el.getElementType(), "Type"), //
				() -> assertEquals(Path.of("dataqwik", "table", "dep", "DEP.TBL"), el.getFilePath(), "Path"));
	}

	@Test
	void testKDHostElementPathTBL() {
		KDHostElement el = new KDHostElement(Path.of("dataqwik", "table", "dep", "DEP.TBL"));
		assertAll("Constructor P", () -> assertEquals("DEP", el.getElementName(), "Name"), //
				() -> assertEquals("DEP.TBL", el.getFileName(), "File Name"), //
				() -> assertEquals(null, el.getElementPackage(), "Package"), //
				() -> assertEquals(KDElementTypes.TABLE, el.getElementType(), "Type"), //
				() -> assertEquals(Path.of("dataqwik", "table", "dep", "DEP.TBL"), el.getFilePath(), "Path"));
	}
}
