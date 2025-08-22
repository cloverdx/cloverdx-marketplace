# <span class='tabLabel'>About</span>

This library provides functionality to read and parse `.ics` files (calendar files used by platforms such as Google Calendar or Outlook).

# Description
The library performs the following operations:
- Reads and parses `.ics` (iCalendar) files.
- Extracts calendar and event details from the `.ics` file.

The component outputs all timestamps as strings since multiple time zones could be defined within the `.ics` file.

# Properties
Name: ICSReaderLib  
Label: ICS Reader  
Author: CloverDX  
Version: 1.1  
Compatible: CloverDX 6.0 and newer  

# Tags

reader ical xml conversion xslt vCalendar vEvent

# <span class='tabLabel'>Documentation</span>

## ICSReader

Reader component, which reads an input ICS file and produces parsed output.

### Attributes

| Parameter name | Description | Required | Default Value|
| :--- | :--- | :--- | :--- |
|File URL |Parameter with a path to ICS file |yes | *no*|

### Ports
| Port     | Required  | Used for              | Description                                                      |
|----------|-----------|-----------------------|------------------------------------------------------------------|
| Input 0  | No        | Unused                |                                                                  |
| Output 0 | Yes       | Calendar information  | Returns calendar info based on the parsed ICS file (vcalendar).  |
| Output 1 | Yes       | Events information    | Returns event info based on the parsed ICS file (vevent).        |

### Metadata for Calendar Information

The calendar details are output as defined by the following CloverDX metadata:

| Field Name         | Description                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| **prodid**         | The identifier for the product that created the calendar.                    |
| **version**        | The iCalendar version being used (e.g., 2.0).                                |
| **calscale**       | The calendar scale used (typically "GREGORIAN").                             |
| **method**         | The method associated with the calendar (e.g., "PUBLISH" or "REQUEST").      |
| **x_wr_calname**   | The display name of the calendar.                                            |
| **x_wr_timezone**  | The time zone used for the calendar events.                                  |
| **x_wr_caldesc**   | A brief description of the calendar.                                         |
| **tzid**           | The time zone identifier associated with the calendar.                       |
| **x_lic_location** | The location related to the license of the calendar.                         |
| **tzoffsetfrom**   | The time zone offset for the calendar from a standard time.                  |
| **tzoffsetto**     | The time zone offset for the calendar to a standard time.                    |
| **tzname**         | The name of the time zone.                                                   |
| **dtstart**        | The date and time when the calendar starts.                                  |
| **rrule**          | The recurrence rule applied to the calendar (if any).                        |

### Metadata for Event Information

The event details are output as defined by the following CloverDX metadata:

| Field Name       | Description                                                                 |
|------------------|-----------------------------------------------------------------------------|
| **dtstart**      | The start date and time of the event, as a string.                           |
| **dtend**        | The end date and time of the event, as a string.                             |
| **dtstamp**      | The timestamp of when the event data was last updated, as a string.          |
| **uid**          | The unique identifier for the event.                                         |
| **created**      | The timestamp indicating when the event was created, as a string.            |
| **description**  | A description or note associated with the event.                             |
| **last_modified**| The timestamp of the most recent modification to the event, as a string.     |
| **location**     | The location where the event will take place, as a string.                   |
| **sequence**     | A number representing the revision of the event.                             |
| **status**       | The status of the event (e.g., confirmed, tentative, or canceled).           |
| **summary**      | A brief summary or title of the event.                                       |
| **transp**       | Transparency of the event, indicating if it blocks time (e.g., opaque, transparent). |
| **rules**        | A map of custom rules associated with the event (recurrence rules, etc.).    |

# <span class='tabLabel'>Installation & Setup</span>

### Online installation (Server connected to Internet)

1. In Server Console, navigate to Libraries > Install library from repository
2. Select Library Repository dropdown > CloverDX Marketplace
3. Check the box next to the libraries you want to install (if there are any dependencies, you can install all of them once - see Requirements above)
4. Click Install

### Offline installation (Server without Internet connection)

1. Download the library you need from the CloverDX Marketplace. You should get a ".clib" file.
2. Transfer the ".clib" file to your offline Server machine (USB stick, ...).
3. In Server Console, navigate to Libraries > Install library from repository > Down arrow for more options > Browse local files...
4. Select the downloaded .clib file on your disk and install.