
/*
 * Sets a field of the $out.0 record ussing a supplied variant value, converting it to the target field's type.
 *
 * HubSpot api returns most of the values as string. This function tries to do the conversion, accounting for
 * most of the HubSpot's quirks and formats
 * 
 * Errors raised have a particular format that can be relied on by the upper layer.
 * Begging with [Configuration error] - error caused by mismatch between the metadata and HubSpot properties,
 *     or other user errors.
 * Begging with [Parsing error] - runtime error during conversion of a value.
 *
 * Some of the parameters are only used for the error messages.
 */

function void set_output_field_from_hubspot_value(variant value, string hubspot_property_name, string clover_field_name, string object_id){

	if(getFieldIndex($out.0, clover_field_name) == -1){
		//field not found on output
		raiseError("[Configuration error] Output metadata does not contain field named: " + clover_field_name);
	}
	
	string output_field_type = getFieldType($out.0, clover_field_name);
	string field_value;
	
	if(output_field_type != "variant"){
			field_value = trim(cast(value,string)):trim(toString(value));
	}
	
	switch(output_field_type){
			case "string":
				setStringValue($out.0,clover_field_name,field_value);
				break;
			case "date":
				setDateValue(
					$out.0,
					clover_field_name,
					isBlank(field_value)?null:str2date(field_value,"iso-8601:dateTime")
				);
				break;
			case "long":
				long long_value;
				try{
					 long_value = isBlank(field_value)?null:str2long(chop_fractional_part_from_decimal_if_zero(field_value));
				}catch(CTLException ex){
					printLog(error, ex);
					raiseError(parsing_error(field_value,hubspot_property_name,output_field_type,ex));
				}
				setLongValue(
					$out.0,
					clover_field_name,
					long_value
				);
				break;
			case "integer":
				integer integer_value;
				try{
					integer_value = isBlank(field_value)?null:str2integer(chop_fractional_part_from_decimal_if_zero(field_value));
				}catch(CTLException ex){
					printLog(error, ex);
					raiseError(parsing_error(field_value,hubspot_property_name,output_field_type,ex));
				}
				setIntValue(
					$out.0,
					clover_field_name,
					integer_value
				);
				break;
			case "decimal":
				decimal decimal_value;
				try{
					decimal_value = isBlank(field_value)?null:str2decimal(field_value);
				}catch(CTLException ex){
					printLog(error, ex);
					raiseError(parsing_error(field_value,hubspot_property_name,output_field_type,ex));
				}
				setDecimalValue(
					$out.0,
					clover_field_name,
					decimal_value
				);
				break;
			case "number":
				double double_value;
				try{
					double_value = isBlank(field_value)?null:str2double(field_value);
				}catch(CTLException outer){
					try{
						//Hubspot sometimes returns funny numbers, with 40+ digits, try to parse anyway
						if(outer.message == "Too many digits - Overflow" and field_value.matches('\d*\.\d*') and indexOf(field_value,".") < 15){
							double_value = str2double(left(field_value, 16));
						}
						printLog(warn, "Object id:" + object_id +" Field: "+ hubspot_property_name +" Fallback to lower precision - truncating value to 15 digits");
					}catch(CTLException inner){
						printLog(error, inner);
						raiseError(parsing_error(field_value,hubspot_property_name,output_field_type,inner));
					}
				}
				setNumValue(
					$out.0,
					clover_field_name,
					double_value
				);
				break;
			case "variant":
				setValue($out.0,
					clover_field_name,
					value
				);
				break;
			case "boolean":
				boolean boolean_value;
				try{
					 boolean_value = isBlank(field_value)?null:str2bool(lowerCase(field_value)):str2bool(field_value,getFieldProperties($out.0,clover_field_name)["format"]);
				}catch(CTLException ex){
					printLog(error, ex);
					raiseError(parsing_error(field_value,hubspot_property_name,output_field_type,ex));
				}
				setBoolValue(
					$out.0,
					clover_field_name,
					boolean_value
				);
				break;
			default:
				raiseError("[Not implemented] Conversion for field " + clover_field_name + " of type " +  output_field_type + " not implemented");
		}
}

/**
 * Parse string using Clover bool patterns.
 *
 * Only simple case supported - First separator needs to be same as last character and at least the A and B clause needs to provided.
 */
function boolean str2bool(string value, string format){
	string separator = charAt(format,0);
	if(isBlank(format)){
		raiseError("Format for conversion is blank.");	
	}
	if(charAt(format,length(format)-1) != separator){
		raiseError("Only patterns beginning and ending with the same separator character (=separator).");
	}
	string[] parts = format.split(separator);
	if(parts.length() < 2 or isBlank(parts[0]) or isBlank(parts[1])){
		raiseError("Invalid format pattern.");
	}
	
	if(value.matches(parts[0])){
		return true;	
	}else if(value.matches(parts[1])){
		return false;
	}else{
		raiseError("Value: " + value + " cannot be parsed as boolean (using pattern: " + format + ")" );
	}
}

/*
 * Helper function, formats error message.
 */
function string parsing_error(string field_value, string field_name, string field_type, CTLException exception){
	return concat(
		"[Parsing error] Error parsing value of property ",
		field_name,
		" with value ",
		field_value,
		" to type ",
		field_type,
		" . Cause: ",
		exception.cause,
		" Error message : ",
		exception.message
	);
}

/*
 * Chops the fractional part of a decimal, if all zeros. Usefull for conversion to integer.
 */

function string chop_fractional_part_from_decimal_if_zero(string input){
	if(matches(input,'\d*\.0*')){
		return chop(input,'\.0*');
	}else{
		return input;
	}
}

function string lookup_mapping_to_clover( map[string,string] mapping, string key){
	string result = mapping[key];
	
	if(not isBlank(result)){
		return result;
	}else{ //aply default -> HubSpot property is mapped to a field of the same name
		return key;	
	}
}

function string format_query_parameters(map[string,string] parameters){
	string[] paramNames = getKeys(parameters);
	string[] paramList = [];
	foreach(string name : paramNames){
		string value = parameters[name];
		if(not isBlank(value)){
			paramList.append(concat(name,"=",parameters[name]));
		}
	}
	if(not isEmpty(paramList)){
		return "?" + join("&",paramList);
	}
	return "";
}

function string get_field_case_insensitive (map[string,string] arr, string value){
	
	if(arr == null || value == null){
		return null;
	}
	
	value = trim(upperCase(value));
	
	string[] keys = getKeys(arr);
	
	for(integer i = 0; i<length(keys); i++){
		string keyUC = trim(upperCase(keys[i]));
		if(keyUC == value){
			return arr[keys[i]];
		}	
	}
	
	return null;
}