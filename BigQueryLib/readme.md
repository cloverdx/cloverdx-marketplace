# <span class='tabLabel'>About</span>

This library contains components for interaction with Google Cloud Big Query.  

# Description
The library allows writing data to BigQuery tables using the <a target="_new" href="https://cloud.google.com/bigquery/docs/write-api">Storage Write API</a>. The library supports streaming ingestion and batch loading of data with different types of streams. The library also handles schema updates, error handling, and retry logic.

The library also contains CloverDX Wrangler target, which could be used for streaming data to Big Query tables. The library accepts data in CloverDX format (on edges), converts internally into JSON format and converts it to protocol buffers before sending it over to the target.

# Features
- Writing data to BigQuery tables using the Storage Write API.
- Streaming ingestion and batch loading of data with different types of streams.
- Handling schema updates, error handling, and retry logic.
- Accepting data in JSON format and converting it to protocol buffers before sending it over to the target.


# Properties
Name: BigQueryLib  
Label: Big Query  
Author: CloverDX  
Version: 1.2  
Compatible: CloverDX 6.2 and newer  

# Tags
connector data-target wrangler big query google cloud writer

# <span class='tabLabel'>Documentation</span>

## BigQueryWriter.sgrf

The **BigQueryWriter** is a writer component in CloverDX that allows writing data to BigQuery tables using Storage Write API. The configuration settings include the Project ID, Data set name, Table name and Path to JSON credentials. The supported Clover data types are:

- `STRING`
- `DATE`
- `NUMBER`
- `INTEGER`
- `LONG`
- `DECIMAL`
- `BYTE`
- `CBYTE`
- `BOOLEAN`

CloverDX fields are mapped automatically by name to BigQuery columns.

The component also has an optional error port for rejected records which can be auto-propagated metadata or alternatively it is possible to set your own metadata. There is also an Ignore unknown fields attribute which if set to true will ignore unknown JSON fields instead of causing an error in BigQuery.

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 1  | Yes      | Data input     | Data to be loaded to the Big Query table. |
| Output 1 | No      | Error port    | Rejected records, auto-propagated metadata. It is possible to assign custom metadata. In that case, there are supported two  auto-filling fields (ErrText and global_row_count) for getting the error details.|

### Parameters

| Parameter label | Description | Required? | Default value
| --- | --- | --- | --- | 
| Project ID | The ID of the Google Cloud project that owns the dataset. | yes | *none* |
| Dataset name | Name of the dataset where the table is | yes | *none* |
| Path to JSON credentials | path to the JSON file that contains your service account key. | yes | *none* |
| Table name | The name of the table to load data into.| yes | *none* |
| Batch size | The number of records that can be sent to a database in one batch update. | yes | 100|
| Ignore unknown fields | If true, unknown data fields to BigQuery will be ignored instead of error out. Please note, it can lead to unintentional data loss, because unknown fields are silently dropped. | yes | true 
| Enable reject information | If true, the component will return information about all the records that were not uploaded into the table. Records are written to the Error port (port 0). Metadata auto-propagation works for the error port. | yes | true |
| Processing type | Type of data writing, available types are: Default stream, committed, pending. For more details, please see <a href="https://cloud.google.com/bigquery/docs/write-api" target="_new">documentation</a>. | yes | Default stream |
| Write mode | Available modes: append or truncate. By default, data will be added to the target table. Using this parameter, you can truncate the target table before writing. | yes | append |
| Max retry count | The Max retry count property determines the number of times the component attempts to append a batch when the previous attempt fails. The component only retries if the failure is caused by an INTERNAL, CANCELLED, or ABORTED error code. If the component reaches the maximum number of retries without success, it will report a failure. | yes | 3 |
| Max recreate count | The Max recreate count property specifies the number of times the component tries to reconnect to the server when the connection is closed. The component only reconnects if the connection is closed by the server or by a network error. If the component reaches the maximum number of recreations without success, it will report a failure. | yes | 3 |

### CloverDX Wrangler Target
Using the BigQueryTarget (**BigQueryWriter** wrapper for CloverDX Wrangler), you can write data into your BigQuery tables. Configuration of the Data Target requires configuration of the library (to configure Project ID, Dataset Name and Path to JSON credentials) and the name of the table you want to write into. 

### Field mapping

By default, only fields with names that match the corresponding field names in the target table are mapped; all other fields are ignored. To configure the job to fail if a source record does not exist in the target table, deselect the **Ignore unknown fields** checkbox.

