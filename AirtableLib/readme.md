# <span class='tabLabel'>About</span>

This library contains components for interaction with Airtable.  

# Description
Library provides subgraphs to streamline workflows involving Airtable platform. Airtable APIs are integrated within the different CloverDX subgraphs. Provides Read/Write records of an Airtable base, creation of new bases and tables, creation of CloverDX metadata from Airtable base, and getting the structure of an Airtable base in JSON format.  
You can pass the input parameters to the subgraphs either via subgraph properties or input edge. Incoming records from an edge have priority over input parameters. Metadata are auto-propagated from the subgraph.  

# Features

- CreateBase: creates a base in Airtable, base structure in JSON format needs to be provided.  
- CreateTable: creates a table in Airtable base, table structure in JSON format needs to be provided.  
- GenerateMetadata: generates clover metadata for each table in a base from an Airtable structure.  
- GetBaseSchema: gives you schema of a specific Airtable base.  
- ReadData: reads records from an Airtable table.  
- WriteData: writes records into an Airtable table.  

# Properties
Name: AirtableLib  
Label: Airtable  
Author: CloverDX  
Version: 1.1  
Compatible: CloverDX 6.0 and newer  

# Tags
connector airtable reader writer oauth

# <span class='tabLabel'>Documentation</span>

## CreateBase.sgrf

CreateBase component creates a base in Airtable. A base structure in JSON format needs to be provided. You can refer to https://airtable.com/developers/web/api/create-base for an example of the Airtable base structure in JSON. Component GetBaseSchema can also be useful for preparing this structure during runtime, although some edits will have to be made like stripping ids the for example.  

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 1  | No       | Data input     | Input parameters for the target API. |
| Output 1 | Yes      | Data output    | Response about base creation. |
| Output 2 | No       | Data output    | Error information. |

### Parameters

- Airtable OAuth Connection must be set in subgraph 'Security' properties, all other input parameters can be set either in subgraph properties or with input edge.  
- You can find all parameters in subgraph properties. When you hover over their name you get a detailed description. They are separated into three categories Basic, Advanced and Security.  
- Basic category: 'Workspace Id' and 'Base name' parameters are required and can be provided with either the input edge or in subgraph categories.  
- Security category: Airtable OAuth Connection URL 
- Advanced category: Tables parameter, Airtable structure in JSON format, it is generally recommended to pass this information via input edge. 
Hover over property name for details.   

## CreateTable.sgrf

CreateTable component creates a table in Airtable base. A table structure in JSON format needs to be provided. You can refer to https://airtable.com/developers/web/api/create-table for an example of the Airtable table structure in JSON. Component GetBaseSchema can also be useful for preparing this structure during runtime, although some edits will have to be made like stripping ids the for example.  

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 1  | No       | Data input     | Input parameters for the target API. |
| Output 1 | Yes      | Data output    | Response about table creation. |
| Output 2 | No       | Data output    | Error information. |

### Parameters

- Airtable OAuth Connection must be set in subgraph 'Security' properties, all other input parameters can be set either in subgraph properties or with input edge.  
- You can find all parameters in subgraph properties. When you hover over their name you get a detailed description. They are separated into three categories Basic, Advanced and Security.  
- Basic category: 'Base Id' , 'Table Name' and 'Table Description' parameters are required and can be provided with either the input edge or in subgraph categories.  
- Security category: Airtable OAuth Connection URL.
- Advanced category: Tables Fields parameter, Airtable structure in JSON format, it is generally recommended to pass this infomation via input edge.  

## GenerateMetadata.sgrf

GenerateMetadata component generates clover metadata for each table in a base from an Airtable structure for any givem Base Id. Output path directory can be also specified e.g. '${META_DIR}/airtable_metadata/' because Airtable allows all kinds of special characters in the table name, the name of the clover metadata is taken from 'Table Id' property. Airtable specific data types are converted to their relevant counterparts.  

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 1  | No       | Data input     | Input parameters for the target API. |
| Output 1 | Yes      | Data output    | Response about metadata creation. |
| Output 2 | No       | Data output    | Error information. |

### Parameters

- Airtable OAuth Connection URL must be set in subgraph 'Security' properties, all other input parameters can be set either in subgraph properties or with input edge.  
- You can find all parameters in subgraph properties. When you hover over their name you get a detailed description. They are separated into three categories Basic, Advanced and Security.  
- Basic category: 'Base Id' and 'Metadata Path' parameters are required and can be provided with either the input edge or in subgraph categories.  
- Security category: Airtable OAuth Connection URL.

## GetBaseSchema.sgrf

GetBaseSchema component gives you the schema of a specific Airtable base.  

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 1  | No       | Data input     | Input parameters for the target API. |
| Output 1 | Yes      | Data output    | Response with base structure. |
| Output 2 | No       | Data output    | Error information. |

### Parameters

