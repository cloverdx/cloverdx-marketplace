function string fixNameCase(string input) {
	if (input == null) {
		return null;
	}
	
	if (input == "") {
		return "";
	}
	
	return upperCase(left(input, 1)) + substring(lowerCase(input), 1);
}

const number SIGMA = 1;
function integer getRandomIntGaussian(integer min, integer max) {
	// This is not entirely correct since generating values from truncated normal distribution is not so easy, but it should work fine.
	// We are using "5 sigma" rule for Normal distribution - we clamp the output to range of [-5*sigma, 5*sigma] - this will cover 99.99994% of values.
	// In our case we are using standard normal distribution generator provided by Clover, so we clamp its output to range [-5, 5] and then "stretch"
	// this to the desired range.
	number g = abs(randomGaussian()); // Use abs so that only clamping to [0, 5] is needed.
	if (g > 5.0 * SIGMA) {
		g = 5.0 * SIGMA;
	}
	
	integer res = double2integer((max - min) * g / 5.0 / SIGMA) + min;
	return res;
}

function string getRandomListItem(string[] items) {
	if (items == null || length(items) == 0) {
		return null;
	}
	
	return items[randomInteger(0, length(items) - 1)];
}

function integer getRecordCount(integer userProvidedValue) {
	integer previewSize = str2integer(getParamValue("NUM_OF_PREVIEW_RECORDS")) : null;
	if (previewSize == null || previewSize == 0) {
		return userProvidedValue;
	}
	
	return previewSize;
}

// Replace all white-space characters with single regular space.
function string normalizeSpaces(string input) {
	return replace(input, "\\s+", " ");
}

// In many local names we have "x.y" - a space is missing after the period character. Fix this to be "x. y". 
function string fixPeriods(string s) {
	string result = replace(s, "\\.", ". ");
	return trim(normalizeSpaces(result));
}

// Extracts language code from the URL of the file with variety of entities.
// This assumes that URL looks like .../data-in/TYPE/LANG/... - it basically looks at path element following the type.
function string getLanguageCodeFromURL(string url, string type) {
	string result = replace(url, ".*/data-in/" + type + "/", "");
	result = replace(result, "/.*", "");
	return upperCase(result);
}

function string trimToNull(string s) {
	string t = trim(s);
	if (t == "") {
		return null;
	}
	
	return t;
}
