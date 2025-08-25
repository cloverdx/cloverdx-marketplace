# <span class='tabLabel'>About</span>

Google Drive suite, providing functionality related to Google Drive management.
Provides ability to list Google Drive content, download, and upload files.

# Description
The library supports the following operations:
- List Google Drive 
- Write files to Google Drive
- Download files/directories from Google Drive

# Properties
Name: GoogleDriveLib  
Label: Google Drive  
Author: CloverDX  
Compatible: CloverDX 5.14 and newer  
Version: 1.0.1  

# Tags
google drive connector reader writer file-manipulation cloud-storage oauth2

# <span class='tabLabel'>Documentation</span>

## GoogleDriveListFiles
Lists all files on Google Drive.

### Attributes
| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| CONNECTION_URL | Specify path to predefined google oauth2 connection | true | basic | File URL  | expecting .cfg file  |

### Ports

| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Output 0 | Yes | File information | Listing of the directory.|


## GoogleDriveWriter
Writes a file onto Google Drive.

### Attributes
  | Parameter label | Description | Required? | Category | Editor type | Editor type details |
  | --- | --- | --- | --- | --- | --- |
  | CONNECTION_URL | Specify path to predefined google oauth2 connection | true | basic | File URL  | expecting .cfg file  |  

### Ports

| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Input 0 | Yes | File information | File URL of the file which will be stored to Google Drive. Please note, use absolute path to the file, e.g. using function [``toAbsolutePath()``](https://doc.cloverdx.com/latest/designer/miscellaneous-functions-ctl2.html#id_ctl2_toabsolutepath).|


## GoogleDriveGetFile
Downloads a file onto the file system from Google Drive. Note that Google Docs, Sheets and Slides are converted to their MS Office equivalents and are limited to 10 MB max size. To avoid this limit, save your document as Word (.docx) or Excel (.xlsx) file onto Google Drive.
File ID can be provided either on the input metadata or input parameter. The input port has priority.


### Attributes

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| CONNECTION_URL | Specify path to predefined google oauth2 connection | true | basic | File URL  | expecting .cfg file  |
| FILE_ID | Google drive file ID | true | basic | string  |   |
| OUTPUT_FILE_DIRECTORY | Specify target file for the file | true | basic | File URL  | expecting directory |

### Ports

| Port | Required | Used for | Description |
|----------|----------|----------------|-------------|
| Input 0 | Yes | File ID | File ID used for downloading.|
| Output 0 | Yes | File information | Listing of the directory.|
| Output 1 | No | Error port | Runtime errors which could possibly occur during the processing.|


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

## Pre-Requisites
* The pre-existing application registered in GCP, with access to Google Drive
* Google OAuth 2.0 connection set up and configured in CloverDX.
* Google API: Files v3  
* User permissions for *https://www.googleapis.com/auth/drive* scope

Set up your OAuth 2.0 Google connection in CloverDX and reference its location on the disk as an input parameter.

<a href="https://www.youtube.com/watch?v=8R5ZUAoYE7I" target="_blank">Watch this video to learn how to set up OAuth2 for use in CloverDX Libraries.</a>
