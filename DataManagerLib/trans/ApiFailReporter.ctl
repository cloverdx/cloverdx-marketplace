function void mapErrorMessage() {
	string content = $in.0.content;
	variant apiErrorMessage = content != null ? parseJson($in.0.content) : null : null;
	string messageText = content != null ? cast(apiErrorMessage["message"], string) : null;
	
	$out.0.errorMessage = "HTTP " + $in.0.statusCode + (messageText == null ? "" : ": " + apiErrorMessage["message"]);
}
