package de.hhu.bsinfo.utils.main;

import java.util.HashMap;
import java.util.Map;

import de.hhu.bsinfo.utils.reflect.dt.DataTypeParser;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserBool;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserBoolean;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserByte;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserDouble;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserFloat;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserInt;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserLong;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserShort;
import de.hhu.bsinfo.utils.reflect.dt.DataTypeParserString;
import de.hhu.bsinfo.utils.reflect.unit.UnitConverter;
import de.hhu.bsinfo.utils.reflect.unit.UnitConverterGBToByte;
import de.hhu.bsinfo.utils.reflect.unit.UnitConverterKBToByte;
import de.hhu.bsinfo.utils.reflect.unit.UnitConverterMBToByte;

//bla[int]{kb2b}:1234594
public class DefaultMainArgumentsParser implements MainArgumentsParser {

	private static final String KEY_VAL_SEPARATOR = ":";
	private static final String TYPE_PREFIX = "[";
	private static final String TYPE_POSTFIX = "]";
	private static final String UNIT_PREFIX = "{";
	private static final String UNIT_POSTFIX = "}";
	
	private Map<String, DataTypeParser> m_dataTypeParsers = new HashMap<String, DataTypeParser>();
	private Map<String, UnitConverter> m_unitConverters = new HashMap<String, UnitConverter>();
	
	public DefaultMainArgumentsParser() {
		// add default type parsers
		addDataTypeParser(new DataTypeParserString());
		addDataTypeParser(new DataTypeParserByte());
		addDataTypeParser(new DataTypeParserShort());
		addDataTypeParser(new DataTypeParserInt());
		addDataTypeParser(new DataTypeParserLong());
		addDataTypeParser(new DataTypeParserFloat());
		addDataTypeParser(new DataTypeParserDouble());
		addDataTypeParser(new DataTypeParserBool());
		addDataTypeParser(new DataTypeParserBoolean());
		
		// add default unit converters
		addUnitConverter(new UnitConverterKBToByte());
		addUnitConverter(new UnitConverterMBToByte());
		addUnitConverter(new UnitConverterGBToByte());
	}

	@Override
	public void parseArguments(final String[] p_args, final MainArguments p_arguments) {		
		for (String arg : p_args) 
		{
			String[] keyVal = splitKeyValue(arg);
			// ignore invalid format
			if (keyVal == null)
				continue; 
			
			String keyName = getKeyName(keyVal[0]);
			String type = getKeyType(keyVal[0]);
			String unit = getKeyUnit(keyVal[0]);
			
			DataTypeParser dataTypeParser = m_dataTypeParsers.get(type);
			// no parser available for data type, ignore
			if (dataTypeParser == null) {
				continue;
			}
			
			Object val = dataTypeParser.parse(keyVal[1]);
			
			UnitConverter unitConverter = m_unitConverters.get(unit);
			if (unitConverter != null)
				val = unitConverter.convert(val);
			
			p_arguments.setArgument(keyName, val);
		}
	}
	
	public boolean addDataTypeParser(final DataTypeParser p_parser)
	{
		return m_dataTypeParsers.put(p_parser.getTypeIdentifer(), p_parser) == null;
	}
	
	public boolean addUnitConverter(final UnitConverter p_converter)
	{
		return m_unitConverters.put(p_converter.getUnitIdentifier(), p_converter) == null;
	}
	
	private String[] splitKeyValue(final String p_argument) {
		// don't use split here. the value can contain the separator as well
		int sepIndex = p_argument.indexOf(KEY_VAL_SEPARATOR);
		if (sepIndex == -1)
			return null;
		
		String[] keyVal = new String[2];
		keyVal[0] = p_argument.substring(0, sepIndex);
		keyVal[1] = p_argument.substring(sepIndex + 1);
		return keyVal;
	}
	
	private String getKeyName(final String p_key) {
		int typeStart = p_key.indexOf(TYPE_PREFIX);

		// no type attached, default to string
		if (typeStart == -1) {
			return p_key;
		} else {
			return p_key.substring(0, typeStart);
		}
	}
	
	private String getKeyType(final String p_key) {
		int typeStart = p_key.indexOf(TYPE_PREFIX);
		int typeEnd = p_key.indexOf(TYPE_POSTFIX);

		// no type attached, default to string
		if (typeStart == -1 || typeEnd == -1) {
			return "str";
		} else {
			return p_key.substring(typeStart + 1, typeEnd);
		}
	}
	
	private String getKeyUnit(final String p_key) {
		int typeStart = p_key.indexOf(UNIT_PREFIX);
		int typeEnd = p_key.indexOf(UNIT_POSTFIX);

		// no type attached, default to string
		if (typeStart == -1 || typeEnd == -1) {
			return "";
		} else {
			return p_key.substring(typeStart + 1, typeEnd);
		}
	}
}