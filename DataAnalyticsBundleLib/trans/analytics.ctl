
function string[] explode(string instr, string delimiter, string quotation) { 
	string[] fragments;
	string fragment;
	boolean quoted;
	boolean eos = false;
	
	integer strlen = length(instr);
	integer quotlen = length(quotation);
	integer[] ptr = [ 0, null ];
	
	do {
		string nextdlm = delimiter;
		
		if (!isnull(quotation) && substring(instr,ptr[0],quotlen) == quotation) {
			nextdlm = quotation.concat(delimiter);
			ptr[0] += quotlen;
			quoted = true;
		}
		
		ptr[1] = indexOf(instr,nextdlm,ptr[0]);
		
		if (ptr[1] < 0) {
			eos = true;
			ptr[1] = strlen - (quoted ? quotlen : 0);
		}
		
		append(fragments,substring(instr,ptr[0],max(0,ptr[1] - ptr[0])));
		ptr[0] = ptr[1] + length(nextdlm);
	// Hitting EOS will terminate parsing
	} while (!eos);
	
	return fragments;
}

function string checkQuotationCharacter(string instr, string delimiter) {
	string quotationExpr = join("",QUOTATION_CHARACTERS);
	string char = null;
	
	foreach (string q: find(instr,"(?>(?<=(?:^|["+ delimiter +"])(["+ quotationExpr +"])).+?(?=\\1"+ delimiter +"|$))",1)) {
		if (char == null) {
			char = q;
		} else if (char != q) {
			raiseError("More than 1 quotation character found in '"+ instr +"' ("+ concat(char," and ",q) +")!");
		}
	}
	
	return char;
}

function string escapeDelimiter(string indlm) {
	string delimiter = "";
	// Escape every character in delimiter - just in case
	for (integer i=0;i<length(indlm);i++) {
		delimiter = delimiter.concat("\\",indlm.charAt(i));
	}
	
	return delimiter;
}

function string getDataType(string value) {
	if (isnull(value) || isBlank(value)) {
		return null;
	}
	
	// Should match anything like:
	// 9.999
	// .999
	// or simple 9
	// and + or - characters in front of the number
	if (!value.isBlank() && value.matches("^[+-]?[0-9]*([\\.][0-9]+([eE]\\+[0-9]+)?)?$")) {
		value = replace(value,"^[+-]","");
		string numeric;
		
		if (value.contains(".")) { // || value.contains(",")) { // FIXME: Only supporting point only, this needs to be determined by locale
			string[] fragments = value.split("[\\.]",-1);
			string[] fraction = fragments[1].split("[eE]");
			integer real = length(fragments[0]);
			integer dec;
			
			// With exponent information
			if (length(fraction) > 1) {
				real = real + str2integer(fraction[1]);
				dec = max(0,length(fraction[0]) - str2integer(fraction[1]));
			// Without exponent
			} else {
				dec = length(fraction[0]);
			}
			
			format = concatWithSeparator(",",toString(real+dec),toString(dec));
			return "decimal";
		
		// Check for simple data type
		} else {
			numeric = isInteger(value) ? "integer" : (isLong(value) ? "long" : "decimal");
			//printLog(info,concat(value," is ",numeric));
		}
		
		// Only happends when value has no decimal separator and is neigther integer nor long
		if (numeric == "decimal") {
			format = concatWithSeparator(",",toString(length(value)),"0");
		}
		
		return numeric;
	}
	
	foreach (string t: TIME_FORMAT) {
		foreach (string d: DATE_FORMAT) {
			boolean valueIsDate = false;
			string f = d.concat(!isBlank(t) ? " " : "",t);
			
			if (isBlank(f)) {
				continue;
			}
		
			if (!isBlank("${LOCALE}")) {
				if (!isBlank("${TIMEZONE}")) {
					valueIsDate = isDate(value, f, "${LOCALE}", "${TIMEZONE}",true);
				} else {
					valueIsDate = isDate(value, f, "${LOCALE}", true);
				}
			} else {
				valueIsDate = isDate(value, f, true);
			}
		
			if (valueIsDate) {
				format = f;
				return "date";
			}
		}
	}
	
	if (value.lowerCase().in(["true","false"])) {
		return "boolean";
	}
	
	return "string";
}
