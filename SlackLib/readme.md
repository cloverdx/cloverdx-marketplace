# <span class='tabLabel'>About</span>

Send Slack notifications from a CloverDX process.

# Components provided by this library

* Chat-PostMessage - send a message to a channel.
* Conversations-List - list available channels

# Properties

Name: SlackLib  
Label: Slack  
Author: CloverDX  
Version: 1.0.1  
Compatible: CloverDX 6.0 and newer  

# Tags

slack notifications messaging

# <span class='tabLabel'>Documentation</span>

## Chat-PostMessage

Sends a message to a channel.

### Ports
| Port          | Required | Used for         | Description |
|---------------|----------|------------------|-------------|
| Input port 0  | Required | Data in          | Per message request parameters and data |
| Output port 0 | Optional | Data out         | Endpoint's response  |
| Output port 1 | Optional | Error handling   | Configuration and runtime errors |

### Usage and metadata

The component sends a message for each incoming record. The contents and properties of the message are determined in one of two ways. Either the data is taken from specific fields in the incoming records (if present) or a message is derived from a template supplied by a graph parameter.

See the <a href="https://api.slack.com/methods/chat.postMessage" target="_blank">API endpoint</a> for details about the required scopes (additional scopes may be required to use advanced options), available parameters and functionality.

#### Request parameters edge input

The subgraph is able to pass most of the parameters accepted by the <a href="https://api.slack.com/methods/chat.postMessage" target="_blank">API endpoint</a> from the input edge. Simply use metadata that have a field named the same as the parameter plus a *slack_* prefix, e.g. to send a "text" parameter the metadata should have a *slack_text* field. You can derive your metadata from _slackMessagePrefixed.fmt_ file, which contains all the fields that can be used request parameters.

#### Example: 

To post a simple message just create metadata with a *slack_text* field and send a record with your message to the component. The *slack_text* will be sent as *text* parameter of the request.

#### Using templates

A message can also be created by filling in data from an input port into a template.Template is a JSON documents using the <a href="https://api.slack.com/reference/block-kit/blocks" target="_blank">block-kit</a> syntax/structure. You can use the <a href="https://app.slack.com/block-kit-builder" target="_blank">builder app</a> to help you construct the template.

Use _$in.fieldname_ syntax to substitute values from input port.

The completed template will be send in the "blocks" request parameter.

#### Example:

**Chat-PostMessage input metadata field:**

| Field name | Field type |
| --- | --- | 
| requestNumber | integer |
| requestContent | string |
| clientName | string |

**Template**

```
{
	"blocks": [
		{
			"type": "section",
			"text": {
				"type": "mrkdwn",
				"text": "There is a new request (req. #$in.requestNumber) in the system for customer $in.clientName."
			}
		},
		{
			"type": "section",
			"text": {
				"type": "mrkdwn",
				"text": "Request: '$in.requestContent'"
			}
		},
		{
			"type": "divider"
		}
	]
}
```

**Chat-PostMessage input record:**

| requestNumber | requestContent | clientName |
| --- | --- | --- |
| 777 | Oil Change and Filter Replacement | John Doe |
| 778 | Brake Inspection and Pad Replacement | David Johnson |
| 779 | Tire Rotation and Alignment Check | Emily Carter |
| 780 | Coolant System Flush and Refill | Sarah Miller |

**Messages delivered to slack channel**

There is a new request (req. #777) in the system for customer John Doe.<br>
Request: 'Oil Change and Filter Replacement'
<hr>
There is a new request (req. #778) in the system for customer David Johnson.<br>
Request: 'Brake Inspection and Pad Replacement'
<hr>
There is a new request (req. #779) in the system for customer Sarah Miller.<br>
Request: 'Tire Rotation and Alignment Check'
<hr>
There is a new request (req. #780) in the system for customer null.<br>
Request: 'Coolant System Flush and Refill'
<hr>




### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| Bearer token | Authentication token granting access to post messages via Slack API. | false | basic |   |   |
| Channel | Id of a channel where the message will be posted.<sup>1</sup>  | false | basic | enumeration | Options: Can be populated by the library initialization graph.|
| Message template | Message template in JSON format with placeholders that will be replaced by field values.<br>Placeholder format: $in.<fieldName> | false | basic | multiline string |   |
| Message template URL | URL of a JSON file with message template. Only used when MESSAGE_TEMPLATE parameter is blank. | false | basic | file picker |  |
| Rate limit delay (ms) | This API endpoint is limited at 1 message per second per channel. See https://api.slack.com/docs/rate-limits. Input value in milliseconds. | false | advanced | integer |   |

<sup>1</sup> The name of the channel might work too, but this is not documented by Slack.
<br><sup>2</sup> The API tolerates bursts to a degree - lowering the delay or setting it zero might improve performance if you are sending a small batch of messages. Automatic retry on exceeding the API limits is not implemented. .

<a name="Conversations-List"></a>
## Conversations-List

Lists all channels in a Slack team/available visible using this auth token.

### Ports
| Port          | Required | Used for         | Description |
|---------------|----------|------------------|-------------|
| Output port 0 | Optional | Data out         | Channel details  |
| Output port 1 | Optional | Error handling   | Configuration and runtime errors |

### Usage and metadata

See the <a href="https://api.slack.com/methods/chat.postMessage" target="_blank">API endpoint</a> for details about the required scopes (additional scopes may be required to use advanced options), available parameters and functionality.

The output metadata covers most of the data returned by the endpoint. 

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| Bearer token | Authentication token granting access to post messages via Slack API. | false | basic |   |   |
| Team ID | Encoded team id to list channels in, required if token belongs to org-wide app. | false | basic |   |   |
| Conversation type filter | Mix and match channel types by providing a comma-separated (or semicolon - will be converted automatically) list of any combination of public_channel, private_channel, mpim, im. | false | basic | multiple metadata fields |   |
| Exclude archived | Set to true to exclude archived channels from the list. | false | advanced | bool |   |
| Limit | The maximum number of items to return. Fewer than the requested number of items may be returned, even if the end of the list hasn't been reached. Must be an integer no larger than 1000. | false | advanced |   |   |

# <span class='tabLabel'>Installation & Setup</span>

## Requirements

- No library dependencies.
- Slack account to create an app and generate auth tokens 

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

See https://api.slack.com/authentication/basics. Create and app, add the required scopes (at least *chat:write*), install it in the workspace and use the generated token. User level OAuth flow and access is not currently implemented by this library. 

### Before first use (Optional)

The library comes with an initialization graph that can pre-fill available channels into a parameter file, making it easier to pick a channel afterwards.

1. In Server Console, go to Libraries > SlackLib > *Configuration* tab
2. Enter the Bearer token (User or Bot User OAuth Token) and team id (workspace) if using an organization wide app.
3. Press "Save changes" button to confirm your settings.
4. Run the graph using the "Initialize library" button
5. You can check the run in execution history

