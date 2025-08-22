# <span class='tabLabel'>About</span>

Component capable of changing/extending metadata connected to its input port. It can extend, and remove fields based on a specified filter, or based on regular expression matching field names.

# Description
The library supports the following operations:
- Modify CloverDX metadata dynamically

# Features
* Extend metadata with an additional (set) of external metadata
* Extend metadata with 1 metadata record
* Filter out fields using regular expression (both inclusion and exclusion mode available)
* Change metadata (record/field) properties, based on regular expression (e.g. field's type)
* If connected to a live data stream, fields get auto-mapped to allow pass-through
* Use `{propertyName}` placeholders for dynamic property change
* Metadata extension is done first, then filter & property updates (changes get applied to all fields)
* Fields with duplicate names are ignored, the first encountered configuration is kept

# Example Use Cases
Data transformation jobs often need to change the format of the subject data. In CloverDX, such format changes are defined and managed as metadata. Joining multiple data sources together, enriching data with new dimensions, changing field data types to match a target system are all common activities that require new or updated metadata. The MetaDataFactory component simplifies the process of creating such metadata definitions at runtime.

For example, use this component to:
* create metadata to represent the combination of multiple input data sets.
* add a set of standard fields to input metadata , say to add an effective_date and current_flag to implement slowly changing dimension processing.
* change the name and type of specific fields in an input data set, say change every field that ends with "ID", "id" or "Id" to type integer.

The MetaDataFactory component has three properties that are typically used in conjunction in use cases that change definitions of existing metadata fields. For example, the following configuration uses these three properties (`Filter Field`, `Filter mode` and `Properties`) to change the input metatdata for all fields that end with the substring "ID" (regardless of case) to have type long, a default value of 0 and the prefix "typed_" prepended to the field name. 

```
Filter Field = regex:.*[Ii][dD]$
Filter Mode = Change properties
Properties = {type=long, nullValue=0 name=typed_{name}}
```

# Properties
Name: MetadataFactoryLib  
Label: Metadata Factory  
Author: CloverDX  
Version: 1.0.1  
Compatible: CloverDX 6.0 and newer  

# Tags

metadata dynamic alter propagation regex

# <span class='tabLabel'>Documentation</span>

## MetadataFactory

This component converts input metadata to auto-generated output metadata based on the configuration.

### Ports

| Port | Used for | Description |
|----------|-------------|-------------------------------------------------------------------------------|
| Input 0 | Input data | Metadata from the input port used for modification |
| Output 0 | Output data | Auto-propagated metadata based on the input metadata and the configuration |

### Parameters
| Parameter | Description |
|-----------------------|---------------------------------------------------------------------------------|
| Name | Name of the new output metadata |
| Record delimiter | New output metadata record delimiter |
| Field delimiter | New output metadata field delimiter |
| Addition mode | Prepend/Append (adding addition metadata before or after input metadata) |
| Extension metadata URL| Path to external metadata file that is to be added to input metadata |
| Field filter | Filter specific field, regex filter is also allowed in format: e.g. regex: .*[Ii][dD]$ (for 'ID' with lenient case sensitivity) |
| Filter mode | Keep/Remove/Keep similar/Remove similar/Change properties (action performed on fields matching the filter statement) |
| Properties | Define new type, default null value, name, etc. |


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



