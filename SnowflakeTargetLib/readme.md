# <span class='tabLabel'>About</span>

Extends SnowflakeWriter capabilities and provides Snowflake as CloverDX Data target.

# Description
The library builds on CloverDX [SnowflakeBlukWriter](https://doc.cloverdx.com/latest/designer/snowflakebulkwriter.html), adding additional functionality and exposing Snowflake as Data Target for CloverDX wrangler.

# Features
- **Bulk loading**: The component supports bulk loading of data into Snowflake tables, which improves the performance and efficiency of data ingestion.
- **Create and recreate tables**: The component allows you to create a new table in Snowflake or append data to an existing table. You can also force the component to drop and recreate the target table if it already exists.
- **Ignore errors**: The component can ignore record level error, continuing the load. Rejected records can be returned via the output port.
- **CloverDX Wrangler integration**: Besides being available as a component in graphs, the component is also available as CloverDX Wrangler Data target.

# Properties
Name: SnowflakeTargetLib  
Label: Snowflake Data Target  
Author: CloverDX  
Version: 1.0  
Compatible: CloverDX 6.2 and newer  

# Tags
connector data-target wrangler snowflake bulk-writer writer

# <span class='tabLabel'>Documentation</span>

## SnowflakeTarget.sgrf

This component accepts any metadata on input port 0. The input port is mandatory. The data from input port will be written to the target table. The output port is optional, it is used to for rejected records if continue on error option is used.

### Ports

| Port     | Required | Used for       | Description                               |
|----------|----------|----------------|-------------------------------------------|
| Input 1  | Yes      | Data input     | Data to be loaded to the Snowflake table. |
| Output 1 | No       | Error port     | Rejected records                         |

### Parameters

| Parameter label | Description | Required? | Default value |
| --- | --- | --- | --- |
| DB table | Name of the target table | true |  |
| Create table if not exists | Create target tableif it doesn't exist. | false | true |
| Continue on error | Continue loading even if an error occurs on record level. Rejected records can be retrieved via the output port. | false | false |
| Force re-create table | Re-create the table. Will drop the table deleting all data and create it anew. | false | false |

## DeleteSnowflakeStageFiles.grf

Deletes all stage files associated with a table.

As this file is not a subgraph it will not be visible in the component palette, but can be used by directly specifying the URL in an ExecuteGraph component (e.g. `${SNOWFLAKE_TARGET_LIB_1.0}/graph/DeleteSnowflakeStageFiles.grf` ) 

### Parameters

| Parameter label | Description | Required? | Default value |
| --- | --- | --- | --- |
| DB table | Name of the target table | true |  |


# <span class='tabLabel'>Installation & Setup</span>

### Online installation (Server connected to Internet)

1. In Server Console, navigate to Libraries > Install library from repository
2. Select Library Repository dropdown > CloverDX Marketplace
3. Check the box next to the libraries you want to install
4. Click Install

### Offline installation (Server without Internet connection)

1. Download the library from the CloverDX Marketplace. You should get a ".clib" file for the library
2. Transfer the ".clib" file to your offline Server machine (USB stick, ...)
3. In Server Console, navigate to Libraries > Install library from repository > Down arrow for more options > Browse local files...
4. Select the downloaded .clib file on your disk and install

### Dependencies

These additional libraries will need to be installed on your Server.

- [MetadataFactory](https://marketplace.cloverdx.com/MetadataFactoryLib_1.0.html)

The same installation procedures as described above can be used to install the dependencies.

### Configuration (Required before first use)

The writer component expects settings related to the Snowflake instance such as account, warehouse, region etc. This information can be obtained from the Snowflake.

In Server Console, go to Libraries > Snowflake (name of library set during installation) > Configuration tab
and provide the following values.

| Parameter label | Description |
| --- | --- |
| Account Identifier | Identifier of the Snowflake account |
| Warehouse | The name target Snowflake warehouse. |
| Region | The region where the Snowflake compute resources are located, e.g. "west-europe.azure". |
| Database name | Name of the target database. |
| Schema | Name of the target schema. |
| Username | Username of the user used to connect to Snowflake. |
| Password | Password of the user used to connect to Snowflake. |