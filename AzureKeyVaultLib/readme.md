# <span class='tabLabel'>About</span>

Connectors that allow reading and writing secrets to/from Azure Key Vault.

# Description
The library supports the following operations:
- Get a secret from the Azure Key Vault
- Set a secret to the Azure Key Vault
- Get a certificate from the Azure Key Vault

# Properties
Name: AzureKeyVaultLib  
Label: Azure Key Vault  
Author: CloverDX  
Version: 1.1  
Compatible: CloverDX 5.14 and newer  

# Tags
azure-key-vault secrets connector reader writer certificate cloud  


# <span class='tabLabel'>Documentation</span>

## KeyVaultSecretGet

Read the value of a secret from Azure Key Vault. Allows access to previous secret versions as well as the latest (current) version.

### Attributes

| Name                         | Required | Description |
|------------------------------|----------|-------------|
| **Key Vault name**           | Yes      | Name of the Key Vault instance to work with. Instance must exist and must be writable by user configured in the OAuth2 connection below. |
| **OAuth2 connection URL**    | Yes      | URL of the OAuth2 connection that allows read/write access to the Key Vault configured in previous attribute. Connection must be properly authorized. |
| **Key Vault secret name**    | Yes      | Name of the secret to query. Names are case-sensitive, and the secret must exist for the operation to succeed. |
| **Key Vault secret version** | No       | Version of the secret to query. If not set, latest version is queried. |

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output 1 | No       | Success output | Information about secret created or modified. |
| Output 2 | No       | Error output   | Information about any error that occurred while the secret was written. If the port is not connected and an error occures, the component fails.|

### Output port 0: Success output

Exactly one record will be produced on an output port.

| Field name      | Type    | Required |Description |
|-----------------|---------|----------|------------|
| `secretName`    | string  | yes      | Name of the secret. |
| `secretValue`   | string  | yes      | Value of the secret. |
| `id`            | string  | yes      | Full unique id of the secret. Id is a full URL to the secret including the version that was queried. |
| `created`       | date    | yes      | Date when the secret was created. |
| `updated`       | date    | yes      | Date when the secret was last updated. |
| `enabled`       | boolean | yes      | Flag storing information about whether the secret is enable (`true`) or disabled (`false`). If secret is disabled, its value cannot be retrieved. |
| `recoveryLevel` | string  | yes      | Recovery level. |

### Output port 1: error output

At maximum one record will be produced on this port if the secret read operation failed.

| Field name     | Type    | Required |Description |
|----------------|---------|----------|------------|
| `errorMessage` | string  | yes      | Full error message describing the failure reason. |


## KeyVaultSecretSet

Write new secret value into Azure Key Vault secret. If the secret does not exist, it is automatically created. Secrets are created based on data coming into the component via the input port to allow for sending new values generated based on any algorithm.

### Attributes

| Name  | Required | Description |
|-------|----------|-------------|
| **Key Vault name** | Yes | Name of the Key Vault instance to work with. Instance must exist and must be writable by user configured in the OAuth2 connection below. |
| **OAuth2 connection URL** | Yes | URL of the OAuth2 connection that allows read/write access to the Key Vault configured in previous attribute. Connection must be properly authorized. |

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Input 0  | Yes      | Input data     | New secret values. |
| Output 1 | No       | Success output | Information about secret created or modified. |
| Output 2 | No       | Error output   | Information about any error that occurred while the secret was written. If the port is not connected and an error occures, the component fails.|

### Input port 0: Secret settings

Any number of records can be submitted to the input port - each record will cause one secret to be created/updated in Key Vault.

| Field name      | Type    | Required |Description |
|-----------------|---------|----------|------------|
| `secretName`    | string  | yes      | Name of the secret to create or update. |
| `secretValue`   | string  | yes      | New value of the secret. |

### Output port 0: Success output

One record will be produced for every record sent into the component.

| Field name      | Type    | Required |Description |
|-----------------|---------|----------|------------|
| `id`            | string  | yes      | Full unique id of the secret. Id is a full URL to the secret including the version that was queried. |
| `enabled`       | boolean | yes      | Flag storing information about whether the secret is enable (`true`) or disabled (`false`). If secret is disabled, its value cannot be retrieved. |
| `created`       | date    | yes      | Date when the secret was created. |
| `updated`       | date    | yes      | Date when the secret was last updated. |
| `recoveryLevel` | string  | yes      | Recovery level. |

