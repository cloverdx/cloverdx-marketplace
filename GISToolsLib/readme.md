# <span class='tabLabel'>About</span>

Tools handy when working with shape files and with geo coordinates in general.

The library provides ESRI Shape file extraction capability and a component able to filter shapes by their mutual position (intersecting, containing fully, or a combination of the two).

# Description
The library supports the following operations:
- Read geometry data from shape files (.shp)
- Filter shapes intersections

# Properties
Name: GISToolsLib  
Label: GIS Tools  
Author: CloverDX  
Version: 1.0  
Compatible: CloverDX 5.14 and newer  

# Tags

gis esri shapefile geo coordinates reader


# <span class='tabLabel'>Documentation</span>

## ShapeFileReader

Reads geometry data from shape files (.shp) and optionally reads shape attribute data from a .dbf file.

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Output 0 | Shape geometry data (.shp) |
| Output 1 (optional)| Shape attributes data (.dbf) |

Metadata is set and propagated out of the component on output port 0.

Metadata on output port 1 is expected to be provided by the user. Use the "Extract metadata from DBF file" option in the new metadata context action (right-click an edge or the metadata section in the outline) in Designer to generate the metadata.

### Parameters

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| Shapefile URL | Provide URL to the shapefile you wish to read. If output port 1 is connected, a .dbf file of the same name in the same location will be read too. | true | basic | file picker | Only files. |

## ShapeIntersect

Filters shapes from input port 0 based on chosen condition (intersects, is contained in, has no relation) against the set of shapes read from input port 1.

### Ports & metadata

| Port     | Used for           |
|----------|--------------------|
| Input 0 | Master port shape records |
| Input 1 | Slave port shape records |
| Output 0 | Records from master port that fulfill the filtering condition |
| Output 1 (optional)| Records from master port that do not fulfill the filtering condition |

Input ports accept any metadata, however, each needs to contain at least one list of list number types holding the shape geometry data. Metadata from input port 0 is propagated to output ports 0 and 1.

### Properties

| Parameter label | Description | Required? | Category | Editor type | Editor type details |
| --- | --- | --- | --- | --- | --- |
| Shape key | Select the fields holding the shape geometry information that should be compared. Format: $field1=$field2 | true | basic |   |   |
| Keep only | Select comparison condition. | false | basic | enumeration | Options (value&#124;label):<br>0&#124;Intersecting<br>1&#124;Contained (IN-1 contains IN-0)<br>2&#124;Intersecting or contained<br>3&#124;Unrelated<br> |
| Input 0 shape field | Select the field that holds shape type for input port 0 records. | false | advanced | metadata field |   |
| Input 1 shape field | Select the field that holds shape type for input port 1 records. | false | advanced | metadata field |   |
| Input 0 shape | Provide the default shape type for input port 0 records. | false | basic | enumeration | Options (value&#124;label):<br>point&#124;Point<br>polyline&#124;Polyline<br>polygon&#124;Polygon<br>multipoint&#124;Multipoint<br> |
| Input 1 shape | Provide the default shape type for input port 1 records. | false | basic | enumeration | Options (value&#124;label):<br>point&#124;Point<br>polyline&#124;Polyline<br>polygon&#124;Polygon<br>multipoint&#124;Multipoint<br> |

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
