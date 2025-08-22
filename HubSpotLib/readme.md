# <span class='tabLabel'>About</span>

Provides read-only access to HubSpot CRM data - Contacts, Companies, Deals, LineItems, Products and Tickets.

Data is accessed directly (live) through HubSpot CRM API. Please note, using these components counts towards your [HubSpot API usage limits](https://developers.hubspot.com/docs/api/usage-details). Due to speed limitations of the API, larger data sets might take longer to process.

# Properties

Name: HubSpotLib  
Label: HubSpot CRM  
Author: CloverDX  
Version: 1.2  
Compatible: CloverDX 5.17 and newer  

# Tags

hubspot crm reader wrangler connector data-source-connectors

# <span class='tabLabel'>Documentation</span>

## Components provided by this library

## GetAllCompanies

Returns all *Companies* with all properties (including custom properties).

### Ports
| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output port 0 | Required      | Data    | Company details |
| Output port 1 | Optional       | Error handling   | Parsing and runtime errors |

## GetAllContacts

Returns all *Contacts* with all properties (including custom properties).

### Ports
| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output port 0 | Required      | Data    | Contact details |
| Output port 1 | Optional       | Error handling   | Parsing and runtime errors |

## GetAllDeals

Returns all *Deals* with all properties (including custom properties).

Will also extract the deal owner info and optionally the primary associated company's info. The retrieved properties of the associated records cannot be customized. 

The extraction of the primary associated company's info is controlled by the "Get primary associated company?" configuration option, which can be set to one of:
* "Get associated company data" - (Default) 
* "Only get the ids of associated companies"
* "Get associated company data (by downloading the whole lookup)" - might be faster/less memory intensive than the default method in rare cases


### Ports
| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output port 0 | Required      | Data    | Deal details |
| Output port 1 | Optional       | Error handling   | Parsing and runtime errors |

## GetAllLineItems

Returns all *Line Items* with all properties.
Line items are *Products* attached to a particular *Deal*.

### Ports
| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output port 0 | Required      | Data    | Line item details |
| Output port 1 | Optional       | Error handling   | Parsing and runtime errors |

## GetAllProducts

Returns all *Products*.

### Ports
| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output port 0 | Required      | Data    | Product details |
| Output port 1 | Optional       | Error handling   | Parsing and runtime errors |

## GetAllTickets

Returns all *Tickets* with all properties (including custom properties).

### Ports
| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output port 0 | Required      | Data    | Ticket details |
| Output port 1 | Optional       | Error handling   | Parsing and runtime errors |


# Data Source Connectors provided by this library

Data Source Connectors are modules that allow CloverDX Wrangler to access and manipulate data using this library. The following entities are available as Data Source Connectors. Structure of the actual entities is the same as it was described above.

- Products
- Tickets
- Companies
- Deals
- Contacts

# <span class='tabLabel'>Installation & Setup</span>

## Dependencies

These additional libraries will need to be installed on your Server.

- [DataAnalyticsBundleLib v. 1.0](https://marketplace.cloverdx.com/DataAnalyticsBundleLib_1.1.html)
- [HubSpotCrmApiLib v. 1.0](https://marketplace.cloverdx.com/HubSpotCrmApiLib_1.1.html)

For online installation, you can go to Server Console, navigate to Libraries > *Install library from repository* and select the required libraries.  
For offline installation, download the libraries from CloverDX Marketplace (.clib files). In Server Console, navigate to Libraries > *Install library from repository* > Down arrow for more options > *Browse local files...* and install the Libraries. 

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

### Health-check

The library implements a health check using a account details endpoint to check whether it is possible to connect HubSpot using the provided credentials. The "oauth" scope might be needed for this check to work.

## Before first use (REQUIRED)

1. In Server Console, go to Libraries > HubSpot > *Configuration* tab
2. Configure connection method.
    - Enter Private App token **(recommended option)**<br>
      **OR** 
    - Provide OAuth2 configuration file (e.g. sandboxes://MySandbox/conn/HubSpot.cfg) see [OAuth2 documentation](https://doc.cloverdx.com/latest/designer/oauth2-connections.html) for more details.
3. Press "Save changes" button to confirm your settings.
4. Run "Initialize library"
5. You don't need to initialize the dependencies listed under Requirements, just install them.

Please note that the library uses all fields of the entities (Products, Tickets, ...) by default. If you need to specify a subset of the fields, you can set the fields you need in the configuration. For every entity, there is a configuration property. If you leave it blank, all fields will be used in the output metadata (default behavior). If you need to, put there field names separated by commas, and only the specified fields will be part of the output metadata.

