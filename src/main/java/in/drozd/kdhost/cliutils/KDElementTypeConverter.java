package in.drozd.kdhost.cliutils;

import in.drozd.kdhost.KDElementTypes;
import picocli.CommandLine.ITypeConverter;

public class KDElementTypeConverter implements ITypeConverter<KDElementTypes> {

	@Override
	public KDElementTypes convert(String value) throws Exception {
		return KDElementTypes.typeForName(value);
	}

}