- Airtable OAuth Connection URL must be set in subgraph 'Security' properties, all other input parameters can be set either in subgraph properties or with input edge.  
- You can find all parameters in subgraph properties. When you hover over their name you get a detailed description. They are separated into three categories Basic, Advanced and Security.  
- Basic category: 'Base Id' parameters are required and can be provided with either the input edge or in subgraph categories.  
- Security category: Airtable OAuth Connection URL.

## ReadData.sgrf

ReadData component reads records from an Airtable table. Output metadata must have same structure as the Airtable table, subgraph will try to match any fields from Airtable into fields in clover metadata. You can use GenerateMetadata.sgrf subgraph to generate your metadata. Paging for API calls is implemented, Airtable limits the amount of requests per second so reading large amount of data can take a while.  

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 1  | No       | Data input     | Input parameters for the target API. |
| Output 1 | Yes      | Data output    | Response with data records. |

### Parameters

- Airtable OAuth Connection URL must be set in subgraph 'Security' properties, all other input parameters can be set either in subgraph properties or with input edge.  
- You can find all parameters in subgraph properties. When you hover over their name you get a detailed description. They are separated into three categories Basic, Advanced and Security.  
- Basic category: 'Base Id' and 'Table Id' parameters are required and can be provided with either the input edge or in subgraph categories.  
- Security category: Airtable OAuth Connection URL.

## WriteData.sgrf

WriteData component writes records into an Airtable table. Clover metadata have to be specified on the input edge, the structure of the clover metadata on the input must be appropriate to the Airtable table structure that it's writing into. You can use GenerateMetadata.sgrf subgraph to generate your metadata. Paging for API calls is implemented, Airtable limits the amount of requests per second so writing large amount of data can take a while.  

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 1  | No       | Data input     | Input parameters for the target API. |
| Output 1 | Yes      | Data output    | Response about records written. |
| Output 2 | No       | Data output    | Error information. |

### Parameters

- Airtable OAuth Connection URL must be set in subgraph 'Security' properties, all other input parameters can be set either in subgraph properties or with input edge.  
- You can find all parameters in subgraph properties. When you hover over their name you get a detailed description. They are separated into three categories Basic, Advanced and Security.  
- Basic category: 'Base Id' and 'Table Id' parameters are required and can be provided with either the input edge or in subgraph categories.  
- Security category: Airtable OAuth Connection URL.  

# <span class='tabLabel'>Installation & Setup</span>

### Online installation (Server connected to Internet)

1. In Server Console, navigate to Libraries > Install library from repository
2. Select Library Repository dropdown > CloverDX Marketplace
3. Check the box next to the libraries you want to install
4. Click Install

### Offline installation (Server without Internet connection)

1. Download all the libraries you need from the CloverDX Marketplace. You should get a ".clib" file for each library
2. Transfer the ".clib" file(s) to your offline Server machine (USB stick, ...)
3. In Server Console, navigate to Libraries > Install library from repository > Down arrow for more options > Browse local files...
4. Select the downloaded .clib files on your disk and install

### Configuration

By default, the library is pre-configured to utilize a built-in connection. This connection can be set via the CloverDX Server UI, Configuration tab of the library. The connection can also be authorized. Default path to the built-in connection is: `${CONN_DIR}/Airtable.cfg`.

For each component offered by the library, there is an option to override the connection. This feature is particularly useful in scenarios where you have created a connection independently. In such cases, you can simply reference the connection by its URL. The URL should adhere to the following format: `sandbox://<sandbox_code>/conn/Connection.cfg`.

#### How to setup OAUth Connection

1. Registering a new integration: Visit https://airtable.com/create/oauth to register a new integration.
2. Obtain Client ID and generate secret: Once the integration is registered, you will receive a Client ID. Generate a secret for this ID.

##### Settings of the OAuth Connection:

| Attribute      | Value |
|----------------|-------|
|*ClientID*      | Enter the ID of the integration obtained during registration.  |
|*Client Secret* | Input the generated ID.  |
|*Scopes*        | Specify the scopes you wish to allow, separated by a space. The full list includes: `data.records:read` `data.records:write` `data.recordComments:read` `data.recordComments:write` `schema.bases:read` `schema.bases:write` `user.email:read` `webhook:manage`.  |

In the *Advanced* tab, configure the following:

| Attribute               | Value |
|-------------------------|-------|
|*Authorization endpoint* | `https://airtable.com/oauth2/v1/authorize`          |
|*Token endpoint*         | `https://airtable.com/oauth2/v1/token`              |
|*Redirect URL*           | `https://<cloverdx_server>:<port>/oauth2/v1/token`  |
|*Use PKCE*               | Select `yes`                                        |

# <span class='tabLabel'>Versions</span>

Current version: 1.1

| Version  | Description                                                                  |
|----------|------------------------------------------------------------------------------|
|1.1       | Introduced OAuth connection support, Airtable API key deprecated by Airtable |     
|1.0       | Initial version						  		                              |     

