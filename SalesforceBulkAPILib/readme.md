# <span class='tabLabel'>About</span>

The Salesforce Bulk API Library provides functionality for interacting with Salesforce Bulk API v2.0. It enables efficient and scalable data operations. This library is designed for high-volume data processing, allowing users to asynchronously **query, insert, update, upsert, and delete** records in Salesforce.  

## Key Features  
- **Asynchronous Data Processing** – Execute bulk operations, improving performance for large datasets.  
- **Flexible Data Input** – Supports query execution via input metadata or direct parameter input.  
- **Multiple Write Methods** – Process records via **stream-based input** or **file-based input** for better flexibility.  
- **Customizable Mapping** – Output data can be mapped either by **column names** or **position-based mapping** for greater control over data transformation.  
- **Error Handling & Monitoring** – Retrieve detailed error outputs and track job statuses for efficient debugging.  
- **OAuth 2.0 Authentication** – Securely connect to Salesforce using standard OAuth 2.0 authentication mechanisms.  

For more details on **Salesforce Bulk API v2.0**, refer to the official documentation:  
[Salesforce Bulk API v2.0](https://developer.salesforce.com/docs/atlas.en-us.api_asynch.meta/api_asynch/bulk_api_2_0.htm)  

For installation and setup instructions, see the **Installation & Setup** section.  

# Limitations  

When using the **Salesforce Bulk API Library**, keep in mind the following limitations:  

### General Limitations  
- The **Salesforce Bulk API** has inherent platform restrictions, such as batch size limits and processing constraints.  
- Refer to the official **Salesforce Bulk API Limits** documentation for details:  
  [Salesforce Bulk API Limits](https://developer.salesforce.com/docs/atlas.en-us.254.0.salesforce_app_limits_cheatsheet.meta/salesforce_app_limits_cheatsheet/salesforce_app_limits_platform_bulkapi.htm)  

### Query Limitations  
- Bulk API queries must comply with **SOQL (Salesforce Object Query Language) considerations**.  
- Some SOQL features, such as aggregate functions, order or limit clauses, may not be supported.  
- Detailed **SOQL query limitations** can be found here:  
  [Salesforce Bulk API Query Considerations](https://developer.salesforce.com/docs/atlas.en-us.api_asynch.meta/api_asynch/queries.htm)  

### Operation-Specific Limitations  
- **Update Operation**  
  - The metadata **must not** contain **system fields**, as these **cannot be updated**.  
- **Delete Operation**  
  - The metadata **must contain only** the **Salesforce ID** field, with the column name **`ID`**.  

### Components  
The library includes the following components:  
- **SalesforceBulkAPIReader** – Executes queries and retrieves data from Salesforce.  
- **SalesforceBulkAPIStreamWriter** – Performs **insert, update, upsert, delete, and hard delete** operations directly from an input edge.  
- **SalesforceBulkAPIFileWriter** – Processes bulk data from files and submits them to Salesforce for batch operations.  

# Properties
Name: SalesforceBulkAPILib  
Label: Salesforce Bulk API  
Author: CloverDX  
Version: 1.1  
Compatible: CloverDX 6.2 and newer  

# Tags
salesforce api crm reader writer oauth2 

# <span class='tabLabel'>Documentation</span>

## SalesforceBulkAPIReader

The **SalesforceBulkAPIReader** is a component designed to process input queries and generate output based on standard reader metadata. Queries can be provided either through **an input port** (via metadata) or **as an input parameter** (only one query can be specified at a time). The output is parsed according to the output metadata defined by the user.

### Required parameters:  
- **Salesforce Instance URL** – Specifies the URL of the Salesforce instance for data retrieval. The URL must start with https://. 
- **Salesforce OAuth2 Connection** – Provides authentication credentials for secure access to Salesforce data. This must be linked to an OAuth2 connection configured in CloverDX Server.  

### Optional Parameters  
- **Metadata mapping** – Defines how the output is mapped:  
  - **Map by Column Names** – Matches Salesforce column names to output metadata column names.  
  - **Map by Position** – Maps result columns by their position, with the first result column mapped to the first output column, and so on.  
- **Operation** – Specifies whether the reader should perform a `query` or `queryAll` operation (the latter also returns softly deleted records).  
- **Salesforce query** – Allows users to specify a query if not provided via the input edge.  
- **Salesforce bulk API version** – Specifies the version of the Salesforce Bulk API to be used.  
- **Max Records per API Call** – Defines the maximum number of records returned per API call to optimize performance.  

### Ports
| Port     | Required | Purpose      | Description                            |
|----------|----------|--------------|----------------------------------------|
| Input  1 | No       | Query Input  | Provides input queries.                |
| Output 1 | Yes      | Data Output  | Outputs data based on defined metadata.|
| Output 2 | No       | Error Output | Outputs error information.             |


## SalesforceBulkAPIStreamWriter

The **SalesforceBulkAPIStreamWriter** processes data from an input edge and performs operations such as **insert, update, upsert, delete, and hard delete**. The input metadata must match the field names in Salesforce. The output reflects the results of the requested operation.  

### Required Parameters  
- **Salesforce Instance URL** – Specifies the URL of the Salesforce instance the writer connects to for data operations. The URL must start with https://. 
- **Salesforce OAuth2 Connection** – Provides authentication credentials for secure access to Salesforce data. This must be linked to an OAuth2 connection configured in CloverDX Server.  
- **Salesforce object** – Specifies the Salesforce object on which the writer will perform data operations.  
- **Operation** – Defines the operation to be performed: `insert`, `update`, `upsert`, `delete`, or `hardDelete`.  
- **External field name** – *(Required for `upsert` operations)* Specifies the external field used to match external data with existing Salesforce records.    
- **Batch Size** – Defines the number of records processed per batch. Adjusting this value optimizes performance and resource utilization.  

### Optional Parameters  
- **Salesforce bulk API version** – Specifies the version of the Salesforce Bulk API to be used.  


### Ports  
| Port         | Required | Purpose             | Description                                                              |
|--------------|----------|---------------------|--------------------------------------------------------------------------|
| **Input 1**  | Yes      | Data Input          | Provides input data for the requested operation.                         |
| **Output 1** | No       | Successful Data     | Retrieves successfully processed records for a completed job.            |
| **Output 2** | No       | Failed Data         | Retrieves records that failed processing.                                |
| **Output 3** | No       | Unprocessed Data    | Retrieves records that were not processed due to failed or aborted jobs. |
| **Output 4** | No       | Error Output        | Outputs error information.                                               |


## SalesforceBulkAPIFileWriter

The **SalesforceBulkAPIFileWriter** processes data from files, either by specifying the **file URL on the input edge** or by providing it as an **input parameter**. The file must have a **header with field names matching Salesforce field names** to ensure proper data mapping.  

Once the data is processed, the component generates an **output file** containing the results of the requested operation. The output is stored in a **defined folder**, allowing users to access and review the results.  

### Required Parameters  
- **Salesforce Instance URL** – Specifies the URL of the Salesforce instance the writer connects to for data operations. The URL must start with https://.
- **Salesforce OAuth2 Connection** – Provides authentication credentials for secure access to Salesforce data. This must be linked to an OAuth2 connection configured in CloverDX Server.  
- **Salesforce object** – Specifies the Salesforce object on which the writer will perform data operations.  
- **Operation** – Defines the operation to be performed: `insert`, `update`, `upsert`, `delete`, or `hardDelete`.  
- **External field name** – *(Required for `upsert` operations)* Specifies the external field used to match external data with existing Salesforce records.  
- **File line ending** – Defines the line-ending format for the file: `LF` (Unix-style) or `CRLF` (Windows-style).  
- **Output folder** – Specifies the folder where the output files will be stored.  
- **File URL** – Specifies the URL of the file to be processed.  

### Optional Parameters  
- **Salesforce Bulk API Version** – Specifies the version of the Salesforce Bulk API to be used.  

### Optional Parameters  
- **Salesforce bulk API version** – Specifies the version of the Salesforce Bulk API to be used.  

### Ports  

| Port         | Required | Purpose       | Description                                                                      |
|--------------|----------|---------------|----------------------------------------------------------------------------------|
| **Input 1**  | No       | Data Input    | Specifies the URL of the files to be processed. Each record represents one file. |
| **Output 1** | No       | Error Output  | Outputs error information.                                                       |


# <span class='tabLabel'>Installation & Setup</span>

## Online installation (Server connected to Internet)

1. In the **Server Console**, navigate to **Libraries > Install library from repository**.  
2. In the **Library Repository** dropdown, select **CloverDX Marketplace**.  
3. Check the box next to the libraries you want to install. If there are any dependencies, you can install all of them at once (see **Requirements** below).  
4. Click **Install**.  

## Offline Installation (Server Without Internet Connection)  

1. Download all required libraries from **CloverDX Marketplace** (including dependencies). Each library is provided as a **.clib** file.  
2. Transfer the **.clib** file(s) to your offline **CloverDX Server** (via USB, file transfer, etc.).  
3. In the **Server Console**, navigate to **Libraries > Install library from repository** and click the **down arrow for more options**.  
4. Select **Browse local files...**, locate the downloaded **.clib** files, and install them.  


## Requirements  
- A **Salesforce Connected Application** with API scopes:  
  - `api` (Manage user data via APIs)  
  - `refresh_token` (Perform requests at any time)  

## Salesforce OAuth2 Setup  

### 1. Create a New Salesforce Connected Application  

A **Connected Application** is required to authenticate and access Salesforce resources using **Bulk API v2.0**.  

1. Log in to the **Salesforce Developer Portal**.  
2. Navigate to **Platform Tools > Apps > App Manager**.  
3. Click **New Connected App** (top-right corner).  
4. In the **Basic Information** section, provide:  
   - **Connected App Name**  
   - **API Name**  
   - **Email**  

### 2. Configure OAuth Settings  

1. Enable **OAuth Settings** and configure the following:  
   - **Callback URL**: Use the **CloverDX Server redirect URL**:  
     `https://localhost:8080/clover/oauth2`  
   - **Selected OAuth Scopes**:  
     - `Manage user data via APIs (api)`  
     - `Perform requests at any time (refresh_token, offline_access)`  
   - **Enable the following settings**:  
     - ☑ Require PKCE  
     - ☑ Require Secret For Web Server Flow  
     - ☑ Require Secret For Refresh Token Flow  
     - ☑ Enable Token Exchange Flow  
     - ☑ Require Secret For Token Exchange Flow  
     - ☑ Enable Refresh Token Rotation  

2. Click **Save and Continue**.  
3. Navigate to **Manage Connected Apps > Manage Consumer Details**.  
4. Copy the **Consumer Key** and **Consumer Secret** – these will be required to set up the **OAuth2 connection in CloverDX**.  

## CloverDX Server Setup

## 1. Create an OAuth2 Connection in CloverDX Sandbox  

Refer to the [CloverDX documentation](https://doc.cloverdx.com/latest/designer/oauth2-connections.html#id_oauth2_connection_creating) for detailed steps on **Creating an OAuth2 Connection**.  

### Configure OAuth2 Connection  

In the **Edit OAuth2 Connection** window, fill in the following properties:  

#### **Basic Settings**  
- **Client ID** – *(Consumer Key from Salesforce)*  
- **Client Secret** – *(Consumer Secret from Salesforce)*  
- **Scopes** – `api refresh_token`  

#### **Advanced Settings**  
- **Authorization Endpoint** – `https://yourSalesforceInstance.com/services/oauth2/authorize`  
- **Token Endpoint** – `https://yourSalesforceInstance.com/services/oauth2/token`  
- **Redirect URL** – `https://hostname:port/clover/oauth2`  
  - Example for a local server: `http://localhost:8083/clover/oauth2`  
  - ⚠ **Note:** The redirect URL must use **HTTPS**, except when using `localhost`.  
- ☑ **Use PKCE** – Check this box.  

### Authorize the Connection  

1. Click the **Authorize** button.  
2. Enter your Salesforce user credentials and grant access to the client application.  
3. Upon successful authorization, an OAuth2 token file is created:  
   - *ConnectionName.tokens*  
4. The connection can be **re-authorized** if needed.  

## 2. Configure the OAuth2 Connection in the Library  

- Provide the **absolute path** to the OAuth2 configuration file stored in the CloverDX sandbox:  
  - Example: `sandbox://Sandbox_name/conn/OAuth2_connection.cfg`  
- Click **Save changes** to confirm your settings.  