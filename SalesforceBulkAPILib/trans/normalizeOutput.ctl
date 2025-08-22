//#CTL2
// This transformation defines the way in which a single input record is normalized
// into multiple output records.
string [] rows;
string [] FirstRow;
map [integer, integer] fieldsMapping;

// This function is called for all input records.
// It parses each single input record and returns the number of records
// that should be created from such input record.
function integer count() {
	rows = split($in.0.content,'\n');
    FirstRow = split(rows[0],',');		
	
	return length(rows) - 1;
	
}

// This function creates new records for the output, based on single input record
// that has been parsed by the count() function.
// It is called count() times for each input record.
// The idx argument specifies which output record is being created,
// its values range is from 0 to count() - 1.
function integer transform(integer idx) {
	
	string [] row = split(rows[idx + 1],',');
	string field_type;
	string field_name;	
	string value;
	
	
	//map by order  
    for (integer i = 0; i<length($out.0);i++){
   	    field_type = getFieldType($out.0,i);
   	    field_name = getFieldName($out.0,i);
 	    value =chop(row [i], '"'); 

 	try{
		switch(field_type){
			case "string":
				setStringValue($out.0,i,value);
				break;
			case "date":
				setDateValue(
					$out.0,
					i,
					isBlank(value)?null:str2date(value,"iso-8601:dateTime")
				);
				break;
			case "long":
				long long_value;
				try{
					 long_value = isBlank(value)?null:str2long(chop_fractional_part_from_decimal_if_zero(value));
				}catch(CTLException ex){
					printLog(error, ex);
					raiseError(parsing_error(value,field_name,field_type,ex));
				}
				setLongValue(
					$out.0,
					i,
					long_value
				);
				break;
			case "integer":
				integer integer_value;
				try{
					integer_value = isBlank(value)?null:str2integer(chop_fractional_part_from_decimal_if_zero(value));
				}catch(CTLException ex){
					printLog(error, ex);
					raiseError(parsing_error(value,field_name,field_type,ex));
				}
				setIntValue(
					$out.0,
					i,
					integer_value
				);
				break;
			case "decimal":
				decimal decimal_value;
				try{
					decimal_value = isBlank(value)?null:str2decimal(value);
				}catch(CTLException ex){
					printLog(error, ex);	
					raiseError(parsing_error(value,field_name,field_type,ex));		
				}
				setDecimalValue(
					$out.0,
					i,
					decimal_value
				);
				break;
			case "number":
				double double_value;
				try{
					double_value = isBlank(value)?null:str2double(value);
				}catch(CTLException ex){
					printLog(error, ex);
					raiseError(parsing_error(value,field_name,field_type,ex));
				}
				setNumValue(
					$out.0,
					i,
					double_value
				);
				break;
			case "boolean":
				boolean boolean_value;
				try{
					 boolean_value = isBlank(value)?null:str2bool(lowerCase(value));
				}catch(CTLException ex){
					printLog(error, ex);
					raiseError(parsing_error(value,field_name,field_type,ex));
				}
				setBoolValue(
					$out.0,
					i,
					boolean_value
				);
				break;
			default:
				raiseError("[Not implemented] Conversion for field " + field_name + " of type " +  field_type + " not implemented");
		}
	 }
		catch(CTLException ex){
			printLog(error, ex);
			raiseError(parsing_error(value,field_name,field_type,ex));
 	 }	
		
 	}
    		   	
 
	    
	return OK;
}

function string chop_fractional_part_from_decimal_if_zero(string input){
			if(matches(input,'\d*\.0*')){
				return chop(input,'\.0*');
			}else{
				return input;
			}
		}  

function string parsing_error(string field_value, string field_name, string field_type, CTLException exception){
    return concat(
        "[Parsing error] Error parsing value of field ",
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
