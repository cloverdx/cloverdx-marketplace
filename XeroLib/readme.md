# <span class='tabLabel'>About</span>

Easily connect live Xero data to CloverDX. This library contains components for getting data from <a href="https://developer.xero.com/documentation/api/accounting/overview" target="_blank">Xero Accounting API</a>.
The library supports obtaining data from Xero Accounting API. There are two main groups of functionality - Contextual Connectors allowing to solve specific use cases and Generic Components to get raw Xero data.

# Properties
Name: XeroLib  
Label: Xero  
Author: CloverDX  
Version: 1.1  
Compatible: CloverDX 6.0.0 and newer  

# Tags
xero accounting reader wrangler data-source-connector

# <span class='tabLabel'>Documentation</span>

## Connectors


## Get Accounts Payable

Provides supplier invoices

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/invoices#get-invoices" target="_blank">invoice data</a> 
| Output 1 | Output invoice payments data
| Output 2 | Output invoice credit notes data
| Output 3 | Errors

## Get Accounts Payable Near Due Date

Allows you to get supplier invoices with coresponding customer details with due date in the next 7 days. Number of days can be changed as optional input parameter.

### Optional input filter conditions

- **Date** - Number of days before due date, default value set to 7

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/invoices#get-invoices" target="_blank">invoice data</a>
| Output 1 | Output contact data
| Output 2 | Output contact address data
| Output 3 | Output contact phone data

## Get Accounts Receivable

Provides customer invoices

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/invoices#get-invoices" target="_blank">invoice data</a> 
| Output 1 | Output invoice payments data
| Output 2 | Output invoice credit notes data
| Output 3 | Errors

## Get Accounts Receivable After Due Date

Allows you to get supplier invoices with coresponding customer details with due date in the last 7 days. Number of days can be changed as optional input parameter.

### Optional input filter conditions

- **Date** - Number of days after due date, default value set to 7

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/invoices#get-invoices" target="_blank">invoice data</a>
| Output 1 | Output contact data
| Output 2 | Output contact address data
| Output 3 | Output contact phone data

## GET Accounts

Allows you to retrieve the accounts with optional filter conditions. <a href="https://developer.xero.com/documentation/api/accounting/accounts" target="_blank">API full details</a>

### Optional input filter conditions

- **Name** - Optional filter by account name
- **Status** - Optional filter by account status
- **Type** - Optional filter by account type

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/accounts#get-accounts" target="_blank">account data</a>
| Output 1 | Output API call data     
| Output 2 | Errors

## GET Bank Transactions

Allows you to retrieve any spend or receive money transactions with optional filter condition. <a href="https://developer.xero.com/documentation/api/accounting/banktransactions" target="_blank">API full details</a>

### Optional input filter conditions

- **Type** - Optional filter by transaction type

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output bank <a href="https://developer.xero.com/documentation/api/accounting/banktransactions#get-banktransactions" target="_blank">transaction data</a>
| Output 1 | Output API call data     
| Output 2 | Errors 

## GET Contacts

Allows you to retrieve the contacts with optional filter conditions. <a href="https://developer.xero.com/documentation/api/accounting/contacts" target="_blank">API full details</a>

### Optional input filter conditions

- **Name** - Optional filter by contact name
- **Email Address** - Optional filter by email

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/contacts#get-contacts" target="_blank">contact data</a>
| Output 1 | Output contact address data
| Output 2 | Output contact phone data
| Output 3 | Output API call data
| Output 4 | Errors

## GET Credit Notes

Allows you to retrieve the credit notes data. <a href="https://developer.xero.com/documentation/api/accounting/creditnotes" target="_blank">API full details</a>

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/creditnotes#get-creditnotes" target="_blank">credit note data</a>
| Output 1 | Output API call data     
| Output 2 | Errors

## GET Invoices

Allows you to retrieve the invoices with optional filter conditions. <a href="https://developer.xero.com/documentation/api/accounting/invoices" target="_blank">API full details</a>

### Optional input filter conditions

- **Name** - Optional filter by account name
- **Status** - Optional filter by account status
- **Type** - Optional filter by account type

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/invoices#get-invoices" target="_blank">invoice data</a> 
| Output 1 | Output invoice payments data
| Output 2 | Output invoice credit notes data
| Output 3 | Output API call data
| Output 4 | Errors

## GET Journals

Allows you to retrieve the journal data. <a href="https://developer.xero.com/documentation/api/accounting/journals" target="_blank">API full details</a>

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output <a href="https://developer.xero.com/documentation/api/accounting/journals#get-journals" target="_blank">journal data</a>
| Output 1 | Output journal line data
| Output 2 | Output API call data
| Output 3 | Errors

## GET Manual Journals

Allows you to retrieve the manual journals data. <a href="https://developer.xero.com/documentation/api/accounting/manualjournals" target="_blank">API full details</a>

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output manual <a href="https://developer.xero.com/documentation/api/accounting/manualjournals#get-manualjournals" target="_blank">journal data</a>
| Output 1 | Output API call data     
| Output 2 | Errors

## GET Payments

Allows you to retrieve the payments data. <a href="https://developer.xero.com/documentation/api/accounting/payments" target="_blank">API full details</a>

### Optional input filter conditions

- **Payment type** - Optional filter by type
- **Status** - Optional filter by status
- **Date** - Optional filter by date - greater than type of condition (e.g. 2020-01-01 will be evaluated as Date>DateTime(2020, 01, 01))

### Ports

| Port     | Used for
|----------|-----------------------------
| Output 0 | Output  <a href="https://developer.xero.com/documentation/api/accounting/payments#get-payments" target="_blank">payment data</a>
| Output 1 | Output API call data     
| Output 2 | Errors

# <span class='tabLabel'>Installation & Setup</span>

### Online installation (Server connected to Internet)

1. In Server Console, navigate to Libraries > Install library from repository.
2. Select Library Repository dropdown > CloverDX Marketplace.
3. Check the box next to the libraries you want to install.
4. Click Install.

### Offline installation (Server without Internet connection)

1. Download the library from the CloverDX Marketplace. You should get a ".clib" file.
2. Transfer the ".clib" file to your offline Server machine (USB stick, ...)
3. In Server Console, navigate to Libraries > Install library from repository > Down arrow for more options > Browse local files...
4. Select the downloaded .clib file on your disk and install

## Configuration

<a href="https://www.youtube.com/watch?v=Mxga8kzqCg8&ab_channel=CloverDX" target="_blank">Xero Library setup video</a>

Please watch this step by step video on how to install and configure the Xero library in three easy steps:  
#### 1) Create account on Xero development console  
- Page for setup Xero: <a href="https://developer.xero.com/app/manage" target="_blank">https://developer.xero.com/app/manage</a>
#### 2) Create connection in CloverDX sandbox  

Info for setup OAuth2 connection:  
- Scopes: accounting.reports.read accounting.transactions.read accounting.settings.read accounting.journals.read accounting.contacts.read offline_access  
    All required scopes are read only, please visit the following <a href="https://developer.xero.com/documentation/guides/oauth2/scopes" target="_blank">page</a> for more info about scopes.  
- Authorization endpoint: https://login.xero.com/identity/connect/authorize  
- Token endpoint: https://identity.xero.com/connect/token  
- Redirect URL: https://l<clover server>/oauth2 (e.g. http://localhost:8083/clover/oauth2). This needs to be https with the exception of localhost.  
#### 3) Import and setup Library from  CloverDX marketplace  
