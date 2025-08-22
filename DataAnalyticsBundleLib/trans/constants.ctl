string[] METADATA_SHARED_FIELDS = [ "delimiter","size" ];

string[] ALPHABET_CHARACTERS = ["a-zA-Z","0-9","\\ "];
string[] QUOTATION_CHARACTERS = ['"',"'"];
string[] DATE_FORMAT = [
	"","yyyy-MM-dd","yyyy-M-d","yyyy/MM/dd","yyyy/M/d","MM/dd/yyyy","M/d/yyyy","MM-dd-yyyy","M-d-yyyy",
	"dd/MM/yyyy","dd-MM-yyyy","dd.MM.yyyy","d/M/yyyy","d-M-yyyy","d.M.yyyy","dd.MM.YYYY"
];

string[] TIME_FORMAT = [
	"","HH:mm:ss","HH:mm"
];

decimal DENSITY_WEIGHTING = 1d;	// > 1 will increase importance when characters are in close clusters
decimal OCCURANCE_WEIGHTING = 1d; // > 1 will increase importance of a character based on its occurance

integer DEFAULT_SIZE = 12;
integer DEFAULT_SCALE = 2;
string DEFAULT_FIELD_NAME = "field";

string format; // This is hack for not-referenced parameters of CTL functions
