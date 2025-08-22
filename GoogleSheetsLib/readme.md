# <span class='tabLabel'>About</span>

Connects to Google Sheet service, downloads contents and parses according to provided metadata.

# Description

The library supports the following operations:
 - Read and parse Google Sheets document
 
The reader will return parsed data on **output port 0**, **output port 1** will return sheet information (name, width and total row count). Specific sheets can be specified as _COMMA_ separated list.

# Properties
Name: GoogleSheetsLib  
Label: Google Sheets  
Author: CloverDX  
Version: 1.0  
Compatible: CloverDX 5.14 and newer  

# Tags

google spreadsheet sheet reader excel oauth2

# <span class='tabLabel'>Documentation</span>

## GoogleSheetsReader

A component that reads data directly from Google Sheets and parses the output to the prepared metadata.

### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
| Spreadsheet ID         | Unique ID of the sheet. It is a URL fragment, which can be found upon opening file in browser URL or on Google Drive.<br>Example of the URL with Spreadsheet ID: https://docs.google.com/spreadsheets/d/`1e1lXXXXXXXXXXXXXXXXXXXXXXZ8ZfSCiyUAbm9a25yY`/                                                                    | yes | *no*     |
| Sheet name             | Name of the sheet                                                                                                      | no  | *no*     |
| Data offset            | Number or skipped rows in the sheet.                                                                                   | yes | 0        |
| Connection URL         | URL of OAuth2 connection to be used for connecting to the Google Docs.                                                 | yes | *no*     |
| Data policy            | Specifies handling of misformatted or incorrect data - strict or lenient                                               | yes | *Strict* |

### Ports
| Port     | Required  | Used for             | Description                                                            |
|----------|-----------|----------------------|------------------------------------------------------------------------|
| Output 0 | Yes       | Standard output      | Successfully read records.                                             |
| Output 1 | Yes       | Sheet information    | Information about the sheet (name, width and total row count)          |


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

- Authorized Google OAuth2 connection. <a href="https://doc.cloverdx.com/latest/designer/oauth2-connections.html#id_oauth2_connection_creating" target="_blank">See documentation for more details</a>. <br>In the connection, specify the following scope: https://www.googleapis.com/auth/drive
- On Google Cloud, you need to enable Google Sheets API. (Google Cloud -> Enabled APIs & services -> Google Sheets API )

<a href="https://www.youtube.com/watch?v=8R5ZUAoYE7I" target="_blank">Watch this video to learn how to set up OAuth2 for use in CloverDX Libraries.</a>