### Output port 1: error output

One record will be produced on this port for each failure when setting the values of secrets that are submitted to the component.

| Field name     | Type    | Required |Description |
|----------------|---------|----------|------------|
| `errorMessage` | string  | yes      | Full error message describing the failure reason. |


## KeyVaultCertificateGet

Query a certificate from Azure Key Vault. Gets information about a specific certificate. This operation requires the certificates/get permission.

### Attributes

| Name | Required | Description |
| --- | --- | --- |
| Key Vault name | true | Name of the Key Vault instance to work with. Instance must exist and must be writable by user configured in the OAuth2 connection below. |
| OAuth2 connection URL | true | URL of the OAuth2 connection that allows read/write access to the Key Vault configured in previous attribute. Connection must be properly authorized. |
| Certificate name | true | The name of the certificate in the given vault.  |
| Certificate version | false | This parameter is optional. If not specified, the latest version of the certificate is returned.  |
| Certificate target file URL | false | File URL where to save certificate. If empty, certificate will be sent only to the output port. |

### Ports

| Port     | Required | Used for       | Description |
|----------|----------|----------------|-------------|
| Output 1 | No       | Success output | Information about certificate. |
| Output 2 | No       | Error output   | Information about any error that occurred while the secret was written. If the port is not connected and an error occures, the component fails.|

### Output port 0: Success output

Exactly one record will be produced on an output port.

| Field name      | Type    |Description |
|-----------------|---------|------------|
| `id`| string  | The certificate id |
| `kid`| string  | The key id |
| `sid`| string  | The secret id |
| `x5t`| string  | Thumbprint of the certificate |
| `cer`| string  | CER contents of x509 certificate |
| `created`| date  | Creation time in UTC |
| `exp`| date  | Expiry date in UTC |
| `nbf`| date  | Not before date in UTC |
| `enabled`| boolean  | Determines whether the object is enabled |
| `updated`| date  | Last updated time in UTC |
| `recoverableDays`| string  | softDelete data retention days. Value should be >=7 and <=90 when softDelete enabled, otherwise 0 |
| `recoveryLevel`| string  | Reflects the deletion recovery level currently in effect for certificates in the current vault. If it contains 'Purgeable', the certificate can be permanently deleted by a privileged user; otherwise, only the system can purge the certificate, at the end of the retention interval. |
| `key_kty`| string  | The type of key pair to be used for the certificate. |
| `key_size`| integer  | The key size in bits. For example: 2048, 3072, or 4096 for RSA |
| `key_crv`| string  | Elliptic curve name |
| `key_exportable`| boolean  | Indicates if the private key can be exported. Release policy must be provided when creating the first version of an exportable key |
| `key_reuse`| boolean  | Indicates if the same key pair will be used on certificate renewal |
| `secret_content_type`| string  | The media type (MIME type) |
| `X509_subject`| string  | The subject name. Should be a valid X509 distinguished Name |
| `X509_ekus`| string  | The enhanced key usage |
| `X509_key_usage`| string  | Defines how the certificate's key may be used |
| `X509_sans_dns_names`| string  | SubjectAlternativeNames - Domain names |
| `X509_sans_emails`| string  | SubjectAlternativeNames - Email addresses |
| `X509_sans_upns`| string  | SubjectAlternativeNames - User principal names |
| `issuer_cert_transparency`| boolean  | Indicates if the certificates generated under this policy should be published to certificate transparency logs |
| `issuer_cty`| string  | Certificate type as supported by the provider (optional); for example 'OV-SSL', 'EV-SSL' |
| `issuer_name`| string  | Name of the referenced issuer object or reserved names; for example, 'Self' or 'Unknown' |

### Output port 1: error output

At maximum one record will be produced on this port if the secret read operation failed.

| Field name     | Type    | Required |Description |
|----------------|---------|----------|------------|
| `errorMessage` | string  | yes      | Full error message describing the failure reason. |

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

Before the subgraphs can be used, a valid OAuth2 connection is needed. This connection must allow sufficient access to the Key Vault instance - at minimum read access is required to be able to query values. This connection must be created in the calling project. The connection is passed to Key Vault subgraphs as a URL to the OAuth2 `cfg` file.

<a href="https://www.youtube.com/watch?v=8R5ZUAoYE7I" target="_blank">Watch this video to learn how to set up OAuth2 for use in CloverDX Libraries.</a>
