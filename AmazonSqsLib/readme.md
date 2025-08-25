# <span class='tabLabel'>About</span>

Provides Reader/Writer and Deleter components for Amazon SQS.  

# Description

The project supports the following operations with SQS messages:
 - Read
 - Write
 - Delete

# AWS SDK

| Library version | AWS SQS SDK version |
|----------|----------------------------|
| 1.0      | 1.11.1009                  |

# Properties
Name: AmazonSqsLib  
Label: Amazon SQS  
Author: CloverDX  
Version: 1.0  
Compatible: CloverDX 5.14 and newer  

# Tags
aws sqs reader writer java

# <span class='tabLabel'>Documentation</span>

## Common parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| AWS Access key | Enter AWS access key. If access and secret keys are not provided, components will attempt to get them from the environment. | false | basic |   |   |
| AWS Secret key | Enter AWS secret key. If access and secret keys are not provided, components will attempt to get them from the environment. | false | basic |   |   |
| Queue name | Enter the name of the AWS SQS queue. | true | basic |   |   |
| SQSRegion | Select the region in which the SQS queue resides. This will be used to derive both the service endpoints and signing region by the AWS SDK. If no value is provided some default value is derived by the SDK. Format for manual input from parent graph: regions code e.g us-east-1 (NOT the label e.g. US East (N. Virginia)). | true | basic | enumeration | Options: <br> A list of existing regions as defined in the regions enum of the included SDK version.|

# Components

## AwsSqsReader

The reader component can either read data or get an estimated number of pending messages. Enabling this feature, a user will get a dummy message and queue health report on the secondary output (as message attribute) of the component. 

The component also can read structured meta information (called attributes in AWS SQS).

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Output 1 | Message body       |
| Output 2 | Message attributes |

Metadata is set and auto-propagated out of the component on each port.

### Parameters

In addition to the common parameters.

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| Remove read messages | If enabled, the messages read will be deleted from the queue. | false | advanced | bool |   |
| Only check queue status | If enabled, the component will return the approximate number of pending messages on port 2, but will not actually read any messages. | false | advanced | bool |   |
| Limit number of retrieved messages | Set maximum number of messages that should be retrieved from the queue. Leave empty or set 0/negative for unlimited. | false | basic | integer |   |
| Wait up to (sec) | Amount of time (in seconds) to wait for a message to appear in the queue. Max 20. Also changes the polling strategy: short vs long polling. Default is short polling (0 sec).<br>Read more [here](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-short-and-long-polling.html) | false | advanced | integer |   |
| Visibility timeout (sec) | The duration (in seconds) that the received messages are hidden from subsequent retrieve requests after being retrieved. Default 30s. Maximum 12 hours. | false | advanced | integer |   |

## AwsSqsWriter

The writer component can write SQS messages with (optionally) attributes. Use the id field in the input metadata as a synthetic key for joining messages with their attributes. Records need to be sorted as if you were performing a merge join. If you do not want to use message attributes you can leave the id field empty.

Read more about SQS attributes [here](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-message-metadata.html)
Read more about limits placed on SQS messages [here](https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/quotas-messages.html)

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Input 1  | Message body       |
| Input 2  | Message attributes |

Metadata is set and auto-propagated out of the component on each port.

## AwsSqsDeleter

The deleter component can be used to manually delete a message from an SQS queue (if you are not using the auto-delete feature of the reader). The component uses the receipt handle that you receive when reading the message using the reader.

### Ports & metadata
| Port     | Used for              |
|----------|-----------------------|
| Input 1  | Message receipt handle|


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

# Requirements
AWS SQS, IAM user with access to SQS

# Authorization
Each component is capable of directly using AWS credentials or taking credential information from the environment. See [Amazon documentation](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-roles.html), section "Default provider chain" for details.