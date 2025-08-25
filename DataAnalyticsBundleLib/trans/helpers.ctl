
function void populateMetadataFieldRecord(string[] value, integer idx) {
	format = null;
	$out.0.value = value[idx];
	$out.0.nullable = isnull(value[idx]) || isBlank(value[idx]);
	$out.0.type = getDataType(value[idx]); // format variable should be populated here (if applicable)
	$out.0.position = idx;
	$out.0.quoteChar = dictionary.quotation;
	
	if (format != null && !isBlank(format)) {
		if ($out.0.type == "decimal") {
			string[] fragments = format.split(",");
			$out.0.length = str2integer(fragments[0]);
			$out.0.scale = str2integer(fragments[1]);
		} else {
			$out.0.format = format;
		}
	}
}
