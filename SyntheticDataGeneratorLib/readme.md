# <span class='tabLabel'>About</span>

Generator for variety of synthetic data sets that can be used in examples or as basis for more complex synthesised data sets.

# Properties
Name: SyntheticDataLib  
Label: Synthetic Data Generator  
Author: CloverDX  
Version: 2.0  
Compatible: CloverDX 6.0 and newer  

# Tags
synthetic-data random contact person adddress wrangler data source connector

# <span class='tabLabel'>Documentation</span>

## Data Source Connectors

### OnlineStoreProducts

Get list of all products sold by the OnlineStore.

#### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Generated products. |


### OnlineStoreContacts

Get customer contacts in an OnlineStore. Contacts have name, address, email and phone.

#### Attributes

| Name                                          | Required | Default        | Description |
|-----------------------------------------------|----------|----------------|-------------|
| **Output language**                           | No       | English (US)   | Language of the generated data. Available values: English (US), Czech, Japanese (Kana), All (Mixed). |
| **Number of records to generate**             | No       | 10000          | Number of generated records. |
| **Minimum customer age (years)**              | No       | 18             | Maximum age of years |

#### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Generated contacts. |

### OnlineStoreInvoices

Return Invoices from the OnlineStore.

#### Attributes

| Name                                                           | Required       | Default        |
|----------------------------------------------------------------|----------------|----------------|
| **Number of records to generate**                              | No             | 1000           | 
| **Minimum Invoice Due Time**                                   | No             | 2023-01-01     |
| **Minimum Invoice Due Time**                                   | No             | 2023-12-31     |
| **Maximum Invoice Amount**                                     | No             | 50000          |
| **Probability to Past-Due Invoice in percentage point value**  | No             | 5              |

#### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Generated Invoices. |


### OnlineStoreOrders

Return orders from the OnlineStore.

#### Attributes

| Name                                                           | Required       | Default        |
|----------------------------------------------------------------|----------------|----------------|
| **Number of records to generate**                              | No             | 10000          | 
| **Percentage of accounts who have made an order**              | No             | 95             |
| **Minimum number of orders per customer**                      | No             | 1              |
| **Maximum number of orders per customer**                      | No             | 8              |
| **Minimum number of items per order**                          | No             | 1              |
| **Maximum number of items per order**                          | No             | 5              |
| **Minimum order date**                                         | No             | 2018-01-01     |
| **Maximum order date**                                         | No             | 2023-03-28     |

#### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Generated orders. |


### OnlineStoreOrdersLineItems

Return line items belonging to orders from the OnlineStore.

#### Attributes

| Name                                                           | Required       | Default        |
|----------------------------------------------------------------|----------------|----------------|
| **Number of records to generate**                              | No             | 10000          | 
| **Percentage of accounts who have made an order**              | No             | 18             |
| **Minimum number of orders per customer**                      | No             | 1              |
| **Maximum number of orders per customer**                      | No             | 8              |
| **Minimum number of items per order**                          | No             | 1              |
| **Maximum number of items per order**                          | No             | 5              |
| **Minimum order date**                                         | No             | 2018-01-01     |
| **Maximum order date**                                         | No             | 2023-03-28     |


#### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Generated line items. |



## Subgraphs

### GeneratePerson

Generate random person with full names and basic contact details.

#### Attributes

| Name                                          | Required | Default        | Description |
|-----------------------------------------------|----------|----------------|-------------|
| **Language**                                  | Yes      | English (US)   | Language of the generated data. Available values: English (US), Czech, Japanese (Kana), All (Mixed). |
| **Number of records to generate**             | Yes      | 10000          | Number of generated records. |
| **Min date of birth**                         | Yes      | 1940-01-01     | Minimal data of birth in format yyyy-MM-dd |
| **Max date of birth**                         | Yes      | 1923-01-01     | Maximal data of birth in format yyyy-MM-dd |
| **Middle name percent**                       | Yes      | 5              | Percentage probability whether the generated person has a middle name |
| **Percentage of people with email**           | Yes      | 99             | Percentage probability whether the generated person has an e-mail |

#### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Generated person. |

### GenerateAddress

Generate full address with realistic properties.

#### Attributes

| Name                                          | Required | Default        | Description |
|-----------------------------------------------|----------|----------------|-------------|
| **Language**                                  | Yes      | English (US)   | Language of the generated data. Available values: English (US), Czech, Japanese (Kana), All (Mixed). |
| **Number of records to generate**             | Yes      | 10000          | Number of generated records. |

#### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Generated address. |


### GenerateOrders

Generate random orders complete with dates, addresses and line items.


#### Attributes

| Name                                                           | Required       | Default        |
|----------------------------------------------------------------|----------------|----------------|
| **Number of records to generate**                              | No             | 10000          | 
| **Language for contacts and addresses**                        | No             | English (US)   |
| **Minimum customer age (years)**                               | No             | 18             |
| **Percentage of accounts who have made an order**              | No             | 95             |
| **Minimum number of orders per customer**                      | No             | 1              |
| **Maximum number of orders per customer**                      | No             | 8              |
| **Minimum number of items per order**                          | No             | 1              |
| **Maximum number of items per order**                          | No             | 5              |
| **Minimum order date**                                         | No             | 2018-01-01     |
| **Maximum order date**                                         | No             | 2023-03-28     |

#### Ports
| Port     | Required | Used for     | Description |
|----------|----------|--------------|-------------|
| Output 1 | Yes      | Data output  | Generated address. |


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

## Configuration

No special configuration is necessary. You can change on parameter called `GLOBAL_RANDOM_SEED` which determines what kind of data is generated. Using the same number will always produce the same data.