## BigQueryReader.sgrf

The BigQueryReader is a CloverDX component that reads data from a BigQuery table. It propagates metadata automatically, considers the Selected fields attribute (only selected fields are propagated to the output metadata), and can use custom metadata. The BigQueryReader uses the BigQuery Storage API.

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output 1 | Yes      | Output data    | Data from BigQuery table, automatically propagated. It is possible to assign custom metadata.  The field names must match the names of the fields in the BigQuery table. The field types must match the data types of the fields in the BigQuery table. |

### Parameters

| Parameter label | Description | Required? | Default value |
|---|---|---|---|
| Project ID | The ID of the Google Cloud project that owns the dataset. | yes | *none* |
| Dataset name | Name of the dataset where the table is | yes | *none* |
| Path to JSON credentials | path to the JSON file that contains your service account key. | yes | *none* |
| Table name | The name of the BigQuery table to read data from. | yes | *none* |
| Selected fields | The fields to return. If this property is empty, all fields will be returned. | no | *none* |
| Row restriction | A where condition to restrict the rows that are read. | no | *none* |

### Using Custom Metadata

It is possible to use custom metadata with the BigQueryReader component. To do this, assign your custom metadata to the output port. The field names must match the names of the fields in the BigQuery table. The field types must match the data types of the fields in the BigQuery table.

## BigQueryExecuteCommand.sgrf

The **BigQueryExecuteCommand** is an utility which allows to execute Big Query commands, typically DDL statements. It is not expected to run SELECT command, anyway, it is possible to run this command but only first 20 MB data will be returned in a JSON format. For reading from Big Query tables, please use Big Query Reader component (will be available later in this library).

Configuration of this component is similar to the **BigQueryWriter** component (Project ID, Dataset name, JSON credentials).

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 1  | Yes      | Data input     | Data to be loaded to the Big Query table. |
| Output 1 | No      | Output data    | If parameter **Return results** is true, the output port contains the original query and number of affected rows. If the command returns data, first 20 MB is returned in JSON format.|

### Parameters

| Parameter label | Description | Required? | Default value
| --- | --- | --- | --- | 
| Project ID | The ID of the Google Cloud project that owns the dataset. | yes | *none* |
| Dataset name | Name of the dataset where the table is | yes | *none* |
| Table name | The name of the table to load data into.| yes | *none* |
| Command | Big Query command which will be executed. | yes | *none* |
| Return results | If is set to true, the output port contains the original query and number of affected rows. If the command returns data, first 20 MB is returned in JSON format. | yes | true |
| Fail on error | If an error occurs during processing, the component fails (when set to true), or finishes OK (when set to false). | yes | false |

## BigQueryGetTableSchema.sgrf

Using this component you can get a schema for a specific Big Query table. The output is in a JSON format.

Configuration of this component is similar to the **BigQueryWriter** component (Project ID, Dataset name, JSON credentials).

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output 1 | Yes      | Output schema  | If the specified table exists, the output field *schema* contains a schema in JSON format.|

### Parameters

| Parameter label | Description | Required? | Default value
| --- | --- | --- | --- | 
| Project ID | The ID of the Google Cloud project that owns the dataset. | yes | *none* |
| Dataset name | Name of the dataset where the table is | yes | *none* |
| Table name | The name of the table to load data into.| yes | *none* |
| Fail on non exists | If the table does not exist, the component returns zero records (when set to false), or fails (when set to true). | yes | false |

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

### Configuration

The writer component expects settings related to the Big Query instance such as project, data set and table. This information can be obtained from the Big Query project.

For authentication, a service account is used and the authentication details are stored in a JSON file.

### How to get Service account

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Select your project.
3. Click on the **Navigation menu** and then click on **IAM & Admin**.
4. Click on **Service Accounts**.
5. Click on **Create Service Account**.
6. Enter a name for your service account and click on **Create**.
7. In the **Role** dropdown, select **BigQuery Admin**.
8. Click on **Continue**.
9. Click on **Create Key** and select **JSON** as the key type.
10. Click on **Create**.

You can now use this JSON key file to authenticate with the BigQuery API using Java API  .

Note that the JSON file with credentials can be stored in a sandbox and referenced as e.g. `${DATAIN_DIR}/secret.json`. It is highly recommended to ensure that the credentials in JSON are sensitive so users can take care of who can access them.