# <span class='tabLabel'>About</span>

This library contains components for interaction with Dropbox.

# Description
The library supports the following operations:
- Upload file to Dropbox
- List Dropbox folder
- Purge Dropbox folder
- Get Dropbox download link

# Properties
Name: DropboxLib  
Label: Dropbox  
Author: CloverDX  
Version: 1.0.1  
Compatible: CloverDX 6.0 and newer  

# Tags
connector dropbox reader writer cloud-storage file-manipulation

# <span class='tabLabel'>Documentation</span>

## Common parameters

All parameters can be set via component parameters. Parameters that have a corresponding metadata field can also be set via record. Parameter values sent via record have priority.  
Common parameters are displayed in *Security* and *Advanced* sections of the component.

| Parameter label | Description | Required | Default Value | Metadata Field|
| :--- | :--- | :--- | :--- | :--- |
|OAuth2 connection |URL of OAuth2 connection to be used for connecting to the Dropbox|true|||
|API URL|URL of the Dropbox API|true|*ht<span>tps://</span>api.Dropboxapi.com/2*||
|Content API URL|URL of the Dropbox content API|true|*https://content.Dropboxapi.com/2*||
|Retry count|Retry count for upload file to dropbox|true|3||
|Date format|Format of timestamps used in the API calls|true|*yyyy-MM-dd'T'H:m:s'Z'*||

# Components

Below is a description of each component included in the library. 

## UploadFileToDropbox

This component uploads a file to specified Dropbox folder. It can also generate a download link for the uploaded file. Configuration of the component through fields from the input port takes precedence over the configuration defined in the component's parameters.

### Parameters

| Parameter label | Description | Required | Default Value | Metadata Field|
| :--- | :--- | :--- | :--- | :--- |
|Source path    |Path to the local file to be uploaded.|true||sourceFileUrl|
|Write mode     |Selects what to do if the file already exists.<br>Allowed values: *add*,*overwrite*,*update*.|false|*add*|writeMode|
|Target path    |Path in the user's Dropbox to save the file.<br>Format: /directory/directory/file|true||targetFileUrl|
|Autorename     |If there's a conflict, as determined by mode, have the Dropbox server try to autorename the file to avoid conflict.|false|*false*|autorename|
|Mute           |Normally, users are made aware of any file modifications in their Dropbox account via notifications in the client software.<br>If true, this tells the clients that this modification shouldn't result in a user notification.|false|*false*|mute|
|Generate download link|set to true if a download link should be generated.|false|*true*|getLink|
|Link expiration days|Expiration of the generated Download links. If set to 0, link will not expire.<br>Usage of this feature is limited only for accounts with Dropbox Professional plan and upwards.|false|*30*|
|Upload session threshold|Size limit (in bytes) for using upload session instead of uploading the file in single request.|false|*1000000*|
|Chunk size|Max file size (in bytes) to upload with single request.<br>Dropbox limit is 130MB.|false|*500000*|
|Set password|Set to true if the download link should be password protected.|false|*false*|setPassword|
|Link password|Password that will be set for the download link.<br>If empty and "Set password" is true, random password will be generated.|false||linkPassword|

## ListDropboxFolder

This component returns a list of files/folders present in the specified Dropbox path. Configuration of the component through fields from the input port takes precedence over the configuration defined in the component's properties.

### Parameters

| Parameter label | Description | Required | Default Value | Metadata Field|
| :--- | :--- | :--- | :--- | :--- |
|List path|Path to the folder to list. Leave empty for root path.|true||listPath|
|Recursive|Enables recursive search|true|*false*| recursive|

## PurgeDropboxFolder

This component deletes files from the specified Dropbox path. Configuration of the component through fields from the input port takes precedence over the configuration defined in the component's properties.

### Parameters
| Parameter label | Description | Required | Default Value | Metadata Field|
| :--- | :--- | :--- | :--- | :--- |
|Purge path|Path to directory to be purged.|true||purgePath|
|File age in days|Specifies the number of days since the file was last modified to delete it.<br>If set to *0* all files will be deleted.|true|*3*||
|Expired only|If enabled, only files with no or expired download links will be deleted|true|*false*||
|Recursive|Set to true if the purge should be recursive.|true|*false*|recursive|

## GetDropboxDownloadLink

This component creates new or updates existing download link for Dropbox file. Password and expiration can be set for the generated links. Configuration of the component through fields from the input port takes precedence over the configuration defined in the component's properties.
**Note:** Usage of this component is limited only for accounts with Dropbox Professional plan and upwards.

### Parameters

| Parameter label | Description | Required | Default Value | Metadata Field|
| :--- | :--- | :--- | :--- | :--- |
|File path|Path to Dropbox file to get the link for.|true||filePath|
|Password length|Length of the auto-generated password|false|*10*||
|Link expiration days|Expiration of the generated Download links. If set to 0, the link will not expire.|false|*30*|
|Set password|Set to true is the download link should be password protected.|true|*false*|setPassword|
|Link password|Password that will be set for the download link.<br>If empty and "Set password" is true, a random password will be generated.|false||linkPassword|

## DownloadFileFromDropBox
This component downloads a file (or a folder as a .zip) from specified Dropbox path. Configuration of the component through fields from the input port takes precedence over the configuration defined in the component's properties.

### Parameters
| Parameter label | Description | Required | Default Value | Metadata Field|
| :--- | :--- | :--- | :--- | :--- |
|File path|Path to Dropbox file/folder to be downloaded.|true||filePath|
|Target directory|Path to directory where the downloaded file will be stored.|true||targetDir|
|Zip|Enable if the target path is a folder and should be downloaded as a .zip.|true|*false*|zip|
|Keep original order|Make sure the records are sorted the same way on input an output ports.<br>When enabled, all files have to be downloaded before any records are sent to output port.|false|*false*||



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

## Configuration

- Dropbox account with authorized APP set up. <a href="https://www.Dropbox.com/developers/reference/getting-started#app%20console" target="_blank">See Dropbox documentation for more details.</a>
- Authorized OAuth2 connection. <a href="https://doc.cloverdx.com/latest/designer/oauth2-connections.html#id_oauth2_connection_creating" target="_blank">See documentation for more details.</a>
- List of required application permissions (scopes): 
    - files.metadata.write
    - files.metadata.read
    - files.content.write
    - files.content.read
    - sharing.write
    - sharing.read

<a href="https://www.youtube.com/watch?v=8R5ZUAoYE7I" target="_blank">Watch this video to learn how to set up OAuth2 for use in CloverDX Libraries.</a>
