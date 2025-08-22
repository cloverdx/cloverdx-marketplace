# <span class='tabLabel'>About</span>

Library enables user to connect to personal OneDrive and company's SharePoint. Listing SharePoint document libraries (drives) and items, downloading and uploading files.

# Properties
Name: OneDriveLib  
Label: OneDrive  
Author: CloverDX  
Version: 1.1.1  
Compatible: CloverDX 6.0  

# Tags
onedrive sharepoint microsoft-graph-api file-operations  

# <span class='tabLabel'>Documentation</span>

## getSitesAndPersonalDrive

Get all accessible SharePoint Sites and Personal OneDrive.  

### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | SharePoint Sites ID and name. |
| Output 2 | Yes      | Data output  | Personal OneDrive ID and name. |
| Output 3 | No       | Error output | Error information. |

## ListDrives

Lists all Document libraries (drives) from specified SharePoint Site.  
Required parameters:  
- SharePoint Site ID  

### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Document libraries (drives) information. |
| Output 2 | No       | Error output | Error information. |

## ListItems

List Items from SharePoint Document library or Personal OneDrive.  
Required parameters:  
- Drive ID  
- Item ID (can be set to *root* to list top level structure)  

### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Items information. |
| Output 2 | No       | Error output | Error information. |

## Download

Downloads files from SharePoint Document library or Personal OneDrive. Only the child items of the Item ID are downloaded; the Item itself is not.

Required parameters:  
- Drive ID  
- Item ID  (must be ID of a directory - files directly would not be downloaded)
- Local directory  

Optional parameters:  
- List files  
- Create parent directories  
- List files delimiter  

Optional input via dictionary:
- listFiles (takes precedence over the parameter)

### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Information about downloaded files. |
| Output 2 | No       | Error output | Error information. |

## Upload

Uploads file to SharePoint Document library or Personal OneDrive.  
Required parameters:  
- Drive ID  
- Item ID  
- File path  

**NOTE:** Component requires connection with authorized _Sites.ReadWrite.All_ scope. 

### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Information about uploaded file. |
| Output 2 | No       | Error output | Error information. |


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

## Requirements
User account in Microsoft Azure Active Directory with assigned *Application Developer* or *Application Administrator* role.

## Microsoft Azure Active Directory set up steps

#### 1. Create new Application  
Application is used to login to your Azure Active Directory tenant and to access resources using the MS Graph API.

*Azure Active Directory > App registrations > New registration*  
- Provide name for your application  
- In the *Supported account types* select who can access this application and API (single tenant, multi tenant, personal accounts)  
- Set redirect URL. Select *Web* platfrom and provide redirect URL to your CloverDX Server   
(for more information about redirect URL see *Create OAuth2 connection in CloverDX sandbox* section)

Application has its own client ID (*ApplicationName > Overview > Application (client) ID*).  

#### 2. Generate Secret for Application
*Azure Active Directory > App registrations > ApplicationName > Certificates & secrets*  
Select *New client secret*.  
Provide description and expiration period and add new secret.  

#### 3. Set API permissions for Application.  
*Azure Active Directory > App registrations > ApplicationName > API permissions*  
Select *Add a permission*.  
Choose *Microsoft Graph API* and *Delegated permissions*.

List of required permissions:  
- *Files.ReadWrite.All* (Have full access to all files user can access)  
- *Sites.Read.All* (Read items in all site collections)  

After adding permission click the *Grant admin consent for YourTenantName* button.  
In the permissions table the *Status* field will contain the *Granted for YourTenantName* message.  

## CloverDX Server set up steps

#### 1. Create OAuth2 connection in CloverDX sandbox  
Please refer to CloverDX documentation to article about [Creating OAuth2 Connection](https://doc.cloverdx.com/latest/designer/oauth2-connections.html#id_oauth2_connection_creating).  

In **Edit OAuth2 Connection window** set *Azure* provider and add values to all properties:  
- Client ID (Your application Client ID)  
- Client Secret (Your application Client Secret)  
- Scopes (Permissions of the connection)  
The *ht<span>tps://</span>graph.microsoft.com/.default* can be used as default value.  
The returned token will contains the granted scopes for the application.  
Otherwise the exact list of scopes separated by spaces can be used (e.g. *ht<span>tps://</span>graph.microsoft.com/Files.ReadWrite.All Sites.Read.All*)  
- Tenant ID (Identifier of Azure subscription)  
*Azure Active Directory > Overview > Tenant ID*  
- Redirect URL (advanced tab)  
ht<span>tps://</span>hostname:port/clover/oauth2 (e.g. ht<span>tp://</span>localhost:8083/clover/oauth2 for the local server).  
Note: This needs to be https with the exception of localhost.  

Click *Authorize* button. Provide user credentials and confirm client application access permissions.  
After successful authorization the special file which contains OAuth2 connection tokens is created (*ConnectionName.tokens*).  
Connection can be re-authorized.  

#### 2. Set OAuth2 connection in Library Configuration  
- Provide absolute path to OAuth2 configuration file saved in sandbox on CloverDX Server (e.g. sandbox://Sandbox_name/conn/OAuth2_connection.cfg).  
- Confirm your settings by pressing the **Save changes** button.  

#### Second Authentication method using username and password
User can also authenticate using username and password directly.  
In Library configuration provide values for all these parameters: Tenant ID, Client ID, Client Secret, Scope.  
Parameter values provided by user: Username, Password.  
Note: OAuth2 takes precedence over a username and password authentication method.  

## Library initialization
- Run **Initialize library**.  

Initialization graph (*getSitesAndPersonalDrive.grf*) generates values for *SharePoint Site ID* and *Drive ID* parameters.  
Values are in form of Key-Value pair (Site or Drive ID and its name).  
After the graph run finished, press *Refresh libraries* button to see the generated values.  
