# <span class='tabLabel'>About</span>

This library allows you to integrate CloverDX with Salesforce, the leading cloud-based CRM platform. With this library, you can easily access and manipulate data from SalesForce objects such as Accounts, Campaigns, Leads, Opportunities, ObjectRecords and Metadata. The library uses the Salesforce bulk API, which is natively supported by CloverDX, to enable fast and efficient data transfers between CloverDX and Salesforce. 

# Properties
Name: SalesforceLib  
Label: Salesforce  
Author: CloverDX  
Version: 1.0   
Compatible: CloverDX 6.0 and newer  

# Tags
salesforce crm reader wrangler data source connector


# <span class='tabLabel'>Documentation</span>

## Accounts
Data Source Connector for getting all records from the Salesforce Account object.

### Ports

| Port      | Required  | Used For      | Description   |
|-----------|-----------|---------------|---------------|
| Output 0  | Yes       | Data Output   | Output based on generated metadata from the fields of the Account object.  |

### Private Parameters

For usage as a subgraph, it is possible to set connection details via input mapping of private parameters.

| Parameter     | Required  | Default value                                                                         | Description   |
|---------------|-----------|---------------------------------------------------------------------------------------|---------------|
| SF_USERNAME   | No        | Username to access Salesforce that has been set during library initialization.        | Username to access Salesforce account.        |
| SF_PASSWORD   | No        | Password to access Salesforce that has been set during library initialization.        | Password to access Salesforce account.        |
| SF_TOKEN      | No        | Security token to access Salesforce that has been set during library initialization.  | Security token to access Salesforce account.  |


## Campaigns
Data Source Connector for getting all records from the Salesforce Campaign object.

### Ports

| Port      | Required  | Used For      | Description   |
|-----------|-----------|---------------|---------------|
| Output 0  | Yes       | Data Output   | Output based on generated metadata from the fields of the Campaign object.  |

### Private Parameters

For usage as a subgraph, it is possible to set connection details via input mapping of private parameters.

| Parameter     | Required  | Default value                                                                         | Description   |
|---------------|-----------|---------------------------------------------------------------------------------------|---------------|
| SF_USERNAME   | No        | Username to access Salesforce that has been set during library initialization.        | Username to access Salesforce account.        |
| SF_PASSWORD   | No        | Password to access Salesforce that has been set during library initialization.        | Password to access Salesforce account.        |
| SF_TOKEN      | No        | Security token to access Salesforce that has been set during library initialization.  | Security token to access Salesforce account.  |

## Leads
Data Source Connector for getting all records from the Salesforce Lead object.

### Ports

| Port      | Required  | Used For      | Description   |
|-----------|-----------|---------------|---------------|
| Output 0  | Yes       | Data Output   | Output based on generated metadata from the fields of the Lead object.  |

### Private Parameters

For usage as a subgraph, it is possible to set connection details via input mapping of private parameters.

| Parameter     | Required  | Default value                                                                         | Description   |
|---------------|-----------|---------------------------------------------------------------------------------------|---------------|
| SF_USERNAME   | No        | Username to access Salesforce that has been set during library initialization.        | Username to access Salesforce account.        |
| SF_PASSWORD   | No        | Password to access Salesforce that has been set during library initialization.        | Password to access Salesforce account.        |
| SF_TOKEN      | No        | Security token to access Salesforce that has been set during library initialization.  | Security token to access Salesforce account.  |

## Opportunities
Data Source Connector for getting all records from the Salesforce Opportunity object.

### Ports

| Port      | Required  | Used For      | Description   |
|-----------|-----------|---------------|---------------|
| Output 0  | Yes       | Data Output   | Output based on generated metadata from the fields of the Opportunity object  |

### Private Parameters

For usage as a subgraph, it is possible to set connection details via input mapping of private parameters.

| Parameter     | Required  | Default value                                                                         | Description   |
|---------------|-----------|---------------------------------------------------------------------------------------|---------------|
| SF_USERNAME   | No        | Username to access Salesforce that has been set during library initialization.        | Username to access Salesforce account.        |
| SF_PASSWORD   | No        | Password to access Salesforce that has been set during library initialization.        | Password to access Salesforce account.        |
| SF_TOKEN      | No        | Security token to access Salesforce that has been set during library initialization.  | Security token to access Salesforce account.  |


## getSalesforceObjectRecords
ComponentGet all records from the specified Salesforce object.

### Ports
| Port      | Required  | Used For      | Description   |
|-----------|-----------|---------------|---------------|
| Output 0  | Yes       | Data Output   | Output based on metadata connected to output port from the fields of the required object. Fields are mapped by name to target metadata.   |

### Public Parameters

| Parameter         | Required  | Default value | Description   |
|-------------------|-----------|---------------|---------------|
| OBJECT            | Yes       |               | Salesforce object for which data records should be retrieved.     |
| REQUESTED_FIELDS  | Yes       |               | Specify the fields of the Salesforce object that are required.    |
| SF_USERNAME       | Yes       |               | Username to access Salesforce account.        |
| SF_PASSWORD       | Yes       |               | Password to access Salesforce account.        |
| SF_TOKEN          | Yes       |               | Security token to access Salesforce account.  |

## getSalesforceObjectMetadata
Get metadata for the specified Salesforce object and save it to provided location. It can be saved either directly in library or in any other sandbox.

### Ports
| Port      | Required  | Used For      | Description   |
|-----------|-----------|---------------|---------------|
| Output 0  | Yes       | Data Output   | Comma separated list of fields for required Salesforce object.  |

### Public Parameters

| Parameter             | Required  | Default value                                                                         | Description   |
|-----------------------|-----------|---------------------------------------------------------------------------------------|---------------|
| TARGET_METADATA_DIR   | No        | Library folder meta/generated/                                                        | The target directory for generated metadata file. |
| OBJECT                | No        | Account                                                                               | Salesforce object for which metadata should be generated.     |
| TARGET_METADATA_NAME  | No        | Value of the OBJECT parameter                                                         | Name for target metadata. Do not include the file extension, it is added automatically.   |
| SF_USERNAME           | No        | Username to access Salesforce that has been set during library initialization.        | Username to access Salesforce account.        |
| SF_PASSWORD           | No        | Password to access Salesforce that has been set during library initialization.        | Password to access Salesforce account.        |
| SF_TOKEN              | No        | Security token to access Salesforce that has been set during library initialization.  | Security token to access Salesforce account.  |

# <span class='tabLabel'>Installation & Setup</span>

Following additional steps are required for library configuration:

In your CloverDX Server UI, navigate to the Libraries > *Configuration* tab

1. In Library parameters section:
    1. Set SALESFORCE_USER to a valid username for the Salesforce connection.
    2. Set SALESFORCE_PASSWORD to a valid password for the Salesforce connection.
    3. Set SALESFORCE_SECURITY_TOKEN to a valid security token for the Salesforce connection. <br />*You can get the security token in Salesforce settings under "Reset Security Token" menu.*
2. Confirm your settings by pressing the **Save changes** button.
3. Run **Initialize library**. <br />The initialization graph generates metadata for all Data Source Connectors (Accounts, Campaigns, Leads, Opportunities). After library initialization, the Data Source Connectors will auto-propagate metadata. 

