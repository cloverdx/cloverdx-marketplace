
function string toStandardName(string field) {
	return field.replace("[^A-Za-z0-9_ ]","").translate(" ","_").replace("^([^_A-Za-z])","_$1");
}
