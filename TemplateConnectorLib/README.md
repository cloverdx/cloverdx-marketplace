# <span class='tabLabel'>About</span>

This library helps you create your own Data Source Connectors and Targets for CloverDX Wrangler. Data Source Connectors are modules that allow CloverDX Wrangler to access and manipulate data from various sources, such as databases, APIs, files, etc. Targets allow a possibility to save data to the specified target (database, online service, etc.). Data Catalog Connectors Template provides a basic structure and examples for building a Data Source Connector and Target, as well as documentation. You can download this library from CloverDX Marketplace, install it to CloverDX Wrangler, and customize it according to your needs. Data Catalog Connectors Template is a useful tool for developers who want to extend the functionality of CloverDX Wrangler and integrate it with different data sources and targets.

# Description

If you have downloaded this template from the Marketplace, you may notice that it has a .clib extension. This is actually a ZIP file that contains a CloverDX project. You can open the template by renaming the .clib file to .zip and extracting the archive. Inside, you will find a CloverDX project folder that you can import into your workspace and customize as you wish. When you are done with your modifications, you can export the project as a library again by using the File -> Export as Library... menu option. This will create a new .clib file that you can share or reuse.

# Customization 

Replace with the documentation for your own implementation of library using Markdown syntax - [documentation here.](https://www.markdownguide.org/basic-syntax/)

# Properties
Name: TemplateConnectorLib  
Label: Data Catalog Connectors Template  
Author: CloverDX  
Version: 1.1  
Compatible: CloverDX 6.0 and newer  

# Tags
connector data-source data-target wrangler template data-catalog

# <span class='tabLabel'>Documentation</span>

## DataSource

It is recommended to describe all components/data source connectors included in your Library. This section should cover description of the *DataSource*. You can consider also description of ports and parameters.

### Ports
| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output port 0 | Required      | Data    | Output data |

### Parameters

| Parameter label | Parameter label | Description | Required | Default Value |
| :--- | :--- | :--- | :--- | :--- |
|Active customers only|ACTIVE_INACTIVE|If set to true, only active customers are returned.|false|*empty*|
|State|STATE|Filter only selected state.|false|*empty*|

## DataTarget

It is recommended to describe all targets included in your Library. This section should cover description of the *DataTarget*. You can consider also description of ports and parameters.

This template points out all requirements needed for the development of a Data Target. Data Target Connectors are special subgraphs designed to be used as data targets data targets in the Data Catalog in CloverDX Wrangler.

### Ports
| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input port 0 | Required      | Data    | Output data |

### Parameters

| Parameter label | Parameter label | Description | Required | Default Value |
| :--- | :--- | :--- | :--- | :--- |
|*Parameter*|*Label*|*Description*|false|*empty*|


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
