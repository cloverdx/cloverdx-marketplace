# <span class='tabLabel'>About</span>

Sample Northwind Data Warehouse. Provides a handful of CloverDX Wrangler Data Sources for learning purposes.

# Description

This library is an example of Data Sources implementations which can be used in CloverDX Wrangler.  
Some DataSources are just 1:1 wrappers of underlying DB tables. Others represent more complex SQL queries which combine several tables into denormalized flat structures which are easier to be used by business users.  
Handling DataSource preview and also user configurable DataSource filters is shown/implemented in some of the connectors.

## Northwind ERD

Full E-R schema/diagram of the database with relationships among tables can be found in `icons/northwind-ERD.png`.

## Tables are as follows:

### Customers 
List of 91 customers with information such as customerid, name, address, phone, etc.

### Employees
List of 9 employees with information such as employeeid, first_name, birth_date, hire_date, address, etc.

### EmployeesByRevenue
List of 9 employees with additional information like total_revenue and total_number_of_orders per employee

### Orders
List of 830 Orders with information such as order_id, customer_id shipped date, ship_address, quantity, unit_price, etc.

### Products
List of 77 Products with information such as product_id, product_name unit_price, supplier_id, etc. 

### TotalOrdersByCustomers
List of 830 orders with additional information such as num_products and order_total_value

# Properties
Name:		Northwind Demo  
Label:		Northwind Demo Database Wrapper  
Author:		CloverDX  
Version:	1.5.1  
Compatible:	CloverDX 6.0 and newer  

# Tags
template database source connectors wrangler demo DWH DataLake

# <span class='tabLabel'>Installation & setup</span>

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

Make sure to configure this library with appropriate JDBC URL and username + password with enough access rights *(create table,alter table,select,insert,update...)*.  
PostgreSQL database is expected, however other *(MySQL, Oracle, MSSQL)* should work as well. 

*Hint: the JDBC URL should be in following shape: `jdbc:postgresql://your_server_name_or_ip/your_database_name`*

Example:

  *  `jdbc:postgresql://localhost/northwind`  
  *referencing PostgreSQL on your local machine and database named "northwind"*



*Note: if you are using different database than PostgreSQL, you have to update the JDBC URL accordingly.*

This library contains init graph (`graph/init-db.grf`) which creates DB tables and populates them with data. The graph actually just executes a SQL script.  
The script expects PostgreSQL database. Other database provider can be used, but the script may not work properly.
*(In case  you would like to modify the script, it is in `data-in/northwind.sql`)*

In order to create a new PostgreSQL database for this library, you can run:


`CREATE DATABASE northwind OWNER nwuser`

### Dependencies
There are no dependencies on other libraries. The only dependency is that relational database (prefferably PostgreSQL) is required as primary source of data.

# Customization 

You can change any and all aspects of this library. It serves as an example of wrapper on top of local (or remote) database - DataWarehouse / DataLake - being made available to business users through Wrangler.  
