# <span class='tabLabel'>About</span>

The **Data Manager Library** is a collection of components designed to integrate seamlessly with **Data Manager** in CloverDX.  
Data Manager enables domain experts to directly interact with data processed by or used within CloverDX,  
allowing them to **view, edit, and approve changes** as data flows through the system.  

Data Manager supports two primary use cases:  
- **Data Quality Management** – Ensuring data consistency, integrity, and compliance.  
- **Reference Data Management** – Managing and governing reference data sets efficiently.  

This library  allows you to interact with Data Manager. The library provides a set of additional components as well as various Data Apps that can help you manage data sets configured in your Data Manager.  

For more details about **Data Manager**, see the official documentation:  
[Data Manager Introduction](https://doc.cloverdx.com/latest/user/data-manager-introduction.html)  

## Key Features  
- Create new data sets based on metadata  
- Update data set rows  
- Delete data sets  
- Retrieve data set audit logs  
- Get an overview of a data set  
- Approve reference data set rows  

The library interacts with **CloverDX Server’s internal API** for seamless data management.  

# Properties
Name: DataManagerLib  
Label: Data Manager  
Author: CloverDX  
Version: 3.2.0  
Compatible: CloverDX 7.0  

# Tags  
data-management, data-quality, reference-data, API, data-approval

# <span class='tabLabel'>Documentation</span>

## CreateDataSetFromMetadata  

Creates a new data set based on a provided metadata file.  

### Required Parameters  
- **Metadata URL** – Path to the metadata file used for creating the data set.  
- **Data set type** – Type of data set to create. Options:  
  - **Transactional data set** (default)  
  - **Reference data set**  

### Optional Parameters (Advanced Settings) 
- **Data retention (days, for transactional data sets)** – Specifies how long the data should be retained.  
- **Data set name** – Custom name for the created data set.  
  - **Use the filename** (default) of the linked metadata file.  
  - **Use the provided custom value**  
  - **Use the record name** of the linked metadata file. (Requires DataAnalyticsLib)  
  - **Use the record label** of the linked metadata file. (Requires DataAnalyticsLib)  
- **System columns to add** – Specifies system columns to include.  
- **Effective dates (for reference data sets)** – Defines effective dates for reference data management.  
- **Permission settings** add the list of CloverDX users to the respective role. Accepts comma/semicolon separated list.
  - **Admin** - users who can configure the data set, approve and edit data in the data set.  
  - **Approver** - Users who can approve and edit data in the data set.  
  - **Editor** - Users who can edit data in the data set.  
  - **Reader (for reference data sets)**  - User who can only see published data and cannot make any changes in the data set.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose                      | Description                                      |  
|------------|----------|------------------------------|--------------------------------------------------|  
| Output 1   | No       | DataSet metadata             | Outputs metadata about the created data set.     |  
| Output 2   | No       | DataSetColumn metadata       | Outputs metadata describing all created columns (one record per column). |  
| Output 3   | No       | Error metadata               | Outputs error details if the process fails.      |  

*This component does not require an input edge.*  

## GetDataSetsOverview  

Retrieves basic information about all data sets in the **Data Manager**.  

This component has no specific settings apart from the **common settings** required for connecting to the **CloverDX Server**.  
It returns all data sets available on the server for the authenticated user.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose             | Description                                         |  
|------------|----------|---------------------|-----------------------------------------------------|  
| Output 1   | Yes      | Data sets overview  | Outputs a list of all available data sets with basic details. |  
| Output 2   | No       | Error metadata      | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  

*This component does not require an input edge.*  


## DeleteAllDataSets  

Deletes all data sets in the **Data Manager**.  

This component has no specific settings apart from the **common settings** required for connecting to the **CloverDX Server**.  
It deletes all data sets available on the server for the authenticated user.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose            | Description                                         |  
|------------|----------|--------------------|-----------------------------------------------------|  
| Output 1   | Yes      | Deleted data sets  | Outputs the **dataSetCode** of each deleted data set. |  
| Output 2   | No       | Error metadata     | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  

*This component does not require an input edge.*  

## DeleteDataSet  

Deletes a data set based on its **dataSetCode**.  

This component requires an **input edge** with a field containing the **dataSetCode** of the data set to be deleted.  
The **Input mapping** attribute allows users to specify which field contains the **dataSetCode**.  

### Required Parameters  
- **Input mapping** – Specifies the field containing the **dataSetCode** to be deleted.  

### Optional Parameters  
- **Ignore missing data sets** – If set to `true`, the component will **skip** deletion attempts for non-existent data sets instead of failing.  
  - **Default:** `false`  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose             | Description                                         |  
|------------|----------|---------------------|-----------------------------------------------------|  
| Input 1    | Yes      | Data set input      | Accepts records containing the **dataSetCode** to be deleted. |  
| Output 1   | Yes      | Deleted data set    | Outputs the **dataSetCode** of each successfully deleted data set. |  
| Output 2   | No       | Error metadata      | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  


## DataSetRowUpdate  

Updates values of matching columns in a **Data Manager** data set row based on **star mapping** from the input.  

### Required Parameters  
- **Data set code** – The code of the target data set.  
- **Field with row ID** – The name of the field in the input metadata that contains the row ID used for identifying the row to update.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose            | Description                                         |  
|------------|----------|--------------------|-----------------------------------------------------|  
| Input 1    | Yes      | Data set row input | Accepts metadata with fields to be updated. One field is used as the row ID, and the rest are referenced by name and updated in the data set. |  
| Output 1   | Yes      | Update status      | Outputs update details, including **dataSetCode**, **rowId**, and **columnName** of the updated fields. |  
| Output 2   | No       | Error metadata     | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  


## DataSetRowColumnUpdate  

Updates the value of a specific column in a **Data Manager** data set row. Instead of using star mapping like **DataSetRowUpdate**, this component expects all details from the input metadata in a specific format (**dataSetCode**, **rowId**, **columnName**, **newValue**). The input metadata is auto-propagated to help users provide the correct input.  

If **dataSetCode** is provided as an attribute, it overrides the **dataSetCode** in the input metadata.

### Parameters  
- **Data set code** – The code of the target data set. If provided, it will override the **dataSetCode** in the input metadata.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose            | Description                                         |  
|------------|----------|--------------------|-----------------------------------------------------|  
| Input 1    | Yes      | Row identification | Accepts input metadata in the format: **dataSetCode**, **rowId**, **columnName**, **newValue**. |  
| Output 1   | Yes      | Update status      | Outputs update details, including **dataSetCode**, **rowId**, and **columnName** of the updated field. |  
| Output 2   | No       | Error metadata     | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  


## GetDataSetAudit  

Returns audit records for a specified data set. This component retrieves the audit logs for a given data set and optionally filters the results based on date/time and column name.  

### Required Parameters  
- **Data set code** – The code of the target data set for which audit records are to be retrieved.  

### Optional Parameters  
- **Min date/time of the audit event** – The minimum date/time for filtering the audit events. Use the format `yyyy-MM-dd HH:mm:ss`. Leave empty to disable the filter.  
- **Max date/time of the audit event** – The maximum date/time for filtering the audit events. Use the format `yyyy-MM-dd HH:mm:ss`. Leave empty to disable the filter.  
- **Column name** – Filter by column name for the audit events. Leave empty to disable the column filter.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose            | Description                                         |  
|------------|----------|--------------------|-----------------------------------------------------|  
| Input 1    | Yes      | Row reference      | Accepts metadata containing the **rowId** to reference the row for getting the audit records. |  
| Output 1   | Yes      | Audit entries      | Outputs the audit information for the records, including the following fields:  
  - **id** – Unique identifier of the audit record  
  - **event_timestamp** – Timestamp of the audit event  
  - **username** – Username of the user who made the change  
  - **column_name** – Name of the column affected by the change  
  - **old_value** – Previous value of the column  
  - **new_value** – New value of the column |  


## ReferenceDataSetApproveAll  

Approves all rows in the selected reference data set. This component allows for bulk approval of all entries in a given reference data set.  

### Required Parameters  
- **Reference data set code** – The code of the reference data set for which all rows are to be approved.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose           | Description                                          |  
|------------|----------|-------------------|------------------------------------------------------|  
| Output 1   | Yes      | Approved data set | Outputs the **dataSetCode** of the reference data set that was approved. |  
| Output 2   | No       | Error port        | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  

## ReferenceDataSetApproveRow  

Approves specific rows in the reference data set. This component allows the approval of individual rows in a given reference data set based on the **rowId** provided in the input metadata.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose            | Description                                          |  
|------------|----------|--------------------|------------------------------------------------------|  
| Input 1    | Yes      | Row reference      | Accepts metadata containing the **rowId** to reference the row for approval. |  
| Output 1   | Yes      | Approved data set  | Outputs the **dataSetCode** of the reference data set that was approved. |  
| Output 2   | No       | Error port         | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  

## TransactionalDataSetSetRowStatus  

Sets the status of a row in a transactional data set. This component allows you to change the status of a specific row in the transactional data set to one of the specified statuses (NEW, APPROVED, EDITED, DELETE, UNDELETE).  

### Required Parameters  
- **Data set code** – The code of the transactional data set to be updated. This is used when the data set code from the input edge is not specified.  
- **New status** – The status to set for the row. Acceptable values are:  
  - "APPROVED"
  - "EDITED"
  This is used when the new status is not specified on the input edge.  

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose              | Description                                          |  
|------------|----------|----------------------|------------------------------------------------------|  
| Input 1    | Yes      | Row status change     | Accepts metadata containing the **dataSetCode**, **rowId**, and the **newStatus** for the row. The status can be one of the following values: "NEW", "APPROVED", "EDITED", "DELETE", or "UNDELETE". |  
| Output 1   | Yes      | Status change result  | Outputs information about the result of setting the status for the row. |  
| Output 2   | No       | Error port           | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  

## ResetCaches 

Clear content of sandbox-based lookup cache used for Data Manager. If you use lookups backed by Reference Data Sets you do not need to use this API. If server is part of cluster, the cache is automatically cleared on all nodes of the cluster.

### Common Settings  
- **CloverDX Server URL** – URL of the CloverDX Server.  
- **CloverDX Server user** – Username for authentication.  
- **CloverDX Server pass** – Password for authentication.  

### Ports  

| Port       | Required | Purpose              | Description                                          |  
|------------|----------|----------------------|------------------------------------------------------|  
| Input 1    | No       | None	               | Only offered as a convenience. |
| Output 1   | No       | Success port         | Only offered as a convenience. |  
| Output 2   | No       | Error port           | If an error occurs and this port is connected, the error is propagated here.  
If the port is not connected and an error occurs, the component fails. |  

# <span class='tabLabel'>Installation & Setup</span>

## Dependencies

These additional libraries need to be installed on your Server, in case advanced functionality is needed.
- [DataAnalyticsBundleLib v. 1.1.2](https://marketplace.cloverdx.com/DataAnalyticsBundleLib_1.1.2.html) or newer - see CreateDataSetFromMetadata

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