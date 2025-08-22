# <span class='tabLabel'>About</span>

This library contains algorithms capable of analyzing the structure of delimited text files (flat file/CSV) and JSON files. Results can be then materialized into either a JSON document or a CloverDX metadata file.

This library is particularly useful in building CloverDX solutions that automatically handle data that arrives in an unknown or varying format. This library can used to build a graph to analyze an incoming file's structure and create the necessary CloverDX metadata file(s). This graph, in turn, can then be used in a CloverDX jobflow that automatically detects the arrival of a data file, creates the metadata necessary to read that file, and then performs the desired processing. The result is a robust data process that can automatically adapt to changes in source data structure. 


# Description
The library supports the following operations:
- Structure analysis of a flat file
- Structure analysis of a JSON file
- CloverDX metadata generator (based on the analysis output) 
- JSON file generator (based on the analysis output) 

# Properties
Name: DataAnalyticsBundleLib  
Label: Data Analytics Bundle  
Author: CloverDX  
Version: 1.1.2  
Compatible: CloverDX 6.0 and newer    

# Tags

data flat-file json analyze structure metadata fmt

# <span class='tabLabel'>Documentation</span>

Components in this library come in two categories: analyzers and formatters - using common metadata ensures that every analyzer is compatible with every formatter. 

## AnalyzeFlatFile.sgrf

Analyses a flat file, using statistical methods tries to determine the line and field delimiters, quotation characters, field data types and other metadata properties.

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Input 1  | Alternative input (Optional)|
| Output 1 | Output             |

Metadata is set and auto-propagated out of the component on each port.

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| File URL | URL to the file to analyse. Alternatively, records representing individual lines of a file can be read from the first input port, if it is connected. | false | basic | file picker | Only files. |
| Structural analysis sample size | Sets the sample size (number of lines from the beginning of the input file) for structural analysis. Should be set to at least 2 to read the file header and first row of data. | false | advanced | integer |   |
| Data type sample size | Sets the sample size (number of lines from the beginning of the input file, skipping the header row) for extended data type analysis. Increasing the value may produce more accurate results at the expense of computational resources. | false | advanced | integer |   |
| Allowed delimiters | Provide a list (one per line) of allowed field delimiters. If not provided default regex expression will be used, matching everything but alphanumerical characters and quotation characters. | false | advanced | multiline string |   |
| Charset | Defines character-set of the input file. | false | advanced | charset |   |
| Locale | Locale that will be used for data type analysis. Particularly important for correct date and time types recognition. | false | advanced | locale |   |
| Time zone | Time zone that will be used for data type analysis. Particularly important for correct date and time types recognition. | false | advanced | time zone |   |

## AnalyzeJsonFile.sgrf

Analyses a JSON file, producing metadata information per each object type found. Additionally creates a mapping that can be used by a JSON extract component and outputs it on the second port.

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Input 1  | Alternative input (Optional)|
| Output 1 | Metadata output   |
| Output 2 | Mapping output (Optional)|

Metadata is set and auto-propagated out of the component on each port.

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| File URL | URL to the file to analyse. Alternatively, the contents of the file can be streamed from the first input port, if it is connected (the file URL is set to port:$0.data:stream automatically). | false | basic | file picker | Only files. |
| Data type sample % | Sets the percentage of JSON objects that will be used for data type analysis. The whole file will still be read and used for other parts of the analysis. | false | basic | integer |   |
| Charset | Defines character set of the input file. | false | advanced | charset |   |
| Locale | Locale that will be used for data type analysis. Particularly important for correct date and time types recognition. | false | advanced | locale |   |
| Time zone | Time zone that will be used for data type analysis. Particularly important for correct date and time types recognition. | false | advanced | time zone |   |

## FmtFormatter.sgrf

Processes data produced by an analyzer and produces output in the form of a CloverDX metadata file.

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Input 1  | Input              |
| Output 1 | Alternative output (optional) |

Metadata is set and auto-propagated out of the component on each port. Alternative input metadata (extending the default metadata) may be provided by the user (enabled by parameter).

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| File URL | File URL for the output file(s). Select a directory rather than a file if you expect more than 1 metadata definition on input. | false | basic |   |   |
| Default record delimiter | Sets the record delimiter for the resulting metadata. | false | advanced |   |   |
| Use custom metadata | Enable if you want to provide extended metadata on input - the component will not propagate the default metadata. | false | basic | bool |   |
| Force File URL as directory | If enabled, the component will use the path to the parent directory of FILE_URL if it does not point to directory already. | false | advanced | bool |   |
| Default metadata name | Sets default metadata name. | false | basic |   |   |

## JsonFormatter.sgrf

Processes data produced by an analyzer and produces output in the form of a JSON file.

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Input 1  | Input              |
| Output 1 | Alternative output (optional) |

Metadata is set and auto-propagated out of the component on each port. Alternative input metadata (extending the default metadata) may be provided by the user (enabled by parameter).

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| File URL | File URL for the output file. | false | basic |   |   |
| Use custom metadata | Enable if you want to provide extended metadata on input - the component will not propagate the default metadata. | false | basic | bool |

## FmtReader.sgrf

Read a CloverDX metadata file. Default output metadata match those produced by the analysers. Can read custom properties if custom metadata is provided.

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Input 1  | Input              |
| Output 1 | Alternative output (optional) |

Metadata is set and auto-propagated out of the component on each port. Alternative output metadata (extending the default metadata) may be provided by the user (enabled by parameter).

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| Fmt file URL | File URL for the input file. | false | basic |   |   |
| Use custom metadata | Enable if you want to provide extended metadata on output - the component will not propagate the default metadata. | false | basic | bool |

# <span class='tabLabel'>Installation & Setup</span>

### Online installation (Server connected to Internet)

1. In Server Console, navigate to Libraries > Install library from repository
2. Select Library Repository dropdown > CloverDX Marketplace
3. Check the box next to the libraries you want to install (if there are any dependencies, you can install all of them once - see Requirements above)
4. Click Install

### Offline installation (Server without Internet connection)

1. Download all the libraries you need from the CloverDX Marketplace (including dependencies, see Requirements above). You should get a ".clib" file for each library
2. Transfer the ".clib" file(s) to your offline Server machine (USB stick, ...)
3. In Server Console, navigate to Libraries > Install library from repository > Down arrow for more options > Browse local files...
4. Select the downloaded .clib files on your disk and install


