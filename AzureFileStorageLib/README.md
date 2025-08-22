# <span class='tabLabel'>About</span>

Set of subgraphs implementing basic file operations over Azure File Storage.

# Description
The library supports the following operations:
 - Copy file
 - Create file
 - Delete file
 - Get file
 - Get file properties
 - List files

# Properties
Name: AzureFileStorageLib  
Label: Azure File Storage  
Author: CloverDX  
Version: 1.2  
Compatible: CloverDX 6.0 and newer  

# Tags
azure file storage AFS reader writer file-manipulation cloud-storage

# <span class='tabLabel'>Documentation</span>

## Common parameters

| Parameter label | Parameter name | Description | Required | Default Value |
| :--- | :--- | :--- | :--- | :--- |
|Storage Account |STORAGE_NAME | A storage account provides a unique namespace in Azure for your data.|true|||
|File Share|SHARE_NAME|Name of the specific share.|true||
|SAS token|SAS_KEY|Shared access signatures (SAS) - secure delegated access to resources in your storage account.|true||
|File storage address|STORAGE_FILE_ADDRESS|URL of the Azure Endpoint|true|*file.core.windows.net*||
|Debug mode|DEBUG|Boolean value, if is set to true, provides more debug information|true|*false*||

## Common error record fields

| Field name | Description  |
| :--- | :--- |
| statusCode | Azure API HTTP status code |
| errorMessage | Short error message. |
| errorDetails | Further error details. |
| rawErrorResponse | Usually a full XML content of the HTTP response returned by the Azure API endpoint. |

# Components

Below is a description of each subgraph included in the library.

## CopyAzureFSFiles

Implementation of the *Copy File* operation, which copies a blob or file to a destination file within the storage account.
Source and target file paths can be provided either via input port, or configuration attributes.

### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
|Source file path |File path which it will be copied from (on the target)|yes (or provide input port)||
|Target file path |File path on the target system where the file will be copied to.|yes (or provide input port)||

### Ports
| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Input 1 | No | Input information | Setting of the paths (source/target). If it is empty, paths from the component configuration are used. |
| Output 1 | Yes | Operation output | The initial configuration, along with the boolean result status and selected header fields extracted from the API response (x-ms-copy-id, x-ms-copy-status). |
| Output 2 | No | Error output | If an edge is attached to the port, the component will not fail; instead, it will provide error details in case of an error. Each error record will contain both Source and Target file paths as well as the Common error record fields (see above) |


## CreateAzureFSFiles

Implementation of the *Create File* operation. It creates a new file or replaces a file. When you call Create File, you only initialize the file.

### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
|File path |File which will be created to the target path|yes||
|Target path |Target path on the Azure FS|yes||
|Block size (bytes) |Number of bytes in one batch. The default value is appropriate for most cases.|yes|*4000000*|

### Ports
| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Input 1 | No | Input information | Setting of the path to the directory. If it is empty, the path from the component configuration is used. |
| Output 1 | Yes | Success output | File path of the successfully uploaded file with a status code. One output record is returned for one input record. |
| Output 2 | No | Error output | Error information about every unsuccessful input record. The error record contains the file path and standard error fields. |

## DeleteAzureFSFile

Implementation of the *Delete File operation*, which immediately removes the file from the storage account.

### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
|File path |Target path which will be deleted|yes||

### Ports
| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Input 1 | No | Input information | Setting of the target path to be deleted. If it is empty, the path from the component configuration is used. |
| Output 1 | Yes | Output | File path and boolean status of the deletion. |
| Output 2 | No | Output | File path, along with standard error fields. |

## GetAzureFSFile

Implementation of the *Get File* operation, which downloads a file from the system.
### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
|File path |File which will be downloaded from the target.|no||
|Target directory |Target file URL where the file will be stored.|yes||
|Chunk size (bytes) |Number of bytes in one batch. The default value is appropriate for most cases.|yes|*1000000*|

### Ports
| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Input 1 | No | Input information | Setting of the path to the target file. If it is empty, the path from the component configuration is used. |
| Output 1 | No | File output | If the edge is connected, the byte content of the file is returned.|
| Output 2 | No | Error output | Error information about every unsuccessful input record. The error record contains the file path and standard error fields. |

## GetAzureFSFileProperties

Implementation of the *Get File Properties* operation, which returns all user-defined metadata, standard HTTP properties, and system properties for the file. It does not return the content of the file.

### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
|File path |Path to the file on the target system|no||

### Ports
| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Input 1 | No | Input information | Setting of the path to the target file. If it is empty, the path from the component configuration is used. |
| Output 1 | Yes | File output | Properties of the target file.|
| Output 2 | No | Error output | Error information about every unsuccessful input record. The error record contains the file path and standard error fields. |

## ListAzureFSFiles

Implementation of the *List Directories and Files* operation, which returns a list of files or directories under the specified share or directory. It lists the contents only for a single level of the directory hierarchy.

### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
|Directory path |Directory path on the Azure FS target system which will be listed |yes |*empty*|

### Ports
| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Input 1 | No | Input information | Setting of the path to the directory. If it is empty, the path from the component configuration is used. |
| Output 1 | Yes | Success output | Listing of the directory (name, type, and size of the files). |
| Output 2 | No | Error output | Information about any error that occurred while the secret was written. The output contains the directory path and standard error fields. |

# <span class='tabLabel'>Installation & Setup</span>

### Online installation (Server connected to Internet)

1. In Server Console, navigate to Libraries > Install library from repository
2. Select Library Repository drop-down > CloverDX Marketplace
3. Check the box next to the libraries you want to install (if there are any dependencies, you can install all of them once - see Requirements above)
4. Click Install

### Offline installation (Server without Internet connection)

1. Download all the libraries you need from the CloverDX Marketplace (including dependencies, see Requirements above). You should get a ".clib" file for each library
2. Transfer the ".clib" file(s) to your offline Server machine (USB stick, ...)
3. In Server Console, navigate to Libraries > Install library from repository > Down arrow for more options > Browse local files...
4. Select the downloaded .clib files on your disk and install

It is necessary to set all the required parameters. All parameters can be set via subgraph parameters. All the following parameters should be available from the Azure administration console.
Notably SAS token can be found under Security + networking; Shared access signature tab.

