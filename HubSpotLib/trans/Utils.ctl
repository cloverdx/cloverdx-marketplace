function string singular(string noun){
	if(endsWith(noun,'ies')){
		return left(noun, length(noun)-3) + "y";
	}else if(endsWith(noun,'s')){
		return left(noun, length(noun)-1);
	}else{
		return noun;
	}
}