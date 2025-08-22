# <span class='tabLabel'>About</span>

Simple wrapper that can make running Python script in CloverDX easier.

# Description

The library supports the following operations:
- Execute a Python script in CloverDX and get the output

A subgraph is used to wrap an ExecuteScript component that calls Python making use of external scripts more user-friendly. The subgraph allows the user to pass a path to the Python script using a parameter and input parameters via input edge. Script output is returned through the output port.
 
# Properties
Name: PythonIntegrationLib  
Label: Python Integration  
Author: CloverDX  
Version: 1.0  
Compatible: CloverDX 5.14 and newer  

# Tags
python execution wrapper script

# <span class='tabLabel'>Documentation</span>

## PythonExecute

A component that wraps the ExecuteScript components and helps to run a Python script.

### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
| Python script URL         | Path to the script which will be executed.  |yes   | *no*    |
| Script charset            | Character set for the input script          | yes | *UTF-8* |
| Python interpreter URL    | Absolute path to the Python interpreter (e.g. /usr/bin/python3)     | yes | *no*    |

### Ports
| Port     | Required  | Used for        | Description                                                                   |
|----------|-----------|-----------------|-------------------------------------------------------------------------------|
| Input 0  | No        | Standard input  | If the Python script expects data on standard input, input 0 is used. |
| Output 0 | Yes       | Standard output | Standard output of the Python script.                                         |
| Output 1 | Yes       | Error output    | Error output, used in cases of runtime error during the script execution.     |

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

A local Python interpreter must be installed to use PythonExecute component. The path to the Python interpreter must be set as an attribute of the component. 