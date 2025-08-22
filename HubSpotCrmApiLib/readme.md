# <span class='tabLabel'>About</span>

This library exposes the [HubSpot CRM API](https://developers.hubspot.com/docs/api/crm/understanding-the-crm) to developers in CloverDX.

DISCLAIMER: If you are not a developer and need a simple interface to access HubSpot CRM objects like Contacts, Companies or Deals, please use this [HubSpot CRM](https://marketplace.cloverdx.com/HubSpotLib_1.0.html) library instead.

The library supports the following operations:
- Basic operations on CRM objects
- Manage association between instances of objects
- Read owner records

# Properties

Name: HubSpotCrmApiLib  
Label: HubSpot CRM API (for developers)  
Author: CloverDX  
Version: 1.2  
Compatible: CloverDX 6.0 and newer  

# Tags

hubspot connector crm reader writer


# <span class='tabLabel'>Documentation</span>

## Components provided by this library

## CrmObjectApiV3

Implements generic basic operations (list, read, update, archive) on CRM objects (e.g. Deals, Companies ...), including custom objects.

Note: Custom Object API Change (Effective June 4, 2025)
HubSpot has removed support for referencing custom object types by their base name (e.g., /crm/v3/objects/vendor). You must now reference custom object types using one of the following supported formats:
- Short-hand name: /crm/v3/objects/p_{object_name} (e.g., p_vendor)
- Object type ID: /crm/v3/objects/{objectTypeId} (e.g., 2-123)
- Fully qualified name: /crm/v3/objects/{fullyQualifiedName} (e.g., p345_vendor)

Requests using base names will return a 400 error with message: Unable to infer object type from: {baseName}
If your CloverDX integration refers to custom object types using their base name, update your configuration or mapping logic to use the correct identifier format.

[See full changelog entry here.](https://developers.hubspot.com/changelog/breaking-change-removed-support-for-referencing-custom-object-types-by-base-name)

### Ports

| Port     | Used for    | Operation             |
|----------|-------------|-----------------------|
| Input port 0  | Input data  | read, update, archive |
| Output port 0 | Output data | read, update, list    |
| Output port 1 | Errors      | *                     |

### Usage, metadata and field mapping
The subgraph works with HubSpot object IDs to identify and manipulate said objects. The metadata field name for the ID is configurable via a parameter. By default, it will be calculated based on the object type name, converted to the singular and lower case and suffixed with "_id", e.g. product_id.

The user can select the properties of the object to read/write by providing a mapping. HubSpot object property is addressed via its "internal name". This can be set during the creation of the property or later viewed in the HubSpot web UI by clicking on a \</\> icon to the right of the property name. The expected format of the mapping between HubSpot internal names and CloverDX metadata field names is [properties format](https://doc.cloverdx.com/latest/designer/miscellaneous-functions-ctl2.html#id_ctl2_parseproperties) (essentially key:value pairs), where the keys represent HubSpot internal names and values are metadata field names (these can be omitted to map to a field of the same name).

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| HS object name | Use one of the provided values or provide your own for a custom object. Custom objects have to be refereced either by their short_hand name, object type ID or fully qualified name. See the note above for more details.| true | basic | enumeration | Options:<br>companies<br>contacts<br>deals<br>line_items<br>products<br>tickets<br>quotes<br><br>Custom values allowed. |
| HS properties to metadata mapping | Provide mapping between HubSpot property names and CloverDX metadata field names, in the .properties file format. Left side represents HubSpot names, right side represents CloverDX names. CloverDX names are optional, if blank the HubSpot name will be used. | false | basic | properties |   |
| Operation | Select one of the operations: List, Read, Batch read, Create, Update, Archive (move to trash). | true | basic | enumeration | Options:<br>Read<br>Create<br>Update<br>Archive<br>List<br>Batch read<br> |
| Object ID field name | CloverDX field name mapped to the HubSpot ID. Optional, if no value provided, field name will be generated as lowercase singular object name + _id suffix. | false | advanced | metadata field |   |
| Private app token | HubSpot Private App token. Takes priority over OAuth2 if both provided. | false | basic |   |   |
| OAuth2 connection | File URL of the .cfg file holding an authorised CloverDX HubSpot OAuth2 connection. | false | basic | file picker | Only files.<br>Files with extension(s):<br>*.cfg<br> |
| Look in archived records? | Switches between working on regular or archived records (in trash). | false | advanced | bool |   |
| Associations | Get IDs of associated objects. Provide a comma separated list of HubSpot object names to get associations for. | false | advanced |   |   |
| Batch size | Size of a page. Default 10. Max (appears to be) 100. Larger value should speed up the list operation. | false | advanced | integer |   |
| Http call retry count | Defines number of retry attempts when HTTP call fails. Set to zero to disable retry after HTTP call failure. | false | advanced | integer |   |
| Http call retry delay | Defines delay between retry attempts in milliseconds. | false | advanced | integer |   |
| Associations output field name | Output metadata field for saving variant object of returned associations | false | advanced |   |   |
| Http call timeout | How long the component waits to get a response. If it does not receive a response within a specified limit, the execution of the component fails. <br>Timeout is in milliseconds. Different time units can be used. See Time Intervals in clover documentation. | false | advanced |   |   |

## CrmObjectAssociationsApiV3

Allows managing associations between objects (e.g. a deal to its associated company, etc).
This component allows you to list associations, create and delete them.

See [HubSpot Associations API](https://developers.hubspot.com/docs/api/crm/associations) for further reference on how associations work in HubSpot.

### Ports

| Port     | Used for    | Operation             |
|----------|-------------|-----------------------|
| Input 0  | Input data  | create, remove        |
| Output 0 | Output data | create, list          |
| Output 1 | Errors      | *                     |

### Usage and metadata

The graph expects IDs of the objects on both ends (associations are directional) in the fields that are configurable via graph parameters (By default it will be calculated based on the object type name, converted to singular and lowercase and suffixed with "_id", e.g. product_id). "To" field must be present on output metadata, "from" field must be present on both input and output.

Optional field "association_type" can be provided to be filled, especially useful when using the list operation.

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| From object type | Select association's source object type.  | true | basic | enumeration | Options:<br>companies<br>contacts<br>deals<br>line_items<br>products<br>tickets<br>quotes<br><br>Custom values allowed. |
| To object type | Select association's target object type. | false | basic | enumeration | Options:<br>companies<br>contacts<br>deals<br>line_items<br>products<br>tickets<br>quotes<br><br>Custom values allowed. |
| Association type | Specify an association type in case this is not a list operation. Remember associations are directional. E.g. company_to_deal | false | basic |   |   |
| From object ID field name | CloverDX field name mapped to the HubSpot ID. Optional, if no value provided, field name will be generated as lowercase object name + _id suffix. | false | advanced |   |   |
| To object ID field name | CloverDX field name mapped to the HubSpot ID. Optional, if no value provided, field name will be generated as lowercase object name + _id suffix. | false | advanced |   |   |
| Operation | Choose one of available operations:<br>* create<br>* list<br>* remove | true | basic | enumeration | Options:<br>Create&#124;Create association<br>Remove&#124;Remove association<br>List&#124;List associations<br><br>Custom values allowed. |
| Private app token | HubSpot Private App token. Takes priority over OAuth2 if both provided. | false | basic |   |   |
| OAuth2 connection | File URL of the .cfg file holding an authorised CloverDX HubSpot OAuth2 connection. | false | basic | file picker | Only files.<br>Files with extension(s):<br>*.cfg<br> |
| Http call retry count | Defines number of retry attempts when HTTP call fails. Set to zero to disable retry after HTTP call failure. | false | advanced | integer |   |
| Http call retry delay | Defines delay between retry attempts in milliseconds. | false | advanced | integer |   |
| Http call timeout | How long the component waits to get a response. If it does not receive a response within a specified limit, the execution of the component fails. <br>Timeout is in milliseconds. Different time units can be used. See Time Intervals in clover documentation. | false | advanced |   |   |

## CrmOwnersApiV3

Reads owner records.

### Ports

| Port     | Used for    | Operation             |
|----------|-------------|-----------------------|
| Input 0  | Input data  | read                  |
| Output 0 | Output data | read, list            |
| Output 1 | Errors      | *                     |

### Usage and metadata

Behaves similarly to CrmObjectApiV3 but comes with default mapping and metadata.

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| HS properties to metadata mapping | Provide mapping between HubSpot property names and CloverDX metadata field names, in the .properties file format. Left side represents HubSpot names, right side represents CloverDX names. CloverDX names are optional, if blank the HubSpot name will be used. | false | basic | properties |   |
| Operation | Select one of the operations: List, Read. | true | basic | enumeration | Options:<br>Read<br>List<br> |
| Object ID field name | CloverDX field name mapped to the HubSpot ID. Optional, if no value provided, field name will be generated as lowercase singular object name + _id suffix. | false | advanced | metadata field |   |
| Private app token | HubSpot Private App token. Takes priority over OAuth2 if both provided. | false | basic |   |   |
| OAuth2 connection | File URL of the .cfg file holding an authorised CloverDX HubSpot OAuth2 connection. | false | basic | file picker | Only files.<br>Files with extension(s):<br>*.cfg<br> |
| Look in archived records? | Switches between working on regular or archived records (in trash). | false | advanced | bool |   |
| Batch size | Size of a page. Default 10. Max (appears to be) 100. Larger value should speed up the list operation. | false | advanced | integer |   |
| Http call retry count | Defines number of retry attempts when HTTP call fails. Set to zero to disable retry after HTTP call failure. | false | advanced | integer |   |
| Http call retry delay | Defines delay between retry attempts in milliseconds. | false | advanced | integer |   |
| Http call timeout | How long the component waits to get a response. If it does not receive a response within a specified limit, the execution of the component fails. <br>Timeout is in milliseconds. Different time units can be used. See Time Intervals in clover documentation. | false | advanced |   |   |
| Use default metadata | If enabled pre-made owner metadata is propagated to the output port. | true | basic | bool |   |

## ParseAssociations

A companion graph of CrmObjectApiV3 graph - can parse the JSON/variant value returned, when using the associations option into flat records.

### Ports

| Port     | Used for    |
|----------|-------------|
| Input 0  | Input data  |
| Output 0 | Output data |

### Usage and metadata
A variant field in the input metadata is expected to hold the data. The field is be selected using a parameter. Predefined metadata is propagated through output port.

### Parameters
| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| Associations input field name | Selects field holding the association data. | false | advanced | metadata field |   |

## AccountDetailsApiV3

Returns information about the HubSpot account such as its ID, type, currency, time-zone, etc. Furthermore API rate limits are also read from the response header.

### Ports

| Port     | Used for    |
|----------|-------------|
| Output 0 | Output data  |

### Usage and metadata

Has predefined output metadata.

## CrmListAllObjects

Returns a list of all object types available in the HubSpot CRM schema along with their respective properties.

This component is useful for developers who want to dynamically inspect or document the structure of custom or standard CRM objects. The output can serve as the basis for building automated mappings or interfaces for custom HubSpot object handling.

| Port     | Used for    | Operation             |
|----------|-------------|-----------------------|
| Output 0 | Output data | read, list            |
| Output 1 | Output data | read, list            |
| Output 2 | Errors      | *                     |

### Usage and metadata

The subgraph queries the CRM Object Schemas API and returns a list of objects and their properties:
- Output 0: List of available CRM objects, with key fields such as fullyQualifiedName, objectTypeId, and timestamps.
- Output 1: Field/property definitions for each object, including fields like name, label, type, archived, etc.
- Output 2 (optional): Allows for handling HTTP request errors gently.

The subgraph supports both OAuth2 and private app token authentication. Authentication method is chosen dynamically based on the presence of the PRIVATE_APP_TOKEN parameter value.

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| Private app token | HubSpot Private App token. Takes priority over OAuth2 if both provided. | false | basic |   |   |
| OAuth2 connection | File URL of the .cfg file holding an authorised CloverDX HubSpot OAuth2 connection. | false | basic | file picker | Only files.<br>Files with extension(s):<br>*.cfg<br> |
| Http call retry count | Defines number of retry attempts when HTTP call fails. Set to zero to disable retry after HTTP call failure. | false | advanced | integer |   |
| Http call retry delay | Defines delay between retry attempts in milliseconds. | false | advanced | integer |   |
| Http call timeout | How long the component waits to get a response. If it does not receive a response within a specified limit, the execution of the component fails. <br>Timeout is in milliseconds. Different time units can be used. See Time Intervals in clover documentation. | false | advanced |   |   |

# <span class='tabLabel'>Installation & Setup</span>

## Requirements

- No library dependencies
- OAuth2 connection or Private App token.  
- HubSpot developers account (for OAuth2 and testing portals).  
- Administrator access to a HubSpot portal (to set up the OAuth2 connection or to generate the private app token).  

## Installation

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

### Authorization

One of (see [HubSpot documentation](https://developers.hubspot.com/docs/api/intro-to-auth)):

- authorized OAuth2 connection: scoped public application uploaded to HubSpot marketplace which can connect to multiple portals. HubSpot developer account is needed to create the application. [See documentation for more details](https://doc.cloverdx.com/latest/developer/oauth2-connections.html#id_oauth2_connection_creating)
- API key: portal-wide unrestricted access (deprecated by HubSpot in November 2022)
- private app token: scoped private app at given portal (replaced API key)

<a href="https://www.youtube.com/watch?v=8R5ZUAoYE7I" target="_blank">Watch this video to learn how to set up OAuth2 for use in CloverDX Libraries.</a>

### Health-check

The library implements a health check using the AccountDetailsApiV3.sgrf to check whether it is possible to connect to HubSpot using the provided credentials. The "oauth" scope might be needed for this check to work.